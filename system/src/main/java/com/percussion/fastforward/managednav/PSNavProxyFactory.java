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
package com.percussion.fastforward.managednav;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSComponentProcessor;
import com.percussion.cms.objectstore.PSActiveAssemblyProcessorProxy;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.server.IPSRequestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * PSNavProxyFactory is a factory for relationship proxy objects. Rather than
 * create these objects repeated, a set of proxy objects is created for each
 * <code>IPSRequestContext</code> object.
 * <p>
 * There are 3 proxy objects
 * <ul>
 * <li>the Active Assembly processor proxy for finding items in a slot</li>
 * <li>the Relationship processor proxy for finding all relationships</li>
 * <li>the Component processor proxy for loading all other components</li>
 * </ul>
 * <p>
 * This class implements the <code>Singleton</code> pattern. It is never
 * directly constructed. There is one proxy factory for each
 * <code>IPSRequestContext</code>
 * 
 * @author DavidBenua
 *  
 */
public class PSNavProxyFactory
{
   
   private static volatile PSNavProxyFactory inst = null;
         
         
   /**
    * Constructs a new factory for the callering request
    * 
    * @param req the parent request
    * @throws PSNavException when any error occurs.
    */
   private PSNavProxyFactory(IPSRequestContext req) throws PSNavException
   {

      try
      {
         m_aaProxy = new PSActiveAssemblyProcessorProxy(
               PSActiveAssemblyProcessorProxy.PROCTYPE_SERVERLOCAL, req);

         m_relProxy = PSRelationshipProcessor.getInstance();
         m_compProxy = PSDesignModelUtils.getComponentProxy();

      }
      catch (PSCmsException ex)
      {
         log.error(this.getClass().getName(), ex);
         throw new PSNavException(this.getClass().getName(), ex);
      }

   }

   /**
    * Gets the processor factory for this request context. If one does not
    * exist, it will be created.
    * 
    * @param req the parent request context
    * @return the factory instance. Never <code>null</code>
    * @throws PSNavException
    */
   public static PSNavProxyFactory getInstance(IPSRequestContext req)
         throws PSNavException
   {
     
      if (inst == null)
      {
         synchronized (PSNavProxyFactory.class)
         {
            if (inst == null)
            {
               inst = new PSNavProxyFactory(null);
            }
         }
         
      }
     
      return inst;
   }

   /**
    * Gets the Active Assembly Processor proxy.
    * 
    * @return the Active Assembly Processor proxy.
    */
   public PSActiveAssemblyProcessorProxy getAaProxy()
   {
      return m_aaProxy;
   }

   /**
    * Gets the Component Processor proxy.
    * 
    * @return the component processor proxy.
    */
   public IPSComponentProcessor getCompProxy()
   {
      return m_compProxy;
   }

   public PSRelationshipProcessor getRelProxy()
   {
      return m_relProxy;
   }
   /**
    * Writes messages from this class to the log file.
    */
   private static final Logger log = LogManager.getLogger(PSNavProxyFactory.class);

   /**
    * The Active Assembly Processor proxy.
    */
   private PSActiveAssemblyProcessorProxy m_aaProxy = null;

   /**
    * The Component Processor proxy.
    */
   private IPSComponentProcessor m_compProxy = null;

   /**
    * The Relationship Processor proxy.
    */
   private PSRelationshipProcessor m_relProxy = null;

}
