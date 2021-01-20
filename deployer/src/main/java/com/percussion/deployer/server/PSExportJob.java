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
package com.percussion.deployer.server;

import com.percussion.deployer.client.IPSDependencySuppressor;
import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.client.PSCollectionDependencySuppressor;
import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.deployer.objectstore.PSArchive;
import com.percussion.deployer.objectstore.PSArchiveDetail;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSDependencyTreeContext;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSDescriptor;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.rx.config.IPSConfigService;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.server.job.IPSJobErrors;
import com.percussion.server.job.PSJobException;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.util.PSFormatVersion;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import java.io.File;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;

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
         if (m_descriptor.getPackages().hasNext())
         {
            initDepCount(m_descriptor.getPackages());
         }
         
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
         
         String category = PSPkgInfo.PackageCategory.USER.name();
         IPSPkgInfoService pkgSvc = PSPkgInfoServiceLocator.getPkgInfoService();
         PSPkgInfo pkgInfo = pkgSvc.findPkgInfo(m_descriptor.getName());
         if (pkgInfo != null)
         {
            category = pkgInfo.getCategory().name();
         }
         
         PSArchiveInfo info = new PSArchiveInfo(m_descriptor.getName(), 
            PSServer.getHostName() + ":" + PSServer.getListenerPort(), 
            m_serverVersion, dbmsHelper.getServerRepositoryInfo(), getUserId(),
            category);
         PSArchiveDetail detail = new PSArchiveDetail(m_descriptor);
         info.setArchiveDetail(detail);
         archive = new PSArchive(archiveFile, info);
         PSArchiveHandler ah = new PSArchiveHandler(archive);
         
         // Handle adding config files into archive if they have been specified
         boolean hasConfigDef = StringUtils.isNotBlank(
            m_descriptor.getConfigDefFile());
         boolean hasLocalConfig = StringUtils.isNotBlank(
            m_descriptor.getLocalConfigFile());
         File config = null;
         String configRef = null;
         
         if(hasConfigDef)
         {
            configRef = m_descriptor.getName() + "_" + "configDef"; 
            config = dh.getConfigTempFile(configRef);
            if(!config.exists() || !config.isFile())
               throw new PSJobException(
                  IPSJobErrors.CONFIG_FILE_NOT_FOUND, 
                  m_descriptor.getConfigDefFile());
            archive.storeFile(config, 
               PSDescriptor.getConfigArchiveEntryPath(
                  IPSConfigService.ConfigTypes.CONFIG_DEF));
         }
         if(hasLocalConfig)
         {
            configRef = m_descriptor.getName() + "_" + "localConfig"; 
            config = dh.getConfigTempFile(configRef);
            if(!config.exists() || !config.isFile())
               throw new PSJobException(
                  IPSJobErrors.CONFIG_FILE_NOT_FOUND, 
                  m_descriptor.getLocalConfigFile());
            archive.storeFile(config, 
               PSDescriptor.getConfigArchiveEntryPath(
                  IPSConfigService.ConfigTypes.LOCAL_CONFIG));
         }         
         
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
         LogFactory.getLog(getClass()).error("Error creating Deployer package", 
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
      return "Create Package Job";
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
