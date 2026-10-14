import { useState } from 'react';
import { Button, Modal, Space, message } from 'antd';
import { QrcodeOutlined, CopyOutlined, LinkOutlined } from '@ant-design/icons';
import QRCode from 'qrcode';
import { articleApi } from '../api/article';

/**
 * 分享按钮组：QQ / 微信（二维码）/ 复制链接，点击均上报分享数。
 */
export default function ShareButtons({ articleId, title }: { articleId: number; title: string }) {
  const [qrOpen, setQrOpen] = useState(false);
  const [qrData, setQrData] = useState('');

  const url = location.origin + `/article/${articleId}`;

  const report = (channel: string) => {
    articleApi.share(articleId, channel).catch(() => {});
  };

  const shareQQ = () => {
    report('qq');
    const q = new URLSearchParams({
      url,
      title,
      summary: '来自 迷失客客の博客 的分享捏~',
    });
    window.open(`https://sns.qzone.qq.com/cgi-bin/qzshare/cgi_qzshare_onekey?${q}`, '_blank', 'width=600,height=560');
  };

  const shareWechat = async () => {
    report('wechat');
    const dataUrl = await QRCode.toDataURL(url, { width: 220, margin: 1, color: { dark: '#80a942' } });
    setQrData(dataUrl);
    setQrOpen(true);
  };

  const copyLink = async () => {
    report('copy');
    await navigator.clipboard.writeText(url);
    message.success('链接已复制捏~');
  };

  return (
    <Space wrap>
      <Button icon={<LinkOutlined />} onClick={shareQQ}>QQ 好友</Button>
      <Button icon={<QrcodeOutlined />} onClick={shareWechat}>微信扫码</Button>
      <Button icon={<CopyOutlined />} onClick={copyLink}>复制链接</Button>
      <Modal
        open={qrOpen}
        title="微信扫一扫，分享给好友"
        footer={null}
        onCancel={() => setQrOpen(false)}
        centered
      >
        <div style={{ textAlign: 'center', padding: 8 }}>
          <img src={qrData} alt="微信二维码" style={{ borderRadius: 12 }} />
          <p style={{ color: 'var(--text-sub)', marginTop: 12 }}>{title}</p>
        </div>
      </Modal>
    </Space>
  );
}
