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
package com.percussion.server;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.conn.PSServerException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The PSRemoteConsole class is used for the submission of remote
 * console commands to an E2 server.
 * <p>
 * To use the remote console, a connection to the E2 server must first be 
 * established. Requests can then be made through this object. This is
 * done by sending the command string through the
 * <code>{@link #execute execute}</code> method. This method will pass
 * the command string to the server. If the submitter does not have admin
 * access to the server, an exception will be thrown. If access is granted,
 * the command will be interpreted and executed. An XML document will
 * be returned containing the results of the command.
 * <p>
 * The following sample shows how to get the list of applications
 * running on a server.
 * <pre><code>
 *      try {
 *         Properties connProps = new Properties();
 *         props.put("hostName", "myserver");
 *         props.put("loginId",  "myid");
 *         props.put("loginPw",  "mypw");
 *
 *         PSDesignerConnection   conn      = new PSDesignerConnection(connProps);
 *         PSRemoteConsole      console   = new PSRemoteConsole(conn);
 *         
 *         Document            xmlDoc   = console.execute("show active applications");
 *         PSXmlTreeWalker   walker   = new PSXmlTreeWalker(xmlDoc);
 *         Node               saveCur;
 *         
 *         // list the active apps
 *         System.out.println("Active applications:");
 *         if (walker.getNextElement("Application", true, true) != null) {
 *            saveCur = walker.getCurrent();
 *            while (walker.getNextElement("name", true, true) != null)
 *               System.out.println(walker.getElementData("name", false));
 *            walker.setCurrent(saveCur);
 *         }
 *      }
 *      catch (Exception e) {
 *         e.printStackTrace();
 *      }
 * </code></pre>
 *
 * <P>
 * For a description of the XML document structures being returned, see the
 * {@link PSRemoteConsoleHandler PSRemoteConsoleHandler} class.
 *
 * @author      Tas Giakouminakis
 * @version      1.0
 * @since      1.0
 */
public class PSRemoteConsole
{
   /**
    * Creates a remote console connected to the specified E2 server.
    *
    * @param      conn                        the connection object for the
    *                                          desired E2 server
    *
    * @exception   PSIllegalArgumentException   if <code>conn</code> is
    *                                          <code>null</code>
    */
   public PSRemoteConsole(PSDesignerConnection conn)
      throws PSIllegalArgumentException
   {
      super();

      if (conn == null)
         throw new PSIllegalArgumentException(IPSServerErrors.RCONSOLE_CONN_OBJ_NULL);

      m_conn = conn;
   }

   /**
    * Execute a remote console command against the connected server.
    * Currently supported commands are:
    *   <TABLE BORDER="1">
    *      <TR>
    *         <TH>Command</TH>
    *         <TH>Parameters</TH>
    *         <TH>Description</TH>
    *      </TR>
    *      <TR>
    *         <TD rowspan="2">start</TD>
    *         <TD>server</TD>
    *         <TD>if the E2 server is down, this will start it</TD>
    *      </TR>
    *      <TR>
    *         <TD>application appName</TD>
    *         <TD>this will start the specified application if it is not
    *            currently running. If the application is set as disabled, it
    *            will be enabled.</TD>
    *      </TR>
    *      <TR>
    *         <TD rowspan="2">restart</TD>
    *         <TD>server</TD>
    *         <TD>this will shut down the E2 server and restart it</TD>
    *      </TR>
    *      <TR>
    *         <TD>restart application appName</TD>
    *         <TD>this will stop the specified application if it is
    *            currently running and restart it. If it is not running,
    *            it will be started</TD>
    *      </TR>
    *      <TR>
    *         <TD rowspan="2">stop</TD>
    *         <TD>server</TD>
    *         <TD>this will shut down the E2 server</TD>
    *      </TR>
    *      <TR>
    *         <TD>stop application appName</TD>
    *         <TD>this will stop the specified application if it is
    *            currently running.</TD>
    *      </TR>
    *      <TR>
    *         <TD rowspan="5">show</TD>
    *         <TD>status server</TD>
    *         <TD>general server statistics such as time running,
    *            performance metrics, etc.</TD>
    *      </TR>
    *      <TR>
    *         <TD>status application appName</TD>
    *         <TD>application statistics such as time running,
    *            performance metrics, etc.</TD>
    *      </TR>
    *      <TR>
    *         <TD>applications active</TD>
    *         <TD>return the list of active applications</TD>
    *      </TR>
    *      <TR>
    *         <TD>applications all</TD>
    *         <TD>return the list of all applications defined on the server</TD>
    *      </TR>
    *      <TR>
    *         <TD>applications disabled</TD>
    *         <TD>return the list of disabled applications</TD>
    *      </TR>
    *      <TR>
    *         <TD rowspan="2">log</TD>
    *         <TD>flush</TD>
    *         <TD>flush any queued log entries to the log</TD>
    *      </TR>
    *      <TR>
    *         <TD>dump</TD>
    *         <TD>dump the log, locating entries by date, type and/or app.
    *            The following arguments can be passed in along with this
    *            command:
    *            <UL>
    *               <LI>since 'YYYYMMDD HH:MM:SS' - used to locate all log
    *               entries since the specified date and time. Omit this
    *               to ignore the start time.</LI>
    *               <LI>until 'YYYYMMDD HH:MM:SS' - used to locate all log
    *               entries until the specified date and time. Omit this
    *               to ignore the end time.</LI>
    *               <LI>type [int] - the numeric log entry type. There can be
    *               multiple type [int] definitions to get multiple entry
    *               types (eg, type 1 type 7). Omit this to get all entry
    *               types. The entry types currently defined are:
    *               <OL TYPE="1">
    *                  <LI>errors</LI>
    *                  <LI>server start</LI>
    *                  <LI>server stop</LI>
    *                  <LI>application start</LI>
    *                  <LI>application stop</LI>
    *                  <LI>application statistics</LI>
    *                  <LI>basic user activity</LI>
    *                  <LI>detailed user activity</LI>
    *                  <LI>full user activity</LI>
    *                  <LI>warnings (informational messages)</LI>
    *                  <LI>mutiple application handlers</LI>
    *               </OL></LI>
    *               <LI>server - used to log messages originated by the server.
    *               If this and application are both omitted, all messages are
    *               returned. If this is omitted but at least one application
    *               is specified, server entries are not returned.</LI>
    *               <LI>application [id] - used to log messages originated by
    *               the specified application (using the specified application
    *               id).  There can be multiple application [id] definitions to
    *               get multiple application's logs
    *               (eg, application 1 application 2).
    *               If no applications are specified, and server is not
    *               specified, all entries are returned.</LI>
    *            </UL>
    *         </TD>
    *      </TR>
    * </TABLE>
    *
    * @param      command      the command string to execute
    *
    * @exception   PSIllegalArgumentException
    *                           if command is <code>null</code> or empty
    *
    * @exception   PSServerException
    *                           if the server is not responding
    *
    * @exception   PSAuthorizationException
    *                           if admin access to the server is denied
    *
    * @exception   java.io.IOException
    *                           if an I/O error occurs
    */
   public Document execute(String command)
      throws PSIllegalArgumentException, PSServerException,
               PSAuthorizationException, PSAuthenticationFailedException,
               java.io.IOException
   {
      if (command == null)
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_CMD_EMPTY);

      // remove any whitespace so we know if we have some valid text
      command = command.trim();
      if (command.length() == 0)
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_CMD_EMPTY);

      /* send the console command as an XML document
       * it's format is:
       *
       * <PSXRemoteConsoleCommand>
       *    <command>command string</command>
       * </PSXRemoteConsoleCommand>
       */
      Document sendDoc = PSXmlDocumentBuilder.createXmlDocument();

      Element root = PSXmlDocumentBuilder.createRoot(
         sendDoc, "PSXRemoteConsoleCommand");

      PSXmlDocumentBuilder.addElement(sendDoc, root, "command", command);

      /* send the request to the server */
      synchronized (m_conn) {
         m_conn.setRequestType("admin");
         return m_conn.execute(sendDoc);
      }
   }


   private PSDesignerConnection      m_conn;
}

