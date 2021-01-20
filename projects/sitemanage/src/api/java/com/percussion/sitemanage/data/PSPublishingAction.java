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

package com.percussion.sitemanage.data;

import org.apache.commons.lang.StringUtils;

public class PSPublishingAction
{

    private String name;

    private boolean enabled;

    public static String PUBLISHING_ACTION_PUBLISH = "Publish";
	
	public static String PUBLISHING_ACTION_SCHEDULE = "Schedule...";

    public static String PUBLISHING_ACTION_TAKEDOWN = "Remove from Site";

    public static String PUBLISHING_ACTION_STAGE = "Stage";

    public static String PUBLISHING_ACTION_REMOVE_FROM_STAGING = "Remove from Staging";

    /**
     * For serialization.
     */
    public PSPublishingAction()
    {
        
    }
    
    /**
     * Constructs a publishing action object.
     * 
     * @param name of the action, may not be blank.
     * @param enabled <code>true</code> if the action is enabled, <code>false</code> if it is disabled.
     */
    public PSPublishingAction(String name, boolean enabled)
    {
        if (StringUtils.isBlank(name))
        {
            throw new IllegalArgumentException("name may not be blank");
        }
        
        this.name = name;
        this.enabled = enabled;
    }
    
    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
}
