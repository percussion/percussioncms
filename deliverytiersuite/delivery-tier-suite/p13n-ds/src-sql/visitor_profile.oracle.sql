-- DROP TABLE visitor_profile_weight ;
-- DROP table visitor_profile ;
CREATE TABLE visitor_profile (
		id NUMBER(20, 0) NOT NULL,
		label VARCHAR2(512),
		last_updated DATE,
		lock_profile INTEGER,
		userid VARCHAR2(256)
	) ;
ALTER TABLE visitor_profile ADD CONSTRAINT visitor_profile_key PRIMARY KEY (id) ;
CREATE TABLE visitor_profile_weight (
		id NUMBER(20, 0) NOT NULL,
		segment_id VARCHAR2(256) NOT NULL,
		weight INTEGER NOT NULL
	) ;
ALTER TABLE visitor_profile_weight ADD CONSTRAINT visitor_profile_weight_key PRIMARY KEY (id,segment_id) ;
ALTER TABLE visitor_profile_weight ADD CONSTRAINT visitor_profile_fk FOREIGN KEY (id) REFERENCES visitor_profile (id);