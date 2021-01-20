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

package com.percussion.debug;

import com.percussion.server.IPSCgiVariables;
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
    * @roseuid 39F4650F0271
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
    * @roseuid 39FEE2F20251
    */
   protected String getMessageBody(Object source)
   {

      StringBuffer buf = new StringBuffer();
      PSRequest request = (PSRequest)source;

      // Add HTML params if requested
      if (m_typeFlag != PSTraceMessageFactory.POST_EXIT_CGI_FLAG)
      {

         HashMap params = request.getParameters();

         buf.append("HTML Parameters:");
         buf.append(NEW_LINE);
         if (params != null)
            logMap(buf, params);
      }

      // Add CGI vars
      buf.append("CGI Variables:");
      buf.append(NEW_LINE);
      Map cgiVars = new HashMap();
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
    * @param buf the StringBuffer to append to
    * @param map the HashMap containing String keys and String values
    */
   private void logMap(StringBuffer buf, Map map)
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
