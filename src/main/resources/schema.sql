-- Schema initialization for in-studio
-- ============================================================
-- FITNESS STUDIO BOOKING SYSTEM
-- PostgreSQL 17 DDL
-- ============================================================

BEGIN;

-- ============================================================
-- CUSTOM ENUM TYPES
-- ============================================================

CREATE TYPE gender_type        AS ENUM ('M', 'F', 'OTHER');
CREATE TYPE user_role           AS ENUM ('CLIENT', 'ADMIN', 'STAFF');
CREATE TYPE membership_status   AS ENUM ('ACTIVE', 'EXPIRED', 'CANCELLED', 'FROZEN');
CREATE TYPE session_status      AS ENUM ('SCHEDULED', 'CANCELLED', 'COMPLETED');
CREATE TYPE booking_status      AS ENUM ('CONFIRMED', 'CANCELLED', 'NO_SHOW', 'ATTENDED');
CREATE TYPE plan_type           AS ENUM ('PACK', 'UNLIMITED', 'MONTHLY', 'DROP_IN');
CREATE TYPE payment_method      AS ENUM ('CARD', 'CASH', 'TRANSFER', 'PAYPAL', 'STRIPE');
CREATE TYPE payment_status      AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED');

-- ============================================================
-- TABLE: discipline
-- ============================================================

CREATE TABLE discipline (
    discipline_id   INT             GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL UNIQUE,
    description     TEXT,
    color_hex       VARCHAR(7),
    icon            VARCHAR(50),
    active          BOOLEAN         NOT NULL DEFAULT TRUE
);

COMMENT ON TABLE  discipline                IS 'Class types: Pilates, Spinning, Functional, Barre, etc.';
COMMENT ON COLUMN discipline.color_hex      IS 'Hex color for UI display, e.g. #FF5733';

-- ============================================================
-- TABLE: instructor
-- ============================================================

CREATE TABLE instructor (
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
);

COMMENT ON TABLE instructor IS 'Coaches / trainers that lead class sessions';

-- ============================================================
-- TABLE: room
-- ============================================================

CREATE TABLE room (
    room_id         INT             GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL,
    capacity        INT             NOT NULL CHECK (capacity > 0),
    location        VARCHAR(150),
    equipment       TEXT,
    active          BOOLEAN         NOT NULL DEFAULT TRUE
);

COMMENT ON TABLE room IS 'Physical rooms or studios where classes take place';

-- ============================================================
-- TABLE: "user" (quoted — reserved word in PostgreSQL)
-- ============================================================

CREATE TABLE "user" (
    user_id         BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email           VARCHAR(150)    NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    first_name      VARCHAR(80)     NOT NULL,
    last_name       VARCHAR(80)     NOT NULL,
    phone           VARCHAR(20),
    birthdate       DATE,
    gender          gender_type,
    role            user_role       NOT NULL DEFAULT 'CLIENT',
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    email_verified  BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE "user" IS 'Registered clients and staff';

CREATE INDEX idx_user_email ON "user" (email);

-- ============================================================
-- TABLE: email_confirmation_token
-- ============================================================

CREATE TABLE email_confirmation_token (
    id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token           VARCHAR(255)    NOT NULL UNIQUE,
    user_id         BIGINT          NOT NULL REFERENCES "user" (user_id)
                                        ON DELETE CASCADE,
    expires_at      TIMESTAMPTZ     NOT NULL,
    used            BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE email_confirmation_token IS 'Tokens for email verification after registration';

CREATE INDEX idx_ect_token   ON email_confirmation_token (token);
CREATE INDEX idx_ect_user_id ON email_confirmation_token (user_id);

-- ============================================================
-- TABLE: plan
-- ============================================================

CREATE TABLE plan (
    plan_id         INT             GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    credits         INT             NOT NULL CHECK (credits >= 0),
    price           NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    duration_days   INT             NOT NULL CHECK (duration_days > 0),
    type            plan_type       NOT NULL,
    discipline_id   INT             REFERENCES discipline (discipline_id)
                                        ON DELETE SET NULL,
    active          BOOLEAN         NOT NULL DEFAULT TRUE
);

COMMENT ON TABLE  plan                  IS 'Membership plans available for purchase';
COMMENT ON COLUMN plan.credits          IS 'Number of class credits included; 0 for UNLIMITED';
COMMENT ON COLUMN plan.discipline_id    IS 'NULL means the plan is valid for all disciplines';

-- ============================================================
-- TABLE: membership
-- ============================================================

CREATE TABLE membership (
    membership_id   BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES "user" (user_id)
                                        ON DELETE CASCADE,
    plan_id         INT             NOT NULL REFERENCES plan (plan_id)
                                        ON DELETE RESTRICT,
    start_date      DATE            NOT NULL,
    end_date        DATE            NOT NULL,
    credits_left    INT             NOT NULL CHECK (credits_left >= 0),
    status          membership_status NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_membership_dates CHECK (end_date >= start_date)
);

COMMENT ON TABLE membership IS 'Purchased memberships linking users to plans';

CREATE INDEX idx_membership_user   ON membership (user_id);
CREATE INDEX idx_membership_status ON membership (status) WHERE status = 'ACTIVE';

-- ============================================================
-- TABLE: class_session
-- ============================================================

CREATE TABLE class_session (
    session_id      BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    discipline_id   INT             NOT NULL REFERENCES discipline (discipline_id)
                                        ON DELETE RESTRICT,
    instructor_id   INT             NOT NULL REFERENCES instructor (instructor_id)
                                        ON DELETE RESTRICT,
    room_id         INT             NOT NULL REFERENCES room (room_id)
                                        ON DELETE RESTRICT,
    start_datetime  TIMESTAMPTZ     NOT NULL,
    end_datetime    TIMESTAMPTZ     NOT NULL,
    capacity        INT             NOT NULL CHECK (capacity > 0),
    booked_count    INT             NOT NULL DEFAULT 0 CHECK (booked_count >= 0),
    status          session_status  NOT NULL DEFAULT 'SCHEDULED',
    notes           TEXT,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_session_times CHECK (end_datetime > start_datetime)
);

COMMENT ON TABLE class_session IS 'Scheduled class instances on the calendar';

CREATE INDEX idx_session_start       ON class_session (start_datetime);
CREATE INDEX idx_session_discipline  ON class_session (discipline_id);
CREATE INDEX idx_session_instructor  ON class_session (instructor_id);

-- Prevent overlapping sessions in the same room (exclusion constraint)
-- Requires btree_gist extension
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE class_session
    ADD CONSTRAINT excl_room_no_overlap
    EXCLUDE USING GIST (
        room_id WITH =,
        TSTZRANGE(start_datetime, end_datetime) WITH &&
    )
    WHERE (status <> 'CANCELLED');

-- ============================================================
-- TABLE: booking
-- ============================================================

CREATE TABLE booking (
    booking_id      BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES "user" (user_id)
                                        ON DELETE CASCADE,
    session_id      BIGINT          NOT NULL REFERENCES class_session (session_id)
                                        ON DELETE RESTRICT,
    membership_id   BIGINT          NOT NULL REFERENCES membership (membership_id)
                                        ON DELETE RESTRICT,
    status          booking_status  NOT NULL DEFAULT 'CONFIRMED',
    checked_in      BOOLEAN         NOT NULL DEFAULT FALSE,
    booked_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    cancelled_at    TIMESTAMPTZ,

    CONSTRAINT uq_user_session UNIQUE (user_id, session_id)
);

COMMENT ON TABLE booking IS 'Class reservations made by users';

CREATE INDEX idx_booking_session ON booking (session_id);
CREATE INDEX idx_booking_user    ON booking (user_id);

-- ============================================================
-- TABLE: waitlist
-- ============================================================

CREATE TABLE waitlist (
    waitlist_id     BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id         BIGINT          NOT NULL REFERENCES "user" (user_id)
                                        ON DELETE CASCADE,
    session_id      BIGINT          NOT NULL REFERENCES class_session (session_id)
                                        ON DELETE CASCADE,
    position        INT             NOT NULL CHECK (position > 0),
    notified        BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_waitlist_user_session UNIQUE (user_id, session_id)
);

COMMENT ON TABLE waitlist IS 'Queue for full classes; auto-promote when a spot opens';

CREATE INDEX idx_waitlist_session ON waitlist (session_id, position);

-- ============================================================
-- TABLE: payment
-- ============================================================

CREATE TABLE payment (
    payment_id      BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    membership_id   BIGINT          NOT NULL REFERENCES membership (membership_id)
                                        ON DELETE RESTRICT,
    amount          NUMERIC(10, 2) NOT NULL CHECK (amount > 0),
    currency        CHAR(3)         NOT NULL DEFAULT 'USD',
    method          payment_method  NOT NULL,
    status          payment_status  NOT NULL DEFAULT 'PENDING',
    transaction_ref VARCHAR(100),
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE payment IS 'Payment transactions linked to memberships';

CREATE INDEX idx_payment_membership ON payment (membership_id);
CREATE INDEX idx_payment_status     ON payment (status) WHERE status = 'PENDING';

-- ============================================================
-- FUNCTION: auto-update updated_at on "user"
-- ============================================================

CREATE OR REPLACE FUNCTION trg_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_user_updated_at
    BEFORE UPDATE ON "user"
    FOR EACH ROW
    EXECUTE FUNCTION trg_set_updated_at();

-- ============================================================
-- SEED: default disciplines
-- ============================================================

INSERT INTO discipline (name, description, color_hex, icon) VALUES
    ('Pilates',     'Mat and reformer Pilates classes',         '#6C63FF', 'pilates'),
    ('Spinning',    'Indoor cycling / spin classes',            '#FF6B6B', 'spinning'),
    ('Functional',  'Functional training and HIIT workouts',    '#4ECDC4', 'functional'),
    ('Barre',       'Ballet-inspired strength and flexibility', '#FFD93D', 'barre');

COMMIT;
