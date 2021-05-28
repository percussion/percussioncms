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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.workflow;

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
    System.out.println("");
    System.out.println(
       "             *** Beginning of Pre-Processor Exit Debugger ***");
    System.out.println("");

    if(null == request)
    {
      System.out.println("Request context is null!");
    }
    else
    {
      PSPostExitHandler.printRequestContext(request);

      System.out.println("");
      System.out.println("Input XML Document:");
      System.out.println("");
      System.out.println("*** Starts Here ***");
      try
      {
        if(null == request.getInputDocument())
          System.out.println("   Document is empty");
        else
        {
          StringWriter writer = new StringWriter();
          PrintNode.printNode(request.getInputDocument(), " " , writer);
          System.out.println(writer.toString());
        }
      }
      catch (Exception e)
      {
          log.error(e.getMessage());
          log.debug(e.getMessage(), e);
      }
      System.out.println("*** Ends Here ***");
    }
    System.out.println("");
    System.out.println(
       "             *** End of Pre-Processor Exit Debugger ***");
    System.out.println("");
   }
}
