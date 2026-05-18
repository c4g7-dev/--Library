import React, { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import usePlayerStore from '../store/playerStore.js';
import { audioToggle, audioSeek, audioNext, audioPrev } from '../hooks/useAudio.js';
import VinylDisk from './VinylDisk.jsx';

function formatTime(seconds) {
  if (!seconds || !isFinite(seconds)) return '0:00';
  const m = Math.floor(seconds / 60);
  const s = Math.floor(seconds % 60);
  return `${m}:${s.toString().padStart(2, '0')}`;
}

function BackIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="15 18 9 12 15 6" />
    </svg>
  );
}

function PrevIcon() {
  return (
    <svg width="26" height="26" viewBox="0 0 24 24" fill="currentColor">
      <polygon points="19,20 9,12 19,4" />
      <rect x="5" y="4" width="3" height="16" rx="1" />
    </svg>
  );
}

function NextIcon() {
  return (
    <svg width="26" height="26" viewBox="0 0 24 24" fill="currentColor">
      <polygon points="5,4 15,12 5,20" />
      <rect x="16" y="4" width="3" height="16" rx="1" />
    </svg>
  );
}

function PlayIcon() {
  return (
    <svg width="26" height="26" viewBox="0 0 24 24" fill="currentColor">
      <polygon points="5,3 19,12 5,21" />
    </svg>
  );
}

function PauseIcon() {
  return (
    <svg width="26" height="26" viewBox="0 0 24 24" fill="currentColor">
      <rect x="6" y="4" width="4" height="16" rx="1" />
      <rect x="14" y="4" width="4" height="16" rx="1" />
    </svg>
  );
}

export default function PlayerView() {
  const navigate = useNavigate();
  const { currentTrack, isPlaying, progress, duration } = usePlayerStore();

  const currentTime = (progress || 0) * (duration || 0);

  const handleProgressClick = useCallback((e) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const fraction = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width));
    audioSeek(fraction);
  }, []);

  if (!currentTrack) {
    return (
      <div className="player-view" style={{ justifyContent: 'center', gap: 16 }}>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
          No track selected.
        </p>
        <button
          onClick={() => navigate('/')}
          style={{
            background: 'var(--surface-el)',
            border: '1px solid var(--border)',
            borderRadius: 10,
            color: 'var(--text-primary)',
            padding: '10px 20px',
            fontFamily: 'inherit',
            fontSize: '0.85rem',
            cursor: 'pointer',
          }}
        >
          Go to Library
        </button>
      </div>
    );
  }

  return (
    <div className="player-view">
      {/* Top bar */}
      <div className="player-top-bar">
        <button
          className="player-back-btn"
          onClick={() => navigate('/')}
          aria-label="Back to library"
        >
          <BackIcon />
        </button>
        <span className="player-label">Now Playing</span>
        <div style={{ width: 40 }} />
      </div>

      {/* Vinyl */}
      <div className="player-vinyl-section">
        <VinylDisk
          coverArt={currentTrack.coverArt}
          isPlaying={isPlaying}
        />
      </div>

      {/* Track info */}
      <div className="player-info">
        <div className="player-track-title">{currentTrack.title}</div>
        <div className="player-track-artist">{currentTrack.artist}</div>
      </div>

      {/* Progress */}
      <div className="player-progress-section">
        <div
          className="player-progress-bar-track"
          onClick={handleProgressClick}
          role="slider"
          aria-label="Seek"
          aria-valuemin={0}
          aria-valuemax={100}
          aria-valuenow={Math.round((progress || 0) * 100)}
        >
          <div
            className="player-progress-bar-fill"
            style={{ width: `${(progress || 0) * 100}%` }}
          />
        </div>
        <div className="player-time-row">
          <span className="player-time">{formatTime(currentTime)}</span>
          <span className="player-time">{formatTime(duration)}</span>
        </div>
      </div>

      {/* Controls */}
      <div className="player-controls">
        <button
          className="ctrl-btn"
          onClick={audioPrev}
          aria-label="Previous track"
        >
          <PrevIcon />
        </button>

        <button
          className="ctrl-btn-play"
          onClick={audioToggle}
          aria-label={isPlaying ? 'Pause' : 'Play'}
        >
          {isPlaying ? <PauseIcon /> : <PlayIcon />}
        </button>

        <button
          className="ctrl-btn"
          onClick={audioNext}
          aria-label="Next track"
        >
          <NextIcon />
        </button>
      </div>
    </div>
  );
}
