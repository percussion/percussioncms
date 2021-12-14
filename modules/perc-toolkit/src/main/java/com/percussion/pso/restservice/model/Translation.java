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

import javax.xml.bind.annotation.XmlAttribute;

/**
 */
public class Translation extends Relationship {
	/**
	 * Field locale.
	 */
	private String locale;


	/**
	 * Method setLocale.
	 * @param locale String
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}
	/**
	 * Method getLocale.
	 * @return String
	 */
	@XmlAttribute
	public String getLocale() {
		return locale;
	}
	
	
	
	 /**
	  * Method equals.
	  * @param otherO Object
	  * @return boolean
	  */
	 public boolean equals(Object otherO) {
		  if ( this == otherO ) return true;
		    if ( !(otherO instanceof Translation) ) return false;
		    Translation other = (Translation)otherO;
		   
		    if (this.getContentId()!= other.getContentId()) return false; 
		    if (this.getRevision()!= other.getRevision()) return false; 
		    if ( ! (this.getLocale() == null ? other.getLocale() == null : this.getLocale().equals(other.getLocale()))) return false;  
		    return true;
	    }

	    /**
	     * Method hashCode.
	     * @return int
	     */
	    public int hashCode() {
	        return 59878489;
	    }

	}


