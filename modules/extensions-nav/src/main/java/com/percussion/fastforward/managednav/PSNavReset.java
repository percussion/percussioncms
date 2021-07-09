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
package com.percussion.fastforward.managednav;

import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSRequestContext;
import com.percussion.server.PSRequestValidationException;

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
         log.error("PSNavException found: {}", e.getMessage());
         log.error(PSNavAutoSlotExtension.class, e);
         log.debug(e.getMessage(), e);

         throw new PSExtensionProcessingException(0, e.getMessage());

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
         log.error("PSNavException found: {}", e.getMessage());
         log.error(PSNavAutoSlotExtension.class, e);
         log.debug(e.getMessage(), e);
         throw new PSExtensionProcessingException(0, e.getMessage());

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
