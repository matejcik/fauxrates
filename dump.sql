--
-- PostgreSQL database dump
--

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: character_component; Type: TABLE; Schema: public; Owner: fauxrates; Tablespace: 
--

CREATE TABLE character_component (
    id bigint NOT NULL,
    player bigint NOT NULL,
    name character varying NOT NULL,
    online boolean
);


ALTER TABLE public.character_component OWNER TO fauxrates;

--
-- Name: entities_id_seq; Type: SEQUENCE; Schema: public; Owner: fauxrates
--

CREATE SEQUENCE entities_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.entities_id_seq OWNER TO fauxrates;

--
-- Name: entities_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fauxrates
--

SELECT pg_catalog.setval('entities_id_seq', 21, true);


--
-- Name: entities; Type: TABLE; Schema: public; Owner: fauxrates; Tablespace: 
--

CREATE TABLE entities (
    id bigint DEFAULT nextval('entities_id_seq'::regclass) NOT NULL,
    comment character varying(128) NOT NULL,
    name character varying(50)
);


ALTER TABLE public.entities OWNER TO fauxrates;

--
-- Name: outpost_component; Type: TABLE; Schema: public; Owner: fauxrates; Tablespace: 
--

CREATE TABLE outpost_component (
    id bigint NOT NULL,
    name character varying NOT NULL,
    x double precision NOT NULL,
    y double precision NOT NULL
);


ALTER TABLE public.outpost_component OWNER TO fauxrates;

--
-- Name: plane_component; Type: TABLE; Schema: public; Owner: fauxrates; Tablespace: 
--

CREATE TABLE plane_component (
    id bigint NOT NULL,
    x double precision NOT NULL,
    y double precision NOT NULL,
    location_id bigint,
    target_id bigint,
    target_x double precision,
    target_y double precision,
    time_to_land timestamp without time zone,
    fuel double precision NOT NULL
);


ALTER TABLE public.plane_component OWNER TO fauxrates;

--
-- Name: users; Type: TABLE; Schema: public; Owner: fauxrates; Tablespace: 
--

CREATE TABLE users (
    id integer NOT NULL,
    name character varying NOT NULL,
    admin boolean DEFAULT false NOT NULL
);


ALTER TABLE public.users OWNER TO fauxrates;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: fauxrates
--

CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER TABLE public.users_id_seq OWNER TO fauxrates;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: fauxrates
--

ALTER SEQUENCE users_id_seq OWNED BY users.id;


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: fauxrates
--

SELECT pg_catalog.setval('users_id_seq', 3, true);


--
-- Name: id; Type: DEFAULT; Schema: public; Owner: fauxrates
--

ALTER TABLE users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);


--
-- Data for Name: character_component; Type: TABLE DATA; Schema: public; Owner: fauxrates
--

COPY character_component (id, player, name, online) FROM stdin;
15	1	ferret Rincewind	f
16	2	dede Cloudkicker	f
\.


--
-- Data for Name: entities; Type: TABLE DATA; Schema: public; Owner: fauxrates
--

COPY entities (id, comment, name) FROM stdin;
15		\N
16		\N
17		OUTPOST_ZERO
18		\N
19		\N
20		\N
21		\N
\.


--
-- Data for Name: outpost_component; Type: TABLE DATA; Schema: public; Owner: fauxrates
--

COPY outpost_component (id, name, x, y) FROM stdin;
17	Zero One	0	0
18	Bregna	40	70
19	Cape Cod	10	0
20	Faraway Land	100	30
21	Nearaway Land	150	-50
\.


--
-- Data for Name: plane_component; Type: TABLE DATA; Schema: public; Owner: fauxrates
--

COPY plane_component (id, x, y, location_id, target_id, target_x, target_y, time_to_land, fuel) FROM stdin;
15	0	0	17	\N	\N	\N	\N	100
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: fauxrates
--

COPY users (id, name, admin) FROM stdin;
1	ferret	f
2	dede	f
3	admin	t
\.


--
-- Name: character_component_pkey; Type: CONSTRAINT; Schema: public; Owner: fauxrates; Tablespace: 
--

ALTER TABLE ONLY character_component
    ADD CONSTRAINT character_component_pkey PRIMARY KEY (id);


--
-- Name: entities_pkey; Type: CONSTRAINT; Schema: public; Owner: fauxrates; Tablespace: 
--

ALTER TABLE ONLY entities
    ADD CONSTRAINT entities_pkey PRIMARY KEY (id);


--
-- Name: name_key; Type: CONSTRAINT; Schema: public; Owner: fauxrates; Tablespace: 
--

ALTER TABLE ONLY entities
    ADD CONSTRAINT name_key UNIQUE (name);


--
-- Name: outpost_component_pkey; Type: CONSTRAINT; Schema: public; Owner: fauxrates; Tablespace: 
--

ALTER TABLE ONLY outpost_component
    ADD CONSTRAINT outpost_component_pkey PRIMARY KEY (id);


--
-- Name: plane_component_pkey; Type: CONSTRAINT; Schema: public; Owner: fauxrates; Tablespace: 
--

ALTER TABLE ONLY plane_component
    ADD CONSTRAINT plane_component_pkey PRIMARY KEY (id);


--
-- Name: users_pkey; Type: CONSTRAINT; Schema: public; Owner: fauxrates; Tablespace: 
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: character_component_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: fauxrates
--

ALTER TABLE ONLY character_component
    ADD CONSTRAINT character_component_id_fkey FOREIGN KEY (id) REFERENCES entities(id);


--
-- Name: character_component_player_fkey; Type: FK CONSTRAINT; Schema: public; Owner: fauxrates
--

ALTER TABLE ONLY character_component
    ADD CONSTRAINT character_component_player_fkey FOREIGN KEY (player) REFERENCES users(id);


--
-- Name: outpost_component_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: fauxrates
--

ALTER TABLE ONLY outpost_component
    ADD CONSTRAINT outpost_component_id_fkey FOREIGN KEY (id) REFERENCES entities(id);


--
-- Name: plane_component_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: fauxrates
--

ALTER TABLE ONLY plane_component
    ADD CONSTRAINT plane_component_id_fkey FOREIGN KEY (id) REFERENCES entities(id);


--
-- Name: plane_component_location_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: fauxrates
--

ALTER TABLE ONLY plane_component
    ADD CONSTRAINT plane_component_location_id_fkey FOREIGN KEY (location_id) REFERENCES entities(id);


--
-- Name: plane_component_target_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: fauxrates
--

ALTER TABLE ONLY plane_component
    ADD CONSTRAINT plane_component_target_id_fkey FOREIGN KEY (target_id) REFERENCES entities(id);


--
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


--
-- PostgreSQL database dump complete
--

