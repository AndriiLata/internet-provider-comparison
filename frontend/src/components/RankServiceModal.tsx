import { useState } from "react";
import { submitRating } from "../api/ratings";
import type { RatingRequestDto } from "../types/ratings";
import { toast } from "react-hot-toast";

interface Props {
  serviceName: string;
  open: boolean;
  onClose: () => void;
}

export default function RankServiceModal({ serviceName, open, onClose }: Props) {
  const [userName, setUserName]   = useState("");
  const [email, setEmail]         = useState("");
  const [ranking, setRanking]     = useState<number>(0);
  const [comment, setComment]     = useState("");
  const [loading, setLoading]     = useState(false);

  const canSend = userName && email && ranking >= 1;

  async function handleSubmit() {
    if (!canSend) return;
    const dto: RatingRequestDto = { serviceName, userName, email, ranking, comment };

    try {
      setLoading(true);
      await submitRating(dto);
      toast.success("Thanks for your review!");
      onClose();
    } catch {
      toast.error("Could not submit your review");
    } finally {
      setLoading(false);
    }
  }

  return (
    open && (
      <dialog className="modal modal-open" onCancel={onClose}>
        <div className="modal-box w-11/12 max-w-md">
          <h3 className="font-bold text-lg mb-4">Rate this service</h3>

          <div className="form-control mb-3">
            <label className="label">
              <span className="label-text">Name *</span>
            </label>
            <input
              type="text"
              className="input input-bordered"
              value={userName}
              onChange={e => setUserName(e.target.value)}
              required
            />
          </div>

          <div className="form-control mb-3">
            <label className="label">
              <span className="label-text">Email *</span>
            </label>
            {/* daisyUI email validator */}
            <input
              type="email"
              className="input input-bordered"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
            />
          </div>

          <div className="form-control mb-3">
            <label className="label">
              <span className="label-text">Your rating *</span>
            </label>
            <div className="rating rating-lg">
              {[1,2,3,4,5].map(v => (
                <input
                  key={v}
                  type="radio"
                  name="rating"
                  className="mask mask-star-2 bg-orange-400"
                  checked={ranking === v}
                  onChange={() => setRanking(v)}
                />
              ))}
            </div>
          </div>

          <div className="form-control mb-6">
            <label className="label">
              <span className="label-text">Comment (optional)</span>
            </label>
            <textarea
              className="textarea textarea-bordered"
              rows={3}
              value={comment}
              onChange={e => setComment(e.target.value)}
            ></textarea>
          </div>

          <div className="modal-action">
            <button className="btn btn-ghost" onClick={onClose}>Cancel</button>
            <button
              className="btn btn-primary"
              disabled={!canSend || loading}
              onClick={handleSubmit}
            >
              {loading ? "Sending…" : "Send"}
            </button>
          </div>
        </div>
      </dialog>
    )
  );
}
