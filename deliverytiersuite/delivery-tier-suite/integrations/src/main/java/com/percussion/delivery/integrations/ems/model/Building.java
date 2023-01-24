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

package com.percussion.delivery.integrations.ems.model;

import com.percussion.delivery.integrations.ems.IPSEMSEventService;
import com.percussion.error.PSExceptionUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.ParseException;
import java.util.Date;

/***
 * <Data>
    <Description>Z (old) Loker Student Union</Description>
    <BuildingCode>(old) LSU</BuildingCode>
    <ID>1</ID>
    <TimeZoneDescription>Pacific Time (US &amp; Canada); Tijuana</TimeZoneDescription>
    <TimeZoneAbbreviation>PT</TimeZoneAbbreviation>
    <CurrentLocalTime>2018-05-14T12:51:08.493</CurrentLocalTime>
  </Data>
  
 * @author natechadwick
 *
 */
@XmlRootElement(name = "PSXEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class Building {
	
	private static final Logger log = LogManager.getLogger(Building.class);
	private Integer id;
	private String buildingCode;
	
	@XmlElement(name = "PSXDisplayText")
	private String description;
	private String timeZoneAbbreviation;
	private String timeZoneDescription;
	private Date currentLocalTime;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getBuildingCode() {
		return buildingCode;
	}
	
	public void setBuildingCode(String buildingCode) {
		this.buildingCode = buildingCode;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getTimeZoneAbbreviation() {
		return timeZoneAbbreviation;
	}
	
	public void setTimeZoneAbbreviation(String timeZoneAbbreviation) {
		this.timeZoneAbbreviation = timeZoneAbbreviation;
	}
	
	public String getTimeZoneDescription() {
		return timeZoneDescription;
	}
	
	public void setTimeZoneDescription(String timeZoneDescription) {
		this.timeZoneDescription = timeZoneDescription;
	}
	
	public Date getCurrentLocalTime() {
		return currentLocalTime;
	}
	
	public void setCurrentLocalTime(String currentLocalTime) {
		try {
			this.currentLocalTime = FastDateFormat.getInstance(IPSEMSEventService.DATETIME_FORMAT_STRING).parse(currentLocalTime.replace("T", " "));
		} catch (ParseException e) {
			log.error("Error setting CurrentLocalTime with value {} and format: {}, Error: {}",currentLocalTime, IPSEMSEventService.DATETIME_FORMAT_STRING,e.getMessage());
			log.debug(PSExceptionUtils.getDebugMessageForLog(e));
		};
	}
	

}
