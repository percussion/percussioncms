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
package com.percussion.membership.data;

import org.apache.commons.lang.Validate;


public class PSUserGroup
{
    private String email;
    private String groups;
    
    public void setEmail(String email)
    {
        Validate.notEmpty(email);
        this.email = email;
    }
    
    /**
     * Get the user's email.
     * 
     * @return The email, not <code>null</code> or empty.
     */
    public String getEmail()
    {
        return email;
    }
    
    /**
     * @param groups the groups to set, may be empty or <code>null</code>.
     */
    public void setGroups(String groups)
    {
        this.groups = groups;
    }

    /**
     * @return the groups, may be empty or <code>null</code>.
     */
    public String getGroups()
    {
        return groups;
    }

}
