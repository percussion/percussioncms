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

