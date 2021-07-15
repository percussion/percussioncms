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
package com.percussion.services.publisher;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.percussion.rx.publisher.IPSPublisherItemStatus;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.catalog.IPSCataloger;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.publisher.data.PSContentListItem;
import com.percussion.services.publisher.data.PSContentListResults;
import com.percussion.services.publisher.data.PSItemPublishingHistory;
import com.percussion.services.publisher.data.PSSiteItem;
import com.percussion.services.publisher.data.PSSortCriterion;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;

import javax.jcr.query.QueryResult;

/**
 * Service to do CRUD operations on content lists and run content lists.s
 * 
 * @author dougrand
 * 
 */
public interface IPSPublisherService extends IPSCataloger
{
   /**
    * Create a content list object with the given name. A GUID will be assigned,
    * but the returned instance will be transient until
    * {@link #saveContentLists(List)} has been called.
    * 
    * @param name the name, must be unique when the instance is saved, never
    *            <code>null</code> or empty
    * @return a new instance, never <code>null</code>
    */
   IPSContentList createContentList(String name);

   /**
    * Load the content lists specified by the IDs
    * 
    * @param ids a list of GUIDs, never <code>null</code> or empty
    * @return a corresponding list of instances, never <code>null</code> or
    *         empty. Instances that are not in the database will be returned as
    *         <code>null</code> elements in the list.
    * @throws PSPublisherException if there is a database error
    */
   List<IPSContentList> loadContentLists(List<IPSGuid> ids)
         throws PSPublisherException;

   /**
    * Load the content list specified by the ID. The returned object should be
    * treated as an immutable object and may not be saved by calling
    * {@link #saveContentList(IPSContentList)}.
    * 
    * @param id a GUID of the content list, never <code>null</code>.
    * 
    * @return the Content List instance, never <code>null</code>. 
    * 
    * @throws PSNotFoundException if cannot find the specified Content List.
    */
   IPSContentList loadContentList(IPSGuid id) throws PSNotFoundException;

   /**
    * Load the content list specified by the ID. The returned object can be
    * modified and saved by calling {@link #saveContentList(IPSContentList)}.
    * 
    * @param id a GUID of the content list, never <code>null</code>.
    * @return the Content List instance, never <code>null</code>.
    * 
    * @throws PSNotFoundException if cannot find the specified Content List.
    */
   IPSContentList loadContentListModifiable(IPSGuid id) 
      throws PSNotFoundException;

   /**
    * Load the Content List by the specified name.
    * @param name the name of the Content List, never <code>null</code> or empty.
    * @return the Content List instance, never <code>null</code>.
    * @throws PSNotFoundException if cannot find the specified Content List.
    */
   IPSContentList loadContentList(String name) throws PSNotFoundException;
   
   /**
    * Save the passed content lists to the database. The content lists with
    * <code>null</code> versions will be inserted and the others will be
    * updated.
    * 
    * @param lists a list of content lists, never <code>null</code> or empty
    */
   void saveContentLists(List<IPSContentList> lists);

   /**
    * Save the passed content list to the database. The content list with
    * <code>null</code> version will be inserted and the others will be
    * updated.
    * 
    * @param clist the to be saved content list, never <code>null</code>.
    */
   void saveContentList(IPSContentList clist);

   /**
    * Delete the passed content lists from the database.
    * 
    * @param lists a list of content list objects, never <code>null</code> or
    *            empty
    */
   void deleteContentLists(List<IPSContentList> lists);

   /**
    * Delete the passed status list from the database
    * 
    * @param statusList a list of status objects, never <code>null</code>
    */
   void deleteStatusList(List<IPSPubStatus> statusList);
   
   /**
    * Find the named content list
    * 
    * @param name the name, never <code>null</code> or empty
    * @return the single matching content list. It may be <code>null</code>
    *    if cannot find a Content List with the specified name.
    */
   IPSContentList findContentListByName(String name) throws PSNotFoundException;

   /**
    * Find the content list whose Id matches the given Id
    * 
    * @param contListId the Id of the content list , never <code>null</code> 
    * @return the single matching content list. It may be <code>null</code>
    *    if cannot find a Content List with the specified Id.
    */
   IPSContentList findContentListById(IPSGuid contListID) throws PSNotFoundException;

   /**
    * Get all the content lists, sorted by the passed criteria
    * 
    * @param filter a name filter, only content lists with names that include
    *            the given string will be returned. Equivalent to %filter% in
    *            SQL. never <code>null</code> but can be empty.
    * @return a list of content lists, might be empty, but never
    *         <code>null</code>
    */
  List<IPSContentList> findAllContentLists(String filter);

  /**
   * Get all the content list names, sorted by name
   * 
   * @param filter a name filter, only content lists with names that include
   *            the given string will be returned. Equivalent to %filter% in
   *            SQL. never <code>null</code> but can be empty.
   * @return a list of content lists, might be empty, but never
   *         <code>null</code>
   */
  List<String> findAllContentListNames(String filter);

   /**
    * Get all delivery types.
    * 
    * @return the delivery types, never <code>null</code>, could only be
    *         empty on a system without any configured.
    */
   public List<IPSDeliveryType> findAllDeliveryTypes();

   /**
    * Finds all editions in the specified site that have 1 or more content 
    * list whose generator matches the supplied name.
    * 
    * @param siteId Never <code>null</code>.
    * @param clistGenerator The name of a generator used by content lists. 
    * Never blank. Generally, the fully qualified extension name, e.g.
    * Java/global/percussion/system/sys_SelectedItemsGenerator.
    * 
    * @return Ids of all the editions that match the criteria.
    * 
    * @throws PSPublisherException If any problems with the DB.
    */
   List<IPSGuid> findEditionsBySiteAndContentListGenerator(IPSGuid siteId, 
         String clistGenerator)
         throws PSPublisherException;

   /**
    * Run the content list
    * 
    * @param list the content list to run, never <code>null</code>
    * @param overrides a set of override parameters to apply, never
    * <code>null</code>. It must contain {@link IPSHtmlParameters#SYS_SITEID}
    * parameter as the ID of the publishing Site.
    * @param publish if <code>true</code> the content list is setup for
    * publishing content, if <code>false</code> the content list is setup
    * using existing published locations for the items in order to unpublish
    * correctly
    * @param deliveryContext the context of the delivery
    * @return a list of zero or more content list items corresponding to the
    * content list at the moment the method is called.
    * @throws PSPublisherException if there is a database or other problem
    * running the content list.
    * 
    * @deprecated use
    * {@link #executeContentList(IPSContentList, Map, boolean, IPSGuid, IPSGuid)}
    * instead.
    */
   List<PSContentListItem> executeContentList(IPSContentList list,
         Map<String, String> overrides, boolean publish, int deliveryContext)
         throws PSPublisherException;

   /**
    * Run the content list
    * 
    * @param list the content list to run, never <code>null</code>
    * @param overrides a set of override parameters to apply, it may be
    * <code>null</code>. If it is not <code>null</code>, the parameter of
    * {@link IPSHtmlParameters#SYS_SITEID} will be overridden by the given 
    * site ID, and the parameter of {@link IPSHtmlParameters#SYS_CONTEXT} will
    * be overridden by the given delivery context ID. 
    * @param publish if <code>true</code> the content list is setup for
    * publishing content, if <code>false</code> the content list is setup
    * using existing published locations for the items in order to unpublish
    * correctly
    * @param deliveryContextId the ID of the delivery context, never
    * <code>null</code>.
    * @param siteId the ID of the publishing site, never <code>null</code>.
    * 
    * @return a list of zero or more content list items corresponding to the
    * content list at the moment the method is called.
    * 
    * @throws PSPublisherException if there is a database or other problem
    * running the content list.
    */
   PSContentListResults runContentList(IPSContentList list,
         Map<String, String> overrides, boolean publish,
         IPSGuid deliveryContextId, IPSGuid siteId)
      throws PSPublisherException;
   
   
   /**
    * Executes the content list see.
    * @deprecated Use {@link #runContentList(IPSContentList, Map, boolean, IPSGuid, IPSGuid)}
    * @param list See other method.
    * @param overrides See other method.
    * @param publish See other method.
    * @param deliveryContextId See other method.
    * @param siteId See other method.
    * @return not null maybe empty.
    * @throws PSPublisherException See other method.
    * @see #runContentList(IPSContentList, Map, boolean, IPSGuid, IPSGuid)
    */
   List<PSContentListItem> executeContentList(IPSContentList list,
         Map<String, String> overrides, boolean publish,
         IPSGuid deliveryContextId, IPSGuid siteId)
      throws PSPublisherException;


   /**
    * Create an assembly url appropriate to the passed parameters. Used by the
    * content list generator, but available for other services.
    * 
    * @param host the host name or ip, may be <code>null</code>, defaults to
    *            the server's ip address
    * @param port the host port, may be <code>0</code> which defaults to the
    *            server's http port
    * @param protocol either the value http or https, may be <code>null</code>,
    *            defaults to http.
    * @param siteguid the guid of the site used in assembly, may be
    *            <code>null</code>
    * @param contentid a guid that references the particular content item, never
    *            <code>null</code>, should reference a specific item
    * @param folderguid a guid that references the folder of the item to be
    *            assembled, may be <code>null</code>
    * @param template the template to be used in assembly, never
    *            <code>null</code>
    * @param filter the item filter for use with assembly, never
    *            <code>null</code>
    * @param context the content for the assembly
    * @param publish passes whether this is a publish (<code>true</code>) or
    *            unpublish (<code>false</code>)
    * @return a fully qualified url, never <code>null</code> or empty
    */
   String constructAssemblyUrl(String host, int port, String protocol,
         IPSGuid siteguid, IPSGuid contentid, IPSGuid folderguid,
         IPSAssemblyTemplate template, IPSItemFilter filter, int context,
         boolean publish);

   /**
    * Get the site items that describe the current state of the site with
    * respect to published content
    * 
    * @param siteid the id of the site being published, never <code>null</code>
    * @param deliveryContext the delivery context being published
    * 
    * @return a collection of items, may be empty, but never <code>null</code>
    */
   Collection<IPSSiteItem> findSiteItems(IPSGuid siteid, int deliveryContext);

   /**
    * Get the site items based on the supplied pub-server and delivery context
    * 
    * @param serverId the id of the pub-server, never <code>null</code>
    * @param deliveryContext the delivery context being published
    * 
    * @return the content IDs, may be empty, but never <code>null</code>
    */
   Collection<IPSSiteItem> findSiteItemsByPubServer(IPSGuid serverId, int deliveryContext);


   /**
    * Finds the site items for the specified site, delivery context and content IDs.
    * This method should be used for Oracle database and non-Oracle database should
    * use {@link #findSiteItemsByIds_ReadUncommit(IPSGuid, int, Collection)}.
    * 
    * @param siteid the ID of the site, not <code>null</code>.
    * @param deliveryContext the delivery context that was used to perform the publishing.
    * @param contentIds the content IDs (in UUID format) of the published items, 
    * not <code>null</code> or empty.
    * 
    * @return the site items, it may be empty, but never <code>null</code>.
    */
   Collection<IPSSiteItem> findSiteItemsByIds(IPSGuid siteid,
         int deliveryContext, Collection<Integer> contentIds);
   
   /**
    * Finds the server items for the specified server, delivery context and content IDs.
    * 
    * @param serverid the ID of the server, not <code>null</code>.
    * @param deliveryContext the delivery context that was used to perform the publishing.
    * @param contentIds the content IDs (in UUID format) of the published items, 
    * not <code>null</code> or empty.
    * 
    * @return the site items, it may be empty, but never <code>null</code>.
    */
   Collection<IPSSiteItem> findServerItemsByIds(IPSGuid serverid,
         int deliveryContext, Collection<Integer> contentIds);
   
   /**
    * Finds the site items for the specified site, delivery context and content IDs.
    * This is the same as {@link #findSiteItemsByIds(IPSGuid, int, Collection)},
    * but the transaction of this is using READ_UNCOMMITTED isolation. 
    * This call should be used for non-Oracle database; otherwise this query
    * (on site item table) may cause deadlock with the publishing process
    * that updating or insert rows on the site item table from different threads.
    * 
    * @param siteid the ID of the site, not <code>null</code>.
    * @param deliveryContext the delivery context that was used to perform the publishing.
    * @param contentIds the content IDs (in UUID format) of the published items, 
    * not <code>null</code> or empty.
    * 
    * @return the site items, it may be empty, but never <code>null</code>.
    */
   Collection<IPSSiteItem> findSiteItemsByIds_ReadUncommit(IPSGuid siteid,
         int deliveryContext, Collection<Integer> contentIds);

   /**
    * Gets the site items that have been published to the specified location.
    * 
    * @param siteid the id of the site to which the item was published, never
    *            <code>null</code>
    * @param location relative URL of the published item, never
    *            <code>null</code> or empty
    * 
    * @return a collection of items, may be empty, but never <code>null</code>
    */
   Collection<IPSSiteItem> findSiteItemsAtLocation(IPSGuid siteid,
         String location);

   /**
    * Mark the specified folder IDs to <code>-1</code> for the last published
    * items on the specified site.
    * 
    * @param siteid the id of the site that the folder may belong, 
    * never <code>null</code>
    * @param folderId the (content) IDs of the specified folders, 
    * never <code>null</code>
    * 
    * @return a collection of items, may be empty, but never <code>null</code>
    */
   void markFolderIdsForMovedFolders(IPSGuid siteId, 
         Collection<Integer> folderIds);
   
   /**
    * Finds content IDs where the items were modified since their last publishing.
    * The returned IDs also include unsuccessful published items.
    * 
    * @param siteId the site ID of the items in question, not <code>null</code>.
    * @param deliveryContext the delivery context that was used for publishing 
    * the items.
    * @param cids the content IDs of the items in question, not <code>null</code>.
    * 
    * @return the content IDs of the items were modified since their last publishing,
    * never <code>null</code>, but may be empty.
    */
   Collection<Integer> findItemsSinceLastPublish(
         IPSGuid siteId, int deliveryContext, Collection<Integer> cids);
   
   /**
    * Touch the specified items
    * 
    * @param cids the content items to update, never <code>null</code>
    */
   void touchContentItems(Collection<Integer> cids);

   /**
    * Touch items for a given set of content types, plus affected parent items
    * through AA relationships.
    * 
    * @param ctypeids the content type IDs, never <code>null</code>
    * @return a collection of affected items content IDs
    */
   Collection<Integer> touchContentTypeItems(Collection<IPSGuid> ctypeids);

   /**
    * Get items for a given set of content types, plus affected parent items
    * through AA relationships.
    * 
    * @param ctypeids the content type IDs, never <code>null</code>
    * @return a collection of affected items content IDs
    */
   Collection<Integer> getContentTypeItems(Collection<IPSGuid> ctypeids);

   /**
    * Touch active assembly related items
    * 
    * @param cids the content item guids to start with, never <code>null</code>
    * @return a collection of affected items content ids
    */
   Collection<Integer> touchActiveAssemblyParentsByGuids(
         Collection<IPSGuid> cids);

   /**
    * Touch active assembly related items
    * 
    * @param cids the content item content ids to start with, never
    *            <code>null</code>
    * @return a collection of affected items content ids
    */
   Collection<Integer> touchActiveAssemblyParents(Collection<Integer> cids);

   /**
    * Touch the specified items and their related active assembly parent items
    * 
    * @param cids the content item content IDs to start with, never
    *            <code>null</code>
    * @return a collection of affected items content IDs
    */
   Collection<Integer> touchItemsAndActiveAssemblyParents(Collection<Integer> cids);


   /**
    * Process a demand publish run. This happens when a user right clicks on a
    * group of items or folders, or during a preview. The ids are stored
    * internally to the service and may be retrieved by calling
    * {@link #getDemandPublishIds(int)}. The ids remain valid until the edition
    * has finished running.
    * 
    * @param ids an array of content ids to publish. These may be references to
    * folders, content items or a mix. Never <code>null</code> or empty.
    * @param parentFolderId the parent folder id, used for reference in creating
    * the content list for items, not important when folders are selected, never
    * <code>null</code> or empty.
    * @param edition the edition id as a string, never <code>null</code> or
    * empty
    * @param wait if <code>true</code> then this method will not return until
    * publishing is complete
    * @return <code>true</code> if the demand publish succeeds,
    * <code>false</code> otherwise
    * @deprecated use the business publisher instead,
    * {@link com.percussion.rx.publisher.IPSRxPublisherService#queueDemandWork(int, com.percussion.rx.publisher.data.PSDemandWork)}
    */
   boolean executeDemandPublish(String ids[], String parentFolderId,
         String edition, boolean wait) throws PSNotFoundException;

   /**
    * Fetch the last content list's content item element count. This method will
    * block until the first content list has been fetched. After the first
    * content list has been fetch, this method will return immediately with the
    * most recent content list's count. It is primarily meant for demand
    * publishing.
    * 
    * @param editionid the edition, never <code>null</code> or empty
    * @return the count of <code>contentitem</code> elements in the content
    *         list or <code>0</code> if there is a problem such as a timeout
    *         waiting for the information.
    *         
    * @deprecated this has been disabled, and is no longer relevant, 
    * use the business publisher for publishing job manipulation.
    */
   int getCurrentContentListItemCount(String editionid);

   /**
    * Retrieve ids stored by a previous call to
    * {@link #executeDemandPublish(String[], String, String, boolean)}.
    * 
    * @param edition the edition that is being published
    * @return an array of ids, <code>null</code> if the edition is not known
    * 
    * @deprecated use the business publisher instead
    */
   int[] getDemandPublishIds(int edition);

   /**
    * Retrieve folder id stored by a previous call to
    * {@link #executeDemandPublish(String[], String, String, boolean)}. Valid
    * until the edition has been run.
    * 
    * @param edition the edition that is being published
    * @return a single folder id
    * 
    * @deprecated use the business publisher instead 
    */
   int getDemandFolderId(int edition);


   /**
    * Load the requested Edition, using cached copy if possible. This is a fast
    * call, but will return a shared instance that must not be modified. The
    * returned object is a complete tree that includes all aggregated
    * objects, if any.
    * <p>
    * If you need to modify the returned object, you must call
    * {@link #loadEditionModifiable(IPSGuid)} instead for that object.
    * 
    * @param editionId the edition id, never <code>null</code>.
    * 
    * @return the referenced edition, never <code>null</code>.
    *         
    * @throws PSNotFoundException if the Edition does not exist.
    */
   IPSEdition loadEdition(IPSGuid editionId) throws PSNotFoundException;
   
   /**
    * This method is used to load an Edition if it needs to be modified.
    * 
    * @param id the Edition id, never <code>null</code>.
    * 
    * @return the Edition object, never <code>null</code>.
    *         
    * @throws PSNotFoundException if the Edition does not exist.
    */
   IPSEdition loadEditionModifiable(IPSGuid id) throws PSNotFoundException;

   /**
    * Create a new Edition/Content-List association object. A GUID will be
    * assigned, but the instance will otherwise be uninitialized.
    * 
    * @return the created object, never <code>null</code>.
    */
   IPSEditionContentList createEditionContentList();
   
   /**
    * Load the content list descriptors associated with the given edition.
    * 
    * @param editionId the edition id, never <code>null</code>
    * @return the list of associated content lists, could be empty but not
    *         <code>null</code>.
    */
   List<IPSEditionContentList> loadEditionContentLists(IPSGuid editionId);
   
   /**
    * Save the given edition content lists.
    * 
    * @param list the edition content list to save, never <code>null</code>.
    */
   void saveEditionContentList(IPSEditionContentList list);
   
   /**
    * Delete the given edition content list
    * 
    * @param list a single content list to remove, never <code>null</code>.
    */
   void deleteEditionContentList(IPSEditionContentList list);

   /**
    * Load the specified delivery type.
    * 
    * @param dtypeName the delivery type name, never <code>null</code> or 
    *    empty.
    *    
    * @return the specified delivery type, never <code>null</code>.
    * 
    * @throws PSNotFoundException if the delivery type does not exist.
    */
   IPSDeliveryType loadDeliveryType(String dtypeName)
      throws PSNotFoundException;

   /**
    * Load the specified delivery type. It returns a cached object, which is
    * used for read only and cannot be modified and saved by calling
    * {@link #saveDeliveryType(IPSDeliveryType)}. 
    * 
    * @param id the ID of the location, never <code>null</code>.
    * @return the cached delivery type, never <code>null</code>.
    * 
    * @throws PSNotFoundException if the delivery type does not exist.
    */
   IPSDeliveryType loadDeliveryType(IPSGuid id) throws PSNotFoundException;

   /**
    * Load the specified delivery type. The returned object can be modified and 
    * saved by calling {@link #saveDeliveryType(IPSDeliveryType)}. 
    * 
    * @param id the ID of the location, never <code>null</code>.
    * @return the delivery type, never <code>null</code>.
    * 
    * @throws PSNotFoundException if the delivery type does not exist.
    */
   IPSDeliveryType loadDeliveryTypeModifiable(IPSGuid id) 
      throws PSNotFoundException;

   /**
    * Update the publishing information given the data in the status object.
    * The status of the same Content Item may be updated more than ones. 
    * However, the Content-ID must exist when it is saved at the 1st time.
    * 
    * @param stati a list of status objects, never <code>null</code> or empty.
    *            Any status items that are not in a persistable state 
    *            (see {@link  IPSPublisherJobStatus.ItemState#isPersistable})
    *            ignored with a warning message.
    */
   void updatePublishingInfo(List<IPSPublisherItemStatus> stati);

   /**
    * Update the publish date (system) field of the items which were successfully
    * published in the specified publish job. The publish date will be the
    * starting date of the specified job.
    * 
    * @param jobId the publish job ID.
    */
   void updateItemPubDateByJob(long jobId, Date date);
   

   /**
    * This method is called at the start of a publishing run. It saves the
    * status record, which is used to gather all the publication records for
    * individual items. The initial record has the start time and edition. A
    * later call to {@link #finishedPublishingStatus(long, Date,
    * IPSPubStatus.EndingState)} will store the end time in the database for the
    * status, as well as any other calculations we need to do at the end of the
    * publishing run.
    * 
    * @param statusid the status id, must be unique.
    * @param start the start time and date for the edition run, never
    *            <code>null</code>.
    * @param edition the edition guid, never <code>null</code>.
    */
   void initPublishingStatus(long statusid, Date start, IPSGuid edition) throws PSNotFoundException;

   /**
    * This method is called at the end of a publishing run. It updates the
    * status record with the end time, as well as any other processing needed at
    * the end of the publishing run.
    * 
    * @param statusid the status id
    * @param end the end time and date for when the edition completed.
    * @param endingStatus the ending status of the publishing job. Never 
    *    <code>null</code>.
    */
   void finishedPublishingStatus(long statusid, Date end,
         IPSPubStatus.EndingState endingStatus);

   /**
    * Save the given delivery type. If the delivery type already exists then it
    * updates the given delivery type.
    * 
    * @param deliveryType the delivery type to save, never <code>null</code>.
    */
   void saveDeliveryType(IPSDeliveryType deliveryType);

   /**
    * Delete the given deliver type. If the delivery type doesn't exist then
    * this method will do nothing.
    * 
    * @param deliveryType the delivery type to delete, never <code>null</code>.
    */
   void deleteDeliveryType(IPSDeliveryType deliveryType);

   /**
    * Create a new, un-persisted delivery type.
    * 
    * @return a new delivery location, never <code>null</code>.
    */
   IPSDeliveryType createDeliveryType();

   /**
    * Find all editions associated with the given site.
    * 
    * @param siteId the ID of the site to filter editions on, 
    *    never <code>null</code>.
    * @return a list of editions, may be empty but never <code>null</code>.
    */
   List<IPSEdition> findAllEditionsBySite(IPSGuid siteId);

   /**
    * Find all editions associated with the given pub server.
    * 
    * @param pubServerId the ID of the pub server to filter editions on, 
    *    never <code>null</code>.
    * @return a list of editions, may be empty but never <code>null</code>.
    */
   List<IPSEdition> findAllEditionsByPubServer(IPSGuid pubServerId);

   /**
    * Get all the editions, sorted by the passed criteria
    * 
    * @param filter a name filter, only content lists with names that include
    *            the given string will be returned. Equivalent to %filter% in
    *            SQL with case-insensitive. never <code>null</code> but can be 
    *            empty.
    * @return a list of content lists, might be empty, but never
    *         <code>null</code>
    * @throws PSPublisherException if there is a database problem
    */   
   List<IPSEdition> findAllEditions(String filter);
   
   /**
    * Find all content lists associated with the given site. Content lists are
    * associated with a site by benefit of being used in one or more editions in
    * the site.
    * 
    * @param siteId the site ID to filter content lists on, 
    *    never <code>null</code>.
    * @return a list of content lists, may be empty but never <code>null</code>.
    */
   List<IPSContentList> findAllContentListsBySite(IPSGuid siteId) throws PSNotFoundException;

   /**
    * Save the given edition. If the edition already exists then update the
    * edition in the database. .
    * 
    * Save the supplied Edition to persistent storage. Associated tasks must be 
    * saved separately. The Edition must have been created using 
    * {@link #loadEditionModifiable(IPSGuid)} or {@link #createEdition()}. 
    * If the object is found in the cache, an exception is thrown.
    * <p>
    * Note to implementers: if the object is found in the cache (use ==), that
    * object must be flushed from the cache.
    * 
    * @param edition the edition to save, never <code>null</code>.
    */
   void saveEdition(IPSEdition edition);

   /**
    * Delete the given edition. If the edition doesn't exist then this method
    * does nothing. Deleting an edition will delete the associated tasks.
    * 
    * @param edition the edition to delete, never <code>null</code>.
    */
   void deleteEdition(IPSEdition edition);

   /**
    * Create a new uninitialized edition task. The task will have an 
    * assigned task id, but will not be persisted until saved.
    * 
    * @return the new edition task, never <code>null</code>.
    */
   IPSEditionTaskDef createEditionTask();

   /**
    * Find an edition task from the database. It will be more usual to load
    * tasks using {@link #loadEditionTasks(IPSGuid)}.
    * 
    * @param id the id of the task to load.
    * 
    * @return the task data or <code>null</code> if the task data isn't found.
    */
   IPSEditionTaskDef findEditionTaskById(IPSGuid id) throws PSNotFoundException;

   /**
    * Save an edition task. The save method will assign ids to the task and
    * parameters as required. Empty parameters will be removed.
    * 
    * @param task the task to save, never <code>null</code>.
    */
   void saveEditionTask(IPSEditionTaskDef task);

   /**
    * Delete an edition task and it's associated parameters.
    * 
    * @param task the task to delete, never <code>null</code>.
    */
   void deleteEditionTask(IPSEditionTaskDef task);

   /**
    * Load the edition tasks that are associated with the given edition.
    * 
    * @param editionid the GUID of the edition, never <code>null</code>.
    * @return the edition task list, never <code>null</code> but could be
    *         empty. It is (ascending) ordered by the sequence property of task,
    *         see {@link IPSEditionTaskDef#getSequence()}.
    */
   List<IPSEditionTaskDef> loadEditionTasks(IPSGuid editionid);

   /**
    * Create a new edition instance. A GUID will be assigned, but the instance
    * will otherwise be uninitialized. Note that the instance will be transient
    * until it is saved.
    * 
    * @return the new uninitialized edition, never <code>null</code>.
    */
   IPSEdition createEdition();

   /**
    * Create a new edition log entry. Log entries are used to record the running
    * of edition tasks.
    * 
    * @return a newly created instance, which will have the reference id set,
    *         never <code>null</code>.
    */
   IPSEditionTaskLog createEditionTaskLog();

   /**
    * Find the entries for a given job id.
    * 
    * @param jobid the job id
    * @return the ordered list of entries, with reference ids in an increasing
    *         order.
    */
   List<IPSEditionTaskLog> findEditionTaskLogEntriesByJobId(long jobid);

   /**
    * Save or update the given log object.
    * 
    * @param log the object to save, never <code>null</code>.
    */
   void saveEditionTaskLog(IPSEditionTaskLog log);

   /**
    * Load the given entry.
    * 
    * @param referenceId the entry to load, never <code>null</code>.
    * @return the entry, or <code>null</code> if the entry does not exist.
    */
   IPSEditionTaskLog loadEditionTaskLog(long referenceId);

   /**
    * Remove records from the status, task and document logs that belong to the
    * given job id. Records in the document log that are used by PSX_SITE_ITEM
    * will be kept, but the job id will be removed. Records that have no job id,
    * i.e. it is null, and have no reference from the site items will be
    * removed, not just the records associated with the jobid.
    * 
    * @param jobid the job id to purge jobs for
    */
   void purgeJobLog(long jobid);

   /**
    * Sets the status of the job items to {@link IPSSiteItem.Status#CANCELLED}
    * for the items that are neither in {@link IPSSiteItem.Status#SUCCESS} or
    * {@link IPSSiteItem.Status#FAILURE} status.
    * <br/><br/>
    * Note: this is OK to do only on pub docs table.  Siteitem table does not
    * require to be marked as cancelled as the site item entry doesn't exist yet
    * at this point.  That will get updated on successful delivery or unpublish.
    *    
    * @param jobId the ID of the cancelled job.
    */
   void cancelUnfinishedJobItems(long jobId);

   /**
    * Find jobs that were executed before the given date. This is used in
    * conjunction with {@link #purgeJobLog(long)} to remove old pub logs from
    * the database.
    * 
    * @param beforeDate the criteria date, never <code>null</code>.
    * @return a list of the jobs from before the given date as determined from
    *         the status table.
    */
   List<Long> findExpiredJobs(Date beforeDate);
   
   /**
    * Find jobs that were executed before the given date or have been marked
    * as hidden. This is used in conjunction with {@link #purgeJobLog(long)}
    * to remove old pub logs from the database.
    * 
    * @param beforeDate the criteria date, never <code>null</code>.
    * @return a list of the jobs from before the given date or hidden as
    *         determined from the status table.
    */
   List<Long> findExpiredAndHiddenJobs(Date beforeDate);

   /**
    * Finds the reference IDs for the given job. The order of the returned IDs 
    * is defined by the the parameter of sorts. 
    * order 
    * 
    * @param jobid the job ID.
    * @param sorts the sort criteria that determines the order of the returned
    *    reference IDs.
    * 
    * @return the reference IDs for the supplied publishing job. It may be 
    *    <code>null</code> or empty if not found.
    */
   List<Long> findRefIdForJob(long jobid, List<PSSortCriterion> sort);

   /**
    * 
    * See {@link #findPubItemStatusForJobIterable(long)}.
    * 
    * @param jobid the job id
    * @return a list of results, never <code>null</code> but could be empty if
    *         the job id does not correspond to any results.
    * @deprecated Please use {@link #findPubItemStatusForJobIterable(long)} at is memory efficient.
    */
   List<IPSPubItemStatus> findPubItemStatusForJob(long jobid);

   /**
    * Get the item status for the job in a block. This is used for edition tasks
    * that wish to know the status information for the edition. For example, a
    * post task might want to move published files across a number of
    * destination servers, this information could tell such a task what items
    * were published successfully and where they were published.
    * 
    * @param jobid
    * @return an Iterable that you can use to get an iterator of the job status.
    * Neither the return Iterable or the {@link Iterable#iterator()} will be null.
    */
   Iterable<IPSPubItemStatus> findPubItemStatusForJobIterable(long jobid);
   
   /**
    * Finds the last successful publish status for the specified item.
    * 
    * @param id the ID of the item in question, never <code>null</code>.
    * 
    * @return the published status of the specified item. It may be
    * <code>null</code> if the item has not been published successfully.
    */
   public IPSPubItemStatus findLastPublishedItemStatus(IPSGuid id);
   
   /**
    * Finds the last successful publish status for the specified item for a specified server
    * 
    * @param id the ID of the item in question, never <code>null</code>.
    * @param serverId must not be <code>null</code>
    * 
    * @return the published status of the specified item. It may be
    * <code>null</code> if the item has not been published successfully.
    */
   public IPSPubItemStatus findLastPublishedItemStatus(IPSGuid id, Long serverId);
   
   /**
    * Finds the publish history for the specified item.
    * 
    * @param id the ID of the item in question, never <code>null</code>.
    * 
    * @return the publishing history of the specified item. It may be
    * <code>empty</code> if the item has never been published.
    */
   public List<PSItemPublishingHistory> findItemPublishingHistory(IPSGuid id);
   
   /**
    * Find and return all the completed job information for a given edition.
    * 
    * @param edition the edition guid, never <code>null</code>.
    * @return a list of prior job status data in descending start date order,
    *         but could be empty.
    */
   List<IPSPubStatus> findPubStatusByEdition(IPSGuid edition);
   
   /**
    * Find and return the most recent completed job information for a given edition.
    * 
    * @param editionId The edition guid, not <code>null</code>.
    * 
    * @return The status, may be <code>null</code> if the edition has not been run to completion.
    */
   IPSPubStatus findLastPubStatusByEdition(IPSGuid editionId);

   /**
    * Find and return all the completed job information for a given site.
    * 
    * @param siteId the ID of the site, never <code>null</code>.
    * @return a list of prior job status data in descending start date order,
    *         but could be empty.
    */
   List<IPSPubStatus> findPubStatusBySite(IPSGuid siteId);
   
   /**
    * Find and return all the completed job information for a given site.
    * Differs from above method in that it allows for filtering of items in
    * the executed query to improve performance time when loading pub logs.
    * 
    * @param siteId the ID of the site, never <code>null</code>.
    * @param numDays the number of days to include as a date range when querying pub status entries, never <code>null</code>.
    * 
    * @param maxCount the max number of entries to return included as a limit on the query.
    * @return a list of prior job status data in descending start date order,
    *         but could be empty.
    */
   List<IPSPubStatus> findPubStatusBySiteWithFilters(IPSGuid siteId, int numDays, int maxCount);
   
   /**
    * Find and return all the completed job information for a given site and server name.
    * 
    * @param siteId the ID of the site, never <code>null</code>.
    * @param serverId the ID of the publish server, can be <code>null</code>.
    * @return a list of prior job status data in descending start date order,
    *         but could be empty.
    */
   List<IPSPubStatus> findPubStatusBySiteAndServer(IPSGuid siteId, IPSGuid serverId);
   
   /**
    * Find and return all the completed job information for a given site and server name.
    * Differs from above method in that it allows for filtering within the SQL query
    * as opposed to doing the filtering for numDays and the count after returning the results
    * of the query.
    * 
    * @param siteId the ID of the site, never <code>null</code>.
    * @param serverId the ID of the publish server, can be <code>null</code>.
    * @param numDays the number of days to include as a date range when querying pub status entries, never <code>null</code>.
    * @param maxCount the max number of entries to return included as a limit on the query.
    * @return a list of prior job status data in descending start date order,
    *         but could be empty.
    */
   List<IPSPubStatus> findPubStatusBySiteAndServerWithFilters(IPSGuid siteId, IPSGuid serverId, int numDays, int maxCount);

   /**
    * Finds total number of last published items for a given site and a list of items.
    * 
    * @param siteId the site ID in question, not <code>null</code>.
    * @param contentIds the IDs of content items in question, not <code>null</code> or empty.
    * 
    * @return the total number of last published items.
    */
   int findLastPublishedItemsBySite(IPSGuid siteId, Collection<Integer> contentIds);
   
   /**
    * Find and return all the completed job information for all sites.
    * 
    * @return a list of prior job status data in descending start date order,
    *         but could be empty.
    */
   List<IPSPubStatus> findAllPubStatus();
   
   /**
    * Find and return all the completed job information for all sites.
    * Includes filters to be able to restrict the amount of data that comes back.
    * 
    * @param numDays the number of days to include as a date range when querying pub status entries, never <code>null</code>.
    * @param maxCount the max number of entries to return included as a limit on the query.
    * @return a list of prior job status data in descending start date order,
    *         but could be empty.
    */
   List<IPSPubStatus> findAllPubStatusWithFilters(int days, int maxCount);

   /**
    * Find the edition for a given name or display-title
    * 
    * @param name the name or display-title of the searched edition. 
    *    Never <code>null</code> or empty.
    *    
    * @return the edition, or <code>null</code> if it cannot be found.
    */
   IPSEdition findEditionByName(String name);
   
   /**
    * Find the edition for a given job (status) id.
    * 
    * @param jobid the job id
    * @return the edition GUID, or <code>null</code> if it cannot be found.
    */
   IPSGuid findEditionIdForJob(long jobid);

   /**
    * @param jobid the job to retrieve the status for
    * @return the status, never <code>null</code>
    */
   IPSPubStatus findPubStatusForJob(long jobid);

   /**
    * Remove all site items for the given site. This will also clear any
    * dependencies between the site items and publications log.
    * 
    * @param siteguid the guid of the site, never <code>null</code>
    */
   void deleteSiteItems(IPSGuid siteguid);

   /**
    * Find what known site items, identified by reference id, should be
    * unpublished. Items are selected if:
    * <ul>
    * <li>The content is no longer in the system.
    * <li>The content is no longer in the published folder.
    * <li>The content is now in an archive state as configured for the system.
    * </ul>
    * 
    * @param siteId the ID of the site, never <code>null</code>.
    * @param unpublishFlags the unpublishing flags. It is one or more 
    * characters corresponding to content valid flags used in the workflow, 
    * multiples should be separated by commas. Never <code>null</code> or empty.
    * 
    * @return a list of IDs to unpublish, may be empty but not <code>null</code>.
    */
   List<Long> findReferenceIdsToUnpublish(IPSGuid siteId, String unpublishFlags);


   /**
    * Find what known server items, identified by reference id, should be
    * unpublished. Items are selected if:
    * <ul>
    * <li>The content is no longer in the system.
    * <li>The content is no longer in the published folder.
    * <li>The content is now in an archive state as configured for the system.
    * </ul>
    * 
    * @param serverId the ID of the server, never <code>null</code>.
    * @param unpublishFlags the unpublishing flags. It is one or more 
    * characters corresponding to content valid flags used in the workflow, 
    * multiples should be separated by commas. Never <code>null</code> or empty.
    * 
    * @return a list of IDs to unpublish, may be empty but not <code>null</code>.
    */
   List<Long> findReferenceIdsToUnpublishByServer(IPSGuid serverId, String unpublishFlags);

   /**
    * Find the status records for the given reference ids.
    * 
    * @param refs the reference ids, never <code>null</code>.
    * @return the status records, never <code>null</code>.
    */
   List<IPSPubItemStatus> findPubItemStatusForReferenceIds(List<Long> refs);

   /**
    * Find the site items for the given reference ids.
    * 
    * @param refs the reference ids, never <code>null</code>.
    * @return the site item records, never <code>null</code>.
    */
   List<PSSiteItem> findSiteItemsForReferenceIds(List<Long> refs);

   /**
    * Find the unpublishing data corresponding to the given parameters of an 
    * assembly item. Used specifically for legacy content lists doing 
    * unpublishing. We must use the location specified to lookup the 
    * data since the folder is not (necessarily) supplied.
    * 
    * @param contentId the content ID, never <code>null</code>.
    * @param contextId the delivery context ID.
    * @param templateId the template ID, never <code>null</code>.
    * @param siteId the site ID, never <code>null</code>.
    * @param serverId the publish server ID, it may be <code>null</code> if not defined.
    * @param targetPath the target path when the item was originally published, 
    *    or <code>null</code> if there wasn't one (e.g. for DB publish).
    *    Note, this was returned from the location generator, which is relative
    *    to the publish root of the site. 
    * 
    * @return the info, or <code>null</code> if the information cannot be 
    * determined. The first element in the information is the delivery type 
    * name, the second is the reference ID and the third is the unpublishing 
    * info as a <code>byte[]</code> array. The unpublishing information may be 
    * <code>null</code>. The fourth element is the folder id set by the 
    * published item.
    */
   public Object[] findUnpublishInfoForAssemblyItem(IPSGuid contentId,
         IPSGuid contextId, IPSGuid templateId, IPSGuid siteId, Long serverId,
         String targetPath);

   /**
    * Find all the content lists that are not associated with some edition in
    * some site.
    * 
    * @return the list of such content lists, may be empty but not 
    * <code>null</code>.
    */
   List<IPSContentList> findAllUnusedContentLists();

   /**
    * Gets the server id used to differentiate different publish job status
    * that could be happening on multiple servers/pub-hubs.
    * @return never null.
    * @see IPSPubStatus#getServer()
    */
   public String getServerId();

   /**
    * Calculates the PubStatus counts and update the database.
    * Multiple calls are safe and  are not cumulative 
    * (counts won't be added together).
    * 
    * @param statusid pub status id.
    * @return the pub status with current counts.
    */
   public IPSPubStatus updateCounts(long statusid);
   
   /**
    * Returns true if the site has been published.
    * 
    * @param siteId the ID of the site, never <code>null</code>.
    * @return <code>true</code> if the site has been published.
    */
   boolean isSitePublished(IPSGuid siteId);

   List<PSContentListItem> getContentListItems(IPSContentList list,
         IPSTemplateExpander expander, QueryResult result, boolean publish,
         IPSGuid siteId, IPSGuid deliveryContextId,
         Map<String, String> expparams, Map<String, String> overrideParams) throws PSPublisherException;

    public void  updatePubLogHidden(PSPubServer psPubServer);

}
