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
package com.percussion.webservices.rhythmyx;

import com.percussion.cms.objectstore.PSFolder;
import com.percussion.design.objectstore.PSConditionalEffect;
import com.percussion.design.objectstore.PSRelationshipConfig.SysConfigEnum;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSSystemTestBase;
import com.percussion.webservices.PSTestUtils;
import com.percussion.webservices.common.ObjectType;
import com.percussion.webservices.common.Reference;
import com.percussion.webservices.common.RelationshipFilterRelationshipType;
import com.percussion.webservices.content.ContentSOAPStub;
import com.percussion.webservices.content.PSContentType;
import com.percussion.webservices.content.PSItem;
import com.percussion.webservices.content.PSItemStatus;
import com.percussion.webservices.content.PSRevisions;
import com.percussion.webservices.content.ReleaseFromEditRequest;
import com.percussion.webservices.faults.PSContractViolationFault;
import com.percussion.webservices.faults.PSInvalidLocaleFault;
import com.percussion.webservices.faults.PSInvalidSessionFault;
import com.percussion.webservices.faults.PSNotAuthorizedFault;
import com.percussion.webservices.faults.PSUnknownContentTypeFault;
import com.percussion.webservices.faults.PSUserNotMemberOfCommunityFault;
import com.percussion.webservices.security.data.PSCommunity;
import com.percussion.webservices.security.data.PSLocale;
import com.percussion.webservices.security.data.PSLogin;
import com.percussion.webservices.system.CreateRelationshipRequest;
import com.percussion.webservices.system.CreateRelationshipResponse;
import com.percussion.webservices.system.FindDependentsRequest;
import com.percussion.webservices.system.FindOwnersRequest;
import com.percussion.webservices.system.GetAllowedTransitionsResponse;
import com.percussion.webservices.system.LoadRelationshipTypesRequest;
import com.percussion.webservices.system.LoadWorkflowsRequest;
import com.percussion.webservices.system.PSAclImpl;
import com.percussion.webservices.system.PSAgingTransition;
import com.percussion.webservices.system.PSAudit;
import com.percussion.webservices.system.PSAuditTrail;
import com.percussion.webservices.system.PSNotification;
import com.percussion.webservices.system.PSRelationship;
import com.percussion.webservices.system.PSRelationshipConfig;
import com.percussion.webservices.system.PSRelationshipFilter;
import com.percussion.webservices.system.PSRelationshipFilterCategory;
import com.percussion.webservices.system.PSState;
import com.percussion.webservices.system.PSTransition;
import com.percussion.webservices.system.PSWorkflow;
import com.percussion.webservices.system.RelationshipCategory;
import com.percussion.webservices.system.RelationshipConfigSummary;
import com.percussion.webservices.system.RelationshipConfigSummaryType;
import com.percussion.webservices.system.SwitchCommunityRequest;
import com.percussion.webservices.system.SwitchLocaleRequest;
import com.percussion.webservices.system.SystemSOAPStub;
import com.percussion.webservices.system.TransitionItemsRequest;
import com.percussion.webservices.systemdesign.SystemDesignSOAPStub;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.AssertionFailedError;

import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

/**
 * Test all system soap services
 */
@Category(IntegrationTest.class)
public class SystemTestCase extends PSSystemTestBase
{
   /**
    * Tests transition items operation
    */
   @Test
   public void testTransitionItems() throws Exception
   {
      String session = m_session;

      SystemSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, session);

      ContentSOAPStub contentBinding = getContentSOAPStub(null);
      PSTestUtils.setSessionHeader(contentBinding, session);

      PSContentType contentType = null;
      List<PSAclImpl> acls = new ArrayList<PSAclImpl>();
      List<PSItem> items = new ArrayList<PSItem>();

      ContentTestCase contentTest = new ContentTestCase(m_session, m_login);
      try
      {
         // try to cleanup the content type that may left from previous tests
         contentTest.cleanUpContentType(session, "test3");
         
         // create the test content types
         contentType = contentTest.createContentType("test3", session, acls);

         // create one item for the new content type
         items.addAll(contentTest.createTestItems(contentType.getName(), 1,
            true, true, false, null, session, contentBinding));

         // make sure we have a directToPublic transition
         long[] itemIds = contentTest.toItemIds(items);
         GetAllowedTransitionsResponse allowedTransitions = binding
            .getAllowedTransitions(itemIds);
         String directToPublic = null;
         for (String trigger : allowedTransitions.getTransition())
         {
            if (trigger.equals("DirecttoPublic"))
            {
               directToPublic = trigger;
               break;
            }
         }
         assertTrue(directToPublic != null);

         TransitionItemsRequest request = null;

         // try with invalid ids
         try
         {
            request = new TransitionItemsRequest();
            request.setId(null);
            request.setTransition(directToPublic);
            binding.transitionItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid ids
         try
         {
            request = new TransitionItemsRequest();
            request.setId(new long[0]);
            request.setTransition(directToPublic);
            binding.transitionItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid transition
         try
         {
            request = new TransitionItemsRequest();
            request.setId(itemIds);
            request.setTransition(null);
            binding.transitionItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (RemoteException e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid transition
         try
         {
            request = new TransitionItemsRequest();
            request.setId(itemIds);
            request.setTransition(" ");
            binding.transitionItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected exception
            assertTrue(true);
         }

         // try with invalid transition
         try
         {
            request = new TransitionItemsRequest();
            request.setId(itemIds);
            request.setTransition("sometransition");
            binding.transitionItems(request);
            assertFalse("Should have thrown exception", false);
         }
         catch (RemoteException e)
         {
            // expected exception
            assertTrue(true);
         }

         // try to transition item that is CHECKED OUT by the same user
         request = new TransitionItemsRequest();
         request.setId(itemIds);
         request.setTransition(directToPublic);
         String[] states = binding.transitionItems(request).getStates();
         assertTrue(states.length == 1 && states[0].equals("Public"));

         // make sure we have an Expire transition
         allowedTransitions = binding.getAllowedTransitions(itemIds);
         String expire = null;
         for (String trigger : allowedTransitions.getTransition())
         {
            if (trigger.equals("Expire"))
            {
               expire = trigger;
               break;
            }
         }
         assertTrue(expire != null);

         // transition to Archive state
         request = new TransitionItemsRequest();
         request.setId(itemIds);
         request.setTransition(expire);
         states = binding.transitionItems(request).getStates();
         assertTrue(states.length == 1 && states[0].equals("Archive"));
      }
      catch (PSInvalidSessionFault e)
      {
         throw new junit.framework.AssertionFailedError("Invalid session: " + e);
      }
      catch (PSNotAuthorizedFault e)
      {
         throw new junit.framework.AssertionFailedError("Not authorized: " + e);
      }
      catch (PSUnknownContentTypeFault e)
      {
         throw new junit.framework.AssertionFailedError(
            "Unknown content type: " + e);
      }
      catch (RemoteException e)
      {
         throw new junit.framework.AssertionFailedError("Unexpected error: "
            + e);
      }
      finally
      {
         // cleanup
         contentTest.cleanUpContentType(session, contentType, acls);
      }
   }

   /**
    * Tests loadAugitTrails operation
    */
   @Test
   public void testAuditTrail() throws Exception
   {
      SystemSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      long id335 = getLegacyGuid(335);
      PSAuditTrail[] auditTrails = binding
         .loadAuditTrails(new long[] { id335 });
      assertTrue(auditTrails.length == 1);
      assertTrue(auditTrails[0].getId() == id335);
      PSAudit[] audits = auditTrails[0].getAudits();
      assertTrue(audits.length > 2);
      assertTrue(audits[0].getTransitionName().equals("Checked out"));
      assertTrue(audits[1].getTransitionName().equals("Checked in"));

      long id460 = getLegacyGuid(460);
      long[] ids = new long[] { id335, id460 };
      auditTrails = binding.loadAuditTrails(ids);
      assertTrue(auditTrails.length == 2);
      assertTrue(auditTrails[0].getId() == id335);
      assertTrue(auditTrails[0].getAudits().length > 0);
      assertTrue(auditTrails[1].getId() == id460);
      assertTrue(auditTrails[1].getAudits().length > 0);

      // Negative test - load audit trail for a non-existing item
      try
      {
         auditTrails = binding
            .loadAuditTrails(new long[] { getLegacyGuid(99999) });
         assertTrue(false); // should never get here
      }
      catch (Exception e)
      {
         // the above should fail to here
         assertTrue(true);
      }
   }

   /**
    * Tests relationship instances related services.
    * 
    * @throws Exception if any error occurs. 
    */
   @Test
   public void testSystemSOAPRelationships() throws Exception
   {
      SystemSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);
      SystemDesignSOAPStub dsBinding = getDesignBinding(null);
      PSTestUtils.setSessionHeader(dsBinding, m_session);

      final String USER_RELATIONSHIP = "BenRelationship";

      try 
      {
         // cleanup
         cleanupRelationships(binding, USER_RELATIONSHIP);
         cleanupRelationshipType(dsBinding, USER_RELATIONSHIP);
         // prepare relationship type for the following test
         createSaveRelationshipType(dsBinding, USER_RELATIONSHIP,
               RelationshipCategory.Copy);

         // create a relationship with system type
         PSRelationship rel = createRelationship(
               binding,
               335,
               460,
               com.percussion.design.objectstore.PSRelationshipConfig.TYPE_NEW_COPY);

         // create a relationship with user defined type
         rel = createRelationship(binding, 335, 460, USER_RELATIONSHIP);
         // validate the saved relationship
         assertTrue(getContentIdFromLegacyGuid(rel.getOwnerId()) == 335);
         
         assertTrue(rel.getDependentId() == getLegacyGuid(460));
         // cleanup the created relationship
         binding.deleteRelationships(new long[] { rel.getId() });

         // save relationship
         assertTrue(rel.getDependentId() == getLegacyGuid(460));
         rel.setDependentId(getLegacyGuid(461));
         binding.saveRelationships(new PSRelationship[] { rel });

         // load the above relationship
         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.setId(rel.getId());
         PSRelationship[] rels = loadRelationships(binding, filter);
         assertTrue(rels.length == 1);
         assertTrue(rels[0].getDependentId() == getLegacyGuid(461));

         // find dependents
         filter = new PSRelationshipFilter();
         filter.setConfigurations(new String[] { USER_RELATIONSHIP });
         FindDependentsRequest fdReq = new FindDependentsRequest();
         fdReq.setPSRelationshipFilter(filter);
         fdReq.setId(getLegacyGuid(335));
         long[] dependents = binding.findDependents(fdReq).getIds();
         assertTrue(dependents.length == 1);
         assertTrue(dependents[0] == getLegacyGuid(461));

         // find owners
         filter = new PSRelationshipFilter();
         filter.setConfigurations(new String[] { USER_RELATIONSHIP });
         FindOwnersRequest fwReq = new FindOwnersRequest();
         fwReq.setPSRelationshipFilter(filter);
         fwReq.setId(getLegacyGuid(461));
         long[] owners = binding.findOwners(fwReq).getIds();
         assertTrue(owners.length == 1);
         assertTrue(getContentIdFromLegacyGuid(owners[0]) == 335);
      }
      finally
      {
         // cleanup at the end
         cleanupRelationships(binding, USER_RELATIONSHIP);
         cleanupRelationshipType(dsBinding, USER_RELATIONSHIP);
      }
   }

   private int getContentIdFromLegacyGuid(long legacyId)
   {
      PSLegacyGuid ctId = new PSLegacyGuid(legacyId);
      return ctId.getContentId();
   }
   
   /**
    * Removes all relationship instances with the specified relationship name.
    * 
    * @param binding the stub used to communicate with the server, assumed
    *    not <code>null</code>.
    * @param configName the name of the specified relationship config, assumed
    *    not <code>null</code> or empty.
    * 
    * @throws Exception
    */
   private void cleanupRelationships(SystemSOAPStub binding, String configName)
      throws Exception
   {
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setConfigurations(new String[] { configName });
      PSRelationship[] rels = loadRelationships(binding, filter);
      if (rels.length > 0)
      {
         long[] ids = new long[rels.length];
         for (int i = 0; i < rels.length; i++)
            ids[i] = rels[i].getId();
         binding.deleteRelationships(ids);
      }
   }

   private PSRelationship createRelationship(SystemSOAPStub binding,
      int ownerId, int dependentId, String relationshipName) throws Exception
   {
      CreateRelationshipRequest crReq = new CreateRelationshipRequest();
      long owner = getLegacyGuid(ownerId);
      long dependent = getLegacyGuid(dependentId);
      crReq.setName(relationshipName);
      crReq.setOwnerId(owner);
      crReq.setDependentId(dependent);
      CreateRelationshipResponse response = binding.createRelationship(crReq);
      PSRelationship rel = response.getPSRelationship();
      assertTrue(rel != null);

      return rel;
   }

   /**
    * Tests load relationship configurations
    * @throws Exception 
    */
   @Test
   public void testSystemSOAPLoadRelationshipTypes() throws Exception
   {
      // testing loading the non-design object, RelationshipConfigSummary
      SystemSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      LoadRelationshipTypesRequest lreq = new LoadRelationshipTypesRequest();
      RelationshipConfigSummary[] result = binding.loadRelationshipTypes(lreq);
      
      // should be all system type
      long[] allConfigIds = new long[result.length]; // collect ids used later
      for (int i = 0; i < result.length; i++)
      {
         assertTrue(result[i].getType() == 
            RelationshipConfigSummaryType.system);
         allConfigIds[i] = result[i].getId();
      }

      lreq.setName("*assembl*");
      result = binding.loadRelationshipTypes(lreq);
      for (RelationshipConfigSummary cfgSum : result)
      {
         assertTrue(cfgSum.getName().toLowerCase().contains("assembl"));
      }

      lreq.setName("");
      lreq.setCategory(RelationshipCategory.ActiveAssembly);
      result = binding.loadRelationshipTypes(lreq);
      // There are 2 AA configure, but Cougar project added another one
      assertTrue(result.length == 2 || result.length == 3);

      long[] aaIds = new long[] { result[0].getId(), result[1].getId() };

      // testing loading the design object, RelationshipConfig
      SystemDesignSOAPStub dsBinding = getDesignBinding(null);
      PSTestUtils.setSessionHeader(dsBinding, m_session);
      com.percussion.design.objectstore.PSRelationshipConfig tgtConfig;
      com.percussion.webservices.systemdesign.LoadRelationshipTypesRequest dsLreq = new com.percussion.webservices.systemdesign.LoadRelationshipTypesRequest();

      // load all configs
      dsLreq.setId(allConfigIds);
      dsLreq.setLock(false);
      dsLreq.setOverrideLock(false);
      PSRelationshipConfig[] dsResult = dsBinding.loadRelationshipTypes(dsLreq);
      // validate the loaded configs
      assertTrue(dsResult.length == allConfigIds.length);
      for (PSRelationshipConfig config : dsResult)
      {
         tgtConfig = getServerConfigFromClient(config);
         assertTrue(tgtConfig.isSystem());
      }

      // load AA configs
      dsLreq.setId(aaIds);
      dsResult = dsBinding.loadRelationshipTypes(dsLreq);
      assertTrue(dsResult.length == 2);

      for (PSRelationshipConfig config : dsResult)
      {
         tgtConfig = getServerConfigFromClient(config);
         assertTrue(tgtConfig.isSystem());

         // validates effects in the TYPE_ACTIVE_ASSEMBLY_MANDATORY
         Iterator effects = tgtConfig.getEffects();
         PSConditionalEffect effect;
         String effectName;
         while (effects.hasNext())
         {
            assertTrue(tgtConfig
               .getName()
               .equals(
                  com.percussion.design.objectstore.PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY_MANDATORY));

            effect = (PSConditionalEffect) effects.next();
            effectName = effect.getEffect().getName();
            assertTrue(effectName.equals("sys_PublishMandatory")
               || effectName.equals("sys_UnpublishMandatory"));
         }
      }
   }

   /**
    * Tests load relationship with specified relationship filter.
    * 
    * @throws Exception if any error occurs.
    */
   @Test
   public void testLoadRelationshipsByFilter() throws Exception
   {
      SystemSOAPStub binding = getBinding(null);
      PSTestUtils.setSessionHeader(binding, m_session);

      String[] folderConfigName = new String[] { SysConfigEnum.FOLDER_CONTENT
         .getName() };
      String[] aaConfigName = new String[] { SysConfigEnum.ACTIVE_ASSEMBLY
         .getName() };
      String[] newCopyConfigName = new String[] { SysConfigEnum.NEW_COPY
         .getName() };
      Reference folderContentType = getContentTypeRef(PSFolder.FOLDER_CONTENT_TYPE_ID);

      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
      // test find relationship by RID
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setId(getRelationshipId(1));
      PSRelationship[] rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 1);

      // filter by wrong relationship name
      filter.setId(getRelationshipId(1));
      filter.setConfigurations(aaConfigName);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 0);

      // filter by wrong RID
      filter.setId(getRelationshipId(Integer.MAX_VALUE));
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 0);

      // load the FOLDER relationship with owner as the root folder. 
      filter = new PSRelationshipFilter();
      filter.setOwner(getLegacyGuid(1));
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 2);

      // filter by the owner content type id
      filter = new PSRelationshipFilter();
      filter.setOwner(getLegacyGuid(1));
      filter.setOwnerContentType(folderContentType);
      PSRelationship[] rels_2 = loadRelationships(binding, filter);
      // should be the same as above
      assertTrue(rels_2.length == rels.length);

      // cannot set content type id for both owner & dependent
      try
      {
         filter
            .setDependentContentType(new Reference[] { getContentTypeRef(PSFolder.FOLDER_CONTENT_TYPE_ID) });
         rels_2 = loadRelationships(binding, filter);
         assertTrue("Should fail here", false);
      }
      catch (Exception ie)
      {
         assertTrue(true); // should go through here
      }

      // query by both owner id and relationship name, test folder cache
      filter = new PSRelationshipFilter();
      filter.setOwner(getLegacyGuid(1));
      filter.setConfigurations(folderConfigName);
      rels_2 = loadRelationships(binding, filter);
      // should be the same as above
      assertTrue(rels_2.length == rels.length);

      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
      // filter by limitToOwnerRevisions / limitToEditOrCurrentOwnerRevision
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/

      // query by owner without limit to owner revision
      filter = new PSRelationshipFilter();
      filter.setOwner(getLegacyGuid(335, 1));
      filter.setConfigurations(aaConfigName);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length >= 15); // 5 rels per rev, total 3 rev for 335

      filter.setLimitToOwnerRevisions(true);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 5); // 5 rels per rev

      // query and filter by the owner head (edit or current) revision
      filter = new PSRelationshipFilter();
      filter.setDependent(new long[] { getLegacyGuid(633) });
      filter.setConfigurations(aaConfigName);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length > 1);

      filter.setLimitToEditOrCurrentOwnerRevision(true);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 1);

      // cannot join both owner_id and dependent_id (from 
      // IPSConstants.PSX_RELATIONSHIPS table) with COTNENTSTATUS.contentid
      filter = new PSRelationshipFilter();
      filter.setDependent(new long[] { getLegacyGuid(633) });
      filter.setLimitToEditOrCurrentOwnerRevision(true);
      filter.setDependentContentType(new Reference[] { folderContentType });
      try
      {
         rels = loadRelationships(binding, filter);
         assertTrue(false);
      }
      catch (Exception ie)
      {
         assertTrue(true); // should go through here
      }

      // cannot join both owner_id and dependent_id (from 
      // IPSConstants.PSX_RELATIONSHIPS table) with COTNENTSTATUS.contentid
      filter = new PSRelationshipFilter();
      filter.setOwner(getLegacyGuid(335));
      filter.setLimitToEditOrCurrentOwnerRevision(true);
      filter.setDependentObjectType(ObjectType.item);
      try
      {
         rels = loadRelationships(binding, filter);
         assertTrue(false);
      }
      catch (Exception ie)
      {
         assertTrue(true); // should go through here
      }

      //\/\/\/\/\/\/\/\/\/\/\/\/
      // filter by dependent (s)
      //\/\/\/\/\/\/\/\/\/\/\/\/

      // query by dependent  *** without using folder cache ***
      filter = new PSRelationshipFilter();
      filter.setDependent(new long[] { getLegacyGuid(2) });
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 1);

      // query by dependent  +++ using folder cache +++
      filter = new PSRelationshipFilter();
      filter.setConfigurations(folderConfigName);
      filter.setDependent(new long[] { getLegacyGuid(2) });
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 1);

      // query by more than one dependent. *** without using folder cache ***  
      // id 2 & 3 should only have one parent -> 1
      filter = new PSRelationshipFilter();
      filter.setDependent(new long[] { getLegacyGuid(2), getLegacyGuid(3) });
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 2);

      // query by more than one dependent. +++ without using folder cache +++  
      // id 2 & 3 should only have one parent -> 1
      filter = new PSRelationshipFilter();
      filter.setConfigurations(folderConfigName);
      filter.setDependent(new long[] { getLegacyGuid(2), getLegacyGuid(3) });
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 2);

      //\/\/\/\/\/\/\/\/\/\/\/\/
      // filter by config name(s)
      //\/\/\/\/\/\/\/\/\/\/\/\/

      // query by relationship names
      filter = new PSRelationshipFilter();
      filter.setConfigurations(folderConfigName);
      int numFolderRels = loadRelationships(binding, filter).length;
      filter.setConfigurations(newCopyConfigName);
      int numNewCopyRels = loadRelationships(binding, filter).length;

      filter.setConfigurations(new String[] { folderConfigName[0],
         newCopyConfigName[0] });
      int numFolderNewCopyRels = loadRelationships(binding, filter).length;
      assertTrue(numFolderNewCopyRels == (numFolderRels + numNewCopyRels));

      // get number of translation relationships
      PSRelationshipFilter transFilter = new PSRelationshipFilter();
      String[] transConfigNames = new String[] {
            SysConfigEnum.TRANSLATION.getName(),
            SysConfigEnum.TRNASLATION_MANDATORY.getName() };
      transFilter.setConfigurations(transConfigNames);
      int numOfTransRels = loadRelationships(binding, transFilter).length;
      
      // add translation category into the filter of Folder & NewCopy
      filter.setCategory(PSRelationshipFilterCategory.translation);
      rels = loadRelationships(binding, filter);
      /* the counts are equal because config name, category and rel type are
       * OR'd together, not AND'd */
      assertTrue(rels.length == numFolderNewCopyRels + numOfTransRels);

      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
      // filter by "system" or "user" type
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/

      // add filteringy by relationship "user" type
      // but there is no "user" type, so the result should be the same
      filter.setRelationshipType(RelationshipFilterRelationshipType.user);
      rels_2 = loadRelationships(binding, filter);
      /* the counts are equal because config name, category and rel type are
       * OR'd together, not AND'd */
      assertTrue(rels_2.length == rels.length);

      // query by relationship "system" type
      filter = new PSRelationshipFilter();
      filter.setRelationshipType(RelationshipFilterRelationshipType.system);
      rels_2 = loadRelationships(binding, filter);
      assertTrue(rels_2.length > 0);

      // query by "user" type, the result should be empty
      // since there is no "user" type in FastForward
      filter = new PSRelationshipFilter();
      filter.setRelationshipType(RelationshipFilterRelationshipType.user);
      rels_2 = loadRelationships(binding, filter);
      assertTrue(rels_2.length == 0);

      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/
      // filter by owner object type
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/

      // filter by owner content type *** without using foler cache ***
      filter = new PSRelationshipFilter();
      filter.setDependent(new long[] { getLegacyGuid(2), getLegacyGuid(3) });
      filter.setOwnerObjectType(ObjectType.folder);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 2);

      // filter by owner content type +++ using foler cache +++
      filter = new PSRelationshipFilter();
      filter.setConfigurations(folderConfigName);
      filter.setDependent(new long[] { getLegacyGuid(2), getLegacyGuid(3) });
      filter.setOwnerObjectType(ObjectType.folder);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 2);

      // cannot set object type for both owner & dependent
      try
      {
         filter.setDependentObjectType(ObjectType.folder);
         rels = loadRelationships(binding, filter);
         assertTrue(false);
      }
      catch (Exception ie)
      {
         assertTrue(true); // should go throug here
      }

      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // filter by owner content type
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\

      // filter by owner content type *** without using foler cache ***
      filter = new PSRelationshipFilter();
      filter.setDependent(new long[] { getLegacyGuid(2), getLegacyGuid(3) });
      filter.setOwnerContentType(folderContentType);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 2);

      // owner can only be folder
      filter.setOwnerObjectType(ObjectType.item);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 0);

      // 301 = Auto Index content type
      filter.setOwnerObjectType(null);
      filter.setOwnerContentType(getContentTypeRef(301));
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 0);

      // filter by owner content type +++ using foler cache +++
      filter = new PSRelationshipFilter();
      filter.setConfigurations(folderConfigName);
      filter.setDependent(new long[] { getLegacyGuid(2), getLegacyGuid(3) });
      filter.setOwnerContentType(folderContentType);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 2);

      // owner can only be folder
      filter.setOwnerObjectType(ObjectType.item);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 0);

      // 301 = Auto Index content type
      filter.setOwnerObjectType(null);
      filter.setOwnerContentType(getContentTypeRef(301));
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 0);

      // cannot set content type for both owner & dependent
      try
      {
         filter.setDependentContentType(new Reference[] { folderContentType });
         rels = loadRelationships(binding, filter);
         assertTrue(false);
      }
      catch (Exception ie)
      {
         assertTrue(true); // should go throug here
      }

      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
      // filter by dependent object type
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/

      // filter by owner content type *** without using foler cache ***
      filter = new PSRelationshipFilter();
      filter.setOwner(getLegacyGuid(1));
      filter.setDependentObjectType(ObjectType.folder);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 2);

      filter.setDependentObjectType(ObjectType.item);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 0);

      // filter by owner content type +++ using foler cache +++
      filter = new PSRelationshipFilter();
      filter.setConfigurations(folderConfigName);
      filter.setOwner(getLegacyGuid(1));
      filter.setDependentObjectType(ObjectType.folder);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 2);

      filter.setDependentObjectType(ObjectType.item);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 0);

      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // filter by dependent content type
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\

      Reference[] homeAndNavTree = new Reference[] { getContentTypeRef(312),
         getContentTypeRef(315) };

      // filter by owner content type *** without using foler cache ***
      filter = new PSRelationshipFilter();
      filter.setOwner(getLegacyGuid(1));
      filter.setDependentContentType(new Reference[] { folderContentType });
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 2);

      filter
         .setDependentContentType(new Reference[] { getContentTypeRef(301) });
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 0);

      filter = new PSRelationshipFilter();
      filter.setOwner(getLegacyGuid(301)); // EnterpriseInvestments folder
      filter.setDependentContentType(homeAndNavTree);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 2);

      // filter by owner content type +++  using foler cache +++
      filter = new PSRelationshipFilter();
      filter.setConfigurations(folderConfigName);
      filter.setOwner(getLegacyGuid(1));
      filter.setDependentContentType(new Reference[] { folderContentType });
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 2);

      filter
         .setDependentContentType(new Reference[] { getContentTypeRef(301) });
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 0);

      filter = new PSRelationshipFilter();
      filter.setConfigurations(folderConfigName);
      filter.setOwner(getLegacyGuid(301)); // EnterpriseInvestments folder
      filter.setDependentContentType(homeAndNavTree);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 2);

      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      // filter by Active Assembly properties
      //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
      filter = new PSRelationshipFilter();
      filter.setOwner(getLegacyGuid(664, 1));
      filter.setDependent(new long[] { getLegacyGuid(367) });
      filter.setLimitToOwnerRevisions(true);
      com.percussion.webservices.common.Property[] properties = new com.percussion.webservices.common.Property[5];
      properties[0] = getProperty("sys_siteid", "301");
      properties[1] = getProperty("sys_folderid", "314");
      properties[2] = getProperty("sys_slotid", "509");
      properties[3] = getProperty("sys_variantid", "504");
      properties[4] = getProperty("sys_sortrank", "1");
      filter.setProperties(properties);

      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 1);

      // less efficient if set limitToOwnerRevision(false)
      filter.setLimitToOwnerRevisions(false);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length >= 1);

      // unknown site
      filter = new PSRelationshipFilter();
      filter.setOwner(getLegacyGuid(664, 1));
      properties = new com.percussion.webservices.common.Property[1];
      properties[0] = getProperty("sys_siteid", "9999");
      filter.setProperties(properties);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 0);

      // unknown inline link property
      properties[0] = getProperty("rs_inlinerelationship", "unknown");
      filter.setProperties(properties);
      rels = loadRelationships(binding, filter);
      assertTrue(rels.length == 0);
   }

   /**
    * Test loading relationship with the {@link PSRelationshipFilter} object
    * that setLimitToEditOrCurrentOwnerRevision(true).
    * @throws Exception if error occurs.
    */
   @Test
   public void testLimitToEditOrCurrentOwnerRev() throws Exception
   {
      SystemSOAPStub sysBinding = getBinding(null);
      PSTestUtils.setSessionHeader(sysBinding, m_session);

      // assumed the owner is in public state and has an dependent=344
      final int OWNER_ID = 466;
      long ownerId = getLegacyGuid(OWNER_ID);
      // get owner ID for the dependent = 344
      long dependentId = getLegacyGuid(344);

      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setDependent(new long[] { dependentId });
      filter.setOwner(ownerId);
      filter.setLimitToEditOrCurrentOwnerRevision(true);
      String[] configs = new String[] { com.percussion.design.objectstore.PSRelationshipConfig.TYPE_ACTIVE_ASSEMBLY }; 
      filter.setConfigurations(configs);
      
      // get the relationship with owner_revision = CURRENTREVISION
      PSRelationship[] rels = loadRelationships(sysBinding, filter);
      assertEquals(rels.length, 1);
      PSLegacyGuid ownerGuid = new PSLegacyGuid(rels[0].getOwnerId());
      assertTrue( ownerGuid.getContentId() == OWNER_ID);

      ContentSOAPStub csBinding = getContentSOAPStub(null);
      PSTestUtils.setSessionHeader(csBinding, m_session);

      // Edit Revision == -1 if owner item is in public state
      PSRevisions[] revs = csBinding.findRevisions(new long[]{ownerId});
      assertEquals(revs[0].getEditRevision(), -1);
      
      // check out the owner
      PSItemStatus[] statusarr = csBinding.prepareForEdit(new long[] {ownerId});
      assertEquals(statusarr.length, 1);

      // Quick Edit made EditRevision == CurrRevision+1
      revs = csBinding.findRevisions(new long[]{ownerId});
      assertEquals(revs[0].getEditRevision(), revs[0].getCurrentRevision()+1);

      // get the relationship with owner_revision = EDITREVISION
      rels = loadRelationships(sysBinding, filter);
      assertEquals(rels.length, 1);
      ownerGuid = new PSLegacyGuid(rels[0].getOwnerId());
      assertTrue( ownerGuid.getContentId() == OWNER_ID);

      // restore owner item back to checked in status
      ReleaseFromEditRequest releaseReq = new ReleaseFromEditRequest();
      releaseReq.setPSItemStatus(statusarr);
      csBinding.releaseFromEdit(releaseReq);
   }
   
   /**
    * Convenient method to create a relationship property from the
    * specified name and value.
    * 
    * @param name the name of the property, assumed not <code>null</code> or 
    *    empty.
    * @param value the value of the property, may be <code>null</code> or empty.
    * 
    * @return the created property, never <code>null</code>.
    */
   private com.percussion.webservices.common.Property getProperty(String name,
      String value)
   {
      return new com.percussion.webservices.common.Property(name, value);
   }

   /**
    * Gets the long value of the GUID for the specified item's id and revision
    * @param id the content id of the item
    * @param rev the revision of the item. It may be <code>-1</code> if it is 
    *    undefined. 
    * @return the long value as described above.
    */
   public static long getLegacyGuid(int id, int rev)
   {
      return new PSDesignGuid(new PSLegacyGuid(id, rev)).getValue();
   }

   /**
    * Creates a content type reference for the specified content type id
    *  
    * @param id the content type id for which to create the reference object.
    * 
    * @return the reference object for the specified content type id.
    */
   private Reference getContentTypeRef(int id)
   {
      long contentTypeId = new PSDesignGuid(PSTypeEnum.NODEDEF, id).getValue();
      return new Reference(contentTypeId, null);
   }

   /**
    * Gets the long value of the GUID for a specified relationship id.
    * 
    * @param id the relationship id for which to get the GUID value.
    * 
    * @return the long value as described above.
    */
   private long getRelationshipId(int id)
   {
      return new PSDesignGuid(PSTypeEnum.RELATIONSHIP, id).getValue();
   }

   /**
    * Converts a specified relationship config from client (or webservice) to 
    * server (or objectstore) object.
    * 
    * @param source the client object, assumed not <code>null</code>.
    * @return the converted server object, never <code>null</code>.
    * @throws Exception if any error occurs.
    */
   private com.percussion.design.objectstore.PSRelationshipConfig getServerConfigFromClient(
      PSRelationshipConfig source) throws Exception
   {
      PSTransformerFactory factory = PSTransformerFactory.getInstance();
      Converter converter = factory.getConverter(PSRelationshipConfig.class);

      com.percussion.design.objectstore.PSRelationshipConfig tgtConfig;
      tgtConfig = (com.percussion.design.objectstore.PSRelationshipConfig) converter
         .convert(com.percussion.design.objectstore.PSRelationshipConfig.class,
            source);

      return tgtConfig;
   }

   /**
    * Test the loadWorkflows service call
    * 
    * @throws Exception if the test fails
    */
   @Test
   public void testSystemSOAPLoadWorkflows() throws Exception
   {
      // test no session
      SystemSOAPStub binding = getBinding(null);
      LoadWorkflowsRequest req = new LoadWorkflowsRequest();
      try
      {
         binding.loadWorkflows(req);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      // test invalid session
      PSTestUtils.setSessionHeader(binding, "nosuchsession");
      try
      {
         binding.loadWorkflows(req);
         assertTrue("Should have thrown", false);
      }
      catch (PSInvalidSessionFault e)
      {
         // expected
      }

      PSTestUtils.setSessionHeader(binding, m_session);

      // Test valid operation
      try
      {
         PSWorkflow[] wfs = binding.loadWorkflows(req);
         assertNotNull(wfs);
         assertTrue(wfs.length > 0);
         req.setName("");
         assertTrue(wfs.length == binding.loadWorkflows(req).length);

         for (PSWorkflow workflow : wfs)
         {
            int agingCount = 0;
            int transNotifCount = 0;
            int transRoleCount = 0;
            
            String wfName = workflow.getName();
            req.setName(wfName);
            
            boolean isLocalContent = false;
            if (wfName.equals("LocalContent"))
            {
               isLocalContent = true;
            }
            
            PSWorkflow[] results = binding.loadWorkflows(req);
            assertNotNull(results);
            assertTrue(results.length == 1);
            assertEquals(results[0], workflow);

            assertNotNull(workflow.getAdministratorRole());
            assertNotNull(workflow.getDescription());

            assertTrue(workflow.getStates().length > 0);
            assertTrue(workflow.getRoles().length > 0);
            
            if (isLocalContent)
            {
               assertTrue(workflow.getNotifications().length == 0);
            }
            else
            {
               assertTrue(workflow.getNotifications().length > 0);
            }
            
            for (PSState state : workflow.getStates())
            {
               assertNotNull(state.getName());
               
               if (isLocalContent)
               {
                  assertTrue(state.getTransitions().length == 0);
               }
               else
               {
                  assertTrue(state.getTransitions().length > 0);
               }
               
               assertTrue(state.getAssignedRoles().length > 0);
               agingCount += state.getAgingTransitions().length;
               for (PSTransition transition : state.getTransitions())
               {
                  assertNotNull(transition.getLabel());
                  assertNotNull(transition.getFromState());
                  assertNotNull(transition.getToState());
                  assertNotNull(transition.getComment());
                  transRoleCount += transition.getRoles().length;
                  transNotifCount += transition.getNotifications().length;

                  for (PSNotification notif : transition.getNotifications())
                  {
                     assertNotNull(notif.getStateRoleRecipientType());
                  }

                  if (!transition.isAllowAllRoles())
                  {
                     assertNotNull(transition.getRoles());
                     assertTrue(transition.getRoles().length > 0);
                  }

               }

               for (PSAgingTransition transition : state.getAgingTransitions())
               {
                  assertNotNull(transition.getLabel());
                  assertNotNull(transition.getFromState());
                  assertNotNull(transition.getToState());
                  assertNotNull(transition.getType());
                  transNotifCount += transition.getNotifications().length;

                  for (PSNotification notif : transition.getNotifications())
                  {
                     assertNotNull(notif.getStateRoleRecipientType());
                  }
               }
            }
            
            if (isLocalContent)
            {
               assertTrue(agingCount == 0);
               assertTrue(transNotifCount == 0);
               assertTrue(transRoleCount == 0);
            }
            else
            {
               assertTrue(agingCount > 0);
               assertTrue(transNotifCount > 0);
               assertTrue(transRoleCount > 0);
            }
         }
      }
      catch (com.percussion.webservices.faults.PSInvalidSessionFault e1)
      {
         throw new AssertionFailedError(
            "InvalidSessionFault Exception caught: " + e1);
      }
      catch (Exception e2)
      {
         throw new AssertionFailedError("Unexpected Exception caught: " + e2);
      }
   }

   /**
    * Test switching community.
    * 
    * @throws Exception if the test fails.
    */
   @Test
   public void testSystemSOAPSwitchCommunity() throws Exception
   {
      PSLogin login;
      login = m_login;
      String curComm = login.getDefaultCommunity();
      SwitchCommunityRequest req = new SwitchCommunityRequest();
      SystemSOAPStub binding = getBinding(null);
      boolean didSwitch = false;
      try
      {
         Set<PSCommunity> commSet1 = new HashSet<PSCommunity>();
         commSet1.addAll(Arrays.asList(login.getCommunities()));

         String newCommunity = null;
         for (PSCommunity comm : commSet1)
         {
            if (comm.getName().equals(curComm))
               continue;
            newCommunity = comm.getName();
            break;
         }

         PSTestUtils.setSessionHeader(binding, m_session);
         req.setName(newCommunity);
         binding.switchCommunity(req);
         didSwitch = true;

         PSTestUtils.login();
         login = PSTestUtils.getLastLogin();
         assertEquals(login.getDefaultCommunity(), newCommunity);

         // try bad community
         try
         {
            PSTestUtils.setSessionHeader(binding, login.getSessionId());
            req.setName("foo");
            binding.switchCommunity(req);
            assertFalse("should have thrown", true);
         }
         catch (PSContractViolationFault e)
         {
         }

         // try invalid community
         PSTestUtils.login("admin2", "demo");
         login = PSTestUtils.getLastLogin();
         Set<PSCommunity> commSet2 = new HashSet<PSCommunity>();
         commSet2.addAll(Arrays.asList(login.getCommunities()));
         commSet1.removeAll(commSet2);

         if (!commSet1.isEmpty())
         {
            try
            {
               PSTestUtils.setSessionHeader(binding, login.getSessionId());
               req.setName(commSet1.iterator().next().getName());
               binding.switchCommunity(req);
               assertFalse("should have thrown", true);
            }
            catch (PSUserNotMemberOfCommunityFault e)
            {
            }
         }

      }
      finally
      {
         // switch back
         if (didSwitch)
         {
            PSTestUtils.setSessionHeader(binding, m_session);
            req.setName(curComm);
            binding.switchCommunity(req);
         }
      }

   }

   /**
    * Test switching locale.
    * 
    * @throws Exception if the test fails.
    */
   @Test
   public void testSystemSOAPSwitchLocale() throws Exception
   {
      PSLogin login;
      login = m_login;
      String curLocale = login.getDefaultLocaleCode();
      SystemSOAPStub binding = getBinding(null);
      SwitchLocaleRequest req = new SwitchLocaleRequest();
      boolean didSwitch = false;

      try
      {
         String newLocale = "en-us";
         for (PSLocale locale : login.getLocales())
         {
            if (locale.getCode().equals(curLocale))
               continue;
            newLocale = locale.getCode();
            break;
         }

         PSTestUtils.setSessionHeader(binding, m_session);
         req.setCode(newLocale);
         binding.switchLocale(req);
         didSwitch = true;

         PSTestUtils.login();
         login = PSTestUtils.getLastLogin();
         assertEquals(login.getDefaultLocaleCode(), newLocale);

         // try invalid locale
         try
         {
            req.setCode("foo");
            binding.switchLocale(req);
            assertFalse("should have thrown", true);
         }
         catch (PSInvalidLocaleFault e)
         {

         }
      }
      finally
      {
         // switch back
         if (didSwitch)
         {
            req.setCode(curLocale);
            binding.switchLocale(req);
         }
      }
   }

   /**
    * Switch the current login to the specified community if the community of
    * the login is not the specified community.
    * 
    * @param binding the stub object used to communicate to server, 
    *   not <code>null</code>.
    * @param community the target or to be switched to community, not
    *   <code>null</code> or empty.
    * 
    * @throws Exception for any error.
    */
   protected void switchCommunity(SystemSOAPStub binding, String community)
      throws Exception
   {
      if (binding == null)
         throw new IllegalArgumentException("binding cannot be null");

      if (StringUtils.isBlank(community))
         throw new IllegalArgumentException("community cannot be null or empty");

      String curComm = m_login.getDefaultCommunity();

      // switch to EI community if necessary
      if (!community.equals(curComm))
      {
         SwitchCommunityRequest switchReq = new SwitchCommunityRequest();
         switchReq.setName(community);
         binding.switchCommunity(switchReq);
      }
   }

   /**
    * Test the getAllowedTransitions web service call.
    * 
    * @throws Exception If the test fails.
    */
   @Test
   public void testSystemSOAPGetAllowedTransitions() throws Exception
   {
      PSLogin login;
      login = m_login;
      String curComm = login.getDefaultCommunity();
      SwitchCommunityRequest switchReq = new SwitchCommunityRequest();
      SystemSOAPStub binding = getBinding(60000);
      boolean didSwitchEI = false;

      try
      {
         long ei_item1 = getLegacyGuid(335);
         long ei_item2 = getLegacyGuid(498);

         long ci_item1 = getLegacyGuid(634);
         long ci_item2 = getLegacyGuid(588);

         long[] eiIds = new long[] { ei_item1, ei_item2 };
         long[] ciIds = new long[] { ci_item1, ci_item2 };

         String ei_comm = "Enterprise_Investments";

         Map<String, String> pubTrans = new HashMap<String, String>();
         pubTrans.put("Expire", "Expire");
         pubTrans.put("Quick Edit", "Move to Quick Edit");

         Map<String, String> qeTrans = new HashMap<String, String>();
         qeTrans.put("ReturntoPublic", "Return to Public");

         GetAllowedTransitionsResponse resp;

         // test session
         try
         {
            resp = binding.getAllowedTransitions(eiIds);
            assertTrue("should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         PSTestUtils.setSessionHeader(binding, "nosuchsession");
         try
         {
            resp = binding.getAllowedTransitions(eiIds);
            assertTrue("should have thrown", false);
         }
         catch (PSInvalidSessionFault e)
         {
            // expected
         }

         // test contract
         PSTestUtils.setSessionHeader(binding, m_session);
         try
         {
            resp = binding.getAllowedTransitions(new long[0]);
            assertTrue("should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         PSTestUtils.setSessionHeader(binding, m_session);
         try
         {
            resp = binding
               .getAllowedTransitions(new long[] { getLegacyGuid(99999) });
            assertTrue("should have thrown", false);
         }
         catch (PSContractViolationFault e)
         {
            // expected
         }

         // switch to EI community if necessary
         if (!curComm.equals(ei_comm))
         {
            switchReq.setName(ei_comm);
            binding.switchCommunity(switchReq);
            didSwitchEI = true;
         }

         // test transitions for items in and out of community
         resp = binding.getAllowedTransitions(new long[] { ei_item1 });
         Map<String, String> results = getTransitionsMap(resp);
         assertEquals(pubTrans, results);

         resp = binding.getAllowedTransitions(eiIds);
         results = getTransitionsMap(resp);
         assertEquals(pubTrans, results);

         resp = binding.getAllowedTransitions(new long[] { ci_item1 });
         results = getTransitionsMap(resp);
         assertEquals(0, results.size());

         resp = binding.getAllowedTransitions(ciIds);
         results = getTransitionsMap(resp);
         assertEquals(0, results.size());

         resp = binding
            .getAllowedTransitions(new long[] { ei_item1, ci_item1 });
         results = getTransitionsMap(resp);
         assertEquals(0, results.size());
      }
      finally
      {
         // switch back
         if (didSwitchEI)
         {
            PSTestUtils.setSessionHeader(binding, m_session);
            switchReq.setName(curComm);
            binding.switchCommunity(switchReq);
         }
      }

   }

   /**
    * Get the resulting transitions as a map
    * 
    * @param resp The response from getAllowedTransitions(), assumed not 
    * <code>null</code>.
    * 
    * @return A map of transition trigger/name to label, never 
    * <code>null</code>.
    */
   private Map<String, String> getTransitionsMap(
      GetAllowedTransitionsResponse resp)
   {
      Map<String, String> results = new HashMap<String, String>();

      String[] trans = resp.getTransition();
      String[] labels = resp.getLabel();
      if (trans == null)
      {
         assertTrue(labels == null);
      }
      else
      {
         assertEquals(trans.length, labels.length);
         for (int i = 0; i < trans.length; i++)
         {
            results.put(trans[i], labels[i]);
         }
      }
      return results;
   }
}
