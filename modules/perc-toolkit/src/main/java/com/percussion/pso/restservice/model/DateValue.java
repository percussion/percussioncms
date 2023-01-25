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
package com.percussion.pso.restservice.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 */
@XmlRootElement(name="DateValue")
@XmlAccessorType(XmlAccessType.NONE)
public class DateValue implements Value {

	/**
	 * Logger for this class
	 */
	private static final Logger log = LogManager.getLogger(DateValue.class);
	/**
	 * Field DATE_FORMAT.
	 * (value is ""yyyy-MM-dd HH:mm:ss"")
	 */
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * Field date.
	 */
	private Date date;
	/**
	 * Field type.
	 */
	public static final int TYPE=2;  
	
	/**
	 * Method getDate.
	 * @return Date
	 */
	public Date getDate() {
		return date;
	}
	
	/**
	 * Method setDate.
	 * @param date Date
	 */
	public void setDate(Date date) {
		this.date = date;
	}
	
	
	/**
	 * Method getStringValue.
	 * @return String
	 * @see Value#getStringValue()
	 */
	@XmlValue
	public String getStringValue() {
		SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
		return df.format(date);
	}
	/**
	 * Method setStringValue.
	 * @param value String
	 * @see Value#setStringValue(String)
	 */
	public void setStringValue(String value) {
		// handle other formats
		SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
	
		try {
			date = df.parse(value);
		} catch (ParseException e) {
			log.debug("cannot parse date" + value);
		}
	}
	

	
}
