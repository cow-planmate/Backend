-- V7: Add memo column and allow CUSTOM blocks (environment-independent)

-- ------------------------------------------------------------
-- 1. memo 컬럼 추가 (기존 데이터는 NULL)
-- ------------------------------------------------------------
ALTER TABLE time_table_place_block
    ADD COLUMN IF NOT EXISTS memo TEXT;


-- ------------------------------------------------------------
-- 2. CUSTOM 블럭을 위해 장소 관련 컬럼 NOT NULL 해제
-- ------------------------------------------------------------
ALTER TABLE time_table_place_block
    ALTER COLUMN place_theme DROP NOT NULL,
ALTER COLUMN place_rating DROP NOT NULL,
    ALTER COLUMN place_address DROP NOT NULL,
    ALTER COLUMN place_link DROP NOT NULL,
    ALTER COLUMN x_location DROP NOT NULL,
    ALTER COLUMN y_location DROP NOT NULL,
    ALTER COLUMN place_id DROP NOT NULL;


-- ------------------------------------------------------------
-- 3. place_rating 기존 CHECK 제약 제거 (환경 독립)
--    pg_constraint + pg_attribute 기반 (컬럼 정확 매칭)
-- ------------------------------------------------------------
DO $$
DECLARE
constraint_name text;
BEGIN
SELECT c.conname
INTO constraint_name
FROM pg_constraint c
         JOIN pg_class t ON c.conrelid = t.oid
         JOIN pg_attribute a ON a.attrelid = t.oid
WHERE t.relname = 'time_table_place_block'
  AND c.contype = 'c'              -- CHECK constraint
  AND a.attname = 'place_rating'
  AND a.attnum = ANY (c.conkey);

IF constraint_name IS NOT NULL THEN
        EXECUTE format(
            'ALTER TABLE time_table_place_block DROP CONSTRAINT %I',
            constraint_name
        );
END IF;
END $$;


-- ------------------------------------------------------------
-- 4. place_rating NULL 허용 + 범위 CHECK 재정의
-- ------------------------------------------------------------
ALTER TABLE time_table_place_block
    ADD CONSTRAINT chk_time_table_place_block_place_rating
        CHECK (
            place_rating IS NULL
                OR place_rating BETWEEN 0 AND 5
            );
