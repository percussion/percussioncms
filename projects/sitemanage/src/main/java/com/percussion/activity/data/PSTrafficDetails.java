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

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSItemProperties;



/**
 * This object holds traffic details of the items under named site by date.
 */
@JsonRootName(value = "TrafficDetails")
public class PSTrafficDetails extends PSItemProperties
{
	public PSTrafficDetails()
	{
		
	}
	
	/**
	 * Total number of visits for this page.
	 * @return visits
	 */
    public int getVisits()
    {
        return visits;
    }

    /**
     * Delta of visits for this item.
     * @return visitsDelta
     */
    public int getVisitsDelta()
    {
        return visitsDelta;
    }

    /**
     * Sets Delta of visits for this item.
     * @param visitsDelta
     */
    public void setVisitsDelta(int visitsDelta)
    {
        this.visitsDelta = visitsDelta;
    }
    
    /**
     * Sets total number of visits for this page.
     * @param visits
     */
    public void setVisits(int visits)
    {
        this.visits = visits;
    }	
	
	private int visits;
	private int visitsDelta;
}
