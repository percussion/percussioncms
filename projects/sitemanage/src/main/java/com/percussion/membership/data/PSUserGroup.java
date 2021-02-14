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
package com.percussion.membership.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.Validate;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "UserGroup")
public class PSUserGroup
{
    private String email;
    private String groups;
    
    /**
     * Default ctor required by jax-b
     */
    public PSUserGroup()
    {
        
    }

    
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
