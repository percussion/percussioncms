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

package com.percussion.server.command;

import com.percussion.data.PSDatabaseMetaData;
import com.percussion.data.PSMetaDataCache;
import com.percussion.data.PSSqlException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.IPSConsoleCommand;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.percussion.utils.jdbc.PSConnectionInfo;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.sql.SQLException;
import java.util.StringTokenizer;

import javax.naming.NamingException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PSConsoleCommandFlushDbmd extends PSConsoleCommand
{
   /**
    * The constructor for this class.  Parses the command so that it is ready
    * to execute.  Format of the command is:
    * <p>
    * flush dbmd -d <datasource> -t <table> -o <origin>
    * <p>
    * or <p>
    * flush dbmd [-h]
    * <p>
    * The switches are not case sensitive, and the space between the switch and
    * its value is optional.  Switches may be specified in any order.  
    * Datasource may be ommited or empty to use the default, and an origin may 
    * only be supplied if a table has been specified as well.  If the "-h" 
    * switch or no switches are passed, then the help for this command is 
    * displayed.
    *
    * @param cmdArgs the arguments passed into the "flush dbmd" console command.
    * This is everything after the "flush dbmd" portion of the command.
    *
    * @throws PSIllegalArgumentException if cmdArgs does not meet the
    * requirements specified above.
    */
   public PSConsoleCommandFlushDbmd(String cmdArgs)
      throws PSIllegalArgumentException
   {
      super(cmdArgs);

      if ((cmdArgs == null) || (cmdArgs.length() == 0))
      {
         m_help = true;
         return;
      }

      StringTokenizer tok = new StringTokenizer(cmdArgs, " ");

      // check first arg
      if (!tok.hasMoreTokens())
      {
         m_help = true;
         return;
      }

      char curSwitch = OPTION_HELP;
      String curArg = null;
      String badArg = null;
      boolean inArg = false;
      boolean isError = false;
      int count = 0;

      while (tok.hasMoreTokens())
      {
         String token = tok.nextToken();
         count++;

         // see if we have an option switch
         if (token.startsWith(OPTION_SWITCH))
         {
            // make sure we're not still expecting a value for a previous switch
            if (inArg)
            {
               isError = true;
               badArg = token;
               break;
            }

            // make sure we got a full switch
            if (token.length() < 2)
            {
               isError = true;
               badArg = token;
               break;
            }
            curSwitch = token.charAt(1);

            // see if there's no space between switch and arg
            if (token.length() > 2)
            {
               curArg = token.substring(2, token.length());
               inArg = false;
            }
            else if (Character.toLowerCase(curSwitch) == OPTION_HELP)
            {
               // no arg to follow this switch
               inArg = false;
            }
            else
            {
               inArg = true;
            }
         }
         else
         {
            // this should be an arg
            curArg = token;
            inArg = false;
         }

         // see if we have an arg to save
         if (!inArg)
         {
            char tmpSwitch = Character.toLowerCase(curSwitch);

            switch(tmpSwitch)
            {
               case OPTION_HELP:
                  if (count > 1)
                  {
                     isError = true;
                     badArg = OPTION_SWITCH + curSwitch;
                     break;
                  }
                  else
                  {
                     m_help = true;
                     return;
                  }

               case OPTION_DATASOURCE:
                  m_datasource = curArg;
                  break;

               case OPTION_TABLE:
                  m_table = curArg;
                  break;

               default:
                  isError = true;
                  badArg = OPTION_SWITCH + curSwitch;
            }

            // make sure we got the arg
            if (isError)
               break;

            // reset for next time
            curArg = null;
         }
      }

      // make sure we got at least what we need
      if (!isError)
      {
         // can't have origin with no table
         if (m_origin != null && (m_table == null ||
            m_table.trim().length() == 0))
         {
            isError = true;
            badArg = cmdArgs;
         }
      }

      if (isError)
      {
         Object[] args = { ms_cmdName, badArg };
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
    * &lt;ELEMENT PSXConsoleCommandResults (command, resultCode, resultText)&gt;
    * &lt;--
    * the command that was executed
    * --&gt;
    * &lt;ELEMENT command (#PCDATA)&gt;
    * &lt;--
    * the result code for the command execution
    * --&gt;
    * &lt;ELEMENT resultCode (#PCDATA)&gt;
    * &lt;--
    * the message text associated with the result code
    * --&gt;
    * &lt;ELEMENT resultText (#PCDATA)&gt;
    * </CODE></PRE>
    * See {@link IPSConsoleCommand#execute(PSRequest)} for information on
    * parameters and exceptions.
    */
   public Document execute(PSRequest request)
      throws PSConsoleCommandException
   {

      //Construct the result doc
      Document respDoc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(
         respDoc, "PSXConsoleCommandResults");
      PSXmlDocumentBuilder.addElement(respDoc, root, "command", ms_cmdName +
         " " + m_cmdArgs);
      PSXmlDocumentBuilder.addElement(respDoc, root, "resultCode", "0");

      String msg = null;

      if (m_help)
      {
         msg = getHelpText();
      }
      else
      {
         // flush the cache as directed
         if (m_table != null)
         {
            // flush just the table
            boolean flushed = false;
            PSConnectionInfo connInfo = new PSConnectionInfo(m_datasource);
            PSDatabaseMetaData dbmd = 
               PSMetaDataCache.getCachedDatabaseMetaData(connInfo);

            if (dbmd != null)
            {
               if (m_origin == null)
               {
                  PSConnectionDetail detail;
                  try
                  {
                     detail = PSConnectionHelper.getConnectionDetail(connInfo);
                  }
                  catch (NamingException e)
                  {
                     throw new PSConsoleCommandException(
                        IPSServerErrors.RAW_DUMP, e.getLocalizedMessage());
                  }
                  catch (SQLException e)
                  {
                     throw new PSConsoleCommandException(
                        IPSServerErrors.RAW_DUMP, 
                        PSSqlException.getFormattedExceptionText(e));
                  }
                  m_origin = detail.getOrigin();
               }
               flushed = dbmd.flushTableMetaData(m_table, m_origin);
            }

            if (flushed)
               msg = "Table cache has been flushed";
            else
               msg = "No table meta data located to flush";
         }
         else
         {
            // flush the whole db
            boolean flushed = PSMetaDataCache.flushDatabaseMetaData(
               m_datasource);

            if (flushed)
               msg = "Database cache has been flushed";
            else
               msg = "No database meta data located to flush";
         }
      }

      PSXmlDocumentBuilder.addElement(respDoc, root, "resultText", msg);

      return respDoc;
   }


   /**
    * Constructs text to display proper usage of this command.
    *
    * @return The help text.
    */
   private String getHelpText()
   {
     StringBuffer buf = new StringBuffer();

     buf.append(NEW_LINE);
     buf.append("Format of this command is:");
     buf.append(NEW_LINE);
     buf.append("flush dbmd -d [datasource] -t [table]");
     buf.append(NEW_LINE);
     buf.append("or");
     buf.append(NEW_LINE);
     buf.append("flush dbmd [-h]");
     buf.append(NEW_LINE);
     buf.append("The switches are not case sensitive, and the space between ");
     buf.append("the switch and its value is optional.  Switches may be ");
     buf.append("specified in any order.  Driver and Server must be provided,");
     buf.append(" and an origin may only be supplied if a table has been ");
     buf.append("specified as well.  If the -h switch or no switches ");
     buf.append("are passed, then the help for this command is displayed.");
     buf.append(NEW_LINE);
                 
     return buf.toString();
   }

   /**
    * allow package members to see our command name
    */
   final static String   ms_cmdName = "flush dbmd";


   /**
    * Datasource of the data to be flushed.  Initialized in the
    * ctor, may be <code>null</code> or empty, never modified after that.
    */
   private String m_datasource = null;

   /**
    * Name of the table that will have data flushed.  Initialized in the
    * ctor, may be <code>null</code>, never empty modified after that.
    */
   private String m_table = null;

   /**
    * Origin of the table that will have data flushed.  Initialized in the
    * ctor, may be <code>null</code>, never empty modified after that.
    */
   private String m_origin = null;

   /**
    * If <code>true</code>, indicates if help text is to be displayed in lieu of
    * executing a flush command.  If <code>false</code>, then a flush command
    * will be executed.
    */
   private boolean m_help = false;

   /**
    * Character used as part of a switch to specify the datasource.  Never <code>
    * null</code> or empty.
    */
   private static final char OPTION_DATASOURCE = 'd';

   /**
    * Character used as part of a switch to specify the table.  Never <code>
    * null</code> or empty.
    */
   private static final char OPTION_TABLE = 't';

   /**
    * Character used as part of a switch to request help.  Never <code>
    * null</code> or empty.
    */
   private static final char OPTION_HELP = 'h';

   /**
    * Character used as prefix of a switch.  Never <code>null</code> or empty.
    */
   private static final String OPTION_SWITCH = "-";

   /**
    * Character used to specifiy a newline in any text that is displayed.  Never
    * <code>null</code> or empty.
    */
   private static final String NEW_LINE = System.getProperty("line.separator");
}
