type ShareModalProps = {
  /** Controls visibility of the modal */
  isOpen: boolean;
  /** URL that will be shared + copied */
  shareUrl: string;
  /** Called when user closes the modal (or clicks outside) */
  onClose: () => void;
};

/**
 * Re‑usable share‑link modal component.
 *
 * Inspired by DaisyUI’s modal markup but polished with
 * rounded corners, better spacing, and a sticky close button.
 *
 * Usage example:
 * ```tsx
 * const [modalOpen, setModalOpen] = useState(false);
 * const [shareUrl, setShareUrl] = useState("https://example.com");
 *
 * <ShareModal
 *   isOpen={modalOpen}
 *   shareUrl={shareUrl}
 *   onClose={() => setModalOpen(false)}
 * />
 * ```
 */
export default function ShareModal({ isOpen, shareUrl, onClose }: ShareModalProps) {
  if (!isOpen) return null;

  const copyToClipboard = async () => {
    try {
      await navigator.clipboard.writeText(shareUrl);
      // Swap this alert for your toast system of choice
      alert("Link copied!");
    } catch {
      alert("Copy failed; please try manually.");
    }
  };

  return (
    <div className="modal modal-open z-50 cursor-pointer" onClick={onClose}>
      <div
        className="modal-box w-11/12 max-w-lg bg-base-200 shadow-xl rounded-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Close */}
        <button
          className="btn btn-circle btn-ghost absolute right-4 top-4"
          onClick={onClose}
          aria-label="Close share modal"
        >
          ✕
        </button>

        <h3 className="text-2xl font-bold text-center mb-8">Share Your Search</h3>

        {/* Link & copy */}
        <div className="flex w-full mb-10">
          <input
            type="text"
            readOnly
            value={shareUrl}
            className="input input-bordered flex-1 rounded-l-lg"
          />
          <button className="btn btn-primary rounded-l-none" onClick={copyToClipboard}>
            Copy
          </button>
        </div>

        {/* Share shortcuts */}
        <div className="grid grid-cols-2 gap-6">
          <button
            className="btn btn-success btn-outline gap-2 hover:bg-success hover:text-white transition"
            onClick={() =>
              window.open(
                `https://api.whatsapp.com/send?text=${encodeURIComponent(shareUrl)}`,
                "_blank"
              )
            }
          >
            <span className="text-2xl">📱</span>
            WhatsApp
          </button>

          <button
            className="btn btn-info btn-outline gap-2 hover:bg-info hover:text-white transition"
            onClick={() =>
              (window.location.href = `sms:?&body=${encodeURIComponent(shareUrl)}`)
            }
          >
            <span className="text-2xl">💬</span>
            SMS
          </button>
        </div>
      </div>
    </div>
  );
}