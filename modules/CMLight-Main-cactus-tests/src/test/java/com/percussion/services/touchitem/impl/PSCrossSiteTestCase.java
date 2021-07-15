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

package com.percussion.services.touchitem.impl;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.server.PSServer;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.relationship.IPSRelationshipService;
import com.percussion.services.relationship.PSRelationshipServiceLocator;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.cactus.ServletTestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public abstract class PSCrossSiteTestCase extends ServletTestCase
{

   private static final Logger log = LogManager.getLogger(PSCrossSiteTestCase.class);

   protected IPSContentWs c;
   protected IPSGuidManager g;
   protected IPSRelationshipService rservice;
   private PSServerFolderProcessor folderProcessor;
   
   
   public void setUp() throws Exception {
      super.setUp();
      c = PSContentWsLocator.getContentWebservice();
      g = PSGuidManagerLocator.getGuidMgr();
      rservice = PSRelationshipServiceLocator.getRelationshipService();
      session.setAttribute("RX_LOGIN_ATTEMPTS", null);
      PSSecurityFilter.authenticate(request, response, "admin1", "demo");
      folderProcessor = PSServerFolderProcessor.getInstance();
   }
   
   public void tearDown() throws Exception {
      super.tearDown();
   }
   
   public void assertRel(int ownerId, int dependentId, int revision, Integer siteId, Integer folderId) 
      throws Exception
   {
      PSRelationshipFilter f = new PSRelationshipFilter();
      f.setDependentId(dependentId);
      f.setOwnerId(ownerId);

   }
   
   public String folderIdToPath(Integer folderId) {
      if (folderId == null) return null;
      IPSGuid guid = new PSLegacyGuid(new PSLocator(folderId));
      try
      {
         String[] paths = c.findFolderPaths(guid);
         if (paths.length == 0)
            return null;
         return paths[0];
      }
      catch (PSErrorException e)
      {
         return null;
      }
   }
   
   public void assertRel(PSRelationshipFilter f, int revision, Integer siteId, Integer folderId) 
      throws Exception {
      List<PSRelationship> rs = rservice.findByFilter(f);
      assertRel(rs, revision, siteId, folderId);
   }
   
   public void assertRel(List<PSRelationship> rs, int revision, Integer siteId, Integer folderId)
   {
      boolean found = false;
      for (PSRelationship r : rs) {
         if (r.getOwner().getRevision() == revision) {
            Integer actualFolderId = r.getLegacyFolderId();
            String path = null;
            if ( actualFolderId != null ) {
               path = folderIdToPath(actualFolderId);
               assertNotNull("Path should exist if rel has folderId set.", path);
            }
            assertEquals("Revision: " + revision + " actual path: " + path, folderId, actualFolderId);
            assertEquals(siteId, r.getLegacySiteId());
            found = true;
         }
      }
      assertTrue("rel not found", found);
   }     
   
   protected Integer getFolderId(String path) {
      try
      {
         PSLegacyGuid guid = (PSLegacyGuid) c.getIdByPath(path);
         return guid.getContentId();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
   
   
   protected void sleep()
   {
      try
      {
         /*
          * Need to sleep to wait for the QUEUE.
          */
         TimeUnit.SECONDS.sleep(6);
      }
      catch (InterruptedException e)
      {
         log.error(e.getMessage());
         log.debug(e.getMessage(), e);
      }
   }


   public boolean removeFolderLikeCX(String path)
   {
      try
      {
         PSLegacyGuid guid = (PSLegacyGuid) c.getIdByPath(path);
         String p = c.findFolderPaths(guid)[0];
         PSLegacyGuid parentGuid = (PSLegacyGuid) c.getIdByPath(p);
         folderProcessor.removeChildren(parentGuid.getLocator(), asList(guid.getLocator()));
      }
      catch (Exception e)
      {
         return false;
      }
      return true;
   }
   
   public boolean removeFolder(String path)
   {
      try
      {
         IPSGuid guid = c.getIdByPath(path);
         c.deleteFolders(asList(guid), false);
      }
      catch (Exception e)
      {
         return false;
      }
      return true;
   }


   public boolean deleteFolder(String p)
   {
      try
      {
         c.deleteFolders(asList(c.getIdByPath(p)), true);
      }
      catch (Exception e)
      {
         return false;
      }
      return true;
   }


   public class ItemBuilder {
      private PSCoreItem item;
      private IPSGuid guid;
      
      public ItemBuilder(PSCoreItem item)
      {
         super();
         this.item = item;
      }

      public ItemBuilder fillItem(String name)
      {
         String value = name;
         setField("sys_title", value);
         setField("displaytitle", value);
         setField("callout", value);
         setField("sys_contentstartdate", "2011-04-12 00:00:00.0");
         return this;
      }
      
      public ItemBuilder addToPath(String path) {
         notNull(getGuid());
         try
         {
            c.addFolderChildren(path, asList(getGuid()));
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
         return this;
      }
      
      public ItemBuilder removeFromPathLikeCX(String path) {
         notNull(getGuid());
         try
         {
            PSLegacyGuid folderGuid = (PSLegacyGuid) c.getIdByPath(path);

            folderProcessor.removeChildren(folderGuid.getLocator(), asList(getLocator()));
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
         return this;
      }
      
      public ItemBuilder removeFromPath(String path) {
         notNull(getGuid());
         try
         {
            c.removeFolderChildren(path, asList(getGuid()), false);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
         return this;
      }
      
      public ItemBuilder setField(String name, String value) {
         PSItemField f = item.getFieldByName(name);
         f.addValue(f.createFieldValue(value));
         return this;
      }
      
      public ItemBuilder addDependent(IPSGuid dep, Integer siteId, Integer folderId) {
         addRel(guid, dep, siteId, folderId);
         return this;
      }
      
      public ItemBuilder addOwner(IPSGuid owner, Integer siteId, Integer folderId) {
         addRel(owner, guid, siteId, folderId);
         return this;
      }
      
      public Integer getContentId() {
         notNull(getGuid());
         PSLegacyGuid gid = (PSLegacyGuid) getGuid();
         return gid.getContentId();
      }
      
      public PSLocator getLocator() {
         notNull(getGuid());
         PSLegacyGuid gid = (PSLegacyGuid) getGuid();
         return gid.getLocator();
      }
      
      private void addRel(IPSGuid owner, IPSGuid dep, Integer siteId, Integer folderId) {
         try
         {
            notNull(guid);
            IPSSystemWs service = PSSystemWsLocator.getSystemWebservice();
            PSRelationship r = service.createRelationship(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY, owner, dep);
            r.setLegacyFolderId(folderId);
            r.setLegacySiteId(siteId);
            service.saveRelationships(asList(r));
         }
         catch (Exception e)
         {
            throw new RuntimeException("Error in test ",e);
         }
      }
      
      public void delete() {
         if (getGuid() == null) return;
         try
         {
            c.deleteItems(asList(getGuid()));
         }
         catch (Exception e)
         {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
         }
      }
      
      public IPSGuid getGuid()
      {
         return guid;
      }
      
      public PSCoreItem getItem()
      {
         return item;
      }
      
      public ItemBuilder save() {
         try
         {
            guid = c.saveItems(asList(item), false, true).get(0);
            return this;
         }
         catch (PSErrorResultsException e)
         {
            throw new RuntimeException(e);
         }
      }
   }
}
