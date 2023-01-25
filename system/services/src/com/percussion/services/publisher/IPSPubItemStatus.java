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

import com.percussion.services.publisher.IPSSiteItem.Operation;
import com.percussion.services.publisher.IPSSiteItem.Status;

import java.util.Date;

/**
 * Each published item is recorded in the database. This object allows that 
 * information to be retrieved.
 * 
 * @author dougrand
 */
public interface IPSPubItemStatus
{

   /**
    * The reference id, this is the unique id per row.
    * @return the referenceId
    */
   public long getReferenceId();

   /**
    * @return the statusId the status, or job id of the run.
    */
   public long getStatusId();

   /**
    * @return the contentId published
    */
   public int getContentId();

   /**
    * @return the revisionId the revision of the item published
    */
   public int getRevisionId();

   /**
    * @return the folderId if present, the folder id published.
    */
   public Integer getFolderId();

   /**
    * @return the templateId used to publish the item.
    */
   public Long getTemplateId();

   /**
    * Get the published location. This location is relative to the publish root
    * of the published site. 
    * @return the location the path of the published item, may be 
    * <code>null</code> (e.g. for DB publish).
    */
   public String getLocation();

   /**
    * @return the date the item was assembled and delivered.
    */
   public Date getDate();

   /**
    * @return the operation 
    */
   public Operation getOperation();

   /**
    * Gets the reference ID of the origin of the unpublishing operation.
    * 
    * @return the unpublish reference ID. It may be <code>null</code> if it is
    * a publishing operation.
    */
   public Long getUnpublishRefId();

   /**
    * The assembly url. This is going to be a synthetic assembly url as the
    * actual assembly is done "internally" via a service call.
    * 
    * @return the assemblyUrl
    */
   public String getAssemblyUrl();

   /**
    * @return the elapsed time in milliseconds
    */
   public Integer getElapsed();

   /**
    * @return the status of the operation
    */
   public Status getStatus();

   /**
    * Is the record hidden from the list view?
    * @return <code>true</code> if this record is hidden from the list view
    */
   public boolean isHidden();

   /**
    * @return the message
    */
   public String getMessage();
   
   /**
    * @return the delivery type used to deliver the content.
    */
   public String getDeliveryType();

}
