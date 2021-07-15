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
package com.percussion.deploy.server;

import com.percussion.deploy.client.IPSDependencySuppressor;
import com.percussion.deploy.client.IPSDeployConstants;
import com.percussion.deploy.client.PSCollectionDependencySuppressor;
import com.percussion.deploy.client.PSDeploymentManager;
import com.percussion.deploy.error.IPSDeploymentErrors;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSArchive;
import com.percussion.deploy.objectstore.PSArchiveDetail;
import com.percussion.deploy.objectstore.PSArchiveInfo;
import com.percussion.deploy.objectstore.PSDependencyTreeContext;
import com.percussion.deploy.objectstore.PSDeployableElement;
import com.percussion.deploy.objectstore.PSExportDescriptor;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.job.IPSJobErrors;
import com.percussion.server.job.PSJobException;
import com.percussion.util.PSFormatVersion;

import java.io.File;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Document;

/**
 * Job to create a deployment archive from an export descriptor.  Archive 
 * created will be named using the export descriptor name.
 */
public class PSExportJob extends PSDeployJob
{
   /**
    * Restores the export descriptor from the supplied document, and validates
    * that the user is authorized to perform this job.  Saves the security token
    * from the request to use for subsequent operations during the run method.
    * <br>
    * See {@link com.percussion.server.job.PSJobRunner#init(int, Document, 
    * PSRequest, Properties) PSJobRunner.init()} for more info.
    */
   @Override
   public void init(int id, Document descriptor, PSRequest req, 
      Properties initParams) 
      throws PSAuthenticationFailedException, PSAuthorizationException, 
         PSJobException
   {
      if (descriptor == null)
         throw new IllegalArgumentException("descriptor may not be null");
         
      if (req == null)
         throw new IllegalArgumentException("req may not be null");

      super.init(id, req, initParams);      
      
      try 
      {
         m_descriptor = new PSExportDescriptor(descriptor.getDocumentElement());
         if (!m_descriptor.getPackages().hasNext())
         {
            PSDeployException ex = new PSDeployException(
               IPSDeploymentErrors.EMPTY_PACKAGE_LIST);
            throw new PSJobException(IPSJobErrors.INVALID_JOB_DESCRIPTOR, 
               ex.getLocalizedMessage());
         }
         initDepCount(m_descriptor.getPackages());
      }
      catch (PSUnknownNodeTypeException e) 
      {
         throw new PSJobException(IPSJobErrors.INVALID_JOB_DESCRIPTOR, 
            e.getLocalizedMessage());
      }
      
      m_serverVersion = new PSFormatVersion("com.percussion.util");
   }
   
   /**
    * Runs this export job.  Creates an archive file and stores all files in it 
    * that will be required to deploy the items specified by the descriptor
    * supplied to the <code>init()</code> method.
    */
   @SuppressWarnings("unchecked")
   @Override
   public void doRun() 
   {
      PSArchive archive = null;
      PSDbmsHelper dbmsHelper = null;
      PSDependencyManager dm = null;
      try 
      {
         ResourceBundle bundle = PSDeploymentManager.getBundle();
         setStatusMessage(bundle.getString("init"));
         
         // enable cache for non-system schema
         dbmsHelper = PSDbmsHelper.getInstance();
         dbmsHelper.enableSchemaCache();
         
         // make sure all dependencies are in the descriptor
         PSDeploymentHandler dh = PSDeploymentHandler.getInstance();
         dm = dh.getDependencyManager();
         
         // enable cache for dependencies
         dm.setIsDependencyCacheEnabled(true);
         
         // build a full tree context so included state of any added 
         // dependencies are updated
         PSDependencyTreeContext treeCtx = new PSDependencyTreeContext();
         Iterator pkgs = m_descriptor.getPackages();
         while (pkgs.hasNext())
         {
            PSDeployableElement de = (PSDeployableElement)pkgs.next();
            treeCtx.addPackage(de, true);
         }
         
         // add suppression filter from descriptor to context
         if (m_descriptor.getDepKeysToExclude() != null)
         {
            IPSDependencySuppressor suppressor = 
               new PSCollectionDependencySuppressor(
                     m_descriptor.getDepKeysToExclude());
            treeCtx.setDependencySuppressor(suppressor);
         }
         
         // now add missing deps
         pkgs = m_descriptor.getPackages();
         while (pkgs.hasNext()&& !isCancelled())
         {
            PSDeployableElement de = (PSDeployableElement)pkgs.next();
            String msg = MessageFormat.format(bundle.getString("analyzingDeps"), 
               new Object[] {de.getDisplayIdentifier()});
            setStatusMessage(msg);
            dm.addMissingDependencies(getSecurityToken(), de, treeCtx, this);            
         }

         // create the archive
         File archiveFile = new File(PSDeploymentHandler.EXPORT_ARCHIVE_DIR, 
            m_descriptor.getName() + IPSDeployConstants.ARCHIVE_EXTENSION);
         archiveFile.getParentFile().mkdirs();
         archiveFile.deleteOnExit();
         
         PSArchiveInfo info = new PSArchiveInfo(m_descriptor.getName(), 
            PSServer.getHostName() + ":" + PSServer.getListenerPort(), 
            m_serverVersion, dbmsHelper.getServerRepositoryInfo(), getUserId());
         PSArchiveDetail detail = new PSArchiveDetail(m_descriptor);
         info.setArchiveDetail(detail);
         archive = new PSArchive(archiveFile, info);
         PSArchiveHandler ah = new PSArchiveHandler(archive);
         
         pkgs = m_descriptor.getPackages();
         while (pkgs.hasNext() && !isCancelled())
         {
            PSDeployableElement de = (PSDeployableElement)pkgs.next();
            String msg = MessageFormat.format(bundle.getString("processing"), 
               new Object[] {de.getDisplayIdentifier()});
            setStatusMessage(msg);
            dm.addToArchive(getSecurityToken(), de, ah, this);
         }
         ah.close();
         if (!isCancelled())
         {
            setStatus(100);  
            setStatusMessage(bundle.getString("completed"));       
         }
      }
      catch (Exception ex) 
      {
         setStatusMessage("error: " + ex.getLocalizedMessage());
         setStatus(-1);
         LogManager.getLogger(getClass()).error("Error creating MSM archive", 
            ex);         
      }
      finally
      {
         // disable non-system schema cache before releasing job lock
         if (dbmsHelper != null)
            dbmsHelper.disableSchemaCache();
         
         // disable dependency caching before releasing job lock
         if (dm != null)
            dm.setIsDependencyCacheEnabled(false);
         
         setCompleted();
            
         if (archive != null)
            archive.close();
      }
      
   }
   
   // see base class
   protected String getJobType()
   {
      return "Create Archive Job";
   }
 
   /** 
    * The export descriptor supplied to the <code>init()</code> method, never
    * <code>null</code> or modified after that.
    */
   private PSExportDescriptor m_descriptor;
   
   /**
    * Contains the version info of the server on which this job is running,
    * initialized during the <code>init()</code> method, never <code>null</code>
    * or modified after that.
    */
   private PSFormatVersion m_serverVersion;
   
}
