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

package com.percussion.share.dao.impl;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSAuthentication;
import com.percussion.design.objectstore.PSDirectory;
import com.percussion.design.objectstore.PSDirectorySet;
import com.percussion.design.objectstore.PSProvider;
import com.percussion.design.objectstore.PSReference;
import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.server.PSServerXmlObjectStore;
import com.percussion.design.objectstore.server.PSXmlObjectStoreLockerId;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSDirectoryServerCataloger;
import com.percussion.security.PSSecurityProvider;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequest;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.share.dao.IPSServerConfigUpdater;
import com.percussion.share.dao.PSXmlFileDataRepository.PSXmlFileDataRepositoryException;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.user.data.PSLdapConfig;
import com.percussion.user.data.PSLdapConfig.PSLdapServer;
import com.percussion.user.data.PSLdapConfig.PSLdapServer.CatalogType;
import com.percussion.user.service.impl.PSDirectoryServiceConfig;
import com.percussion.util.PSCollection;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.request.PSRequestInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.ext.Provider;

/**
 * This class is notified when both the server and container have completed initialization.  It is used to modify
 * the runtime server configuration.
 * 
 * @author peterfrontiero
 */
@Provider
@PSSiteManageBean("serverConfigUpdater")
public class PSServerConfigUpdater implements IPSServerConfigUpdater, IPSNotificationListener
{
    
    private PSDirectoryServiceConfig directoryServiceConfig;

    /**
     * Name of the auto-generated security provider.
     */
    public static final String SECURITY_PROVIDER_NAME = "ldap security provider";
    
    /**
     * Name of the auto-generated authentication.
     */    
    public static final String AUTHENTICATION_NAME = "ldap authentication";
    
    /**
     * Name of the auto-generated directory set.
     */
    public static final String DIRECTORY_SET_NAME = "ldap directory set";
    
    /**
     * The prefix for all auto-generated directory names.
     */
    private static final String DIRECTORY_NAME_PREFIX = "ldap directory";
    
    /**
     * The prefix for the provider url of directories.
     */
    private static final String PROVIDER_URL_PREFIX = "ldap://";
    
    private static final String SECURE_PROVIDER_URL_PREFIX = "ldaps://";
    
    private static final Logger log = LogManager.getLogger(PSServerConfigUpdater.class);

    /**
     * Constructs the updater.
     * 
     * @param notificationService service used to notify listeners of events, may be <code>null</code>.
     * @param directoryServiceConfig service used to load the ldap configuration file, may be <code>null</code>.
     */
    @Autowired
    public PSServerConfigUpdater(IPSNotificationService notificationService, PSDirectoryServiceConfig 
            directoryServiceConfig)
    {
        super();
        
        if (notificationService != null)
        {
            notificationService.addListener(EventType.CORE_SERVER_INITIALIZED, this);
        }
        
        if (directoryServiceConfig != null)
        {
            this.directoryServiceConfig = directoryServiceConfig;
        }
    }

    private String errorMessage = "The server could not update the server configuration. "
            + "This is very bad!";

    @Override
    public void update() throws Exception
    {
        PSLdapServer ldapServer = null;
        try
        {
            // load the new ldap configuration
            ldapServer = loadLdapConfig();
        }
        catch (PSXmlFileDataRepositoryException e)
        {
            log.error("The LDAP configuration is invalid: ", e);
            
            return;
        }
        catch (PSBeanValidationException e)
        {
            log.error("The LDAP configuration contains illegal values: ", e);
            
            return;
        }
        
        boolean isLocked = false;
        PSServerXmlObjectStore os = PSServerXmlObjectStore.getInstance();
        PSRequest configReq = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
        PSXmlObjectStoreLockerId lockerId = new PSXmlObjectStoreLockerId(
                configReq.getUserSession().getRealAuthenticatedUserEntry(), true, configReq.getUserSessionId());
                
        try 
        {
            // get the server configuration
            os.getServerConfigLock(lockerId, 5);
            isLocked = true;
            
            PSSecurityToken stok = configReq.getSecurityToken();
            PSServerConfiguration serverConfig = os.getServerConfigObject(lockerId, stok);
            
            // update the server configuration
            updateServerConfig(ldapServer, serverConfig);   
            
            // save the modified server configuration
            os.saveServerConfig(serverConfig, lockerId, stok);
            
            log.info("LDAP configuration completed.");
            
            // clear the user password
            directoryServiceConfig.clearPassword();
            
            if (log.isDebugEnabled())
            {
                String host = "";
                String port = "";
                String user = "";
                String objAttrName = "";
                String catalog = "";
                StringBuilder orgUnits = new StringBuilder("");
                
                // load the server configuration and log the LDAP properties
                PSServerConfiguration newConfig = os.getServerConfigObject(stok);
                PSAuthentication auth = newConfig.getAuthentication(AUTHENTICATION_NAME);
                if (auth != null)
                {
                    user = auth.getUser();
                    PSDirectorySet dirSet = newConfig.getDirectorySet(DIRECTORY_SET_NAME);
                    objAttrName = dirSet.getRequiredAttributeName(PSDirectorySet.OBJECT_ATTRIBUTE_KEY);
                    Iterator<?> iter = newConfig.getDirectories();
                    while (iter.hasNext())
                    {
                        if (orgUnits.length() > 0)
                        {
                            orgUnits.append(", ");
                        }
                        
                        PSDirectory dir = (PSDirectory) iter.next();
                        if (StringUtils.isEmpty(catalog))
                        {
                            catalog = dir.isShallowCatalogOption() ? PSDirectory.CATALOG_SHALLOW :
                                PSDirectory.CATALOG_DEEP;
                        }
                        
                        //FIXME:  URI needs encoded or this will error out.
                        URI providerUri = new URI(dir.getProviderUrl());
                                                
                        if (StringUtils.isEmpty(host))
                        {
                            host = providerUri.getHost();
                        }
                        
                        if (StringUtils.isEmpty(port))
                        {
                            port = String.valueOf(providerUri.getPort());
                        }
                        
                        String orgUnit = StringUtils.removeStart(providerUri.getPath(), "/");
                        orgUnits.append("organizationalUnit:").append(orgUnit);
                    }
                }
                
                if (StringUtils.isBlank(orgUnits.toString()))
                {
                    orgUnits.append("organizationalUnit:");
                }
                
                log.debug("LDAP configuration properties [host:" + host + ", port:" + port + ", user:" + user
                        + ", catalog:" + catalog + ", objectAttributeName:" + objAttrName + ", " + orgUnits + "].");
                //TODO:  What is this code doing with the parsed data other than writing this debug entry?
            }
        }
        finally
        {
            try
            {
                if (isLocked)
                {
                    os.releaseServerConfigLock(lockerId);
                }
            }
            catch (PSServerException se)
            {
                log.error("Failed to release lock on server configuration.", se);
            }
        }
    }
    

    @Override
    public void notifyEvent(PSNotificationEvent event)
    {
        notNull(event, "event");
        isTrue(EventType.CORE_SERVER_INITIALIZED == event.getType(), "Should only be registered for server startup.");

        try
        {
            update();
        }
        catch (Exception e)
        {
            throw new RuntimeException(errorMessage, e);
        }

    }
    
    /**
     * Updates the LDAP components of the supplied server configuration.  If an LDAP configuration does not exist, then
     * a security provider, authentication, directory set, and one or more directories will be created.  If an LDAP
     * configuration does exist, then only those components which have been modified will be updated.
     *
     * @param ldapServer the new LDAP configuration.  If <code>null</code>, the LDAP configuration will be removed from
     * the server configuration if it exists.
     * @param serverConfig the server configuration, may not be <code>null</code>.
     */    
    public void updateServerConfig(PSLdapServer ldapServer, PSServerConfiguration serverConfig)
    {
        if (serverConfig == null)
        {
            throw new IllegalArgumentException("serverConfig may not be null");
        }
        
        try 
        {
            // get the current authentication password
            PSAuthentication auth = serverConfig.getAuthentication(AUTHENTICATION_NAME);
            String oldPwd = auth != null ? auth.getPassword() : null;
            
            boolean removeLdap = ldapServer == null; 
            if (removeLdap)
            {            
                log.info("Removing LDAP configuration...");
            }
            
            // clear the current ldap configuration
            removeLdapConfig(serverConfig);
                        
            if (!removeLdap)
            {
                log.info("Configuring LDAP...");
                
                // add the new ldap configuration
                addLdapConfig(ldapServer, serverConfig, oldPwd);
            }
        }
        catch (Exception e) 
        {
            log.error("Failed to update server configuration.", e);
        }
    }
    
    /**
     * Reads the LDAP configuration file, ensuring it is a valid, and loads the LDAP server configuration.
     * 
     * @return LDAP server object created from the file, will be <code>null</code> if the configuration file was not
     * found, if a configuration was not specified, or if the directory service is not initialized.
     * 
     * @throws PSXmlFileDataRepositoryException if the file is invalid.
     */
    private PSLdapServer loadLdapConfig() throws PSXmlFileDataRepositoryException, PSDataServiceException {
        log.info("Loading LDAP configuration...");
        
        PSLdapServer ldapServer = null;
        
        if (directoryServiceConfig != null)
        {
            directoryServiceConfig.init();
            PSLdapConfig ldapConfig = directoryServiceConfig.getData().getLdapConfig();
            if (ldapConfig != null)
            {
                ldapServer = ldapConfig.getServer();
            }
        }
        
        return ldapServer;
    }
    
    /**
     * Removes the ldap configuration from the supplied server configuration.  This includes removing the security
     * provider, as well as all authentications, directory sets, and directories.
     * 
     * @param config the server configuration object, may not be <code>null</code>.
     */
    public void removeLdapConfig(PSServerConfiguration config)
    {
        if (config == null)
        {
            throw new IllegalArgumentException("config may not be null");
        }
        
        PSSecurityProviderInstance ldapSp = null;
        PSCollection secProviders = config.getSecurityProviderInstances();
        for (Object obj : secProviders)
        {
            PSSecurityProviderInstance sp = (PSSecurityProviderInstance) obj;
            if (sp.getName().equals(SECURITY_PROVIDER_NAME))
            {
                ldapSp = sp;
                break;
            }
        }
        
        if (ldapSp != null)
        {
            secProviders.remove(ldapSp);
        }
        
        config.removeAllAuthentications();
        config.removeAllDirectorySets();
        config.removeAllDirectories();
    }
    
    /**
     * Creates and adds an ldap configuration to the supplied server configuration.  The ldap configuration includes
     * a security provider, authentication, directory set, and one or more directories.
     * 
     * @param ldapServer the new ldap configuration, may not be <code>null</code>.
     * @param serverConfig the server configuration, may not be <code>null</code>.
     * @param oldPwd the current authentication password, may be <code>null</code> to indicate that it has not been set.
     * 
     * @throws PSIllegalArgumentException if an error occurs creating the security provider.
     */
    public void addLdapConfig(PSLdapServer ldapServer, PSServerConfiguration serverConfig, String oldPwd)
    throws PSIllegalArgumentException
    {
        if (ldapServer == null)
        {
            throw new IllegalArgumentException("ldapConfig may not be null");
        }
        
        if (serverConfig == null)
        {
            throw new IllegalArgumentException("serverConfig may not be null");
        }
        
        // add security provider
        PSReference dirSetRef = new PSReference(DIRECTORY_SET_NAME, PSDirectorySet.class.getName());
        PSProvider dirProvider = new PSProvider(PSDirectoryServerCataloger.class.getName(), PSProvider.TYPE_DIRECTORY,
                dirSetRef);
        PSSecurityProviderInstance ldapSp = new PSSecurityProviderInstance(SECURITY_PROVIDER_NAME,
                PSSecurityProvider.SP_TYPE_DIRCONN);
        ldapSp.setDirectoryProvider(dirProvider);
        ldapSp.setProperties(new Properties());
        serverConfig.getSecurityProviderInstances().add(ldapSp);
        
        // add authentication
        String newPwd = ldapServer.getPassword();
        String pwd = (oldPwd == null || !StringUtils.isEmpty(newPwd)) ? newPwd : oldPwd;
        PSAuthentication ldapAuth = new PSAuthentication(AUTHENTICATION_NAME, PSAuthentication.SCHEME_SIMPLE, 
                ldapServer.getUser(), null, pwd, null);
        serverConfig.addAuthentication(ldapAuth);
        
        String host = ldapServer.getHost();
        int port = ldapServer.getPort();
        CatalogType catalog = ldapServer.getCatalogType();
       
        // add directories
        List<PSReference> dirRefs = new ArrayList<>();
        int i = 0;
        for (String orgUnit : ldapServer.getOrganizationalUnits())
        {
            PSDirectory dir = new PSDirectory(DIRECTORY_NAME_PREFIX + ' ' + i++, catalog.name(),
                    PSDirectory.FACTORY_LDAP, AUTHENTICATION_NAME,
                    (ldapServer.getSecure() ? SECURE_PROVIDER_URL_PREFIX : PROVIDER_URL_PREFIX) + host + ':' + port + '/' + orgUnit, null);  
            serverConfig.addDirectory(dir);
            PSReference dirRef = new PSReference(dir.getName(), PSDirectory.class.getName());
            dirRefs.add(dirRef);
        }
        
        // add directory set
        PSDirectorySet dirSet = new PSDirectorySet(DIRECTORY_SET_NAME, ldapServer.getObjectAttributeName());
        String emailAttrName = ldapServer.getEmailAttributeName();
        if (StringUtils.isBlank(emailAttrName))
        {
            dirSet.setEmailAttributeName(null);
        }
        else
        {
            dirSet.setEmailAttributeName(emailAttrName);
        }
        
        for (PSReference ref : dirRefs)
        {
            dirSet.addDirectoryRef(ref);
        }
        serverConfig.addDirectorySet(dirSet);
    }
}
