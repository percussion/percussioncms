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

import com.percussion.pubserver.IPSPubServerService.PSPubServerServiceException;
import com.percussion.pubserver.data.PSPublishServerInfo;
import com.percussion.share.service.exception.PSNotImplementedException;
import com.percussion.share.test.PSDataServiceRestClient;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

/**
 * 
 * @author leonardohildt
 * 
 */
public class PSPubServerServiceRestClient extends PSDataServiceRestClient<PSPublishServerInfo>
{
    /**
     * 
     * @param url
     */
    public PSPubServerServiceRestClient(String url)
    {
        super(PSPublishServerInfo.class, url, "/Rhythmyx/services/publishmanagement/servers/");
    }

    public PSPublishServerInfo getPubServer(String siteId, String serverId) throws PSPubServerServiceException
    {
        return getObjectFromPath(concatPath(getPath(), siteId, serverId), PSPublishServerInfo.class);
    }

    public List<PSPublishServerInfo> getPubServerList(String siteId) throws PSPubServerServiceException
    {
        return getObjectsFromPath(concatPath(getPath(), siteId));
    }

    public PSPublishServerInfo createPubServer(String siteId, String serverName, PSPublishServerInfo pubServerInfo)
            throws PSPubServerServiceException
    {
        return postObjectToPath(concatPath(getPath(), siteId, "/", serverName), pubServerInfo,
                PSPublishServerInfo.class);
    }

    public PSPublishServerInfo updatePubServer(String siteId, String serverId, PSPublishServerInfo pubServerInfo)
            throws PSPubServerServiceException
    {
        return putObjectToPath(concatPath(getPath(), siteId, "/", serverId), pubServerInfo,
                PSPublishServerInfo.class);
    }
    
    public List<PSPublishServerInfo> deleteServer(String siteId, String serverId) throws PSPubServerServiceException
    {
        return deleteObjectFromPathAndGetObjects(concatPath(getPath(), siteId, "/", serverId), PSPublishServerInfo.class);
    }

    public Map<String, Boolean> getAvailableDrivers()
    {
        // TODO Implement getAvailableDrivers
       throw new PSNotImplementedException("getAvailableDrivers is not yet implemented");
    }

    public Boolean isServerModified(String siteName, String serverName)
    {
    	//TODO: Implement isServerModified
        throw new PSNotImplementedException("getAvailableDrivers is not yet implemented");
    }

    public String testDbConnection(String siteName, String serverName, PSPublishServerInfo pubServerInfo)
    {
    	//TODO Implement testDbConnection
        throw new PSNotImplementedException("getAvailableDrivers is not yet implemented");
    }

}
