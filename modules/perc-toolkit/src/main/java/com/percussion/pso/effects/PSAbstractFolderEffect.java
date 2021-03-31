/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.effects;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   protected static final Log log = LogFactory.getLog(PSFolderFollowerEffect.class);
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