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

package com.percussion.debug;

import com.percussion.utils.server.IPSCgiVariables;
import com.percussion.server.PSRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Used to generate trace messages for Html paramenters and Cgi varibles.
 * Includes all parameter in the request and their values, and all CGI variables
 * and their values.  Sends output as one per line in the form 'name=value'.
 * Handles the following trace message types:
 * - 0x0002 Initial HTML/CGI
 * - 0x0020 HTML/CGI post PreProc exit - HTML and CGI vars after all pre proc
 * exitts are run
 * - 0x2000 CGI post PostProc exit - CGI vars after post proc exits are run.
 */
public class PSTraceHtmlCgi extends PSTraceMessage
{

   /**
    * The constructor for this class.
    *
    * @param traceFlag Since this class handles multiple trace message types,
    * the trace flag must be passed to specify which trace message type is
    * being handled.
    * @throws java.lang.IllegalArgumentException if the trace flag passed is not
    * handled by this class.
    */
   public PSTraceHtmlCgi(int traceFlag)
   {
      super(traceFlag);
   }

   // see parent class for javadoc
   protected String getMessageHeader()
   {
      String header = null;
      switch (m_typeFlag)
      {
         case PSTraceMessageFactory.INIT_HTTP_VAR_FLAG:
            header = "traceInitHttpVar_dispname";
            break;
         case PSTraceMessageFactory.POST_PREPROC_HTTP_VAR_FLAG:
            header = "tracePostPreProcHttpVar_dispname";
            break;
         case PSTraceMessageFactory.POST_EXIT_CGI_FLAG:
            header = "tracePostExitCgi_dispname";
            break;
      }

      return ms_bundle.getString(header);
   }

   /**
    * Formats the output for the body of the message, extracting the information
    * required from the source object.
    * @param source a PSRequest object containing the information required for the
    * trace message
    * @return the message body
    */
   protected String getMessageBody(Object source)
   {

      StringBuilder buf = new StringBuilder();
      PSRequest request = (PSRequest)source;

      // Add HTML params if requested
      if (m_typeFlag != PSTraceMessageFactory.POST_EXIT_CGI_FLAG)
      {

         Map<String, Object> params = request.getParameters();

         buf.append("HTML Parameters:");
         buf.append(NEW_LINE);
         if (params != null)
            logMap(buf, params);
      }

      // Add CGI vars
      buf.append("CGI Variables:");
      buf.append(NEW_LINE);
      Map<String, Object> cgiVars = new HashMap();
      Enumeration headers = request.getServletRequest().getHeaderNames();
      while (headers.hasMoreElements())
      {
         String name = (String) headers.nextElement();
         cgiVars.put(name, request.getServletRequest().getHeader(name));
      }
      
      if (cgiVars != null)
         logMap(buf, cgiVars);

      return new String(buf);
   }

   /**
    * Formats the output for the name-value pairs found in the map
    * @param buf the StringBuilder to append to
    * @param map the HashMap containing String keys and String values
    */
   private void logMap(StringBuilder buf, Map<String,Object> map)
   {
      Iterator entries = map.entrySet().iterator();
      String key = null;
      String value = null;
      while (entries.hasNext())
      {
         Map.Entry entry = (Map.Entry)entries.next();
         // skip over files - they will be handled by the PSTraceFileInfo type
         Object o = entry.getValue();
         if (!(o instanceof File))
         {
            if (o instanceof ArrayList)
            {
               // check the first element to see if this is a list of files
               ArrayList tmpList = (ArrayList)o;
               if (tmpList.size() > 0 && tmpList.get(0) instanceof File)
                  continue;
            }
            key = entry.getKey().toString();
            if(ms_hiddenVars.contains(key))
               value = "**********";
            else
               value = (null == o) ? "" : o.toString();
            buf.append(key);
            buf.append(" = ");
            buf.append(value);
            buf.append(NEW_LINE);
         }
      }
      buf.append(' ');
      buf.append(NEW_LINE);
   }
   /**
    * List of names of the CGI variables that need to be hidden in the trace
    * information. Certain variables such as unencrypted passwords may need to
    * be hidden in the trace file. This variable holds all such CGI variable
    * names. Never <code>null</code> or empty. Values are filled during loading
    * of the class.
    */
   static private List ms_hiddenVars = new ArrayList();
   static
   {
      ms_hiddenVars.add(IPSCgiVariables.CGI_AUTH_PASSWORD);
      //add any other variable to the list to hide in the trace file like the above
   }
}
