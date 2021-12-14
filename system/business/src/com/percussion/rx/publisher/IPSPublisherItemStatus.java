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
package com.percussion.rx.publisher;

import com.percussion.utils.guid.IPSGuid;

import java.util.Date;

/**
 * Report the status for a single item being published. Sent by the delivery
 * handler and assembly handler as items are completed or fail.
 * <p>
 * When this message is sent for the delivered or failed case, the message is
 * used to update the site items and pub docs table. These are messages where
 * the state from {@link #getState()} is "terminal" and will therefore never be
 * updated further.
 * 
 * @author dougrand
 */
public interface IPSPublisherItemStatus
{
   /**
    * The reference id identifies a particular publishing request to the system.
    * Each unique item to be delivered will have a different reference id.
    * 
    * @return the referenceId, which identifies a particular item to be
    *         assembled and delivered to the publishing system.
    */
   public abstract long getReferenceId();

   /**
    * The job id identifies a particular run of an edition. The job id is
    * allocated at the beginning of the job and is used to group all the logging
    * records together in the database.
    * 
    * @return the job id
    */
   public abstract long getJobId();

   /**
    * Gets the publish-server ID that is used by the publishing process.
    * @return the publish-server ID. It may be <code>null</code> if there is no publish-server used during publishing this item.
    */
   public abstract Long getPubServerId();
   
   /**
    * Sets the publish-server ID that is used by the publishing process.
    * @param id the publish-server ID. It may be <code>null</code> if the publishing edition does not relate to a publish-server.
    */
   public abstract void setPubServerId(Long id);
   
   /**
    * The items go through various states as they are assembled and delivered.
    * 
    * @return the state from this update, never <code>null</code>
    */
   public abstract IPSPublisherJobStatus.ItemState getState();

   /**
    * Get the assembly url. This is the url originally calculated in the content
    * list. This information is saved to allow the user to debug publishing
    * issues.
    * 
    * @return the assembly url, never <code>null</code> or empty if the state
    *         in the message is a terminal state.
    */
   public abstract String getAssemblyUrl();

   /**
    * Get the elapsed assembly time in milliseconds.
    * 
    * @return the assembly time, may be <code>0</code> if the state in the
    *         message is not a terminal state.
    */
   public abstract int getElapsed();

   /**
    * Get the GUID of the content item.
    * 
    * @return the content item GUID, never <code>null</code> if the state in
    *         the message is a terminal state.
    */
   public abstract IPSGuid getId();

   /**
    * If available, get the folder id that holds the item being published.
    * 
    * @return the folder id, or <code>null</code> if not specified.
    */
   public abstract IPSGuid getFolderId();

   /**
    * Is this a status from a publish request?
    * 
    * @return <code>true</code> for a publish request and <code>false</code>
    *         for an unpublish request.
    */
   public abstract boolean isPublish();

   /**
    * Get the published location.
    * 
    * @return the published location, never <code>null</code> or empty if the
    *         state in the message is a terminal state.
    */
   public abstract String getPublishedLocation();

   /**
    * Get the published template GUID.
    * 
    * @return the published template GUID, never <code>null</code> if the
    *         state in the message is a terminal state.
    */
   public abstract IPSGuid getTemplateId();

   /**
    * Get the published date.
    * 
    * @return the published date, never <code>null</code> if the state in the
    *         message is a terminal state.
    */
   public abstract Date getPublishedDate();

   /**
    * Get the site id for the published item.
    * 
    * @return the site id, never <code>null</code> if the state in the message
    *         is a terminal state.
    */
   public abstract IPSGuid getSiteId();

   /**
    * The publishing (delivery) context.
    * 
    * @return the context, set if the state in the message is a terminal state.
    */
   public abstract int getDeliveryContext();

   /**
    * Get one or more informational or failure messages associated with the item
    * status.
    * 
    * @return the message, messages or <code>null</code> if there are no
    *         messages.
    */
   public String[] getMessages();

   /**
    * For delivery types that can unpublish, this can return data to be passed
    * back when the item is unpublished. This may be as simple as the file
    * system path, or complex such as a primary key and table combination. This
    * information is opaque to the publishing system.
    * 
    * @return the unpublishing information, may be <code>null</code> if the
    *         delivery type handler doesn't support unpublishing.
    */
   public byte[] getUnpublishingInformation();

   /**
    * Get the reference ID that the unpublishing was originated from.
    * @return the reference ID. It may be <code>null</code> if 
    *    {@link #isPublish()} returns <code>true</code>.
    */
   public Long getUnpublishRefId();

   /**
    * Get the delivery type, which dictates what delivery handler is being used
    * to deliver the content.
    * 
    * @return the delivery type, never <code>null</code> or empty.
    */
   public abstract String getDeliveryType();
   
   /**
    * If this status is for a paged item's page, then this will return the page
    * number. Page numbers are <code>1</code> based. If the status contains
    * a page number, it will also contain a parent reference id.
    * 
    * @return the page number or <code>null</code> if there is no page number.
    */
   Integer getPage();
   
   /**
    * If this status is for a paged item's page, then this will return the 
    * original paginated page's reference id so that status update can mark
    * the parent as failed if the child page fails.
    * 
    * @return the parent reference id or <code>null</code> if this is not
    * a page child status.
    */
   Long getParentPageReferenceId();
}
