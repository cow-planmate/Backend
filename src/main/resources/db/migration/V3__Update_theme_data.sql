DELETE FROM preferred_theme;

-- 1. 새로운 테마 데이터 삽입 (깔끔하게 새로 넣기)
-- 관광지
INSERT INTO preferred_theme (preferred_theme_name, preferred_theme_category_id) VALUES 
    ('유적지', 0),
    ('공원', 0),
    ('테마파크', 0),
    ('산책로', 0),
    ('호수', 0),
    ('해수욕장', 0),
    ('전망대', 0),
    ('박물관', 0),
    ('미술관', 0),
    ('한옥마을', 0);

-- 숙소
INSERT INTO preferred_theme (preferred_theme_name, preferred_theme_category_id) VALUES 
    ('호텔', 1),
    ('모텔', 1),
    ('게스트하우스', 1),
    ('펜션', 1),
    ('리조트', 1),
    ('캠핑장', 1),
    ('글램핑', 1),
    ('풀빌라', 1);

-- 맛집/카페
INSERT INTO preferred_theme (preferred_theme_name, preferred_theme_category_id) VALUES 
    ('한식', 2),
    ('카페', 2),
    ('전통시장', 2),
    ('베이커리', 2),
    ('고기집', 2),
    ('횟집', 2),
    ('퓨전음식', 2),
    ('브런치', 2),
    ('분식', 2),
    ('이자카야', 2);