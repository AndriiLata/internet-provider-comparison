import { Routes, Route } from 'react-router-dom';
import LandingPage        from './pages/LandingPage';
import MainPage           from './pages/MainPage';
import SharedResultsPage  from './pages/SharedResultsPage';

export default function App() {
  return (
    
      <Routes>
        <Route path="/"            element={<LandingPage/>} />
        <Route path="/search"        element={<MainPage/>}   />
        <Route path="/share/:sessionId" element={<SharedResultsPage/>} />
      </Routes>
    
  );
}
