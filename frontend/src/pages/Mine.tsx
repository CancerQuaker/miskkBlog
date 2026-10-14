import { Image } from 'antd';
import { galleryConfig } from '../config/site';

/**
 * 「我的」页面：图片墙展示。
 */
export default function Mine() {
  return (
    <div className="page-container">
      <div className="card fade-in" style={{ padding: '26px 30px', marginBottom: 20 }}>
        <h1 style={{ margin: 0, fontSize: 24 }}>📷 我的相册</h1>
        <p style={{ color: 'var(--text-sub)', margin: '8px 0 0' }}>
          一些喜欢的图与生活碎片，点击可放大观看~
        </p>
      </div>
      <Image.PreviewGroup>
        <div className="gallery-masonry">
          {galleryConfig.map((g, i) => (
            <div key={i} className="card shot fade-in">
              <Image src={g.src} alt={g.caption} style={{ objectFit: 'cover' }} />
              <div className="cap">{g.caption}</div>
            </div>
          ))}
        </div>
      </Image.PreviewGroup>
    </div>
  );
}
