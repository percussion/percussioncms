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
package com.percussion.services.pubserver.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidHelper;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.pubserver.data.PSPubServerProperty;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.util.PSBaseBean;
import com.percussion.utils.guid.IPSGuid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.apache.commons.lang.Validate.notNull;

@Transactional
@PSBaseBean("sys_pubserverdao")
public class PSPubServerDao
      implements
         IPSPubServerDao
{

   @PersistenceContext
   private EntityManager entityManager;

   private Session getSession(){
      return entityManager.unwrap(Session.class);
   }
   
   /**
    * Logger for the site manager
    */
   static Logger log = LogManager.getLogger(IPSConstants.PUBLISHING_LOG);

   /**
    * Default server name when a new server is created
    */
   private static final String DEFAULT_PUBLISH_TYPE = "filesystem";
   private static final String DEFAULT_PUBLISH_DRIVER = "Local";

   /**
    * Default constructor.
    */
   public PSPubServerDao()
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.pubservermgr.IPSPubServerManager#createServer(
    * com.percussion.services.sitemgr.IPSSite)
    */
   @Transactional
   public PSPubServer createServer(IPSSite site)
   {
      PSPubServer pubServer = new PSPubServer();
      long nextId = PSGuidHelper.generateNext(PSTypeEnum.PUBLISHING_SERVER).longValue();

      pubServer.setServerId(nextId);
      pubServer.setSiteId(site.getSiteId());
      pubServer.setName(site.getName());
      pubServer.setDescription("Publishing server for site " + site.getName());
      pubServer.setPublishType(DEFAULT_PUBLISH_TYPE);
      pubServer.setServerType(PSPubServer.PRODUCTION);

      pubServer.addProperty(PUBLISH_FOLDER_PROPERTY, site.getRoot());
      pubServer.addProperty(PUBLISH_DRIVER_PROPERTY, DEFAULT_PUBLISH_DRIVER);
      pubServer.addProperty(PUBLISH_OWN_SERVER_PROPERTY, "false");
      pubServer.addProperty(PUBLISH_FORMAT_PROPERTY, "HTML");

      return pubServer;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.pubservermgr.IPSPubServerManager#findServer(com
    * .percussion.utils.guid.IPSGuid)
    */
   public PSPubServer findPubServer(IPSGuid serverId)
   {
      notNull(serverId);
      
      return findServerFromDatabase(serverId);

   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.pubservermgr.IPSPubServerManager#findServer(long)
    */
   public PSPubServer findPubServer(long serverId)
   {
      IPSGuid guid = getGuidMgr().makeGuid(serverId, PSTypeEnum.PUBLISHING_SERVER);
      return findPubServer(guid);
   }

   /**
    * Gets GUID manager, lazy load.
    * @return the GUID manager, never <code>null</code>.
    */
   private IPSGuidManager getGuidMgr()
   {
      if (m_guidMgr == null)
         m_guidMgr = PSGuidManagerLocator.getGuidMgr();
      
      return m_guidMgr;
   }
   
   IPSGuidManager m_guidMgr = null;
   
   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.pubservermgr.IPSPubServerManager#loadServer(com
    * .percussion.utils.guid.IPSGuid)
    */
   public PSPubServer loadPubServer(IPSGuid serverId) throws PSNotFoundException
   {
      notNull(serverId, "serverId may not be null");

      PSPubServer pubServer = findPubServer(serverId);
      if (pubServer == null)
         throw new PSNotFoundException(serverId);

      return pubServer;
   }

   public PSPubServer loadPubServerModifiable(IPSGuid serverId) throws PSNotFoundException
   {
      notNull(serverId, "serverId may not be null");

      PSPubServer pubServer = findServerFromDatabase(serverId);
      if (pubServer == null)
         throw new PSNotFoundException(serverId);

      return pubServer;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.pubservermgr.IPSPubServerManager#findServersBySite
    * (com.percussion.utils.guid.IPSGuid)
    */
   @SuppressWarnings("unchecked")
   public List<PSPubServer> findPubServersBySite(IPSGuid siteId)
   {
      notNull(siteId);

      return getSession().createQuery(
            "from PSPubServer where siteId = :siteid").setParameter("siteid",
            siteId.longValue()).list();

   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.pubservermgr.IPSPubServerManager#saveServer(com
    * .percussion.services.pubservermgr.data.PSPubServer)
    */
   @Transactional
   public void savePubServer(PSPubServer pubServer)
   {
      notNull (pubServer);


      setValidPersistedIds(pubServer);

      getSession().saveOrUpdate(pubServer);

   }

   /**
    * Sets the persisted IDs for the properties of the supplied publish server if needed. 
    * @param pubServer the publish server in question, assumed not <code>null</code>.
    */
   private void setValidPersistedIds(PSPubServer pubServer)
   {
      for (PSPubServerProperty p : pubServer.getProperties())
      {
         if (p.getPropertyId() == -1L)
         {
            long nextId = PSGuidHelper.generateNext(PSTypeEnum.SERVER_PROPERTY).longValue();
            p.setPropertyId(nextId);
            p.setServerId(pubServer.getServerId());
         }
      }
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see
    * com.percussion.services.pubservermgr.IPSPubServerManager#deleteServer(
    * com.percussion.services.pubservermgr.data.PSPubServer)
    */
   @Transactional
   public void deletePubServer(PSPubServer pubServer)
   {
      notNull (pubServer);

      getSession().delete(pubServer);

   }

   /**
    * Look up the specified server from the database.
    * 
    * @param serverId the ID of the server, assumed not <code>null</code>.
    * 
    * @return the specified server, it may be <code>null</code> if the server
    *         does not exist.
    */
   private PSPubServer findServerFromDatabase(IPSGuid serverId)
   {
      return getSession().get(PSPubServer.class,
            serverId.longValue());
   }
}
