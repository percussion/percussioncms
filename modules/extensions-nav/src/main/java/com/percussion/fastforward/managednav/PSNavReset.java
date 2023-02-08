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

import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

/**
 * an exit to reset the Nav configuration
 * 
 * @author DavidBenua
 *  
 */
public class PSNavReset extends PSDefaultExtension
      implements
         IPSRequestPreProcessor,
         IPSResultDocumentProcessor
{
   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSRequestPreProcessor#preProcessRequest(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext)
    */
   public void preProcessRequest(Object[] params, IPSRequestContext req)
         throws PSAuthorizationException, PSRequestValidationException,
         PSParameterMismatchException, PSExtensionProcessingException
   {
      try
      {
         PSNavConfig.reset(req);
      }
      catch (PSNavException e)
      {
         req.printTraceMessage(e.getMessage());
         log.error("PSNavException found: {}",PSExceptionUtils.getMessageForLog(e));
         log.error(PSNavAutoSlotExtension.class, e);
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));

         throw new PSExtensionProcessingException(0,PSExceptionUtils.getMessageForLog(e));

      }
      catch (Exception ex)
      {
         log.error("unexcepted exception");
         log.error(getClass().getName(), ex);
         log.debug(ex.getMessage(), ex);
         log.debug(ex.getMessage(), ex);
         throw new PSExtensionProcessingException(getClass().getName(), ex);
      }

   }

   /**
    * This exit never modifies the stylesheet.
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#canModifyStyleSheet()
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * process the extension. Resets the current nav configuration.
    * 
    * @see com.percussion.extension.IPSResultDocumentProcessor#processResultDocument(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext, org.w3c.dom.Document)
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext req, Document result)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      try
      {
         PSNavConfig.reset(req);
      }
      catch (PSNavException e)
      {
         req.printTraceMessage(e.getMessage());
         log.error("PSNavException found: {}",PSExceptionUtils.getMessageForLog(e));
         log.error(PSNavAutoSlotExtension.class, e);
         log.debug(PSExceptionUtils.getDebugMessageForLog(e));
         throw new PSExtensionProcessingException(0, PSExceptionUtils.getMessageForLog(e));

      }
      catch (Exception ex)
      {
         log.error("unexcepted exception");
         log.error(getClass().getName(), ex);
         log.debug(ex.getMessage(), ex);
         throw new PSExtensionProcessingException(getClass().getName(), ex);
      }
      return result;
   }

   /**
    * Reference to Log4j singleton object used to log any errors or debug info.
    */
   private static final Logger log = LogManager.getLogger(PSNavReset.class);

}
