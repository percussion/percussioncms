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
package com.percussion.pubserver;

import com.percussion.error.PSException;
import com.percussion.pubserver.data.PSPublishServerInfo;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSPubInfo;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;
import java.util.Map;

/**
 * The pubserver service is responsible for exposing the publish server
 * information
 *
 * @author leonardohildt
 * @author ignacioerro
 *
 */
public interface IPSPubServerService
{
    public static final String DEFAULT_DTS = "NONE";
    /**
     * Load the server information based on the site name and server name as the
     * parameters.
     *
     * @param siteId
     * @param serverId
     * @return a <code>PSPublishServerInfo</code> object maybe empty never
     *         <code>null</code>
     * @throws PSPubServerServiceException
     */
     PSPublishServerInfo getPubServer(String siteId, String serverId) throws PSPubServerServiceException;

    /**
     * Retrieves a list of all publish server existing including their
     * information.
     *
     * @return a list of <code>PSPublishServerInfo</code>, never empty or
     *         <code>null</code>
     * @throws PSPubServerServiceException, if an error occurs.
     */
     List<PSPublishServerInfo> getPubServerList(String name) throws PSPubServerServiceException;

    /**
     * Creates a new publish server with the provided name.
     *
     * @param siteId the site id, never empty or <code>null</code>
     * @param serverName the name of the publish server to be created
     * @param pubServerInfo the <code>PSPubServer</code> object containing the
     *            server information to create that server. Must not be empty or
     *            <code>null</code>
     * @return a <code>PSPublishServerInfo</code> object maybe empty never
     *         <code>null</code>
     * @throws PSPubServerServiceException, if the supplied object is invalid.
     */
     PSPublishServerInfo createPubServer(String siteId, String serverName, PSPublishServerInfo pubServerInfo)
             throws PSPubServerServiceException,  PSNotFoundException, PSValidationException;

    /**
     * Updates a publish server with the provided name.
     *
     * @param siteId the site id, never empty or <code>null</code>
     * @param serverId the id of the publish server to be created
     * @param pubServerInfo the <code>PSPubServer</code> object containing the
     *            server information to update that publish server. Must not be
     *            empty or <code>null</code>
     * @return a <code>PSPublishServerInfo</code> object never empty or
     *         <code>null</code>
     * @throws PSPubServerServiceException, if the supplied object is invalid.
     */
     PSPublishServerInfo updatePubServer(String siteId, String serverId, PSPublishServerInfo pubServerInfo)
             throws PSPubServerServiceException, PSDataServiceException, PSNotFoundException;

    /**
     *
     * @param siteId
     * @param serverId
     * @return
     * @throws PSPubServerServiceException
     */
     List<PSPublishServerInfo> deleteServer(String siteId, String serverId) throws PSPubServerServiceException, PSDataServiceException, PSNotFoundException;

    /**
     * Deletes all publish-servers that belong to the specified site.
     * @param siteId the ID of the site, never <code>null</code>.
     */
    public void deletePubServersBySite(IPSGuid siteId);

    /**
     *
     * @param jobId
     * @throws PSPubServerServiceException
     */
     void stopPublishing(String jobId) throws PSPubServerServiceException;

    /**
     * Get information about drivers availability
     *
     * @return a <code>Map<String, Boolean></code> object never empty or
     *         <code>null</code> at least we always will get drivers for ORACLE
     *         and MSSQL.
     */
     Map<String, Boolean> getAvailableDrivers();

    /**
     * Determine if the default publish server that belongs to a site is
     * modified or not.
     *
     * @param siteId The site ID that contains the publish server.
     * @return <code>true</code> if the default server was modified by the user.
     */
     Boolean isDefaultServerModified(String siteId);

    /**
     * Returns the default folder location for the new server.
     *
     * @param siteId the name of the site
     * @param publishType the type of publication server
     * @param driver the driver for the new server
     * @return the path for the default location for publishing
     */
     String getDefaultFolderLocation(String siteId, String publishType, String driver, String serverType);

    /**
     * Returns the default publish server defined for the site.
     *
     * @param siteId the name of the site
     * @return a <code>PSPubServer</code> object never empty or
     *         <code>null</code>
     */
     PSPubServer getDefaultPubServer(IPSGuid siteId) throws PSNotFoundException;

    /**
     * Returns the staging publish server defined for the site.
     *
     * @param siteId site guid must not be <code>null</code>.
     * @return a <code>PSPubServer</code> object may be <code>null</code> if a staging server has not been created.
     */
     PSPubServer getStagingPubServer(IPSGuid siteId) throws PSNotFoundException;

    /**
     * Create a new server with the default settings based on the site name.
     *
     * @param site the associated site
     * @param serverName the name to set for the new publishing server
     * @return the {@link PSPubServer publish server} object maybe
     *         <code>null</code> if it cannot be created
     * @throws PSPubServerServiceException
     */
     PSPubServer createDefaultPubServer(IPSSite site, String serverName) throws PSPubServerServiceException;

    /**
     * Updates folder root after renaming a site.
     *
     * @param site never <code>null</code>
     * @param root the root with the new name, never <code>null</code>
     * @param oldName The old site name, may be <code>null</code>
     *
     * @return <code>true</code> if the folder location was changed for any pub server, <code>false</code> if not
     */
     boolean updateDefaultFolderLocation(IPSSite site, String root, String oldName);

    /**
     * Thrown when an error is encountered in the publish service.
     *
     * @author leonardohildt
     *
     */
    class PSPubServerServiceException extends PSException
    {
        private static final long serialVersionUID = 1L;

        public PSPubServerServiceException()
        {
            super();
        }

        public PSPubServerServiceException(String message)
        {
            super(message);
        }

        public PSPubServerServiceException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public PSPubServerServiceException(Throwable cause)
        {
            super(cause);
        }
    }

     boolean checkPubServerConfig(PSPublishServerInfo pubServerInfo, IPSSite site);

    /**
     * Returns S3 publishing info if the default pubserver for the supplied site is amazon s3 server.
     * @param siteId must not be <code>null</code>
     * @return PSPubInfo of amazon s3 pub server, may be <code>null</code>.
     * @throws PSPubServerServiceException
     */
     PSPubInfo getS3PubInfo(IPSGuid siteId) throws PSPubServerServiceException, PSNotFoundException;

    /**
     * Finds the pub server for the supplied server id, returns null if server doesn't exist.
     * @param serverId id of the server
     * @return pub server may be <code>null</code> if no pub server exists for the supplied server id.
     * @throws PSPubServerServiceException
     */
     PSPubServer findPubServer(long serverId) throws PSPubServerServiceException;

     String getDefaultAdminURL(String siteName) throws PSPubServerServiceException, PSNotFoundException;

}
