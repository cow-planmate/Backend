CREATE TABLE place_search_result (
                                     result_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                     condition_id BIGINT NOT NULL,

    -- 장소 스냅샷 정보
                                     place_id VARCHAR(100) NOT NULL,      -- google place_id
                                     place_name VARCHAR(255) NOT NULL,
                                     place_address TEXT,
                                     place_rating NUMERIC(2,1),

                                     photo_url TEXT,                      -- 대표 사진
                                     icon_url TEXT,                       -- 아이콘
                                     place_link TEXT,

                                     x_location DOUBLE PRECISION,
                                     y_location DOUBLE PRECISION,

                                     sort_order INT,

                                     CONSTRAINT fk_place_search_result_condition
                                         FOREIGN KEY (condition_id)
                                             REFERENCES place_search_condition(condition_id)
                                             ON DELETE CASCADE
);

CREATE INDEX idx_place_search_result_condition_id
    ON place_search_result (condition_id);
