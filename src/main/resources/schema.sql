-- Create the database
CREATE DATABASE IF NOT EXISTS postgres;

-- Connect to the database
\c postgres;

-- Create the data_records table
CREATE TABLE IF NOT EXISTS data (
    id SERIAL PRIMARY KEY,
    match_id VARCHAR(50) NOT NULL,
    market_id VARCHAR(50) NOT NULL,
    outcome_id VARCHAR(50) NOT NULL,
    specifiers TEXT,
    date_insert TIMESTAMP NOT NULL,
    sequence_number BIGINT NOT NULL,
    original_order BIGINT NOT NULL
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_data_records_match_id ON data(match_id);
CREATE INDEX IF NOT EXISTS idx_data_records_date_insert ON data(date_insert);
CREATE INDEX IF NOT EXISTS idx_data_records_match_sequence ON data(match_id, sequence_number);