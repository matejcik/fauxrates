# --- !Ups

CREATE TABLE users (
	id INT NOT NULL AUTO_INCREMENT,
	name VARCHAR(50) NOT NULL UNIQUE,
	admin BIT NOT NULL DEFAULT 0
);

CREATE TABLE entities (
	id BIGINT(20) NOT NULL AUTO_INCREMENT,
	name VARCHAR(255) UNIQUE,
	comment TEXT NOT NULL DEFAULT ''
);

CREATE TABLE character_component (
	id BIGINT(20) NOT NULL REFERENCES entities(id),
	player INT NOT NULL REFERENCES users(id),
	name VARCHAR(255) NOT NULL UNIQUE,
	online BIT
);

CREATE TABLE outpost_component (
	id BIGINT(20) NOT NULL REFERENCES entities(id),
	name VARCHAR(255) NOT NULL UNIQUE,
	x DOUBLE NOT NULL,
	y DOUBLE NOT NULL
);

CREATE TABLE plane_component (
	id BIGINT(20) NOT NULL REFERENCES entities(id),
	x DOUBLE NOT NULL,
	y DOUBLE NOT NULL,
	location_id BIGINT(20) REFERENCES entities(id),
	fuel DOUBLE NOT NULL
);

CREATE TABLE in_flight_component (
	id BIGINT(20) NOT NULL REFERENCES entities(id),
	target_x DOUBLE NOT NULL,
	target_y DOUBLE NOT NULL,
	target_id BIGINT(20) REFERENCES entities(id),
	time_to_land DATETIME
);

INSERT INTO entities (name, comment) VALUES ('OUTPOST_ZERO', 'starting point');
INSERT INTO outpost_component (id, name, x, y)
	VALUES (1, 'Outpost Zero', 0, 0);

# --- !Downs

DROP TABLE users;

DROP TABLE entities;
DROP TABLE character_component;
DROP TABLE outpost_component;
DROP TABLE plane_component;
DROP TABLE in_flight_component;