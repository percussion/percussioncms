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
