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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;
/**
 */
@XmlRootElement(name="XmlValue")
public class XhtmlValue implements Value {
	/**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(XhtmlValue.class);
 
	/**
	 * Field stringValue.
	 */
	private String stringValue;
	/**
	 * Field href.
	 */
	private String href;
	/**
	 * Field type.
	 */
	public static final int TYPE=1;  

	/**
	 * Method setStringValue.
	 * @param stringValue String
	 * @see Value#setStringValue(String)
	 */
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	/**
	 * Method getStringValue.
	 * @return String
	 * @see Value#getStringValue()
	 */
	@XmlValue
	public String getStringValue() {
		return stringValue;
	}
	/**
	 * Method setHref.
	 * @param href String
	 */
	public void setHref(String href) {
		this.href = href;
	}
	/**
	 * Method getHref.
	 * @return String
	 */
	@XmlAttribute
	public String getHref() {
		return href;
	}

}
