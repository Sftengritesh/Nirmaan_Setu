CREATE TABLE notification (
    id UUID PRIMARY KEY,
    recipient_user_id UUID NOT NULL REFERENCES app_user(id),
    type VARCHAR(40) NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    reference_entity_type VARCHAR(40),
    reference_entity_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'UNREAD',
    read_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_notification_status CHECK (status IN ('UNREAD', 'READ')),
    CONSTRAINT chk_notification_read_at CHECK (
        (status = 'UNREAD' AND read_at IS NULL) OR
        (status = 'READ' AND read_at IS NOT NULL)
    )
);

CREATE INDEX idx_notification_recipient_status_created 
    ON notification (recipient_user_id, status, created_at DESC);

CREATE TRIGGER trg_notification_updated_at 
    BEFORE UPDATE ON notification 
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();
