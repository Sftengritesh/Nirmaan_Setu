-- Migration V4: Operational Domains (Sites, Milestones, Attendance, Salaries, Invoices, Materials, Expenses, DPR, Audit Logs)

CREATE TABLE IF NOT EXISTS site (
    id UUID PRIMARY KEY,
    project_id UUID REFERENCES project(id),
    name VARCHAR(160) NOT NULL,
    project_title VARCHAR(200),
    address TEXT NOT NULL,
    city VARCHAR(80) NOT NULL,
    state VARCHAR(80) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    geofence_radius_meters INTEGER NOT NULL DEFAULT 150,
    manager_name VARCHAR(120),
    contact_phone VARCHAR(20),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project_milestone (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES project(id) ON DELETE CASCADE,
    phase_name VARCHAR(160) NOT NULL,
    description TEXT,
    target_date DATE NOT NULL,
    completion_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'TODO',
    order_index INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS attendance (
    id UUID PRIMARY KEY,
    worker_user_id UUID NOT NULL REFERENCES app_user(id),
    worker_name VARCHAR(120) NOT NULL,
    worker_trade VARCHAR(80),
    project_id UUID REFERENCES project(id),
    project_title VARCHAR(200),
    site_id UUID REFERENCES site(id),
    site_name VARCHAR(160),
    work_date DATE NOT NULL,
    check_in_time VARCHAR(30) NOT NULL,
    check_out_time VARCHAR(30),
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    is_within_geofence BOOLEAN NOT NULL DEFAULT TRUE,
    distance_from_site_meters INTEGER NOT NULL DEFAULT 0,
    selfie_url TEXT,
    approval_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by VARCHAR(120),
    approved_at TIMESTAMPTZ,
    remarks TEXT,
    hours_worked NUMERIC(5,2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_attendance_worker_date UNIQUE (worker_user_id, work_date)
);

CREATE TABLE IF NOT EXISTS worker_salary (
    id UUID PRIMARY KEY,
    worker_user_id UUID NOT NULL REFERENCES app_user(id),
    worker_name VARCHAR(120) NOT NULL,
    salary_month VARCHAR(7) NOT NULL,
    days_present INTEGER NOT NULL DEFAULT 0,
    half_days INTEGER NOT NULL DEFAULT 0,
    overtime_hours NUMERIC(5,2) NOT NULL DEFAULT 0,
    daily_rate NUMERIC(12,2) NOT NULL,
    base_salary NUMERIC(12,2) NOT NULL,
    overtime_pay NUMERIC(12,2) NOT NULL DEFAULT 0,
    bonus NUMERIC(12,2) NOT NULL DEFAULT 0,
    deductions NUMERIC(12,2) NOT NULL DEFAULT 0,
    net_payable NUMERIC(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    paid_date TIMESTAMPTZ,
    payment_method VARCHAR(30),
    payment_ref VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS invoice (
    id UUID PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    project_id UUID REFERENCES project(id),
    project_title VARCHAR(200),
    client_user_id UUID REFERENCES app_user(id),
    client_name VARCHAR(160),
    payment_type VARCHAR(40) NOT NULL DEFAULT 'CLIENT_INVOICE',
    amount NUMERIC(14,2) NOT NULL,
    tax_amount NUMERIC(14,2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(14,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    due_date DATE NOT NULL,
    paid_at TIMESTAMPTZ,
    payment_method VARCHAR(30),
    transaction_ref VARCHAR(100),
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS material (
    id UUID PRIMARY KEY,
    project_id UUID REFERENCES project(id),
    project_title VARCHAR(200),
    site_id UUID REFERENCES site(id),
    item_name VARCHAR(120) NOT NULL,
    category VARCHAR(40) NOT NULL,
    quantity NUMERIC(12,2) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    min_threshold NUMERIC(12,2) NOT NULL DEFAULT 10,
    unit_cost NUMERIC(12,2) NOT NULL,
    supplier_name VARCHAR(160),
    supplier_contact VARCHAR(40),
    status VARCHAR(20) NOT NULL DEFAULT 'IN_STOCK',
    last_restocked_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS expense (
    id UUID PRIMARY KEY,
    project_id UUID REFERENCES project(id),
    project_title VARCHAR(200),
    site_id UUID REFERENCES site(id),
    category VARCHAR(40) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    description TEXT,
    paid_to VARCHAR(160),
    payment_mode VARCHAR(30) NOT NULL,
    expense_date DATE NOT NULL,
    approved_by VARCHAR(120),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS daily_progress_report (
    id UUID PRIMARY KEY,
    project_id UUID REFERENCES project(id),
    project_title VARCHAR(200),
    site_id UUID REFERENCES site(id),
    site_name VARCHAR(160),
    report_date DATE NOT NULL,
    reported_by VARCHAR(120) NOT NULL,
    reported_by_user_id UUID REFERENCES app_user(id),
    weather_condition VARCHAR(40) NOT NULL,
    skilled_labor_count INTEGER NOT NULL DEFAULT 0,
    unskilled_labor_count INTEGER NOT NULL DEFAULT 0,
    tasks_completed TEXT NOT NULL,
    delays_or_issues TEXT,
    safety_observations TEXT,
    photos TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'SUBMITTED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS system_audit_log (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES app_user(id),
    user_name VARCHAR(120),
    role VARCHAR(30) NOT NULL,
    action VARCHAR(60) NOT NULL,
    target_type VARCHAR(40) NOT NULL,
    target_id VARCHAR(60),
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Triggers for updated_at
CREATE TRIGGER trg_site_updated_at BEFORE UPDATE ON site FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_project_milestone_updated_at BEFORE UPDATE ON project_milestone FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_attendance_updated_at BEFORE UPDATE ON attendance FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_worker_salary_updated_at BEFORE UPDATE ON worker_salary FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_invoice_updated_at BEFORE UPDATE ON invoice FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_material_updated_at BEFORE UPDATE ON material FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_expense_updated_at BEFORE UPDATE ON expense FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_dpr_updated_at BEFORE UPDATE ON daily_progress_report FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- Indexes for performance & security filtering
CREATE INDEX IF NOT EXISTS idx_site_project ON site (project_id);
CREATE INDEX IF NOT EXISTS idx_project_milestone_project ON project_milestone (project_id, order_index);
CREATE INDEX IF NOT EXISTS idx_attendance_worker ON attendance (worker_user_id, work_date DESC);
CREATE INDEX IF NOT EXISTS idx_attendance_site ON attendance (site_id, work_date DESC);
CREATE INDEX IF NOT EXISTS idx_worker_salary_worker ON worker_salary (worker_user_id, salary_month DESC);
CREATE INDEX IF NOT EXISTS idx_invoice_client ON invoice (client_user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_invoice_project ON invoice (project_id);
CREATE INDEX IF NOT EXISTS idx_material_project ON material (project_id);
CREATE INDEX IF NOT EXISTS idx_expense_project ON expense (project_id, expense_date DESC);
CREATE INDEX IF NOT EXISTS idx_dpr_project ON daily_progress_report (project_id, report_date DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_created ON system_audit_log (created_at DESC);
