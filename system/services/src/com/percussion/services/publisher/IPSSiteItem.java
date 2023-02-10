/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.services.publisher;

import com.percussion.services.publisher.data.PSPubItem;
import com.percussion.services.publisher.data.PSSiteItem;

import java.util.Date;

/**
 * A site item represents the state of a single published item to a site. Site
 * items as shown by this interface are actually composites of 
 * {@link PSSiteItem} and {@link PSPubItem}. These are joined on the 
 * reference id. However, updates to {@link PSSiteItem} must take into account
 * that new records from {@link PSPubItem} must be joined as updates occur.
 * 
 * @author dougrand
 *
 */
public interface IPSSiteItem
{
   /**
    * The operation that was performed on the item
    */
   public enum Operation 
   {
      /**
       * The item was published
       */
      PUBLISH,
      /**
       * The item was unpublished
       */
      UNPUBLISH;
   }
   
   /**
    * The status of the operation, note that the order here must match
    * the above order for operation so that the value for publish and success
    * match, and unpublish and failure match.
    */
   public enum Status
   {
      /**
       * The operation succeeded
       */
      SUCCESS,
      
      /**
       * The operation failed
       */
      FAILURE,
      
      /**
       * The item has been published or unpublished due to the job has been
       * cancelled by user.
       */
      CANCELLED,
      
      /**
       * Generally, the item is still being processed. It may be waiting in a Q
       * or waiting to be committed. However, if the server happened to abort in
       * the middle of a job, then the items would be orphaned.
       */
      UNDEFINED;
   }
   
   /**
    * Get the site id
    * @return the site id
    */
   long getSiteId();

   /**
    * Gets the status (or publishing job) ID.
    * @return the ID of the publishing run.
    */
   long getStatusId();
   
   /**
    * Set a new site id
    * @param siteid the site id
    */
   void setSiteId(long siteid);
   
   /**
    * Get the published item's content id
    * @return the content id
    */
   Integer getContentId();

   /**
    * Set the published item's content id
    * @param contentid the content id
    */
   void setContentId(Integer contentid);
   
   /**
    * Get the publishing context
    * @return the context
    */
   Integer getContext();

   /**
    * Set the publishing context
    * @param context the context
    */
   void setContext(Integer context);
   
   /**
    * Get the published template id
    * @return the template id
    */
   Long getTemplateId();
   
   /**
    * Set the published template id
    * @param templateid the new template id
    */
   void setTemplateId(Long templateid);
   
   /**
    * Get the hibernate version value, only used for hibernate and should be 
    * generally ignored
    * @return the versionid of the site item
    */
   Integer getVersionId();

   /**
    * Set unknown
    * @param versionid
    */
   void setVersionId(Integer versionid);

   /**
    * The revision of the published content item
    * @return the revision
    */
   Integer getRevisionId();

   /**
    * Set the revision
    * @param revisionid
    */
   void setRevisionId(Integer revisionid);

   /**
    * Get the publication date
    * @return the publication date
    */
   Date getDate();

   /**
    * Set the publication date
    * @param pubdate the publication date
    */
   void setDate(Date pubdate);

   /**
    * Get the operation, either publish
    * @return the operation
    */
   Operation getOperation();

   /**
    * Set the operation
    * @param puboperation the operation
    */
   void setOperation(Operation puboperation);

   /**
    * Get the status
    * @return the status
    */
   Status getStatus();

   /**
    * Set the status
    * @param pubstatus the status
    */
   void setStatus(Status pubstatus);

   /**
    * Get the location of the published item
    * @return the published item
    */
   String getLocation();

   /**
    * Set the location of the published item
    * @param location the location
    */
   void setLocation(String location);

   /**
    * @return the folder id, or <code>null</code> if no folder id was set.
    */
   Integer getFolderId();
   
   /**
    * Set the folder id
    * @param id the folder id, or <code>null</code> if none used.
    */
   void setFolderId(Integer id);
   
   /**
    * Get the content url
    * @return the content url
    */
   String getContentUrl();

   /**
    * Set the content url
    * @param contenturl the content url
    */
   void setContentUrl(String contenturl);

   /**
    * Get the elapsed time for the assembly
    * @return the elapsed time in milliseconds
    */
   Integer getElapsedTime();

   /**
    * Set the elapsed time
    * @param elapsetime the elapsed time in milliseconds
    */
   void setElapsedTime(Integer elapsetime);

   /**
    * @return the actual site item record
    */
   PSSiteItem getSiteItem();
   
   /**
    * @return the delivery type
    */
   String getDeliveryType();

   /**
    * Gets the page number of the item. It is <code>1</code>
    * base number for paginated items, but it is always
    * <code>0</code> for non-paginated items. 
    * 
    * @return the page number.
    */
   int getPage();
}
