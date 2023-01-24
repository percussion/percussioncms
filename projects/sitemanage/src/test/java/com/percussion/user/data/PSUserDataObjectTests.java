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
package com.percussion.user.data;

import static org.junit.Assert.assertNotNull;

import com.percussion.share.dao.PSSerializerUtils;
import com.percussion.share.data.PSDataObjectTestCase;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.user.data.PSLdapConfig.PSLdapServer;
import com.percussion.user.data.PSLdapConfig.PSLdapServer.CatalogType;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.UnmarshalException;

import org.junit.Test;

public class PSUserDataObjectTests
{

    public static class PSLdapConfigTest extends PSDataObjectTestCase<PSLdapConfig> {

        @Override
        public PSLdapConfig getObject() throws Exception
        {
            PSLdapConfig c = new PSLdapConfig();
            PSLdapServer s = new PSLdapServer();
            
            s.setHost("stuff.com");
            s.setCatalogType(CatalogType.shallow);
            s.setPort(3000);
            Set<String> organizationalUnits = new HashSet<String>();
            organizationalUnits.add("asdfasdf");
            s.setOrganizationalUnits(organizationalUnits);
            s.setPassword("hidden");
            s.setUser("coolio");
            
            c.setServer(s);
            
            return c;
        }
        
        @Test
        public void testValidXml() throws Exception
        {
            PSLdapConfig config = loadXml("ValidLdapConfig.xml");
            assertNotNull(config.getServer());
            validate(config);
        }
        
        @Test(expected=PSBeanValidationException.class)
        public void testInValidXml() throws Exception
        {
            PSLdapConfig config = loadXml("InvalidLdapConfig.xml");
            assertNotNull(config.getServer());
            validate(config);
        }
        
        @Test(expected=UnmarshalException.class)
        public void testBadXml() throws Exception
        {
            loadXml("BadXmlLdapConfig.xml");
        }
        
        @Test(expected=UnmarshalException.class)
        public void testBadXmlMissingOrgUnits() throws Exception
        {
            loadXml("BadXmlOrgUnitsLdapConfig.xml");
        }
        
        private PSSpringValidationException validate(PSLdapConfig c) throws PSSpringValidationException {
            return PSBeanValidationUtils.validate(c.getServer()).throwIfInvalid();
        }
    
        private PSLdapConfig loadXml(String name) throws Exception {
            return PSSerializerUtils.unmarshalWithValidation(getClass().getResourceAsStream(name), PSLdapConfig.class);
        }
    }
    
}

