import React from 'react';

export default function ProgressRing({ progress = 0, size = 32, stroke = 2.5 }) {
  if (progress <= 0) return null;

  const radius = (size - stroke) / 2;
  const circumference = 2 * Math.PI * radius;
  const offset = circumference * (1 - Math.min(progress, 1));

  return (
    <svg
      className="progress-ring-svg"
      width={size}
      height={size}
      viewBox={`0 0 ${size} ${size}`}
    >
      {/* Track */}
      <circle
        cx={size / 2}
        cy={size / 2}
        r={radius}
        fill="none"
        stroke="rgba(255,255,255,0.1)"
        strokeWidth={stroke}
      />
      {/* Fill */}
      <circle
        cx={size / 2}
        cy={size / 2}
        r={radius}
        fill="none"
        stroke="#ff3232"
        strokeWidth={stroke}
        strokeLinecap="round"
        strokeDasharray={circumference}
        strokeDashoffset={offset}
        transform={`rotate(-90 ${size / 2} ${size / 2})`}
      />
      {/* Completed dot */}
      {progress >= 0.99 && (
        <circle cx={size / 2} cy={size / 2} r={3} fill="#ff3232" />
      )}
    </svg>
  );
}
