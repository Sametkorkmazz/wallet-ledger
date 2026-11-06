ALTER TABLE journal_lines
    ADD CONSTRAINT uq_journal_lines_entry_account UNIQUE (entry_id, account_id);
-- idx_journal_lines_entry_id is not needed anymore as uq_journal_lines_entry_account can answer any query
-- filtering on entry_id alone.
DROP INDEX idx_journal_lines_entry_id;
