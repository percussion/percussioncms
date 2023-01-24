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
