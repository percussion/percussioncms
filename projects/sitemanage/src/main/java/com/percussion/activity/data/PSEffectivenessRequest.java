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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.activity.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.activity.service.IPSContentActivityService.PSUsageEnum;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A request object used for getting the effectiveness data from the rest service.  Extends the content activity
 * request by adding additional fields for usage and threshold. 
 */
@JsonRootName(value = "EffectivenessRequest")
public class PSEffectivenessRequest extends PSContentActivityRequest
{
    /**
     * @return the usage metric to use when calculating effectiveness.  This will be
     */
    public PSUsageEnum getUsage()
    {
        return usage;
    }

    /**
     * @param usage the usage to set.
     */
    public void setUsage(PSUsageEnum usage)
    {
        this.usage = usage;
    }

    /**
     * @return the acceptable threshold (views/changes) to use when calculating effectiveness.
     */
    public int getThreshold()
    {
        return threshold;
    }

    /**
     * @param threshold the threshold to set.
     */
    public void setThreshold(int threshold)
    {
        this.threshold = threshold;
    }	

    /**
     * See {@link #getUsage()}.
     */
	private PSUsageEnum usage;
	
	/**
	 * See {@link #getThreshold()}.
	 */
	private int threshold;
   
}
