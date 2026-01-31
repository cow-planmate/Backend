CREATE TABLE IF NOT EXISTS place_search_condition (
                                                      condition_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    -- 추천 조건 (논리적 참조)
                                                      travel_id INT NOT NULL,
                                                      place_category_id INT NOT NULL,
                                                      preferred_theme_id INT,

    -- 검색 조건 조합 키 (서버에서 생성)
                                                      cache_key VARCHAR(128) NOT NULL,

    -- 캐시 관리
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expired_at TIMESTAMP NOT NULL
    );

CREATE UNIQUE INDEX IF NOT EXISTS ux_place_search_condition_cache_key
    ON place_search_condition (cache_key);

CREATE INDEX IF NOT EXISTS idx_place_search_condition_expired
    ON place_search_condition (expired_at);
