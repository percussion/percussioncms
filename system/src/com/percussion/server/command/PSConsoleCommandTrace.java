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

package com.percussion.server.command;

import com.percussion.debug.PSDebugManager;
import com.percussion.debug.PSTraceFlag;
import com.percussion.debug.PSTraceMessageFactory;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSTraceOption;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.util.Iterator;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
                    
/**
 * Implements functionality for the "Trace" console command in all its forms
 */
public class PSConsoleCommandTrace extends PSConsoleCommand
{

   /**
    * The constructor for this class
    *
    * @param cmdArgs the arguments passed into the "Trace" console command.
    * @roseuid 39F4A1F502BF
    */
   public PSConsoleCommandTrace(String cmdArgs)
      throws PSIllegalArgumentException
   {                        
      super(cmdArgs);

      boolean isValid = true;
      String first = null;
      String badArg = null;

      // need the application name for this command
      if ((cmdArgs == null) || (cmdArgs.length() == 0))
         m_help = true;
      else
      {
         StringTokenizer tok = new StringTokenizer(cmdArgs, " ");

         // check first arg
         if (!tok.hasMoreTokens())
            m_help = true;
         else
         {
            first = tok.nextToken();
            if (first.toLowerCase().equals("default"))
               m_default = true;
            else if (first.toLowerCase().equals("help"))
               m_help = true;
         }

         // handle each case
         if (m_default)
         {
            // next arg should be app name
            if (!tok.hasMoreTokens())
               isValid = false;
            else
            {
               m_appname = tok.nextToken();
               if (tok.hasMoreTokens())
                  isValid = false;  // should not be any more
            }
         }
         else if (!m_help) // don't need to do anything more if help
         {
            // either have a flag or an appname
            if (first.toLowerCase().startsWith("0x") || first.equals("0"))
            {
               // should be 1-4 flags followed by app name
               String next = first;
               String flag = null;
               do
               {
                  try
                  {
                     if (next.toLowerCase().startsWith("0x"))
                        flag = next.substring(2);
                     else
                        flag = next;
                     m_traceFlags[m_flagCount] = Integer.parseInt(flag, 16);
                     m_flagCount++;
                  }
                  catch(NumberFormatException e)
                  {
                     isValid = false;
                     badArg = flag;
                     break;
                  }
                  next = null;
                  if (tok.hasMoreTokens())
                     next = tok.nextToken();

               } while (m_flagCount < 4 && (next != null) &&
                           (next.startsWith("0x") || next.equals("0")));

               // now next should be the appname
               if (isValid)
               {
                  if (next == null)
                     isValid = false;
                  else
                  {
                     // should have nothing after appname
                     if (tok.hasMoreTokens())
                     {
                        isValid = false;
                        badArg = tok.nextToken();
                     }
                     else
                        m_appname = next;
                  }
               }
            }
            else
            {
               // its just the appname - should have nothing after
               if (tok.hasMoreTokens())
               {
                  isValid = false;
                  badArg = tok.nextToken();
               }
               else
                  m_appname = first;
            }
         }
      }

      if (!isValid)
      {
         Object arg = null;
         if (badArg != null)
            arg = badArg;
         else
            arg = cmdArgs;

         Object[] args = { ms_cmdName, arg };
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_INVALID_ARGS, args);
      }
   }

   /**
    * Execute the command specified by this object. The results are returned
    * as an XML document of the appropriate structure for the command.
    * <P>
    * The execution of this command results in the following XML document
    * structure:
    * <PRE><CODE>
    * &lt;ELEMENT PSXConsoleCommandResults   (command, resultCode, resultText)&gt;
    * &lt;--
    * the command that was executed
    * --&gt;
    * &lt;ELEMENT command                     (#PCDATA)&gt;
    * &lt;--
    * the result code for the command execution
    * --&gt;
    * &lt;ELEMENT resultCode                  (#PCDATA)&gt;
    * &lt;--
    * the message text associated with the result code
    * --&gt;
    * &lt;ELEMENT resultText                  (#PCDATA)&gt;
    * </CODE></PRE>
    * @param request the requestor object
    * @return the result document
    * @roseuid 39FF04590119
    */
   public Document execute(PSRequest request) throws PSConsoleCommandException
   {
      //Construct the result doc
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(
         respDoc, "PSXConsoleCommandResults");
      PSXmlDocumentBuilder.addElement(respDoc, root, "command", ms_cmdName + " " + m_cmdArgs);
      PSXmlDocumentBuilder.addElement(respDoc, root, "resultCode", "0");
      PSXmlDocumentBuilder.addElement(respDoc, root, "resultText", "");

      try
      {
         // we need a debugmanager
         PSDebugManager mgr = PSDebugManager.getDebugManager();

         // case help
         if (m_help)
         {
            Iterator list = PSTraceMessageFactory.getPossibleOptions().iterator();
            while (list.hasNext())
            {
               Element el = PSXmlDocumentBuilder.addEmptyElement(
                  respDoc, root, "PSXTraceOption");
               PSTraceOption option = (PSTraceOption)list.next();
               el.setAttribute("flag", option.toString());
               el.setAttribute("name", option.getName());
            }
         }
         // case default
         else if (m_default)
         {
            // restore the initial options
            mgr.restoreInitialTraceOptions(m_appname);
         }
         else if (m_flagCount == 0)
         // case appname and no flags
         {
            // get the current flag
            PSTraceFlag flag = mgr.getTraceOptionsFlag(m_appname);

            // print out the current trace
            root = PSXmlDocumentBuilder.addEmptyElement(
               respDoc, root, "PSXTraceFlag");

            if (mgr.getLogHandler(m_appname).isTraceEnabled())
            {
               root.setAttribute("flag1", flag.toString(0));
               root.setAttribute("flag2", flag.toString(1));
               root.setAttribute("flag3", flag.toString(2));
               root.setAttribute("flag4", flag.toString(3));
            }
            else
            {
               // need to pass back all zero's as trace is disabled
               root.setAttribute("flag1", "0");
               root.setAttribute("flag2", "0");
               root.setAttribute("flag3", "0");
               root.setAttribute("flag4", "0");
            }

         }
         // case one or more flags
         else
         {
            // get the current flag
            PSTraceFlag flag = mgr.getTraceOptionsFlag(m_appname);

            synchronized (flag)
            {
               // set the new options on it
               for (int i=0; i < m_traceFlags.length; i++)
               {
                  flag.setFlag(m_traceFlags[i], i);
               }

               // need to set it back to trigger trace state change
               mgr.setTraceOptionsFlag(m_appname, flag);
            }
         }
      }
      catch (PSNotFoundException e)
      {
         // throw a PSConsoleCommandException instead
         Object[] args = {e.getMessage(), m_appname};
         throw new PSConsoleCommandException(
            IPSServerErrors.RCONSOLE_INVALID_ARGS, args);
      }

      return respDoc;
   }

   /**
    * allow package members to see our command name
    */
   final static String   ms_cmdName = "trace";

   /**
    * Determine if trace cmd with default options
    */
   private boolean m_default = false;

   /**
    * Determine if help cmd
    */
   private boolean m_help = false;

   /**
    * If trace cmd, the appname
    */
   private String m_appname = null;


   /**
    * If trace cmd, the flags
    */
   private int[] m_traceFlags = {0, 0, 0, 0};

   /**
    * If trace cmd, the number of flags passed
    */
   private int m_flagCount = 0;

}
