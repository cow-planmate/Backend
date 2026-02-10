INSERT INTO preferred_theme_category (preferred_theme_category_id, preferred_theme_category_name)
VALUES
    (0, '관광지'),
    (1, '숙소'),
    (2, '식당')
    ON CONFLICT (preferred_theme_category_id) DO NOTHING;

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

INSERT INTO transportation_category (transportation_category_id, transportation_category_name)
VALUES
    (0, '대중교통'),
    (1, '자가용')
    ON CONFLICT (transportation_category_id) DO NOTHING;

INSERT INTO travel_category (travel_category_name)
VALUES
    ('서울특별시'),
    ('부산광역시'),
    ('대구광역시'),
    ('인천광역시'),
    ('광주광역시'),
    ('대전광역시'),
    ('울산광역시'),
    ('세종특별자치시'),
    ('경기도'),
    ('강원특별자치도'),
    ('충청북도'),
    ('충청남도'),
    ('전라북도'),
    ('전라남도'),
    ('경상북도'),
    ('경상남도'),
    ('제주특별자치도');

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('종로구', 1),
    ('중구', 1),
    ('용산구', 1),
    ('성동구', 1),
    ('광진구', 1),
    ('동대문구', 1),
    ('중랑구', 1),
    ('성북구', 1),
    ('강북구', 1),
    ('도봉구', 1),
    ('노원구', 1),
    ('은평구', 1),
    ('서대문구', 1),
    ('마포구', 1),
    ('양천구', 1),
    ('강서구', 1),
    ('구로구', 1),
    ('금천구', 1),
    ('영등포구', 1),
    ('동작구', 1),
    ('관악구', 1),
    ('서초구', 1),
    ('강남구', 1),
    ('송파구', 1),
    ('강동구', 1);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('중구', 2),
    ('서구', 2),
    ('동구', 2),
    ('영도구', 2),
    ('부산진구', 2),
    ('동래구', 2),
    ('남구', 2),
    ('북구', 2),
    ('해운대구', 2),
    ('사하구', 2),
    ('금정구', 2),
    ('강서구', 2),
    ('연제구', 2),
    ('수영구', 2),
    ('사상구', 2),
    ('기장군', 2);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('중구', 3),
    ('동구', 3),
    ('서구', 3),
    ('남구', 3),
    ('북구', 3),
    ('수성구', 3),
    ('달서구', 3),
    ('달성군', 3);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('중구', 4),
    ('동구', 4),
    ('미추홀구', 4),
    ('연수구', 4),
    ('남동구', 4),
    ('부평구', 4),
    ('계양구', 4),
    ('서구', 4),
    ('강화군', 4),
    ('옹진군', 4);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('동구', 5),
    ('서구', 5),
    ('남구', 5),
    ('북구', 5),
    ('광산구', 5);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('동구', 6),
    ('중구', 6),
    ('서구', 6),
    ('유성구', 6),
    ('대덕구', 6);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('중구', 7),
    ('남구', 7),
    ('동구', 7),
    ('북구', 7),
    ('울주군', 7);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('조치원읍', 8),
    ('한솔동', 8),
    ('도담동', 8),
    ('아름동', 8),
    ('보람동', 8),
    ('장군면', 8);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('수원시', 9),
    ('성남시', 9),
    ('의정부시', 9),
    ('안양시', 9),
    ('부천시', 9),
    ('광명시', 9),
    ('평택시', 9),
    ('동두천시', 9),
    ('안산시', 9),
    ('고양시', 9),
    ('과천시', 9),
    ('구리시', 9),
    ('남양주시', 9),
    ('오산시', 9),
    ('시흥시', 9),
    ('군포시', 9),
    ('의왕시', 9),
    ('하남시', 9),
    ('용인시', 9),
    ('파주시', 9),
    ('이천시', 9),
    ('안성시', 9),
    ('김포시', 9),
    ('화성시', 9),
    ('광주시', 9),
    ('양주시', 9),
    ('포천시', 9),
    ('여주시', 9),
    ('연천군', 9),
    ('가평군', 9),
    ('양평군', 9);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('춘천시', 10),
    ('원주시', 10),
    ('강릉시', 10),
    ('동해시', 10),
    ('태백시', 10),
    ('속초시', 10),
    ('삼척시', 10),
    ('홍천군', 10),
    ('횡성군', 10),
    ('영월군', 10),
    ('평창군', 10),
    ('정선군', 10),
    ('철원군', 10),
    ('화천군', 10),
    ('양구군', 10),
    ('인제군', 10),
    ('고성군', 10),
    ('양양군', 10);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('청주시', 11),
    ('충주시', 11),
    ('제천시', 11),
    ('보은군', 11),
    ('옥천군', 11),
    ('영동군', 11),
    ('진천군', 11),
    ('괴산군', 11),
    ('음성군', 11),
    ('단양군', 11),
    ('증평군', 11);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('천안시', 12),
    ('공주시', 12),
    ('보령시', 12),
    ('아산시', 12),
    ('서산시', 12),
    ('논산시', 12),
    ('계룡시', 12),
    ('당진시', 12),
    ('금산군', 12),
    ('부여군', 12),
    ('서천군', 12),
    ('청양군', 12),
    ('홍성군', 12),
    ('예산군', 12),
    ('태안군', 12);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('전주시', 13),
    ('군산시', 13),
    ('익산시', 13),
    ('정읍시', 13),
    ('남원시', 13),
    ('김제시', 13),
    ('완주군', 13),
    ('진안군', 13),
    ('무주군', 13),
    ('장수군', 13),
    ('임실군', 13),
    ('순창군', 13),
    ('고창군', 13),
    ('부안군', 13);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('목포시', 14),
    ('여수시', 14),
    ('순천시', 14),
    ('나주시', 14),
    ('광양시', 14),
    ('담양군', 14),
    ('곡성군', 14),
    ('구례군', 14),
    ('고흥군', 14),
    ('보성군', 14),
    ('화순군', 14),
    ('장흥군', 14),
    ('강진군', 14),
    ('해남군', 14),
    ('영암군', 14),
    ('무안군', 14),
    ('함평군', 14),
    ('영광군', 14),
    ('장성군', 14),
    ('완도군', 14),
    ('진도군', 14),
    ('신안군', 14);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('포항시', 15),
    ('경주시', 15),
    ('김천시', 15),
    ('안동시', 15),
    ('구미시', 15),
    ('영주시', 15),
    ('영천시', 15),
    ('상주시', 15),
    ('문경시', 15),
    ('경산시', 15),
    ('군위군', 15),
    ('의성군', 15),
    ('청송군', 15),
    ('영양군', 15),
    ('영덕군', 15),
    ('청도군', 15),
    ('고령군', 15),
    ('성주군', 15),
    ('칠곡군', 15),
    ('예천군', 15),
    ('봉화군', 15),
    ('울진군', 15),
    ('울릉군', 15);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('창원시', 16),
    ('진주시', 16),
    ('통영시', 16),
    ('사천시', 16),
    ('김해시', 16),
    ('밀양시', 16),
    ('거제시', 16),
    ('양산시', 16),
    ('의령군', 16),
    ('함안군', 16),
    ('창녕군', 16),
    ('고성군', 16),
    ('남해군', 16),
    ('하동군', 16),
    ('산청군', 16),
    ('함양군', 16),
    ('거창군', 16),
    ('합천군', 16);

INSERT INTO travel (travel_name, travel_category_id)
VALUES
    ('제주시', 17),
    ('서귀포시', 17);

INSERT INTO place_category (place_category_id, place_category_name)
VALUES
    (0, '관광지'),
    (1, '숙소'),
    (2, '식당'),
    (3, '자유'),
    (4, '검색')
    ON CONFLICT (place_category_id) DO NOTHING;
