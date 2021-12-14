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
package com.percussion.rx.delivery.data;

import com.percussion.rx.delivery.IPSDeliveryHandler;
import com.percussion.rx.delivery.IPSDeliveryResult;
import com.percussion.utils.guid.IPSGuid;

import static org.apache.commons.lang.Validate.notNull;

/**
 * A data object that describes whether a particular delivery was successful or
 * not, and what failure occurred if one has occurred. Delivery results are
 * generated by the delivery handlers, {@link IPSDeliveryHandler}. 
 * <p>
 * Results are generated at two points in the process. The initial call to
 * {@link
 * IPSDeliveryHandler#deliver(com.percussion.rx.delivery.IPSDeliveryItem)}
 * will return a delivery result. Then the call to 
 * {@link IPSDeliveryHandler#commit(long)} will return a collection of results.
 * <p>
 * The logging system will combine multiple messages from the results if needed.
 * <p>
 * Another important role of the delivery result is to carry unpublishing 
 * information from the delivery handler to be stored with the item's logging
 * information in the database. By storing a characteristic piece of data such
 * as path or the table and primary key, the delivered item can later be 
 * unpublished. The unpublishing information can be any serializable java
 * data object.
 * 
 * @author dougrand
 */
public class PSDeliveryResult implements IPSDeliveryResult
{
   /**
    * Ctor.
    * 
    * @param status the delivery status. Not <code>null</code>.
    * @param failureMessage the failure message if required, may be
    *            <code>null</code> or empty.
    * @param id the id of the item being published, never <code>null</code>.
    *            Note that this may not be unique for a given delivery result.
    * @param jobId the jobId, which identifies all the items in a particular run
    *            of a particular edition.
    * @param referenceId which identifies a particular assembled item. The
    *            reference id will be unique to a given published item location.
    * @param unpublishData optional data that identifies how to unpublish an
    *            item. It is not necessary to have this data present on all
    *            result objects for a given location. The data will be
    *            maintained through updates.
    */
   public PSDeliveryResult(Outcome status, String failureMessage, IPSGuid id,
         long jobId, long referenceId, int deliveryContext, byte[] unpublishData)
   {
      notNull(id, "id may not be null");
      notNull(status, "status may not be null");

      m_status = status;
      m_failureMessage = failureMessage;
      m_id = id;
      m_jobId = jobId;
      m_referenceId = referenceId;
      m_deliveryContext = deliveryContext;
      m_unpublishData = unpublishData;
   }
   
   public PSDeliveryResult(Outcome status, String failureMessage, IPSGuid id,
         long jobId, long referenceId, byte[] unpublishData)
   {
      notNull(id, "id may not be null");
      notNull(status, "status may not be null");

      m_status = status;
      m_failureMessage = failureMessage;
      m_id = id;
      m_jobId = jobId;
      m_referenceId = referenceId;
      m_deliveryContext = -1;
      m_unpublishData = unpublishData;
   }
   

   public String getFailureMessage()
   {
      return m_failureMessage;
   }

   public Outcome getOutcome()
   {
      return m_status;
   }

   public IPSGuid getId()
   {
      return m_id;
   }

   public long getJobId()
   {
      return m_jobId;
   }

   public long getReferenceId()
   {
      return m_referenceId;
   }

   public byte[] getUnpublishData()
   {
      return m_unpublishData;
   }

   public int getDeliveryContext()
   {
      return m_deliveryContext;
   }
   
   @Override
   public void updateSent()
   {
      hasUpdateSent = true;
   }

   @Override
   public boolean hasUpdateSent()
   {
      return hasUpdateSent;
   }
   
   private boolean hasUpdateSent = false;
   
   /**
    * Was this result a success.
    */
   private final Outcome m_status;

   /**
    * If it was a failure, what was the problem?
    */
   private final String m_failureMessage;

   /**
    * Id of the item being delivered, never <code>null</code> after
    * construction.
    */
   private final IPSGuid m_id;

   /**
    * The job id that the result belongs to.
    */
   private final long m_jobId;

   /**
    * The reference id for the item.
    */
   private final long m_referenceId;

   /**
    * The context id for the item.
    */
   private int m_deliveryContext;
   
   /**
    * Unpublish data, may be <code>null</code>.
    */
   private final byte[] m_unpublishData;

   @Override
   public void setDeliveryContext(int deliveryContext)
   {
      m_deliveryContext = deliveryContext;
   }

 
}
