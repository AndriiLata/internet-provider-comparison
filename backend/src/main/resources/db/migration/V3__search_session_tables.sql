/* Stores one row per search */
CREATE TABLE search_session (
                                session_id  UUID PRIMARY KEY,
                                criteria    JSONB        NOT NULL,
                                created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

/* Stores every offer that streamed for this search */
CREATE TABLE session_offer (
                               id          BIGSERIAL PRIMARY KEY,
                               session_id  UUID         NOT NULL REFERENCES search_session(session_id) ON DELETE CASCADE,
                               offer       JSONB        NOT NULL
);

CREATE INDEX session_offer_session_id_idx ON session_offer(session_id);
