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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.services.datasource.PSDatasourceMgrLocator;
import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionInfo;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.sql.SQLException;

import javax.naming.NamingException;

/**
 * Console command to dump all active datasource configurations. Here is an
 * example of the command output:
 * 
 * <pre><code>
 *  &lt;datasources&gt;
 *    &lt;datasource name=&quot;rxdefault&quot; 
 *          isCmsRepository=&quot;yes&quot;&gt;  
 *       &lt;jndiDatsourceName&gt;jdbc/rxdefault&lt;/jndiDatasourceName&gt;
 *       &lt;jdbcUrl&gt;jdbc:jtds:sqlserver://bender&lt;/jdbcUrl&gt;
 *       &lt;database&gt;rxRhino&lt;/database&gt;
 *       &lt;schema&gt;dbo&lt;/schema&gt;
 *    &lt;/datasource&gt;  
 *  &lt;/datasources&gt;
 * </code></pre>
 */
public class PSConsoleCommandDumpDatasources extends PSConsoleCommand
{
   // see base class
   public PSConsoleCommandDumpDatasources(String cmdArgs)
      throws PSIllegalArgumentException
   {
      super(cmdArgs);

      // there should be no other args for this command
      if ((cmdArgs != null) && (cmdArgs.length() > 0))
      {
         Object[] args =
         {ms_cmdName, cmdArgs};
         throw new PSIllegalArgumentException(
            IPSServerErrors.RCONSOLE_UNEXPECTED_ARGS, args);
      }
   }

   // see base class
   public Document execute(PSRequest request) throws PSConsoleCommandException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = PSXmlDocumentBuilder.createRoot(doc,
         "PSXConsoleCommandResults");
      PSXmlDocumentBuilder.addElement(doc, root, "command", ms_cmdName);
      Element datasources = PSXmlDocumentBuilder.addEmptyElement(doc, root, 
         "datasources");
      
      IPSDatasourceManager dsMgr = PSDatasourceMgrLocator.getDatasourceMgr();
      String defaultDs = dsMgr.getRepositoryDatasource();
      
      try
      {
         for (String dsName : dsMgr.getDatasources())
         {
            PSConnectionDetail detail = dsMgr.getConnectionDetail(
               new PSConnectionInfo(dsName));

            // detail should not be null since the mgr gave us the name
            if (detail == null)
            {
               throw new RuntimeException("No conn detail found for: " + 
                  dsName);
            }

            Element dsEl = PSXmlDocumentBuilder.addEmptyElement(doc,
               datasources, "datasource");
            
            dsEl.setAttribute("name", dsName);
            dsEl.setAttribute("isCmsRepository", dsName.equals(defaultDs) ? 
               "yes" : "no");
            PSXmlDocumentBuilder.addElement(doc, dsEl, "jndiDatasourceName", 
               detail.getDatasourceName());
            PSXmlDocumentBuilder.addElement(doc, dsEl, "jdbcUrl", 
               detail.getJdbcUrl());
            PSXmlDocumentBuilder.addElement(doc, dsEl, "database", 
               detail.getDatabase());
            PSXmlDocumentBuilder.addElement(doc, dsEl, "schema", 
               detail.getOrigin());
         }
      }
      catch (NamingException e)
      {
         throw new PSConsoleCommandException(
            IPSServerErrors.RCONSOLE_COMMAND_EXCEPTION, 
            e.getLocalizedMessage());
      }
      catch (SQLException e)
      {
         throw new PSConsoleCommandException(
            IPSServerErrors.RCONSOLE_COMMAND_EXCEPTION, 
            e.getLocalizedMessage());
      }      
      
      return doc;
   }

   /**
    * allow package members to see our command name
    */
   final static String   ms_cmdName = "dump datasources";
}
