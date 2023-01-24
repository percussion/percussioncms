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

/**
 * Object to change the state about of an account.
 * 
 * @author rafaelsalis
 */
public class PSAccountSummary
{
    private String email;
    private String action;
    
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
