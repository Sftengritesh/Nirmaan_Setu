CREATE TABLE auth_phone_identity (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES app_user(id),
    phone_e164 VARCHAR(16) NOT NULL UNIQUE,
    verified_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_auth_phone_identity_indian_phone CHECK (phone_e164 ~ '^\+91[6-9][0-9]{9}$')
);

CREATE TABLE auth_otp_challenge (
    id UUID PRIMARY KEY,
    phone_e164 VARCHAR(16) NOT NULL,
    code_hash VARCHAR(100) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    attempts SMALLINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_auth_otp_challenge_indian_phone CHECK (phone_e164 ~ '^\+91[6-9][0-9]{9}$'),
    CONSTRAINT chk_auth_otp_challenge_attempts CHECK (attempts >= 0),
    CONSTRAINT chk_auth_otp_challenge_expiry CHECK (expires_at > created_at)
);

CREATE TABLE auth_session (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES app_user(id),
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_auth_session_expiry CHECK (expires_at > created_at)
);

CREATE INDEX idx_auth_otp_challenge_phone_created
    ON auth_otp_challenge (phone_e164, created_at DESC);
CREATE INDEX idx_auth_session_user_expiry ON auth_session (user_id, expires_at);

CREATE TRIGGER trg_auth_phone_identity_updated_at BEFORE UPDATE ON auth_phone_identity FOR EACH ROW EXECUTE FUNCTION set_updated_at();
