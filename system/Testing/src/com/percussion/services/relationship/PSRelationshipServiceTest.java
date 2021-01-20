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
package com.percussion.services.relationship;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.PSRelationshipData;
import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSProperty;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.error.PSException;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.PSStopwatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test managing relationships in the persistent layer. 
 */
@Category(IntegrationTest.class)
public class PSRelationshipServiceTest
{
   private static IPSRelationshipService ms_svc = PSRelationshipServiceLocator
      .getRelationshipService();

   public PSRelationshipServiceTest() {
   }

   @Test
   public void testCrossSiteLinks() throws Exception
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setLimitToCrossSiteLinks(true);
      List<Integer> ids = new ArrayList<Integer>();
      ids.add(new Integer(504));
      ids.add(new Integer(489));
      ids.add(new Integer(490));
      filter.setDependentIds(ids);
      
      List<PSRelationship> rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 2);
      assertTrue(rels.get(0).getDependent().getId() == 504);
      assertTrue(rels.get(1).getDependent().getId() == 504);
   }

   @Test
   public void testPerformance() throws Exception
   {
      int[] ownerIds = new int[] {313, 2, 1, 313};
      
      for (int ownerId : ownerIds)
      {
         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setOwner(new PSLocator(ownerId, -1));

         PSStopwatch watch = new PSStopwatch();
         watch.start();
         List<PSRelationship> rels = ms_svc.findByFilter(filter);
         watch.stop();
         System.out.println("HQL elapse = " + watch.toString()
               + " with rows = " + rels.size());

         /*
         watch.start();
         rels2 = ms_svc.findByCriteria(ownerId);
         watch.stop();
         System.out.println("Criteria elapse = " + watch.toString()
               + " with rows = " + rels2.size());

         watch.start();
         rels2 = ms_svc.findByJDBC(ownerId);
         watch.stop();
         System.out.println("JDBC elapse = " + watch.toString()
               + " with rows = " + rels2.size());
         */
      }
      
   }
   
   /**
    * Test services that manages {@link PSRelationshipData} object.
    * It primarily tests the join for
    * {@link IPSConstants#PSX_RELATIONSHIPS}.owner_id = CONTENTSTATUS.contentid. 
    * 
    * @throws Exception 
    */
   @Test
   public void testRelationshipDataWithOwnerJoin() throws Exception
   {
      // Test find persisted rids
      ArrayList<Integer> ids = new ArrayList<Integer>();
      ids.add(new Integer(1));
      ids.add(new Integer(-1));
      List<Integer>ids_2 = ms_svc.findPersistedRid(ids);
      assertTrue(ids_2.size() == 1);
      
      // load the FOLDER relationship with owner as the root folder. 
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(new PSLocator(1, -1));
      List<PSRelationship> rels = ms_svc.findByFilter(filter);
      // should have 2 child folder
      assertTrue(rels.size() == 2);

      // filter by the owner content type id
      filter = new PSRelationshipFilter();
      filter.setOwner(new PSLocator(1, -1));
      filter.setOwnerContentTypeId(101); // folder content type id
      List<PSRelationship> rels_2 = ms_svc.findByFilter(filter);
      // should be the same as above
      assertTrue(rels_2.size() == rels.size());
      
      try
      {
         // cannot set content type id for both owner dependent
         filter.setDependentContentTypeId(101);
      }
      catch (IllegalStateException ie)
      {
      }
      
      // query by both owner id and relationship name
      filter = new PSRelationshipFilter();
      filter.setOwner(new PSLocator(1, -1));
      filter.setName(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      rels_2 = ms_svc.findByFilter(filter);
      assertTrue(rels_2.size() == rels.size());
      assertTrue(rels_2.containsAll(rels));

      // same query as above except the name contains space character.
      // should get the same result
      filter.setName("Folder Content");
      rels_2 = ms_svc.findByFilter(filter);
      assertTrue(rels_2.size() == rels.size());
      assertTrue(rels_2.containsAll(rels));
      
      
      // query by owner rev
      filter = new PSRelationshipFilter();
      filter.setOwner(new PSLocator(1, -1));
      filter.limitToOwnerRevision(true);
      rels_2 = ms_svc.findByFilter(filter);
      assertTrue(rels_2.size() == rels.size());
      assertTrue(rels_2.containsAll(rels));
      
      // query by dependent
      filter = new PSRelationshipFilter();
      filter.setDependent(new PSLocator(2, -1));
      rels_2 = ms_svc.findByFilter(filter);
      assertTrue(rels_2.size() == 1);
      assertTrue(rels.containsAll(rels_2));
      
      // query by more than one dependent. 
      // id 2 & 3 should only have one parent -> 1
      filter = new PSRelationshipFilter();
      List<PSLocator> dependents = new ArrayList<PSLocator>();
      dependents.add(new PSLocator(2, -1));
      dependents.add(new PSLocator(3, -1));
      filter.setDependents(dependents);
      rels_2 = ms_svc.findByFilter(filter);
      assertTrue(rels_2.size() == 2);
      assertTrue(rels_2.get(0).getOwner().equals(rels_2.get(1).getOwner()));
            
      // query by more than one dependent ID. 
      // id 2 & 3 should only have one parent -> 1
      filter = new PSRelationshipFilter();
      filter.setDependents(dependents);
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.equals(rels_2));
      
      // query with additional FAKE id, should get the same result as above
      dependents.add(new PSLocator(98888, -1)); 
      filter.setDependents(dependents);
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.equals(rels_2));
      
      // query by relationship names
      filter = new PSRelationshipFilter();
      filter.setName(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      int numFolderRels = ms_svc.findByFilter(filter).size();
      filter.setName(PSRelationshipConfig.TYPE_NEW_COPY);
      int numNewCopyRels = ms_svc.findByFilter(filter).size();
      
      Collection<String> rnames = new ArrayList<String>();
      rnames.add(PSRelationshipConfig.TYPE_FOLDER_CONTENT);
      rnames.add(PSRelationshipConfig.TYPE_NEW_COPY);
      filter.setNames(rnames);
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == (numFolderRels + numNewCopyRels));

      // query by relationship category. Since there is no translation
      // relationships in the FastForward, so the result should be the same.
      filter.setCategory(PSRelationshipConfig.CATEGORY_TRANSLATION);
      rels_2 = ms_svc.findByFilter(filter);
      assertTrue(rels_2.size() == rels.size());
      
      // add filtering by relationship "user" type
      // but there is no "user" type, so the result should be the same
      filter.setType(PSRelationshipFilter.FILTER_TYPE_USER);
      rels_2 = ms_svc.findByFilter(filter);
      assertTrue(rels_2.size() == rels.size());
      
      // query by relationship "system" type
      filter = new PSRelationshipFilter();
      filter.setType(PSRelationshipFilter.FILTER_TYPE_SYSTEM);
      rels_2 = ms_svc.findByFilter(filter);
      assertTrue(rels_2.size() > 0);

      // query by "user" type, the result should be empty
      // since there is no "user" type in FastForward
      filter = new PSRelationshipFilter();
      filter.setType(PSRelationshipFilter.FILTER_TYPE_USER);
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 0);
      
      int FAKE_ID;
      
      // test add and remove single entity
      
      // create a FAKE FOLDER PSRelationshipData
      PSRelationship rdata = new PSRelationship(-1, new PSLocator(98888, -1),
         new PSLocator(97777, -1),
         getRelationshipConfig(PSRelationshipConfig.TYPE_FOLDER_CONTENT));
      ms_svc.saveRelationship(rdata);

      FAKE_ID = rdata.getId();
      
      // load by relationship id
      PSRelationship rdata1 = ms_svc.loadRelationship(FAKE_ID);
      assertTrue(rdata.equals(rdata1));
      
      filter = new PSRelationshipFilter();
      filter.setRelationshipId(FAKE_ID);
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 1);
      rdata1 = rels.iterator().next();
      assertTrue(rdata.equals(rdata1));

      // load by id, but with wrong relationship name
      filter = new PSRelationshipFilter();
      filter.setRelationshipId(FAKE_ID);
      filter.setName(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 0);

      // remove the data
      ms_svc.deleteRelationship(rdata);
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels == null || rels.size() == 0);
      
      
      // query and filter by the owner tip (edit or current) revision
      filter = new PSRelationshipFilter();
      filter.setDependent(new PSLocator(633, 1));
      filter.setName(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() > 1);
      
      filter.limitToEditOrCurrentOwnerRevision(true);
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 1);
      
      // test filter by owner public revision, should be the same as above
      // because the owner's (551) public-revision == current-revision 
      filter.limitToEditOrCurrentOwnerRevision(false);
      filter.limitToPublicOwnerRevision(true);
      rels_2 = ms_svc.findByFilter(filter);
      assertTrue(rels_2.size() == 1);
      assertTrue(rels_2.get(0).equals(rels.get(0)));
      
      // cannot join both owner_id and dependent_id (from 
      // IPSConstants.PSX_RELATIONSHIPS table) with COTNENTSTATUS.contentid
      Exception e = null;
      try
      {
         filter.setDependentContentTypeId(311);
      }
      catch (IllegalStateException ie)
      {
         e = ie;
      }
      assertTrue(e instanceof IllegalStateException);

      // cannot join both owner_id and dependent_id (from 
      // IPSConstants.PSX_RELATIONSHIPS table) with COTNENTSTATUS.contentid
      try
      {
         filter.setDependentObjectType(PSComponentSummary.TYPE_FOLDER);
      }
      catch (IllegalStateException ie)
      {
         e = ie;
      }
      assertTrue(e instanceof IllegalStateException);
      
      // additionally filtered by the owner content type id
      filter.setOwnerContentTypeId(312); // Home content type id
      rels_2 = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == rels_2.size());

      // additionally filtered by the owner object type
      filter.setOwnerObjectType(PSComponentSummary.TYPE_FOLDER);
      rels_2 = ms_svc.findByFilter(filter);
      assertTrue(rels_2.size() == 0);
      filter.setOwnerObjectType(-1);  // reset the object type

      try // cannot set object type for both owner dependent
      { filter.setDependentObjectType(PSComponentSummary.TYPE_FOLDER);}
      catch (IllegalStateException ie){}
      
      filter.setOwnerContentTypeId(307); // Image content type id
      rels_2 = ms_svc.findByFilter(filter);
      assertTrue(rels_2.size() == 0);
   }


   /**
    * Tests finding relationships by relationship names.
    * 
    * @throws Exception for any errors.
    */
   @Test
   public void testFindByConfigName() throws Exception
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setName("BugasName");
      List<PSRelationship> rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 0);
   }
   
   
   /**
    * Tests updating existing relationships.
    * 
    * @throws Exception if any error occurs.
    */
   @Test
   public void testUpdateExistingRelationship() throws Exception
   {
      // load existing Active Assembly relationships
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(new PSLocator(335, -1));
      filter.setName(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY);
      List<PSRelationship> rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() > 0);
      
      // validate sys_siteid=null and sys_folderid=null
      for (PSRelationship rel : rels)
      {
         assertTrue(rel.getProperty(PSRelationshipConfig.PDU_FOLDERID) == null);
         assertTrue(rel.getProperty(PSRelationshipConfig.PDU_SITEID) == null);
         
         // reset to BAD string for integer
         rel.setProperty(PSRelationshipConfig.PDU_FOLDERID, "abc");
         rel.setProperty(PSRelationshipConfig.PDU_SITEID, "abc");
      }
      
      // update exiting relationships
      ms_svc.saveRelationship(rels);
      
      // retrieves the same set of relationships
      rels = ms_svc.findByFilter(filter);
      
      // validate sys_siteid=null and sys_folderid=null
      for (PSRelationship rel : rels)
      {
         assertTrue(rel.getProperty(PSRelationshipConfig.PDU_FOLDERID) == null);
         assertTrue(rel.getProperty(PSRelationshipConfig.PDU_SITEID) == null);
      }
   }
   
   /**
    * Test save relationship with pre-defined and unknown (-1) relationship ids 
    * 
    * @throws Exception 
    */
   @Test
   public void testSaveToRepository() throws Exception
   {
      int FAKE_ID = 1900000000;
      int FAKE_OWNER_ID = FAKE_ID + 1;
      int FAKE_SLOT_ID = FAKE_ID + 2;
      
      // clean up 
      ms_svc.deleteRelationshipByRid(FAKE_ID);
      
      PSLocator owner = new PSLocator(FAKE_OWNER_ID, 1);
      
      // save with a pre-set relationship id
      PSRelationship rel = new PSRelationship(FAKE_ID, owner, new PSLocator(2, -1),
            getRelationshipConfig(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY));
      rel.setPersisted(false);
      rel.setProperty(PSRelationshipConfig.PDU_WIDGET_NAME, "WidgetName-" + FAKE_ID);
      rel.setProperty(PSRelationshipConfig.PDU_FOLDERID, "123");
      rel.setProperty(PSRelationshipConfig.PDU_SITEID, "13");
      rel.setProperty(PSRelationshipConfig.PDU_VARIANTID, String.valueOf(FAKE_SLOT_ID));
      rel.setProperty(PSRelationshipConfig.PDU_INLINERELATIONSHIP, "body");
      
      ms_svc.saveRelationship(rel);
      PSRelationship rel_2 = ms_svc.loadRelationship(FAKE_ID);
      
      assertTrue(rel.equals(rel_2));
      
      // retrieve relationship by relationship filter
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(owner);
      filter.limitToOwnerRevision(true);
      List<PSRelationship> rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 1);
      rel_2 = rels.get(0);
      assertTrue(rel.equals(rel_2));
      
      // remove the above
      ms_svc.deleteRelationshipByRid(FAKE_ID);
      rel = ms_svc.loadRelationship(FAKE_ID);
      assertTrue(rel == null);
      
      // save a unknown relationship id
      rel = new PSRelationship(-1, 
            new PSLocator(1, 1), new PSLocator(2, -1),
            getRelationshipConfig(PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY));
      assertFalse(rel.isPersisted());
      ms_svc.saveRelationship(rel);
      
      FAKE_ID = rel.getId();
      rel = ms_svc.loadRelationship(FAKE_ID);
      assertTrue(rel != null);
      
      // remove the above
      ms_svc.deleteRelationshipByRid(FAKE_ID);
      rel = ms_svc.loadRelationship(FAKE_ID);
      assertTrue(rel == null);
   }


   /**
    * Test services that manages {@link PSRelationshipData} object.
    * It primarily tests the join for
    * {@link IPSConstants#PSX_RELATIONSHIPS}.dependent_id = 
    *    CONTENTSTATUS.contentid. 
    * 
    * @throws Exception 
    */
   @Test
   public void testRelationshipDataWithDependentJoin() throws Exception
   {
      // filter by the dependent content type id
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(new PSLocator(1, -1));
      filter.setDependentContentTypeId(101); // folder content type id
      Collection<PSRelationship> rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 2);

      filter = new PSRelationshipFilter();
      filter.setOwner(new PSLocator(1, -1));
      filter.setDependentContentTypeId(311); // there is no such dependent type
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 0);
      
      // filter by more than one dependent content type ids
      filter = new PSRelationshipFilter();
      filter.setOwner(new PSLocator(1, -1));
      List<Long> ctIds = new ArrayList<Long>();
      ctIds.add(new Long(101));
      ctIds.add(new Long(311));
      filter.setDependentContentTypeIds(ctIds);
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 2);

      // filter by the dependent object type
      filter = new PSRelationshipFilter();
      filter.setOwner(new PSLocator(1, -1));
      filter.setDependentObjectType(PSComponentSummary.TYPE_FOLDER);
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 2);
      
      filter.setDependentObjectType(PSComponentSummary.TYPE_ITEM);
      rels = ms_svc.findByFilter(filter); // there is no such dependent item
      assertTrue(rels.size() == 0);
   }

   /**
    * Get relationship config from a given config name.
    * 
    * @param configName the name of the relationship configuration, assumed
    *   not <code>null</code>.
    *   
    * @return the relationship config object, never <code>null</code>.
    * 
    * @throws PSMissingBeanConfigurationException
    * @throws PSException
    */
   private PSRelationshipConfig getRelationshipConfig(String configName)
         throws PSMissingBeanConfigurationException, PSException
   {
      // make sure the configs are loaded
      PSRelationshipCommandHandler.loadConfigs();  

      PSRelationshipConfig config = 
         PSRelationshipCommandHandler.getRelationshipConfig(configName);

      if (config == null)
         throw new RuntimeException("Cannot find config name = " + configName);
      else
         return config;
   }
   /**
    * Testing query the relationships which contains pre-defined user properties
    * such as Active Assembly Relationships.
    * 
    * @throws Exception if an error occurs.
    */
   @Test
   public void testPreDefinedUserProperties() throws Exception
   {
      final int OWNER_ID = 664;     // existing item in FastForward
      final int DEPENDENT_ID = 367; // existing item in FastForward

      PSLocator owner = new PSLocator(OWNER_ID, 1);
      PSLocator dependent = new PSLocator(DEPENDENT_ID, 1);
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(owner);
      filter.limitToOwnerRevision(true);
      filter.setDependent(dependent);
      filter.setProperty(PSRelationshipConfig.PDU_SITEID, "301");
      filter.setProperty(PSRelationshipConfig.PDU_FOLDERID, "314");
      filter.setProperty(PSRelationshipConfig.PDU_SLOTID, "509");
      filter.setProperty(PSRelationshipConfig.PDU_VARIANTID, "504");
      filter.setProperty(PSRelationshipConfig.PDU_SORTRANK, "1");
      
      Collection<PSRelationship>rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 1);

      // less efficient if set limitToOwnerRevision(false), but should get
      // the same result because the AA relationship uses owner revision
      filter.limitToOwnerRevision(false);
      Collection<PSRelationship>rels_2 = ms_svc.findByFilter(filter);
      assertTrue(rels_2.size() == rels.size());
      
      // unknown site
      filter = new PSRelationshipFilter();
      filter.setOwner(owner);
      filter.setProperty(PSRelationshipConfig.PDU_SITEID, "9999");
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 0);

      // unknown inline link property
      filter = new PSRelationshipFilter();
      filter.setOwner(owner);
      filter.setProperty(PSRelationshipConfig.PDU_INLINERELATIONSHIP, 
         "unknown");
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 0);
   }
   
   /**
    * Testing manage the relationships which contains additional CUSTOM 
    * properties
    * 
    * @throws Exception if an error occurs.
    */
   @Test
   public void testCustomProperties() throws Exception
   {
      int FAKE_RID;
      int FAKE_RID2; 
      final int FAKE_OWNER_ID = 320;     // existing item in FastForward
      final int FAKE_DEPENDENT_ID = 321; // existing item in FastForward

      // Modify "NewCopy" with additional user properties
      PSRelationshipConfig newcopy = addUserRelationship(
            PSRelationshipConfig.TYPE_NEW_COPY, 2);      
      
      // Pre-cleanup
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setProperty(USER_PROP_NAME_1, "v1");
      Collection<PSRelationship>rels = ms_svc.findByFilter(filter);
      if (!rels.isEmpty()) ms_svc.deleteRelationship(rels);
      
      filter = new PSRelationshipFilter();
      filter.setProperty(USER_PROP_NAME_2, "v2");
      rels = ms_svc.findByFilter(filter);
      if (!rels.isEmpty()) ms_svc.deleteRelationship(rels);

      // make up a relationship with the extra properties
      PSRelationship rdata1 = new PSRelationship(-1,
            new PSLocator(FAKE_OWNER_ID, 1), 
            new PSLocator(FAKE_DEPENDENT_ID, 1),
            newcopy);
      rdata1.setProperty(USER_PROP_NAME_1, "v1");
      rdata1.setProperty(USER_PROP_NAME_2, null); // test null value
      
      ms_svc.saveRelationship(rdata1);
      FAKE_RID = rdata1.getId();
      
      filter = new PSRelationshipFilter();
      filter.setRelationshipId(FAKE_RID);
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 1);
      
      PSRelationship rdata = rels.iterator().next();
      // exclude USER_PROP_NAME_2
      assertTrue(rdata.getUserProperties().size() == 1);
      assertTrue(rdata.equals(rdata1));
      
      rdata = ms_svc.loadRelationship(FAKE_RID);
      assertTrue(rdata.equals(rdata1));

      // test to set non-null value to user properties
      rdata.setProperty(USER_PROP_NAME_2, "v2");
      ms_svc.saveRelationship(rdata);
      rdata = ms_svc.loadRelationship(FAKE_RID);
      // include both USER_PROP_NAME_1 & 2
      assertTrue(rdata.getUserProperties().size() == 2);
      assertFalse(rdata.equals(rdata1));
      
      // filter by custom properties
      filter = new PSRelationshipFilter();
      filter.setProperty(USER_PROP_NAME_1, "v1");
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 1);

      // filter by custom properties and join owner id
      filter = new PSRelationshipFilter();
      filter.limitToEditOrCurrentOwnerRevision(true);
      filter.setDependent(new PSLocator(FAKE_DEPENDENT_ID, 1));
      filter.setProperty(USER_PROP_NAME_1, "v1");
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 1);

      // filter by custom properties and join owner id
      filter = new PSRelationshipFilter();
      filter.limitToEditOrCurrentOwnerRevision(true);
      filter.setOwnerObjectType(PSComponentSummary.TYPE_ITEM);
      filter.setProperty(USER_PROP_NAME_1, "v1");
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 1);

      // filter by custom properties and join dependent id
      filter = new PSRelationshipFilter();
      filter.setDependentObjectType(PSComponentSummary.TYPE_ITEM);
      filter.setProperty(USER_PROP_NAME_1, "v1");
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 1);

      // filter by unknown custom properties
      filter = new PSRelationshipFilter();
      filter.setProperty(USER_PROP_NAME_1, "UNKNOWN v1");
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 0);
      
      filter = new PSRelationshipFilter();
      filter.setProperty(USER_PROP_NAME_1, "v1");
      filter.setProperty(USER_PROP_NAME_2, "v2");
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 1);
      
      filter = new PSRelationshipFilter();
      filter.setProperty(USER_PROP_NAME_1, "v1");
      filter.setProperty(USER_PROP_NAME_2, "UNKNOWN_v2");
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 0);

      //\/\/\/\//\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
      // Testing remove custom (user) properties, 
      // remove USER_PROP_NAME_2, but keep USER_PROP_NAME_1
      //\/\/\/\//\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
      
      // modify relationship config
      newcopy = addUserRelationship(PSRelationshipConfig.TYPE_NEW_COPY, 1);

      // make up a relationship with the extra properties
      PSRelationship rdata2 = new PSRelationship(-1,
            new PSLocator(FAKE_OWNER_ID, 1), 
            new PSLocator(FAKE_DEPENDENT_ID, 1),
            newcopy);
      rdata2.setProperty(USER_PROP_NAME_1, "v1");
      ms_svc.saveRelationship(rdata2);
      FAKE_RID2 = rdata2.getId();
      
      // retrieve the relationship data
      filter = new PSRelationshipFilter();
      filter.setRelationshipId(FAKE_RID2);
      rels = ms_svc.findByFilter(filter);
      assertTrue(rels.size() == 1);
      
      rdata1 = rels.iterator().next();
      // should only have one user property
      assertTrue(rdata2.getUserProperties().size() == 1); 
      assertTrue(rdata2.equals(rdata1));
      assertFalse(rdata2.equals(rdata));

      // Post cleanup
      ms_svc.deleteRelationship(rdata);
      ms_svc.deleteRelationship(rdata2);
      newcopy.setUserDefProperties(Collections.EMPTY_LIST.iterator());
   }
   
   /**
    * Add user defined properties to the NewCopy relationship.
    * 
    * @throws PSException if failed to load the relationship configurations
    */
   private PSRelationshipConfig addUserRelationship(String configName,
         int numProps) throws PSException
   {
      // make sure the configs are loaded
      PSRelationshipCommandHandler.loadConfigs();  

      // add a couple of user properties to the "NewCopy" relationship
      PSRelationshipConfig newcopy = PSRelationshipCommandHandler
            .getRelationshipConfig(configName);
      
      Collection<PSProperty> userProps = new ArrayList<PSProperty>();
      userProps.add(new PSProperty(USER_PROP_NAME_1));
      if (numProps == 2)
         userProps.add(new PSProperty(USER_PROP_NAME_2));
      newcopy.setUserDefProperties(userProps.iterator());
      
      return newcopy;
   }
   
   /**
    * Tests implicitely set the limitToOwnerRev flag if the 'useOwnerRevision'
    * flag is <code>true</code> for all requested relationship configs.
    * 
    * @throws Exception for any errors.
    */
   @Test
   public void testImpliciteLimitToOwnerRev() throws Exception
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
      PSComponentSummary summ = getItemSummary(335);
      filter.setOwner(summ.getHeadLocator());
      List<PSRelationship> rels = ms_svc.findByFilter(filter);
      
      filter.limitToOwnerRevision(true);
      List<PSRelationship> rels2 = ms_svc.findByFilter(filter);
      // the size of implicite query == explicite one
      assertTrue(rels.size() == rels2.size());
      
      if (summ.getHeadLocator().getRevision() > 1)
      {
         // make a query without owner revision should get relationships
         // for all owner revisions
         filter = new PSRelationshipFilter();
         filter.setCategory(PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
         filter.setOwner(new PSLocator(summ.getContentId(), -1));
         rels2 = ms_svc.findByFilter(filter);
         
         assertTrue(rels.size() < rels2.size());
      }
   }

   private PSComponentSummary getItemSummary(int cid)
      throws Exception
   {
      IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary summary = objMgr.loadComponentSummary(cid);
      return summary;
   }

   /**
    * A list of user property names, used by the test
    */
   private static final String USER_PROP_NAME_1 = "NewCopyProperty_1";
   private static final String USER_PROP_NAME_2 = "NewCopyProperty_2";
}
