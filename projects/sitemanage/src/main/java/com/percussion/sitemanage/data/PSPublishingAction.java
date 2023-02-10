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
