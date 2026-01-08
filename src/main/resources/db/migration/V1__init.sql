CREATE TABLE users (
    user_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    provider VARCHAR(20) NOT NULL DEFAULT 'local'
        CHECK (provider IN ('local', 'kakao', 'google', 'naver')),

    provider_id VARCHAR(100),

    email VARCHAR(255),
    password VARCHAR(255),

    nickname VARCHAR(100) NOT NULL UNIQUE,

    age INT CHECK (age > 0),
    gender INT CHECK (gender IN (0, 1))
);

-- 로컬 이메일 중복
CREATE UNIQUE INDEX unique_lower_email 
    ON users (LOWER(email)) WHERE provider = 'local';

-- SNS 중복 (provider + provider_id)
CREATE UNIQUE INDEX uniq_user_provider 
    ON users (provider, provider_id);


CREATE TABLE preferred_theme_category (
    preferred_theme_category_id INT PRIMARY KEY,
    preferred_theme_category_name VARCHAR(100) NOT NULL
);

CREATE TABLE preferred_theme (
    preferred_theme_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    preferred_theme_name VARCHAR(100) NOT NULL,
    preferred_theme_category_id INT NOT NULL,
    FOREIGN KEY (preferred_theme_category_id) REFERENCES preferred_theme_category(preferred_theme_category_id)
);

CREATE TABLE user_preferred_theme (
    user_id INT NOT NULL,
    preferred_theme_id INT NOT NULL,
    PRIMARY KEY (user_id, preferred_theme_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, 
    FOREIGN KEY (preferred_theme_id) REFERENCES preferred_theme(preferred_theme_id)
);

CREATE TABLE transportation_category (
    transportation_category_id INT PRIMARY KEY,
    transportation_category_name VARCHAR(100) NOT NULL
);

CREATE TABLE travel_category (
    travel_category_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    travel_category_name VARCHAR(100) NOT NULL
);

CREATE TABLE travel (
    travel_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    travel_name VARCHAR(100) NOT NULL,
    travel_category_id INT NOT NULL,
    FOREIGN KEY (travel_category_id) REFERENCES travel_category(travel_category_id)
);

CREATE TABLE place_photo (
    place_id VARCHAR(100) PRIMARY KEY,
    photo_url TEXT NOT NULL
);

CREATE TABLE plan (
    plan_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    plan_name VARCHAR(100) NOT NULL,
    departure VARCHAR(100) NOT NULL,
    adult_count INT NOT NULL CHECK (adult_count >= 0),
    child_count INT NOT NULL CHECK (child_count >= 0),
    user_id INT NOT NULL,
    transportation_category_id INT NOT NULL,
    travel_id INT NOT NULL,
    UNIQUE (user_id, plan_name),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (transportation_category_id) REFERENCES transportation_category(transportation_category_id),
    FOREIGN KEY (travel_id) REFERENCES travel(travel_id)
);

CREATE TABLE plan_share (
    plan_id INTEGER PRIMARY KEY,
    share_token VARCHAR(128) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (plan_id) REFERENCES plan(plan_id) ON DELETE CASCADE
);

CREATE TABLE time_table (
    time_table_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    date DATE NOT NULL,
    time_table_start_time TIME NOT NULL,
    time_table_end_time TIME NOT NULL,
    plan_id INT NOT NULL,
    FOREIGN KEY (plan_id) REFERENCES plan(plan_id) ON DELETE CASCADE 
);

CREATE TABLE place_category (
    place_category_id INT PRIMARY KEY,
    place_category_name VARCHAR(100) NOT NULL
);

CREATE TABLE time_table_place_block (
    block_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    place_name VARCHAR(255) NOT NULL,
    place_theme VARCHAR(100) NOT NULL,
    place_rating NUMERIC(2,1) NOT NULL CHECK (place_rating BETWEEN 0 AND 5),
    place_address TEXT NOT NULL,
    place_link TEXT NOT NULL,
    block_start_time TIME NOT NULL,
    block_end_time TIME NOT NULL,
    x_location DOUBLE PRECISION NOT NULL,
    y_location DOUBLE PRECISION NOT NULL,
    place_category_id INT NOT NULL,
    place_id VARCHAR(100) NOT NULL,
    time_table_id INT NOT NULL,
    UNIQUE (block_start_time, time_table_id),
    FOREIGN KEY (place_category_id) REFERENCES place_category(place_category_id),
    FOREIGN KEY (place_id) REFERENCES place_photo(place_id) ON DELETE RESTRICT,
    FOREIGN KEY (time_table_id) REFERENCES time_table(time_table_id) ON DELETE CASCADE
);

CREATE TABLE plan_editor (
    plan_editor_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id        INTEGER NOT NULL,
    plan_id        INTEGER NOT NULL,
    UNIQUE (user_id, plan_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES plan(plan_id) ON DELETE CASCADE
);

CREATE TABLE collaboration_request (
    collaboration_request_id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    collaboration_request_type VARCHAR(10) NOT NULL CHECK (collaboration_request_type IN ('INVITE', 'REQUEST')),
    collaboration_request_status VARCHAR(10) NOT NULL CHECK (collaboration_request_status IN ('PENDING', 'APPROVED', 'DENIED', 'ACCEPTED', 'DECLINED')),
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sender_id INTEGER NOT NULL,
    receiver_id INTEGER NOT NULL,
    plan_id INTEGER NOT NULL,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (plan_id) REFERENCES plan(plan_id) ON DELETE CASCADE
);
CREATE UNIQUE INDEX unique_pending_invite ON collaboration_request (
    sender_id, receiver_id, plan_id, collaboration_request_type
)
WHERE collaboration_request_status = 'PENDING';


CREATE TABLE beta_feedback (
    feedback_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    content     TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
