import { useEffect, useRef } from 'react';
import usePlayerStore from '../store/playerStore.js';

let audioEl = null;

function getAudio() {
  if (!audioEl) {
    audioEl = new Audio();
    audioEl.preload = 'auto';
  }
  return audioEl;
}

export function useAudio() {
  const {
    currentTrack,
    isPlaying,
    setIsPlaying,
    setProgress,
    setDuration,
    updateTrackProgress,
    tracks,
    setCurrentTrack,
  } = usePlayerStore();

  const trackRef = useRef(null);

  useEffect(() => {
    const audio = getAudio();

    const onTimeUpdate = () => {
      if (!audio.duration) return;
      const pct = audio.currentTime / audio.duration;
      setProgress(pct);
      if (trackRef.current) {
        updateTrackProgress(trackRef.current.id, pct);
      }
    };

    const onLoadedMetadata = () => {
      setDuration(audio.duration || 0);
    };

    const onEnded = () => {
      setIsPlaying(false);
      setProgress(1);
      // auto-advance
      const store = usePlayerStore.getState();
      const list = store.tracks;
      const idx = list.findIndex((t) => t.id === store.currentTrack?.id);
      if (idx !== -1 && idx < list.length - 1) {
        store.setCurrentTrack(list[idx + 1]);
      }
    };

    audio.addEventListener('timeupdate', onTimeUpdate);
    audio.addEventListener('loadedmetadata', onLoadedMetadata);
    audio.addEventListener('ended', onEnded);

    return () => {
      audio.removeEventListener('timeupdate', onTimeUpdate);
      audio.removeEventListener('loadedmetadata', onLoadedMetadata);
      audio.removeEventListener('ended', onEnded);
    };
  }, [setIsPlaying, setProgress, setDuration, updateTrackProgress]);

  useEffect(() => {
    const audio = getAudio();
    if (!currentTrack) return;

    trackRef.current = currentTrack;

    if (audio.src !== (currentTrack.url || '')) {
      audio.src = currentTrack.url || '';
      audio.load();
    }

    if (isPlaying) {
      audio.play().catch(() => setIsPlaying(false));
    } else {
      audio.pause();
    }
  }, [currentTrack, isPlaying, setIsPlaying]);

  // expose imperative controls via store-style helper attached to module
}

export function audioPlay() {
  const audio = getAudio();
  audio.play().catch(() => {});
  usePlayerStore.getState().setIsPlaying(true);
}

export function audioPause() {
  getAudio().pause();
  usePlayerStore.getState().setIsPlaying(false);
}

export function audioToggle() {
  const store = usePlayerStore.getState();
  if (store.isPlaying) audioPause(); else audioPlay();
}

export function audioSeek(fraction) {
  const audio = getAudio();
  if (audio.duration) {
    audio.currentTime = fraction * audio.duration;
  }
}

export function audioNext() {
  const store = usePlayerStore.getState();
  const idx = store.tracks.findIndex((t) => t.id === store.currentTrack?.id);
  if (idx !== -1 && idx < store.tracks.length - 1) {
    store.setCurrentTrack(store.tracks[idx + 1]);
    store.setIsPlaying(true);
  }
}

export function audioPrev() {
  const audio = getAudio();
  if (audio.currentTime > 3) {
    audio.currentTime = 0;
    return;
  }
  const store = usePlayerStore.getState();
  const idx = store.tracks.findIndex((t) => t.id === store.currentTrack?.id);
  if (idx > 0) {
    store.setCurrentTrack(store.tracks[idx - 1]);
    store.setIsPlaying(true);
  }
}
