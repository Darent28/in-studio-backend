-- Schema initialization for in-studio
-- ============================================================
-- FITNESS STUDIO BOOKING SYSTEM
-- PostgreSQL 17 DDL
-- Statement separator: ;; (Spring Boot spring.sql.init.separator=;;)
-- No BEGIN/COMMIT — Spring Boot's JDBC executor runs in autocommit mode.
-- ============================================================

-- ============================================================
-- DROP REMOVED COLUMNS / TYPES (idempotent)
-- ============================================================

ALTER TABLE "user"        DROP COLUMN IF EXISTS gender;;
ALTER TABLE instructor    DROP COLUMN IF EXISTS first_name;;
ALTER TABLE instructor    DROP COLUMN IF EXISTS last_name;;
ALTER TABLE instructor    DROP COLUMN IF EXISTS email;;
ALTER TABLE instructor    DROP COLUMN IF EXISTS phone;;
ALTER TABLE class_session DROP COLUMN IF EXISTS capacity;;
DROP INDEX IF EXISTS membership_one_active_per_user;;
ALTER TABLE membership    DROP COLUMN IF EXISTS payment_method;;
ALTER TABLE membership    DROP COLUMN IF EXISTS payment_status;;
ALTER TABLE membership    DROP COLUMN IF EXISTS stripe_payment_intent_id;;
ALTER TABLE membership    DROP COLUMN IF EXISTS plan_id;;
ALTER TABLE membership    ADD COLUMN IF NOT EXISTS start_date    DATE;;
ALTER TABLE membership    ADD COLUMN IF NOT EXISTS end_date      DATE;;
ALTER TABLE membership    ADD COLUMN IF NOT EXISTS credits_total INT NOT NULL DEFAULT 0;;
ALTER TABLE payment       ADD COLUMN IF NOT EXISTS plan_id    INT          REFERENCES plan (plan_id);;
DROP TYPE IF EXISTS gender_type;;

-- ============================================================
-- CUSTOM ENUM TYPES
-- ============================================================

DO $$ BEGIN CREATE TYPE user_role         AS ENUM ('CLIENT', 'ADMIN', 'STAFF');                    EXCEPTION WHEN duplicate_object THEN NULL; END $$;;
DO $$ BEGIN CREATE TYPE membership_status AS ENUM ('ACTIVE', 'EXPIRED', 'CANCELLED', 'FROZEN');    EXCEPTION WHEN duplicate_object THEN NULL; END $$;;
DO $$ BEGIN CREATE TYPE session_status    AS ENUM ('SCHEDULED', 'CANCELLED', 'COMPLETED');         EXCEPTION WHEN duplicate_object THEN NULL; END $$;;
DO $$ BEGIN CREATE TYPE plan_type         AS ENUM ('PACK', 'UNLIMITED', 'MONTHLY', 'DROP_IN');     EXCEPTION WHEN duplicate_object THEN NULL; END $$;;
DO $$ BEGIN CREATE TYPE payment_method    AS ENUM ('CARD', 'CASH', 'TRANSFER', 'PAYPAL', 'STRIPE'); EXCEPTION WHEN duplicate_object THEN NULL; END $$;;
DO $$ BEGIN CREATE TYPE payment_status    AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED');  EXCEPTION WHEN duplicate_object THEN NULL; END $$;;

-- ============================================================
-- TABLE: instructor
-- ============================================================

CREATE TABLE IF NOT EXISTS instructor (
    instructor_id   INT             GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name      VARCHAR(80)     NOT NULL,
    last_name       VARCHAR(80)     NOT NULL,
    email           VARCHAR(150)    UNIQUE,
    phone           VARCHAR(20),
    bio             TEXT,
    specialty       VARCHAR(100),
    photo_url       VARCHAR(255),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);;

COMMENT ON TABLE instructor IS 'Coaches / trainers that lead class sessions';;

ALTER TABLE instructor ADD COLUMN IF NOT EXISTS bio         TEXT;;
ALTER TABLE instructor ADD COLUMN IF NOT EXISTS specialty   VARCHAR(100);;
ALTER TABLE instructor ADD COLUMN IF NOT EXISTS photo_url   VARCHAR(255);;
ALTER TABLE instructor ADD COLUMN IF NOT EXISTS active      BOOLEAN      NOT NULL DEFAULT TRUE;;
ALTER TABLE instructor ADD COLUMN IF NOT EXISTS created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW();;
ALTER TABLE instructor ADD COLUMN IF NOT EXISTS user_id     BIGINT       REFERENCES "user" (user_id) ON DELETE SET NULL;;

-- ============================================================
-- TABLE: room
-- ============================================================

CREATE TABLE IF NOT EXISTS room (
    room_id         INT             GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL,
    capacity        INT             NOT NULL CHECK (capacity > 0),
    location        VARCHAR(150),
    equipment       TEXT,
    active          BOOLEAN         NOT NULL DEFAULT TRUE
);;

COMMENT ON TABLE room IS 'Physical rooms or studios where classes take place';;

ALTER TABLE room ADD COLUMN IF NOT EXISTS layout_rows INT NOT NULL DEFAULT 0;;
ALTER TABLE room ADD COLUMN IF NOT EXISTS layout_cols INT NOT NULL DEFAULT 0;;
ALTER TABLE room ADD COLUMN IF NOT EXISTS layout_data TEXT;;

-- ============================================================
-- TABLE: "user" (quoted — reserved word in PostgreSQL)
-- ============================================================

CREATE TABLE IF NOT EXISTS "user" (
    user_id         BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email           VARCHAR(150)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    first_name      VARCHAR(80)     NOT NULL,
    last_name       VARCHAR(80)     NOT NULL,
    phone           VARCHAR(20),
    birthdate       DATE,
    role            user_role       NOT NULL DEFAULT 'CLIENT',
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    email_verified  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);;

COMMENT ON TABLE "user" IS 'Registered clients and staff';;

CREATE INDEX IF NOT EXISTS idx_user_email ON "user" (email);;

-- ============================================================
-- TABLE: email_confirmation_token
-- ============================================================

CREATE TABLE IF NOT EXISTS email_confirmation_token (
    id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token           VARCHAR(255)    NOT NULL UNIQUE,
    user_id         BIGINT          NOT NULL REFERENCES "user" (user_id)
                                        ON DELETE CASCADE,
    expires_at      TIMESTAMPTZ     NOT NULL,
    used            BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);;

COMMENT ON TABLE email_confirmation_token IS 'Tokens for email verification after registration';;

CREATE INDEX IF NOT EXISTS idx_ect_token   ON email_confirmation_token (token);;
CREATE INDEX IF NOT EXISTS idx_ect_user_id ON email_confirmation_token (user_id);;

-- ============================================================
-- TABLE: plan
-- ============================================================

CREATE TABLE IF NOT EXISTS plan (
    plan_id         INT             GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    credits         INT             NOT NULL CHECK (credits >= 0),
    price           NUMERIC(10, 2)  NOT NULL CHECK (price >= 0),
    duration_days   INT             NOT NULL CHECK (duration_days > 0),
    type            plan_type       NOT NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE
);;

COMMENT ON TABLE  plan         IS 'Membership plans available for purchase';;
COMMENT ON COLUMN plan.credits IS 'Number of class credits included; 0 for UNLIMITED';;

-- ============================================================
-- TABLE: membership
-- ============================================================

CREATE TABLE IF NOT EXISTS membership (
    membership_id   BIGINT            GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT            NOT NULL REFERENCES "user" (user_id)
                                          ON DELETE CASCADE,
    plan_id         INT               NOT NULL REFERENCES plan (plan_id)
                                          ON DELETE RESTRICT,
    start_date      DATE              NOT NULL,
    end_date        DATE              NOT NULL,
    credits_left    INT               NOT NULL CHECK (credits_left >= 0),
    status          membership_status NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ       NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_membership_dates CHECK (end_date >= start_date)
);;

COMMENT ON TABLE membership IS 'Purchased memberships linking users to plans';;

CREATE INDEX IF NOT EXISTS idx_membership_user   ON membership (user_id);;
CREATE INDEX IF NOT EXISTS idx_membership_status ON membership (status) WHERE status = 'ACTIVE';;

-- ============================================================
-- TABLE: class_session
-- ============================================================

CREATE TABLE IF NOT EXISTS class_session (
    session_id      BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    instructor_id   INT             NOT NULL REFERENCES instructor (instructor_id)
                                        ON DELETE RESTRICT,
    room_id         INT             NOT NULL REFERENCES room (room_id)
                                        ON DELETE RESTRICT,
    start_time      TIME            NOT NULL,
    end_time        TIME            NOT NULL,
    days_of_week    SMALLINT        NOT NULL DEFAULT 0,
    scheduled_count INT             NOT NULL DEFAULT 0 CHECK (scheduled_count >= 0),
    status          session_status  NOT NULL DEFAULT 'SCHEDULED',
    notes           TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_session_times CHECK (end_time > start_time)
);;

COMMENT ON TABLE class_session IS 'Scheduled recurring class sessions with day/time pattern';;

CREATE INDEX IF NOT EXISTS idx_session_instructor ON class_session (instructor_id);;
CREATE INDEX IF NOT EXISTS idx_session_room       ON class_session (room_id);;

-- ============================================================
-- TABLE: waitlist
-- ============================================================

CREATE TABLE IF NOT EXISTS waitlist (
    waitlist_id     BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES "user" (user_id)
                                        ON DELETE CASCADE,
    session_id      BIGINT          NOT NULL REFERENCES class_session (session_id)
                                        ON DELETE CASCADE,
    position        INT             NOT NULL CHECK (position > 0),
    notified        BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_waitlist_user_session UNIQUE (user_id, session_id)
);;

COMMENT ON TABLE waitlist IS 'Queue for full classes; auto-promote when a spot opens';;

CREATE INDEX IF NOT EXISTS idx_waitlist_session ON waitlist (session_id, position);;

-- ============================================================
-- TABLE: payment
-- ============================================================

CREATE TABLE IF NOT EXISTS payment (
    payment_id      BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    membership_id   BIGINT          NOT NULL REFERENCES membership (membership_id)
                                        ON DELETE RESTRICT,
    amount          NUMERIC(10, 2)  NOT NULL CHECK (amount > 0),
    currency        CHAR(3)         NOT NULL DEFAULT 'USD',
    method          payment_method  NOT NULL,
    status          payment_status  NOT NULL DEFAULT 'PENDING',
    transaction_ref VARCHAR(100),
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);;

COMMENT ON TABLE payment IS 'Payment transactions linked to memberships';;

CREATE INDEX IF NOT EXISTS idx_payment_membership ON payment (membership_id);;
CREATE INDEX IF NOT EXISTS idx_payment_status     ON payment (status) WHERE status = 'PENDING';;

-- ============================================================
-- FUNCTION + TRIGGER: delete all expired tokens before a new one is inserted
-- ============================================================

CREATE OR REPLACE FUNCTION trg_delete_expired_tokens()
RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM email_confirmation_token
    WHERE expires_at < NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;;

DROP TRIGGER IF EXISTS trg_ect_cleanup_expired ON email_confirmation_token;;
CREATE TRIGGER trg_ect_cleanup_expired
    BEFORE INSERT ON email_confirmation_token
    FOR EACH STATEMENT
    EXECUTE FUNCTION trg_delete_expired_tokens();;

-- ============================================================
-- FUNCTION: auto-update updated_at on "user"
-- ============================================================

CREATE OR REPLACE FUNCTION trg_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;;

DROP TRIGGER IF EXISTS trg_user_updated_at ON "user";;
CREATE TRIGGER trg_user_updated_at
    BEFORE UPDATE ON "user"
    FOR EACH ROW
    EXECUTE FUNCTION trg_set_updated_at();;

