import { Link } from 'react-router-dom';

export default function LandingPage() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center gap-8 bg-base-200">
      <div className="text-center space-y-4">
        <h1 className="text-6xl font-black tracking-tight">
          internet<span className="text-primary">CHECK</span>
        </h1>
        <p className="text-xl opacity-80">
          Find the best internet provider at your address
        </p>
      </div>

      <Link to="/search" className="btn btn-primary btn-lg shadow-lg">
        Start now
      </Link>
    </div>
  );
}