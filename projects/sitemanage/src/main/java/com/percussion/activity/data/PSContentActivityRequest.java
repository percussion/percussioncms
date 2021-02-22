/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.activity.data;

import com.fasterxml.jackson.annotation.JsonRootName;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A request object used for getting the content activity data from the rest service. 
 */
@JsonRootName(value = "ContentActivityRequest")
public class PSContentActivityRequest implements Serializable  
{
	
	/**
     * Default serial version
     */
    private static final long serialVersionUID = 1L;
    
    /**
	 * @return the path may be <code>null</code> or empty.
	 */
	public String getPath() 
	{
		return path;
	}
	
	public void setPath(String path) 
	{
		this.path = path;
	}

	/**
	 * @return the duration type may be <code>null</code> or empty.
	 */
	public String getDurationType() 
	{
		return durationType;
	}
	
	public void setDurationType(String durationType) 
	{
		this.durationType = durationType;
	}
	
	/**
	 * @return the duration may be <code>null</code> or empty.
	 */
	public String getDuration() 
	{
		return duration;
	}
	
	public void setDuration(String duration) 
	{
		this.duration = duration;
	}
	
	private String path;
	private String durationType;
	private String duration;
	
}
