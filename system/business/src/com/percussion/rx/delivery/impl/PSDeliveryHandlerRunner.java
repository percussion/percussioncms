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
package com.percussion.rx.delivery.impl;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.rx.delivery.IPSDeliveryResult;
import com.percussion.rx.delivery.PSDeliveryException;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.sitemgr.IPSSite;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Invokes a list of delivery handlers, so that it can deliver
 * the (assembled) content to more than one locations.
 *  
 * @author YuBingChen
 */
public class PSDeliveryHandlerRunner extends PSBaseDeliveryHandler
{
   /**
    * A list of to be dispatched handlers, never <code>null</code>, but may be empty.
    */
   private List<PSBaseDeliveryHandler> m_handlers = new ArrayList<>();
   
   /**
    * The logger.
    */
    private static final Logger ms_log = LogManager.getLogger(PSDeliveryHandlerRunner.class);
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.delivery.impl.PSBaseDeliveryHandler#init(long, com.percussion.services.sitemgr.IPSSite)
    */
   public void init(long jobid, IPSSite site, IPSPubServer pubServer) throws PSDeliveryException
   {
      super.init(jobid, site, pubServer);
      
      for (PSBaseDeliveryHandler handler : m_handlers)
      {
         handler.init(jobid, site, pubServer);
      }
   }
   
   @Override
   protected IPSDeliveryResult doDelivery(Item item, long jobId, String location)
         throws PSDeliveryException
   {
      item.setDoRelease(false);
      IPSDeliveryResult result = null;
      try
      {
         for (PSBaseDeliveryHandler handler : m_handlers)
         {
            result = handler.doDelivery(item, jobId, location);
            if(result.getDeliveryContext()<0)
               result.setDeliveryContext(item.getDeliveryContext());
            if (result.getOutcome() == IPSDeliveryResult.Outcome.FAILED)
               return result;
         }
         return result;
      }
      finally
      {
         item.setDoRelease(true);
         item.release();
      }
   }

   @Override
   protected IPSDeliveryResult doRemoval(Item item, long jobId, String location)
   {
      item.setDoRelease(false);
      IPSDeliveryResult result = null;
      try
      {
         for (PSBaseDeliveryHandler handler : m_handlers)
         {
            result = handler.doRemoval(item, jobId, location);
            if(result.getDeliveryContext()<0)
               result.setDeliveryContext(item.getDeliveryContext());
            if (result.getOutcome() == IPSDeliveryResult.Outcome.FAILED)
               return result;
         }
         return result;
      }
      finally
      {
         item.setDoRelease(true);
         item.release();
      }
   }

   @Override
   protected void removeEmptyDirectory(String dir)
   {
      for (PSBaseDeliveryHandler handler : m_handlers)
      {
         handler.removeEmptyDirectory(dir);
      }
   }
   
   /*
    * (non-Javadoc)
    * @see com.percussion.rx.delivery.impl.PSBaseDeliveryHandler#prepareForDelivery(long)
    */
   protected Collection<IPSDeliveryResult> prepareForDelivery(long jobId)
      throws PSDeliveryException
   {
      validateHandlers();
      Collection<IPSDeliveryResult> errorResult = null;
      
      for (PSBaseDeliveryHandler handler : m_handlers)
      {
         if (errorResult == null)
         {
            errorResult = handler.prepareForDelivery(jobId);
         }
      }
      return errorResult;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.rx.delivery.impl.PSBaseDeliveryHandler#releaseForDelivery(long)
    */
   protected void releaseForDelivery(long jobId)
   {
      for (PSBaseDeliveryHandler handler : m_handlers)
      {
         handler.releaseForDelivery(jobId);
      }      
   }

   /**
    * Validates the list of handlers, it must not be empty.
    * The transaction property ({@link #isTransactional()}) must be consistent
    * between this instance and the list of handlers.
    * 
    * @throws IllegalStateException if the list of handlers is empty or the
    * transaction property is not consistent.
    */
   private void validateHandlers()
   {
      if (m_handlers.isEmpty())
      {
         String msg = "The list of delivery handlers of the PSDeliveryHandlerDispatcher cannot be empty.";
         IllegalStateException e = new IllegalStateException(msg);
         ms_log.error(msg, e);

         throw e;
      }
      
      for (PSBaseDeliveryHandler handler : m_handlers)
      {
         if (isTransactional() != handler.isTransactional())
         {
            String msg = "The \"transactional\" property of PSDeliveryHandlerDispatcher is \"" + isTransactional() + "\", but it is \"" + handler.isTransactional() + "\" for " + handler.getClass().getName();
            IllegalStateException e = new IllegalStateException(msg);
            ms_log.error(msg, e);

            throw e;
         }
      }      
   }
   
   /**
    * Sets the dispatched handlers.
    * 
    * @param handlers the handlers, not <code>null</code>, may be empty.
    */
   public void setDeliveryHandlers(List<PSBaseDeliveryHandler> handlers)
   {
      notNull(handlers);
      
      m_handlers = handlers;
   }
}
