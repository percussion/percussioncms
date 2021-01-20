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
package com.percussion.user.service.impl;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PSDefaultPasswordEncryptionBeanTest
{

    /*
     * TODO For Dave's password filter work, there are two implementations that should
     * produce the same filter password, hence the following test.
     * This is obviously not ideal for if one changes the other one will have to also.
     */
    PSDefaultPasswordEncryptionBean filter = new PSDefaultPasswordEncryptionBean();


    @Test
    public void shouldCreateHashForDemoPasswordAndShouldBeEqualForBothFilters() throws Exception
    {
        String beanPassword = filter.encrypt("demo");
        String systemPassword = encrypt("demo");
        log.info("demo ==> " + beanPassword);
        assertEquals("password hashes should be equal", beanPassword, systemPassword);
    }

    public String encrypt(String password)
    {
        if(StringUtils.isBlank(password))
        {
            return StringUtils.EMPTY;
        }
        return DigestUtils.shaHex(password.trim());

    }
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSDefaultPasswordEncryptionBeanTest.class);
}
