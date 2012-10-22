# --- !Ups

CREATE TABLE users (
	id bigserial NOT NULL PRIMARY KEY,
	name varchar(50) NOT NULL UNIQUE,
	admin boolean NOT NULL DEFAULT FALSE
);

CREATE TABLE entities (
	id bigserial NOT NULL PRIMARY KEY,
	name varchar(255) UNIQUE,
	comment text NOT NULL DEFAULT ''
);

CREATE TABLE character_component (
	id bigint NOT NULL PRIMARY KEY REFERENCES entities(id),
	player int NOT NULL REFERENCES users(id),
	name varchar(255) NOT NULL UNIQUE,
	online boolean
);

CREATE TABLE outpost_component (
	id bigint NOT NULL PRIMARY KEY REFERENCES entities(id),
	name varchar(255) NOT NULL UNIQUE,
	x double precision NOT NULL,
	y double precision NOT NULL
);

CREATE TABLE plane_component (
	id bigint NOT NULL PRIMARY KEY REFERENCES entities(id),
	x double precision NOT NULL,
	y double precision NOT NULL,
	location_id bigint REFERENCES entities(id),
	fuel double precision NOT NULL
);

CREATE TABLE in_flight_component (
	id bigint NOT NULL PRIMARY KEY REFERENCES entities(id),
	target_x double precision NOT NULL,
	target_y double precision NOT NULL,
	target_id bigint REFERENCES entities(id),
	time_to_land timestamp with time zone
);

INSERT INTO entities (name, comment) VALUES ('OUTPOST_ZERO', 'starting point');
INSERT INTO outpost_component (id, name, x, y)
	VALUES (1, 'Outpost Zero', 0, 0);

# --- !Downs

-- order is important, otherwise we violate foreign keys
-- if needed, use DROP TABLE name CASCADE;
DROP TABLE character_component;
DROP TABLE outpost_component;
DROP TABLE plane_component;
DROP TABLE in_flight_component;

DROP TABLE users;
DROP TABLE entities;