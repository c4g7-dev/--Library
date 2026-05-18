import React from 'react';

export default function VinylDisk({ coverArt, isPlaying, size = 280 }) {
  const innerRing = Math.round(size * 0.528);
  const artSize = Math.round(size * 0.486);

  return (
    <div
      className={`vinyl-outer${isPlaying ? ' spinning' : ''}`}
      style={{ width: size, height: size }}
    >
      <div
        className="vinyl-inner-ring"
        style={{ width: innerRing, height: innerRing }}
      >
        <div
          className="vinyl-art-circle"
          style={{ width: artSize, height: artSize }}
        >
          {coverArt ? (
            <img src={coverArt} alt="Album art" draggable={false} />
          ) : (
            <div
              style={{
                width: '100%',
                height: '100%',
                background: 'var(--card)',
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: artSize * 0.3,
                color: 'var(--text-muted)',
              }}
            >
              ♪
            </div>
          )}
          <div className="vinyl-center-dot" />
        </div>
      </div>
    </div>
  );
}
