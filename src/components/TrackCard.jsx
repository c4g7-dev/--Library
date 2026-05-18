import React from 'react';
import { useNavigate } from 'react-router-dom';
import usePlayerStore from '../store/playerStore.js';
import { audioPlay } from '../hooks/useAudio.js';
import ProgressRing from './ProgressRing.jsx';

export default function TrackCard({ track }) {
  const navigate = useNavigate();
  const { currentTrack, setCurrentTrack, setIsPlaying, trackProgress } = usePlayerStore();
  const isActive = currentTrack?.id === track.id;
  const progress = trackProgress[track.id] || 0;

  function handleClick() {
    if (!isActive) {
      setCurrentTrack(track);
      setIsPlaying(false);
    }
    setTimeout(() => {
      audioPlay();
    }, 50);
    navigate('/player');
  }

  return (
    <div
      className={`track-card${isActive ? ' active' : ''}`}
      onClick={handleClick}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => e.key === 'Enter' && handleClick()}
      aria-label={`Play ${track.title} by ${track.artist}`}
    >
      <div className="track-card-cover-wrap">
        {track.coverArt ? (
          <img
            className="track-card-cover"
            src={track.coverArt}
            alt={track.title}
            draggable={false}
          />
        ) : (
          <div className="track-card-cover-placeholder">
            <span style={{ fontSize: '1.4rem', color: 'var(--text-muted)' }}>♪</span>
          </div>
        )}

        {progress > 0 && (
          <div className="track-card-ring">
            <ProgressRing progress={progress} size={28} stroke={2.5} />
          </div>
        )}
      </div>

      <div className="track-card-info">
        <div className="track-card-title">{track.title}</div>
        <div className="track-card-artist">{track.artist}</div>
      </div>
    </div>
  );
}
