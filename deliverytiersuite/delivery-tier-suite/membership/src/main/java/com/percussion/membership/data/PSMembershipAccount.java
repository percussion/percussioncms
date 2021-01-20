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
