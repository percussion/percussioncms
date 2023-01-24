/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
