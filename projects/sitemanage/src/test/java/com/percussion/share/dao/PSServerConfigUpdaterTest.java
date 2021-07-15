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
package com.percussion.share.dao;

import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.w3c.dom.Document;

import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.share.dao.impl.PSServerConfigUpdater;
import com.percussion.share.test.PSTestUtils;
import com.percussion.user.data.PSLdapConfig.PSLdapServer;
import com.percussion.user.data.PSLdapConfig.PSLdapServer.CatalogType;
import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlDocumentBuilder;

public class PSServerConfigUpdaterTest extends TestCase
{
    private PSServerConfigUpdater serverConfigUpdater = new PSServerConfigUpdater(null, null);
    
    private static String SECURE_SCHEME = "ldaps";
    
    @Test
    public void testAddLdapConfig() throws Exception
    {
        PSLdapServer ldapServer = getLdapServer();
        PSServerConfiguration serverConfig = getConfig();
        
        assertFalse(doesLdapConfigExist(serverConfig));
        
        // create config
        serverConfigUpdater.addLdapConfig(ldapServer, serverConfig, null);
        
        assertTrue(doesLdapConfigExist(serverConfig));
        
        // make sure it was added correctly
        PSLdapServer serverLdapServer = getLdapServer(serverConfig);
        assertEquals(ldapServer, serverLdapServer);
        
        String oldPwd = serverLdapServer.getPassword();
        ldapServer.setHost("foo");
        
        // update existing config
        serverConfigUpdater.addLdapConfig(ldapServer, serverConfig, oldPwd);
        
        // make sure it was updated correctly
        PSLdapServer updatedLdapServer = getLdapServer(serverConfig);
        assertEquals(ldapServer, updatedLdapServer);
        assertFalse(updatedLdapServer.equals(serverLdapServer));
                
        // update with empty password
        ldapServer.setPassword("");
        serverConfigUpdater.addLdapConfig(ldapServer, serverConfig, oldPwd);
        
        // password should not have been updated
        updatedLdapServer = getLdapServer(serverConfig);
        assertFalse(updatedLdapServer.getPassword().equals(ldapServer.getPassword()));
        
        // set email attribute name
        ldapServer.setEmailAttributeName("emailaddress");
        serverConfigUpdater.addLdapConfig(ldapServer, serverConfig, oldPwd);
        
        // email attribute name should have been updated
        updatedLdapServer = getLdapServer(serverConfig);
        assertEquals(ldapServer.getEmailAttributeName(), updatedLdapServer.getEmailAttributeName());
        
        // update with empty email attribute name
        ldapServer.setEmailAttributeName("");
        serverConfigUpdater.addLdapConfig(ldapServer, serverConfig, oldPwd);
        
        // email attribute name should have been cleared
        updatedLdapServer = getLdapServer(serverConfig);
        assertNull(updatedLdapServer.getEmailAttributeName());
        
        // update with null email attribute name
        ldapServer.setEmailAttributeName(null);
        serverConfigUpdater.addLdapConfig(ldapServer, serverConfig, oldPwd);
        
        // email attribute name should still be cleared
        updatedLdapServer = getLdapServer(serverConfig);
        assertNull(updatedLdapServer.getEmailAttributeName());
    }

    @Test
    public void testAddLdapsConfig() throws Exception
    {
        PSLdapServer ldapServer = getLdapsServer();
        PSServerConfiguration serverConfig = getConfig();
        
        assertFalse(doesLdapConfigExist(serverConfig));
        
        // create config
        serverConfigUpdater.addLdapConfig(ldapServer, serverConfig, null);

        assertNotNull(ldapServer.getPassword());

        assertTrue(doesLdapConfigExist(serverConfig));
        
        // make sure it was added correctly
        PSLdapServer serverLdapServer = getLdapServer(serverConfig);
        assertEquals(ldapServer, serverLdapServer);
    }
    
    @Test
    public void testRemoveLdapConfig() throws Exception
    {
        PSServerConfiguration serverConfig = getConfig();
        PSServerConfiguration origServerConfig = new PSServerConfiguration();
        origServerConfig.fromXml(serverConfig.toXml());
        
        assertFalse(doesLdapConfigExist(serverConfig));
        
        // remove nothing
        serverConfigUpdater.removeLdapConfig(serverConfig);
    
        // add config
        serverConfigUpdater.addLdapConfig(getLdapServer(), serverConfig, null);
    
        assertTrue(doesLdapConfigExist(serverConfig));
        
        // remove config
        serverConfigUpdater.removeLdapConfig(serverConfig);
        
        assertFalse(doesLdapConfigExist(serverConfig));
        assertEquals(origServerConfig, serverConfig);        
    }
        
    /**
     * Loads the test server configuration file.
     * 
     * @return server configuration, never <code>null</code>.
     * 
     * @throws Exception
     */
    private PSServerConfiguration getConfig() throws Exception
    {
        String configStr = PSTestUtils.resourceToString(getClass(), "PSServerConfigUpdaterTest-config.xml");
        Reader r = new StringReader(configStr);
        Document doc = PSXmlDocumentBuilder.createXmlDocument(r, false);
        PSServerConfiguration config = new PSServerConfiguration();
        config.fromXml(doc);
        
        return config;
    }
    
    /**
     * Checks if the specified server configuration includes ldap.
     * 
     * @param config server configuration, assumed not <code>null</code>.
     * 
     * @return <code>true</code> if ldap is configured, <code>false</code> otherwise.
     */
    private boolean doesLdapConfigExist(PSServerConfiguration config)
    {
        boolean spExists = false;
        PSCollection secProviders = config.getSecurityProviderInstances();
        for (Object obj : secProviders)
        {
            PSSecurityProviderInstance sp = (PSSecurityProviderInstance) obj;
            if (sp.getName().equals(PSServerConfigUpdater.SECURITY_PROVIDER_NAME))
            {
                spExists = true;
                break;
            }
        }
        
        return spExists && (config.getAuthentication(PSServerConfigUpdater.AUTHENTICATION_NAME) != null)
              && (config.getDirectorySet(PSServerConfigUpdater.DIRECTORY_SET_NAME) != null)
              && (config.getDirectories().hasNext());
    }
    
    /**
     * Gets the ldap configuration from the specified server configuration.
     * 
     * @param config server configuration, assumed not <code>null</code>.]
     * 
     * @return the ldap server configuration or <code>null</code> if not found.
     * @throws URISyntaxException if an error occurs loading a directory's provider url.
     */
    @SuppressWarnings("unchecked")
    private PSLdapServer getLdapServer(PSServerConfiguration config) throws URISyntaxException
    {
        PSLdapServer ldapServer = null;
        
        if (doesLdapConfigExist(config))
        {
            ldapServer = new PSLdapServer();
            
            PSAuthentication auth = config.getAuthentication(PSServerConfigUpdater.AUTHENTICATION_NAME);
            ldapServer.setUser(auth.getUser());
            ldapServer.setPassword(auth.getPassword());
            ldapServer.setObjectAttributeName(config.getDirectorySet(
                    PSServerConfigUpdater.DIRECTORY_SET_NAME).getRequiredAttributeName(
                            PSDirectorySet.OBJECT_ATTRIBUTE_KEY));
            ldapServer.setEmailAttributeName(config.getDirectorySet(
                    PSServerConfigUpdater.DIRECTORY_SET_NAME).getRequiredAttributeName(
                            PSDirectorySet.EMAIL_ATTRIBUTE_KEY));
            
            Set<String> orgUnits = new HashSet<String>();
            
            Iterator iter = config.getDirectories();
            while (iter.hasNext())
            {
                PSDirectory dir = (PSDirectory) iter.next();
                URI providerUri = new URI(dir.getProviderUrl());
                                
                if (ldapServer.getHost() == null)
                {
                    ldapServer.setHost(providerUri.getHost());
                }
                
                if (ldapServer.getPort() == null)
                {
                    ldapServer.setPort(providerUri.getPort());
                }
                
                if (ldapServer.getCatalogType() == null)
                {
                    ldapServer.setCatalogType(dir.isShallowCatalogOption() ? CatalogType.shallow : CatalogType.deep);
                }
                
                if (SECURE_SCHEME.equalsIgnoreCase(providerUri.getScheme()))
                {
                    ldapServer.setSecure(true);
                }
                
                orgUnits.add(StringUtils.removeStart(providerUri.getPath(), "/"));                
            }
            
            ldapServer.setOrganizationalUnits(orgUnits);
        }
                
        return ldapServer;
    }
    
    /**
     * Creates a test ldap server configuration.
     * 
     * @return the configuration, never <code>null</code>.
     */
    private PSLdapServer getLdapServer()
    {
        PSLdapServer ldapServer = new PSLdapServer();
        ldapServer.setHost("e2srv");
        ldapServer.setPort(390);
        ldapServer.setUser("cn=Directory Manager");
        ldapServer.setPassword("password");
        ldapServer.setCatalogType(CatalogType.deep);
        ldapServer.setObjectAttributeName("cn");
        Set<String> organizationalUnits = new HashSet<String>();
        organizationalUnits.add("ou=Users,dc=percussion.com,dc=com");
        organizationalUnits.add("ou=Members,dc=percussion.org,dc=org");
        ldapServer.setOrganizationalUnits(organizationalUnits);
        
        return ldapServer;
    }
    
    /**
     * Creates a test ldap server configuration using ldaps.
     * 
     * @return the configuration, never <code>null</code>.
     */
    private PSLdapServer getLdapsServer()
    {
        PSLdapServer ldapServer = new PSLdapServer();
        ldapServer.setHost("e2srv");
        ldapServer.setPort(686);
        ldapServer.setUser("cn=Directory Manager");
        ldapServer.setPassword("password");
        ldapServer.setCatalogType(CatalogType.deep);
        ldapServer.setObjectAttributeName("cn");
        Set<String> organizationalUnits = new HashSet<String>();
        organizationalUnits.add("ou=Users,dc=percussion.com,dc=com");
        organizationalUnits.add("ou=Members,dc=percussion.org,dc=org");
        ldapServer.setOrganizationalUnits(organizationalUnits);
        ldapServer.setSecure(true);
        
        return ldapServer;
    }    
}
