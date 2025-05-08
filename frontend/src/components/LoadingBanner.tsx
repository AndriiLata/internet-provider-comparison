export default function LoadingBanner() {
    return (
      <div className="alert shadow-lg mb-6 bg-base-200">
        <span className="loading loading-spinner loading-sm mr-2"></span>
        Fetching offers … this can take a few seconds.
      </div>
    );
  }