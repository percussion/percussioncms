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

import static com.percussion.share.test.PSMatchers.emptyString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.user.data.PSLdapConfig;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.taskdefs.condition.IsTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Test XML file loading and clearing password.
 * @author adamgent
 *
 */
public class PSDirectoryServiceConfigTest
{
    
    private static final String CLEAR_PASSWORD_LDAP_CONFIG_XML = "ClearPasswordLdapConfig.xml";
    
    private static final String CHECK_LDAP_CONFIG_NO_SECURE_PROPERTY_XML = "CheckLdapConfigWithoutProperty.xml";
    
    private static final String CHECK_LDAP_CONFIG_XML = "CheckLdapConfig.xml";
    
    private static final String CHECK_LDAPS_CONFIG_XML = "CheckLdapsConfig.xml";

    private static final String SRC_TEST_RESOURCES_USER = "src/test/resources/user";

    private static final String BUILD_TEST_RESOURCES_USER = "build/test/resources/user";
    
    private PSDirectoryServiceConfig config;
    
    @Before
    public void setup() throws IOException {
        FileUtils.copyDirectory(
                new File(SRC_TEST_RESOURCES_USER), 
                new File(BUILD_TEST_RESOURCES_USER));
        config = new PSDirectoryServiceConfig();
        config.setRepositoryDirectory(new File(BUILD_TEST_RESOURCES_USER).getAbsolutePath());
    }
    
    @Test
    public void testClearPassword() throws Exception
    {
        config.setConfigFileName(CLEAR_PASSWORD_LDAP_CONFIG_XML);
        config.clearPassword();
        File f = new File(BUILD_TEST_RESOURCES_USER, CLEAR_PASSWORD_LDAP_CONFIG_XML);
        String xml = IOUtils.toString(new FileReader(f));
        PSLdapConfig obj = PSSerializerUtils.unmarshal(xml, PSLdapConfig.class);
        //The password should be removed now.
        assertThat(obj.getServer().getPassword(), is(emptyString())); 
        //Make sure the comment was not removed.
        assertThat(xml, containsString("<!-- FOO BAR"));
    }

    @Test
    public void testCheckLdapWithoutProperty() throws Exception
    {
        config.setConfigFileName(CHECK_LDAP_CONFIG_NO_SECURE_PROPERTY_XML);
        File f = new File(BUILD_TEST_RESOURCES_USER, CHECK_LDAP_CONFIG_NO_SECURE_PROPERTY_XML);
        String xml = IOUtils.toString(new FileReader(f));
        PSLdapConfig obj = PSSerializerUtils.unmarshal(xml, PSLdapConfig.class);
        //Check the properties from secure and port
        assertThat(obj.getServer().getSecure(), is(false));
        assertThat(obj.getServer().getPort(), is(389)); 
    }

    @Test
    public void testCheckLdap() throws Exception
    {
        config.setConfigFileName(CHECK_LDAP_CONFIG_XML);
        File f = new File(BUILD_TEST_RESOURCES_USER, CHECK_LDAP_CONFIG_XML);
        String xml = IOUtils.toString(new FileReader(f));
        PSLdapConfig obj = PSSerializerUtils.unmarshal(xml, PSLdapConfig.class);
        //Check the properties from secure and port
        assertThat(obj.getServer().getSecure(), is(false));
        assertThat(obj.getServer().getPort(), is(389)); 
    }
    
    @Test
    public void testCheckLdaps() throws Exception
    {
        config.setConfigFileName(CHECK_LDAPS_CONFIG_XML);
        File f = new File(BUILD_TEST_RESOURCES_USER, CHECK_LDAPS_CONFIG_XML);
        String xml = IOUtils.toString(new FileReader(f));
        PSLdapConfig obj = PSSerializerUtils.unmarshal(xml, PSLdapConfig.class);
        //Check the properties from secure and port
        assertThat(obj.getServer().getSecure(), is(true));
        assertThat(obj.getServer().getPort(), is(686)); 
    }
    
    @Test
    public void testMissingConfigForClearPassword() throws Exception
    {
        config.setConfigFileName("foo.xml");
        config.clearPassword();
    }
    
    

}

