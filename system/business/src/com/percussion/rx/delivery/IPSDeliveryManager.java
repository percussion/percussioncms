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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.rx.delivery;

import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.sitemgr.IPSSite;

import java.util.Collection;

/**
 * The delivery manager is responsible for using the publisher plug in
 * identified by the bean to persist the data to the appropriate durable
 * location. At this point that delivery is only performed locally. This is a
 * marker interface since there are no methods added beyond those for the
 * delivery handler.
 * 
 * @author dougrand
 * 
 */
public interface IPSDeliveryManager
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
    * @pubServer the publishing server, may be <code>null</code>.
    */
   void init(long jobid, IPSSite site, IPSPubServer pubServer);

   /**
    * The process method queues a single item for delivery or
    * removal. If the handler is <i>transactional</i> then the item is not
    * actually delivered or removed until the {@link #commit(long)} method is
    * called. Otherwise the item is handled immediately.
    * 
    * @param deliveryItem the item to be delivered or removed, never
    *            <code>null</code>.
    * @return the delivery result, which includes any failures occurring during
    *         immediate delivery or removal and any immediate validation errors
    *         for transactional delivery. The delivery result also contains the
    *         unpublishing information for the given result when an item is
    *         delivered via a compatible handler.
    */
   IPSDeliveryResult process(IPSDeliveryItem deliveryItem);

   /**
    * If transactional, then commit any stored results to the final location,
    * then free any held resources for the given job.
    * For not transactional can perform cleanup activities.
    * <p>
    * Errors during commit can result in some items being delivered while others
    * are not, leaving the destination site in an inconsistent state.
    * 
    * @param jobId the job id.
    * @return the statistics for the job. Never <code>null</code>, can be empty.
    * @throws PSDeliveryException on commit failure.
    */
   Collection<IPSDeliveryResult> commit(long jobId) throws PSDeliveryException;

   /**
    * If transactional, then simply discard any held work, then free any held
    * resources for the given job. Never called if not transactional, and should
    * be implemented to do nothing in this case.
    * 
    * @param jobId the job id.
    * @throws PSDeliveryException on failure
    */
   void rollback(long jobId) throws PSDeliveryException;
   
   
   /**
    * The absolute path of temporary directory.
    * 
    * @return the path of the temporary directory. Never <code>null</code>, can be empty.
    */
   String getTempDir();

   void updateItemState(IPSDeliveryResult result); 
}
