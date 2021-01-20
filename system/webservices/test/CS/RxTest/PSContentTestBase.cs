using System;
using System.Collections.Generic;
using System.Text;
using RxTest.RxWebServices;

namespace RxTest
{
    public class PSContentTestBase
    {
        protected PSContentTestBase(PSTest test)
        {
            m_test = test;
        }

        protected void switchToIECommunity()
        {
            SwitchCommunityRequest req = new SwitchCommunityRequest();
            req.Name = "Enterprise_Investments";
            m_test.m_sysService.SwitchCommunity(req);
        }

        protected PSAaRelationship[] loadAaRelationshipsByOwner(int contentId)
        {
            PSAaRelationshipFilter filter = new PSAaRelationshipFilter();
            long legacyGuid = PSGetGuidFromContentID(contentId);
            filter.Owner = legacyGuid;
            filter.limitToOwnerRevisions = true;
            return loadAaRelationships(filter);
        }

        protected PSAaRelationship[] loadAaRelationships(PSAaRelationshipFilter filter)
        {
            LoadContentRelationsRequest lreq = new LoadContentRelationsRequest();
            lreq.PSAaRelationshipFilter = filter;
            lreq.loadReferenceInfo = true;

            return m_test.m_contService.LoadContentRelations(lreq);
        }

        protected PSSearchResults[] PSSearchItemByContentID(long contentId)
        {
            FindItemsRequest    searchRequest   = new FindItemsRequest();
            PSSearch            search          = new PSSearch();
            PSSearchField[]     searchFields     = new PSSearchField[1];

            searchFields[0] = new PSSearchField();
            searchFields[0].name    = "sys_contentid";
            searchFields[0].Value   = string.Format("{0}", contentId);

            PSSearchParams Parms = new PSSearchParams();

            Parms.Parameter = searchFields;

            search.PSSearchParams = Parms;

            searchRequest.PSSearch = search;

            return(m_test.m_contService.FindItems(searchRequest));
        }

        public long PSGetGuidFromContentID(long contentId)
        {
            PSSearchResults[] results = PSSearchItemByContentID(contentId);

            PSFileUtils.RxAssert(results.Length == 1, string.Format("Found {0} content items for content id {1}", results.Length, contentId));

            return (results[0].id);
         }

         // for compatability reasons with Java code
         public long getLegacyGuid(long contentID)
         {
            return (PSGetGuidFromContentID(contentID));
         }

        /**
         * Calls the <code>loadContentTypes</code> webservice.
         *
         * @param name The name to search on, may be <code>null</code> or empty or
         * contain the "*" wildcard.
         * @param session The current session, may be <code>null</code> or empty to
         * test authentication.
         *
         * @return The list of content type summaries, never <code>null</code>.
         *
         */
        protected PSContentTypeSummary[] loadContentTypeSummaries(String name)
        {
           PSContentTypeSummary[] value = null;


           LoadContentTypesRequest req = new LoadContentTypesRequest();

           req.Name = name;

           value = m_test.m_contService.LoadContentTypes(req);


           return value;
        }

        /**
         * Calls the <code>LoadCommunities</code> webservice.
         *
         * @param name The name to search on, may be <code>null</code> or empty or
         * contain the "*" wildcard.
         *
         * @return The list of Communities.
         *
         */
        protected PSCommunity[] LoadCommunitiesByName(String name)
        {
           PSCommunity[] value = null;


           LoadCommunitiesRequest req = new LoadCommunitiesRequest();

           req.Name = name;

           value = m_test.m_secService.LoadCommunities(req);


           return value;
        }

        /**
         * Calls the <code>LoadWorkflows</code> webservice.
         *
         * @param name The name to search on, may be <code>null</code> or empty or
         * contain the "*" wildcard.
         *
         * @return The list of Workflows.
         *
         */
        protected PSWorkflow[] LoadWorkflowsByName(String name)
        {
           PSWorkflow[] value = null;


           LoadWorkflowsRequest req = new LoadWorkflowsRequest();

           req.Name = name;

           value = m_test.m_sysService.LoadWorkflows(req);


           return value;
        }
        protected PSItemStatus checkoutItemLegacy(long contentId)
        {
            long[] GuidArray = { PSGetGuidFromContentID(contentId) };
            return m_test.m_contService.PrepareForEdit(GuidArray)[0];
        }

        protected PSItemStatus checkoutItemGuid(long Guid)
        {
            long[] GuidArray = { Guid };
            return m_test.m_contService.PrepareForEdit(GuidArray)[0];
        }
        protected void checkinItem(PSItemStatus item)
        {
            ReleaseFromEditRequest req = new ReleaseFromEditRequest();
            req.PSItemStatus = new PSItemStatus[] { item };
            m_test.m_contService.ReleaseFromEdit(req);
        }

        protected PSAaRelationship loadAaRelationshipsByRid(long rid, bool isRequired)
        {
            PSAaRelationshipFilter filter = new PSAaRelationshipFilter();
            filter.Id = rid;
            PSAaRelationship[] rels = loadAaRelationships(filter);

            if (isRequired)
                PSFileUtils.RxAssert(rels.Length == 1, String.Format("Unable to load Content Relations for rid {0}, length = {1}", rid, rels.Length));

            return rels.Length == 0 ? null : rels[0];
        }

       /**
        * Calls the <code>loadAutoTranslations</code> webservice.
        *
        * @param session The current session, may be <code>null</code> or empty to
        * test authentication.
        *
        * @return The list of auto translations, never <code>null</code>.
        *
        * @throws Exception If there are any errors.
        */
        protected List<PSAutoTranslation> loadAutoTranslations()
        {
            PSAutoTranslation[] values = null;

            values = m_test.m_contService.LoadTranslationSettings();

            List<PSAutoTranslation> result = new List<PSAutoTranslation>(values.Length);

            foreach (PSAutoTranslation value in values)
            {
                result.Add(value);
            }

            return(result);
        }


       /**
        * Use the audit trail to determine if the item was checked out, assumes item
        * has been public.
        *
        * @param ctId The item content id.
        * @param user The actor, assumed not <code>null</code> or empty.
        * @param comment Optional comment, may be <code>null</code> or empty, used
        * to check if the audit trail comment matches.
        *
        * @return <code>true</code> if the last audit trail entry indicates the item
        * was checked out by the specified user with the specified comment,
        * <code>false</code> if not.
        *
        * @throws Exception If there are any errors.
        */
        public Boolean didCheckOut(long ctId, String user, String comment)
        {
            PSAuditTrail[] trails = m_test.m_sysService.LoadAuditTrails(new long[] { ctId });
            PSFileUtils.RxAssert(trails.Length == 1, String.Format("Wrong number {0} of records returned for Content ID", trails.Length, ctId));
            PSAuditTrail trail = trails[0];
            PSAudit[] audits = trail.Audits;
            int len = audits.Length;
            PSFileUtils.RxAssert(len > 0 , String.Format("No records returned for Content ID {0}", ctId));
            int iAudit = len - 1;
            PSAudit audit = audits[iAudit];

            Boolean isCheckout = audit.actor == user && audit.transitionId == 0;
            // ensure previous entry had same revision, otherwise this is a checkin
            if (isCheckout && iAudit > 1)
            {
                if (audit.revision != audits[iAudit - 1].revision)
                {
                    isCheckout = false;
                }
                else
                {
                    if (comment == null)
                    {
                        comment = "";
                    }
                    String trComment = audit.transitionComment;
                    if (trComment == null)
                    {
                        trComment = "";
                    }
                    if (comment != trComment)
                    {
                        isCheckout = false;
                    }
                }
            }
            else
            {
                isCheckout = false;
            }

            return isCheckout;
        }

       /**
        * Use the audit trail to determine if the item was checked in, assumes item
        * has been public.
        *
        * @param ctId The item content id.
        * @param user The actor, assumed not <code>null</code> or empty.
        * @param comment Optional comment, may be <code>null</code> or empty, used
        * to check if the audit trail comment matches.
        *
        * @return <code>true</code> if the last audit trail entry indicates the item
        * was checked in by the specified user with the specified comment,
        * <code>false</code> if not.
        *
        * @throws Exception If there are any errors.
        */
        protected Boolean didCheckIn(long ctId, String user, String comment)
        {
            PSAuditTrail[] trails = m_test.m_sysService.LoadAuditTrails(new long[] { ctId });
            PSFileUtils.RxAssert(trails.Length == 1, String.Format("Wrong number {0} of records returned for Content ID", trails.Length, ctId));
            PSAuditTrail trail = trails[0];
            PSAudit[] audits = trail.Audits;
            int len = audits.Length;
            PSFileUtils.RxAssert(len > 0, String.Format("No records returned for Content ID {0}", ctId));
            int iAudit = len - 1;
            PSAudit audit = audits[iAudit];

            Boolean isCheckin = audit.actor == user && audit.transitionId == 0;
            // ensure previous entry had lower revision, otherwise this is a checkout
            if (isCheckin && iAudit > 1)
            {
                if (audit.revision != audits[iAudit - 1].revision + 1)
                {
                    isCheckin = false;
                }
                else
                {
                    if (comment == null)
                    {
                        comment = "";
                    }
                    String trComment = audit.transitionComment;
                    if (trComment == null)
                    {
                        trComment = "";
                    }
                    if (comment != trComment)
                    {
                        isCheckin = false;
                    }
                }
            }
            else
            {
                isCheckin = false;
            }
            return (isCheckin);
        }



     /**
      * Convert the supplied items list into an array of item ids.
      *
      * @param items the items to convert, not <code>null</code>, may be empty.
      * @return an array with all item ids, never <code>null</code>, may be empty.
      */
      public long[] toItemIds(List<PSItem> items)
      {
         PSFileUtils.RxAssert(items != null, "Items cannot be null");

         long[] ids = new long[items.Count];
         int index = 0;
         foreach(PSItem item in items)
            ids[index++] = item.id;

         return ids;
      }

     /**
      * Convert the supplied search results into an array of item ids.
      *
      * @param searchResults the search results to convert, assumed not
      *    <code>null</code>, may be empty.
      * @return an array with all items ids, never <code>null</code>, may be
      *    empty.
      */
      private long[] toItemIds(PSSearchResults[] searchResults)
      {
         PSFileUtils.RxAssert(searchResults == null, "Search Results cannot be null");

         long[] ids = new long[searchResults.Length];
         int index = 0;
         foreach(PSSearchResults searchResult in searchResults) {
            ids[index++] = searchResult.id;
         }

         return ids;
      }

    /**
     * Save the items supplied with the request and update the items ids after
     * a successful save.
     *
     * @param request the save request with all items and options to save,
     *    assumed not <code>null</code>.
     * @param binding the binding to use for the save operation, assumed not
     *    <code>null</code>.
     * @return the new ids of the saved items, never <code>null</code> or empty.
     * @throws Exception fof any error.
     */
     private long[] saveItems(SaveItemsRequest request)
     {
        long[] ids = m_test.m_contService.SaveItems(request).Ids;

        // update the inserted items ids
        int index = 0;

        foreach (PSItem item in request.PSItem)
        {
            item.id = ids[index++];
        }

        return ids;
    }
        
   /**
    * Verifies that the supplied items are attached to the paths specified in 
    * the same order.
    * 
    * @param items the items to verify, assumed not <code>null</code>, may be 
    *    empty.
    * @param paths the folder paths to verify, assumed not <code>null</code>
    *    and of same length as items.
    * @param none <code>true</code> to specify that none of the items should
    *    be attached to a folder, <code>false</code> otherwise.
    * @param binding the stub to do the lookup, assumed not <code>null</code>.
    * @throws Exception for any error.
    */
        protected void verifyFolders(PSItem[] items, String[] paths, bool none)
        {
            for (int i = 0; i < items.Length; i++)
            {
                PSItem item = items[i];
                String path = paths[i];

                FindFolderPathRequest request = new FindFolderPathRequest();
                request.Id = item.id;
                FindFolderPathResponse foundPaths = m_test.m_contService.FindFolderPath(request);

                if (none)
                {
                    PSFileUtils.RxAssert(foundPaths.Paths.Length == 0, String.Format("Folder Paths found {0}", foundPaths.Paths.Length));
                }
                else
                {
                    bool found = false;
                    foreach (String foundPath in foundPaths.Paths)
                    {
                        if (foundPath == path)
                        {
                            found = true;
                            break;
                        }
                    }
                    PSFileUtils.RxAssert(found, "No paths found");
                }
            }
        }   

       /**
        * Deletes the specified folder with all its children.
        * 
        * @param path the folder path to delete, may be <code>null</code> or empty.
        * @param session the user session, assumed not <code>null</code> or empty.
        * @throws Exception for any error.
        */
        protected void cleanUpFolders(String path)
        {
            if(!String.IsNullOrEmpty(path))
            {
                LoadFoldersRequest loadRequest = new LoadFoldersRequest();
                loadRequest.Path = new string[] { path };

                PSFolder[] folders = m_test.m_contService.LoadFolders(loadRequest);

                long[] ids = new long[folders.Length];
                int index = 0;
                foreach (PSFolder folder in folders)
                {
                    ids[index++] = folder.id;
                }

                DeleteFoldersRequest deleteRequest = new DeleteFoldersRequest();
                deleteRequest.Id = ids;

                m_test.m_contService.DeleteFolders(deleteRequest);
            }
        }

       /**
        * Creates the specified number of content items for the specified content 
        * type, populates all required field values and saves the items as 
        * requested.
        *  
        * @param count the number of items to create, must be > 0.
        * @param enableRevisions <code>true</code> to enable revisions for all
        *    saved items, <code>false</code> otherwise.
        * @param path the folder path to which to attach the created items, may be 
        *    <code>null</code> or empty.
        * @return a list with all new items created, never <code>null</code> or
        *    empty. The returned items are not persisted yet.
        * @throws Exception for any error.
        */
        public List<PSItem> createTestItems(int count, string path, bool enableRevisions, out PSItem[] ItemArray)
        {
            PSFileUtils.RxAssert(count > 0 && count <= 3, String.Format("Must be at least one and no more then 3", count));

            long[]          ContentIds  = { 367, 368, 369 };
            long[]          ids         = new long[count];
            List<PSItem>    items       = new List<PSItem>();
            string[]        paths       = new string[count];

            for (int inc = 0; inc < count; ++inc)
            {
                paths[inc] = path;
                ids[inc] = getLegacyGuid(ContentIds[inc]);
            }

            ItemArray = null;

            // create new copies
            NewCopiesRequest request = new NewCopiesRequest();
            request.Ids = ids;
            request.Paths = paths;
            request.Type = m_test.NewCopyRelationshipType;
            request.EnableRevisions = enableRevisions;

            ItemArray = m_test.m_contService.NewCopies(request);
            PSFileUtils.RxAssert(ItemArray != null && ItemArray.Length == ids.Length, "Unable to create new copies");

            for (int inc = 0; inc < ItemArray.Length; ++inc)
            {
                items.Add(ItemArray[inc]);
            }

            return items;
        }

        public void deleteNewCopies(PSItem[] newCopies)
        {
            if (newCopies != null)
            {
                // cleanup                 
                long[] DeleteItemsRequest = new long[newCopies.Length];

                int inc1;
                for (inc1 = 0; inc1 < newCopies.Length; ++inc1)
                {
                    DeleteItemsRequest[inc1] = newCopies[inc1].id;
                }

                m_test.m_contService.DeleteItems(DeleteItemsRequest);
            }
        }

       /**
        * Catalog all workflows.
        * 
        * @return all defined workflows in the system, never <code>null</code>
        *    or empty.
        * @throws Exception for any error.
        */
        protected List<PSWorkflow> catalogWorkflows()
        {
            LoadWorkflowsRequest request = new LoadWorkflowsRequest();
            request.Name = "*";
            PSWorkflow[] workflows = m_test.m_sysService.LoadWorkflows(request);

            List<PSWorkflow> workflowsList = new List<PSWorkflow>();

            for(int inc = 0 ; inc < workflows.Length ; ++ inc) 
            {
                workflowsList.Add(workflows[inc]);
            }

            return workflowsList;
        }

       /**
        * Load Folder Children by either parent folder id (if specified) or by
        * parent folder path.
        * 
        * @param parentId the parent folder id, may be <code>null</code> if wanting
        *    to request by folder path.
        * @param parentPath the path of the parent folder, may be <code>null</code>
        *    if wanting to request by folder id.
        * @param isLoadOperations it is <code>true</code> if load the allowed 
        *    operations for the returned summaries; otherwise, the allowed
        *    operations in the returned summaries will be <code>null</code> or 
        *    empty.
        * 
        * @return the child summaries of the specified folder, never 
        *    <code>null</code>, may be empty.
        * 
        * @throws Exception if any error occurs.
        */
        protected PSItemSummary[] findFolderChildren(System.Nullable<long> parentId, String parentPath, bool isLoadOperations)
        {
            FindFolderChildrenRequest fcreq = new FindFolderChildrenRequest();
            fcreq.Folder        = new FolderRef();

            if (parentId != null)
            {
                fcreq.Folder.Id     = parentId;
                fcreq.Folder.Path   = null;
            }
            else
            {
                fcreq.Folder.Id     = null;
                fcreq.Folder.Path   = parentPath;
            }

            fcreq.loadOperations = isLoadOperations;

            return m_test.m_contService.FindFolderChildren(fcreq);
        }

       /**
        * findLocale
        * @param a locale
        * @returns the index in hard coded values
        */
        protected int findLocale(PSLocale locale)
        {
            for(int inc = 0 ; inc <localeCodes.Length ; ++inc)
            {
                if(locale.id == localeIds[inc]) 
                {
                    return(inc);
                }
            }
            PSFileUtils.RxAssert(false);
            return (-1);
        }
       
       /**
        * Validates that the supplied array of locales contains the expected locales
        * to be found.
        * 
        * @param locales The locales to validate, assumed not <code>null</code>.
        */
        protected void validateLocaleSummaries(PSLocale[] locales)
        {
            for( int inc = 0 ; inc < locales.Length ; ++inc)
            {
                int index = findLocale(locales[inc]);

                PSFileUtils.RxAssert(localeCodes[index]  == locales[inc].code);
                PSFileUtils.RxAssert(localeDescription[index] == locales[inc].description);
                PSFileUtils.RxAssert(localeLabel[index]  == locales[inc].label);
            }
        }
                       
        /**
         * Moves the specified item to the public state if it is in Quick-Edit state.
         * Assumes the given item is either in public or quick-edit state.
         *  
         * @param contentId the id of the specified item.
         * 
         * @throws Exception if any error occurs.
         */
         protected void moveItemInPublicState(int contentId)
         {
            PSItemStatus item = checkoutItemLegacy(contentId);

            if(item.didCheckout == false && item.didTransition == false)
            {
               item.didCheckout     = true;
               item.didTransition   = true;
               item.FromState       = new Reference();
               item.FromState.id    = 5;
               item.FromState.name  = "Public";
               item.ToState         = new Reference();
               item.ToState.id      = 6;
               item.ToState.name    = "Quick Edit";
            }
      
            checkinItem(item);
         }
         
        /**
         * Verifies that the supplied items are all for the revision supplied.
         * Assumes that the <code>field_1</code> fiels all represent the items 
         * revosion.
         * 
         * @param items all items to verify, assumed not <code>null</code>, may
         *    be empty.
         * @param revision the revision to verify.
         * @throws Exception for any error.
         */
         protected void verifyRevisions(PSItem[] items, int revision)
         {
            foreach (PSItem item in items)
            {
               PSField[] fields = item.Fields;
               foreach (PSField field in fields)
               {
                  if (field.name.Equals(DisplayTitleFieldName, StringComparison.OrdinalIgnoreCase))
                  {
                     PSFieldValue[] values = field.PSFieldValue;
                     foreach (PSFieldValue value in values)
                     {
                        PSFileUtils.RxAssert(value.RawData.Equals(revision.ToString(), StringComparison.OrdinalIgnoreCase));
                     }
                  }
               }
            }
         }   
                
        /**
         * Creates the specified number of revisions for the supplied items, updates
         * the <code>field_1</code> for each revision. Assumes that revisions 
         * are enabled for all items already.
         * 
         * @param items the items to create revisions for, assumed not 
         *    <code>null</code>, may be empty.
         * @param count the number of revisions to create, assumed >= 0.
         * @return the updated items, never <code>null</code>, may be empty. All
         *    items will be checked out on return.
         * @throws Exception for any error.
         */
         protected List<PSItem> createItemRevisions(PSItem[] itemArray, List<PSItem> items, int count)
         {
            for (int i = 0; i < count; i++)
            {
               // update field_1 for each item
               foreach (PSItem item in items)
               {
                  updateField(item, DisplayTitleFieldName, String.Format("{0}", i + 1));
               }

               // save all changed items
               SaveItemsRequest saveRequest = new SaveItemsRequest();
               saveRequest.PSItem = itemArray;
               long[] ids = saveItems(saveRequest);
               PSFileUtils.RxAssert(ids != null && ids.Length == items.Count);

               String commentString = "Create revision " + i;

               // checkin all items
               CheckinItemsRequest checkinRequest = new CheckinItemsRequest();
               checkinRequest.Id = toItemIds(items);
               checkinRequest.Comment = commentString;
               m_test.m_contService.CheckinItems(checkinRequest);

               // checkout all items
               CheckoutItemsRequest checkoutRequest = new CheckoutItemsRequest();
               checkoutRequest.Id = toItemIds(items);
               checkoutRequest.Comment = commentString;
               m_test.m_contService.CheckoutItems(checkoutRequest);
            }
            return items;
         }

        /**
         * Update the identified field for all supplied items.
         * 
         * @param item the items to update, assumed not <code>null</code>, may
         *    be empty.
         * @param fieldName the name of the field to update, assumed not 
         *    <code>null</code> or empty.
         * @param fieldValue the new field value, may be <code>null</code> or empty.
         */
         protected void updateField(PSItem item, String fieldName, String fieldValue)
         {
            PSField[] fields = item.Fields;
            foreach(PSField field in fields)
            {
               if(field.name.Equals(fieldName, StringComparison.OrdinalIgnoreCase))
               {
                  PSFieldValue[] values = field.PSFieldValue;
                  if (values == null)
                  {
                     values = new PSFieldValue[1];
                     field.PSFieldValue = values;
                  }

                  for(int inc = 0 ; inc < values.Length ; ++ inc)
                  {
                     if (values[inc] == null)
                     {
                        values[inc] = new PSFieldValue();
                     }

                     values[inc].RawData = fieldValue;
               }
               break;
            }
         }
      }

      protected String[] localeCodes  = { "de-ch", "it-ch", "en-us" };
      protected String[] localeLabel  = { "Swiss German", "Swiss Italian", "US English" };
      protected String[] localeDescription = { "", "Italian language used in Switzerland", "English language used in United States of America" };
      protected long[]   localeIds    = { 429496729701, 429496729603, 429496729601 };
      protected PSTest m_test;
      protected String ContentTypeName                    = "rffContacts";
      protected String WorkflowName                       = "Simple Workflow";
      protected String Locale                             = "en-us";
      protected String PromotableVersionRelationshipType  = "PromotableVersion";
      protected String TranslationType                    = "Translation";
      protected String SystemTitleFieldName               = "sys_title";
      protected String DisplayTitleFieldName              = "displaytitle";
   }  
}
