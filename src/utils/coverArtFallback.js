/**
 * Generate a gradient placeholder cover art as a data URL using Canvas.
 * Each track gets a deterministic color pair derived from its title/artist.
 */

const GRADIENT_PAIRS = [
  ['#1a0a2e', '#4a1a6e'],
  ['#0a1a2e', '#1a4a6e'],
  ['#2e0a0a', '#6e1a1a'],
  ['#0a2e0a', '#1a6e2a'],
  ['#2e1a0a', '#6e3a1a'],
  ['#0a2e2e', '#1a5e6e'],
  ['#1e0a2e', '#5a1a7e'],
  ['#2e0a1a', '#7e1a4a'],
  ['#0a0a2e', '#2a1a7e'],
  ['#2e2a0a', '#6e5a1a'],
];

function hashString(str) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i);
    hash = (hash << 5) - hash + char;
    hash |= 0;
  }
  return Math.abs(hash);
}

/**
 * Generate a gradient placeholder as a data URL.
 * @param {string} title - Track title
 * @param {string} artist - Track artist
 * @param {number} size - Canvas size in pixels (default 300)
 * @returns {string} data URL (PNG)
 */
export function generateCoverArt(title = '', artist = '', size = 300) {
  const canvas = document.createElement('canvas');
  canvas.width = size;
  canvas.height = size;
  const ctx = canvas.getContext('2d');

  const seed = hashString(`${title}${artist}`);
  const [colorA, colorB] = GRADIENT_PAIRS[seed % GRADIENT_PAIRS.length];

  // Background gradient
  const grad = ctx.createLinearGradient(0, 0, size, size);
  grad.addColorStop(0, colorA);
  grad.addColorStop(1, colorB);
  ctx.fillStyle = grad;
  ctx.fillRect(0, 0, size, size);

  // Subtle circular highlight
  const radGrad = ctx.createRadialGradient(
    size * 0.35, size * 0.35, 0,
    size * 0.35, size * 0.35, size * 0.65
  );
  radGrad.addColorStop(0, 'rgba(255,255,255,0.08)');
  radGrad.addColorStop(1, 'rgba(0,0,0,0)');
  ctx.fillStyle = radGrad;
  ctx.fillRect(0, 0, size, size);

  // Music note icon
  ctx.fillStyle = 'rgba(255,255,255,0.18)';
  ctx.font = `bold ${Math.round(size * 0.38)}px serif`;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText('♪', size / 2, size / 2 + size * 0.02);

  // Artist initial(s) — small, bottom-left
  const initials = [title, artist]
    .filter(Boolean)
    .map((s) => s.trim()[0]?.toUpperCase() || '')
    .join('');

  if (initials) {
    ctx.fillStyle = 'rgba(255,255,255,0.55)';
    ctx.font = `600 ${Math.round(size * 0.11)}px Inter, system-ui, sans-serif`;
    ctx.textAlign = 'left';
    ctx.textBaseline = 'bottom';
    ctx.fillText(initials, size * 0.08, size * 0.93);
  }

  return canvas.toDataURL('image/png');
}
