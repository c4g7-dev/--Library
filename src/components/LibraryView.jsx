import React from 'react';
import { useNavigate } from 'react-router-dom';
import usePlayerStore from '../store/playerStore.js';
import { useTracks } from '../hooks/useTracks.js';
import TrackCard from './TrackCard.jsx';

function GearIcon() {
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="3" />
      <path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06A1.65 1.65 0 0 0 4.68 15a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06A1.65 1.65 0 0 0 9 4.68a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06A1.65 1.65 0 0 0 19.4 9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z" />
    </svg>
  );
}

function FolderIcon() {
  return (
    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
    </svg>
  );
}

export default function LibraryView() {
  const navigate = useNavigate();
  const { tracks } = usePlayerStore();
  const { loadDirectory } = useTracks();
  const [loading, setLoading] = React.useState(false);

  async function handlePickFolder() {
    if (!('showDirectoryPicker' in window)) {
      alert('Directory picker not supported in this browser. Use Chrome/Edge.');
      return;
    }
    try {
      const handle = await window.showDirectoryPicker({ mode: 'read' });
      setLoading(true);
      await loadDirectory(handle);
    } catch (e) {
      if (e.name !== 'AbortError') console.error(e);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="library-view">
      <header className="library-header">
        <h1>Library</h1>
        <button
          className="gear-btn"
          onClick={() => navigate('/settings')}
          aria-label="Settings"
        >
          <GearIcon />
        </button>
      </header>

      {tracks.length === 0 ? (
        <div className="library-empty">
          <FolderIcon />
          <p>
            <strong>No tracks loaded.</strong>
            <br />
            Pick a folder of <code>.opus</code> files to get started.
          </p>
          <button
            onClick={handlePickFolder}
            disabled={loading}
            style={{
              marginTop: 8,
              background: 'var(--accent)',
              color: '#fff',
              border: 'none',
              borderRadius: 10,
              padding: '12px 24px',
              fontSize: '0.88rem',
              fontWeight: 600,
              cursor: loading ? 'not-allowed' : 'pointer',
              fontFamily: 'inherit',
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              opacity: loading ? 0.6 : 1,
              transition: 'opacity 0.2s',
            }}
          >
            {loading ? <span className="spinner" /> : null}
            {loading ? 'Loading…' : 'Open Folder'}
          </button>
        </div>
      ) : (
        <>
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              padding: '8px 16px 4px',
            }}
          >
            <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
              {tracks.length} track{tracks.length !== 1 ? 's' : ''}
            </span>
            <button
              onClick={handlePickFolder}
              disabled={loading}
              style={{
                background: 'none',
                border: '1px solid var(--border)',
                borderRadius: 6,
                padding: '4px 10px',
                fontSize: '0.7rem',
                fontWeight: 500,
                color: 'var(--text-secondary)',
                cursor: 'pointer',
                fontFamily: 'inherit',
                display: 'flex',
                alignItems: 'center',
                gap: 4,
              }}
            >
              {loading ? <span className="spinner" style={{ width: 12, height: 12 }} /> : null}
              {loading ? 'Loading…' : 'Change folder'}
            </button>
          </div>

          <div className="library-grid">
            {tracks.map((track) => (
              <TrackCard key={track.id} track={track} />
            ))}
          </div>
        </>
      )}
    </div>
  );
}
