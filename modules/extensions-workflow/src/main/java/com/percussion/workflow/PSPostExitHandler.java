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
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.tools.PrintNode;

import java.io.File;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;

import org.w3c.dom.Document;

public class PSPostExitHandler implements IPSResultDocumentProcessor
{
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
   }
   /**
    * This is the main request processing handler
    */
   public Document processResultDocument(Object[] params,
                                         IPSRequestContext request,
                                         Document resDoc)
      throws PSParameterMismatchException, PSExtensionProcessingException
   {
    System.out.println("");
    System.out.println(
       "             *** Beginning of Post-Document Exit Debugger ***");
    System.out.println("");

    if(null == request)
    {
      System.out.println("Request context is null!");
    }
    else
    {
      printRequestContext(request);

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
//        e.printStackTrace();
      }
      System.out.println("*** Ends Here ***");
    }

    if(null != resDoc)
    {
      System.out.println("");
      System.out.println("Result XML Document:");
      System.out.println("");
      System.out.println("*** Starts Here ***");
      try
      {
        StringWriter writer = new StringWriter();
        PrintNode.printNode(resDoc, " " , writer);
        System.out.println(writer.toString());
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      System.out.println("*** Ends Here ***");
    }
    else
      System.out.println("   Document is empty");

    System.out.println("");
    System.out.println(
       "             *** End of Post-Document Exit Debugger ***");
    System.out.println("");

      return resDoc;
   }

  static public void printRequestContext(IPSRequestContext request)
  {
    System.out.println("");
    System.out.println("Contents of the Request Context...");
    System.out.println("");

    printString(request.getCurrentApplicationName(), "Application Name");
    printString(request.getRequestFileURL(), "Request File URL");
    printString(request.getRequestPage(), "Request Page ");
    printString(request.getRequestRoot(), "Request Root");

    System.out.println("");
    System.out.println("List of CGI variables and values:");
    Enumeration headers = request.getHeaders();
    while (headers.hasMoreElements())
   {
      String header = (String) headers.nextElement();
      String value = request.getCgiVariable(header);
      printString(header, value);
   }

    System.out.println("");
    System.out.println("List HTML parameters and values:");
    printMap(request.getParameters());

    System.out.println("");
    System.out.println("List Response cookies and values:");
    printMap(request.getResponseCookies());
  }

  static public void printMap(HashMap map)
  {
    if(null == map)
    {
      System.out.println("Map containing the list is null");
      return;
    }
    Set keyset = map.keySet();
    if(null == keyset || keyset.size() < 1)
    {
      System.out.println("List is empty");
    }
    Object[] obArray = keyset.toArray();
    for(int i=0; i<obArray.length; i++)
    {
      System.out.println(i+1 + "   " + obArray[i].toString() + " = "
                         + map.get(obArray[i].toString()));
    }
  }

  static public void printString(String value, String name)
  {
    System.out.println(name + " = " + value);
    System.out.println("");
  }
}

