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

import com.percussion.error.PSExceptionUtils;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.server.IPSRequestContext;
import com.percussion.tools.PrintNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.StringWriter;

/**
 * This extension returns the value of a counter obtained from a database
 * stored procedure.
 */
public class PSPreExitHandler implements IPSRequestPreProcessor
{

  private static final Logger log = LogManager.getLogger(PSPreExitHandler.class);

  /**
  * Default constructor, as requiredL for use by IPSExtensionHandler.
  */
  public PSPreExitHandler()
  {
    super();
  }

  public void init(IPSExtensionDef extensionDef, File file)
     throws PSExtensionException
  {
  }

   /**
    * This is the main request processing handler
    */
   public void preProcessRequest(Object[] params, IPSRequestContext request)
      throws PSExtensionProcessingException
   {
    log.info("");
    log.info("             *** Beginning of Pre-Processor Exit Debugger ***");
    log.info("");

    if(null == request)
    {
      log.info("Request context is null!");
    }
    else
    {
      PSPostExitHandler.printRequestContext(request);

      log.info("");
      log.info("Input XML Document:");
      log.info("");
      log.info("*** Starts Here ***");
      try
      {
        if(null == request.getInputDocument())
          log.info("   Document is empty");
        else
        {
          StringWriter writer = new StringWriter();
          PrintNode.printNode(request.getInputDocument(), " " , writer);
          log.info(writer.toString());
        }
      }
      catch (Exception e)
      {
          log.error(PSExceptionUtils.getMessageForLog(e));
          log.debug(PSExceptionUtils.getDebugMessageForLog(e));
      }
      log.info("*** Ends Here ***");
    }
    log.info("");
    log.info("             *** End of Pre-Processor Exit Debugger ***");
    log.info("");
   }
}
