-- 1. place_id → place_photo FK 제거 (환경 독립적)
DO $$
DECLARE
constraint_name text;
BEGIN
SELECT tc.constraint_name
INTO constraint_name
FROM information_schema.table_constraints tc
         JOIN information_schema.key_column_usage kcu
              ON tc.constraint_name = kcu.constraint_name
WHERE tc.table_name = 'time_table_place_block'
  AND tc.constraint_type = 'FOREIGN KEY'
  AND kcu.column_name = 'place_id';

IF constraint_name IS NOT NULL THEN
        EXECUTE format(
            'ALTER TABLE time_table_place_block DROP CONSTRAINT %I',
            constraint_name
        );
END IF;
END $$;

-- 2. 장소 블럭에 사진 / 설명 컬럼 추가
ALTER TABLE time_table_place_block
    ADD COLUMN IF NOT EXISTS photo_url TEXT,
    ADD COLUMN IF NOT EXISTS description TEXT;

-- 3. place_photo 테이블 삭제
DROP TABLE IF EXISTS place_photo;
