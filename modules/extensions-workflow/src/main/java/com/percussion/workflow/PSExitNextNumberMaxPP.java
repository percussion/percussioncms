/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.workflow;

import com.percussion.cms.IPSConstants;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * This extension returns the value of a counter obtained using
 * max(primarykeycolumn) + 1 with matching workflowid
 * @deprecated Use PSExitNextNumber
 */
@Deprecated
public class PSExitNextNumberMaxPP implements IPSRequestPreProcessor
{

   private static PSExitNextNumber newNextNumberExt = new PSExitNextNumber();
   private static final Logger log = LogManager.getLogger(IPSConstants.WORKFLOW_LOG);


   /**************  IPSExtension Interface Implementation ************* */
   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
      newNextNumberExt.init(extensionDef, file);
   }

   // This is the main request processing handler (see IPSRequestPreProcessor)
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException, PSParameterMismatchException
   {
      log.warn("Extension {} is deprecated and may produce false results, please update references to use PSExitNextNumber.",ms_exitName);
      log.warn("Invoking PSExitNextNumber instead...");

      newNextNumberExt.preProcessRequest(params, request);
   }

   private static String ms_exitName = "PSExitNextNumberMaxPP";
}
