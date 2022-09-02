-- DROP TABLE visitor_profile_weight ;
-- DROP table visitor_profile ;
CREATE TABLE visitor_profile (
		id BIGINT NOT NULL,
		label VARCHAR(512),
		last_updated DATETIME,
		lock_profile SMALLINT,
		userid VARCHAR(256)
	) ;
ALTER TABLE visitor_profile ADD CONSTRAINT visitor_profile_key PRIMARY KEY (id) ;
CREATE TABLE visitor_profile_weight (
		id BIGINT NOT NULL,
		segment_id VARCHAR(256) NOT NULL,
		weight INTEGER NOT NULL
	) ;
ALTER TABLE visitor_profile_weight ADD CONSTRAINT visitor_profile_weight_key PRIMARY KEY (id,segment_id) ;
ALTER TABLE visitor_profile_weight ADD CONSTRAINT visitor_profile_fk FOREIGN KEY (id) REFERENCES visitor_profile (id);
