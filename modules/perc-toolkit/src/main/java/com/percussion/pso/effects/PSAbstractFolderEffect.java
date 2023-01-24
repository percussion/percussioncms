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
package com.percussion.pso.effects;
import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffectResult;
import com.percussion.relationship.annotation.PSHandlesEffectContext;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.content.PSContentWsLocator;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;


/**
 * Base class for folder effects.  
 * 
 * @author DavidBenua
 *
 */
@PSHandlesEffectContext()
public abstract class PSAbstractFolderEffect implements IPSEffect
{
   /**
    * Logger for this class
    */
   protected static final Logger log = LogManager.getLogger(PSFolderFollowerEffect.class);
   protected static IPSSystemWs sws = null;
   protected static IPSGuidManager gmgr = null;
   protected static IPSContentWs cws = null; 

   /**
    * Initialize service pointers. 
    */
   protected static void initServices()
   {
      if(sws == null)
      {
         sws = PSSystemWsLocator.getSystemWebservice(); 
         gmgr = PSGuidManagerLocator.getGuidMgr(); 
         cws = PSContentWsLocator.getContentWebservice(); 
      }
   }

   
   public void recover(Object[] params, IPSRequestContext req, IPSExecutionContext exCtx, PSExtensionProcessingException ex,
         PSEffectResult result) throws PSExtensionProcessingException
   { //Nothing to do here      
      result.setSuccess(); 
   }

   public void test(Object[] params, IPSRequestContext req, IPSExecutionContext exCtx, PSEffectResult result)
         throws PSExtensionProcessingException, PSParameterMismatchException
   { //nothing to do here
      result.setSuccess(); 
   }
   
   /**
    * Default constructor. 
    */
   public PSAbstractFolderEffect()
   {
      super();
   }

   
   public void init(IPSExtensionDef arg0, File arg1) throws PSExtensionException
   {
         
   }

   
}