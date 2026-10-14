import { useEffect, useRef, useState } from 'react';
import {
  AudioMutedOutlined,
  CaretRightOutlined,
  CustomerServiceOutlined,
  LoadingOutlined,
  SoundOutlined,
  NodeIndexOutlined,
  PauseOutlined,
  ReadOutlined,
  RetweetOutlined,
  SoundFilled,
  StepBackwardFilled,
  StepForwardFilled,
  UnorderedListOutlined,
} from '@ant-design/icons';
import { Slider } from 'antd';
import { musicConfig, type MusicTrack } from '../config/site';

type PlayMode = 'list' | 'one' | 'random';
type LrcStatus = 'none' | 'loading' | 'loaded' | 'failed';

interface LyricLine {
  time: number;
  text: string;
}

const VOLUME_KEY = 'music-player-volume';
const MODE_KEY = 'music-player-mode';
const MODE_ORDER: PlayMode[] = ['list', 'one', 'random'];
const MODE_LABEL: Record<PlayMode, string> = { list: '列表循环', one: '单曲循环', random: '随机播放' };

function formatTime(s: number): string {
  if (!isFinite(s) || s < 0) return '0:00';
  const m = Math.floor(s / 60);
  const sec = Math.floor(s % 60);
  return `${m}:${sec.toString().padStart(2, '0')}`;
}

/** 解析 LRC 文本为按时间排序的歌词行（兼容 .mm 与 .ms 两种毫秒位宽） */
function parseLRC(text: string): LyricLine[] {
  const timeReg = /\[(\d{2}):(\d{2})\.(\d{2,3})\]/g;
  const result: LyricLine[] = [];
  for (const line of text.split('\n')) {
    const stamps = [...line.matchAll(timeReg)];
    if (!stamps.length) continue;
    const content = line.replace(timeReg, '').trim();
    if (!content) continue;
    for (const m of stamps) {
      const time = parseInt(m[1]) * 60 + parseInt(m[2]) + parseInt(m[3]) / 10 ** m[3].length;
      result.push({ time, text: content });
    }
  }
  return result.sort((a, b) => a.time - b.time);
}

/** 拉取 Meting 在线歌单：主 API 失败后依次尝试备用 API，字段兼容 title/name、author/artist 两种返回 */
async function fetchMetingPlaylist(): Promise<MusicTrack[]> {
  const m = musicConfig.meting;
  const apis = [m.api, ...(m.fallbackApis ?? [])];
  for (const base of apis) {
    try {
      const url = base
        .replace(':server', m.server)
        .replace(':type', m.type)
        .replace(':id', m.id)
        .replace(':r', String(Math.random()));
      const res = await fetch(url);
      if (!res.ok) throw new Error(`HTTP ${res.status}`);
      const data = await res.json();
      if (Array.isArray(data) && data.length > 0) {
        return data.map((item: Record<string, string>) => ({
          name: item.title || item.name || '未知曲目',
          artist: item.author || item.artist || '未知歌手',
          url: item.url,
          cover: item.pic || item.cover || undefined,
          lrc: item.lrc || undefined,
        }));
      }
    } catch (e) {
      console.warn('[music] Meting API 失败，尝试下一个:', base, e);
    }
  }
  throw new Error('所有 Meting API 均不可用');
}

/**
 * 音乐播放器：全局单例 audio（面板关了音乐不停），导航栏图标 + 悬浮面板。
 * 功能对齐 Firefly 主题：三种播放模式、LRC 歌词同步、旋转封面、音量记忆、失败自动跳曲。
 */
export default function MusicPlayer({ panelOpen, onTogglePanel }: { panelOpen: boolean; onTogglePanel: () => void }) {
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const loadVerRef = useRef(0); // 换曲版本号：丢弃过期的 play()/歌词异步回调
  const failedVerRef = useRef(-1); // 已处理过失败的换曲版本号（play() reject 与 error 事件会重复上报）
  const errStreakRef = useRef(0); // 连续失败次数，超过曲单长度则停止跳曲
  const userScrollingRef = useRef(false);
  const scrollTimerRef = useRef<number | undefined>(undefined);
  const lrcBoxRef = useRef<HTMLDivElement | null>(null);

  // 音频事件回调需要读到的最新值
  const playlistRef = useRef<MusicTrack[]>([]);
  const indexRef = useRef(0);
  const modeRef = useRef<PlayMode>('list');
  const lyricsRef = useRef<LyricLine[]>([]);
  const lrcIndexRef = useRef(-1);
  const playIndexRef = useRef<(i: number, auto: boolean) => void>(() => {});
  const autoStepRef = useRef<(dir: 1 | -1) => void>(() => {});
  const handleFailedRef = useRef<() => void>(() => {});

  const [playlist, setPlaylist] = useState<MusicTrack[]>([]);
  const [index, setIndex] = useState(0);
  const [playing, setPlaying] = useState(false);
  const [mode, setMode] = useState<PlayMode>(() => {
    const saved = localStorage.getItem(MODE_KEY) as PlayMode | null;
    return saved === 'list' || saved === 'one' || saved === 'random' ? saved : musicConfig.playMode;
  });
  const [volume, setVolume] = useState(() => {
    const saved = parseFloat(localStorage.getItem(VOLUME_KEY) ?? '');
    return isFinite(saved) ? Math.min(1, Math.max(0, saved)) : musicConfig.volume;
  });
  const [muted, setMuted] = useState(false);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [lyrics, setLyrics] = useState<LyricLine[]>([]);
  const [lrcIndex, setLrcIndex] = useState(-1);
  const [lrcStatus, setLrcStatus] = useState<LrcStatus>('none');
  const [initLoading, setInitLoading] = useState(true);
  const [error, setError] = useState('');
  const [drawer, setDrawer] = useState<'none' | 'lrc' | 'list'>('none');

  playlistRef.current = playlist;
  indexRef.current = index;
  modeRef.current = mode;
  lyricsRef.current = lyrics;
  lrcIndexRef.current = lrcIndex;

  /** 换曲：设 src、拉歌词；auto 为 true 时自动开播 */
  const playIndex = (i: number, auto: boolean) => {
    const audio = audioRef.current;
    const list = playlistRef.current;
    if (!audio || i < 0 || i >= list.length) return;
    const ver = ++loadVerRef.current;
    setIndex(i);
    setCurrentTime(0);
    setDuration(0);
    setLrcIndex(-1);
    setError('');
    const track = list[i];
    audio.src = track.url;

    setLyrics([]);
    if (track.lrc) {
      setLrcStatus('loading');
      fetch(track.lrc)
        .then((r) => (r.ok ? r.text() : Promise.reject(new Error(`HTTP ${r.status}`))))
        .then((text) => {
          if (ver !== loadVerRef.current) return; // 已切歌，丢弃过期歌词
          const lines = parseLRC(text);
          setLyrics(lines);
          setLrcStatus(lines.length > 0 ? 'loaded' : 'none');
        })
        .catch(() => {
          if (ver === loadVerRef.current) setLrcStatus('failed');
        });
    } else {
      setLrcStatus('none');
    }

    if (auto) {
      audio.play().catch((e: DOMException) => {
        if (ver !== loadVerRef.current || e.name === 'AbortError') return;
        handleFailedRef.current();
      });
    } else {
      setPlaying(false);
    }
  };
  playIndexRef.current = playIndex;

  /** 顺序/随机切曲（事件回调与按钮共用） */
  const autoStep = (dir: 1 | -1) => {
    const list = playlistRef.current;
    const len = list.length;
    if (!len) return;
    let ni: number;
    if (modeRef.current === 'random' && len > 1) {
      do {
        ni = Math.floor(Math.random() * len);
      } while (ni === indexRef.current);
    } else {
      ni = (indexRef.current + dir + len) % len;
    }
    playIndexRef.current(ni, true);
  };
  autoStepRef.current = autoStep;

  /** 播放失败处理：自动跳下一首，整轮都失败才停下提示 */
  const handleFailed = () => {
    if (failedVerRef.current === loadVerRef.current) return; // play() reject 与 error 事件只处理一次
    failedVerRef.current = loadVerRef.current;
    errStreakRef.current += 1;
    const len = playlistRef.current.length;
    if (musicConfig.autoSkipOnError && len > 1 && errStreakRef.current < len) {
      autoStepRef.current(1);
    } else {
      setPlaying(false);
      setError('播放失败，请稍后重试');
    }
  };
  handleFailedRef.current = handleFailed;

  // 单例 audio：事件只绑一次，回调经 ref 读最新状态
  useEffect(() => {
    const audio = new Audio();
    audio.preload = 'metadata';
    audio.addEventListener('play', () => {
      errStreakRef.current = 0;
      setError('');
      setPlaying(true);
    });
    audio.addEventListener('pause', () => setPlaying(false));
    audio.addEventListener('loadedmetadata', () => setDuration(isFinite(audio.duration) ? audio.duration : 0));
    audio.addEventListener('timeupdate', () => {
      setCurrentTime(audio.currentTime);
      const lines = lyricsRef.current;
      if (lines.length) {
        let idx = -1;
        for (let i = 0; i < lines.length; i++) {
          if (audio.currentTime >= lines[i].time) idx = i;
          else break;
        }
        setLrcIndex(idx); // 同值 setState 不会触发多余渲染
      }
    });
    audio.addEventListener('ended', () => {
      if (modeRef.current === 'one') {
        audio.currentTime = 0;
        void audio.play();
        return;
      }
      autoStepRef.current(1);
    });
    audio.addEventListener('error', () => {
      if (!audio.getAttribute('src')) return; // 卸载时移除 src 触发的空错误忽略
      handleFailedRef.current();
    });
    audioRef.current = audio;

    // 初始化歌单：meting 在线拉取，local 直接读取
    (async () => {
      try {
        const list = musicConfig.mode === 'meting' ? await fetchMetingPlaylist() : musicConfig.local.playlist;
        if (!list.length) throw new Error('歌单为空');
        setPlaylist(list);
        setInitLoading(false);
        const start = modeRef.current === 'random' && list.length > 1 ? Math.floor(Math.random() * list.length) : 0;
        playIndexRef.current(start, false);
      } catch (e) {
        console.warn('[music] 歌单初始化失败:', e);
        setInitLoading(false);
        setError('歌单加载失败，请检查网络后刷新');
      }
    })();

    return () => {
      audio.pause();
      audio.removeAttribute('src');
      audioRef.current = null;
      window.clearTimeout(scrollTimerRef.current);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // 音量 / 静音同步到音频元素并持久化
  useEffect(() => {
    const audio = audioRef.current;
    if (audio) {
      audio.volume = volume;
      audio.muted = muted;
    }
    localStorage.setItem(VOLUME_KEY, String(volume));
  }, [volume, muted]);

  // 播放模式持久化
  useEffect(() => {
    localStorage.setItem(MODE_KEY, mode);
  }, [mode]);

  // 歌词跟随：高亮行变化时自动居中（用户手动滚动期间暂停，见 markUserScroll）
  useEffect(() => {
    if (drawer !== 'lrc' || lrcIndex < 0 || userScrollingRef.current) return;
    lrcBoxRef.current?.querySelector(`[data-i="${lrcIndex}"]`)?.scrollIntoView({ behavior: 'smooth', block: 'center' });
  }, [lrcIndex, drawer]);

  /** 用户手动滚动歌词后暂停跟随，停止滚动 3 秒回弹到当前行 */
  const markUserScroll = () => {
    userScrollingRef.current = true;
    window.clearTimeout(scrollTimerRef.current);
    scrollTimerRef.current = window.setTimeout(() => {
      userScrollingRef.current = false;
      const line = lrcBoxRef.current?.querySelector(`[data-i="${lrcIndexRef.current}"]`);
      line?.scrollIntoView({ behavior: 'auto', block: 'center' });
    }, 3000);
  };

  const togglePlay = () => {
    const audio = audioRef.current;
    if (!audio || !playlist.length) return;
    if (audio.paused) audio.play().catch(() => handleFailedRef.current());
    else audio.pause();
  };

  const playAt = (i: number) => {
    if (i === index) togglePlay();
    else playIndexRef.current(i, true);
  };

  const cycleMode = () => setMode((m) => MODE_ORDER[(MODE_ORDER.indexOf(m) + 1) % MODE_ORDER.length]);

  const toggleDrawer = (d: 'lrc' | 'list') => setDrawer((cur) => (cur === d ? 'none' : d));

  const onSeek = (e: React.MouseEvent<HTMLDivElement>) => {
    const audio = audioRef.current;
    if (!audio || !duration) return;
    const rect = e.currentTarget.getBoundingClientRect();
    const ratio = Math.min(1, Math.max(0, (e.clientX - rect.left) / rect.width));
    audio.currentTime = ratio * duration;
    setCurrentTime(audio.currentTime);
  };

  const seekToTime = (t: number) => {
    const audio = audioRef.current;
    if (!audio || !duration) return;
    audio.currentTime = Math.min(Math.max(0, t), duration);
  };

  const current = playlist[index];
  const progressPct = duration > 0 ? (currentTime / duration) * 100 : 0;
  const modeIcon =
    mode === 'random' ? (
      <NodeIndexOutlined />
    ) : mode === 'one' ? (
      <span className="mode-one">
        <RetweetOutlined />
        <i>1</i>
      </span>
    ) : (
      <RetweetOutlined />
    );

  return (
    <>
      <span
        className={`nav-icon-btn music-trigger${playing ? ' playing' : ''}`}
        title="音乐"
        onClick={onTogglePanel}
      >
        <CustomerServiceOutlined style={{ fontSize: 18 }} />
      </span>

      {panelOpen && (
        <div
          className="float-panel music-panel fade-in"
          style={{ top: 'calc(var(--nav-h) + 8px)', right: 16 }}
          onClick={(e) => e.stopPropagation()}
        >
          {initLoading && (
            <div className="music-loading">
              <LoadingOutlined spin />
            </div>
          )}

          {/* 封面 + 曲目信息 + 歌词开关 */}
          <div className="music-head">
            <div className="music-cover-box">
              {current?.cover && (
                <img
                  key={current.cover}
                  className="music-cover"
                  src={current.cover}
                  alt=""
                  style={{ animationPlayState: playing ? 'running' : 'paused' }}
                  onError={(e) => (e.currentTarget.style.visibility = 'hidden')}
                />
              )}
              <SoundOutlined className="music-cover-fallback" />
            </div>
            <div className="music-meta">
              <div className="music-title" title={current?.name}>
                {error || (current ? current.name : '加载中…')}
              </div>
              <div className="music-artist" title={current?.artist}>
                {current?.artist ?? ''}
              </div>
            </div>
            <span
              className={`music-mini-btn${drawer === 'lrc' ? ' active' : ''}`}
              title="歌词"
              onClick={() => toggleDrawer('lrc')}
            >
              <ReadOutlined style={{ fontSize: 15 }} />
            </span>
          </div>

          {/* 时间 + 静音 + 音量 */}
          <div className="music-sub">
            <span className="music-time">
              {formatTime(currentTime)} / {formatTime(duration)}
            </span>
            <span
              className={`music-mini-btn${muted ? ' active' : ''}`}
              title={muted ? '取消静音' : '静音'}
              onClick={() => setMuted((m) => !m)}
            >
              {muted || volume === 0 ? <AudioMutedOutlined /> : <SoundFilled />}
            </span>
            <Slider
              className="music-vol"
              min={0}
              max={1}
              step={0.02}
              value={muted ? 0 : volume}
              tooltip={{ open: false }}
              onChange={(v) => {
                setVolume(v);
                setMuted(false);
              }}
            />
          </div>

          {/* 进度条 */}
          <div className="music-progress" onClick={onSeek}>
            <div className="played" style={{ width: `${progressPct}%` }} />
            <div className="music-progress-thumb" style={{ left: `${progressPct}%` }} />
          </div>

          {/* 播放模式 / 上一首 / 播放 / 下一首 / 播放列表 */}
          <div className="music-controls">
            <span
              className={`music-ctrl-btn${mode !== 'list' ? ' active' : ''}`}
              title={`播放模式：${MODE_LABEL[mode]}`}
              onClick={cycleMode}
            >
              {modeIcon}
            </span>
            <StepBackwardFilled className="music-ctrl-btn big" onClick={() => autoStepRef.current(-1)} />
            <div className="play" onClick={togglePlay}>
              {playing ? <PauseOutlined /> : <CaretRightOutlined />}
            </div>
            <StepForwardFilled className="music-ctrl-btn big" onClick={() => autoStepRef.current(1)} />
            <span
              className={`music-ctrl-btn${drawer === 'list' ? ' active' : ''}`}
              title="播放列表"
              onClick={() => toggleDrawer('list')}
            >
              <UnorderedListOutlined />
            </span>
          </div>

          {/* 歌词抽屉 */}
          <div className={`music-drawer${drawer === 'lrc' ? ' open' : ''}`}>
            <div className="music-drawer-inner">
              <div className="music-lrc" ref={lrcBoxRef} onWheel={markUserScroll} onTouchStart={markUserScroll}>
                {lrcStatus === 'loading' && <div className="music-lrc-empty">歌词加载中…</div>}
                {lrcStatus === 'failed' && <div className="music-lrc-empty">歌词加载失败</div>}
                {lrcStatus === 'none' && <div className="music-lrc-empty">纯音乐，请欣赏~</div>}
                {lrcStatus === 'loaded' &&
                  lyrics.map((l, i) => (
                    <div
                      key={i}
                      data-i={i}
                      className={`music-lrc-line${i === lrcIndex ? ' active' : ''}`}
                      onClick={() => seekToTime(l.time)}
                    >
                      {l.text}
                    </div>
                  ))}
              </div>
            </div>
          </div>

          {/* 播放列表抽屉 */}
          <div className={`music-drawer${drawer === 'list' ? ' open' : ''}`}>
            <div className="music-drawer-inner">
              <div className="music-list">
                {playlist.map((song, i) => (
                  <div key={i} className={`music-list-row${i === index ? ' active' : ''}`} onClick={() => playAt(i)}>
                    <div className="cover">
                      {song.cover ? <img src={song.cover} alt="" loading="lazy" /> : <SoundOutlined />}
                      {i === index && (
                        <div className="overlay">
                          {playing ? (
                            <span className="eq-bars">
                              <i />
                              <i />
                              <i />
                            </span>
                          ) : (
                            <CaretRightOutlined style={{ fontSize: 13 }} />
                          )}
                        </div>
                      )}
                    </div>
                    <div className="info">
                      <span className="t">{song.name}</span>
                      <span className="a">{song.artist}</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
