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
package com.percussion.membership.services.impl;

import com.percussion.membership.data.IPSMembership;
import com.percussion.membership.data.IPSMembership.PSMemberStatus;
import com.percussion.membership.services.IPSAuthProvider;
import com.percussion.membership.services.IPSMembershipDao;
import com.percussion.membership.services.PSAuthenticationFailedException;

import org.apache.commons.lang.Validate;
import org.jasypt.util.password.BasicPasswordEncryptor;
import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of {@link IPSAuthProvider} that uses {@link IPSMembershipDao} 
 * for its implementation.
 * 
 * @author JaySeletz
 *
 */
public class PSMembershipAuthProvider implements IPSAuthProvider
{
    private IPSMembershipDao dao;
    public static final String LOGIN_ERROR_MESSAGE = "Authentication failed, invalid username or password";
    
    @Autowired
    public PSMembershipAuthProvider(IPSMembershipDao dao)
    {
        this.dao = dao;
    }
    
    @Override
    public void authenticate(String userId, String password) throws Exception
    {
        Validate.notEmpty(userId);
        
        IPSMembership member = dao.findMemberByUserId(userId);
        if (member == null || !member.getStatus().equals(PSMemberStatus.Active))
        {
            throw new PSAuthenticationFailedException(LOGIN_ERROR_MESSAGE);
        }
        
        PasswordEncryptor passwordEncryptor = PSMembershipPasswordEncryptorFactory.getPasswordEncryptor();
        if (!passwordEncryptor.checkPassword(password, member.getPassword()))
        {
            throw new PSAuthenticationFailedException(LOGIN_ERROR_MESSAGE);
        }
    }
}
