-- Run after V1 against a disposable database. All deterministic test rows roll back.
\set ON_ERROR_STOP on
BEGIN;

INSERT INTO app_user (id) VALUES
    ('20000000-0000-0000-0000-000000000001'),
    ('20000000-0000-0000-0000-000000000002'),
    ('20000000-0000-0000-0000-000000000003');

INSERT INTO user_role (user_id, role_id)
SELECT '20000000-0000-0000-0000-000000000001', id FROM app_role WHERE code IN ('WORKER', 'CONTRACTOR');
INSERT INTO user_role (user_id, role_id)
SELECT '20000000-0000-0000-0000-000000000002', id FROM app_role WHERE code = 'CLIENT';
INSERT INTO user_role (user_id, role_id)
SELECT '20000000-0000-0000-0000-000000000003', id FROM app_role WHERE code = 'CONTRACTOR';

DO $$
BEGIN
    IF (SELECT COUNT(*) FROM user_role WHERE user_id = '20000000-0000-0000-0000-000000000001') <> 2 THEN
        RAISE EXCEPTION 'A user must support multiple roles';
    END IF;
END;
$$;

INSERT INTO worker_profile (id, user_id, display_name, location)
VALUES ('30000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Schema Test Worker', 'Mohali');
INSERT INTO worker_skill (worker_profile_id, skill_id)
VALUES ('30000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001');
INSERT INTO contractor_profile (id, user_id, display_name, location)
VALUES ('40000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Schema Test Contractor', 'Mohali');
INSERT INTO contractor_profile (id, user_id, display_name, location)
VALUES ('40000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000003', 'Second Schema Test Contractor', 'Mohali');
INSERT INTO contractor_worker_association (contractor_profile_id, worker_profile_id, starts_on, ends_on)
VALUES ('40000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', DATE '2026-01-01', DATE '2026-01-31');
INSERT INTO contractor_worker_association (contractor_profile_id, worker_profile_id, starts_on)
VALUES ('40000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', DATE '2026-02-01');
INSERT INTO team (id, manager_user_id, name)
VALUES ('50000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Schema Test Team');
INSERT INTO team_member (team_id, worker_profile_id, starts_on, ends_on)
VALUES ('50000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', DATE '2026-02-01', DATE '2026-02-28');
INSERT INTO team (id, manager_user_id, name)
VALUES ('50000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000003', 'Second Schema Test Team');
INSERT INTO team_member (team_id, worker_profile_id, starts_on)
VALUES ('50000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', DATE '2026-03-01');

INSERT INTO client_profile (id, user_id, client_type, display_name)
VALUES ('60000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000002', 'HOMEOWNER', 'Schema Test Client');
INSERT INTO project (id, client_profile_id, title, location)
VALUES ('70000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', 'Schema Test Project', 'Mohali');
INSERT INTO workforce_requirement (id, project_id, location, start_date, duration_days, worker_type, skill_id, quantity, daily_rate, status)
VALUES ('80000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001', 'Mohali', DATE '2026-09-01', 45, 'SKILLED_WORKER', '10000000-0000-0000-0000-000000000001', 2, 1500.00, 'OPEN');
INSERT INTO workforce_requirement (id, project_id, location, start_date, duration_days, worker_type, skill_id, quantity, budget_amount, status)
VALUES ('80000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000001', 'Mohali', DATE '2026-09-01', 45, 'LABOUR', '10000000-0000-0000-0000-000000000013', 5, 45000.00, 'OPEN');
INSERT INTO workforce_requirement (id, project_id, location, start_date, duration_days, worker_type, skill_id, quantity, status)
VALUES ('80000000-0000-0000-0000-000000000003', '70000000-0000-0000-0000-000000000001', 'Mohali', DATE '2026-09-01', 45, 'LABOUR', '10000000-0000-0000-0000-000000000013', 3, 'OPEN');

INSERT INTO booking (id, requirement_id, provider_type, provider_worker_profile_id, quantity)
VALUES ('90000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000001', 'WORKER', '30000000-0000-0000-0000-000000000001', 1);
INSERT INTO booking (id, requirement_id, provider_type, provider_team_id, quantity)
VALUES ('90000000-0000-0000-0000-000000000002', '80000000-0000-0000-0000-000000000002', 'TEAM', '50000000-0000-0000-0000-000000000001', 3);
INSERT INTO booking (id, requirement_id, provider_type, provider_contractor_profile_id, quantity, status, responded_at)
VALUES ('90000000-0000-0000-0000-000000000003', '80000000-0000-0000-0000-000000000002', 'CONTRACTOR', '40000000-0000-0000-0000-000000000001', 4, 'ACCEPTED', CURRENT_TIMESTAMP);

DO $$
BEGIN
    BEGIN
        INSERT INTO user_role (user_id, role_id)
        SELECT '20000000-0000-0000-0000-000000000001', id FROM app_role WHERE code = 'WORKER';
        RAISE EXCEPTION 'Duplicate user role should fail';
    EXCEPTION WHEN unique_violation THEN
        NULL;
    END;

    BEGIN
        INSERT INTO worker_skill (worker_profile_id, skill_id)
        VALUES ('30000000-0000-0000-0000-000000000001', 'ffffffff-ffff-ffff-ffff-ffffffffffff');
        RAISE EXCEPTION 'Unknown skill should fail foreign key validation';
    EXCEPTION WHEN foreign_key_violation THEN
        NULL;
    END;

    BEGIN
        INSERT INTO booking (id, requirement_id, provider_type, quantity)
        VALUES ('90000000-0000-0000-0000-000000000004', '80000000-0000-0000-0000-000000000001', 'WORKER', 1);
        RAISE EXCEPTION 'Invalid booking provider should fail';
    EXCEPTION WHEN check_violation THEN
        NULL;
    END;

    BEGIN
        INSERT INTO booking (id, requirement_id, provider_type, provider_worker_profile_id, quantity)
        VALUES ('90000000-0000-0000-0000-000000000005', '80000000-0000-0000-0000-000000000001', 'WORKER', '30000000-0000-0000-0000-000000000001', 2);
        RAISE EXCEPTION 'Worker booking quantity above one should fail';
    EXCEPTION WHEN check_violation THEN
        NULL;
    END;

    BEGIN
        INSERT INTO workforce_requirement (id, project_id, location, start_date, duration_days, worker_type, skill_id, quantity, daily_rate)
        VALUES ('80000000-0000-0000-0000-000000000004', '70000000-0000-0000-0000-000000000001', 'Mohali', DATE '2026-09-01', 45, 'SKILLED_WORKER', '10000000-0000-0000-0000-000000000001', 1, -1.00);
        RAISE EXCEPTION 'Negative daily rate should fail';
    EXCEPTION WHEN check_violation THEN
        NULL;
    END;

    BEGIN
        INSERT INTO workforce_requirement (id, project_id, location, start_date, duration_days, worker_type, skill_id, quantity, budget_amount)
        VALUES ('80000000-0000-0000-0000-000000000005', '70000000-0000-0000-0000-000000000001', 'Mohali', DATE '2026-09-01', 45, 'LABOUR', '10000000-0000-0000-0000-000000000013', 1, -1.00);
        RAISE EXCEPTION 'Negative budget amount should fail';
    EXCEPTION WHEN check_violation THEN
        NULL;
    END;
END;
$$;

ROLLBACK;
