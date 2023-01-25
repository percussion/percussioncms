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

import com.percussion.legacy.security.deprecated.PSLegacyEncrypter;
import com.percussion.security.ToDoVulnerability;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;

public interface IPSPubServerDao
{

   /**
    * Publishing server properties names
    */
   
   public static final String PUBLISH_SERVER_NAME_PROPERTY = "serverName";
   public static final String PUBLISH_FOLDER_PROPERTY = "folder";
   public static final String PUBLISH_SERVER_IP_PROPERTY = "serverip";
   public static final String PUBLISH_USER_ID_PROPERTY = "userid";
   public static final String PUBLISH_PORT_PROPERTY = "port";
   public static final String PUBLISH_PASSWORD_PROPERTY = "password";
   public static final String PUBLISH_SECURE_FTP_PROPERTY = "secure";
   public static final String PUBLISH_SID_PROPERTY = "sid";
   public static final String PUBLISH_SCHEMA_PROPERTY = "schema";
   public static final String PUBLISH_DATABASE_NAME_PROPERTY = "database";
   public static final String PUBLISH_DATABASE_SERVER_NAME = "server";
   public static final String PUBLISH_OWNER_PROPERTY = "owner";
   public static final String PUBLISH_DEFAULT_SERVER_PROPERTY = "defaultServer";
   public static final String PUBLISH_PRIVATE_KEY_PROPERTY = "privateKey";
   public static final String PUBLISH_OWN_SERVER_PROPERTY = "ownServer";
   public static final String PUBLISH_DRIVER_PROPERTY = "driver";
   public static final String PUBLISH_RESOURCES_PROPERTY = "resources";
   public static final String PUBLISH_FORMAT_PROPERTY = "format";
   public static final String PUBLISH_EC2_REGION = "region";
   public static final String PUBLISH_AS3_BUCKET_PROPERTY = "bucketlocation";
   public static final String PUBLISH_AS3_SECURITYKEY_PROPERTY = "securitykey";
   public static final String PUBLISH_AS3_ACCESSKEY_PROPERTY = "accesskey";
   public static final String PUBLISH_AS3_USE_ASSUME_ROLE = "useAssumeRole";
   public static final String PUBLISH_AS3_ARN_ROLE = "ARNRole";
   public static final String PUBLISH_RELATED_PROPERTY = "publishRelatedItems";

   @ToDoVulnerability
   @Deprecated
   public static final String encryptionKey = PSLegacyEncrypter.PUBSERVER_ENCRYPTION_KEY;

   public static final String PUBLISH_SECURE_SITE_CONF = "publishSecureSiteConfigOnExactPath";
   
   /**
    * Create a publish server for the given site.
    * The created publish server will contain valid ID.
    * The returned object is not persisted to the repository. 
    * 
    * @return the publish server, never <code>null</code>
    */
   PSPubServer createServer(IPSSite site);

   /**
    * Load a server object from the cache, it may not be modified and saved
    * through {@link #savePubServer(PSPubServer)}.
    * 
    * @param serverId the server ID, never <code>null</code>
    * 
    * @return the server. It may be <code>null</code> if the server does not exist.
    */
   PSPubServer findPubServer(IPSGuid serverId);

   /**
    * Convenient method to find the server with id, calls the
    * {@link #findPubServer(IPSGuid)} to actually find the server.
    * <p>
    * Note, the returned object must not be modified and saved. 
    * Must use {@link #loadPubServerModifiable(IPSGuid)} if for updating a publish server.
    * 
    * @param serverId the server ID
    * 
    * @return the server. It may be <code>null</code> if the server does not exist.
    */
   PSPubServer findPubServer(long serverId);

   /**
    * Load a server object from the cache, it may not be modified and saved
    * through {@link #savePubServer(PSPubServer)}.
    * 
    * @param serverId the server ID, never <code>null</code>
    * 
    * @return the server, never <code>null</code>
    * 
    * @throws PSNotFoundException if the server does not exist
    */
   PSPubServer loadPubServer(IPSGuid serverId) throws PSNotFoundException;

   /**
    * This does the same as {@link #loadPubServer(IPSGuid)}, but the returned object
    * can be modified and saved.
    * 
    * @param serverId the server ID, never <code>null</code>.
    * 
    * @return the server, never <code>null</code>
    * 
    * @throws PSNotFoundException if the server does not exist
    */
   PSPubServer loadPubServerModifiable(IPSGuid serverId) throws PSNotFoundException;
   
   /**
    * Finds all publish servers in the specified site.
    * 
    * @param siteId Never <code>null</code>.
    * 
    * @return Ids of all the editions that match the criteria.
    */
   List<PSPubServer> findPubServersBySite(IPSGuid siteId);

   /**
    * Save or update the publishing server in the database
    * 
    * @param pubServer the publishing server, never <code>null</code>
    */
   void savePubServer(PSPubServer pubServer);

   /**
    * Delete the publishing servers from the database for the given site
    * 
    * @param pubServer the pub server, never <code>null</code>
    */
   void deletePubServer(PSPubServer pubServer);
}
