import { useEffect, useRef, useState } from 'react';
import {
  CaretRightOutlined,
  PauseOutlined,
  StepBackwardFilled,
  StepForwardFilled,
  CustomerServiceOutlined,
  SoundFilled,
} from '@ant-design/icons';
import { Slider } from 'antd';
import { musicConfig } from '../config/site';

/**
 * 音乐播放器：全局单例 audio，导航栏图标 + 悬浮面板。
 */
export default function MusicPlayer({ panelOpen, onTogglePanel }: { panelOpen: boolean; onTogglePanel: () => void }) {
  const audioRef = useRef<HTMLAudioElement | null>(null);
  const [index, setIndex] = useState(0);
  const [playing, setPlaying] = useState(false);
  const [progress, setProgress] = useState(0);
  const [duration, setDuration] = useState(0);
  const [volume, setVolume] = useState(musicConfig.volume);
  const [error, setError] = useState(false);

  const playlist = musicConfig.playlist;
  const current = playlist[index];

  useEffect(() => {
    const audio = new Audio();
    audio.volume = musicConfig.volume;
    audio.preload = 'none';
    audio.addEventListener('timeupdate', () => setProgress(audio.currentTime));
    audio.addEventListener('loadedmetadata', () => setDuration(audio.duration));
    audio.addEventListener('ended', () => next());
    audio.addEventListener('play', () => setPlaying(true));
    audio.addEventListener('pause', () => setPlaying(false));
    audioRef.current = audio;
    return () => {
      audio.pause();
      audioRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    const audio = audioRef.current;
    if (!audio || !current) return;
    const wasPlaying = playing;
    audio.src = current.url;
    setError(false);
    setProgress(0);
    setDuration(0);
    if (wasPlaying) {
      audio.play().catch(() => setError(true));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [index]);

  const play = () => {
    const audio = audioRef.current;
    if (!audio || !current) return;
    if (playing) {
      audio.pause();
      return;
    }
    audio.play().catch(() => setError(true));
  };

  const prev = () => setIndex((i) => (i - 1 + playlist.length) % playlist.length);
  const next = () => setIndex((i) => (i + 1) % playlist.length);

  const seek = (ratio: number) => {
    const audio = audioRef.current;
    if (audio && duration) audio.currentTime = ratio * duration;
  };

  const fmt = (s: number) => {
    if (!isFinite(s)) return '0:00';
    const m = Math.floor(s / 60);
    const sec = Math.floor(s % 60);
    return `${m}:${sec.toString().padStart(2, '0')}`;
  };

  return (
    <>
      <span
        onClick={onTogglePanel}
        style={{
          width: 40,
          height: 40,
          borderRadius: 10,
          display: 'inline-flex',
          alignItems: 'center',
          justifyContent: 'center',
          cursor: 'pointer',
          background: panelOpen || playing ? 'var(--primary-weak)' : 'transparent',
          color: playing ? 'var(--primary)' : 'var(--text-main)',
          transition: 'background .2s',
        }}
        title="音乐"
      >
        <CustomerServiceOutlined style={{ fontSize: 18 }} />
      </span>

      {panelOpen && (
        <div className="float-panel music-panel" style={{ top: 'calc(var(--nav-h) + 8px)', right: 16 }} onClick={(e) => e.stopPropagation()}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div style={{ minWidth: 0 }}>
              <div className="song">{error ? '播放失败，试试下一首叭' : current.name}</div>
              <div className="artist">{current.artist}</div>
            </div>
            <SoundFilled style={{ color: 'var(--primary)', fontSize: 18 }} />
          </div>

          <div className="music-progress" onClick={(e) => {
            const rect = e.currentTarget.getBoundingClientRect();
            seek((e.clientX - rect.left) / rect.width);
          }}>
            <div className="played" style={{ width: `${duration ? (progress / duration) * 100 : 0}%` }} />
          </div>
          <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 11.5, color: 'var(--text-faint)' }}>
            <span>{fmt(progress)}</span>
            <span>{fmt(duration)}</span>
          </div>

          <div className="music-controls">
            <StepBackwardFilled onClick={prev} style={{ cursor: 'pointer' }} />
            <div className="play" onClick={play}>
              {playing ? <PauseOutlined /> : <CaretRightOutlined />}
            </div>
            <StepForwardFilled onClick={next} style={{ cursor: 'pointer' }} />
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 10 }}>
            <SoundFilled style={{ color: 'var(--text-faint)', fontSize: 13 }} />
            <Slider
              style={{ flex: 1, margin: 0 }}
              min={0}
              max={1}
              step={0.05}
              value={volume}
              tooltip={{ open: false }}
              onChange={(v) => {
                setVolume(v);
                if (audioRef.current) audioRef.current.volume = v;
              }}
            />
          </div>

          <div className="music-list">
            {playlist.map((song, i) => (
              <div
                key={i}
                className={`row ${i === index ? 'playing' : ''}`}
                onClick={() => setIndex(i)}
              >
                <span>{song.name}</span>
                <span>{song.artist}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </>
  );
}
