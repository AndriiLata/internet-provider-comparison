import Sidebar from '../components/Sidebar';

export default function MainPage() {
  const handleSearch = (q: any) => {
    console.log('TODO call API with', q);
  };

  return (
    <div className="min-h-screen flex">
      <Sidebar onSearch={handleSearch} />

      <main className="flex-1 p-10">
        <div className="prose max-w-2xl">
          <h1 className="mb-4">Results</h1>
          <p className="opacity-70">
            After you search, available internet providers will appear here.
            Filters and sorting are coming soon ✨
          </p>
        </div>
      </main>
    </div>
  );
}