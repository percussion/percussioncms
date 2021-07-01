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
package com.percussion.cms.objectstore.ws;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.cms.objectstore.PSObjectPermissions;
import com.percussion.cms.objectstore.PSRelationshipInfoSet;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.cms.objectstore.PSSaveResults;
import com.percussion.cms.objectstore.client.PSRemoteAgent;
import com.percussion.cms.objectstore.client.PSRemoteCataloger;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.search.IPSExecutableSearch;
import com.percussion.search.PSExecutableSearchFactory;
import com.percussion.search.PSWSSearchResponse;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.testing.IPSClientBasedJunitTest;
import com.percussion.testing.PSClientTestCase;
import com.percussion.util.PSRemoteRequester;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit test class for the <code>PSWsFolderProcessor</code> class.
 */
@Category(IntegrationTest.class)
public class PSWsFolderProcessorTest extends PSClientTestCase
{
   private static final Logger ms_log = LogManager.getLogger(PSWsFolderProcessorTest.class);

   /**
    * Construct this unit test
    *
    * @param name The name of this test.
    */
    public PSWsFolderProcessorTest(String name)
   {
      super(name);
   }

   /**
    * Get component processor proxy for remote processor.
    *
    * @return The remote proxy, never <code>null</code>.
    * @throws Exception
    */
   private static PSComponentProcessorProxy getRemoteComponentProxy()
      throws Exception
   {
      PSRemoteRequester requester = getRemoteRequester();

      PSComponentProcessorProxy proxy = new PSComponentProcessorProxy(
         PSComponentProcessorProxy.PROCTYPE_REMOTE, requester);

      return proxy;
   }

   /**
    * Get relationship processor proxy for remote processor.
    *
    * @return The remote proxy, never <code>null</code>.
    * @throws Exception
    */
   private static PSRelationshipProcessorProxy getRemoteRelationshipProxy()
      throws Exception
   {
      PSRemoteRequester requester = getRemoteRequester();

      PSRelationshipProcessorProxy proxy =
         new PSRelationshipProcessorProxy(
            PSRelationshipProcessorProxy.PROCTYPE_REMOTE,
            requester,
            PSDbComponent.getComponentType(PSFolder.class));

      return proxy;
   }

   /**
    * Testing copy folder children. This will be used by PSRemoteAgentTest. 
    *
    * @param itemLocator An existing item locator, assume not <code>null</code>
    *
    * @throws Exception if an error occurs.
    */
   @Test
   public static void testCopyFolderChildrenItem(PSLocator itemLocator)
      throws Exception
   {
      // The get the component type for the PSFolder class
      String folderType = PSDbComponent.getComponentType(PSFolder.class);

      // Testing createFolder
      int communityId = -1;
      PSFolder folder = new PSFolder("folder1", communityId,
         PSObjectPermissions.ACCESS_ADMIN, "this is folder1");
      folder.setProperty("p1", "v1");
      folder.setProperty("p2", "v2");

      PSFolder folder2 = new PSFolder("folder2", communityId,
         PSObjectPermissions.ACCESS_ADMIN, "this is folder 2");
      PSFolder folder3 = new PSFolder("folder3", communityId,
         PSObjectPermissions.ACCESS_ADMIN, "this is folder 3");


      PSComponentProcessorProxy compProxy = getRemoteComponentProxy();

      PSComponentSummary[] summaries;

      // Testing creating folder
      PSSaveResults results =
         compProxy.save(new IPSDbComponent[] {folder, folder2, folder3});
      ms_log.info("create folder successful");

      folder = (PSFolder) results.getResults()[0];
      folder2 = (PSFolder) results.getResults()[1];
      folder3 = (PSFolder) results.getResults()[2];

      PSLocator locator  = folder.getLocator();
      PSLocator locator2 = folder2.getLocator();
      PSLocator locator3 = folder3.getLocator();

      // ==== Testing relationship ========
      List<PSLocator> locatorList = new ArrayList<PSLocator>();

      // Testing addFolderChildren
      // insert owner "locator" with dependent "locator3", "itemLocator"
      locatorList.add(locator3);
      locatorList.add(itemLocator);

      PSRelationshipProcessorProxy relProxy = getRemoteRelationshipProxy();
      relProxy.add(
         PSRelationshipConfig.TYPE_FOLDER_CONTENT, 
         locatorList, 
         locator);
      summaries = relProxy.getChildren(folderType, locator);
      assertTrue(summaries.length == 2);

      /*
       * locator  <--+- locator3
       *             |
       *             +- itemLocator
       */

      // Testing copyFolderChildren
      // dependent "locator3", "itemLocator" to owner "locator2"
      relProxy.copy(PSRelationshipConfig.TYPE_FOLDER_CONTENT, locatorList,
         locator2);
      PSComponentSummary[] summaries2 = relProxy.getChildren(folderType, locator2);

      /*
       * locator  <--+- locator3
       *             |
       *             +- itemLocator
       *
       * locator2 <--+- locator3 (cloned)
       *             |
       *             +- itemLocator (cloned)
       */

      assertTrue(summaries.length == 2);
      assertTrue(summaries2.length == 2);
      //FB: EC_BAD_ARRAY_COMPARE NC 1-17-16
      assertTrue(! Arrays.equals(summaries,summaries2));

      ms_log.info("copy folder children successful");

      // Testing delete all folders, not the items
      int deleted = compProxy.delete(folderType, new PSKey[] {locator, locator2});

      ms_log.info("deleted " + deleted + " folders and their children");
   }

   /**
    * Get a remote request object, which can be used to communicate to remote
    * Rhythmyx.
    * 
    * @return the remote request object, never <code>null</code>.
    * @throws Exception
    */
   private static PSRemoteRequester getRemoteRequester() throws Exception
   {
      PSWsFolderProcessorTest tst = new PSWsFolderProcessorTest("Test");
      
      return new PSRemoteRequester(            
            tst.getConnectionProps(IPSClientBasedJunitTest.CONN_TYPE_RXSERVER));
      
   }
   
   /**
    * Testing search
    */
   @Test
   public void testSearch() throws Exception
   {
      PSRemoteRequester requester = getRemoteRequester();

      List<String> names = new ArrayList<String>();
      names.add("sys_title");
      names.add("sys_contentcreateddate");
      names.add("sys_contentlastmodifieddate");
      List<Integer> ids = new ArrayList<Integer>();
      ids.add(new Integer(2));
      ids.add(new Integer(3));
      IPSExecutableSearch search = 
         PSExecutableSearchFactory.createExecutableSearch(requester, names, 
            ids);      
      PSWSSearchResponse searchResp = search.executeSearch();
      
      
      // there should be 2 Results
      Iterator rows = searchResp.getRows();
      assertTrue(rows.hasNext());
      rows.next();
      assertTrue(rows.hasNext());
   }
   /**
    * Testing the remote relationship processor
    *
    * @throws Exception if any error occurs.
    */
   @Test
   public void testRelationship() throws Exception
   {
      
      PSRemoteRequester req = getRemoteRequester();

      // testing cataloger
      PSRemoteCataloger cataloger = new PSRemoteCataloger(req);

      assertNotNull(cataloger.getCEFieldXml(0));
      assertNotNull(cataloger.getSearchConfig());

      PSRelationshipInfoSet infoSet = cataloger.getRelationshipInfoSet();

      // default have 7 system pre-configured relationship
      assertTrue(infoSet.size() == 7);

      // root folder, id = 1, has two children, folder "2" & "3"
      PSRelationshipProcessorProxy relProxy =
         new PSRelationshipProcessorProxy(
            PSRelationshipProcessorProxy.PROCTYPE_REMOTE,
            req,
            PSDbComponent.getComponentType(PSFolder.class));

      PSLocator locator = new PSLocator(PSFolder.ROOT_ID, 1);
      PSComponentSummary[] summaries = relProxy.getChildren(
         PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE,
         PSServerFolderProcessor.FOLDER_RELATE_TYPE, locator);

      assertTrue(summaries.length == 2);

      // child of root should have one parent, the root folder
      locator = summaries[0].getCurrentLocator();
      summaries = relProxy.getParents(
         PSRelationshipProcessorProxy.RELATIONSHIP_COMPTYPE,
         PSRelationshipConfig.TYPE_FOLDER_CONTENT, locator);

      assertTrue(summaries.length == 1
            && summaries[0].getName().equalsIgnoreCase(PSFolder.ROOT_TITLE));
   }

   /**
    * Testing all folder operations through the Proxy
    *
    * @throws Exception if an error occurs.
    */
   @Ignore //TODO: Fix this
   @Test
   public void fixme_testFolderProcessor() throws Exception
   {
      // The get the component type for the PSFolder class
      String folderType = PSDbComponent.getComponentType(PSFolder.class);

      PSRelationshipProcessorProxy relProxy = getRemoteRelationshipProxy();
      PSComponentProcessorProxy compProxy = getRemoteComponentProxy();

      // Testing getSummaryByPath
      PSComponentSummary summary = relProxy.getSummaryByPath(folderType, "/",
            PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      assertTrue(summary != null
            && summary.getName().equalsIgnoreCase(PSFolder.ROOT_TITLE));

      summary = relProxy.getSummaryByPath(folderType,
            "//Sites", PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      assertTrue(summary != null && summary.getName().equalsIgnoreCase("Sites"));
      
      summary = relProxy.getSummaryByPath(
         folderType,
         "//Sites/EnterpriseInvestments/Files", 
         PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      assertTrue(summary.getName().equals("Files"));
       
      // Testing getRelationshipOwnerPaths()
      String[] paths = relProxy.getRelationshipOwnerPaths(folderType, summary
            .getCurrentLocator(), PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      assertTrue(paths.length == 1
            && paths[0].equalsIgnoreCase("//Sites/EnterpriseInvestments"));

      paths = relProxy.getRelationshipOwnerPaths(folderType,
            new PSLocator(301, 1), PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      assertTrue(paths.length == 1
            && paths[0].equalsIgnoreCase("//Sites"));
      
      paths = relProxy.getRelationshipOwnerPaths(folderType,
            new PSLocator(2, 1), PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      assertTrue(paths.length == 1
            && paths[0].equalsIgnoreCase("/"));
      
      paths = relProxy.getRelationshipOwnerPaths(folderType,
            new PSLocator(1, 1), PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      assertTrue(paths.length == 0);
      
      // Testing createFolder
      int communityId = -1;
      PSFolder folder = new PSFolder("folder1", communityId,
         PSObjectPermissions.ACCESS_ADMIN, "this is folder1");
      folder.setProperty("p1", "v1");
      folder.setProperty("p2", "v2");

      PSFolder folder2 = new PSFolder("folder2", communityId,
         PSObjectPermissions.ACCESS_ADMIN, "this is folder 2");
      PSFolder folder3 = new PSFolder("folder3", communityId,
         PSObjectPermissions.ACCESS_ADMIN, "this is folder 3");
      PSFolder folder4 = new PSFolder("folder4", communityId,
         PSObjectPermissions.ACCESS_ADMIN, "this is folder 4");
      PSFolder folder5 = new PSFolder("folder5", communityId,
         PSObjectPermissions.ACCESS_ADMIN, "this is folder 5");


      // Catalog pre-installed folders and their relationships
      // get folder 1 (root), 2 (sites), 3 (folders)
      // revision equals -1 is ok since revision is ignored for folders
      PSLocator rootLocator = new PSLocator(1, -1);
      PSLocator sitesLocator = new PSLocator(2, -1);
      PSLocator foldersLocator = new PSLocator(3, -1);

      PSKey[] fkeys = new PSKey[] {rootLocator, sitesLocator, foldersLocator};
      Element[] folderEls = compProxy.load(folderType, fkeys);
      assertTrue(folderEls.length == 3);

      // folder 1 (root) has no parents
      PSComponentSummary[] summaries = null;
      try
      {
         summaries = relProxy.getParents(
            folderType,
            PSServerFolderProcessor.FOLDER_RELATE_TYPE,
            rootLocator);
         
         assertTrue(false); // should not get here since no parent for root
      }
      catch (PSCmsException ex)
      {
      }

      // folder 1 (root) has two children, "sites" & "folders"
      summaries = relProxy.getChildren(folderType, rootLocator);
      assertTrue(summaries.length == 2);

      summaries = relProxy.getParents(folderType,
         PSRelationshipConfig.TYPE_FOLDER_CONTENT, sitesLocator);
      assertTrue(summaries.length == 1);
      assertTrue(rootLocator.getId() == summaries[0].getCurrentLocator().getId());

      ms_log.info("Successfully get pre-installed folders");


      // Testing creating folder
      PSSaveResults results = compProxy.save(
         new IPSDbComponent[] {folder, folder2, folder3, folder4, folder5});
      ms_log.info("create folder successful");

      folder = (PSFolder) results.getResults()[0];
      folder2 = (PSFolder) results.getResults()[1];
      folder3 = (PSFolder) results.getResults()[2];
      folder4 = (PSFolder) results.getResults()[3];
      folder5 = (PSFolder) results.getResults()[4];

      PSLocator locator  = folder.getLocator();
      PSLocator locator2 = folder2.getLocator();
      PSLocator locator3 = folder3.getLocator();
      PSLocator locator4 = folder4.getLocator();
      PSLocator locator5 = folder5.getLocator();

      // Testing open folder
      PSKey[] keys = new PSKey[] {folder.getLocator()};
      Element[] elements = compProxy.load(folderType, keys);
      PSFolder target = new PSFolder(elements[0]);
      assertTrue(target.equalsFull(folder));
      ms_log.info("load folder successful");

      // Testing update folder
      target.setName("folder updated name");
      target.setCommunityId(-1);
      target.setDescription("folder description updated");
      results = compProxy.save(new PSFolder[] {target});
      PSFolder updatedFolder = (PSFolder) results.getResults()[0];
      assertTrue(updatedFolder.equals(target));
      assertTrue(! updatedFolder.equals(folder));
      ms_log.info("update folder successful");

      // ==== Testing relationship ========
      List<PSLocator> locatorList = new ArrayList<PSLocator>();

      // Testing addFolderChildren
      // insert owner "locator4" with dependent "locator5"
      locatorList.add(locator5);
      relProxy.add(
         PSRelationshipConfig.TYPE_FOLDER_CONTENT, 
         locatorList, 
         locator4);
      summaries = relProxy.getChildren(folderType, locator4);
      assertTrue(summaries.length == 1);

      /*
       * locator4 <--- locator5
       */

      // Testing addFolderChildren
      // insert owner "locator" with dependent "locator3" & "locator4"
      locatorList.clear();
      locatorList.add(locator3);
      locatorList.add(locator4);
      relProxy.add(
         PSRelationshipConfig.TYPE_FOLDER_CONTENT, 
         locatorList, 
         locator);
      ms_log.info("add folder children successful");

      /*
       * locator <-+-- locator4 <--- locator5
       *           |
       *           +-- locator3
       */

      // Testing getFolderChildren
      summaries = relProxy.getChildren(folderType, locator);
      assertTrue(summaries.length == 2);
      ms_log.info("get folder children successful");


      // Testing moveFolderChildren
      // move dependent "locator3 & 4" to new owner "locator2"
      relProxy.move(folderType, locator, locatorList, locator2);
      ms_log.info("move folder children successful");

      summaries = relProxy.getChildren(folderType, locator);
      assertTrue(summaries.length == 0);

      summaries = relProxy.getChildren(folderType, locator2);
      assertTrue(summaries.length == 2);

      /*
       * locator
       *
       * locator2 <-+-- locator4 <--- locator5
       *            |
       *            +-- locator3
       */

      // Testing update unique and non-unique names
      elements = compProxy.load(folderType, new PSKey[] {locator3});
      folder3 = new PSFolder(elements[0]);
      folder3.setName("unique folder 3");

      // update unique name
      compProxy.save(new IPSDbComponent[] {folder3});

      boolean failUpdateName = false;
      try
      {
         folder3.setName(folder4.getName()); // set to sibling name

         // update same name as its sibling, this should fail
         compProxy.save(new IPSDbComponent[] {folder3});
      }
      catch (Exception e)
      {
         failUpdateName = true;
      }
      assertTrue(failUpdateName);
      ms_log.info("updating unique and non-unique folder sibling name successful");

      // Testing copyFolderChildren
      // copy dependent "locator3 & 4" to new owner "locator"
      relProxy.copy(
         PSRelationshipConfig.TYPE_FOLDER_CONTENT,
         locatorList,
         locator);
      ms_log.info("copy folder children successful");

      summaries = relProxy.getChildren(folderType, locator);
      assertTrue(summaries.length == 2);

      summaries = relProxy.getChildren(folderType, locator2);
      assertTrue(summaries.length == 2);

      /*
       * locator  <-+-- cloned-locator4 <--- cloned-locator5
       *            |
       *            +-- cloned-locator3
       *
       * locator2 <-+-- locator4 <--- locator5
       *            |
       *            +-- locator3
       */

      // Testing add existing folder (which already has a parent)
      // Server should add a cloned folder3 (not original folder 3) to folder5
      locatorList.clear();
      locatorList.add(locator3);
      relProxy.add(
         PSRelationshipConfig.TYPE_FOLDER_CONTENT, 
         locatorList, 
         locator5);

      summaries = relProxy.getChildren(folderType, locator5);
      PSLocator clonedLocator3 = summaries[0].getCurrentLocator();
      assertTrue(! clonedLocator3.equals(locator3));
      assertTrue(summaries.length == 1);

      ms_log.info("add existing folder children successful");

      /*
       * locator  <-+-- cloned-locator4 <--- cloned-locator5
       *            |
       *            +-- cloned-locator3
       *
       * locator2 <-+-- locator4 <--- locator5 <-- cloned-locator3
       *            |
       *            +-- locator3
       */

      // Testing add a child folder with has a parent and a child folder
      locatorList.clear();
      locatorList.add(locator5);
      relProxy.add(
         PSRelationshipConfig.TYPE_FOLDER_CONTENT, 
         locatorList, 
         locator3);

      summaries = relProxy.getChildren(folderType, locator3);
      assertTrue(summaries.length == 1);

      ms_log.info("add existing folder children which has a parent and a child. successful");

      /*
       * locator  <-+-- cloned-locator4 <--- cloned-locator5
       *            |
       *            +-- cloned-locator3
       *
       * locator2 <-+-- locator4 <--- locator5 <-- cloned-locator3
       *            |
       *            +-- locator3 <--- cloned-locator5 <-- cloned-cloned-locator3
       */

      // Testing delete child folder
      // delete child folder 3 from locator

      summaries = relProxy.getChildren(folderType, locator2);
      assertTrue(summaries.length == 2);

      locatorList.clear();
      locatorList.add(locator3);
      relProxy.delete(folderType, locator2, locatorList);

      summaries = relProxy.getChildren(folderType, locator2);
      assertTrue(summaries.length == 1);

      /*
       * locator  <-+-- cloned-locator4 <--- cloned-locator5
       *            |
       *            +-- cloned-locator3
       *
       * locator2 <-+-- locator4 <--- locator5 <-- cloned-locator3
       */


      // Testing delete folder
      // locator2 --> delete folder & its 2 new child folder and 1 grand child
      // locator --> delete folder & child folder 4 and folder 5 (--> cloned folder 3)
      int deleted = compProxy.delete(folderType, new PSKey[] {locator, locator2});

      // make sure the folders have been deleted by loading the same folders
      boolean notExist = false;
      try
      {
         elements = compProxy.load(folderType, new PSKey[] {locator, locator2});
      }
      catch (Exception e)
      {
         notExist = true;
      }
      assertTrue(notExist);
      ms_log.info("deleted " + deleted + " folders and their children");

   }



   //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
   //\/\/\/\/\/\  USED FOR ENTER CONTENT FOR BENCHMARK CMS  /\/\/\/\/\/\/\/\/\/\
   //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\

   /**
    * Using "New Copy" to create items for benchmark repository.
    * 
    * @throws Exception if error occurs.
    */
   public void performNewCopy() throws Exception
   {
      long startTime = System.currentTimeMillis();
      
      PSRemoteAgent agent = new PSRemoteAgent(getRemoteRequester());
      PSLocator locators[] = new PSLocator[3];
      locators[0] = new PSLocator(303, 1);
      locators[1] = new PSLocator(309, 1);
      locators[2] = new PSLocator(339, 1);
      for (int i=0; i<3; i++)
      {
         for (int j=0; j<99; j++)
         {
            ms_log.info("Processing ["+ i +"] (" + j + ")...");
            agent.newCopyItem(locators[i]);
         }
      }
      long elapse = (System.currentTimeMillis() - startTime) / 1000; 
      ms_log.info("Elaps time: " + elapse + " secs");
   }

   /**
    * Creates site folfers for benchmark reporsitory.
    *  
    * @throws Exception if error occurs.
    */
   public void performCreateFolder() throws Exception
   {
      //for (int i=0; i<5; i++)
      //   folderCount[i] = 1;
         
      // admin1 / demo
      //createSiteFolder("//Sites/Intranet", "Intranet", "newFolderAdmin1.txt",
      //   5404, 10452); // there are 5049 items, from 5405 to 10452
       
      // admin2 / demo 
      createSiteFolder("//Sites/Internet", "Internet", "newFolderAdmin2.txt",
         355, 5403); // there are 5049 items, from 355 to 5403
   }

   /**
    * Creates folders for the given parameters.
    * 
    * @param parentPath The path of the parent folder, assume not 
    *    <code>null</code> or empty.
    * 
    * @param siteName The name of the site, assume not <code>null</code> or 
    *    empty.
    * 
    * @param dataFile The name of the data file that is used to create 
    *    folders, assume not <code>null</code> or empty.
    * 
    * @throws Exception if error occurs.
    */
   private void createSiteFolder(
      String parentPath,
      String siteName,
      String dataFile,
      int firstContentId,
      int lastContentId)
      throws Exception
   {
      FolderInfo info =
         new FolderInfo(siteName, dataFile, firstContentId, lastContentId);
      PSRelationshipProcessorProxy relProxy = getRemoteRelationshipProxy();

      PSComponentSummary parentSummary =
         relProxy.getSummaryByPath(
            PSDbComponent.getComponentType(PSFolder.class),
            parentPath,
            PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      
      PSLocator topLocator = parentSummary.getCurrentLocator();
      
      // create 25 L1 folders
      //        150 L2 folders 
      for (int i=1; i <= 25; i++)
      {
         PSLocator locator = createFolder(topLocator, 1, info);
         // create L1 folder, 1-5 => no sub-folder 
         // create L1 folder, 6-15 => 5 sub-folder
         if (i >= 6 && i <= 15 )
         {
            // create L2 folder: 10 x 5 = 50
            for (int j=1; j <=5; j++)
               createFolder(locator, 2, info);
         }
         // create L1 folder, 16-25 => 10 sub-folder
         else if (i >= 16 && i <= 25 )
         {
            for (int j=16; j <=25; j++)
               createFolder(locator, 2, info);
         }
      }
      
      // create 50 L3 folder; One L3 sub-folder per L2 76-125   
      for (int i=76; i <= 125; i++)
      {
         createFolder(info.m_l2Locators[i-1], 3, info);
      }

      // create 50 L3 folder; TWO L3 sub-folder per L2 126-150   
      for (int i=126; i <= 150; i++)
      {
         createFolder(info.m_l2Locators[i-1], 3, info);
         createFolder(info.m_l2Locators[i-1], 3, info);
      }
         
      // create 25 L4 folder; ONE L4 sub-folder per L3 51-75   
      for (int i=51; i <= 75; i++)
      {
         createFolder(info.m_l3Locators[i-1], 4, info);
      }
      
      // create 75 L4 folder; THREE L4 sub-folder per L3 76-100   
      for (int i=76; i <= 100; i++)
      {
         for (int j=0; j<3; j++)
            createFolder(info.m_l3Locators[i-1], 4, info);
      }

      // create 20 L5 folder; ONE L5 sub-folder per L4 71-90   
      for (int i=71; i <= 90; i++)
      {
         createFolder(info.m_l4Locators[i-1], 5, info);
      }
            
      // create 30 L5 folder; THREE L5 sub-folder per L4 91-100   
      for (int i=91; i <= 100; i++)
      {
         for (int j=0; j<3; j++)
            createFolder(info.m_l4Locators[i-1], 5, info);
      }
      
      assignItemToFolder(info);
   }

   /**
    * Assign folder / item relationship according to the supplied folder info.
    * 
    * @param info The folder info, assume not <code>null</code>.
    * @throws Exception
    */
   private void assignItemToFolder(FolderInfo info)
      throws Exception
   {
      ms_log.info("Processing L1 folders, next available content id: "
         + info.m_nextContentId + " ...");
      
      // L1 folder & item distribution, 25 folder with 146 items
      //    Each 1 Folder contains 50 items
      //    Each 24 Folder contains 4 items
      addItemsToFolder(info.m_l1Locators, 0, 0, 50, info);
      addItemsToFolder(info.m_l1Locators, 1, 24, 4, info);

      ms_log.info("Processing L2 folders, next available content id: "
         + info.m_nextContentId + " ...");
      
      // L2 folder & item distribution, 150 folder with 600 items
      //    Each 100 Folder contains 4 items
      //    Each 25 Folder contains 2 items
      //    Each 25 Folder contains 6 items
      addItemsToFolder(info.m_l2Locators, 0, 99, 4, info);
      addItemsToFolder(info.m_l2Locators, 100, 124, 2, info);
      addItemsToFolder(info.m_l2Locators, 125, 149, 6, info);

      ms_log.info("Processing L3 folders, next available content id: "
         + info.m_nextContentId + " ...");
      
      // L3 folder & item distribution, 100 folder with 2200 items
      //    Each 50 Folder contains 22 items
      //    Each 25 Folder contains 11 items
      //    Each 25 Folder contains 33 items
      addItemsToFolder(info.m_l3Locators, 0, 49, 22, info);
      addItemsToFolder(info.m_l3Locators, 50, 74, 11, info);
      addItemsToFolder(info.m_l3Locators, 75, 99, 33, info);

      ms_log.info("Processing L4 folders, next available content id: "
         + info.m_nextContentId + " ...");

      // L4 folder & item distribution, 100 folder with 1500 items
      //    Each 60 Folder contains 15 items
      //    Each 20 Folder contains 10 items
      //    Each 20 Folder contains 20 items
      addItemsToFolder(info.m_l4Locators, 0, 59, 15, info);
      addItemsToFolder(info.m_l4Locators, 60, 79, 10, info);
      addItemsToFolder(info.m_l4Locators, 80, 99, 20, info);

      ms_log.info("Processing L5 folders, next available content id: "
         + info.m_nextContentId + " ...");
      
      // L5 folder & item distribution, 50 folder with 600 items
      //    Each 25 Folder contains 8 items
      //    Each 25 Folder contains 16 items
      addItemsToFolder(info.m_l5Locators, 0, 24, 8, info);
      addItemsToFolder(info.m_l5Locators, 25, 49, 16, info);
      
      ms_log.info("Last available content id: " + info.m_nextContentId);
   }

   /**
    * For each supplied folders, add a number of items to it.
    * 
    * @param parents The folder locators, assume not <code>null</code> or empty.
    * 
    * @param begin The first index of the <code>parents</code>, assume greater
    *    than zero, less than <code>end</code>.
    * 
    * @param end The last index of the <code>parents</code>, assume greater
    *    than <code>begin</code>.
    * 
    * @param itemCount The number of items need to be added to each folder.
    * 
    * @param info The folder info, assume not <code>null</code>.
    * @throws Exception
    */
   private void addItemsToFolder(
      PSLocator parents[],
      int begin,
      int end,
      int itemCount,
      FolderInfo info) throws Exception
   {
      PSRelationshipProcessorProxy relProxy = getRemoteRelationshipProxy();

      for (int i=begin; i <= end; i++)
      {
         // prepare child locators         
         List<PSLocator> childLocators = new ArrayList<PSLocator>();
         for (int j=0; j < itemCount; j++)
         {
            PSLocator itemLocator = new PSLocator(info.getNextContentId(), 1);
            childLocators.add(itemLocator);
         }
         relProxy.add(
            PSRelationshipConfig.TYPE_FOLDER_CONTENT, 
            childLocators, 
            parents[i]);
      }
   }
   
   /**
    * Creates a folder from the given parameters.
    * 
    * @param parentLocator The locator of the parent folder, assume not 
    *    <code>null</code>.
    * 
    * @param level The level of the created folder, assume its range is 1-5.
    * 
    * @param info The folder info, assume not <code>null</code>.
    * 
    * @return The locator of the created folder, never <code>null</code>.
    * 
    * @throws Exception if error occurs.
    */
   private PSLocator createFolder(
      PSLocator parentLocator,
      int level,
      FolderInfo info) throws Exception
   {
      String name =
         "F"
            + String.valueOf(info.m_nextCount[level - 1])
            + "_L"
            + String.valueOf(level)
            + "_"
            + info.m_siteName;
            
      return createFolder(parentLocator, name, info);
   }
      
   /**
    * Creates a folder from the given parameters.
    * 
    * @param parentLocator The locator of the parent folder, assume not 
    *    <code>null</code>.
    * 
    * @param folderName The name of the created folder, assume it contains
    *    "_L1_", "_L2_", "_L3_", "_L4_", or "_L5_".
    * 
    * @param info The folder info, assume not <code>null</code>.
    * 
    * @return The locator of the created folder, never <code>null</code>.
    * 
    * @throws Exception if error occurs.
    */
   private PSLocator createFolder(
      PSLocator parentLocator,
      String folderName,
      FolderInfo info)
      throws Exception
   {
      // create the new folder object      
      Element folderEl = loadXmlResource(info.m_dataFile);
      PSFolder folder = new PSFolder(folderEl);
      folder.setName(folderName);
   
      // insert the new folder
      PSComponentProcessorProxy compProxy = getRemoteComponentProxy();
   
      PSSaveResults results =
         compProxy.save(new IPSDbComponent[] {folder});
      folder = (PSFolder) results.getResults()[0];
      PSLocator locator = folder.getLocator();
   
      // add the new folder to its parent
      PSRelationshipProcessorProxy relProxy = getRemoteRelationshipProxy();
   
      List<PSLocator> locatorList = new ArrayList<PSLocator>();
      locatorList.add(locator);
      relProxy.add(
         PSRelationshipConfig.TYPE_FOLDER_CONTENT, 
         locatorList, 
         parentLocator);
         
      ms_log.info("create folder successful: " 
         + info.m_nextCount[0] + ", " 
         + info.m_nextCount[1] + ", " 
         + info.m_nextCount[2] + ", " 
         + info.m_nextCount[3] + ", " 
         + info.m_nextCount[4] + ", ");
   
      if (folderName.indexOf("_L1_") != -1)
      {
         info.m_l1Locators[info.m_nextCount[0] - 1] = locator;
         info.m_nextCount[0]++;
      }
      else if (folderName.indexOf("_L2_") != -1)
      {
         info.m_l2Locators[info.m_nextCount[1] - 1] = locator;
         info.m_nextCount[1] += 1;
      }
      else if (folderName.indexOf("_L3_") != -1)
      {
         info.m_l3Locators[info.m_nextCount[2] - 1] = locator;
         info.m_nextCount[2]++;
      }
      else if (folderName.indexOf("_L4_") != -1)
      {
         info.m_l4Locators[info.m_nextCount[3] - 1] = locator;
         info.m_nextCount[3]++;
      }
      else if (folderName.indexOf("_L5_") != -1)
      {
         info.m_l5Locators[info.m_nextCount[4] - 1] = locator;
         info.m_nextCount[4]++;
      }
      
      return locator;
   }
   
   /**
    * An container class used to hold the information that is needed to create
    * (site) folders. This is used for creating folders for the benchmark 
    * database.
    */
   private class FolderInfo
   {
      /**
       * Constructs an object from the given parameters.
       * 
       * @param siteName The name of the site folder, assume not 
       *    <code>null</code> or empty.
       * 
       * @param dataFile The data file that is used to create folder, assume
       *    not <code>null</code> or empty.
       */      
      private FolderInfo(
         String siteName,
         String dataFile,
         int firstContentId,
         int maxContentId)
      {
         m_siteName = siteName;
         m_dataFile = dataFile;
         
         m_nextContentId = firstContentId;
         m_maxContentId = maxContentId;
         
         m_l1Locators = new PSLocator[25];
         m_l2Locators = new PSLocator[150];
         m_l3Locators = new PSLocator[100];
         m_l4Locators = new PSLocator[100];
         m_l5Locators = new PSLocator[50];
         
         m_nextCount = new int[5];
         for (int i=0; i<5; i++)
            m_nextCount[i] = 1;
      }

      /**
       * Get the next available content id.
       * 
       * @return the content id, which will next greater than 
       *    <code>m_maxContentId</code>.
       * 
       * @throws IllegalStateException if there is no content id available
       */
      private int getNextContentId()
      {
         if (m_nextContentId > m_maxContentId)
         {
            throw new IllegalStateException(
               "m_nextContentId ("
                  + m_nextContentId
                  + ") cannot be greater than m_maxContentId ("
                  + m_maxContentId
                  + ")");
         }
         int currentId = m_nextContentId++;
         
         return currentId;
      }
      
      /**
       * The next content id, which is used to be a child of a folder. It will
       * incremented for each usage, but it never greater than 
       * <code>m_lastContentId</code>. It is initialized by ctor.     
       */
      int m_nextContentId;

      /**
       * The maximum content id of the <code>m_nextContentId</code>. 
       * It is initialized by ctor.     
       */
      int m_maxContentId;
      
      /**
       * The name of the site, init by ctor, never <code>null</code> or empty
       * after that.
       */
      private String m_siteName;

      /**
       * The data file used to create folders, init by ctor, never 
       * <code>null</code> or empty after that.
       */
      private String m_dataFile;
      
      // next folder# for 5 levels
      private int m_nextCount[] = new int[5];

      // Array of locators, used to remember the created folder locators
      // for 5 levels. Init by ctor, never <code>null</code> or empty
      // after that.
      private PSLocator m_l1Locators[]; // locators of level 1
      private PSLocator m_l2Locators[]; // locators of level 2
      private PSLocator m_l3Locators[]; // locators of level 3
      private PSLocator m_l4Locators[]; // locators of level 4
      private PSLocator m_l5Locators[]; // locators of level 5
   }
   
   /**
    * Loads the xml resource into an xml document and returns the
    * root element.
    * @param name name of the resource,. Cannot be <code>null</code>.
    * @return root element of the xml document
    * @throws Exception on any error
    */
   private Element loadXmlResource(String name) throws Exception
   {
      java.io.InputStream in = getClass().getResourceAsStream(name);
      Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
      return doc.getDocumentElement();
   }
}
