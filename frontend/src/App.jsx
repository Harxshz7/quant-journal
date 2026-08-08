import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import JournalList from './pages/JournalList';
import JournalDetail from './pages/JournalDetail';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<JournalList />} />
        <Route path="/journal/:id" element={<JournalDetail />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
