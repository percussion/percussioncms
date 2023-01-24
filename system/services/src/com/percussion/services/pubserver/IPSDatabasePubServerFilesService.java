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

package com.percussion.services.pubserver;

import com.percussion.services.pubserver.data.PSDatabasePubServer;
import com.percussion.share.service.exception.PSDataServiceException;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * The service for managing data-source related configuration files.
 * The files include, rx-ds.xml, login-config.xml and server-beans.xml
 * Any modification of the above files requires a server restart; 
 * otherwise the changed files cannot take effect.
 *  
 * @author YuBingChen
 */
public interface IPSDatabasePubServerFilesService
{
    /**
     * Determine if the publish server that belongs to a site is modified or
     * not.
     * 
     * @param siteId The site that contains the publish server.
     * @param serverName The publish server to determine is was modified.
     * @return <code>true</code> if the server was modified by the user.
     */
    Boolean isServerModified(Long siteId, String serverName);

    /**
     * Add a server to the list of modified servers
     * 
     * @param siteId The site that contains the publish server.
     * @param serverName The publish server to determine is was modified.
     */
    void addModifiedServer(Long siteId, String serverName);

    /**
     * Gets all database publish servers, which includes site or non-site specific publish servers.
     * @return all publish servers, never <code>null</code>, but may be empty.
     */
    List<PSDatabasePubServer> getDatabasePubServers();
    
    /**
     * Gets all database publish servers that are used by specified sites.
     * @return all site specific publish servers, never <code>null</code>, but may be empty.
     */
    List<PSDatabasePubServer> getSiteDatabasePubServers();
    
    /**
     * Save the specified database publish server.
     * If the system has already contain a publish server with the same name, 
     * then the existing publish server will be replaced with the specified one; 
     * otherwise the specified publish server will be added.
     * <p>
     * Note, the name of the publish server cannot be <code>null</code> or blank
     * and it cannot contain space characters.
     * 
     * @param s the specified database publish server, not <code>null</code>.
     */
    void saveDatabasePubServer(PSDatabasePubServer s) throws PSDataServiceException;
    
    /**
     * Deletes the specified database publish server. 
     * Do nothing if there is no publish server with the matching name. 
     * @param s the to be deleted publish server, not <code>null</code>.
     */
    void deleteDatabasePubServer(PSDatabasePubServer s) throws PSDataServiceException;
    
    /**
     * Determines if the specified publish server contains valid information to connect to its target database.
     * @param s the publish server in question, not <code>null</code>.
     * @return error message if the publish server failed to connect to target database; otherwise return <code>null</code>.
     */
    String testDatabasePubServer(PSDatabasePubServer s);
    
    /**
     * Set the rx-ds.xml file. Mainly used by unit test.
     * @param dsFile the rx-ds.xml file. It may be <code>null</code>.
     */
    void setDatasourceConfigFile(File dsFile);

    /**
     * Set the login-config.xml file. Mainly used by unit test.
     * @param loginConfigFile the file. It may be <code>null</code>.
     */
    void setLoginConfigFile(File loginConfigFile);

    /**
     * Set the server-beans.xml file. Mainly used by unit test.
     * @param serverBean the server-beans.xml file. It may be <code>null</code>.
     */
    void setServerBeanFile(File serverBean);
    
    /**
     * Get information about drivers availability
     * 
     * @return
     */
    Map<String, Boolean> getAvailableDrivers();
    
    /**
     * Gets the name of the data-source from the supplied publish server.
     * @param pubServer the pub-server in question, not <code>null</code>.
     * @return the data-source name, never blank.
     */
    String getJndiDsName(PSDatabasePubServer pubServer);
}
