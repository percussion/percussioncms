/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
