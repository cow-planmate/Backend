--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5
-- Dumped by pg_dump version 17.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: place_category; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.place_category (
                                       place_category_id integer NOT NULL,
                                       place_category_name character varying(255) NOT NULL
);


ALTER TABLE public.place_category OWNER TO postgres;

--
-- Name: plan; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.plan (
                             plan_id integer NOT NULL,
                             plan_name character varying(255) NOT NULL,
                             departure character varying(255) NOT NULL,
                             adult_count integer NOT NULL,
                             child_count integer NOT NULL,
                             user_id integer NOT NULL,
                             transportation_category_id integer NOT NULL,
                             travel_id integer NOT NULL,
                             CONSTRAINT plan_adult_count_check CHECK ((adult_count >= 0)),
                             CONSTRAINT plan_child_count_check CHECK ((child_count >= 0))
);


ALTER TABLE public.plan OWNER TO postgres;

--
-- Name: plan_plan_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.plan ALTER COLUMN plan_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.plan_plan_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: preferred_theme; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.preferred_theme (
                                        preferred_theme_id integer NOT NULL,
                                        preferred_theme_name character varying(255) NOT NULL,
                                        preferred_theme_category_id integer NOT NULL
);


ALTER TABLE public.preferred_theme OWNER TO postgres;

--
-- Name: preferred_theme_category; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.preferred_theme_category (
                                                 preferred_theme_category_id integer NOT NULL,
                                                 preferred_theme_category_name character varying(255) NOT NULL
);


ALTER TABLE public.preferred_theme_category OWNER TO postgres;

--
-- Name: preferred_theme_preferred_theme_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.preferred_theme ALTER COLUMN preferred_theme_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.preferred_theme_preferred_theme_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: time_table; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.time_table (
                                   time_table_id integer NOT NULL,
                                   date date NOT NULL,
                                   time_table_start_time time without time zone NOT NULL,
                                   time_table_end_time time without time zone NOT NULL,
                                   plan_id integer NOT NULL
);


ALTER TABLE public.time_table OWNER TO postgres;

--
-- Name: time_table_place_block; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.time_table_place_block (
                                               block_id integer NOT NULL,
                                               place_name character varying(255) NOT NULL,
                                               place_theme character varying(255) NOT NULL,
                                               place_rating real NOT NULL,
                                               place_address character varying(255) NOT NULL,
                                               place_link character varying(255) NOT NULL,
                                               block_start_time time without time zone NOT NULL,
                                               block_end_time time without time zone NOT NULL,
                                               x_location double precision NOT NULL,
                                               y_location double precision NOT NULL,
                                               place_category_id integer NOT NULL,
                                               time_table_id integer NOT NULL,
                                               CONSTRAINT time_table_place_block_place_rating_check CHECK (((place_rating >= ((0)::numeric)::double precision) AND (place_rating <= ((5)::numeric)::double precision)))
);


ALTER TABLE public.time_table_place_block OWNER TO postgres;

--
-- Name: time_table_place_block_block_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.time_table_place_block ALTER COLUMN block_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.time_table_place_block_block_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: time_table_time_table_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.time_table ALTER COLUMN time_table_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.time_table_time_table_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: transportation_category; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.transportation_category (
                                                transportation_category_id integer NOT NULL,
                                                transportation_category_name character varying(255) NOT NULL
);


ALTER TABLE public.transportation_category OWNER TO postgres;

--
-- Name: travel; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.travel (
                               travel_id integer NOT NULL,
                               travel_name character varying(255) NOT NULL,
                               travel_category_id integer NOT NULL
);


ALTER TABLE public.travel OWNER TO postgres;

--
-- Name: travel_category; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.travel_category (
                                        travel_category_id integer NOT NULL,
                                        travel_category_name character varying(255) NOT NULL
);


ALTER TABLE public.travel_category OWNER TO postgres;

--
-- Name: travel_category_travel_category_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.travel_category ALTER COLUMN travel_category_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.travel_category_travel_category_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: travel_travel_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.travel ALTER COLUMN travel_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.travel_travel_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Name: user_preferred_theme; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.user_preferred_theme (
                                             user_id integer NOT NULL,
                                             preferred_theme_id integer NOT NULL
);


ALTER TABLE public.user_preferred_theme OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
                              user_id integer NOT NULL,
                              email character varying(255) NOT NULL,
                              password character varying(255) NOT NULL,
                              nickname character varying(255) NOT NULL,
                              age integer NOT NULL,
                              gender integer NOT NULL,
                              CONSTRAINT users_age_check CHECK ((age >= 0)),
                              CONSTRAINT users_gender_check CHECK ((gender = ANY (ARRAY[0, 1])))
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

ALTER TABLE public.users ALTER COLUMN user_id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.users_user_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1
);


--
-- Data for Name: place_category; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.place_category (place_category_id, place_category_name) FROM stdin;
0	愿愿묒?
1	?숈냼
2	?앸떦
\.


--
-- Data for Name: plan; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.plan (plan_id, plan_name, departure, adult_count, child_count, user_id, transportation_category_id, travel_id) FROM stdin;
3	醫낅줈援?2	?띿궛??2	1	1	0	1
4	醫낅줈援?3	?띿궛??2	1	1	0	1
7	??1	紐낆???숆탳 ?몃Ц罹좏띁??3	1	4	0	233
2	new plan	?띿궛??2	1	1	0	1
\.


--
-- Data for Name: preferred_theme; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.preferred_theme (preferred_theme_id, preferred_theme_name, preferred_theme_category_id) FROM stdin;
1	??궗 ?좎쟻吏	0
2	?먯뿰 寃쎄?	0
3	?뚮쭏?뚰겕	0
4	?곗콉濡??섎젅湲?0
5	?몄닔/媛뺣? 紐낆냼	0
6	?대?/諛붾떎 ?ы뻾吏	0
7	?꾨쭩?/?쇨꼍 紐낆냼	0
8	臾명솕 ?좎궛 ?먮갑	0
9	誘몄닠愿/諛뺣Ъ愿	0
10	?꾪넻 留덉쓣 ?먮갑	0
11	?쒖떇 留쏆쭛	1
12	移댄럹 ?ъ뼱	1
13	?꾪넻 ?쒖옣 ?뚯떇	1
14	?붿????꾨Ц??1
15	怨좉린吏?1
16	?댁궛臾??붾━	1
17	?⑥쟾 ?붾━	1
18	梨꾩떇/鍮꾧굔 ?앸떦	1
19	遺꾩떇 留쏆쭛	1
20	吏???뱀궛臾??뚯떇??1
\.


--
-- Data for Name: preferred_theme_category; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.preferred_theme_category (preferred_theme_category_id, preferred_theme_category_name) FROM stdin;
0	愿愿묒?
1	?앸떦
\.


--
-- Data for Name: time_table; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.time_table (time_table_id, date, time_table_start_time, time_table_end_time, plan_id) FROM stdin;
2	2025-08-01	09:00:00	20:00:00	4
3	2025-08-02	09:00:00	20:00:00	4
4	2025-08-03	09:00:00	20:00:00	4
71	2025-08-22	09:00:00	20:00:00	2
72	2025-08-23	09:00:00	20:00:00	2
73	2025-08-24	09:00:00	20:00:00	2
94	2025-07-26	09:00:00	20:00:00	7
95	2025-07-27	09:00:00	20:00:00	7
96	2025-07-28	09:00:00	20:00:00	7
97	2025-07-29	09:00:00	20:00:00	7
98	2025-07-30	09:00:00	20:00:00	7
\.


--
-- Data for Name: time_table_place_block; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.time_table_place_block (block_id, place_name, place_theme, place_rating, place_address, place_link, block_start_time, block_end_time, x_location, y_location, place_category_id, time_table_id) FROM stdin;
25	寃쎈났沅???궗	4.7	?쒖슱 醫낅줈援??ъ쭅濡?161	https://map.example.com/place1	09:00:00	10:30:00	126.9769	37.5796	1	71
26	愿묓솕臾멸킅???쒕?	4.2	?쒖슱 醫낅줈援??몄쥌?濡?https://map.example.com/place2	10:45:00	11:30:00	126.977	37.5714	1	71
27	?몄궗??嫄곕━	?쇳븨	4.3	?쒖슱 醫낅줈援??몄궗??https://map.example.com/place3	13:00:00	14:30:00	126.9863	37.574	1	72
52	?쒕┝怨듭썝	??궗	4.4	??쒕?援??쒖＜?밸퀎?먯튂???쒖＜???쒕┝???쒕┝濡?300	https://www.google.com/maps/place/?q=place_id:ChIJ83lq-MBgDDURc64bn4fHZe8	09:15:00	10:15:00	126.2392884	33.3895279	0	94
53	?곌돔遺由???궗	4.4	??쒕?援??쒖＜?밸퀎?먯튂???쒖＜??議곗쿇??鍮꾩옄由쇰줈 768	https://www.google.com/maps/place/?q=place_id:ChIJ8c4mMmcDDTURdSvFEkuLvpg	10:45:00	11:45:00	126.690658	33.43303	0	94
54	?꾨え?됱뒪由ъ“????궗	3.8	??쒕?援??쒖＜?밸퀎?먯튂???쒖＜???쒗빐?덈줈 216	https://www.google.com/maps/place/?q=place_id:ChIJw2GVjuf6DDUR6pYou7PLQqE	11:30:00	12:45:00	126.4709592	33.5073844	1	95
55	怨좎쭛?뚯슦???⑤뜒????궗	4.6	??쒕?援??쒖＜?밸퀎?먯튂???쒖＜???밸퀎?먯튂?? 議곗쿇???좊턿濡?491-9 2 痢?https://www.google.com/maps/place/?q=place_id:ChIJ00PR6XYfDTURlkix8j-OnkQ	10:00:00	11:00:00	126.6630159	33.5438111	2	95
\.


--
-- Data for Name: transportation_category; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.transportation_category (transportation_category_id, transportation_category_name) FROM stdin;
0	?以묎탳??
1	?먭???
\.


--
-- Data for Name: travel; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.travel (travel_id, travel_name, travel_category_id) FROM stdin;
1	醫낅줈援?1
2	以묎뎄	1
3	?⑹궛援?1
4	?깅룞援?1
5	愿묒쭊援?1
6	?숇?臾멸뎄	1
7	以묐옉援?1
8	?깅턿援?1
9	媛뺣턿援?1
10	?꾨큺援?1
11	?몄썝援?1
12	??됯뎄	1
13	?쒕?臾멸뎄	1
14	留덊룷援?1
15	?묒쿇援?1
16	媛뺤꽌援?1
17	援щ줈援?1
18	湲덉쿇援?1
19	?곷벑?ш뎄	1
20	?숈옉援?1
21	愿?낃뎄	1
22	?쒖큹援?1
23	媛뺣궓援?1
24	?≫뙆援?1
25	媛뺣룞援?1
26	以묎뎄	2
27	?쒓뎄	2
28	?숆뎄	2
29	?곷룄援?2
30	遺?곗쭊援?2
31	?숇옒援?2
32	?④뎄	2
33	遺곴뎄	2
34	?댁슫?援?2
35	?ы븯援?2
36	湲덉젙援?2
37	媛뺤꽌援?2
38	?곗젣援?2
39	?섏쁺援?2
40	?ъ긽援?2
41	湲곗옣援?2
42	以묎뎄	3
43	?숆뎄	3
44	?쒓뎄	3
45	?④뎄	3
46	遺곴뎄	3
47	?섏꽦援?3
48	?ъ꽌援?3
49	?ъ꽦援?3
50	以묎뎄	4
51	?숆뎄	4
52	誘몄텛?援?4
53	?곗닔援?4
54	?⑤룞援?4
55	遺?됯뎄	4
56	怨꾩뼇援?4
57	?쒓뎄	4
58	媛뺥솕援?4
59	?뱀쭊援?4
60	?숆뎄	5
61	?쒓뎄	5
62	?④뎄	5
63	遺곴뎄	5
64	愿묒궛援?5
65	?숆뎄	6
66	以묎뎄	6
67	?쒓뎄	6
68	?좎꽦援?6
69	??뺢뎄	6
70	以묎뎄	7
71	?④뎄	7
72	?숆뎄	7
73	遺곴뎄	7
74	?몄＜援?7
75	議곗튂?먯쓭	8
76	?쒖넄??8
77	?꾨떞??8
78	?꾨쫫??8
79	蹂대엺??8
80	?κ뎔硫?8
81	?섏썝??9
82	?깅궓??9
83	?섏젙遺??9
84	?덉뼇??9
85	遺泥쒖떆	9
86	愿묐챸??9
87	?됲깮??9
88	?숇몢泥쒖떆	9
89	?덉궛??9
90	怨좎뼇??9
91	怨쇱쿇??9
92	援щ━??9
93	?⑥뼇二쇱떆	9
94	?ㅼ궛??9
95	?쒗씎??9
96	援고룷??9
97	?섏솗??9
98	?섎궓??9
99	?⑹씤??9
100	?뚯＜??9
101	?댁쿇??9
102	?덉꽦??9
103	源?ъ떆	9
104	?붿꽦??9
105	愿묒＜??9
106	?묒＜??9
107	?ъ쿇??9
108	?ъ＜??9
109	?곗쿇援?9
110	媛?됯뎔	9
111	?묓룊援?9
112	異섏쿇??10
113	?먯＜??10
114	媛뺣쫱??10
115	?숉빐??10
116	?쒕갚??10
117	?띿큹??10
118	?쇱쿃??10
119	?띿쿇援?10
120	?≪꽦援?10
121	?곸썡援?10
122	?됱갹援?10
123	?뺤꽑援?10
124	泥좎썝援?10
125	?붿쿇援?10
126	?묎뎄援?10
127	?몄젣援?10
128	怨좎꽦援?10
129	?묒뼇援?10
130	泥?＜??11
131	異⑹＜??11
132	?쒖쿇??11
133	蹂댁?援?11
134	?μ쿇援?11
135	?곷룞援?11
136	吏꾩쿇援?11
137	愿댁궛援?11
138	?뚯꽦援?11
139	?⑥뼇援?11
140	利앺룊援?11
141	泥쒖븞??12
142	怨듭＜??12
143	蹂대졊??12
144	?꾩궛??12
145	?쒖궛??12
146	?쇱궛??12
147	怨꾨！??12
148	?뱀쭊??12
149	湲덉궛援?12
150	遺?ш뎔	12
151	?쒖쿇援?12
152	泥?뼇援?12
153	?띿꽦援?12
154	?덉궛援?12
155	?쒖븞援?12
156	?꾩＜??13
157	援곗궛??13
158	?듭궛??13
159	?뺤쓭??13
160	?⑥썝??13
161	源?쒖떆	13
162	?꾩＜援?13
163	吏꾩븞援?13
164	臾댁＜援?13
165	?μ닔援?13
166	?꾩떎援?13
167	?쒖갹援?13
168	怨좎갹援?13
169	遺?덇뎔	13
170	紐⑺룷??14
171	?ъ닔??14
172	?쒖쿇??14
173	?섏＜??14
174	愿묒뼇??14
175	?댁뼇援?14
176	怨≪꽦援?14
177	援щ?援?14
178	怨좏씎援?14
179	蹂댁꽦援?14
180	?붿닚援?14
181	?ν씎援?14
182	媛뺤쭊援?14
183	?대궓援?14
184	?곸븫援?14
185	臾댁븞援?14
186	?⑦룊援?14
187	?곴킅援?14
188	?μ꽦援?14
189	?꾨룄援?14
190	吏꾨룄援?14
191	?좎븞援?14
192	?ы빆??15
193	寃쎌＜??15
194	源泥쒖떆	15
195	?덈룞??15
196	援щ???15
197	?곸＜??15
198	?곸쿇??15
199	?곸＜??15
200	臾멸꼍??15
201	寃쎌궛??15
202	援곗쐞援?15
203	?섏꽦援?15
204	泥?넚援?15
205	?곸뼇援?15
206	?곷뜒援?15
207	泥?룄援?15
208	怨좊졊援?15
209	?깆＜援?15
210	移좉끝援?15
211	?덉쿇援?15
212	遊됲솕援?15
213	?몄쭊援?15
214	?몃쫱援?15
215	李쎌썝??16
216	吏꾩＜??16
217	?듭쁺??16
218	?ъ쿇??16
219	源?댁떆	16
220	諛?묒떆	16
221	嫄곗젣??16
222	?묒궛??16
223	?섎졊援?16
224	?⑥븞援?16
225	李쎈뀞援?16
226	怨좎꽦援?16
227	?⑦빐援?16
228	?섎룞援?16
229	?곗껌援?16
230	?⑥뼇援?16
231	嫄곗갹援?16
232	?⑹쿇援?16
233	?쒖＜??17
234	?쒓??ъ떆	17
\.


--
-- Data for Name: travel_category; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.travel_category (travel_category_id, travel_category_name) FROM stdin;
1	?쒖슱?밸퀎??
2	遺?곌킅??떆
3	?援ш킅??떆
4	?몄쿇愿묒뿭??
5	愿묒＜愿묒뿭??
6	??꾧킅??떆
7	?몄궛愿묒뿭??
8	?몄쥌?밸퀎?먯튂??
9	寃쎄린??
10	媛뺤썝?밸퀎?먯튂??
11	異⑹껌遺곷룄
12	異⑹껌?⑤룄
13	?꾨씪遺곷룄
14	?꾨씪?⑤룄
15	寃쎌긽遺곷룄
16	寃쎌긽?⑤룄
17	?쒖＜?밸퀎?먯튂??
\.


--
-- Data for Name: user_preferred_theme; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.user_preferred_theme (user_id, preferred_theme_id) FROM stdin;
1	1
1	3
1	2
4	1
4	2
4	5
4	15
4	18
4	19
4	16
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (user_id, email, password, nickname, age, gender) FROM stdin;
1	test@naver.com	$2a$10$OCXxmVZHn8XP2TuntcDEzuzsTy54H2dEeEZfDvJKVVqLvv2R4xzTG	test	10	1
4	arlawjdqls012@naver.com	$2a$10$KULiVFBUfLlckmSM7XdoCODtYrANgLl2krYLwJteaYaASrHWu1G32	dddd	23	0
\.


--
-- Name: plan_plan_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.plan_plan_id_seq', 7, true);


--
-- Name: preferred_theme_preferred_theme_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.preferred_theme_preferred_theme_id_seq', 20, true);


--
-- Name: time_table_place_block_block_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.time_table_place_block_block_id_seq', 55, true);


--
-- Name: time_table_time_table_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.time_table_time_table_id_seq', 98, true);


--
-- Name: travel_category_travel_category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.travel_category_travel_category_id_seq', 17, true);


--
-- Name: travel_travel_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.travel_travel_id_seq', 234, true);


--
-- Name: users_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.users_user_id_seq', 4, true);


--
-- Name: place_category place_category_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.place_category
    ADD CONSTRAINT place_category_pkey PRIMARY KEY (place_category_id);


--
-- Name: plan plan_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.plan
    ADD CONSTRAINT plan_pkey PRIMARY KEY (plan_id);


--
-- Name: plan plan_user_id_plan_name_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.plan
    ADD CONSTRAINT plan_user_id_plan_name_key UNIQUE (user_id, plan_name);


--
-- Name: preferred_theme_category preferred_theme_category_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.preferred_theme_category
    ADD CONSTRAINT preferred_theme_category_pkey PRIMARY KEY (preferred_theme_category_id);


--
-- Name: preferred_theme preferred_theme_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.preferred_theme
    ADD CONSTRAINT preferred_theme_pkey PRIMARY KEY (preferred_theme_id);


--
-- Name: time_table time_table_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.time_table
    ADD CONSTRAINT time_table_pkey PRIMARY KEY (time_table_id);


--
-- Name: time_table_place_block time_table_place_block_block_start_time_time_table_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.time_table_place_block
    ADD CONSTRAINT time_table_place_block_block_start_time_time_table_id_key UNIQUE (block_start_time, time_table_id);


--
-- Name: time_table_place_block time_table_place_block_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.time_table_place_block
    ADD CONSTRAINT time_table_place_block_pkey PRIMARY KEY (block_id);


--
-- Name: transportation_category transportation_category_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transportation_category
    ADD CONSTRAINT transportation_category_pkey PRIMARY KEY (transportation_category_id);


--
-- Name: travel_category travel_category_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.travel_category
    ADD CONSTRAINT travel_category_pkey PRIMARY KEY (travel_category_id);


--
-- Name: travel travel_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.travel
    ADD CONSTRAINT travel_pkey PRIMARY KEY (travel_id);


--
-- Name: time_table_place_block ukb8faxvhjxrgtprh9lnyo6oi45; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.time_table_place_block
    ADD CONSTRAINT ukb8faxvhjxrgtprh9lnyo6oi45 UNIQUE (block_start_time, time_table_id);


--
-- Name: plan ukfcle0fge0ra0gl52ibhug5pdo; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.plan
    ADD CONSTRAINT ukfcle0fge0ra0gl52ibhug5pdo UNIQUE (user_id, plan_name);


--
-- Name: user_preferred_theme user_preferred_theme_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_preferred_theme
    ADD CONSTRAINT user_preferred_theme_pkey PRIMARY KEY (user_id, preferred_theme_id);


--
-- Name: users users_nickname_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_nickname_key UNIQUE (nickname);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (user_id);


--
-- Name: unique_lower_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX unique_lower_email ON public.users USING btree (lower((email)::text));


--
-- Name: plan plan_transportation_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.plan
    ADD CONSTRAINT plan_transportation_category_id_fkey FOREIGN KEY (transportation_category_id) REFERENCES public.transportation_category(transportation_category_id);


--
-- Name: plan plan_travel_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.plan
    ADD CONSTRAINT plan_travel_id_fkey FOREIGN KEY (travel_id) REFERENCES public.travel(travel_id);


--
-- Name: plan plan_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.plan
    ADD CONSTRAINT plan_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- Name: preferred_theme preferred_theme_preferred_theme_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.preferred_theme
    ADD CONSTRAINT preferred_theme_preferred_theme_category_id_fkey FOREIGN KEY (preferred_theme_category_id) REFERENCES public.preferred_theme_category(preferred_theme_category_id);


--
-- Name: time_table_place_block time_table_place_block_place_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.time_table_place_block
    ADD CONSTRAINT time_table_place_block_place_category_id_fkey FOREIGN KEY (place_category_id) REFERENCES public.place_category(place_category_id);


--
-- Name: time_table_place_block time_table_place_block_time_table_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.time_table_place_block
    ADD CONSTRAINT time_table_place_block_time_table_id_fkey FOREIGN KEY (time_table_id) REFERENCES public.time_table(time_table_id) ON DELETE CASCADE;


--
-- Name: time_table time_table_plan_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.time_table
    ADD CONSTRAINT time_table_plan_id_fkey FOREIGN KEY (plan_id) REFERENCES public.plan(plan_id) ON DELETE CASCADE;


--
-- Name: travel travel_travel_category_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.travel
    ADD CONSTRAINT travel_travel_category_id_fkey FOREIGN KEY (travel_category_id) REFERENCES public.travel_category(travel_category_id);


--
-- Name: user_preferred_theme user_preferred_theme_preferred_theme_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_preferred_theme
    ADD CONSTRAINT user_preferred_theme_preferred_theme_id_fkey FOREIGN KEY (preferred_theme_id) REFERENCES public.preferred_theme(preferred_theme_id);


--
-- Name: user_preferred_theme user_preferred_theme_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.user_preferred_theme
    ADD CONSTRAINT user_preferred_theme_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(user_id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

