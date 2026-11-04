CREATE TABLE accounts
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    user_id    UUID,
    currency   VARCHAR(3)  NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_accounts_users FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_accounts_currency CHECK (currency IN ('USD', 'EUR', 'TRY')),
    CONSTRAINT uq_accounts_user_currency UNIQUE (user_id, currency)
);

CREATE TABLE journal_entries
(
    id          UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    description VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE journal_lines
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id UUID           NOT NULL,
    entry_id   UUID           NOT NULL,
    amount     NUMERIC(19, 4) NOT NULL,
    CONSTRAINT fk_journal_lines_accounts FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT fk_journal_lines_journal_entries FOREIGN KEY (entry_id) REFERENCES journal_entries (id),
    CONSTRAINT ck_journal_lines_amount_not_zero CHECK ( amount <> 0 )
);

CREATE INDEX idx_journal_lines_account_id ON journal_lines (account_id);
CREATE INDEX idx_journal_lines_entry_id ON journal_lines (entry_id);
CREATE UNIQUE INDEX uq_accounts_external_currency ON accounts
    (currency) WHERE user_id IS NULL;
