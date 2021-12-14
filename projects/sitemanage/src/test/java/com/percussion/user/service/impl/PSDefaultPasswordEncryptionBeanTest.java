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
package com.percussion.user.service.impl;

import com.github.javafaker.Faker;
import com.percussion.security.PSEncryptionException;
import com.percussion.security.PSPasswordHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PSDefaultPasswordEncryptionBeanTest
{
    PSDefaultPasswordEncryptionBean filter = new PSDefaultPasswordEncryptionBean();

    @Test
    public void shouldCreateHashesThatVerifyWithPasswordHandler() throws Exception
    {
        Faker faker = new Faker();

        String testPassword = faker.aquaTeenHungerForce().character().toString();
        String beanPassword = filter.encrypt(testPassword);
        String systemPassword = encrypt(testPassword);

        log.info("These may be different even for same password depending on the salt.");
        log.info("testPassword ==> " + beanPassword);
        log.info("systemPassword ==> " + beanPassword);

        assertTrue(PSPasswordHandler.checkHashedPassword(testPassword,beanPassword));
        assertTrue(PSPasswordHandler.checkHashedPassword(testPassword,systemPassword));

    }

    public String encrypt(String password) throws PSEncryptionException {
        if(StringUtils.isBlank(password))
        {
            return StringUtils.EMPTY;
        }
        return PSPasswordHandler.getHashedPassword(password.trim());

    }
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSDefaultPasswordEncryptionBeanTest.class);
}
