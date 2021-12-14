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
