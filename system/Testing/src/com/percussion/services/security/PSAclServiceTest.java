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
package com.percussion.services.security;


import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.data.PSAccessLevelImpl;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.junit.Ignore;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;


/**
 * Test the {@link IPSAclService} CRUD operations.  See 
 * {@link PSAclServiceAccessTest} for other service functionality.
 */
@Category(IntegrationTest.class)
public class PSAclServiceTest
{
   //All test acls have this name so they can be easily deleted
   private static final String TEST_ACL_NAME = "aclUnitTest";

   /**
    * object guid for template
    */
   private static IPSGuid ms_templateGuid = new PSGuid(PSTypeEnum.TEMPLATE,
         10023);

   private static IPSGuid ms_templateGuid2 = new PSGuid(PSTypeEnum.TEMPLATE,
         10024);
   
   /**
    * object guid for slot
    */
   private static IPSGuid ms_slotTemplate = new PSGuid(PSTypeEnum.SLOT, 10023);


   /**
    * Util method for MSM tests, where an ACL is created but either :
    *    1. persisted for deserialization and saveAcl
    *    2. created for serialization and saveAcl
    *    
    * @param persist if <code>true</code> saveAcl, else just create and return
    * 
    * @return The list, never <code>null</code>.
    * 
    * @throws Exception
    */
   private List<IPSAcl> createAcl(boolean persist) throws Exception
   {
      IPSAclService aclService = PSAclServiceLocator.getAclService();
      
      List<IPSAcl> aclList = new ArrayList<IPSAcl>();
      PSAclImpl acl; 
      acl = (PSAclImpl) aclService.createAcl(ms_templateGuid, 
         new PSTypedPrincipal("admin1", PrincipalTypes.USER));
      aclList.add(acl);
      
      IPSAclEntry aclEntry;
      aclEntry = acl.getEntries().iterator().next();
      PSAccessLevelImpl perm = new PSAccessLevelImpl((PSAclEntryImpl)aclEntry,PSPermissions.READ);
     
      if ( persist )
         aclService.saveAcls(aclList);
      return aclList;
   }
   
   /**
    * Util method for testing deserialization by MSM
    * @param acl the acl that needs update
    */
   private void addDefaultAclEntryToAcl(PSAclImpl acl)
   {
      PSAclEntryImpl aclEntry = new PSAclEntryImpl(new PSTypedPrincipal(
         "Default", PrincipalTypes.COMMUNITY));
      aclEntry.addPermission(PSPermissions.RUNTIME_VISIBLE);
      acl.addEntry(aclEntry);
   }
   
   /**
    * Util method for testing deserialization by MSM
    * @param acl
    */
   private void removeAdminAclEntryToAcl(PSAclImpl acl)
   {
      Collection<IPSAclEntry> entries = acl.getEntries();
      IPSAclEntry adminAclEntry = null;
      for (IPSAclEntry entry : entries)
      {
         if ( entry.getName().compareTo("admin1") == 0 )
         {
            adminAclEntry = entry;
            break;
         }
      }
      if ( adminAclEntry != null )
      {
         entries.remove(adminAclEntry);
      }
   }
   
   /**
    * MSM specific which creates a new acl and persists
    * @throws Exception
    */
   @Test
   public void testAclSerializationAndPersist() throws Exception
   {
      PSAclImpl tmpAcl = null;
      IPSAclService svc = PSAclServiceLocator.getAclService();
      try
      {
         tmpAcl = (PSAclImpl)svc.loadAclForObject(ms_templateGuid);
         List<IPSAcl> acls = new ArrayList<IPSAcl>();
         if ( tmpAcl != null )
         {
            acls.add(tmpAcl);
            svc.deleteAcl(tmpAcl.getGUID());
            tmpAcl = null;
            acls.clear();
         }
         tmpAcl = (PSAclImpl) createAcl(false).get(0);
         String s = tmpAcl.toXML();
         try
         {
            tmpAcl.fromXML(s);
         }
         catch ( Exception e)
         {
            e.printStackTrace();
         }
         
         acls.add(tmpAcl);
         svc.saveAcls(acls);  
         
         PSAclImpl savedAcl = (PSAclImpl)svc.loadAcl(tmpAcl.getGUID());
         assertEquals(tmpAcl, savedAcl);
      }
      finally
      {
         if (tmpAcl != null)
            svc.deleteAcl(tmpAcl.getGUID());
      }
   }

   /**
    * MSM test which loads an existing acl, modifies and updates
    * After deserialization, add "Default" aclEntry, remove "admin1" aclEntry
    * and update the system
    * @throws Exception
    */
   @Test
   public void testAclDeserializationAndUpdate() throws Exception
   {
      IPSAclService svc = PSAclServiceLocator.getAclService();
      boolean created = false;
      PSAclImpl tmpAcl = null;
      try
      {
         createAcl(true);
         tmpAcl = (PSAclImpl) svc.loadAclsForObjectsModifiable(Collections
               .singletonList(ms_templateGuid)).get(0);
         created = true;

         String s = tmpAcl.toXML();
         tmpAcl.fromXML(s);
         addDefaultAclEntryToAcl(tmpAcl);
         removeAdminAclEntryToAcl(tmpAcl);
         s = tmpAcl.toXML();
         List<IPSAcl> acls = new ArrayList<IPSAcl>();
         acls.add(tmpAcl);
         svc.saveAcls(acls);
      }
      finally
      {
         if (created)
            svc.deleteAcl(tmpAcl.getGUID());
      }
   }
  
   /**
    * Test all CRUD operations
    * 
    * @throws Exception If there are any errors.
    */
   @Test
   public void testCRUDOperations() throws Exception
   {
      boolean success = false;
      List<IPSAcl> aclList = createTestAcls();
      
      try
      {
         aclList = testLoadMethods(aclList);
         aclList = testSave(aclList);
         success = true;
      }
      finally
      {
         try
         {
            deleteAcls(aclList);
         }
         catch (Exception e)
         {
            if (success)
               throw (Exception)e.fillInStackTrace();
            System.out.println("Failed to delete acls: " + 
               e.getLocalizedMessage());
         }
      }
   }
   
   /**
    * Tests 7 scenarios: 
    * <ul>
    *    <li>AnyCommunity allowed, no specific entry</li>
    *    <li>AnyCommunity allowed, a specific entry allowed</li>
    *    <li>AnyCommunity allowed, a specific entry not allowed</li>
    * </ul>
    * Repeat all these tests cases w/ AnyCommunity not allowed.
    * Change case of community name on 1 test for 7th scenario.
    * 
    * @throws Exception If test ACL creation fails.
    */
   @Test
   public void testFindObjectsVisibleToCommunities()
      throws Exception
   {
      IPSAclService aclService = PSAclServiceLocator.getAclService();
      List<IPSAcl> aclList = createTestAcls();
      
      try
      {
         //use UPPER case name to test case-insensitivity
         Collection<IPSGuid> ids = aclService.findObjectsVisibleToCommunities(
               Collections.singletonList("COMM1"), PSTypeEnum.TEMPLATE);
         // there may be templates besides our test templates for which this
         // community gets default visibility, thus we must check the actual ids
         // rather than a count
         assertTrue(ids.contains(ms_templateGuid));
         assertFalse(ids.contains(ms_templateGuid2));

         ids = aclService.findObjectsVisibleToCommunities(
               Collections.singletonList("comm2"), PSTypeEnum.TEMPLATE);
         assertTrue(ids.contains(ms_templateGuid2));
         assertFalse(ids.contains(ms_templateGuid));

         ids = aclService.findObjectsVisibleToCommunities(
               Collections.singletonList("comm3"), PSTypeEnum.TEMPLATE);
         assertTrue(ids.contains(ms_templateGuid));
         assertFalse(ids.contains(ms_templateGuid2));
      }
      finally
      {
         deleteAcls(aclList);
      }
      
   }

   /**
    * Verify that ACL count >> 450 is handled correctly. 
    * @throws Exception
    */
   @Ignore("org.hibernate.exception.SQLGrammarException: could not execute query on Derby")
   @Test
   public void testLargeAclCount()
      throws Exception
   {
      
      //TODO: Fix Me
      /*
       * The # of test acls must be > 2*MAX (as defined in the
       * doLoadModifiableAclsForObjects method in PSAclService
       */ 
//      List<IPSGuid> guids = create1000Acls();
//      IPSAclService svc = PSAclServiceLocator.getAclService();
//      //these params must be null to exercise the right code
//      List<IPSAcl> firstLoad = svc.loadAcls(null);
//      List<IPSAcl> secondLoad = svc.loadAcls(null);
//      if (firstLoad.size() != secondLoad.size())
//         fail();
//      for (IPSGuid g : guids)
//         svc.deleteAcl(g);
   }
   
   /**
    * Creates and persists 1000 ACLs (they all are identical except for the
    * ids.) They all have a name of aclUnitTest so they can be cleaned up later
    * by name.
    * 
    * @return Never <code>null</code>.
    * @throws Exception
    */
   private List<IPSGuid> create1000Acls()
      throws Exception
   {
      IPSAclService svc = PSAclServiceLocator.getAclService();
      List<IPSGuid> aclGuids = new ArrayList<IPSGuid>();
      List<IPSAcl> aclList = new ArrayList<IPSAcl>();
      PSAclImpl acl; 

      for (int i = 0; i < 1000; i++)
      {
         acl = (PSAclImpl) svc.createAcl(new PSGuid(PSTypeEnum.ACTION, 55000+i),
               new PSTypedPrincipal("admin1", PrincipalTypes.USER));
         acl.setName(TEST_ACL_NAME);
         aclList.add(acl);
         IPSAclEntry aclEntry;
         aclEntry = acl.getEntries().iterator().next();
         aclEntry.addPermission(PSPermissions.READ);
   
         aclEntry = new PSAclEntryImpl(new PSTypedPrincipal("Editor",
               PrincipalTypes.ROLE));
         aclEntry.addPermission(PSPermissions.UPDATE);
         aclEntry.addPermission(PSPermissions.READ);
         acl.addEntry((PSAclEntryImpl) aclEntry);
   
         aclEntry = new PSAclEntryImpl(new PSTypedPrincipal(
               PSTypedPrincipal.ANY_COMMUNITY_ENTRY, PrincipalTypes.COMMUNITY));
         aclEntry.addPermission(PSPermissions.RUNTIME_VISIBLE);
         acl.addEntry((PSAclEntryImpl) aclEntry);
      }
      svc.saveAcls(aclList);
      for (IPSAcl aclresult : aclList)
      {
         aclGuids.add(aclresult.getGUID());
      }
      return aclGuids;
   }

   /**
    * Verifies that the cache is working properly in regards to loading, saving
    * and such.
    */
   @Test
   @Ignore
   public void skipTestCache()
      throws Exception
   {
      createTestAcls();
      //load same object should come from cache
      IPSAclService aclService = PSAclServiceLocator.getAclService();
      IPSAcl acl1 = aclService.loadAclForObject(ms_templateGuid);
      assertTrue("Cache not working", acl1.equals(aclService.loadAcl(acl1
            .getGUID())));
      assertTrue("Cache not working", acl1.equals(aclService.loadAclsForObjects(
            Collections.singletonList(ms_templateGuid)).get(0)));
      // a load by aclId must come after a load by objectId because of
      // variations in implementation
      assertTrue("Cache not working", acl1.equals(aclService.loadAcls(Collections
            .singletonList(acl1.getGUID())).get(0)));

      //modifiable acl should not come from cache, but should not evict cache
      List<IPSAcl> modAcls = aclService.loadAclsModifiable(Collections
            .singletonList(acl1.getGUID()));
      IPSAcl modAcl = modAcls.get(0);
      assertFalse(acl1 == modAcl);
      assertTrue("Cache evicted on load read/write", 
            acl1.equals(aclService.loadAcl(acl1.getGUID())));
      assertFalse(modAcl.equals(aclService.loadAclsModifiable(Collections
            .singletonList(acl1.getGUID()))));

      //save should evict cache - test load by aclId
      modAcl.setObjectId(120000);
      aclService.saveAcls(modAcls);
      IPSAcl postModAcl = aclService.loadAcl(acl1.getGUID());
      assertFalse(acl1 == postModAcl);
      
      //save should evict cache - test load by objectId
      modAcl = aclService.loadAclsModifiable(
            Collections.singletonList(postModAcl.getGUID())).get(0);
      modAcl.setObjectId(120001);
      aclService.saveAcls(Collections.singletonList(modAcl));
      postModAcl = aclService.loadAcl(postModAcl.getGUID());
      assertFalse(modAcl == postModAcl);
      
      
      //attempting to save read-only object should evict cache
      IPSAcl readOnlyAcl = aclService.loadAclForObject(ms_templateGuid2);
      try
      {
         aclService.saveAcls(Collections.singletonList(readOnlyAcl));
         fail("Don't allow save of read-only acl");
      }
      catch (IllegalArgumentException success)
      {}
      assertFalse(readOnlyAcl == aclService.loadAclForObject(ms_templateGuid2));
      
      //delete should evict cache - test loading by objId
      readOnlyAcl = aclService.loadAclForObject(ms_slotTemplate);
      aclService.deleteAcl(readOnlyAcl.getGUID());
      assertNull(aclService.loadAclForObject(ms_slotTemplate));
      
      //delete should evict cache - test loading by aclId
      List<IPSAcl> testAcls = createTestAcls();
      IPSGuid testGuid = testAcls.get(0).getGUID();
      readOnlyAcl = aclService.loadAcl(testGuid);
      aclService.deleteAcl(readOnlyAcl.getGUID());
      assertNull(aclService.loadAclForObject(testGuid));
   }

   /**
    * Test saving changes to acls
    * 
    * @param aclList The currently persisted acls, assumed not 
    * <code>null</code>.
    * 
    * @return The list of persisted acls after testing save, never
    * <code>null</code>.
    * 
    * @throws Exception if the test fails
    */
   private List<IPSAcl> testSave(List<IPSAcl> aclList) throws Exception
   {
      // modify and add something
      List<IPSGuid> aclGuids = new ArrayList<IPSGuid>();
      for (IPSAcl acl : aclList)
      {
         PSAclImpl aclImpl = (PSAclImpl) acl;
         aclGuids.add(aclImpl.getGUID());
         aclImpl.setDescription("modified");
         for (IPSAclEntry entry : aclImpl.getEntries())
            entry.addPermission(PSPermissions.DELETE);

         PSAclEntryImpl newEntry = new PSAclEntryImpl();
         newEntry.setPrincipal(new PSTypedPrincipal("test1",
               PrincipalTypes.ROLE));
         newEntry.addPermission(PSPermissions.DELETE);
         aclImpl.addEntry(newEntry);
      }
      
      IPSAclService aclService = PSAclServiceLocator.getAclService();
      aclService.saveAcls(aclList);
      List<IPSAcl> loadList = aclService.loadAclsModifiable(aclGuids);
      assertEquals(aclList, loadList);
      aclList = loadList;

      // check basic roundtrip
      aclService.saveAcls(aclList);
      loadList = aclService.loadAclsModifiable(aclGuids);
      assertEquals(aclList, loadList);
      aclList = loadList;
      
      // remove a permission
      for (IPSAcl acl : aclList)
      {
         PSAclImpl aclImpl = (PSAclImpl) acl;
         for (IPSAclEntry entry : aclImpl.getEntries())
         {
            entry.removePermission(PSPermissions.DELETE);
            assertFalse(entry.checkPermission(PSPermissions.DELETE));
         }
      }
      
      aclService.saveAcls(aclList);
      loadList = aclService.loadAclsModifiable(aclGuids);
      assertEquals(aclList, loadList);
      aclList = loadList;

      for (IPSAcl acl : aclList)
      {
         PSAclImpl aclImpl = (PSAclImpl) acl;
         List<IPSAclEntry> entries = new ArrayList<IPSAclEntry>(
            aclImpl.getEntries());
         for (IPSAclEntry entry : entries)
         {
            if (!aclImpl.isOwner(entry.getPrincipal()))
            {
               aclImpl.removeEntry((PSAclEntryImpl)entry);
               assertTrue(aclImpl.findEntry(entry.getPrincipal()) == null);
            }
         }
      }
      
      aclService.saveAcls(aclList);
      loadList = aclService.loadAcls(aclGuids);
      assertEquals(aclList, loadList);
      
      return loadList;
   }

   /**
    * Tests deleting the supplied list of acls.
    * 
    * @param aclList The acls to delete, assumed not <code>null</code>.
    * 
    * @throws Exception if there are any errors.
    */
   public static void deleteAcls(List<IPSAcl> aclList) throws Exception
   {
      IPSAclService aclService = PSAclServiceLocator.getAclService();
      for (IPSAcl acl : aclList)
      {
         IPSGuid aclGuid = ((PSAclImpl)acl).getGUID();
         aclService.deleteAcl(aclGuid);
         
         try
         {
            aclService.loadAcl(aclGuid);
            fail("Should have thrown");
         }
         catch (PSSecurityException e)
         {
            // expected
         }
      }
   }

   /**
    * Creates and saves 3 test acls. The first is for a template 
    * ({@link #ms_templateGuid}) with the following entries:
    * <ul>
    * <li>admin1 (user) - OWNER, READ</li>
    * <li>Editor (role) - READ, UPDATE</li>
    * <li>AnyCommunity (community) - RUNTIME_VISIBLE</li>
    * <li>comm1 (community) - RUNTIME_VISIBLE</li>
    * <li>comm2 (community) - no visibility</li>
    * </ul>
    * The second is for a slot:
    * <ul>
    * <li>admin2 (user) - OWNER, READ</li>
    * <li>Admin (role) - READ, UPDATE</li>
    * <li>AnyCommunity (community) - RUNTIME_VISIBLE</li>
    * <li>comm1 (community) - RUNTIME_VISIBLE</li>
    * <li>comm2 (community) - RUNTIME_VISIBLE</li>
    * </ul>
    * The third is for another template ({@link #ms_templateGuid2}):
    * <ul>
    * <li>admin1 (user) - OWNER, READ</li>
    * <li>Editor (role) - READ, UPDATE</li>
    * <li>AnyCommunity (community) - no visibility</li>
    * <li>comm1 (community) - no visibility</li>
    * <li>comm2 (community) - RUNTIME_VISIBLE</li>
    * </ul>
    * 
    * @return The test acls, never <code>null</code> or empty.
    * 
    * @throws Exception If there are any errors.
    */
   public static List<IPSAcl> createTestAcls() throws Exception
   {
      IPSAclService aclService = PSAclServiceLocator.getAclService();
      List<IPSAcl> aclList = new ArrayList<IPSAcl>();

      //cleanup acls from previous tests
      List<IPSGuid> ids = new ArrayList<IPSGuid>();
      ids.add(ms_slotTemplate);
      ids.add(ms_templateGuid);
      ids.add(ms_templateGuid2);
      
      // because of the way the tests are setup, it is possible to get multiple
      // acls for a given object GUID, this loop will remedy that situation
      List<IPSAcl> toDelete = aclService.loadAclsForObjects(ids);
      boolean finishedCleanup = false;
      while (!finishedCleanup)
      {
         for (IPSAcl acl : toDelete)
         {
            if (acl != null)
               aclService.deleteAcl(acl.getGUID());
         }
         toDelete = aclService.loadAclsForObjects(ids);
         boolean found = false;
         for (IPSAcl acl : toDelete)
         {
            if (acl != null)
               found = true;
         }
         finishedCleanup = !found;
      }
      
      PSAclImpl acl; 
      acl = (PSAclImpl) aclService.createAcl(ms_templateGuid,
         new PSTypedPrincipal("admin1", PrincipalTypes.USER));
      acl.setName(TEST_ACL_NAME);
      aclList.add(acl);
      
      IPSAclEntry aclEntry;
      aclEntry = acl.getEntries().iterator().next();
      aclEntry.addPermission(PSPermissions.READ);

      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal("Editor",
            PrincipalTypes.ROLE));
      aclEntry.addPermission(PSPermissions.UPDATE);
      aclEntry.addPermission(PSPermissions.READ);
      acl.addEntry((PSAclEntryImpl) aclEntry);

      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal(
            PSTypedPrincipal.ANY_COMMUNITY_ENTRY, PrincipalTypes.COMMUNITY));
      aclEntry.addPermission(PSPermissions.RUNTIME_VISIBLE);
      acl.addEntry((PSAclEntryImpl) aclEntry);

      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal("comm1",
            PrincipalTypes.COMMUNITY));
      aclEntry.addPermission(PSPermissions.RUNTIME_VISIBLE);
      acl.addEntry((PSAclEntryImpl) aclEntry);

      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal("comm2",
            PrincipalTypes.COMMUNITY));
      acl.addEntry((PSAclEntryImpl) aclEntry);

      // acl 2
      acl = (PSAclImpl) aclService.createAcl(ms_slotTemplate,
            new PSTypedPrincipal("admin2", PrincipalTypes.USER));
      acl.setName(TEST_ACL_NAME);
      aclList.add(acl);

      aclEntry = acl.getEntries().iterator().next();
      aclEntry.addPermission(PSPermissions.READ);

      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal("Admin",
            PrincipalTypes.ROLE));
      aclEntry.addPermission(PSPermissions.UPDATE);
      aclEntry.addPermission(PSPermissions.READ);
      acl.addEntry((PSAclEntryImpl) aclEntry);

      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal(
            PSTypedPrincipal.ANY_COMMUNITY_ENTRY, PrincipalTypes.COMMUNITY));
      aclEntry.addPermission(PSPermissions.RUNTIME_VISIBLE);
      acl.addEntry((PSAclEntryImpl) aclEntry);

      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal("comm1",
            PrincipalTypes.COMMUNITY));
      aclEntry.addPermission(PSPermissions.RUNTIME_VISIBLE);
      acl.addEntry((PSAclEntryImpl) aclEntry);

      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal("comm2",
            PrincipalTypes.COMMUNITY));
      aclEntry.addPermission(PSPermissions.RUNTIME_VISIBLE);
      acl.addEntry((PSAclEntryImpl) aclEntry);

      // acl 3
      acl = (PSAclImpl) aclService.createAcl(ms_templateGuid2,
            new PSTypedPrincipal("admin1", PrincipalTypes.USER));
      acl.setName(TEST_ACL_NAME);
      aclList.add(acl);

      aclEntry = acl.getEntries().iterator().next();
      aclEntry.addPermission(PSPermissions.READ);

      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal("Editor",
            PrincipalTypes.ROLE));
      aclEntry.addPermission(PSPermissions.UPDATE);
      aclEntry.addPermission(PSPermissions.READ);
      acl.addEntry((PSAclEntryImpl) aclEntry);

      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal(
            PSTypedPrincipal.ANY_COMMUNITY_ENTRY, PrincipalTypes.COMMUNITY));
      acl.addEntry((PSAclEntryImpl) aclEntry);

      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal("comm1",
            PrincipalTypes.COMMUNITY));
      acl.addEntry((PSAclEntryImpl) aclEntry);

      aclEntry = new PSAclEntryImpl(new PSTypedPrincipal("comm2",
            PrincipalTypes.COMMUNITY));
      aclEntry.addPermission(PSPermissions.RUNTIME_VISIBLE);
      acl.addEntry((PSAclEntryImpl) aclEntry);
      
      aclService.saveAcls(aclList);
      
      return aclList;
   }
   
   
   /**
    * Test loading the acls
    * 
    * @param aclList The list of acls to expect, assumed not <code>null</code>.
    * @return The loaded acls, never <code>null</code>. These are modifiable
    * and may be saved.
    * @throws Exception if the test fails.
    */
   private List<IPSAcl> testLoadMethods(List<IPSAcl> aclList) throws Exception
   {
      IPSAclService aclService = PSAclServiceLocator.getAclService();
      List<IPSGuid> aclGuids = new ArrayList<IPSGuid>();
      List<IPSGuid> objectGuids = new ArrayList<IPSGuid>();
      for (IPSAcl acl : aclList)
      {
         PSAclImpl aclImpl = (PSAclImpl)acl; 
         IPSGuid guid = aclImpl.getGUID();
         IPSGuid objGuid = new PSGuid(PSTypeEnum.valueOf(
            aclImpl.getObjectType()), aclImpl.getObjectId());
         IPSAcl idtest = aclService.loadAcl(guid);
         IPSAcl objtest = aclService.loadAclForObject(objGuid);
         assertTrue(idtest.equals(objtest));
         
         aclGuids.add(guid);
         objectGuids.add(objGuid);
      }
      List<IPSAcl> loadList = aclService.loadAclsModifiable(aclGuids);
      assertEquals(aclList, loadList);
      for (IPSAcl acl : loadList)
      {
         PSAclImpl aclImpl = (PSAclImpl)acl; 
         assertEquals(acl, aclService.loadAcl(
            aclImpl.getGUID()));
      }
      
      assertEquals(loadList.size(), 
         aclService.loadAclsForObjects(objectGuids).size());
      
      return loadList;
   }
   
   //clean all test acls before starting test
   static
   {
      IPSAclService svc = PSAclServiceLocator.getAclService();
      try
      {
         List<IPSAcl> acls = svc.loadAcls(null);
         for (IPSAcl acl : acls)
         {
            if (acl.getName().equalsIgnoreCase(TEST_ACL_NAME))
               svc.deleteAcl(acl.getGUID());
         }
      }
      catch (Exception e)
      {
         System.out.println("Failed while cleaning up test acls.");
         e.printStackTrace();
      }
   }
}
