-- DROP TABLE visitor_profile_weight ;
-- DROP table visitor_profile ;
CREATE TABLE visitor_profile (
		id bigint NOT NULL,
		label varchar(512),
		last_updated datetime,
		lock_profile boolean,
		userid varchar(256)
	) ;
ALTER TABLE visitor_profile ADD CONSTRAINT visitor_profile_key PRIMARY KEY (id) ;
CREATE TABLE visitor_profile_weight (
		id bigint NOT NULL,
		segment_id varchar(64) NOT NULL,
		weight integer NOT NULL
	) ;
ALTER TABLE visitor_profile_weight ADD CONSTRAINT visitor_profile_weight_key PRIMARY KEY (id,segment_id) ;
ALTER TABLE visitor_profile_weight ADD CONSTRAINT visitor_profile_fk FOREIGN KEY (id) REFERENCES visitor_profile (id);
