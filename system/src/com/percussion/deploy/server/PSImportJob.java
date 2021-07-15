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

import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.deploy.client.IPSDeployConstants;
import com.percussion.deploy.client.PSDeploymentManager;
import com.percussion.deploy.error.PSDeployException;
import com.percussion.deploy.objectstore.PSArchive;
import com.percussion.deploy.objectstore.PSArchiveInfo;
import com.percussion.deploy.objectstore.PSArchivePackage;
import com.percussion.deploy.objectstore.PSDbmsInfo;
import com.percussion.deploy.objectstore.PSDependency;
import com.percussion.deploy.objectstore.PSDeployableElement;
import com.percussion.deploy.objectstore.PSImportDescriptor;
import com.percussion.deploy.objectstore.PSImportPackage;
import com.percussion.deploy.objectstore.PSTransactionSummary;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServerLockManager;
import com.percussion.server.PSServerLockResult;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.server.job.IPSJobErrors;
import com.percussion.server.job.PSJobException;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;


/**
 * Job to install objects from a deployment archive using an import descriptor.  
 */
public class PSImportJob  extends PSDeployJob
{
   private static final Logger log = LogManager.getLogger(PSImportJob.class);
   /**
    * Restores the import descriptor from the supplied document, and validates
    * that the user is authorized to perform this job.  Saves the security token
    * from the request to use for subsequent operations during the run method.
    * Gets a server lock on the publisher so that editions will not publish
    * while we are installing.
    * <br>
    * See base class for more info.
    */
   @SuppressWarnings("unchecked")
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
         m_descriptor = new PSImportDescriptor(descriptor.getDocumentElement());
         List<PSDeployableElement> pkgList = new ArrayList<>();
         Iterator importPkgs = m_descriptor.getImportPackageList().iterator();
         while (importPkgs.hasNext())
         {
            PSImportPackage importPkg = (PSImportPackage)importPkgs.next();
            pkgList.add(importPkg.getPackage());
         }
         
         // we must acquire the pub lock
         PSServerLockManager lockMgr = PSServerLockManager.getInstance();
         PSServerLockResult result = lockMgr.acquireLock(
            PSServerLockManager.RESOURCE_PUBLISHER, 
            PSDeploymentHandler.DEPLOY_SUBSYSTEM + ":" + getUserId());
         
         if (!result.wasLockAcquired())
         {
            throw new PSJobException(result.formatLockException());
         }
         else
            m_pubLockId = result.getLock().getLockId();
         
         // init dependency count for status messages
         initDepCount(pkgList.iterator());
      }
      catch (PSUnknownNodeTypeException e) 
      {
         throw new PSJobException(IPSJobErrors.INVALID_JOB_DESCRIPTOR, 
            e.getLocalizedMessage());
      }
   }
   /**
    * Runs this import job.  Installs all required files and data from the 
    * archive specified by the descriptor supplied to the <code>init()</code>
    * method.
    */
   @SuppressWarnings("unchecked")
   @Override
   public void doRun() 
   {
      PSArchive archive = null;
      PSItemDefManager itemDefManager = PSItemDefManager.getInstance();
      PSImportCtx ctx = null;
      try 
      {
         itemDefManager.deferUpdateNotifications();
         // initialize next id in memory for each installation.
         PSDbmsHelper dbmsHelper = PSDbmsHelper.getInstance();
         dbmsHelper.clearNextIdInMemory();
         
         ResourceBundle bundle = PSDeploymentManager.getBundle();
         setStatusMessage(bundle.getString("init"));
         
         // locate and load the archive
         PSArchiveInfo info = m_descriptor.getArchiveInfo();
         
         File archiveFile = new File(PSDeploymentHandler.IMPORT_ARCHIVE_DIR, 
            info.getArchiveRef() + IPSDeployConstants.ARCHIVE_EXTENSION);
         archiveFile.getParentFile().mkdirs();
         archive = new PSArchive(archiveFile);
         // get the full info
         info = archive.getArchiveInfo(true);
         List importList = m_descriptor.getImportPackageList();
         PSArchiveHandler ah = new PSArchiveHandler(archive);
         
         // create the import context
         PSDeploymentHandler dh = PSDeploymentHandler.getInstance();
         PSLogHandler lh = dh.getLogHandler();
         ctx = new PSImportCtx(getUserId(), info.getRepositoryInfo(),
               PSDbmsMapManager.getDbmsMap(info.getServerName()), dh
                     .getIdMapMgr(), lh, dh.getAppPolicySettings());
         
         PSDbmsInfo sourceRepository = ctx.getSourceRepository();
         PSDbmsInfo tgtRepository = dbmsHelper.getServerRepositoryInfo();
         if (!tgtRepository.isSameDb(sourceRepository))
            ctx.setCurrentIdMap(ctx.getIdMapMgr().getIdmap(
               sourceRepository.getDbmsIdentifier()));
            
         //create the archive log
         int archiveLogId = lh.createArchiveLog(info, 
            archive.getArchiveManifest());
         ctx.setArchiveLogId(archiveLogId);
         
         // walk the packages and install
         PSDependencyManager dm = dh.getDependencyManager();
         Iterator pkgs = importList.iterator();
         while (pkgs.hasNext()  && !isCancelled())
         {
            PSImportPackage pkg = (PSImportPackage)pkgs.next();
            
            // create a package log for the current package
            int pkgLogId = lh.createPackageLog(archiveLogId, pkg, ctx);
            ctx.setPackageLogId(pkgLogId);
            
            // install the package file
            PSDeployableElement de = pkg.getPackage();
            
            // set the package's validation results on the ctx
            ctx.setCurrentValidationResults(pkg.getValidationResults());
            
            String msg = MessageFormat.format(bundle.getString("processing"), 
               new Object[] {de.getDisplayIdentifier()});
            setStatusMessage(msg);
            int status = PSArchivePackage.STATUS_COMPLETED;
            Exception restoreEx = null;
            try 
            {
               dm.restoreFromArchive(getSecurityToken(), de, ah, ctx, this);
               if (isCancelled())
                  status = PSArchivePackage.STATUS_ABORTED;
            }
            catch (PSDeployException ex) 
            {
               status = PSArchivePackage.STATUS_ABORTED;
               restoreEx = ex;
               
            }
            
            lh.updatePackageStatus(archiveLogId, pkgLogId, de.getObjectType(), 
               de.getDependencyId(), status);
            if (restoreEx != null)
               throw restoreEx;
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
         addTransactionLogForFailure(ctx,  ex);
         setStatusMessage("error: " + ex.getLocalizedMessage());
         ctx.setCurrentDependency(null);
         setStatus(-1);
         log.error("Error installing MSM archive", ex.getMessage());
      }
      finally
      {
         // be sure we release the pub lock
         PSServerLockManager.getInstance().releaseLock(m_pubLockId);
         
         setCompleted();
         if (archive != null)
            archive.close();
            
         // flush entire cache since we don't know what was modified
         PSCacheManager.getInstance().flush();
         
         // Cause the item def manager to finish update notification work
         itemDefManager.commitUpdateNotifications();
      }
      
   }
   
   /**
    * In case of any failures, a transaction log entry for failure is created
    * for the package
    * @param ctx the import context never <code>null</code>
    * @param ex the exception that was thrown cannot be <code>null</code>
    */
   private void addTransactionLogForFailure(PSImportCtx ctx,  
          Exception ex) 
   {
      if ( ctx == null )
         throw new IllegalArgumentException("Import context may not be null");
      
      int logId = ctx.getPackageLogid();
      PSDependency dep = ctx.getCurrentDependency(); 
      // do nothing if dependency is not found
      if ( dep == null )
         return;
      
      String depString = dep.getObjectTypeName() + " \"" + dep.getDisplayName() + 
      "\" (" + dep.getDependencyId() + ")";
      depString += " : " + ex.getLocalizedMessage();
      int action = PSTransactionSummary.ACTION_FAILED_TO_INSTALL;
      PSLogHandler lh = ctx.getLogHandler();
      try
      {
         lh.addTransactionLogEntry(logId, depString,
               PSTransactionSummary.TYPE_SKIPPED,
               PSTransactionSummary.TYPE_SKIPPED, action, ctx
                     .getNextTxnSequence(logId));
      }
      catch (PSDeployException e)
      {
      }
   }
   
   
   // see base class
   protected String getJobType()
   {
      return "Install Archive Job";
   }
   
   /**
    * The id of the publisher lock which must be acquired by the 
    * <code>init()</code> method and released by the <code>run()</code> method.
    */
   private int m_pubLockId = -1;
 
   /** 
    * The import descriptor supplied to the <code>init()</code> method, never
    * <code>null</code> or modified after that.
    */
   private PSImportDescriptor m_descriptor;

}
