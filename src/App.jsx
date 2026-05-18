import React from 'react';
import { BrowserRouter, Routes, Route, useLocation } from 'react-router-dom';
import LibraryView from './components/LibraryView.jsx';
import PlayerView from './components/PlayerView.jsx';
import SettingsView from './components/SettingsView.jsx';
import BottomNav from './components/BottomNav.jsx';
import { useAudio } from './hooks/useAudio.js';

function AppInner() {
  useAudio();
  const location = useLocation();
  const isPlayer = location.pathname === '/player';

  return (
    <div className="app-root">
      <div className={`page-container${isPlayer ? ' no-bottom-pad' : ''}`}>
        <Routes>
          <Route path="/" element={<LibraryView />} />
          <Route path="/player" element={<PlayerView />} />
          <Route path="/settings" element={<SettingsView />} />
        </Routes>
      </div>
      <BottomNav />
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppInner />
    </BrowserRouter>
  );
}
