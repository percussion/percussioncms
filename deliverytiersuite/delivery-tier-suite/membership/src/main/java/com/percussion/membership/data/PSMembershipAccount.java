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


public class PSMembershipAccount
{
    /**
     * The email for the account to create. Never <code>null</code> or empty.
     */
    private String email;
    
    /**
     * The password for the account to create. Never <code>null</code> or empty.
     */
    private String password;
    
    /**
     * Indicates if the confirmation is required. Never <code>null</code> or empty.
     */
    private Boolean confirmationRequired;

    /**
     * The confirmation page to redirect the user. Never <code>null</code> or empty.
     */
    private String confirmationPage;
    
    /**
     * @return the email
     */
    public String getEmail()
    {
        return email;
    }
    public void setEmail(String email)
    {
        this.email = email;
    }
    /**
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }
    /**
     * @param password the activation password to set
     */
    public void setPassword(String password)
    {
        this.password = password;
    }
    /**
     * @return the confirmationRequired
     */
    public Boolean isConfirmationRequired()
    {
        return confirmationRequired == null ? Boolean.FALSE : confirmationRequired;
    }
    /**
     * @param confirmationRequired the confirmation required to set
     */
    public void setConfirmationRequired(Boolean confirmationRequired)
    {
        this.confirmationRequired = confirmationRequired;
    }
    /**
     * @return the confirmation page
     */
    public String getConfirmationPage()
    {
        return confirmationPage;
    }
    /**
     * @param confirmationPage the confirmation page to set
     */
    public void setConfirmationPage(String confirmationPage)
    {
        this.confirmationPage = confirmationPage;
    }
}
