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

package com.percussion.server.command;

import com.percussion.error.PSIllegalArgumentException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.services.datasource.PSDatasourceMgrLocator;
import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionInfo;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.naming.NamingException;
import java.sql.SQLException;

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
