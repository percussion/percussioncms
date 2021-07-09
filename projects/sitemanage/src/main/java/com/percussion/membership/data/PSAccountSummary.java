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
package com.percussion.membership.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.Validate;

/**
 * Object to change the state about of an account.
 * 
 * @author rafaelsalis
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "AccountSummary")
public class PSAccountSummary
{
    private String email;
    private String action;
    
    /**
     * Default ctor required by jax-b
     */
    public PSAccountSummary()
    {
        
    }

    /**
     * 
     * @return the email of the account, never empty or <code>null</code>.
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * 
     * @param email the email of the account, never empty or <code>null</code>.
     */
    public void setEmail(String email)
    {
        Validate.notEmpty(email);
        this.email = email;
    }
    
    /**
     * 
     * @return the action of the account, never empty or <code>null</code>.
     */
    public String getAction()
    {
        return action;
    }
    
    /**
     * 
     * @param action set the action to perform over the account,
     * never empty or <code>null</code>.
     */
    public void setAction(String action)
    {
        Validate.notEmpty(action);
        this.action = action;
    }
}
