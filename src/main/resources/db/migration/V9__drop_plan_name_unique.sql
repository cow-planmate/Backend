DO $$
DECLARE
r RECORD;
BEGIN
FOR r IN
SELECT
    n.nspname AS schema_name,
    t.relname AS table_name,
    c.conname AS constraint_name
FROM pg_constraint c
         JOIN pg_class t ON c.conrelid = t.oid
         JOIN pg_namespace n ON n.oid = t.relnamespace
WHERE t.relname = 'plan'
  AND c.contype = 'u'
  AND (
          SELECT array_agg(a.attname::text ORDER BY a.attname::text)
          FROM unnest(c.conkey) AS colnum
                   JOIN pg_attribute a
                        ON a.attrelid = t.oid
                            AND a.attnum = colnum
      ) = ARRAY['plan_name', 'user_id']
    LOOP
    EXECUTE format(
    'ALTER TABLE %I.%I DROP CONSTRAINT %I',
    r.schema_name,
    r.table_name,
    r.constraint_name
    );
END LOOP;
END $$;
