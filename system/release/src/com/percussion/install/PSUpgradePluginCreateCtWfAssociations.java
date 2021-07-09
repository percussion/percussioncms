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
package com.percussion.install;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSWorkflowInfo;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.tablefactory.PSJdbcDbmsDef;
import com.percussion.util.PSCollection;
import com.percussion.util.PSSqlHelper;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Upgrade plugin for creating the content type workflow associations. Gets the
 * workflow info object from the content editors and creates the workflow
 * content type associations. Avoids duplicates and non existsing workflows.
 * Skips the processing if failed to get the workflows.
 * 
 * @author bjoginipally
 * 
 */
public class PSUpgradePluginCreateCtWfAssociations extends
      PSSpringUpgradePluginBase
{

   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      m_config = config;
      logIt("Creating Content Types Workflow Associations");
      PSPluginResponse response = null;
      m_workflows = getWorkflows();
      if (m_workflows == null || m_workflows.isEmpty())
      {
         // This should not happen, but incase happens log it
         logIt("Error occurred getting the workflows from the "
               + "WORKFLOWAPPS table. Skipping process of Content Types "
               + "Workflow Associations plugin.");
         return response;
      }
      m_associations = getCtWfAssociations();
      try
      {
         File objDir = RxUpgrade.getObjectStoreDir();
         File[] appFiles = objDir.listFiles();

         if (appFiles == null)
         {
            logIt("Error occurred accessing objectstore directory "
                  + objDir.getAbsolutePath());
            return response;
         }

         for (int i = 0; i < appFiles.length; i++)
         {
            File appFile = appFiles[i];
            String appFileName = appFile.getName();

            if (appFile.isDirectory() || !appFileName.endsWith(".xml")
                  || PSPreUpgradePluginLocalCreds.isSystemApp(appFileName))
               continue;
            if (RxUpgrade.isContentEditorApp(appFile, config.getLogStream()))
            {
               processAppFile(appFile);
            }
         }
      }
      catch (Exception e)
      {
         logIt(e);
      }

      logIt("Finished process() of the plugin Create "
            + "Content Types Workflow Associations...");
      return response;
   }

   /**
    * Processes supplied app file by loading it as an application and if the
    * application is of type content editor then calls
    * {@link #createCtWfAssociations(int, Iterator)} to create the content type
    * and workflow associations. If the app file is not of type content editor
    * skips it. Logs if there is any exception.
    * 
    * @param appFile app file that needs to be processed, assumed not
    * <code>null</code>.
    */
   private void processAppFile(File appFile)
   {
      FileInputStream oldIn = null;
      try
      {
         oldIn = new FileInputStream(appFile);
         Document oldDoc = PSXmlDocumentBuilder
               .createXmlDocument(oldIn, false);
         PSApplication app = new PSApplication(oldDoc);
         PSCollection datasets = app.getDataSets();
         for (int i = 0; i < datasets.size(); i++)
         {
            Object obj = datasets.get(i);
            if (obj instanceof PSContentEditor)
            {
               PSContentEditor ce = (PSContentEditor) obj;
               PSWorkflowInfo wfInfo = ce.getWorkflowInfo();
               if (wfInfo == null)
               {
                  String msg = "Skipping the content type ({0}) as "
                        + "the workflow info is null.";
                  Object[] args = { ce.getContentType() };
                  logIt(MessageFormat.format(msg, args));
                  continue;
               }
               else if (wfInfo.getType().equals(
                     PSWorkflowInfo.TYPE_EXCLUSIONARY))
               {
                  String msg = "Skipping the content type ({0}) as "
                        + "the workflow info is exclusionary.";
                  Object[] args = { ce.getContentType() };
                  logIt(MessageFormat.format(msg, args));
                  continue;
               }
               createCtWfAssociations(ce.getContentType(), wfInfo.getValues());
            }
         }
      }
      catch (Exception e)
      {
         String msg = "Failed to process the app file {0} while creating "
               + "the content type workflow associations.";
         Object[] args = { appFile.getAbsolutePath() };
         logIt(MessageFormat.format(msg, args));
         logIt(e);
      }
      finally
      {
         closeObject(oldIn);
      }
   }

   /**
    * Helper method to insert the content type workflow association data in to
    * PSX_CONTENTTYPE_WORKFLOW table. Logs if there is any exception.
    * 
    * @param id Content Type id.
    * @param wfs iterator of Integer objects of workflows. Assumed not
    * <code>null</code>.
    * @throws Exception
    */
   private void createCtWfAssociations(long id, Iterator wfs)
   {
      Connection conn = null;
      try
      {
         conn = RxUpgrade.getJdbcConnection();
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(RxUpgrade
               .getRxRepositoryProps());
         String qualTableName = PSSqlHelper.qualifyTableName(
               "PSX_CONTENTTYPE_WORKFLOW", dbmsDef.getDataBase(), dbmsDef
                     .getSchema(), dbmsDef.getDriver());
         String SQL_START = "INSERT INTO " + qualTableName + " VALUES (";
         String SQL_END = ")";
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         // Create a set of Integers to avoid duplicates.
         Set<Integer> wfSet = new HashSet<>();
         while (wfs.hasNext())
         {
            Integer wf = (Integer) wfs.next();
            if (m_workflows.contains(wf))
            {
               wfSet.add(wf);
            }
            else
            {
               String msg = "Skipping the association of workflow ({0}) "
                     + "with content type ({1}) as the workflow does not "
                     + "exist in the system.";
               Object[] args = { wf, id };
               logIt(MessageFormat.format(msg, args));
            }
         }
         // Add each workflow to the table.
         for (Integer wf : wfSet)
         {
            // If association exists simply continue
            if (m_associations.contains(new CtWfAssociation(Integer
                  .parseInt(id + ""), wf)))
               continue;
            String values = gmgr.createGuid(PSTypeEnum.INTERNAL).longValue()
                  + ", 0, " + id + ", " + wf;
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(SQL_START + values + SQL_END);
         }
      }
      catch (Exception e)
      {
         String msg = "Failed to insert the content type workflow "
               + "associations for content type ({0})";
         Object[] args = { id };
         logIt(MessageFormat.format(msg, args));
         logIt(e);
      }
      finally
      {
         closeConnection(conn);
      }
   }

   /**
    * Helper methos to get the workflows from the system.
    * 
    * @return List of workflows may be empty but never <code>null</code>.
    */
   private List<Integer> getWorkflows()
   {
      List<Integer> wfList = new ArrayList<>();
      Connection conn = null;
      try
      {
         conn = RxUpgrade.getJdbcConnection();
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(RxUpgrade
               .getRxRepositoryProps());
         String qualTableName = PSSqlHelper
               .qualifyTableName("WORKFLOWAPPS", dbmsDef.getDataBase(),
                     dbmsDef.getSchema(), dbmsDef.getDriver());
         String SQL_STMT = "SELECT WORKFLOWAPPID FROM " + qualTableName;
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(SQL_STMT);
         while (rs.next())
         {
            wfList.add(new Integer(rs.getInt("WORKFLOWAPPID")));
         }
      }
      catch (Exception e)
      {
         String msg = "Failed to get the workflows.";
         logIt(msg);
         logIt(e);
      }
      finally
      {
         closeConnection(conn);
      }
      return wfList;
   }

   /**
    * Helper methos to get the workflows from the system.
    * 
    * @return List of workflows may be empty but never <code>null</code>.
    */
   private List<CtWfAssociation> getCtWfAssociations()
   {
      List<CtWfAssociation> ctWfs = new ArrayList<>();
      Connection conn = null;
      try
      {
         conn = RxUpgrade.getJdbcConnection();
         PSJdbcDbmsDef dbmsDef = new PSJdbcDbmsDef(RxUpgrade
               .getRxRepositoryProps());
         String qualTableName = PSSqlHelper.qualifyTableName(
               "PSX_CONTENTTYPE_WORKFLOW", dbmsDef.getDataBase(), dbmsDef
                     .getSchema(), dbmsDef.getDriver());
         String SQL_STMT = "SELECT CONTENTTYPEID,WORKFLOWID FROM "
               + qualTableName;
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(SQL_STMT);
         while (rs.next())
         {
            ctWfs.add(new CtWfAssociation(new Integer(rs
                  .getInt("CONTENTTYPEID")), new Integer(rs
                  .getInt("WORKFLOWID"))));
         }
      }
      catch (Exception e)
      {
         String msg = "Failed to get the workflow associations.";
         logIt(msg);
         logIt(e);
      }
      finally
      {
         closeConnection(conn);
      }
      return ctWfs;
   }

   /**
    * Simple data class to hold content type and workflow association.
    * 
    * @author bjoginipally
    * 
    */
   class CtWfAssociation
   {
      int mi_ct;

      int mi_wf;

      CtWfAssociation(int ct, int wf)
      {
         mi_ct = ct;
         mi_wf = wf;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (!(obj instanceof CtWfAssociation))
            return false;
         CtWfAssociation second = (CtWfAssociation) obj;
         return new EqualsBuilder().append(mi_ct, second.mi_ct).append(mi_wf,
               second.mi_wf).isEquals();
      }

      @Override
      public int hashCode()
      {
         return new HashCodeBuilder().append(mi_ct).append(mi_wf).toHashCode();
      }
   }

   /**
    * Helper method to close closable object if it is not null. The exception
    * thrown while closing the object is ignored.
    * 
    * @param obj a closeable object that needs to be closed.
    */
   private void closeObject(Closeable obj)
   {
      if (obj != null)
      {
         try
         {
            obj.close();
         }
         catch (Exception e)
         {
            // ignore
         }
      }
   }

   /**
    * Helper method to close the supplied SQL connection if it is not null. The
    * exception thrown while closing the object is ignored.
    * 
    * @param conn SQL connection to close.
    */
   private void closeConnection(Connection conn)
   {
      if (conn != null)
      {
         try
         {
            conn.close();
         }
         catch (SQLException e)
         {
            // ignore
         }
      }
   }

   /**
    * Prints message to the log printstream if it exists
    * or just sends it to System.out
    *
    * @param msg the message to be logged, can be <code>null</code>.
    */
   private static void logIt(Object o)
   {
      if (o instanceof Throwable)
      {
         ((Throwable) o).printStackTrace(m_config.getLogStream());
      }
      else if (o instanceof String)
      {
         m_config.getLogStream().println(o.toString());
      }
   }

   /**
    * List of workflows from WORKFLOWAPPS table. Intialized in ctor never
    * <code>null</code> after that.
    */
   private List<Integer> m_workflows = null;

   /**
    * List of existing Content Type, Workflow associations. Intialized in ctor
    * never <code>null</code> after that.
    */
   private List<CtWfAssociation> m_associations = null;

   /**
    * The config object, initialized in ctor never <code>null</code> after that.
    */
   private static IPSUpgradeModule m_config;

}
