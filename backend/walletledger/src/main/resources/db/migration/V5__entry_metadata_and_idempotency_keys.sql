ALTER TABLE journal_entries
    ADD COLUMN reverses_entry_id UUID,
    ADD COLUMN entry_type        VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN created_by        UUID        NULL;

ALTER TABLE journal_entries
    ALTER COLUMN entry_type DROP DEFAULT;

ALTER TABLE journal_entries
    ADD CONSTRAINT fk_journal_entries_reverses_entry FOREIGN KEY (reverses_entry_id) REFERENCES journal_entries (id),
    ADD CONSTRAINT fk_journal_entries_users FOREIGN KEY (created_by) REFERENCES users (id),
    ADD CONSTRAINT ck_journal_entries_entry_type CHECK (entry_type IN ('TRANSFER', 'TOP_UP', 'REVERSAL', 'SYSTEM')),
    ADD CONSTRAINT ck_journal_entries_not_self_reversal CHECK (reverses_entry_id <> id), -- an entry can't reverse itself
    ADD CONSTRAINT ck_journal_entries_reversal_link CHECK ( (entry_type = 'REVERSAL') = (reverses_entry_id IS NOT NULL) ); -- REVERSAL exactly when it points to an entry.

CREATE UNIQUE INDEX uq_journal_entries_reverses_entry_id ON journal_entries (reverses_entry_id);

CREATE TABLE idempotency_keys
(
    user_id         UUID        NOT NULL,
    idempotency_key UUID        NOT NULL,
    request_hash    CHAR(64)    NOT NULL,
    -- entry_id, response_status, response_body: NULL only inside the posting transaction.
    -- The key row is inserted first and filled in before commit (ADR 2).
    entry_id        UUID,
    response_status SMALLINT,
    response_body   JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT pk_idempotency_keys PRIMARY KEY (user_id, idempotency_key),
    CONSTRAINT fk_idempotency_keys_users FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_idempotency_keys_journal_entries FOREIGN KEY (entry_id) REFERENCES journal_entries (id)
);

CREATE INDEX idx_idempotency_keys_created_at ON idempotency_keys (created_at);
