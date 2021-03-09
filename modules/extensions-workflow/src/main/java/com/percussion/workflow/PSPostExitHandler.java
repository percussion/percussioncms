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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.workflow;

import com.percussion.extension.*;
import com.percussion.server.IPSRequestContext;
import com.percussion.tools.PrintNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;

import java.io.File;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

public class PSPostExitHandler implements IPSResultDocumentProcessor
{
    private static final Logger log = LogManager.getLogger(PSPostExitHandler.class);

    /**
    * Constructor
    */
   public PSPostExitHandler()
   {
     super();
   }

  public boolean canModifyStyleSheet()
  {
    return true;
  }


   public void init(IPSExtensionDef extensionDef, File file)
      throws PSExtensionException
   {
       //nothing to initialize
   }
   /**
    * This is the main request processing handler
    */
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext request,
                                         Document resDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
    log.info("");
    log.info(
       "             *** Beginning of Post-Document Exit Debugger ***");
    log.info("");

    if(null == request)
    {
      log.info("Request context is null!");
    }
    else
    {
      printRequestContext(request);

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
         log.error(e.getMessage());
         log.debug(e.getMessage(),e);
      }
      log.info("*** Ends Here ***");
    }

    if(null != resDoc)
    {
      log.info("");
      log.info("Result XML Document:");
      log.info("");
      log.info("*** Starts Here ***");
      try
      {
        StringWriter writer = new StringWriter();
        PrintNode.printNode(resDoc, " " , writer);
        log.info(writer.toString());
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      log.info("*** Ends Here ***");
    }
    else
      log.info("   Document is empty");

    log.info("");
    log.info(
       "             *** End of Post-Document Exit Debugger ***");
    log.info("");

      return resDoc;
   }

  static public void printRequestContext(IPSRequestContext request)
  {
    log.info("");
    log.info("Contents of the Request Context...");
    log.info("");

    printString(request.getCurrentApplicationName(), "Application Name");
    printString(request.getRequestFileURL(), "Request File URL");
    printString(request.getRequestPage(), "Request Page ");
    printString(request.getRequestRoot(), "Request Root");

    log.info("");
    log.info("List of CGI variables and values:");
    Enumeration headers = request.getHeaders();
    while (headers.hasMoreElements())
   {
      String header = (String) headers.nextElement();
      String value = request.getCgiVariable(header);
      printString(header, value);
   }

    log.info("");
    log.info("List HTML parameters and values:");
    printMap(request.getParameters());

    log.info("");
    log.info("List Response cookies and values:");
    printMap(request.getResponseCookies());
  }

  static public void printMap(Map map)
  {
    if(null == map)
    {
      log.info("Map containing the list is null");
      return;
    }
    Set keyset = map.keySet();
    if(null == keyset || keyset.isEmpty())
    {
      log.info("List is empty");
    }

    if (keyset != null) {


          Object[] obArray = keyset.toArray();
          for (int i = 0; i < obArray.length; i++) {
              log.info("{}  {}={}", i + 1,
                      obArray[i],
                      map.get(obArray[i].toString()));
          }
      }
  }

   public static void printString(String value, String name)
  {
    log.info( "{} = {}",name , value);
    log.info("");
  }
}

