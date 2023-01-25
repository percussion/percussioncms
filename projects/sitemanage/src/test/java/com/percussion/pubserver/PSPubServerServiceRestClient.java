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
