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

package com.percussion.services.pubserver.impl;

import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.pubserver.PSPubServerDaoLocator;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class PSPubServerDaoTest
{
   /**
    * The publishing type. Used to indicate which mechanism to be used to
    * publish to the live site.
    */
   public enum PublishType{
       /**
        * Publishing defaults to local
        */
       filesystem,
       /**
        * Publishing will be done via FTP
        */
       ftp,
       /**
        * Publishing will be done via SFTP
        */
       sftp,
       /**
        * publishing will be done to database
        */
       database
    }

   /**
    * 
    */
   static final IPSSiteManager sitemgr = PSSiteManagerLocator.getSiteManager();

   /**
    * 
    */
   static final IPSPubServerDao pubServerDao = PSPubServerDaoLocator.getPubServerManager();

   /**
    * 
    */
   static SecureRandom ms_random = new SecureRandom();

   IPSSite m_site;
   static PSPubServerDaoTest instance;

   @BeforeClass
   public static void setup()
   {
      instance = new PSPubServerDaoTest();
      instance.m_site = sitemgr.createSite();
      instance.setupDummySiteData(instance.m_site);

      sitemgr.saveSite(instance.m_site);
   }

   @AfterClass
   public static void tearDown()
   {
      sitemgr.deleteSite(instance.m_site);
   }
   
   /**
    * 
    * @throws PSSiteManagerException
    */
  @Test
   public void testCreatePublishServer()
   {
      // Create publishing server for this site
      PSPubServer pubServer = pubServerDao.createServer(m_site);
      assertNotNull(pubServer);
      assertNotNull(pubServer.getName());
      assertTrue(pubServer.getPublishType().equals("filesystem"));

      // Remove
      pubServerDao.deletePubServer(pubServer);
   }

   @Test
   public void testDeletePublishServer() throws Exception
   {
      createAndDeleteServer(PublishType.ftp);
      createAndDeleteServer(PublishType.sftp);
      createAndDeleteServer(PublishType.filesystem);
      createAndDeleteServer(PublishType.database);
   }

   private void createAndDeleteServer(PublishType type) throws PSNotFoundException {
      List<PSPubServer> servers = pubServerDao.findPubServersBySite(m_site.getGUID());
      PSPubServer pubServer = createServerForSite(m_site, type);
      
      PSPubServer pubServer_2 = pubServerDao.loadPubServer(pubServer.getGUID());
      
      assertEquals(pubServer, pubServer_2);
      
      List<PSPubServer> servers_2 = pubServerDao.findPubServersBySite(m_site.getGUID());
      assertTrue(servers.size() == servers_2.size() - 1);

      pubServerDao.deletePubServer(pubServer);

      servers_2 = pubServerDao.findPubServersBySite(m_site.getGUID());
      assertTrue(servers.size() == servers_2.size());
   }

   @Test
   public void testFindPubServer()
   {
      PSPubServer pubServer = createServerForSite(m_site, PublishType.ftp);
    
      PSPubServer pubServer_2 = pubServerDao.findPubServer(pubServer.getServerId());
      PSPubServer pubServer_3 = pubServerDao.findPubServer(pubServer.getGUID());
      
      assertEquals(pubServer, pubServer_2);
      assertTrue(pubServer != pubServer_2);
      
      assertTrue(pubServer_2 == pubServer_3);
      
      pubServerDao.deletePubServer(pubServer_2);
      
      PSPubServer pubServer_5 = pubServerDao.findPubServer(pubServer.getServerId());
      assertTrue(pubServer_5 == null);
   }

   @Test
   public void testLoadPubServer() throws PSNotFoundException {
      PSPubServer pubServer = createServerForSite(m_site, PublishType.ftp);
    
      PSPubServer pubServer_2 = pubServerDao.loadPubServer(pubServer.getGUID());
      PSPubServer pubServer_3 = pubServerDao.loadPubServer(pubServer.getGUID());
      
      assertTrue(pubServer_2 == pubServer_3);
      assertEquals(pubServer_2, pubServer_3);

      String password = pubServer.getPropertyValue(IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY);
      assertEquals(password, PASSWORD_VALUE);

      pubServerDao.deletePubServer(pubServer_2);
      
      try
      {
         pubServerDao.loadPubServer(pubServer.getGUID());
         fail("Must fail to load a deleted publish server.");
      }
      catch (PSNotFoundException e)
      {
      }
   }

   @Test
   public void testModifyPublishServer() throws PSNotFoundException {
      PSPubServer pubServer = createServerForSite(m_site, PublishType.ftp);
      
      // cannot use object returned from "loadServer" to save
      PSPubServer pubServer_2 = pubServerDao.loadPubServer(pubServer.getGUID());
      
      assertEquals(pubServer, pubServer_2);
      try
      {
         pubServerDao.savePubServer(pubServer_2);
         fail("Should fail to save a server from loadServer().");
      }
      catch (IllegalArgumentException e)
      {
      }

      // can use object returned from "loadServerModifiable" to save
      pubServer = pubServerDao.loadPubServerModifiable(pubServer.getGUID());
      pubServer.setName(pubServer.getName() + "_change");
      pubServerDao.savePubServer(pubServer);
      pubServer_2 = pubServerDao.loadPubServer(pubServer.getGUID());
      assertEquals(pubServer, pubServer_2);

      // add and remove properties
      addProperty(pubServer, IPSPubServerDao.PUBLISH_PORT_PROPERTY, "2002");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_PORT_PROPERTY, "21");
      assertEquals(pubServer, pubServer_2);

      pubServerDao.savePubServer(pubServer);
      assertEquals(pubServer, pubServer_2);
      
      pubServerDao.deletePubServer(pubServer);
   }

   final static String PASSWORD_VALUE = "cm1 password";
   
   /**
    * @param testSite
    * @return
    */
   private PSPubServer createServerForSite(IPSSite testSite, PublishType type)
   {
      PSPubServer pubServer = pubServerDao.createServer(m_site);
      pubServer.setDescription("Description for the new server, type ");
      pubServer.setName(testSite.getName() + testSite.getSiteId() + "-pubServer"
            + type.name());
      generatePropertiesForPublishType(pubServer, type, testSite);
      
      pubServerDao.savePubServer(pubServer);
      
      return pubServer;
   }

   /**
    * @param type
    * @return
    */
   private void generatePropertiesForPublishType(PSPubServer pubServer,
         PublishType type, IPSSite site)
   {
      switch (type)
      {
         case database :
            generateDatabasePProperties(pubServer, site);
            break;
         case filesystem :
            generateFilesystemProperties(pubServer, site);
            break;
         case ftp :
            generateFTPProperties(pubServer, site);
            break;
         case sftp :
            generateSFTPProperties(pubServer, site);
            break;
      }
      //return props;
   }

   /**
    * @param site
    * @return
    */
   private void generateDatabasePProperties(PSPubServer pubServer, IPSSite site)
   {
      generateCommonProperties(pubServer, site);
      addProperty(pubServer, IPSPubServerDao.PUBLISH_DATABASE_SERVER_NAME, "cmrepository");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, "MySQL");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_PORT_PROPERTY, "3306");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_USER_ID_PROPERTY, "root");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_DATABASE_SERVER_NAME, "localhost");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY, "true");
   }

   private void addProperty(PSPubServer pubServer, String name, String value)
   {
      pubServer.addProperty(name, value);
   }
   
   /**
    * @param site
    * @return
    */
   private void generateFilesystemProperties(PSPubServer pubServer,
         IPSSite site)
   {
      generateCommonProperties(pubServer, site);
      addProperty(pubServer, IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY, "false");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, "FTP");
   }

   /**
    * @param site
    * @return
    */
   private void generateSFTPProperties(PSPubServer pubServer, IPSSite site)
   {
      generateCommonProperties(pubServer, site);
      addProperty(pubServer, IPSPubServerDao.PUBLISH_PORT_PROPERTY, "22");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_SECURE_FTP_PROPERTY, "true");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_SERVER_IP_PROPERTY, "100.100.100.100");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_PRIVATE_KEY_PROPERTY, "private.key");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY, "false");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, "FTP");      
   }

   private void generateCommonProperties(PSPubServer pubServer, IPSSite site)
   {
      addProperty(pubServer, IPSPubServerDao.PUBLISH_FORMAT_PROPERTY, "HTML");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_FOLDER_PROPERTY, "/" + site.getName() + "apps/ROOT");
   }

   /**
    * @param site
    * @return
    */
   private void generateFTPProperties(PSPubServer pubServer, IPSSite site)
   {
      generateCommonProperties(pubServer, site);
      addProperty(pubServer, IPSPubServerDao.PUBLISH_PORT_PROPERTY, "21");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_SECURE_FTP_PROPERTY, "false");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_SERVER_IP_PROPERTY, "100.100.100.100");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_PASSWORD_PROPERTY, PASSWORD_VALUE);
      addProperty(pubServer, IPSPubServerDao.PUBLISH_OWN_SERVER_PROPERTY, "false");
      addProperty(pubServer, IPSPubServerDao.PUBLISH_DRIVER_PROPERTY, "FTP");      
   }
   
   private void setupDummySiteData(IPSSite site)
   {
      site.setBaseUrl(getRandomString());
      site.setDescription(getRandomString());
      site.setFolderRoot(getRandomString());
      site.setRoot(getRandomString());
      site.setGlobalTemplate(getRandomString());
      site.setIpAddress(getRandomString());
      site.setName("test");
      site.setNavTheme(getRandomString());
      site.setPassword(getRandomString());
      site.setUserId(getRandomString());
      site.setPort(ms_random.nextInt(60000));
      site.setAllowedNamespaces("a,b,c");
   }

   private String getRandomString()
   {
      return "random" + ms_random.nextInt(1000);
   }
}
