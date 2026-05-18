import { useCallback } from 'react';
import usePlayerStore from '../store/playerStore.js';
import { generateCoverArt } from '../utils/coverArtFallback.js';

function parseVorbisComments(buffer) {
  const view = new DataView(buffer);
  const meta = { title: '', artist: '', album: '' };

  try {
    // Opus head / tags magic check
    // OggOpus comment header starts after "OpusTags" (8 bytes)
    const bytes = new Uint8Array(buffer);
    let offset = 0;

    const magic = String.fromCharCode(...bytes.slice(0, 8));
    if (magic !== 'OpusTags') return meta;
    offset = 8;

    // vendor string length (LE u32)
    const vendorLen = view.getUint32(offset, true);
    offset += 4 + vendorLen;

    // comment list count
    const commentCount = view.getUint32(offset, true);
    offset += 4;

    for (let i = 0; i < commentCount; i++) {
      const len = view.getUint32(offset, true);
      offset += 4;
      const commentBytes = bytes.slice(offset, offset + len);
      offset += len;
      const comment = new TextDecoder().decode(commentBytes);
      const eq = comment.indexOf('=');
      if (eq === -1) continue;
      const key = comment.slice(0, eq).toUpperCase();
      const val = comment.slice(eq + 1);
      if (key === 'TITLE') meta.title = val;
      else if (key === 'ARTIST') meta.artist = val;
      else if (key === 'ALBUM') meta.album = val;
    }
  } catch (_) {
    // ignore parse errors
  }
  return meta;
}

async function extractOpusPage(fileHandle) {
  const file = await fileHandle.getFile();
  // Read first 64KB — enough for Vorbis comment header
  const slice = file.slice(0, 65536);
  const buffer = await slice.arrayBuffer();
  const bytes = new Uint8Array(buffer);

  // Scan for "OpusTags" magic string
  const magic = [0x4f, 0x70, 0x75, 0x73, 0x54, 0x61, 0x67, 0x73]; // "OpusTags"
  for (let i = 0; i < bytes.length - 8; i++) {
    let match = true;
    for (let j = 0; j < 8; j++) {
      if (bytes[i + j] !== magic[j]) { match = false; break; }
    }
    if (match) {
      return parseVorbisComments(buffer.slice(i));
    }
  }
  return { title: '', artist: '', album: '' };
}

function nameFromHandle(handle) {
  const name = handle.name.replace(/\.opus$/i, '');
  const parts = name.split(/[-–_]/);
  if (parts.length >= 2) {
    return { artist: parts[0].trim(), title: parts.slice(1).join(' ').trim() };
  }
  return { artist: '', title: name };
}

export function useTracks() {
  const { setTracks, setSourcePath, setDirectoryHandle } = usePlayerStore();

  const loadDirectory = useCallback(async (dirHandle) => {
    setDirectoryHandle(dirHandle);
    setSourcePath(dirHandle.name);

    const tracks = [];
    for await (const [name, handle] of dirHandle.entries()) {
      if (handle.kind !== 'file') continue;
      if (!name.toLowerCase().endsWith('.opus')) continue;

      const file = await handle.getFile();
      const url = URL.createObjectURL(file);

      let meta = await extractOpusPage(handle).catch(() => null) || {};
      if (!meta.title) {
        const fallback = nameFromHandle(handle);
        meta.title = meta.title || fallback.title;
        meta.artist = meta.artist || fallback.artist;
      }

      tracks.push({
        id: `${name}-${file.lastModified}`,
        title: meta.title || name,
        artist: meta.artist || 'Unknown',
        album: meta.album || '',
        coverArt: null,
        fileHandle: handle,
        url,
        duration: 0,
      });
    }

    // Sort alphabetically by title
    tracks.sort((a, b) => a.title.localeCompare(b.title));

    // Generate placeholder cover arts
    const tracksWithArt = tracks.map((t) => ({
      ...t,
      coverArt: generateCoverArt(t.title, t.artist),
    }));

    setTracks(tracksWithArt);
    return tracksWithArt;
  }, [setTracks, setSourcePath, setDirectoryHandle]);

  return { loadDirectory };
}
