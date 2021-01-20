using System;
using System.Collections.Generic;
using System.Text;
using System.Collections.Specialized;
using RxTest.RxWebServices;


namespace RxTest
{
   class PSSystemTestCase : PSSystemTestBase
   {
      public PSSystemTestCase(PSTest test, PSContentTestCase testContent) : base(test, testContent)
      {
      }

     /**
      * Tests transition items operation
      */
      public void testTransitionItems()
      {
         PSItem[] ItemArray  = null;
         List<PSItem> items  = null;



         try
         {
            // create 1 test items
            items = m_testContent.createTestItems(1, null, false, out ItemArray);

            // make sure we have a directToPublic transition
            long[] itemIds = m_testContent.toItemIds(items);

            GetAllowedTransitionsResponse allowedTransitions = m_test.m_sysService.GetAllowedTransitions(itemIds);
            String directToPublic = null;
            foreach (String trigger in allowedTransitions.Transition)
            {
               if (trigger == "DirecttoPublic")
               {
                  directToPublic = trigger;
                  break;
               }
            }

            PSFileUtils.RxAssert(directToPublic != null);

            TransitionItemsRequest request = null;

            // try with invalid ids
            try
            {
               request = new TransitionItemsRequest();
               request.Id = null;
               request.Transition = directToPublic;
               m_test.m_sysService.TransitionItems(request);
               PSFileUtils.RxAssert(false, "Should have thrown exception");
            }
            catch(Exception e)
            {
               // expected exception
               String message = e.Message;  //for compiler
            }

            // try with invalid ids
            try
            {
               request = new TransitionItemsRequest();
               request.Id = new long[0];
               request.Transition = directToPublic;
               m_test.m_sysService.TransitionItems(request);
               PSFileUtils.RxAssert(false, "Should have thrown exception");
            }
            catch(Exception e)
            {
               // expected exception
               String message = e.Message;  //for compiler
            }

            // try with invalid transition
            try
            {
               request = new TransitionItemsRequest();
               request.Id = itemIds;
               request.Transition = null;
               m_test.m_sysService.TransitionItems(request);
               PSFileUtils.RxAssert(false, "Should have thrown exception");
            }
            catch(Exception e)
            {
               // expected exception
               String message = e.Message;  //for compiler
            }

            // try with invalid transition
            try
            {
               request = new TransitionItemsRequest();
               request.Id = itemIds;
               request.Transition = " ";
               m_test.m_sysService.TransitionItems(request);
               PSFileUtils.RxAssert(false, "Should have thrown exception");
            }
            catch(Exception e)
            {
               // expected exception
               String message = e.Message;  //for compiler
            }

            // try with invalid transition
            try
            {
               request = new TransitionItemsRequest();
               request.Id = itemIds;
               request.Transition = "sometransition";
               m_test.m_sysService.TransitionItems(request);
               PSFileUtils.RxAssert(false, "Should have thrown exception");
            }
            catch(Exception e)
            {
               // expected exception
               String message = e.Message;  //for compiler
            }

            // try to transition item that is CHECKED OUT by the same user
            request = new TransitionItemsRequest();
            request.Id = itemIds;
            request.Transition = directToPublic;
            TransitionItemsResponse transResp = m_test.m_sysService.TransitionItems(request);
            PSFileUtils.RxAssert(transResp.States.Length == 1 && transResp.States[0] == "Public");

            // make sure we have an Expire transition
            allowedTransitions = m_test.m_sysService.GetAllowedTransitions(itemIds);
            String expire = null;
            foreach(String trigger in allowedTransitions.Transition)
            {
               if(trigger == "Expire")
               {
                  expire = trigger;
                  break;
               }
            }
            PSFileUtils.RxAssert(expire != null);

            // transition to Archive state
            request = new TransitionItemsRequest();
            request.Id = itemIds;
            request.Transition = expire;
            transResp = m_test.m_sysService.TransitionItems(request);
            PSFileUtils.RxAssert(transResp.States.Length == 1 && transResp.States[0] == "Archive");

         }

         finally
         {
            // cleanup
            m_testContent.deleteNewCopies(ItemArray);
         }
      }

     /**
      * Tests loadAugitTrails operation
      */
      public void testAuditTrail()
      {

         long id335 = m_testContent.getLegacyGuid(335);

         PSAuditTrail[] auditTrails = m_test.m_sysService.LoadAuditTrails(new long[] { id335 });
         PSFileUtils.RxAssert(auditTrails.Length == 1);
         PSFileUtils.RxAssert(auditTrails[0].id == id335);
         PSAudit[] audits = auditTrails[0].Audits;
         PSFileUtils.RxAssert(audits.Length > 2);
         PSFileUtils.RxAssert(audits[0].transitionName == "Checked out");
         PSFileUtils.RxAssert(audits[1].transitionName == "Checked in");

         long id460 = m_testContent.getLegacyGuid(460);
         long[] ids = new long[] { id335, id460 };
         auditTrails = m_test.m_sysService.LoadAuditTrails(ids);
         PSFileUtils.RxAssert(auditTrails.Length == 2);
         PSFileUtils.RxAssert(auditTrails[0].id == id335);
         PSFileUtils.RxAssert(auditTrails[0].Audits.Length > 0);
         PSFileUtils.RxAssert(auditTrails[1].id == id460);
         PSFileUtils.RxAssert(auditTrails[1].Audits.Length > 0);

         // Negative test - load audit trail for a non-existing item
         try
         {
            auditTrails = m_test.m_sysService.LoadAuditTrails(new long[] { m_testContent.getLegacyGuid(99999) });
            PSFileUtils.RxAssert(false); // should never get here
         }
         catch (Exception e)
         {
            // the above should fail to here
            String message = e.Message;  //for compiler
         }

      }

     /**
      * Tests relationship instances related services.
      *
      * @throws Exception if any error occurs.
      */
      public void testSystemSOAPRelationships()
      {
         PSItem[] ItemArray  = null;
         List<PSItem> items  = null;

         try
         {
            // create 3 test items
            items = m_testContent.createTestItems(3, null, false, out ItemArray);
            PSFileUtils.RxAssert(ItemArray.Length == 3);

            // make sure we have a directToPublic transition
            long[] itemIds = m_testContent.toItemIds(items);

            // find owners
          PSRelationshipFilter filter2 = new PSRelationshipFilter();
            filter2.Configurations = new String[] { m_test.NewCopyRelationshipType };
            FindOwnersRequest fwReq2 = new FindOwnersRequest();
            fwReq2.PSRelationshipFilter = filter2;
            fwReq2.Id = itemIds[2];
            FindOwnersResponse owners2 = m_test.m_sysService.FindOwners(fwReq2);

            // create a relationship with system type
            PSRelationship rel = createRelationship(itemIds[0], itemIds[1], m_test.NewCopyRelationshipType);
            PSFileUtils.RxAssert(rel.ownerId == itemIds[0]);
            PSFileUtils.RxAssert(rel.dependentId == itemIds[1]);
            // cleanup the created relationship
            m_test.m_sysService.DeleteRelationships(new long[] { rel.id });

            // save relationship
            PSFileUtils.RxAssert(rel.dependentId == itemIds[1]);
            rel.dependentId = itemIds[2];
            m_test.m_sysService.SaveRelationships(new PSRelationship[] { rel });

            // load the above relationship
            PSRelationshipFilter filter = new PSRelationshipFilter();
            filter.Id = rel.id;
            PSRelationship[] rels = loadRelationships(filter);
            PSFileUtils.RxAssert(rels.Length == 1);
            PSFileUtils.RxAssert(rels[0].dependentId == itemIds[2]);

            // find dependents
            filter = new PSRelationshipFilter();
            filter.Configurations = new String[] { m_test.NewCopyRelationshipType };
            FindDependentsRequest fdReq = new FindDependentsRequest();
            fdReq.PSRelationshipFilter = filter;
            fdReq.Id = itemIds[0];
            FindDependentsResponse dependents = m_test.m_sysService.FindDependents(fdReq);
            PSFileUtils.RxAssert(dependents.Ids.Length == 1);
            PSFileUtils.RxAssert(dependents.Ids[0] == itemIds[2]);

            // find owners
            filter = new PSRelationshipFilter();
            filter.Configurations = new String[] { m_test.NewCopyRelationshipType };
            FindOwnersRequest fwReq = new FindOwnersRequest();
            fwReq.PSRelationshipFilter = filter;
            fwReq.Id = itemIds[2];
            FindOwnersResponse owners = m_test.m_sysService.FindOwners(fwReq);
            // In .net there are 2 new copy relationships because we used a copy
            // in Java we create the item?
            PSFileUtils.RxAssert(owners.Ids.Length == 2);
            PSFileUtils.RxAssert(owners.Ids[1] == itemIds[0]);

         }
         finally
         {
            // cleanup
            m_testContent.deleteNewCopies(ItemArray);
         }
      }

     /**
      * Tests load relationship with specified relationship filter.
      *
      * @throws Exception if any error occurs.
      */
      public void testLoadRelationshipsByFilter()
      {

         String[]  folderConfigName  = new String[] { TYPE_FOLDER_CONTENT };
         String[]  aaConfigName      = new String[] { TYPE_ACTIVE_ASSEMBLY };
         String[]  newCopyConfigName = new String[] { TYPE_NEW_COPY };

         Reference folderContentType = PSWsUtils.getContentTypeRef(101);

         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
         // test find relationship by RID
         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
         PSRelationshipFilter filter = new PSRelationshipFilter();
         filter.Id = PSWsUtils.getRelationshipId(1);
         PSRelationship[] rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 1);

         // filter by wrong relationship name
         filter.Id = PSWsUtils.getRelationshipId(1);
         filter.Configurations = aaConfigName;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 0);

         // filter by wrong RID
         filter.Id = PSWsUtils.getRelationshipId(long.MaxValue);
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 0);

         // load the FOLDER relationship with owner as the root folder.
         filter = new PSRelationshipFilter();
         filter.Owner = m_testContent.getLegacyGuid(1);
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 2);

         // filter by the owner content type id
         filter = new PSRelationshipFilter();
         filter.Owner = m_testContent.getLegacyGuid(1);
         filter.OwnerContentType = folderContentType;
         PSRelationship[] rels_2 = loadRelationships(filter);
         // should be the same as above
         PSFileUtils.RxAssert(rels_2.Length == rels.Length);

         // cannot set content type id for both owner & dependent
         try
         {
            filter.DependentContentType = new Reference[] { PSWsUtils.getContentTypeRef(101) };
            rels_2 = loadRelationships(filter);
            PSFileUtils.RxAssert(false, "Should fail here");
         }
         catch (Exception ie)
         {
            String message = ie.Message; // for compiler
         }

         // query by both owner id and relationship name, test folder cache
         filter = new PSRelationshipFilter();
         filter.Owner = m_testContent.getLegacyGuid(1);
         filter.Configurations = folderConfigName;
         rels_2 = loadRelationships(filter);
         // should be the same as above
         PSFileUtils.RxAssert(rels_2.Length == rels.Length);

         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
         // filter by limitToOwnerRevisions / limitToEditOrCurrentOwnerRevision
         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/

         // query by owner without limit to owner revision
         filter = new PSRelationshipFilter();
         filter.Owner = 1533303325007; // getLegacyGuid(335, 1)
         filter.Configurations = aaConfigName;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length >= 15); // 5 rels per rev, total 3 rev for 335

         filter.limitToOwnerRevisions = true;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 5); // 5 rels per rev

         // query and filter by the owner head (edit or current) revision
         filter = new PSRelationshipFilter();
         filter.Dependent = new long[] { m_testContent.getLegacyGuid(633) };
         filter.Configurations = aaConfigName;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length > 1);

         filter.limitToEditOrCurrentOwnerRevision = true;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 1);

         // cannot join both owner_id and dependent_id (from
         // IPSConstants.PSX_RELATIONSHIPS table) with COTNENTSTATUS.contentid
         filter = new PSRelationshipFilter();
         filter.Dependent = new long[] { m_testContent.getLegacyGuid(633) };
         filter.limitToEditOrCurrentOwnerRevision = true;
         filter.DependentContentType = new Reference[] { folderContentType };
         try
         {
            rels = loadRelationships(filter);
            PSFileUtils.RxAssert(false);
         }
         catch (Exception ie)
         {
            String message = ie.Message; // for compiler
         }

         // cannot join both owner_id and dependent_id (from
         // IPSConstants.PSX_RELATIONSHIPS table) with COTNENTSTATUS.contentid
         filter = new PSRelationshipFilter();
         filter.Owner = m_testContent.getLegacyGuid(335);
         filter.limitToEditOrCurrentOwnerRevision = true;
         filter.DependentObjectType = ObjectType.item;
         try
         {
            rels = loadRelationships(filter);
            PSFileUtils.RxAssert(false);
         }
         catch (Exception ie)
         {
             String message = ie.Message; // for compiler
         }

         //\/\/\/\/\/\/\/\/\/\/\/\/
         // filter by dependent (s)
         //\/\/\/\/\/\/\/\/\/\/\/\/

         // query by dependent  *** without using folder cache ***
         filter = new PSRelationshipFilter();
         filter.Dependent = new long[] { m_testContent.getLegacyGuid(2) };
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 1);

         // query by dependent  +++ using folder cache +++
         filter = new PSRelationshipFilter();
         filter.Configurations = folderConfigName;
         filter.Dependent = new long[] {  m_testContent.getLegacyGuid(2) };
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 1);

         // query by more than one dependent. *** without using folder cache ***
         // id 2 & 3 should only have one parent -> 1
         filter = new PSRelationshipFilter();
         filter.Dependent = new long[] { m_testContent.getLegacyGuid(2), m_testContent.getLegacyGuid(3) };
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 2);

         // query by more than one dependent. +++ without using folder cache +++
         // id 2 & 3 should only have one parent -> 1
         filter = new PSRelationshipFilter();
         filter.Configurations = folderConfigName;
         filter.Dependent = new long[] { m_testContent.getLegacyGuid(2), m_testContent.getLegacyGuid(3) };
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 2);

         //\/\/\/\/\/\/\/\/\/\/\/\/
         // filter by config name(s)
         //\/\/\/\/\/\/\/\/\/\/\/\/

         // query by relationship names
         filter = new PSRelationshipFilter();
         filter.Configurations = folderConfigName;
         int numFolderRels = loadRelationships(filter).Length;
         filter.Configurations = newCopyConfigName;
         int numNewCopyRels = loadRelationships(filter).Length;

         filter.Configurations = new String[] { folderConfigName[0], newCopyConfigName[0] };
         int numTotalRels = loadRelationships(filter).Length;
         PSFileUtils.RxAssert(numTotalRels == (numFolderRels + numNewCopyRels));

         // query by relationship category. Since there is no translation
         // relationships in the FastForward, so the result should be the same.
         filter.Category = PSRelationshipFilterCategory.translation;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == numTotalRels);

         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
         // filter by "system" or "user" type
         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/

         // add filteringy by relationship "user" type
         // but there is no "user" type, so the result should be the same
         filter.RelationshipType = RelationshipFilterRelationshipType.user;
         rels_2 = loadRelationships(filter);
         PSFileUtils.RxAssert(rels_2.Length == rels.Length);

         // query by relationship "system" type
         filter = new PSRelationshipFilter();
         filter.RelationshipType = RelationshipFilterRelationshipType.system;
         rels_2 = loadRelationships(filter);
         PSFileUtils.RxAssert(rels_2.Length > 0);

         // query by "user" type, the result should be empty
         // since there is no "user" type in FastForward
         filter = new PSRelationshipFilter();
         filter.RelationshipType = RelationshipFilterRelationshipType.user;
         rels_2 = loadRelationships(filter);
         PSFileUtils.RxAssert(rels_2.Length == 0);

         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/
         // filter by owner object type
         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/

         // filter by owner content type *** without using foler cache ***
         filter = new PSRelationshipFilter();
         filter.Dependent = new long[] { m_testContent.getLegacyGuid(2), m_testContent.getLegacyGuid(3) };
         filter.OwnerObjectType = ObjectType.folder;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 2);

         // filter by owner content type +++ using foler cache +++
         filter = new PSRelationshipFilter();
         filter.Configurations = folderConfigName;
         filter.Dependent = new long[] { m_testContent.getLegacyGuid(2), m_testContent.getLegacyGuid(3) };
         filter.OwnerObjectType = ObjectType.folder;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 2);

         // cannot set object type for both owner & dependent
         try
         {
            filter.DependentObjectType = ObjectType.folder;
            rels = loadRelationships(filter);
            PSFileUtils.RxAssert(false);
         }
         catch (Exception ie)
         {
            String message = ie.Message; // for compiler
         }

         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
         // filter by owner content type
         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\

         // filter by owner content type *** without using foler cache ***
         filter = new PSRelationshipFilter();
         filter.Dependent = new long[] { m_testContent.getLegacyGuid(2), m_testContent.getLegacyGuid(3) };
         filter.OwnerContentType = folderContentType;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 2);

         // owner can only be folder
         filter.OwnerObjectType = ObjectType.item;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 0);

         // 301 = Auto Index content type
         filter.OwnerObjectType = null;
         filter.OwnerContentType = PSWsUtils.getContentTypeRef(301);
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 0);

         // filter by owner content type +++ using foler cache +++
         filter = new PSRelationshipFilter();
         filter.Configurations = folderConfigName;
         filter.Dependent = new long[] { m_testContent.getLegacyGuid(2), m_testContent.getLegacyGuid(3) };
         filter.OwnerContentType = folderContentType;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 2);

         // owner can only be folder
         filter.OwnerObjectType = ObjectType.item;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 0);

         // 301 = Auto Index content type
         filter.OwnerObjectType = null;
         filter.OwnerContentType = PSWsUtils.getContentTypeRef(301);
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 0);

         // cannot set content type for both owner & dependent
         try
         {
            filter.DependentContentType = new Reference[] { folderContentType };
            rels = loadRelationships(filter);
            PSFileUtils.RxAssert(false);
         }
         catch (Exception ie)
         {
            String message = ie.Message; // for compiler
         }

         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/
         // filter by dependent object type
         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/

         // filter by owner content type *** without using foler cache ***
         filter = new PSRelationshipFilter();
         filter.Owner = m_testContent.getLegacyGuid(1);
         filter.DependentObjectType = ObjectType.folder;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 2);

         filter.DependentObjectType = ObjectType.item;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 0);

         // filter by owner content type +++ using foler cache +++
         filter = new PSRelationshipFilter();
         filter.Configurations = folderConfigName;
         filter.Owner =  m_testContent.getLegacyGuid(1);
         filter.DependentObjectType = ObjectType.folder;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 2);

         filter.DependentObjectType = ObjectType.item;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 0);

         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
         // filter by dependent content type
         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\

         Reference[] homeAndNavTree = new Reference[] { PSWsUtils.getContentTypeRef(312), PSWsUtils.getContentTypeRef(315) };

         // filter by owner content type *** without using foler cache ***
         filter = new PSRelationshipFilter();
         filter.Owner =  m_testContent.getLegacyGuid(1);
         filter.DependentContentType = new Reference[] { folderContentType };
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 2);

         filter.DependentContentType = new Reference[] { PSWsUtils.getContentTypeRef(301) };
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 0);

         filter = new PSRelationshipFilter();
         filter.Owner =  m_testContent.getLegacyGuid(301); // EnterpriseInvestments folder
         filter.DependentContentType = homeAndNavTree;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 2);

         // filter by owner content type +++  using foler cache +++
         filter = new PSRelationshipFilter();
         filter.Configurations = folderConfigName;
         filter.Owner = m_testContent.getLegacyGuid(1);
         filter.DependentContentType = new Reference[] {  new Reference() };
         filter.DependentContentType[0].id = folderContentType.id;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 2);

         filter.DependentContentType = new Reference[] { PSWsUtils.getContentTypeRef(301) };
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 0);

         filter = new PSRelationshipFilter();
         filter.Configurations = folderConfigName;
         filter.Owner =  m_testContent.getLegacyGuid(301);  // EnterpriseInvestments folder
         filter.DependentContentType = homeAndNavTree;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 2);

         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
         // filter by Active Assembly properties
         //\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\/\
         filter = new PSRelationshipFilter();
         filter.Owner = 1533303325336;
         filter.Dependent = new long[] { m_testContent.getLegacyGuid(367) };
         filter.limitToOwnerRevisions = true;
         Property1[] properties = new Property1[5];
         properties[0] =  getProperty("sys_siteid", "301");
         properties[1] = getProperty("sys_folderid", "314");
         properties[2] = getProperty("sys_slotid", "509");
         properties[3] = getProperty("sys_variantid", "504");
         properties[4] = getProperty("sys_sortrank", "1");
         filter.Properties = properties;

         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 1);

         // less efficient if set limitToOwnerRevision(false)
         filter.limitToOwnerRevisions = false;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length >= 1);

         // unknown site
         filter = new PSRelationshipFilter();
         filter.Owner = 1533303325336;    //getLegacyGuid(664, 1)
         properties = new Property1[1];
         properties[0] = getProperty("sys_siteid", "9999");
         filter.Properties = properties;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 0);

         // unknown inline link property
         properties[0] = getProperty("rs_inlinerelationship", "unknown");
         filter.Properties = properties;
         rels = loadRelationships(filter);
         PSFileUtils.RxAssert(rels.Length == 0);

      }

     /**
      * Test the getAllowedTransitions web service call.
      *
      * @throws Exception If the test fails.
      */
      public void testSystemSOAPGetAllowedTransitions()
      {

         long ei_item1  = m_testContent.getLegacyGuid(335);
         long ei_item2  = m_testContent.getLegacyGuid(498);

         long ci_item1  = m_testContent.getLegacyGuid(634);
         long ci_item2  = m_testContent.getLegacyGuid(588);

         long[] eiIds   = new long[] { ei_item1, ei_item2 };
         long[] ciIds   = new long[] { ci_item1, ci_item2 };

         StringDictionary pubTrans = new StringDictionary();
         pubTrans.Add("Expire", "Expire");
         pubTrans.Add("Quick Edit", "Move to Quick Edit");


         StringDictionary qeTrans = new StringDictionary();
         qeTrans.Add("ReturntoPublic", "Return to Public");

         GetAllowedTransitionsResponse resp;

         // switch to EI community
         SwitchCommunityRequest switchReq = new SwitchCommunityRequest();
         switchReq.Name = m_test.CommunityName;
         m_test.m_sysService.SwitchCommunity(switchReq);


         // test transitions for items in and out of community
         resp = m_test.m_sysService.GetAllowedTransitions(new long[] { ei_item1 });

         StringDictionary results = getTransitionsMap(resp);
         compareStringDictionaries(pubTrans, results);

         resp = m_test.m_sysService.GetAllowedTransitions(eiIds);
         results = getTransitionsMap(resp);
         compareStringDictionaries(pubTrans, results);

         resp = m_test.m_sysService.GetAllowedTransitions(new long[] { ci_item1 });
         results = getTransitionsMap(resp);
         PSFileUtils.RxAssert(results.Count == 0);

         resp = m_test.m_sysService.GetAllowedTransitions(ciIds);
         results = getTransitionsMap(resp);
         PSFileUtils.RxAssert(results.Count == 0);

         resp = m_test.m_sysService.GetAllowedTransitions(new long[] { ei_item1, ci_item1 });
         results = getTransitionsMap(resp);
         PSFileUtils.RxAssert(results.Count == 0);

      }

      /**
       * Test switching locale.
       *
       * @throws Exception if the test fails.
       */
      public void testSystemSOAPSwitchLocale()
      {
         String saveLocaleCode;
         PSLocale[] value;
         LoadLocalesRequest req = new LoadLocalesRequest();
         SwitchLocaleRequest loginReq = new SwitchLocaleRequest();

         PSLogin login = getLogin("admin1", "demo", null, null, null);

         saveLocaleCode = login.defaultLocaleCode;

         try
         {
            req.Code = null;
            req.Name = null;
            value = m_test.m_contService.LoadLocales(req);

            // If the test locales are here test all the locales

            foreach (PSLocale locale in value)
            {
               loginReq.Code = locale.code;
               m_test.m_sysService.SwitchLocale(loginReq);

               login = getLogin("admin1", "demo", null, null, null);
               PSFileUtils.RxAssert(locale.code == login.defaultLocaleCode);
            }
         }
         finally
         {
            loginReq.Code = saveLocaleCode;
            m_test.m_sysService.SwitchLocale(loginReq);
         }

      }

     /**
      * Tests load relationship configurations
      * @throws Exception
      */
      public void testSystemSOAPLoadRelationshipTypes()
      {

         LoadRelationshipTypesRequest lreq = new LoadRelationshipTypesRequest();
         RelationshipConfigSummary[] result = m_test.m_sysService.LoadRelationshipTypes(lreq);
         PSFileUtils.RxAssert(result.Length == 7);  //PSRelationshipConfig.ID_TRANSLATION_MANDATORY);

         // should be all system type
         long[] allConfigIds = new long[result.Length]; // collect ids used later
         for (int i = 0; i < result.Length; i++)
         {
            PSFileUtils.RxAssert(result[i].type == RelationshipConfigSummaryType.system);
            allConfigIds[i] = result[i].id;
         }

         lreq.Name = "*assembl*";
         result = m_test.m_sysService.LoadRelationshipTypes(lreq);
         PSFileUtils.RxAssert(result.Length == 2);

         lreq.Name = "";
         lreq.Category = RelationshipCategory.ActiveAssembly;
         lreq.CategorySpecified = true;
         result = m_test.m_sysService.LoadRelationshipTypes(lreq);
         PSFileUtils.RxAssert(result.Length == 2);

      }

     /**
      * Test the loadWorkflows service call
      *
      * @throws Exception if the test fails
      */
      public void testSystemSOAPLoadWorkflows()
      {

         LoadWorkflowsRequest req = new LoadWorkflowsRequest();

         // Test valid operation
         PSWorkflow[] wfs = m_test.m_sysService.LoadWorkflows(req);
         PSFileUtils.RxAssert(wfs != null);
         PSFileUtils.RxAssert(wfs.Length > 0);
         req.Name = "";
         PSFileUtils.RxAssert(wfs.Length == m_test.m_sysService.LoadWorkflows(req).Length);

         int agingCount = 0;
         int transNotifCount = 0;
         int transRoleCount = 0;

         foreach(PSWorkflow workflow in wfs)
         {
            req.Name = workflow.name;
            PSWorkflow[] results = m_test.m_sysService.LoadWorkflows(req);
            PSFileUtils.RxAssert(results != null);
            PSFileUtils.RxAssert(results.Length == 1);
            PSFileUtils.RxAssert(results[0].administratorRole == workflow.administratorRole);
            PSFileUtils.RxAssert(results[0].description == workflow.description);
            PSFileUtils.RxAssert(results[0].id == workflow.id);
            PSFileUtils.RxAssert(results[0].idSpecified == workflow.idSpecified);
            PSFileUtils.RxAssert(results[0].initialStateId == workflow.initialStateId);
            PSFileUtils.RxAssert(results[0].name == workflow.name);

            PSFileUtils.RxAssert(results[0].Notifications.Length == workflow.Notifications.Length);
            for (int inc = 0; inc < results[0].Notifications.Length; ++inc)
            {
               PSFileUtils.RxAssert(results[0].Notifications[inc].body == workflow.Notifications[inc].body);
               PSFileUtils.RxAssert(results[0].Notifications[inc].description == workflow.Notifications[inc].description);
               PSFileUtils.RxAssert(results[0].Notifications[inc].id == workflow.Notifications[inc].id);
               PSFileUtils.RxAssert(results[0].Notifications[inc].subject == workflow.Notifications[inc].subject);
            }

            PSFileUtils.RxAssert(results[0].Roles.Length == workflow.Roles.Length);
            for (int inc = 0; inc < results[0].Roles.Length; ++inc)
            {
               PSFileUtils.RxAssert(results[0].Roles[inc].description == workflow.Roles[inc].description);
               PSFileUtils.RxAssert(results[0].Roles[inc].id == workflow.Roles[inc].id);
               PSFileUtils.RxAssert(results[0].Roles[inc].name == workflow.Roles[inc].name);
            }

            PSFileUtils.RxAssert(results[0].States.Length == workflow.States.Length);
            for (int inc = 0; inc < results[0].States.Length; ++inc)
            {
               PSFileUtils.RxAssert(results[0].States[inc].description == workflow.States[inc].description);
               PSFileUtils.RxAssert(results[0].States[inc].id == workflow.States[inc].id);
               PSFileUtils.RxAssert(results[0].States[inc].name == workflow.States[inc].name);


               agingCount += results[0].States[inc].AgingTransitions.Length;
               PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions.Length == workflow.States[inc].AgingTransitions.Length);
               for (int inc2 = 0; inc2 < workflow.States[inc].AgingTransitions.Length; ++inc2)
               {
                  PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].description == workflow.States[inc].AgingTransitions[inc2].description);
                  PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].fromState == workflow.States[inc].AgingTransitions[inc2].fromState);
                  PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].id == workflow.States[inc].AgingTransitions[inc2].id);
                  PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].idSpecified == workflow.States[inc].AgingTransitions[inc2].idSpecified);
                  PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].interval == workflow.States[inc].AgingTransitions[inc2].interval);
                  PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].intervalSpecified == workflow.States[inc].AgingTransitions[inc2].intervalSpecified);
                  PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].label == workflow.States[inc].AgingTransitions[inc2].label);

                  transNotifCount += results[0].States[inc].AgingTransitions[inc2].Notifications.Length;
                  PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].Notifications.Length == workflow.States[inc].AgingTransitions[inc2].Notifications.Length);
                  for (int inc3 = 0; inc3 < results[0].States[inc].AgingTransitions[inc2].Notifications.Length; ++inc3)
                  {
                     PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].Notifications[inc3].CCRecipients.Length == workflow.States[inc].AgingTransitions[inc2].Notifications[inc3].CCRecipients.Length);
                     for (int inc4 = 0; inc4 < results[0].States[inc].AgingTransitions[inc2].Notifications[inc3].CCRecipients.Length; ++inc4)
                     {
                        PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].Notifications[inc3].CCRecipients[inc4].ToString() == workflow.States[inc].AgingTransitions[inc2].Notifications[inc3].CCRecipients[inc4].ToString());
                     }

                     PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].Notifications[inc3].id == workflow.States[inc].AgingTransitions[inc2].Notifications[inc3].id);

                     PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].Notifications[inc3].Recipients.Length == workflow.States[inc].AgingTransitions[inc2].Notifications[inc3].Recipients.Length);
                     for (int inc4 = 0; inc4 < results[0].States[inc].AgingTransitions[inc2].Notifications[inc3].Recipients.Length; ++inc4)
                     {
                        PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].Notifications[inc3].Recipients[inc4].ToString() == workflow.States[inc].AgingTransitions[inc2].Notifications[inc3].Recipients[inc4].ToString());
                     }

                     PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].Notifications[inc3].stateRoleRecipientType == workflow.States[inc].AgingTransitions[inc2].Notifications[inc3].stateRoleRecipientType);
                  }

                  PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].systemField == workflow.States[inc].AgingTransitions[inc2].systemField);
                  PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].toState == workflow.States[inc].AgingTransitions[inc2].toState);
                  PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].transitionAction == workflow.States[inc].AgingTransitions[inc2].transitionAction);
                  PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].trigger == workflow.States[inc].AgingTransitions[inc2].trigger);
                  PSFileUtils.RxAssert(results[0].States[inc].AgingTransitions[inc2].type == workflow.States[inc].AgingTransitions[inc2].type);
               }

               PSFileUtils.RxAssert(results[0].States[inc].AssignedRoles.Length == workflow.States[inc].AssignedRoles.Length);
               PSFileUtils.RxAssert(results[0].States[inc].idSpecified == workflow.States[inc].idSpecified);
               PSFileUtils.RxAssert(results[0].States[inc].isPublishable == workflow.States[inc].isPublishable);
               PSFileUtils.RxAssert(results[0].States[inc].sortOrder == workflow.States[inc].sortOrder);

               PSFileUtils.RxAssert(results[0].States[inc].Transitions.Length == workflow.States[inc].Transitions.Length);
               for (int inc2 = 0; inc2 < results[0].States[inc].Transitions.Length; ++inc2)
               {
                  PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].allowAllRoles == workflow.States[inc].Transitions[inc2].allowAllRoles);
                  PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].approvals == workflow.States[inc].Transitions[inc2].approvals);
                  PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].comment == workflow.States[inc].Transitions[inc2].comment);
                  PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].defaultTransition == workflow.States[inc].Transitions[inc2].defaultTransition);
                  PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].description == workflow.States[inc].Transitions[inc2].description);
                  PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].fromState == workflow.States[inc].Transitions[inc2].fromState);
                  PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].id == workflow.States[inc].Transitions[inc2].id);
                  PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].idSpecified == workflow.States[inc].Transitions[inc2].idSpecified);
                  PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].label == workflow.States[inc].Transitions[inc2].label);

                  transNotifCount += results[0].States[inc].Transitions[inc2].Notifications.Length;
                  PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].Notifications.Length == workflow.States[inc].Transitions[inc2].Notifications.Length);
                  for (int inc3 = 0; inc3 < results[0].States[inc].Transitions[inc2].Notifications.Length; ++inc3)
                  {
                     PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].Notifications[inc3].Recipients.Length == workflow.States[inc].Transitions[inc2].Notifications[inc3].Recipients.Length);
                     for (int inc4 = 0; inc4 < results[0].States[inc].Transitions[inc2].Notifications[inc3].Recipients.Length; ++inc4)
                     {
                        PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].Notifications[inc3].Recipients[inc4].ToString() == workflow.States[inc].Transitions[inc2].Notifications[inc3].Recipients[inc4].ToString());
                     }

                     PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].Notifications[inc3].CCRecipients.Length == workflow.States[inc].Transitions[inc2].Notifications[inc3].CCRecipients.Length);
                     for (int inc4 = 0; inc4 < results[0].States[inc].Transitions[inc2].Notifications[inc3].CCRecipients.Length; ++inc4)
                     {
                        PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].Notifications[inc3].CCRecipients[inc4].ToString() == workflow.States[inc].Transitions[inc2].Notifications[inc3].CCRecipients[inc4].ToString());
                     }

                     PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].Notifications[inc3].id == workflow.States[inc].Transitions[inc2].Notifications[inc3].id);
                     PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].Notifications[inc3].stateRoleRecipientType == workflow.States[inc].Transitions[inc2].Notifications[inc3].stateRoleRecipientType);
                  }

                  transRoleCount += results[0].States[inc].Transitions[inc2].Roles.Length;
                  PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].Roles.Length == workflow.States[inc].Transitions[inc2].Roles.Length);
                  for (int inc3 = 0; inc3 < results[0].States[inc].Transitions[inc2].Roles.Length; ++inc3)
                  {
                     PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].Roles[inc3].description == workflow.States[inc].Transitions[inc2].Roles[inc3].description);
                     PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].Roles[inc3].id == workflow.States[inc].Transitions[inc2].Roles[inc3].id);
                     PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].Roles[inc3].name == workflow.States[inc].Transitions[inc2].Roles[inc3].name);
                  }

                  PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].toState == workflow.States[inc].Transitions[inc2].toState);
                  PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].transitionAction == workflow.States[inc].Transitions[inc2].transitionAction);
                  PSFileUtils.RxAssert(results[0].States[inc].Transitions[inc2].trigger == workflow.States[inc].Transitions[inc2].trigger);
               }

               PSFileUtils.RxAssert(results[0].typedId == workflow.typedId);

               PSFileUtils.RxAssert(workflow.administratorRole != null);
               PSFileUtils.RxAssert(workflow.description != null);

               PSFileUtils.RxAssert(workflow.States.Length > 0);
               PSFileUtils.RxAssert(workflow.Roles.Length > 0);
               PSFileUtils.RxAssert(workflow.Notifications.Length > 0);
            }
         }
         PSFileUtils.RxAssert(agingCount > 0);
         PSFileUtils.RxAssert(transNotifCount > 0);
         PSFileUtils.RxAssert(transRoleCount > 0);

      }
   }
}
