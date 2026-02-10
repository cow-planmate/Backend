CREATE TABLE users (
                       user_id UUID,
                       provider VARCHAR(20) NOT NULL DEFAULT 'local',
                       provider_id VARCHAR(100),
                       email VARCHAR(255),
                       password VARCHAR(255),
                       nickname VARCHAR(100) NOT NULL,
                       age INT,
                       gender INT,

                       CONSTRAINT pk_users PRIMARY KEY (user_id),
                       CONSTRAINT chk_users_provider
                           CHECK (provider IN ('local', 'kakao', 'google', 'naver')),
                       CONSTRAINT chk_users_age
                           CHECK (age > 0),
                       CONSTRAINT chk_users_gender
                           CHECK (gender IN (0, 1)),
                       CONSTRAINT uk_users_nickname UNIQUE (nickname)
);

CREATE UNIQUE INDEX uk_users_local_email
    ON users (LOWER(email))
    WHERE provider = 'local';

CREATE UNIQUE INDEX uk_users_provider_provider_id
    ON users (provider, provider_id);


CREATE TABLE preferred_theme_category (
                                          preferred_theme_category_id INT,
                                          preferred_theme_category_name VARCHAR(100) NOT NULL,

                                          CONSTRAINT pk_preferred_theme_category
                                              PRIMARY KEY (preferred_theme_category_id)
);


CREATE TABLE preferred_theme (
                                 preferred_theme_id INTEGER GENERATED ALWAYS AS IDENTITY,
                                 preferred_theme_name VARCHAR(100) NOT NULL,
                                 preferred_theme_category_id INT NOT NULL,

                                 CONSTRAINT pk_preferred_theme
                                     PRIMARY KEY (preferred_theme_id),
                                 CONSTRAINT fk_preferred_theme_category
                                     FOREIGN KEY (preferred_theme_category_id)
                                         REFERENCES preferred_theme_category (preferred_theme_category_id)
);

CREATE TABLE user_preferred_theme (
                                      user_id UUID NOT NULL,
                                      preferred_theme_id INT NOT NULL,

                                      CONSTRAINT pk_user_preferred_theme
                                          PRIMARY KEY (user_id, preferred_theme_id),
                                      CONSTRAINT fk_user_preferred_theme_user
                                          FOREIGN KEY (user_id)
                                              REFERENCES users (user_id)
                                              ON DELETE CASCADE,
                                      CONSTRAINT fk_user_preferred_theme_theme
                                          FOREIGN KEY (preferred_theme_id)
                                              REFERENCES preferred_theme (preferred_theme_id)
);

CREATE TABLE transportation_category (
                                         transportation_category_id INT,
                                         transportation_category_name VARCHAR(100) NOT NULL,

                                         CONSTRAINT pk_transportation_category
                                             PRIMARY KEY (transportation_category_id)
);

CREATE TABLE travel_category (
                                 travel_category_id INTEGER GENERATED ALWAYS AS IDENTITY,
                                 travel_category_name VARCHAR(100) NOT NULL,

                                 CONSTRAINT pk_travel_category
                                     PRIMARY KEY (travel_category_id)
);

CREATE TABLE travel (
                        travel_id INTEGER GENERATED ALWAYS AS IDENTITY,
                        travel_name VARCHAR(100) NOT NULL,
                        travel_category_id INT NOT NULL,

                        CONSTRAINT pk_travel
                            PRIMARY KEY (travel_id),
                        CONSTRAINT fk_travel_travel_category
                            FOREIGN KEY (travel_category_id)
                                REFERENCES travel_category (travel_category_id)
);

CREATE TABLE plan (
                      plan_id UUID,
                      plan_name VARCHAR(100) NOT NULL,
                      departure VARCHAR(100) NOT NULL,
                      adult_count INT NOT NULL,
                      child_count INT NOT NULL,
                      user_id UUID NOT NULL,
                      transportation_category_id INT NOT NULL,
                      travel_id INT NOT NULL,

                      CONSTRAINT pk_plan
                          PRIMARY KEY (plan_id),
                      CONSTRAINT chk_plan_adult_count
                          CHECK (adult_count >= 0),
                      CONSTRAINT chk_plan_child_count
                          CHECK (child_count >= 0),
                      CONSTRAINT fk_plan_user
                          FOREIGN KEY (user_id)
                              REFERENCES users (user_id)
                              ON DELETE CASCADE,
                      CONSTRAINT fk_plan_transportation_category
                          FOREIGN KEY (transportation_category_id)
                              REFERENCES transportation_category (transportation_category_id),
                      CONSTRAINT fk_plan_travel
                          FOREIGN KEY (travel_id)
                              REFERENCES travel (travel_id)
);

CREATE TABLE plan_share (
                            plan_id UUID,
                            share_token VARCHAR(128) NOT NULL,
                            is_active BOOLEAN DEFAULT TRUE,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                            CONSTRAINT pk_plan_share
                                PRIMARY KEY (plan_id),
                            CONSTRAINT uk_plan_share_token
                                UNIQUE (share_token),
                            CONSTRAINT fk_plan_share_plan
                                FOREIGN KEY (plan_id)
                                    REFERENCES plan (plan_id)
                                    ON DELETE CASCADE
);

CREATE TABLE time_table (
                            time_table_id INTEGER GENERATED ALWAYS AS IDENTITY,
                            date DATE NOT NULL,
                            time_table_start_time TIME NOT NULL,
                            time_table_end_time TIME NOT NULL,
                            plan_id UUID NOT NULL,

                            CONSTRAINT pk_time_table
                                PRIMARY KEY (time_table_id),
                            CONSTRAINT fk_time_table_plan
                                FOREIGN KEY (plan_id)
                                    REFERENCES plan (plan_id)
                                    ON DELETE CASCADE
);

CREATE TABLE place_category (
                                place_category_id INT,
                                place_category_name VARCHAR(100) NOT NULL,

                                CONSTRAINT pk_place_category
                                    PRIMARY KEY (place_category_id)
);

CREATE TABLE time_table_place_block (
                                        block_id INTEGER GENERATED ALWAYS AS IDENTITY,
                                        time_table_id INT NOT NULL,

                                        place_id VARCHAR(100) NULL,
                                        place_name VARCHAR(255) NOT NULL,
                                        place_theme VARCHAR(100) NULL,
                                        place_rating NUMERIC(2,1) NULL,
                                        place_address TEXT NULL,
                                        place_link TEXT NULL,
                                        x_location DOUBLE PRECISION NULL,
                                        y_location DOUBLE PRECISION NULL,

                                        block_start_time TIME NOT NULL,
                                        block_end_time TIME NOT NULL,
                                        place_category_id INT NOT NULL,
                                        memo TEXT NULL,

                                        CONSTRAINT pk_time_table_place_block
                                            PRIMARY KEY (block_id),
                                        CONSTRAINT uk_time_table_place_block_time
                                            UNIQUE (block_start_time, time_table_id),
                                        CONSTRAINT fk_time_table_place_block_time_table
                                            FOREIGN KEY (time_table_id)
                                                REFERENCES time_table (time_table_id)
                                                ON DELETE CASCADE,
                                        CONSTRAINT fk_time_table_place_block_place_category
                                            FOREIGN KEY (place_category_id)
                                                REFERENCES place_category (place_category_id),
                                        CONSTRAINT chk_time_table_place_block_place_rating
                                            CHECK (
                                                place_rating IS NULL
                                                    OR place_rating BETWEEN 0 AND 5
                                                )
);

CREATE TABLE plan_editor (
                             plan_editor_id INTEGER GENERATED ALWAYS AS IDENTITY,
                             user_id UUID NOT NULL,
                             plan_id UUID NOT NULL,

                             CONSTRAINT pk_plan_editor
                                 PRIMARY KEY (plan_editor_id),
                             CONSTRAINT uk_plan_editor_user_plan
                                 UNIQUE (user_id, plan_id),
                             CONSTRAINT fk_plan_editor_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES users (user_id)
                                     ON DELETE CASCADE,
                             CONSTRAINT fk_plan_editor_plan
                                 FOREIGN KEY (plan_id)
                                     REFERENCES plan (plan_id)
                                     ON DELETE CASCADE
);

CREATE TABLE collaboration_request (
                                       collaboration_request_id INTEGER GENERATED ALWAYS AS IDENTITY,
                                       collaboration_request_type VARCHAR(10) NOT NULL,
                                       collaboration_request_status VARCHAR(10) NOT NULL,
                                       sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       sender_id UUID NOT NULL,
                                       receiver_id UUID NOT NULL,
                                       plan_id UUID NOT NULL,

                                       CONSTRAINT pk_collaboration_request
                                           PRIMARY KEY (collaboration_request_id),
                                       CONSTRAINT chk_collaboration_request_type
                                           CHECK (collaboration_request_type IN ('INVITE', 'REQUEST')),
                                       CONSTRAINT chk_collaboration_request_status
                                           CHECK (collaboration_request_status IN ('PENDING', 'APPROVED', 'DENIED', 'ACCEPTED', 'DECLINED')),
                                       CONSTRAINT fk_collaboration_request_sender
                                           FOREIGN KEY (sender_id)
                                               REFERENCES users (user_id)
                                               ON DELETE CASCADE,
                                       CONSTRAINT fk_collaboration_request_receiver
                                           FOREIGN KEY (receiver_id)
                                               REFERENCES users (user_id)
                                               ON DELETE CASCADE,
                                       CONSTRAINT fk_collaboration_request_plan
                                           FOREIGN KEY (plan_id)
                                               REFERENCES plan (plan_id)
                                               ON DELETE CASCADE
);

CREATE UNIQUE INDEX uk_collaboration_request_pending
    ON collaboration_request (sender_id, receiver_id, plan_id, collaboration_request_type)
    WHERE collaboration_request_status = 'PENDING';

CREATE TABLE beta_feedback (
                               feedback_id BIGINT GENERATED ALWAYS AS IDENTITY,
                               content TEXT NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT pk_beta_feedback
                                   PRIMARY KEY (feedback_id)
);

CREATE TABLE place_search_condition (
                                        condition_id BIGINT GENERATED ALWAYS AS IDENTITY,

                                        travel_id INT NOT NULL,
                                        place_category_id INT NOT NULL,
                                        preferred_theme_id INT,

                                        cache_key VARCHAR(128) NOT NULL,

                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        expired_at TIMESTAMP NOT NULL,

                                        CONSTRAINT pk_place_search_condition
                                            PRIMARY KEY (condition_id)
);

CREATE UNIQUE INDEX uk_place_search_condition_cache_key
    ON place_search_condition (cache_key);

CREATE INDEX idx_place_search_condition_expired_at
    ON place_search_condition (expired_at);

CREATE TABLE place_search_result (
                                     result_id BIGINT GENERATED ALWAYS AS IDENTITY,
                                     condition_id BIGINT NOT NULL,

                                     place_id VARCHAR(100) NOT NULL,
                                     place_name VARCHAR(255) NOT NULL,
                                     place_address TEXT,
                                     place_rating NUMERIC(2,1),

                                     photo_url TEXT,
                                     icon_url TEXT,
                                     place_link TEXT,

                                     x_location DOUBLE PRECISION,
                                     y_location DOUBLE PRECISION,

                                     sort_order INT,

                                     CONSTRAINT pk_place_search_result
                                         PRIMARY KEY (result_id),
                                     CONSTRAINT fk_place_search_result_condition
                                         FOREIGN KEY (condition_id)
                                             REFERENCES place_search_condition (condition_id)
                                             ON DELETE CASCADE
);

CREATE INDEX idx_place_search_result_condition_id
    ON place_search_result (condition_id);


