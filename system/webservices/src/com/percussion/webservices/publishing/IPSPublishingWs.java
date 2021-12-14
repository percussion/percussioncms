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
package com.percussion.webservices.publishing;

import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSPublishingJobStatusCallback;
import com.percussion.rx.publisher.data.PSDemandWork;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.publisher.IPSContentList;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionContentList;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSPubStatus;
import com.percussion.services.sitemgr.IPSPublishingContext;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;

import java.util.List;

/**
 * This interface defines all publishing related webservices.
 */
public interface IPSPublishingWs
{
   /**
    * Create a content list object with the given name. A GUID will be assigned,
    * but the returned instance will be transient until
    * {@link #saveContentList(IPSContentList)} has been called.
    * 
    * @param name the name, must be unique when the instance is saved, never
    *  <code>null</code> or empty.
    * @return a new instance, never <code>null</code>.
    */
   public IPSContentList createContentList(String name);
   
   /**
    * Find the content list whose Id matches the given Id.
    * 
    * @param contListId the Id of the content list , never <code>null</code>.
    * @return the single matching content list. It may be <code>null</code> if
    *  a Content List with the specified Id cannot be found.
    */
   public IPSContentList findContentListById(IPSGuid contListId);
   
   /**
    * Load the Content List by the specified name.
    * 
    * @param name the name of the Content List, never <code>null</code> or
    *  empty.
    * @return the Content List instance, never <code>null</code>.
    * 
    * @throws PSErrorException if cannot find the specified Content List.
    */
   public IPSContentList loadContentList(String name)
      throws PSErrorException;
   
   /**
    * Save the passed content list to the database. The content list with
    * <code>null</code> version will be inserted and the others will be
    * updated.
    * 
    * @param clist the to be saved content list, never <code>null</code>.
    */
   public void saveContentList(IPSContentList clist);
   
   /**
    * Delete the passed content lists from the database.
    * 
    * @param lists a list of content list objects, never <code>null</code> or
    *  empty.
    */
   public void deleteContentLists(List<IPSContentList> lists);
   
   /**
    * Delete the passed edition content lists from the database.
    * 
    * @param edtContentList a edition content list, never <code>null</code> or
    *  empty.
    */
   public void deleteEditionContentList(IPSEditionContentList edtContentList);
   
   /**
    * Delete the passed status list from the database
    * 
    * @param statusList a list of status objects, never <code>null</code>
    */
   void deleteStatusList(List<IPSPubStatus> statusList);
   
   /**
    * Create a new edition instance. A GUID will be assigned, but the instance
    * will otherwise be uninitialized. Note that the instance will be transient
    * until it is saved.
    * 
    * @return the new uninitialized edition, never <code>null</code>.
    */
   public IPSEdition createEdition();
   
   /**
    * Find all editions associated with the given site.
    * 
    * @param siteId the ID of the site to filter editions on, never
    *  <code>null</code>.
    * @return a list of editions, may be empty but never <code>null</code>.
    */
   public List<IPSEdition> findAllEditionsBySite(IPSGuid siteId);
   
   /**
    * Find all editions associated with the given publish server.
    * 
    * @param pubServerId the ID of the publish server to filter editions on, never
    *  <code>null</code>.
    * @return a list of editions, may be empty but never <code>null</code>.
    */
   public List<IPSEdition> findAllEditionsByPubServer(IPSGuid pubServerId);
   
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
    * Save the given edition. If the edition already exists then update the
    * edition in the database.
    * 
    * Save the supplied Edition to persistent storage. Associated tasks must be 
    * saved separately. The Edition must have been created using 
    * {@link #createEdition()}. If the object is found in the cache, an
    * exception is thrown.
    * <p>
    * Note to implementers: if the object is found in the cache (use ==), that
    * object must be flushed from the cache.
    * 
    * @param edition the edition to save, never <code>null</code>.
    */
   public void saveEdition(IPSEdition edition);
   
   /**
    * Delete the given edition. If the edition doesn't exist then this method
    * does nothing. Deleting an edition will delete the associated tasks.
    * 
    * @param edition the edition to delete, never <code>null</code>.
    */
   void deleteEdition(IPSEdition edition);
   
   /**
    * Create a new Edition/Content-List association object. A GUID will be
    * assigned, but the instance will otherwise be uninitialized.
    * 
    * @return the created object, never <code>null</code>.
    */
   public IPSEditionContentList createEditionContentList();
   
   /**
    * Load the content list descriptors associated with the given edition.
    * 
    * @param editionId the edition id, never <code>null</code>.
    * @return the list of associated content lists, could be empty but not
    *  <code>null</code>.
    */
   public List<IPSEditionContentList> loadEditionContentLists(
         IPSGuid editionId);
   
   /**
    * Save the given edition content lists.
    * 
    * @param list the edition content list to save, never <code>null</code>.
    */
   public void saveEditionContentList(IPSEditionContentList list);
   
   /**
    * Loads one filter by name. The returned filter should be considered
    * read-only as this method uses an in-memory cache that is shared between
    * threads.
    * 
    * @param name name of filter, never <code>null</code> or empty.
    * @return the item filter, never <code>null</code>.
    * @throws PSErrorException if no filter is found with the given name.
    */
   public IPSItemFilter findFilterByName(String name) throws PSErrorException;
   
   /**
    * Create a new site object and initialize for the database.
    * 
    * @return the new site object, never <code>null</code>.
    */
   public IPSSite createSite();
   
   /**
    * Load a site object by the site name.
    * 
    * @param sitename the name of the site, never <code>null</code> or empty.
    * @return the site, never <code>null</code>.
    * 
    * @throws PSErrorException if the site does not exist.
    */
   public IPSSite findSite(String sitename) throws PSErrorException;
   
   /**
    * Find a site by an id.
    * @param siteId never <code>null</code>.
    * @return never <code>null</code>.
    * @throws PSErrorException
    */
   public IPSSite findSiteById(IPSGuid siteId) throws PSErrorException;
   
   /**
    * Find and return all the sites the item exists in. The sites are ordered by
    * the name of the sites it exists in.
    * 
    * @param contentId the content GUID, never <code>null</code>.
    * @return the sites the item exists in never <code>null</code> may be
    *         empty, if the item does not exist in any site.
    */
   public List<IPSSite> getItemSites(IPSGuid contentId);
   
   /**
    * Save or update the site in the database.
    * 
    * @param site the site, never <code>null</code>.
    */
   public void saveSite(IPSSite site);
   
   /**
    * Delete the site from the database.
    * 
    * @param site the site, never <code>null</code>.
    */
   public void deleteSite(IPSSite site);
   
   /**
    * Remove all site items for the given site. This will also clear any
    * dependencies between the site items and publications log.
    * 
    * @param siteguid the guid of the site, never <code>null</code>
    */
   public void deleteSiteItems(IPSGuid siteguid);
   
   /**
    * Find a publishing context by the specified name.
    * 
    * @param contextname the name of the context, never <code>null</code> or
    *  empty
    * @return the desired context, never <code>null</code>.
    * @throws PSErrorException if there is no context with the given name.
    */
   public IPSPublishingContext loadContext(String contextname)
      throws PSErrorException;
   
   /**
    * Find and load all available sites
    * 
    * @return a list of Site objects, probably not empty, never
    *         <code>null</code>
    */
   public List<IPSSite> findAllSites();
   
   /**
    * Find and return all the completed job information for a given site.
    * 
    * @param siteId the ID of the site, never <code>null</code>.
    * @return a list of prior job status data in descending start date order,
    *         but could be empty.
    */
   public List<IPSPubStatus> findPubStatusBySite(IPSGuid siteId);
   
   /**
    * Find and return all the completed job information for a given edition.
    * 
    * @param editionId the ID of the site, never <code>null</code>.
    * @return a list of prior job status data in descending start date order,
    *         but could be empty.
    */
   public List<IPSPubStatus> findPubStatusByEdition(IPSGuid editionId);
   
   /**
    * Remove records from the status, task and document logs that belong to the
    * given job id. Records in the document log that are used by PSX_SITE_ITEM
    * will be kept, but the job id will be removed. Records that have no job id,
    * i.e. it is null, and have no reference from the site items will be
    * removed, not just the records associated with the jobid.
    * 
    * @param jobid the job id to purge jobs for
    */
   public void purgeJobLog(long jobid);
   
   /**
    * Queues one or more content items for publishing using the given edition.
    * If the edition is already running, the items are queued and will be
    * published the next time the edition runs. The queue operation returns
    * an opaque identifier that can be used to query the status of the job. 
    * The status is retained for a long period of time and is then discarded.
    * <p>
    * The status of this work is unknown if the work has not been started.
    * 
    * @param editionid the edition id, must exist
    * @param work the work to publish, never <code>null</code>.
    * 
    * @return an opaque id that can be used to check the status of the
    *   request.
    */
   public long queueDemandWork(int editionid, PSDemandWork work);
   
   /**
    * Find and return the matching job for the given request id issued for 
    * a demand work item. The value <code>null</code> will be returned if the
    * request is not yet part of a job.
    * 
    * @param requestid the request id
    * @return the job id, or <code>null</code> if the job is unknown.
    */
   public Long getDemandRequestJob(long requestid);
   
   /**
    * Start processing the given edition. This will cause a separate thread
    * to be spawned that is processing the edition asynchronously. The returned
    * job id can be used to query status and control the job.
    * <p>
    * If the edition is already being run then an 
    * {@link IllegalStateException} will be thrown.
    * 
    * @param edition the id of the edition to run, never <code>null</code>
    * @param callback to be notified object when the job is finished. It may be
    *    <code>null</code> if the notification is not needed.
    * 
    * @return the job id
    */
   public long startPublishingJob(IPSGuid edition,
         IPSPublishingJobStatusCallback callback);
   
   /**
    * Get the current state of the job.
    * 
    * @param jobId the jobId, must match existing jobId or an
    *            {@link IllegalStateException} will be thrown.
    * @return a copy of the current status. The returned object is newly created
    *         for each call.
    */
   public IPSPublisherJobStatus getPublishingJobStatus(long jobId);
   
   /**
    * Gets the current in progress publishing jobs for a specified site.
    * 
    * @param siteName never blank.
    * 
    * @return list of in progress job id's, never <code>null</code>, may be empty.
    */
   public List<Long> getInProgressPublishingJobs(String siteName);
   
   /**
    * Create a new uninitialized edition task. The task will have an 
    * assigned task id, but will not be persisted until saved.
    * 
    * @return the new edition task, never <code>null</code>.
    */
   public IPSEditionTaskDef createEditionTask();

   /**
    * Find an edition task from the database.
    * 
    * @param id the id of the task to load.
    * 
    * @return the task data or <code>null</code> if the task data isn't found.
    */
   public IPSEditionTaskDef findEditionTaskById(IPSGuid id);

   /**
    * Save an edition task. The save method will assign ids to the task and
    * parameters as required. Empty parameters will be removed.
    * 
    * @param task the task to save, never <code>null</code>.
    */
   public void saveEditionTask(IPSEditionTaskDef task);

   /**
    * Delete an edition task and it's associated parameters.
    * 
    * @param task the task to delete, never <code>null</code>.
    */
   public void deleteEditionTask(IPSEditionTaskDef task);
   
   /**
    * Gets the edition tasks for a specified edition id.
    * 
    * @param editionid never blank.
    * 
    * @return list of edition tasks, never <code>null</code>, may be empty.
    */
   public List<IPSEditionTaskDef> loadEditionTaskByEdition(IPSGuid editionid);
}

