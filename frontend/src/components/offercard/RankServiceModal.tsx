import { useState } from "react";
import { submitRating } from "../../api/ratings";
import type { RatingRequestDto } from "../../types/ratings";
import { toast } from "react-hot-toast";

interface Props {
  serviceName: string;
  open: boolean;
  onClose: () => void;
}

export default function RankServiceModal({ serviceName, open, onClose }: Props) {
  const [form, setForm] = useState({
    userName: "",
    email: "",
    ranking: 0,
    comment: "",
  });
  const [loading, setLoading] = useState(false);

  const canSend =
    form.userName.trim().length > 0 &&
    form.email.trim().length > 0 &&
    form.ranking >= 1;

  const update = <K extends keyof typeof form>(key: K) => (value: typeof form[K]) =>
    setForm((prev) => ({ ...prev, [key]: value }));

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!canSend) return;

    const dto: RatingRequestDto = {
      serviceName,
      userName: form.userName,
      email: form.email,
      ranking: form.ranking,
      comment: form.comment,
    };

    try {
      setLoading(true);
      await submitRating(dto);
      toast.success("Thanks for your review!");
      onClose();
    } catch (err) {
      toast.error("Could not submit your review");
    } finally {
      setLoading(false);
    }
  }

  return (
    <dialog
      className={`modal ${open ? "modal-open" : ""}`}
      onClose={onClose}
      onCancel={onClose}
    >
      <form
        method="dialog"
        onSubmit={handleSubmit}
        className="modal-box w-11/12 max-w-md space-y-4"
      >
        <h3 className="font-bold text-xl text-center">Rate this service</h3>

        {/* Name */}
        <div className="form-control w-full">
          <label className="label" htmlFor="name">
            <span className="label-text">Name *</span>
          </label>
          <input
            id="name"
            type="text"
            className="input input-bordered w-full"
            placeholder="Your name"
            value={form.userName}
            onChange={(e) => update("userName")(e.target.value)}
            required
          />
        </div>

        {/* Email */}
        <div className="form-control w-full">
          <label className="label" htmlFor="email">
            <span className="label-text">Email *</span>
          </label>
          <input
            id="email"
            type="email"
            className="input input-bordered validator w-full"
            placeholder="mail@site.com"
            value={form.email}
            onChange={(e) => update("email")(e.target.value)}
            required
          />
          <div className="validator-hint">Enter valid email address</div>
        </div>

        {/* Rating */}
        <div className="form-control w-full">
          <label className="label">
            <span className="label-text">Your rating *</span>
          </label>
          <div className="rating rating-lg flex justify-center gap-2">
            {[1, 2, 3, 4, 5].map((v) => (
              <input
                key={v}
                type="radio"
                name="rating"
                aria-label={`${v} star${v > 1 ? "s" : ""}`}
                className="mask mask-star-2 bg-warning"
                checked={form.ranking === v}
                onChange={() => update("ranking")(v)}
              />
            ))}
          </div>
        </div>

        {/* Comment */}
        <div className="form-control w-full">
          <label className="label" htmlFor="comment">
            <span className="label-text">Comment (optional)</span>
          </label>
          <textarea
            id="comment"
            className="textarea textarea-bordered w-full h-24"
            placeholder="Share more about your experience…"
            value={form.comment}
            onChange={(e) => update("comment")(e.target.value)}
          />
        </div>

        {/* Actions */}
        <div className="modal-action justify-between">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={onClose}
            disabled={loading}
          >
            Cancel
          </button>
          <button
            type="submit"
            className="btn btn-warning"
            disabled={!canSend || loading}
          >
            {loading ? (
              <span className="loading loading-spinner loading-xs" />
            ) : (
              "Send"
            )}
          </button>
        </div>
      </form>
    </dialog>
  );
}