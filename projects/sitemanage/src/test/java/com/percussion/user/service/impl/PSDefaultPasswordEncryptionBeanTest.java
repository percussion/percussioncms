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
