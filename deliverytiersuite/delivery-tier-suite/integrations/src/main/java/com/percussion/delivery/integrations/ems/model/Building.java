/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery.integrations.ems.model;

import com.percussion.delivery.integrations.ems.IPSEMSEventService;
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
			log.debug(e.getMessage(), e);
		};
	}
	

}
