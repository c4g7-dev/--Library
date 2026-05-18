import React, { useState } from 'react';
import usePlayerStore from '../store/playerStore.js';
import { useTracks } from '../hooks/useTracks.js';

function FolderIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
    </svg>
  );
}

export default function SettingsView() {
  const { sourcePath, setSourcePath, gaplessPlayback, setGaplessPlayback } = usePlayerStore();
  const { loadDirectory } = useTracks();
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(false);

  async function handlePickFolder() {
    if (!('showDirectoryPicker' in window)) {
      setStatus({ type: 'err', msg: 'Directory picker not supported. Use Chrome or Edge.' });
      return;
    }
    try {
      const handle = await window.showDirectoryPicker({ mode: 'read' });
      setLoading(true);
      setStatus(null);
      const tracks = await loadDirectory(handle);
      setStatus({ type: 'ok', msg: `Loaded ${tracks.length} opus file${tracks.length !== 1 ? 's' : ''}.` });
    } catch (e) {
      if (e.name !== 'AbortError') {
        setStatus({ type: 'err', msg: `Error: ${e.message}` });
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="settings-view">
      <header className="settings-header">
        <h1>Settings</h1>
      </header>

      <div className="settings-body">
        {/* Source section */}
        <div className="settings-section-label">Source</div>

        <div className="settings-item">
          <span className="settings-label">Opus files directory</span>
          <span className="settings-sublabel">
            Point to the folder containing your <code>.opus</code> audio files.
          </span>
          <input
            className="settings-input"
            type="text"
            value={sourcePath}
            onChange={(e) => setSourcePath(e.target.value)}
            placeholder="/storage/emulated/0/Music"
            spellCheck={false}
            autoCapitalize="none"
          />
          <button
            className="settings-pick-btn"
            onClick={handlePickFolder}
            disabled={loading}
          >
            {loading ? (
              <span className="spinner" style={{ width: 16, height: 16 }} />
            ) : (
              <FolderIcon />
            )}
            {loading ? 'Loading…' : 'Browse & Load Folder'}
          </button>
        </div>

        {status && (
          <p className={`settings-status ${status.type}`}>{status.msg}</p>
        )}

        {/* Playback section */}
        <div className="settings-section-label">Playback</div>

        <div className="settings-item-row">
          <div>
            <div className="settings-label">Gapless playback</div>
            <div className="settings-sublabel">
              Seamless transitions between tracks.
            </div>
          </div>
          <label className="toggle-switch" aria-label="Toggle gapless playback">
            <input
              type="checkbox"
              checked={gaplessPlayback}
              onChange={(e) => setGaplessPlayback(e.target.checked)}
            />
            <span className="toggle-slider" />
          </label>
        </div>

        {/* About */}
        <div className="settings-section-label">About</div>

        <div className="settings-item">
          <span className="settings-label">Library</span>
          <span className="settings-sublabel">
            AMOLED dark music player · Opus support · v1.0.0
          </span>
        </div>
      </div>
    </div>
  );
}
