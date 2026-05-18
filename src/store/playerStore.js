import { create } from 'zustand';

const usePlayerStore = create((set) => ({
  // ── Track list ──────────────────────────────────────────────
  tracks: [],
  setTracks: (tracks) => set({ tracks }),

  // ── Current playback ────────────────────────────────────────
  currentTrack: null,
  setCurrentTrack: (track) => set({ currentTrack: track }),

  isPlaying: false,
  setIsPlaying: (isPlaying) => set({ isPlaying }),

  progress: 0,
  setProgress: (progress) => set({ progress }),

  duration: 0,
  setDuration: (duration) => set({ duration }),

  // ── Per-track listen progress (for ProgressRing in grid) ────
  trackProgress: {},
  updateTrackProgress: (id, value) =>
    set((state) => ({
      trackProgress: { ...state.trackProgress, [id]: value },
    })),

  // ── Source directory ────────────────────────────────────────
  sourcePath: localStorage.getItem('sourcePath') || '',
  setSourcePath: (path) => {
    localStorage.setItem('sourcePath', path);
    set({ sourcePath: path });
  },

  // ── Directory handle (not serializable — runtime only) ──────
  directoryHandle: null,
  setDirectoryHandle: (handle) => set({ directoryHandle: handle }),

  // ── Gapless playback toggle ─────────────────────────────────
  gaplessPlayback: false,
  setGaplessPlayback: (val) => set({ gaplessPlayback: val }),
}));

export default usePlayerStore;
