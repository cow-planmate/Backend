ALTER TABLE place_search_result
    ADD COLUMN IF NOT EXISTS user_ratings_total INT;

ALTER TABLE place_search_result
    ADD COLUMN IF NOT EXISTS place_types TEXT;

ALTER TABLE place_search_result
    ADD COLUMN IF NOT EXISTS price_level INT;
