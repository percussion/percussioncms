using System;
using System.Collections.Generic;
using System.Text;
using System.Web.Services.Protocols;
using System.Xml;
using RxTest.RxWebServices;
using RxFaultFactory;

namespace RxTest
{
    public class PSContentTestCase : PSContentTestBase
    {
        public PSContentTestCase(PSTest test) : base(test)
        {
        }

        /// <summary>
        /// Tests various SOAP Faults.
        /// </summary>
        public void testFaults()
        {

            string[] paths = new string[] { "  " };
            LoadFoldersRequest lfReq = new LoadFoldersRequest();
            lfReq.Path = paths;
            try
            {
                m_test.m_contService.LoadFolders(lfReq);
            }
            catch (SoapException e)
            {
                IPSSoapFault fault = PSFaultFactory.GetFault(e);

                PSTest.ConsoleMessage("Caught Exception: " + fault.GetFaultName());
                PSTest.ConsoleMessage("Error Message: " + fault.GetMessage());
            }

            paths = new string[] { "//Sites", "//Folders", "//Sites/unknown", "//Folders/unknown2" };

            lfReq.Path = paths;
            try
            {
                m_test.m_contService.LoadFolders(lfReq);
            }
            catch (SoapException e)
            {
                IPSSoapFault fault = PSFaultFactory.GetFault(e);

                PSTest.ConsoleMessage("Caught Exception: " + fault.GetFaultName());
                PSTest.ConsoleMessage("Error Message: " + fault.GetMessage());

                PSFileUtils.RxAssert(fault.GetFaultName() == "PSErrorResultsFault");
                PSErrorsFault errFault = (PSErrorsFault)fault;
                // there are 2 good path
                PSFileUtils.RxAssert(errFault.getSuccessIds().Count == 2);
                // there are 2 bad path
                PSFileUtils.RxAssert(errFault.getErrorMessages().Count == 2);
            }

            AddFolderTreeRequest afReq = new AddFolderTreeRequest();
            afReq.Path = "//Folders/TestFolder_1/TestFolder_2";
            PSFolder[] folders = m_test.m_contService.AddFolderTree(afReq);
            PSFileUtils.RxAssert(folders.Length == 2);

            DeleteFoldersRequest dfReq = new DeleteFoldersRequest();

            long FakeID = 1533303334671;  // FakeGuid = new PSLegacyGuid(9999,1)
            try
            {
                dfReq.Id = new long[] { folders[1].id, FakeID};
                m_test.m_contService.DeleteFolders(dfReq);
            }
            catch (SoapException e)
            {
                IPSSoapFault fault = PSFaultFactory.GetFault(e);

                PSTest.ConsoleMessage("Caught Exception: " + fault.GetFaultName());
                PSTest.ConsoleMessage("Error Message: " + fault.GetMessage());

                PSFileUtils.RxAssert(fault.GetFaultName() == "PSErrorsFault");
                PSErrorsFault errFault = (PSErrorsFault)fault;
                // there are 1 good path
                PSFileUtils.RxAssert(errFault.getSuccessIds().Count == 1);
                // there are 1 bad path
                PSFileUtils.RxAssert(errFault.getErrorMessages().Count == 1);
                string errorMsg = errFault.getErrorMessages()[FakeID];
                PSFileUtils.RxAssert(errorMsg != null);

            }

            // clean up created folders
            dfReq.Id = new long[] { folders[0].id};
            m_test.m_contService.DeleteFolders(dfReq);

        }

      public void testLoadKeywords()
      {
          string[] keywordNames = { "Checkout_St*",
                                    "Checkout_Status_types" };

          string[] keywordChoices = { "Checked In", "Checked Out", "Checked Out by Me" };

          LoadKeywordsRequest request = null;
          PSKeyword[] keywords = null;


          // load all keywords
          request = new LoadKeywordsRequest();
          request.Name = null;
          keywords = m_test.m_contService.LoadKeywords(request);

          // make sure there are  at least 25
          PSFileUtils.RxAssert(keywords.Length > 25,
                               String.Format("To many Keywords found {0}", keywords.Length));

          // Check wild card and full name search
          // Will use the final result as a keyword we are going to look at further
          for (int inc = 0 ; inc < keywordNames.Length ; ++inc)
          {
              request.Name = keywordNames[inc];
              keywords = m_test.m_contService.LoadKeywords(request);
              PSFileUtils.RxAssert(keywords.Length == 1,
                                   String.Format("Wrong Number ({0}) of Keywords found for {1}", keywords.Length, request.Name));
          }

          // Test to make sure we have correct keyword values
          PSFileUtils.RxAssert(keywords[0].Choices.Length == keywordChoices.Length,
                                   String.Format("Wrong Number ({0}) of Keyword Choices found for {1}", keywords[0].Choices.Length, request.Name));

          for( int inc = 0 ; inc < keywordChoices.Length ; ++inc ) {
               PSFileUtils.RxAssert(keywords[0].Choices[inc].value == keywordChoices[inc],
                                   String.Format("{0} Choice \"{1}\" is not \"{2}\"", request.Name, keywords[0].Choices[inc].value, keywordChoices[inc]));
           }

      }

      public void testAaRelationship()
      {
          switchToIECommunity();

          PSAaRelationship[] rels;


          int ContentID = 335;    // get AA relationships which do not contain site or folder ids
          rels = loadAaRelationshipsByOwner(ContentID);
          PSFileUtils.RxAssert(rels.Length > 0, "Unable to load AA relationships by owner");

          for (int inc = 0; inc < rels.Length; ++inc)
          {
              PSFileUtils.RxAssert(rels[inc].Folder == null, String.Format("Found Folder relationship rels[{0}] on Content ID {1}", inc, ContentID));
              PSFileUtils.RxAssert(rels[inc].Site == null, String.Format("Found Folder relationship rels[{0}] on Content ID {1}", inc, ContentID));
          }

          ContentID = 634;    // get AA relationships which contain site or folder ids
          rels = loadAaRelationshipsByOwner(ContentID);
          PSFileUtils.RxAssert(rels.Length > 0, "Unable to load AA relationships by owner");

          bool FoundSite = false;
          bool FoundFolder = false;
          for (int inc = 0; inc < rels.Length; ++inc)
          {
              if(rels[inc].Folder != null) {
                  String name = rels[inc].Folder.name;
                  String path = rels[inc].Folder.path;
                  PSFileUtils.RxAssertTrue(String.IsNullOrEmpty(name), String.Format("Folder name missing for {0} of Content ID {1}", rels[inc].id, ContentID));
                  PSFileUtils.RxAssertTrue(String.IsNullOrEmpty(path), String.Format("Folder name missing for {0} of Content ID {1}", rels[inc].id, ContentID));

                  FoundFolder = true;
              }
              if(rels[inc].Site != null) {
                  String name = rels[inc].Site.name;
                  PSFileUtils.RxAssertTrue(String.IsNullOrEmpty(name), String.Format("Site name missing for {0} of Content ID {1}", rels[inc].id, ContentID));

                  FoundSite = true;
              }
          }

          PSFileUtils.RxAssert(FoundSite, String.Format("Unable to find site for Content ID {0}", ContentID));
          PSFileUtils.RxAssert(FoundFolder, String.Format("Unable to find Found for Content ID {0}", ContentID));

          // testing addContentRelationships
          const String SLOT_NAME = "rffContacts";
          const String TEMPLATE_NAME = "rffSnFlash";

          // has to check out the owner
          PSItemStatus itemStatus335 = checkoutItemLegacy(335);

          AddContentRelationsRequest areq = new AddContentRelationsRequest();
          //Sites/EnterpriseInvestments/AboutEnterpriseInvestments/Page - About Enterprise Investments
          long ownerId335         = PSGetGuidFromContentID(335);
          long dependentId2       = PSGetGuidFromContentID(460);
          long dependentId3       = PSGetGuidFromContentID(461);
          areq.Id                 = ownerId335;
          areq.RelatedId          = new long[] { dependentId2, dependentId3 };
          areq.Slot               = SLOT_NAME;
          areq.Template           = TEMPLATE_NAME;
          areq.RelationshipConfig = "ActiveAssembly";
          areq.Index              = 0;
          rels = m_test.m_contService.AddContentRelations(areq);
          PSFileUtils.RxAssert(rels.Length == 2, String.Format("Unable to add Content Relations for Content ID 335, length = {0}", rels.Length));

          // save created ids, which will be used for cleanup later
          long[] createdIds = { rels[0].id, rels[1].id };

          // sort-rank(s) are 11 right now
          PSAaRelationship rel = loadAaRelationshipsByRid(createdIds[0], true);
          PSFileUtils.RxAssert(rel.SortRank == "0", String.Format("Sort Rank is not 0 but {0}", rel.SortRank));
          rel = loadAaRelationshipsByRid(createdIds[1], true);
          PSFileUtils.RxAssert(rel.SortRank == "1", String.Format("Sort Rank is not 1 but {0}", rel.SortRank));


          // test reordering
          ReorderContentRelationsRequest roReq = new ReorderContentRelationsRequest();
          roReq.Id = createdIds;
          roReq.index = 0;

          m_test.m_contService.ReorderContentRelations(roReq);
          rel = loadAaRelationshipsByRid(createdIds[0], true);
          PSFileUtils.RxAssert(rel.SortRank.CompareTo("0") == 0, String.Format("Sort Rank is not 0 but {0}", rel.SortRank));
          rel = loadAaRelationshipsByRid(createdIds[1], true);
          PSFileUtils.RxAssert(rel.SortRank.CompareTo("1") == 0, String.Format("Sort Rank is not 1 but {0}", rel.SortRank));


          // testing loadContentRelations
          rel = loadAaRelationshipsByRid(createdIds[0], true);
          PSFileUtils.RxAssert(rel.id == rels[0].id, String.Format("Relationship do not match {0} != {1}", rel.id, rels[0].id));
          PSFileUtils.RxAssert(rel.Slot.name.CompareTo(SLOT_NAME) == 0, String.Format("Slot Name is wrong {0}", rel.Slot.name));
          PSFileUtils.RxAssert(rel.Template.name.CompareTo(TEMPLATE_NAME) == 0, String.Format("Template Name is wrong {0}", rel.Template.name));


          // testing saveContentRelations
          LoadAssemblyTemplatesRequest latReq = new LoadAssemblyTemplatesRequest();
          latReq.Name = TEMPLATE_NAME;
          PSAssemblyTemplate[] templates = m_test.m_assService.LoadAssemblyTemplates(latReq);
          PSFileUtils.RxAssert(templates.Length == 1, String.Format("Wrong number ({0}) of Templates found for {1}", latReq.Name, templates.Length));

          long templateID = templates[0].id;
          rel.Template = new Reference();
          rel.Template.id = templateID;
          rel.SortRank = "0";
          m_test.m_contService.SaveContentRelations(new PSAaRelationship[] { rel });

          // validate the save operation on templateId
          rel = loadAaRelationshipsByRid(rel.id, true);
          PSFileUtils.RxAssert(rel.Template.id == templateID, "Template did not save");


          // find child items
          FindChildItemsRequest fcreq = new FindChildItemsRequest();
          fcreq.Id                        = ownerId335;
          fcreq.loadOperations            = true;
          fcreq.PSAaRelationshipFilter    = new PSAaRelationshipFilter();
          PSItemSummary[] children        = m_test.m_contService.FindChildItems(fcreq);
          PSFileUtils.RxAssert(children.Length > 0, String.Format("No children found for {0}", fcreq.Id));
          PSFileUtils.RxAssert(children[0].Operation != null, String.Format("Child has no operation for {0}", fcreq.Id));

          for(int inc = 0 ; inc < children.Length ; ++inc) {
              PSFileUtils.RxAssert(children[inc].Operation.Length == 3, String.Format("Child {0} has bad Operation Length {1}",
                                                                              inc, children[inc].Operation.Length));
          }


          // find parent items
          FindParentItemsRequest fpreq = new FindParentItemsRequest();
          fpreq.Id                        = dependentId2;
          fpreq.PSAaRelationshipFilter    = new PSAaRelationshipFilter();
          PSItemSummary[] parents = m_test.m_contService.FindParentItems(fpreq);
          PSFileUtils.RxAssert(parents.Length > 0, String.Format("No parents found for {0}", fcreq.Id));
          PSFileUtils.RxAssert(parents[0].Operation == null, String.Format("Parent has operations for {0}", fcreq.Id));


          // testing deleteContentRelationships
          m_test.m_contService.DeleteContentRelations(createdIds);


          PSFileUtils.RxAssert(loadAaRelationshipsByRid(createdIds[0], false) == null,
                                   String.Format("Unable to delete Content Relationship {0}", createdIds[0]));
          PSFileUtils.RxAssert(loadAaRelationshipsByRid(createdIds[1], false) == null,
                                   String.Format("Unable to delete Content Relationship {0}", createdIds[1]));

          checkinItem(itemStatus335); // cleanup
      }

      /**
       * Test loading the content types.
       *
       */
      public void testLoadContentTypes()
      {

          PSContentTypeSummary[] sums;

          sums = loadContentTypeSummaries(null);

          PSFileUtils.RxAssert(sums.Length > 15, String.Format("Not enough content types found ({0})\n", sums.Length));

          sums = loadContentTypeSummaries(ContentTypeName);

          PSFileUtils.RxAssert(sums.Length == 1, String.Format("Unable to find Content Type {0}\n", ContentTypeName));

          PSFileUtils.RxAssert(sums[0].name == ContentTypeName, String.Format("Name field {0} does not match for Content Type {1}\n", sums[0].name, ContentTypeName));

      }


      /**
       * Test the item creation service.
       *
       */
      public void testCreateItem()
      {


         PSContentTypeSummary[] Types = loadContentTypeSummaries(ContentTypeName);
         PSFileUtils.RxAssert(Types.Length == 1, String.Format("Unable to find Content Type {0}\n", ContentTypeName));

         // by default we create 1 item
         CreateItemsRequest request = new CreateItemsRequest();
         request.ContentType = Types[0].name;
         PSItem[] items = m_test.m_contService.CreateItems(request);
         PSFileUtils.RxAssert(items != null && items.Length == 1, String.Format("Unable to create a content item of type {0}", ContentTypeName));

          // create 3 items
         request = new CreateItemsRequest();
         request.ContentType = Types[0].name;
         request.Count = 3;
         items = m_test.m_contService.CreateItems(request);
         PSFileUtils.RxAssert(items != null && items.Length == 3, String.Format("Unable to create 3 content items of Type {0}", ContentTypeName));

      }

      /**
      * Tests AddFolderTree operation.
      *
      * @throws Exception if any error occurs.
      */
      public void testAddFolderTree()
      {
         // create a test folder
         AddFolderTreeRequest atreq = new AddFolderTreeRequest();
         String path = "//Folders/TestFolder_1_" + DateTime.Now.Ticks + "/TestFolder_2";
         atreq.Path = path;
         PSFolder[] folders = m_test.m_contService.AddFolderTree(atreq);
         PSFileUtils.RxAssert(folders.Length == 2, String.Format("Unable to create folder {0}", path));

         // delete the test folders
          long[] FolderIds = { folders[1].id, folders[0].id };
         DeleteFoldersRequest dreq = new DeleteFoldersRequest();
         dreq.Id = FolderIds;
         m_test.m_contService.DeleteFolders(dreq);

      }

      /**
       * Test loading auto translations settings
       *
       * @throws Exception if the test fails
       */
       public void testAutoTranslations()
       {

           List<PSAutoTranslation> transSettings;

           transSettings = loadAutoTranslations();

           if (transSettings.Count == 0)
           {
               PSTest.ConsoleMessage("Locales needed for test not loaded, skipping test");
               return;
           }

           PSFileUtils.RxAssert(transSettings != null, "Unable to load Translation Settings");
           // No Translations shipped with FF so check to see length is 0
           PSFileUtils.RxAssert(transSettings.Count == 2, String.Format("Error loading Translation Settings, Count = {0}", transSettings.Count));

           String[] testLocale = { "de-ch", "de-it" };

           for (int inc = 0; inc < 2; inc++)
           {
               PSFileUtils.RxAssert(transSettings[inc].communityId == 55834574858);
               PSFileUtils.RxAssert(transSettings[inc].communityName == "Default");
               PSFileUtils.RxAssert(transSettings[inc].contentTypeId == 8589934894);
               PSFileUtils.RxAssert(transSettings[inc].contentTypeName == "rffBrief");
               PSFileUtils.RxAssert(transSettings[inc].locale == testLocale[inc]);
               PSFileUtils.RxAssert(transSettings[inc].workflowId == 98784247812);
               PSFileUtils.RxAssert(transSettings[inc].workflowName == "Simple Workflow");
           }

       }

       /**
        * Test checkin and checkout of items.  Requires a revision exists for fixed
        * item (currently 335).
        *
        * @throws Exception if the test fails.
        */
       public void testContentSOAPCheckInOut()
       {
           PSItemStatus[] itemStatus = null;
           long ctId = getLegacyGuid(335);
           long[] idArr = new long[] { ctId };

           CheckoutItemsRequest checkoutReq = new CheckoutItemsRequest();
           checkoutReq.Id = new long[] { ctId };

           // get into quick edit checked out

           itemStatus = new PSItemStatus[] { checkoutItemGuid(ctId) };

           try
           {
               // test checkin no comment
               CheckinItemsRequest checkinReq = new CheckinItemsRequest();
               checkinReq.Id = idArr;
               String user = "admin1";
               checkinReq.Id = idArr;

               m_test.m_contService.CheckinItems(checkinReq);

               PSFileUtils.RxAssert(didCheckIn(ctId, user, null), String.Format("{0} Item did not check in", checkinReq.Id));

               // test already checked in
               try
               {
                   m_test.m_contService.CheckinItems(checkinReq);
               }
               catch (Exception e)
               {
                   PSFileUtils.RxAssert(false, String.Format("Should not have thrown:", e.Message));
               }

               // test checkout no comment
               checkinReq.Id = idArr;
               m_test.m_contService.CheckoutItems(checkoutReq);
               PSFileUtils.RxAssert(didCheckOut(ctId, user, null), String.Format("{0} Item did not check out", checkinReq.Id));

               // test checkin w/comment
               String comment = "test - " + DateTime.Now.ToShortDateString();
               checkinReq.Id = idArr;
               checkinReq.Comment = comment;
               m_test.m_contService.CheckinItems(checkinReq);
               PSFileUtils.RxAssert(didCheckIn(ctId, user, comment), String.Format("{0} Item did not check in", checkinReq.Id));

               // test checkout w/ comment
               checkoutReq.Id = idArr;
               comment = "test - " + DateTime.Now.ToShortDateString();
               checkoutReq.Comment = comment;
               m_test.m_contService.CheckoutItems(checkoutReq);
               PSFileUtils.RxAssert(didCheckOut(ctId, user, comment), String.Format("{0} Item did not check Out", checkoutReq.Id));

               // test already checked out
               try
               {
                   m_test.m_contService.CheckoutItems(checkoutReq);
               }
               catch (Exception e)
               {
                   PSFileUtils.RxAssert(false, String.Format("{0} Item should not have thrown {1}", checkoutReq.Id, e.Message));
               }
           }
           finally
           {
               checkinItem(itemStatus[0]);
           }
       }

       /**
        * Tests all child entry service functionality.
        *
        * @throws Exception if the test fails.
        */
        public void testContentSOAPChildItems()
        {
            // Cannot be done in .net because we cannot create a Content Type
        }

       /**
        * Test the findRevisions service.  Requires that revisions exist for fixed
        * items (currently 360 and 471).
        *
        * @throws Exception if the test fails
        */
        public void testContentSOAPFindRevisions()
        {
            int[] ids = { 360, 471 };
            long[] guids = new long[ids.Length];

            for (int i = 0; i < ids.Length; i++)
            {
                guids[i] = getLegacyGuid(ids[i]);
            }

            PSRevisions[] revisionsList = m_test.m_contService.FindRevisions(guids);
            PSFileUtils.RxAssert(revisionsList.Length == guids.Length, String.Format("revisionsList.Length ({0}) != guids.Length ({1})", revisionsList.Length, guids.Length));

            for (int i = 0; i < revisionsList.Length; i++)
            {
                PSRevisions revisions = revisionsList[i];
                PSRevision[] revList = revisions.Revisions;
                PSFileUtils.RxAssert(revList.Length == 3, String.Format("revList.Length ({0}) != 3", revList.Length));
                int editRev = revisions.EditRevision;
                PSFileUtils.RxAssert(editRev == -1, String.Format("editRev = {0}", editRev));
                int curRev = revisions.CurrentRevision;
                PSFileUtils.RxAssert(curRev == 3, String.Format("curRev ({0}) != 3", revList.Length));

                for (int j = 0; j < revList.Length; j++)
                {
                    PSRevision rev = revList[i];
                    PSFileUtils.RxAssert(rev.isCurrentRevision == (rev.revision == curRev), String.Format("{0} Current Revision Flags Incorrect", ids[i]));
                    PSFileUtils.RxAssert(rev.isCurrentRevision == (rev.revision == editRev), String.Format("{0} Edit Revision Flags Incorrect", ids[i]));
                    PSFileUtils.RxAssert(!String.IsNullOrEmpty(rev.creator), String.Format("{0} does not have a creator", ids[i]));
                }
            }

            // testing items with empty or null revision list, in other words
            // there is no entries in the contentstatus history table since
            // the item has not been checked in after they are created.
            guids = new long[] { getLegacyGuid(2), getLegacyGuid(3) };

            revisionsList = m_test.m_contService.FindRevisions(guids);
            foreach (PSRevisions rev in revisionsList)
            {
                PSFileUtils.RxAssert(rev.Revisions == null || rev.Revisions.Length == 0, String.Format("{0} has History", rev.CurrentRevision));
            }

            // negative test: should get RemoteException for non-existing id
            try
            {
                guids = new long[] { getLegacyGuid(9999) };
                m_test.m_contService.FindRevisions(guids);
                PSFileUtils.RxAssert(false, "Should have thrown looking for non existent item revisions");
            }
            catch (Exception)
            {
                // expected exception
            }
        }

       /**
        * Test the newCopies service.
        *
        * @throws Exception for any error.
        */
        public void testCreateNewCopies()
        {

            long[] ContentIds = { 367, 368, 369 };

            PSItem[] newCopies1 = null;
            PSItem[] newCopies2 = null;

            try
            {
                long[] ids = new long[ContentIds.Length];

                for (int inc = 0; inc < ContentIds.Length; ++inc)
                {
                    ids[inc] = getLegacyGuid(ContentIds[inc]);
                }

                String[] paths              = { "//Folders/Tests/Copy_1", "//Folders/Tests/Copy_2", "//Folders/Tests/Copy_3" };
                String[] nullPaths          = { null, null, null };

                // create new copies
                NewCopiesRequest request = new NewCopiesRequest();
                request.Ids             = ids;
                request.Paths           = paths;
                request.Type            = m_test.NewCopyRelationshipType;
                request.EnableRevisions = false;

                newCopies1 = m_test.m_contService.NewCopies(request);
                PSFileUtils.RxAssert(newCopies1 != null && newCopies1.Length == ids.Length, "Unable to create new copies");

                verifyFolders(newCopies1, paths, false);

                // create new copies and enable revisions
                request = new NewCopiesRequest();
                request.Ids     = ids;
                request.Paths   = nullPaths;
                request.Type    = m_test.NewCopyRelationshipType;
                request.EnableRevisions = true;

                newCopies2 = m_test.m_contService.NewCopies(request);
                PSFileUtils.RxAssert(newCopies2 != null && newCopies2.Length == ids.Length, "Unable to create new copies");

                verifyFolders(newCopies2, paths, true);
            }
            finally
            {
                deleteNewCopies(newCopies1);
                deleteNewCopies(newCopies2);

                cleanUpFolders("//Folders/Tests");
            }
        }


       /**
        * Test the newPromotableVersions service.
        *
        * @throws Exception for any error.
        */
        public void testCreateNewPromotableVersions()
        {
            PSItem[] ItemArray  = null;
            PSItem[] newCopies = null;
            PSItem[] newCopies2 = null;
            bool foldersCreated = false;
            List<PSItem> items  = null;


            try
            {
                // create 3 test items
                items = createTestItems(3, null, false, out ItemArray);

                long[] ids = toItemIds(items);

                String[] paths = { "//Folders/Tests/NewPromotableVersion_1",
                                    "//Folders/Tests/NewPromotableVersion_2",
                                    "//Folders/Tests/NewPromotableVersion_3" };
                String[] nullPaths = { null, null, null };

                NewPromotableVersionsRequest request = null;

                // create new promotable versions
                request = new NewPromotableVersionsRequest();
                request.Ids             = ids;
                request.Paths           = paths;
                request.Type            = PromotableVersionRelationshipType;
                request.EnableRevisions = false;
                newCopies = m_test.m_contService.NewPromotableVersions(request);

                PSFileUtils.RxAssert(newCopies != null && newCopies.Length == ids.Length, "Unable to create new Promotable Versions");

                verifyFolders(newCopies, paths, false);
                foldersCreated = true;

                // create new promotable versions and enable revisions
                request = new NewPromotableVersionsRequest();
                request.Ids             = ids;
                request.Paths           = nullPaths;
                request.Type            = PromotableVersionRelationshipType;
                request.EnableRevisions = true;
                newCopies2 = m_test.m_contService.NewPromotableVersions(request);

                PSFileUtils.RxAssert(newCopies != null && newCopies.Length == ids.Length, "Unable to create new Promotable Versions");

                verifyFolders(newCopies2, paths, true);
            }

            finally
            {
                // cleanup
                if (newCopies2 != null)
                    deleteNewCopies(newCopies2);
                if (newCopies != null)
                    deleteNewCopies(newCopies);
                if (ItemArray != null)
                deleteNewCopies(ItemArray);

                if(foldersCreated)
                {
                    cleanUpFolders("//Folders/Tests");
                }
            }
        }

       /**
        * Test the newTranslations service.
        *
        * @throws Exception for any error.
        *
        */
        public void testCreateNewTranslations()
        {
            List<PSAutoTranslation> autoTranslations        = loadAutoTranslations();
            List<PSItem>            items                   = null;
            PSItem[]                itemArray               = null;
            PSItem[]                newCopies               = null;



            if (autoTranslations.Count != 2)
            {
                PSTest.ConsoleMessage("AutoTranslations needed for the test are not loaded, skipping test");
                return;
            }

            List<PSWorkflow> workflows = catalogWorkflows();

            PSAutoTranslation[] autoTranslationsArray = new PSAutoTranslation[autoTranslations.Count];

            for (int inc = 0; inc < autoTranslations.Count; ++inc)
            {
                autoTranslationsArray[inc] = autoTranslations[inc];
            }


            try
            {

                // create 3 test items for the new content type
                items = createTestItems(3, null, false, out itemArray);
                long[] ids = toItemIds(items);

                String relationshipType = TranslationType;

                NewTranslationsRequest request = null;

                List<PSItem> allCopies = new List<PSItem>();

                // create new translations for autTranslations[0]
                request = new NewTranslationsRequest();
                request.Ids = ids;
                autoTranslationsArray = new PSAutoTranslation[] { autoTranslations[0] };
                request.AutoTranslations = autoTranslationsArray;
                request.Type = relationshipType;
                request.EnableRevisions = false;
                newCopies = m_test.m_contService.NewTranslations(request);

                PSFileUtils.RxAssert(newCopies != null);
                PSFileUtils.RxAssert(newCopies.Length == ids.Length * autoTranslationsArray.Length);

                for (int inc = 0; inc < newCopies.Length; ++inc)
                {
                    allCopies.Add(newCopies[inc]);
                }


                // create new translations for autoTranslations[1]
                request = new NewTranslationsRequest();
                request.Ids = ids;
                autoTranslationsArray = new PSAutoTranslation[] { autoTranslations[1] };
                request.AutoTranslations = autoTranslationsArray;
                request.Type = relationshipType;
                request.EnableRevisions = true;
                newCopies = m_test.m_contService.NewTranslations(request);
                PSFileUtils.RxAssert(newCopies != null);
                PSFileUtils.RxAssert(newCopies.Length == ids.Length * autoTranslationsArray.Length);

                for (int inc = 0; inc < newCopies.Length; ++inc)
                {
                    allCopies.Add(newCopies[inc]);
                }

                // recreate the translations for all defined autoTranslations
                request = new NewTranslationsRequest();
                request.Ids = ids;
                request.AutoTranslations = null;
                request.Type = relationshipType;
                request.EnableRevisions = false;
                newCopies = m_test.m_contService.NewTranslations(request);
                PSFileUtils.RxAssert(newCopies != null);
                PSFileUtils.RxAssert(newCopies.Length == ids.Length * autoTranslations.Count);

                for (int i = 0; i < newCopies.Length; i++)
                {
                    PSFileUtils.RxAssert(allCopies[i].id == newCopies[i].id);
                    PSFileUtils.RxAssert(allCopies[i].Slots == newCopies[i].Slots);
                    PSFileUtils.RxAssert(allCopies[i].dataLocale == newCopies[i].dataLocale);
                    PSFileUtils.RxAssert(allCopies[i].checkedOutBy == newCopies[i].checkedOutBy);
                }
            }

            finally
            {
                // cleanup
                deleteNewCopies(itemArray);
                deleteNewCopies(newCopies);
            }
        }

       /**
        * Test the item delete service.
        *
        * @throws Exception for any error.
        */
        public void testDeleteContentItems()
        {
            PSItem[] ItemArray  = null;
            List<PSItem> items  = null;


            // create 3 test items
            items = createTestItems(3, null, false, out ItemArray);
            long[] ids = toItemIds(items);

            long[] request = null;

            // delete the created items
            request = ids;
            m_test.m_contService.DeleteItems(request);

            // veify that the items were deleted
            try
            {
                LoadItemsRequest loadRequest = new LoadItemsRequest();
                loadRequest.Id = ids;
                m_test.m_contService.LoadItems(loadRequest);

                PSFileUtils.RxAssert(false, "Items were not deleted");
            }
            catch (Exception e)
            {
                String msg = e.Message;
                /*  Need to fix once the exception bug is fixed. Rx-06-07-0216
                PSErrorResultsFaultServiceCall[] calls = e.getServiceCall();
                for (int i = 0; i < ids.length; i++)
                {
                    assertTrue(calls[i].getResult() == null);
                    assertTrue(calls[i].getError() != null);
                } */

            }

        }

       /**
        * Test the find items webservice.
        *
        * @throws Exception for any error.
        */
        public void testDeleteFolderItem()
        {
            PSItem[] ItemArray = null;
            List<PSItem> items = null;
            bool deleteItem = false;


            try
            {
                // create 1 test items
                items = createTestItems(1, null, false, out ItemArray);
                deleteItem = true;
                long[] ids = toItemIds(items);

                // create a new folder
                AddFolderRequest areq = new AddFolderRequest();
                areq.Path = "//Sites/EnterpriseInvestments/Files";
                areq.Name = "testContentSQAPFolder_1_" + DateTime.Now.Ticks;
                AddFolderResponse aresp = m_test.m_contService.AddFolder(areq);

                PSFolder folder_1 = aresp.PSFolder;

                areq.Name = "testContentSQAPFolder_2_" + DateTime.Now.Ticks;
                aresp = m_test.m_contService.AddFolder(areq);

                PSFolder folder_2 = aresp.PSFolder;

                // add folder children
                AddFolderChildrenRequest acreq = new AddFolderChildrenRequest();
                acreq.Parent = new FolderRef();
                acreq.Parent.Id     = folder_1.id;
                acreq.Parent.Path   = null;
                acreq.ChildIds = new long[] { items[0].id };
                deleteItem = false;
                m_test.m_contService.AddFolderChildren(acreq);

                acreq.Parent.Id     = folder_2.id;
                acreq.Parent.Path   = null;
                m_test.m_contService.AddFolderChildren(acreq);

                DeleteFoldersRequest deleteRequest = new DeleteFoldersRequest();
                deleteRequest.Id        = new long[] { folder_1.id, folder_2.id };
                deleteRequest.PurgItems = true;
                m_test.m_contService.DeleteFolders(deleteRequest);
            }
            finally
            {
                // cleanup
                if (deleteItem)
                    deleteNewCopies(ItemArray);
            }
        }

       /**
        * Test the find items webservice.
        *
        * @throws Exception for any error.
        */
        public void testFindContentItems()
        {
            List<PSItem> items = new List<PSItem>();
            PSItem[] ItemArray = null;
            String testPath = "//folders/testFindContentItems";


            try
            {
                // create 3 test items
                items = createTestItems(3, testPath, false, out ItemArray);
                long[] ids = toItemIds(items);

                FindItemsRequest request = null;
                PSSearch search = new PSSearch();
                search.PSSearchParams = new PSSearchParams();

                // find by sys_contentid = 335
                PSSearchParams searchParams = new PSSearchParams();
                PSSearchField field = new PSSearchField();
                field.name = "sys_contentid";
                field.Value = "335";

                searchParams.Parameter = new PSSearchField[] { field };
                search = new PSSearch();
                search.useExternalSearchEngine = false;
                search.PSSearchParams = searchParams;
                request = new FindItemsRequest();
                request.PSSearch = search;
                PSSearchResults[] results = m_test.m_contService.FindItems(request);

                PSFileUtils.RxAssert(results.Length == 1, String.Format("Item {0} not found.", field.Value));

                // find test item 0
                String titleValue = PSWsUtils.GetFirstFieldValue(items[0], SystemTitleFieldName);

                PSSearchParamsTitle title = new PSSearchParamsTitle();
                title.Value = titleValue;
                searchParams = new PSSearchParams();
                searchParams.ContentType = items[0].contentType;
                searchParams.Title = title;
                search = new PSSearch();
                search.useExternalSearchEngine = false;
                search.PSSearchParams = searchParams;
                request = new FindItemsRequest();
                request.PSSearch = search;
                results = m_test.m_contService.FindItems(request);

                PSFileUtils.RxAssert(results.Length == 1, String.Format("Item {0} not found.", field.Value));

                // find all items in the test folder
                PSSearchParamsFolderFilter folderFilter = new PSSearchParamsFolderFilter();
                folderFilter.Value = testPath;
                searchParams = new PSSearchParams();
                searchParams.ContentType =  items[0].contentType;
                searchParams.FolderFilter = folderFilter;
                search = new PSSearch();
                search.useExternalSearchEngine = false;
                search.PSSearchParams = searchParams;
                request = new FindItemsRequest();
                request.PSSearch = search;
                results = m_test.m_contService.FindItems(request);

                PSFileUtils.RxAssert(results.Length == 3, String.Format("Only {0} test items were found.", results.Length));
            }
            finally
            {
                // cleanup
                if (ItemArray != null)
                {
                    deleteNewCopies(ItemArray);
                    cleanUpFolders(testPath);
                }
            }
        }

       /**
        * Testing Content Folder methods
        *
        * @throws Exception if error occurs.
        */
        public void testFolder()
        {
            // Test operation

            long x = DateTime.Now.Ticks;

            switchToIECommunity();

            LoadFoldersRequest lreq = new LoadFoldersRequest();
            // folders do not have display format id property
            long id2 = getLegacyGuid(2);
            long id3 = getLegacyGuid(3);
            // folders do have display format id property
            long id301 = getLegacyGuid(301);
            long id302 = getLegacyGuid(302);
            lreq.Id = new System.Nullable<long>[] { id2, id3, id301, id302 };
            PSFolder[] folders = m_test.m_contService.LoadFolders(lreq);

            // verify the result
            PSFileUtils.RxAssert(folders.Length == 4, String.Format("Only {0} folders were found.", folders.Length == 4));

            String[] Paths = { "//Sites", "//Folders", "//Sites/EnterpriseInvestments", "//Sites/EnterpriseInvestments/Files" };

            for (int inc = 0; inc < folders.Length; ++inc)
            {
                PSFileUtils.RxAssert(folders[inc].path == Paths[inc], String.Format("Folder[{0}] has wrong path {1}", inc, folders[inc].path));
                PSFileUtils.RxAssert(folders[inc].DisplayFormat.name == "Default", String.Format("Folder[{0}] does not have Default Display Format", inc));
                PSFileUtils.RxAssert(folders[inc].Security[0].permissionRread, String.Format("Folder[{0}] does not have read permission", inc));
            }

            for (int inc = 2; inc < folders.Length; ++inc)
            {
                PSFileUtils.RxAssert(folders[inc].Security[0].name == "EI_Members", String.Format("Folder[{0}] has wrong role {1}", inc, folders[inc].Security[0].name));
                PSFileUtils.RxAssert(!folders[inc].Security[0].permissionAdmin, String.Format("Folder[{0}] has Admin permission", inc));
                PSFileUtils.RxAssert(folders[inc].Security[0].permissionWrite, String.Format("Folder[{0}] does not have write permission", inc));
            }

            // save existing folder
            SaveFoldersResponse sfResp = m_test.m_contService.SaveFolders(new PSFolder[] { folders[3] });
            long[]ids = sfResp.Ids;

            PSFileUtils.RxAssert(ids[0] == folders[3].id, "Save folders failed");

            // create a new folder
            AddFolderRequest areq = new AddFolderRequest();
            areq.Path = "//Sites/EnterpriseInvestments/Files";
            areq.Name = "testContentSQAPFolder" + DateTime.Now.Ticks;
            AddFolderResponse aresp = m_test.m_contService.AddFolder(areq);
            PSFolder folder = aresp.PSFolder;

            // validate the folder property
            long displayId_0 = 133143986176;  // Hard coded because we do not have access to design services
            PSFileUtils.RxAssert(folder.DisplayFormat.id == displayId_0, String.Format("Folder id {0}is not equil to {1}", folder.id, displayId_0));
            PSFileUtils.RxAssert(folder.DisplayFormat.name == "Default", String.Format("Display Format name ({0}) is not Default", folder.DisplayFormat.name));

            String propName = "AdamProperty";
            String propValue = "PropValue";
            String propDesc = "Description";
            // save modified display format
            long displayId_1 = 133143986177;
            folder.DisplayFormat = new Reference();
            folder.DisplayFormat.id = displayId_1;
            folder.DisplayFormat.name = "Simple";
            PSFolderProperty[] props = folder.Properties;
            PSFolderProperty[] propsNew;
            int propsLength = (props == null) ? 1 : props.Length + 1;
            propsNew = new PSFolderProperty[propsLength];
            PSFolderProperty prop = new PSFolderProperty();
            prop.name = propName;
            prop.value = propValue;
            prop.description = propDesc;
            propsNew[propsLength - 1] = prop;
            folder.Properties = propsNew;
            SaveFoldersResponse sfResp2 = m_test.m_contService.SaveFolders(new PSFolder[] { folder });
            long[] savedFolderIds_x = sfResp2.Ids;
            System.Nullable<long>[] savedFolderIds = new System.Nullable<long>[savedFolderIds_x.Length];

            for (int inc = 0; inc < savedFolderIds_x.Length; ++inc)
            {
                savedFolderIds[inc] = savedFolderIds_x[inc];
            }


            // load the saved folder
            lreq.Id = savedFolderIds;
            folders = m_test.m_contService.LoadFolders(lreq);
            PSFileUtils.RxAssert(folders.Length == 1, "Loading Folders Failed");
            folder = folders[0];
            PSFileUtils.RxAssert(folder.DisplayFormat.id == displayId_1, String.Format("Folder id {0}is not equil to {1}", folder.id, displayId_1));
            PSFileUtils.RxAssert(folder.DisplayFormat.name == "Simple", String.Format("Display Format name ({0}) is not Simple", folder.DisplayFormat.name));
            // validate the saved property
            PSFileUtils.RxAssert(folder.Properties.Length == propsLength, "Save property did not load");
            PSFileUtils.RxAssert(prop.name == propName, String.Format("Property Name is wrong ({0})", propName));
            PSFileUtils.RxAssert(prop.value == propValue, String.Format("Property Value is wrong ({0})", propValue));
            PSFileUtils.RxAssert(prop.description == propDesc, String.Format("Property Description is wrong ({0})", propDesc));

            // save folder tree
            AddFolderTreeRequest atreq = new AddFolderTreeRequest();
            atreq.Path = folder.path;
            // add folder tree from existing folder path, no folder created
            folders = m_test.m_contService.AddFolderTree(atreq);
            PSFileUtils.RxAssert(folders.Length == 0, "Add Folder Tree Failed");

            // add 2 new child folders
            atreq.Path = folder.path + "/chidFolder1/ChildFolder2";
            folders = m_test.m_contService.AddFolderTree(atreq);
            PSFileUtils.RxAssert(folders.Length == 2, "Add Folder Tree Failed");

            // find folder ids from folder path
            FindPathIdsRequest ffiReq = new FindPathIdsRequest();
            ffiReq.Path = folders[1].path;
            FindPathIdsResponse fpiResp  = m_test.m_contService.FindPathIds(ffiReq);
            long[] idPath = fpiResp.Ids;
            PSFileUtils.RxAssert(fpiResp.Ids.Length == 6, "Find Path IDs Failed");

            // find folder & item ids from a known path
            ffiReq.Path = "//Sites/EnterpriseInvestments/EI Home Page";
            idPath = m_test.m_contService.FindPathIds(ffiReq).Ids;
            PSFileUtils.RxAssert(idPath.Length == 3);
            PSFileUtils.RxAssert(idPath[0] == -665719930878);
            PSFileUtils.RxAssert(idPath[1] == -665719930579);
            PSFileUtils.RxAssert(idPath[2] == -665719930414);

            // find folder path from content id
            FindFolderPathRequest ffpReq = new FindFolderPathRequest();
            ffpReq.Id = folders[1].id;
            String[] paths = m_test.m_contService.FindFolderPath(ffpReq).Paths;
            // validate the PARENT folder path
            PSFileUtils.RxAssert(paths.Length == 1, "Find Folder Path Failed");
            PSFileUtils.RxAssert(paths[0] == folders[0].path, String.Format("Path {0} != Path {1}", paths[0], folders[0].path));

            // find folder children
            // find folder children by id
            PSItemSummary[] children = findFolderChildren(folders[0].id, null, true);
            PSFileUtils.RxAssert(children.Length == 1, "Find Folder Children Failed");
            PSFileUtils.RxAssert(children[0].objectType == ObjectType.folder, "Children Object Type != Folder Object Type");
            PSFileUtils.RxAssert(children[0].ContentType == null, String.Format("Children Object Type != null ({0})", children[0].ContentType));
            PSFileUtils.RxAssert(children[0].Operation.Length == 2, String.Format("Children Operation != 2 ({0})", children[0].Operation.Length));

            // find folder children by path, but don't load allowed operations
            children = findFolderChildren(null, folders[0].path, false);
            PSFileUtils.RxAssert(children.Length == 1, "Find Folder Children Failed");
            PSFileUtils.RxAssert(children[0].Operation == null, String.Format("Find Folder Children Operation is not null ({0})", children[0].Operation));

            long parentFolderId = folders[1].id;
            String parentFolderPath = folders[1].path;

            // find folder children by id
            children = findFolderChildren(parentFolderId, null, false);
            PSFileUtils.RxAssert(children.Length == 0, "Find Folder Children Failed");
            // find folder children by path
            children = findFolderChildren(null, parentFolderPath, false);
            PSFileUtils.RxAssert(children.Length == 0, "Find Folder Children Failed");

            // add folder children
            AddFolderChildrenRequest acreq = new AddFolderChildrenRequest();
            acreq.Parent = new FolderRef();
            acreq.Parent.Id = parentFolderId;
            acreq.Parent.Path = null;
            long childId_489 = getLegacyGuid(489);
            long childId_490 = getLegacyGuid(490);
            acreq.ChildIds = new long[] { childId_489, childId_490 };
            m_test.m_contService.AddFolderChildren(acreq);

            // create test parent and child folders
            areq = new AddFolderRequest();
            areq.Path = "//Folders";
            areq.Name = "parent_1";
            aresp = m_test.m_contService.AddFolder(areq);
            PSFolder parent_1 = aresp.PSFolder;
            areq.Name = "parent_2";
            aresp = m_test.m_contService.AddFolder(areq);
            PSFolder parent_2 = aresp.PSFolder;
            areq.Path = "//Folders/parent_1";
            areq.Name = "child_1";
            aresp = m_test.m_contService.AddFolder(areq);
            PSFolder child_1 = aresp.PSFolder;

            // try to add same folder to multiple parents
            acreq = new AddFolderChildrenRequest();
            acreq.Parent = new FolderRef();
            acreq.Parent.Id = parent_2.id;
            acreq.Parent.Path = null;
            acreq.ChildIds = new long[] { child_1.id };
            m_test.m_contService.AddFolderChildren(acreq);

            // verify that a new folder was created instead
            lreq = new LoadFoldersRequest();
            lreq.Id = new System.Nullable<long>[] { parent_2.id };
            PSFolder[] parent_2Children = m_test.m_contService.LoadFolders(lreq);
            PSFileUtils.RxAssert(parent_2Children.Length == 1, "Find Folder Children Failed");
            PSFileUtils.RxAssert(parent_2Children[0].id != child_1.id, String.Format("Find Folder Children Id's do not match ({0} {1})", parent_2Children[0].id, child_1.id));

            // delete test parent and child folders
            DeleteFoldersRequest dreq = new DeleteFoldersRequest();
            dreq.Id = new long[] { parent_1.id, parent_2.id };
            m_test.m_contService.DeleteFolders(dreq);

            // validate the inserted child item
            children = findFolderChildren(parentFolderId, null, true);
            PSFileUtils.RxAssert(children.Length == 2, "Find Folder Children Failed");
            foreach (PSItemSummary child in children)
            {
                PSFileUtils.RxAssert(child.objectType == ObjectType.item, "Children Object Type != Folder Object Type");
                PSFileUtils.RxAssert(child.ContentType.name == "rffFile", String.Format("Child Content Type Name ({0}) != rffFile", child.ContentType.name));

                PSFileUtils.RxAssert(child.Operation.Length == 3, String.Format("Children Operation != 3 ({0})", child.Operation.Length));
            }

            // remove all folder children by parent id
            RemoveFolderChildrenRequest rfcreq = new RemoveFolderChildrenRequest();
            rfcreq.Parent = new FolderRef();
            rfcreq.Parent.Id = parentFolderId;
            rfcreq.Parent.Path = null;
            m_test.m_contService.RemoveFolderChildren(rfcreq);
            // validate above removal
            children = findFolderChildren(parentFolderId, null, false);
            PSFileUtils.RxAssert(children.Length == 0, "Children Folder not removed");

            // add folder 2 children again, prepairing the tests below
            acreq = new AddFolderChildrenRequest();
            acreq.Parent = new FolderRef();
            acreq.Parent.Id = parentFolderId;
            acreq.Parent.Path = null;
            acreq.ChildIds = new long[] { childId_489, childId_490 };
            m_test.m_contService.AddFolderChildren(acreq);

            // testing remove folder children by parent Id
            rfcreq = new RemoveFolderChildrenRequest();
            rfcreq.Parent = new FolderRef();
            rfcreq.Parent.Id = parentFolderId;
            rfcreq.Parent.Path = null;
            rfcreq.ChildIds = new long[] { childId_489 };
            m_test.m_contService.RemoveFolderChildren(rfcreq);
            // validate the above remove folder children by parent id
            children = findFolderChildren(parentFolderId, null, false);
            PSFileUtils.RxAssert(children.Length == 1, "Find Folder Children Failed");
            PSFileUtils.RxAssert(children[0].id == childId_490, String.Format("Find Folder Children Id's match ({0} {1})", children[0].id, childId_490));

            // testing remove folder children by parent path
            rfcreq = new RemoveFolderChildrenRequest();
            rfcreq.Parent = new FolderRef();
            rfcreq.Parent.Id = null;
            rfcreq.Parent.Path = parentFolderPath;
            rfcreq.ChildIds = new long[] { childId_490 };
            m_test.m_contService.RemoveFolderChildren(rfcreq);
            // validate the above remove folder children by parent path
            children = findFolderChildren(null, parentFolderPath, false);
            PSFileUtils.RxAssert(children.Length == 0, "Children Folder not removed");

            // delete the saved folder
            dreq = new DeleteFoldersRequest();
            long[] Ids = new long[savedFolderIds.Length];
            for (int inc = 0; inc < savedFolderIds.Length; ++inc)
            {
                Ids[inc] = (long) savedFolderIds[inc];
            }
            dreq.Id = Ids;
            m_test.m_contService.DeleteFolders(dreq);

        }

       /**
        * Tests the GetAssemblyUrl operation
        *
        * @throws Exception if any error occurs.
        */
        public void testGetAssemblyUrl()
        {

            //\/\/\/\/\/\/\/
            // positive test
            //\/\/\/\/\/\/\/
            GetAssemblyUrlsRequest req = new GetAssemblyUrlsRequest();
            req.Context     = 0;
            req.Id          = new long[] { getLegacyGuid(335) };
            req.Template    = "rffPgEiGeneric";
            req.ItemFilter  = "preview";

            GetAssemblyUrlsResponse resp = m_test.m_contService.GetAssemblyUrls(req);
            PSFileUtils.RxAssert(resp.Urls.Length == 1);

            req.ItemFilter = "public";
            resp = m_test.m_contService.GetAssemblyUrls(req);
            PSFileUtils.RxAssert(resp.Urls.Length == 1);

            // set the site
            req.Site = "Enterprise Investments";
            resp = m_test.m_contService.GetAssemblyUrls(req);
            PSFileUtils.RxAssert(resp.Urls.Length == 1);

            // set the folder path
            req.FolderPath = "//Sites/EnterpriseInvestments/AboutEnterpriseInvestments";
            resp = m_test.m_contService.GetAssemblyUrls(req);
            PSFileUtils.RxAssert(resp.Urls.Length == 1);

            //\/\/\/\/\/\/\/
            // negative test
            //\/\/\/\/\/\/\/

            // set to a non-existence template name
            req.Template = "unknown";
            try
            {
                resp = m_test.m_contService.GetAssemblyUrls(req);
                PSFileUtils.RxAssert(false);
            }
            catch (Exception e)
            {
                Exception x = e; // to supress worning
                // should come through here since there is no 'unknown' template
                PSFileUtils.RxAssert(true);
            }

            // set to a non-existence item filter name
            req.ItemFilter = "unknown";
            try
            {
                resp = m_test.m_contService.GetAssemblyUrls(req);
                PSFileUtils.RxAssert(false);
            }
            catch (Exception e)
            {
                Exception x = e; // to supress worning
                // should come through here since there is no 'unknown' item filter
                PSFileUtils.RxAssert(true);
            }

            // set to a non-existence site name
            req.Site = "unknown";
            try
            {
                resp = m_test.m_contService.GetAssemblyUrls(req);
                PSFileUtils.RxAssert(false);
            }
            catch (Exception e)
            {
                Exception x = e; // to supress worning
                // should come through here since there is no 'unknown' site name
                PSFileUtils.RxAssert(true);
            }

            // set to a non-existence folder path
            req.FolderPath = "unknown";
            try
            {
                resp = m_test.m_contService.GetAssemblyUrls(req);
                PSFileUtils.RxAssert(false);
            }
            catch (Exception e)
            {
                Exception x = e; // to supress worning
                // should come through here since there is no 'unknown' folder path
                PSFileUtils.RxAssert(true);
            }

            // set to a non-existing content id
            req.Id = new long[] { getLegacyGuid(335) + 99999 };

            try
            {
                resp = m_test.m_contService.GetAssemblyUrls(req);
                PSFileUtils.RxAssert(false);
            }
            catch (Exception e)
            {
                Exception x = e; // to supress worning
                // should come through here since there is no content id = 99999
                PSFileUtils.RxAssert(true);
            }

        }

       /**
        * Test loading locales
        *
        * @throws Exception if the test fails
        */
        public void testLoadLocales()
        {
            PSLocale[] english;
            PSLocale[] value;
            LoadLocalesRequest req = new LoadLocalesRequest();

            // Look for the English Locale
            req.Code = "en-us";
            english = m_test.m_contService.LoadLocales(req);
            PSFileUtils.RxAssert(english.Length == 1);
            validateLocaleSummaries(english);


            req.Code = null;
            req.Name = null;
            value = m_test.m_contService.LoadLocales(req);

            // If the test locales are here test all the locales

            if (value.Length > 1)
            {
                validateLocaleSummaries(value);
            }

        }

        /**
         *
         * Testing Content Folder methods
         *
         * @throws Exception if error occurs.
         */
         public void testMoveFolderChildren()
         {

            PSFolder srcFolder = null;
            PSFolder tgtFolder = null;

            try
            {
               // prepare data for the testing
               // create source folder
               String folderPath = "//Sites/EnterpriseInvestments/Files/testSourceFolder" + DateTime.Now.Ticks;
               AddFolderTreeRequest atreq = new AddFolderTreeRequest();
               atreq.Path = folderPath;
               PSFolder[] folders = m_test.m_contService.AddFolderTree(atreq);
               srcFolder = folders[0];

               // create target folder
               folderPath = "//Sites/EnterpriseInvestments/Files/testTargetFolder" + DateTime.Now.Ticks;
               atreq = new AddFolderTreeRequest();
               atreq.Path = folderPath;
               folders = m_test.m_contService.AddFolderTree(atreq);
               tgtFolder = folders[0];

               // add folder children
               AddFolderChildrenRequest acreq = new AddFolderChildrenRequest();
               acreq.Parent = new FolderRef();
               acreq.Parent.Id = srcFolder.id;
               acreq.Parent.Path = null;
               long childId_489 = getLegacyGuid(489);
               long childId_490 = getLegacyGuid(490);
               acreq.ChildIds = new long[] { childId_489, childId_490 };
               m_test.m_contService.AddFolderChildren(acreq);

               // moving all items from source -> target folder, by id
               MoveFolderChildrenRequest mfcreq = new MoveFolderChildrenRequest();
               mfcreq.Source = new FolderRef();
               mfcreq.Source.Id = srcFolder.id;
               mfcreq.Source.Path = null;
               mfcreq.Target = new FolderRef();
               mfcreq.Target.Id = tgtFolder.id;
               mfcreq.Target.Path = null;
               m_test.m_contService.MoveFolderChildren(mfcreq);

               // validate the move
               PSItemSummary[] children = findFolderChildren(srcFolder.id, null, false);
               PSFileUtils.RxAssert(children.Length == 0);
               children = findFolderChildren(tgtFolder.id, null, false);
               PSFileUtils.RxAssert(children.Length == 2);

               // moving all items from target -> source folder, by path
               mfcreq = new MoveFolderChildrenRequest();
               mfcreq.Source = new FolderRef();
               mfcreq.Source.Id = null;
               mfcreq.Source.Path = tgtFolder.path;
               mfcreq.Target = new FolderRef();
               mfcreq.Target.Id = null;
               mfcreq.Target.Path = srcFolder.path;
               m_test.m_contService.MoveFolderChildren(mfcreq);

               // validate the move
               children = findFolderChildren(srcFolder.id, null, false);
               PSFileUtils.RxAssert(children.Length == 2);
               children = findFolderChildren(tgtFolder.id, null, false);
               PSFileUtils.RxAssert(children.Length == 0);

               // moving all items from target -> source folder, by path
               mfcreq = new MoveFolderChildrenRequest();
               mfcreq.Source = new FolderRef();
               mfcreq.Source.Id = null;
               mfcreq.Source.Path = srcFolder.path;
               mfcreq.Target = new FolderRef();
               mfcreq.Target.Id = null;
               mfcreq.Target.Path = tgtFolder.path;
               mfcreq.ChildId = new long[] { childId_489 };
               m_test.m_contService.MoveFolderChildren(mfcreq);

               // validate the move
               children = findFolderChildren(srcFolder.id, null, false);
               PSFileUtils.RxAssert(children.Length == 1);
               children = findFolderChildren(tgtFolder.id, null, false);
               PSFileUtils.RxAssert(children.Length == 1);
            }
            finally
            {
               DeleteFoldersRequest dreq = new DeleteFoldersRequest();
               dreq.Id = new long[1];

               // delete the test folders
               if(srcFolder != null)
               {
                  dreq.Id[0] = srcFolder.id;
                  m_test.m_contService.DeleteFolders(dreq);
               }

               // delete the test folders
               if (tgtFolder != null)
               {
                  dreq.Id[0] = tgtFolder.id;
                  m_test.m_contService.DeleteFolders(dreq);
               }
            }
         }

        /**
         * Test promoteRevisions web service.  Requires a revision exists for fixed
         * item (currently 335).
         *
         * @throws Exception If the test fails
         */
       public void testPromoteRevision()
       {
          int contentId = 335;
          long[] promoteReq = new long[] { getLegacyGuid(contentId) };
          switchToIECommunity();

          // prepare test data
          moveItemInPublicState(contentId);

          // find revision to promote
          long[] ids = new long[] { getLegacyGuid(contentId) };
          PSRevisions[] revisionsList = m_test.m_contService.FindRevisions(ids);
          PSFileUtils.RxAssert(revisionsList.Length == ids.Length);
          PSRevisions curRevisions = revisionsList[0];
          int numRevs = curRevisions.Revisions.Length;
          PSFileUtils.RxAssert(numRevs > 2);
          long promoteId = curRevisions.Revisions[numRevs - 1].id;

          // test invalid state
          promoteReq = new long[] { promoteId };
          try
          {
             m_test.m_contService.PromoteRevisions(promoteReq);
             PSFileUtils.RxAssert(false, "Should have thrown");
          }
          catch (Exception e)
          {
             // expected
             Exception x = e; //supress warning
          }

          // edit the current revision to make a change
          PSItemStatus itemStatus = m_test.m_contService.PrepareForEdit(ids)[0];
          PSFileUtils.RxAssert(itemStatus.didCheckout);
          long curId = ids[0];

          // test bad promote attempt on checked out item
          promoteReq = new long[] { curId };
          try
          {
             m_test.m_contService.PromoteRevisions(promoteReq);
             PSFileUtils.RxAssert(false, "Should have thrown");
          }
          catch (Exception e)
          {
             // expected
             Exception x = e; //supress warning
          }

          LoadItemsRequest loadReq = new LoadItemsRequest();
          loadReq.Id = new long[] { curId };

          PSItem item = m_test.m_contService.LoadItems(loadReq)[0];
          PSField modField = null;
          int modFieldIndex = 0;
          foreach (PSField field in item.Fields)
          {
             if (field.dataType == PSFieldDataType.text && field.name == "description")
             {
                modField = field;
                break;
             }
             modFieldIndex++;
          }

          PSFileUtils.RxAssert(modField != null);
          PSFieldValue[] oldVal = modField.PSFieldValue;
          modField.PSFieldValue = new PSFieldValue[] { new PSFieldValue() };
          modField.PSFieldValue[0].RawData = "test:" + DateTime.Now;
          modField.PSFieldValue[0].attachmentId = null;


          SaveItemsRequest saveReq = new SaveItemsRequest();
          saveReq.PSItem = new PSItem[] { item };
          saveReq.Checkin = true;
          m_test.m_contService.SaveItems(saveReq);

          // we should have created a new revision
          numRevs++;
          PSRevisions newRevisions = m_test.m_contService.FindRevisions(ids)[0];
          PSFileUtils.RxAssert(newRevisions.Revisions.Length == numRevs);

          loadReq.Id = new long[] { newRevisions.Revisions[numRevs - 1].id };
          item = m_test.m_contService.LoadItems(loadReq)[0];
          PSField testField = item.Fields[modFieldIndex];
          PSFileUtils.RxAssert(modField.name == testField.name);
          PSFileUtils.RxAssert(oldVal != modField.PSFieldValue);

          // ok, finally, now we can promote
          numRevs++;
          promoteReq = new long[] { promoteId };
          m_test.m_contService.PromoteRevisions(promoteReq);
          ReleaseFromEditRequest rfeReq = new ReleaseFromEditRequest();
          rfeReq.PSItemStatus = new PSItemStatus[] { itemStatus };
          rfeReq.CheckInOnly = false;
          m_test.m_contService.ReleaseFromEdit(rfeReq);
          newRevisions = m_test.m_contService.FindRevisions(ids)[0];
          PSFileUtils.RxAssert(newRevisions.Revisions.Length == numRevs);

          loadReq.Id = new long[] { getLegacyGuid(contentId) }; // Always returns last revision
          item = m_test.m_contService.LoadItems(loadReq)[0];
          testField = item.Fields[modFieldIndex];
          PSFileUtils.RxAssert(modField.name == testField.name);
          PSFileUtils.RxAssert(oldVal != modField.PSFieldValue);

       }

     /**
      * Test the item view service.
      *
      * @throws Exception for any error.
      */
      public void testViewContentItems()
      {
         List<PSItem> items = new List<PSItem>();
         PSItem[] ItemArray = null;

         int itemCount = 3;
         int revisionCount = 3;

         try
         {
            // create 3 test items
            items = createTestItems(3, null, true, out ItemArray);
            long[] ids = toItemIds(items);

            PSRevisions[] revisions = m_test.m_contService.FindRevisions(ids);

            PSFileUtils.RxAssert(items.Count == 3);

            long[] GuidArray = { items[0].id, items[1].id, items[2].id };
            m_test.m_contService.PrepareForEdit(GuidArray);

            revisions = m_test.m_contService.FindRevisions(ids);

            int[] existingRevisions = new int[revisions.Length];

            for (int inc = 0; inc < existingRevisions.Length; ++inc)
            {
               existingRevisions[inc] = revisions[inc].Revisions.Length;
            }


            // create 3 revisions
            createItemRevisions(ItemArray, items, revisionCount);

            // verify that all revisions are created
            revisions = m_test.m_contService.FindRevisions(ids);
            PSFileUtils.RxAssert(revisions != null && revisions.Length == itemCount);
            for (int inc = 0; inc < revisions.Length; ++inc)
            {
               PSFileUtils.RxAssert(revisions[inc].Revisions.Length == revisionCount + existingRevisions[inc]);
            }

            ViewItemsRequest request = null;

            // view all current test items
            request = new ViewItemsRequest();
            request.Id = ids;
            PSItem[] viewItems = m_test.m_contService.ViewItems(request);
            PSFileUtils.RxAssert(viewItems != null && viewItems.Length == ids.Length);

            // get the 2nd revision for all items
            int viewRevision = 2;
            long[] revisionIds = new long[revisions.Length];

            for (int index = 0; index < revisions.Length; ++index )
            {
               revisionIds[index] = revisions[index].Revisions[viewRevision + existingRevisions[index] - 1].id;
            }
            request = new ViewItemsRequest();
            request.Id = revisionIds;
            viewItems = m_test.m_contService.ViewItems(request);
            PSFileUtils.RxAssert(viewItems != null && viewItems.Length == ids.Length);
            verifyRevisions(viewItems, viewRevision);

         }
         finally
         {
            // cleanup
            deleteNewCopies(ItemArray);
         }
      }
   }
}
