import { useEffect, useState } from 'react';
import { message, Modal, Slider } from 'antd';
import Cropper, { Area, Point } from 'react-easy-crop';

const OUTPUT_SIZE = 512;
const MAX_FILE_SIZE = 5 * 1024 * 1024;

interface AvatarCropperProps {
  imageUrl: string;
  uploading: boolean;
  onCancel: () => void;
  onConfirm: (file: File) => Promise<void>;
}

/** 头像裁剪弹窗：拖动选择显示区域，确认后输出正方形 PNG。 */
export default function AvatarCropper({ imageUrl, uploading, onCancel, onConfirm }: AvatarCropperProps) {
  const [crop, setCrop] = useState<Point>({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
  const [croppedAreaPixels, setCroppedAreaPixels] = useState<Area | null>(null);
  const [processing, setProcessing] = useState(false);
  const busy = processing || uploading;

  useEffect(() => {
    setCrop({ x: 0, y: 0 });
    setZoom(1);
    setCroppedAreaPixels(null);
  }, [imageUrl]);

  const confirm = async () => {
    if (!croppedAreaPixels || busy) return;
    setProcessing(true);
    try {
      const file = await createAvatarFile(imageUrl, croppedAreaPixels);
      await onConfirm(file);
    } catch (e: any) {
      message.error(e?.message || '图片处理失败，请重新选择');
    } finally {
      setProcessing(false);
    }
  };

  return (
    <Modal
      open
      title="调整头像"
      width={520}
      okText="确认上传"
      cancelText="取消"
      confirmLoading={busy}
      maskClosable={false}
      closable={!busy}
      keyboard={!busy}
      cancelButtonProps={{ disabled: busy }}
      okButtonProps={{ disabled: !croppedAreaPixels || busy }}
      onOk={confirm}
      onCancel={() => { if (!busy) onCancel(); }}
    >
      <div style={{ position: 'relative', height: 360, overflow: 'hidden', borderRadius: 8, background: '#202124' }}>
        <Cropper
          image={imageUrl}
          crop={crop}
          zoom={zoom}
          aspect={1}
          cropShape="round"
          showGrid={false}
          minZoom={1}
          maxZoom={3}
          onCropChange={setCrop}
          onZoomChange={setZoom}
          onCropComplete={(_, areaPixels) => setCroppedAreaPixels(areaPixels)}
        />
      </div>
      <div style={{ marginTop: 18 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--text-sub)', fontSize: 13 }}>
          <span>头像缩放</span>
          <span>{Math.round(zoom * 100)}%</span>
        </div>
        <Slider
          min={1}
          max={3}
          step={0.01}
          value={zoom}
          disabled={busy}
          onChange={setZoom}
          tooltip={{ formatter: (value) => `${Math.round((value || 1) * 100)}%` }}
        />
      </div>
      <p style={{ margin: '4px 0 0', color: 'var(--text-faint)', fontSize: 12 }}>
        拖动图片调整显示区域，也可以使用鼠标滚轮缩放。
      </p>
    </Modal>
  );
}

async function createAvatarFile(imageUrl: string, area: Area): Promise<File> {
  const image = new Image();
  image.src = imageUrl;
  try {
    await image.decode();
  } catch {
    throw new Error('图片读取失败，请重新选择');
  }

  const canvas = document.createElement('canvas');
  canvas.width = OUTPUT_SIZE;
  canvas.height = OUTPUT_SIZE;
  const context = canvas.getContext('2d');
  if (!context) throw new Error('当前浏览器无法处理图片');

  context.drawImage(
    image,
    area.x, area.y, area.width, area.height,
    0, 0, OUTPUT_SIZE, OUTPUT_SIZE,
  );
  const blob = await new Promise<Blob | null>((resolve) => canvas.toBlob(resolve, 'image/png'));
  if (!blob) throw new Error('头像生成失败，请重试');
  if (blob.size > MAX_FILE_SIZE) throw new Error('裁剪后的图片不能超过 5MB');
  return new File([blob], 'avatar.png', { type: 'image/png' });
}
