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


