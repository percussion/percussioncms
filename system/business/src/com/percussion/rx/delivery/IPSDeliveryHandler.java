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
package com.percussion.rx.delivery;

import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.sitemgr.IPSSite;

import java.util.Collection;
import java.util.List;

/**
 * A delivery handler is responsible for delivering assembled content to a 
 * location determined by a location scheme or by the assembly process itself.
 * The delivery handler manages the delivery and may optionally perform the 
 * delivery in a transactional manner.
 * <p>
 * Delivery handlers are invoked in a multi-threaded environment and must be
 * coded accordingly, with global data properly protected from simultaneous
 * thread access.
 * <p>
 * The use of the word transactional in this context does not refer to the 
 * standard ACID criteria. Rather it really talks about the delivery handler
 * buffering the published changes to the end of the job, and applying those
 * as an all or nothing batch. This enables us to keep web sites more generally
 * consistent, longer than if we were outputting changes on a per delivery 
 * mechanism.
 * 
 * @author dougrand
 *
 */
public interface IPSDeliveryHandler
{
   /**
    * Initialize the delivery handler for the given job. This method will be
    * called once by the delivery manager the first time assembled content is to
    * be delivered by the given handler.
    * 
    * @param jobid the job id, never repeated within any reasonable period of
    *            time. In practice, since this is a long, it will never, ever be
    *            repeated.
    * @param site the site, never <code>null</code>.
    * @param pubServer the publishing server, must not be <code>null</code>.
    * @throws PSDeliveryException 
    */
   void init(long jobid, IPSSite site, IPSPubServer pubServer) throws PSDeliveryException;
   
   /**
    * Return whether this plug in is transactional. Transactional plug ins 
    * postpone deliveries and removals until the 
    * {@link #commit(long)} or the {@link #rollback(long)} method is called.
    * Otherwise the content is immediately delivered or removed.
    * 
    * @return <code>true</code> is the plug in supports transactional behavior.
    */
   boolean isTransactional();
   
   /**
    * The deliver method delivers or queues a single item for delivery. If the
    * handler is <i>transactional</i> then the item is not actually delivered
    * until the {@link #commit(long)} method is called. Otherwise the item is
    * delivered immediately.
    * <p>
    * Deliver is also called for handlers that remove items using assembly
    * results, where the value of
    * {@link IPSDeliveryType#isUnpublishingRequiresAssembly()} is
    * <code>true</code>, and the value of {@link IPSDeliveryItem#isPublish()}
    * is <code>false</code>. Such cases call this method instead of
    * {@link #remove(IPSDeliveryItem)}
    * 
    * @param deliveryItem the item to be delivered, never <code>null</code>.
    * @return the delivery result, which includes any failures occurring during
    * immediate delivery and any immediate validation errors for transactional
    * delivery. The delivery result also contains the unpublishing information
    * for the given result.
    */   
   IPSDeliveryResult deliver(IPSDeliveryItem deliveryItem);
   
   /**
    * The remove method removes or queues for removal a single item. If the
    * handler is <i>transactional</i> then the item is not actually removed
    * until the {@link #commit(long)} method is called. Otherwise the item is
    * removed immediately.
    * <p>
    * This remove method is not called if
    * {@link IPSDeliveryType#isUnpublishingRequiresAssembly()} is
    * <code>true</code> regardless the value of
    * {@link IPSDeliveryItem#isPublish()}. See {@link #deliver(IPSDeliveryItem)}
    * for details.
    * 
    * @param deliveryItem the item to be removed, never <code>null</code> and
    * must have non-null unpublishing information present.
    * 
    * @return the delivery result, see {@link #deliver(IPSDeliveryItem)} for
    * details.
    * 
    * @see #deliver(IPSDeliveryItem).
    */
   IPSDeliveryResult remove(IPSDeliveryItem deliveryItem);
   
   /**
    * If transactional, then commit any stored results to the final location,
    * then free any held resources for the given job. Never called if not 
    * transactional, and should be implemented to do nothing in this case.
    * <p>
    * Errors during commit can result in some items being delivered while others
    * are not, leaving the destination site in an inconsistent state.
    * 
    * @param jobId the job id.
    * @return the statistics for the job if the handler is transactional, or
    * <code>null</code> for non-transactional handlers.
    * @throws PSDeliveryException 
    */
   Collection<IPSDeliveryResult> commit(long jobId) throws PSDeliveryException;
   
   /**
    * If transactional, then simply discard any held work, then free any held
    * resources for the given job. Never called if not transactional, and
    * should be implemented to do nothing in this case.
    * 
    * @param jobId the job id.
    * @throws PSDeliveryException on failure
    */
   void rollback(long jobId) throws PSDeliveryException;

   void updateItemStatus(List<IPSDeliveryResult> results);

   void updateItemStatus(IPSDeliveryResult result);
   
   
}
