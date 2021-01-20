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

import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.deployer.error.PSDeployException;
import com.percussion.deployer.objectstore.PSArchive;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSArchivePackage;
import com.percussion.deployer.objectstore.PSDbmsInfo;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSDescriptor;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.deployer.objectstore.PSTransactionSummary;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.rx.config.IPSConfigRegistrationMgr;
import com.percussion.rx.config.IPSConfigService;
import com.percussion.rx.config.PSConfigServiceLocator;
import com.percussion.rx.services.deployer.PSPackageServiceHelper;
import com.percussion.rx.services.deployer.PSPackageVisibility;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServerLockManager;
import com.percussion.server.PSServerLockResult;
import com.percussion.server.cache.PSCacheManager;
import com.percussion.server.job.IPSJobErrors;
import com.percussion.server.job.PSJobException;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgDependency;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageAction;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageActionStatus;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageType;
import com.percussion.services.pkginfo.utils.PSPkgHelper;
import com.percussion.util.IOTools;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Job to install objects from a deployment archive using an import descriptor.
 */
public class PSImportJob extends PSDeployJob
{
   private static Log log = LogFactory.getLog(PSImportJob.class);
    
   /**
    * Restores the import descriptor from the supplied document, and validates
    * that the user is authorized to perform this job. Saves the security token
    * from the request to use for subsequent operations during the run method.
    * Gets a server lock on the publisher so that editions will not publish
    * while we are installing. <br>
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

      setApplyToAllComms(req);

      try
      {
         m_descriptor = new PSImportDescriptor(descriptor.getDocumentElement());

         // we must acquire the pub lock
         acquirePubLock();

         // set the archive file
         PSArchiveInfo info = m_descriptor.getArchiveInfo();         
         m_archiveFile = new File(PSDeploymentHandler.IMPORT_ARCHIVE_DIR,
               info.getArchiveRef() + IPSDeployConstants.ARCHIVE_EXTENSION);
         m_archiveFile.getParentFile().mkdirs();
         
         // init dependency count for status messages
         initDepCount();
      }
      catch (PSUnknownNodeTypeException e)
      {
         throw new PSJobException(IPSJobErrors.INVALID_JOB_DESCRIPTOR, e
               .getLocalizedMessage());
      }
   }


    private void initDepCount()
    {
        List<PSDeployableElement> pkgList = new ArrayList<PSDeployableElement>();
         Iterator importPkgs = m_descriptor.getImportPackageList().iterator();
         while (importPkgs.hasNext())
         {
            PSImportPackage importPkg = (PSImportPackage) importPkgs.next();
            pkgList.add(importPkg.getPackage());
         }         
         initDepCount(pkgList.iterator());
    }


    private void acquirePubLock() throws PSJobException
    {
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
    }
   
   /**
    * Standalone method to install an archive, used by server-side services, does not check authorization.
    * 
    * @param request The current user request, not <code>null</code>
    * @param archiveFile The archive file to install, not <code>null</code>
    * @param descriptor The import descriptor to use, not <code>null</code>
    * @param applytoAllComms <code>true</code> if installing a package that should be made visibible to all communities.
    * 
    * @throws PSJobException If there are any errors.
    */
   public void install(PSRequest request, File archiveFile, PSImportDescriptor descriptor, boolean applytoAllComms) throws PSJobException
   {
       Validate.notNull(request);
       Validate.notNull(archiveFile);
       Validate.notNull(descriptor);
       
       m_archiveFile = archiveFile;
       m_descriptor = descriptor;
       m_isApplyToAllComms = applytoAllComms;
       storeException = true;
       
       initUserInfo(request);
       acquirePubLock();
       initDepCount();

       doRun();
       
       if (runException != null)
       {
           log.error("Install Archive error :",runException);
           PSJobException jobEx = new PSJobException(IPSJobErrors.UNEXPECTED_ERROR, runException.getLocalizedMessage());
           runException = null;
           throw jobEx;
       }
   }

   /**
    * Sets the {@link #m_isApplyToAllComms} flag according to the parameters of
    * {@link IPSDeployConstants#APPLY_TO_ALL_COMMS} in the given request.
    * 
    * @param req the request, assumed not <code>null</code>.
    */
   private void setApplyToAllComms(PSRequest req)
   {
      String visToAllComms = req
            .getParameter(IPSDeployConstants.APPLY_TO_ALL_COMMS);
      m_isApplyToAllComms = "true".equalsIgnoreCase(visToAllComms);
   }

   /**
    * Runs this import job. Installs all required files and data from the
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

         archive = getArchive();
         // get the full info
         PSArchiveInfo info = archive.getArchiveInfo(true);
         PSExportDescriptor expDesc = info.getArchiveDetail()
               .getExportDescriptor();

         ctx = createImportContext(info);

         PSPkgInfo pkgInfo = createPkgInfo(expDesc, ctx, info);
         createPkgDependencies(pkgInfo,expDesc);
         
         // Save the Package GUID in ctx
         ctx.setPkgGuid(pkgInfo.getGuid());

         installPkgElements(archive, ctx, info, expDesc);

         postInstallPackage(pkgInfo, expDesc);
         
         writeDescriptorWithNewIDs(info, ctx);
      }
      catch (Exception ex)
      {
         addTransactionLogForFailure(ctx, ex);
         setStatusMessage("error: " + ex.getLocalizedMessage());
         ctx.setCurrentDependency(null);
         setStatus(-1);
         LogFactory.getLog(getClass()).error(
               "Error installing Deployer " + "package", ex);
         if (storeException)
             runException = ex;
      }
      finally
      {
         ResourceBundle bundle = PSDeploymentManager.getBundle(); 
          
         if(getStatus() != -1 || !isCancelled())
         {
            setStatus(100);
            setStatusMessage(bundle.getString("completed"));
         }

         setCompleted();
         
         // be sure we release the pub lock
         PSServerLockManager.getInstance().releaseLock(m_pubLockId);
         
         if (archive != null)
            archive.close();

         // flush entire cache since we don't know what was modified
         PSCacheManager.getInstance().flush();

         // Cause the item def manager to finish update notification work
         itemDefManager.commitUpdateNotifications();
      }

   }

   /**
    * Use current ID-Map to convert the IDs in the current descriptor to
    * the new IDs, and write the converted descriptor to the location
    * of {@link PSDeploymentHandler#IMPORT_ARCHIVE_DIR}, with the same
    * name as the archive file, but ".xml" file extension.
    * 
    * @param info the info contains current descriptor, assumed not 
    * <code>null</code>
    * @param ctx the import context, assumed not <code>null</code>.
    * 
    * @throws IOException if failed to create/write to the descriptor file.
    * @throws PSDeployException if any other error occurs. 
    */
   private void writeDescriptorWithNewIDs(PSArchiveInfo info, PSImportCtx ctx)
      throws PSDeployException, IOException
   {
      PSDeploymentHandler dh = PSDeploymentHandler.getInstance();
      PSDependencyManager dm = dh.getDependencyManager();

      PSExportDescriptor cvtDesc = dm.convertExportDescriptor(info, dh
            .getLogHandler(), ctx.getCurrentIdMap());

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element expEl = cvtDesc.toXml(doc);
      String descString = PSXmlDocumentBuilder.toString(expEl);
      
      File descFile = new File(PSDeploymentHandler.IMPORT_ARCHIVE_DIR,
            info.getArchiveRef() + ".xml");
      descFile.getParentFile().mkdirs();
      
      FileUtils.writeStringToFile(descFile, descString, "UTF8");
   }
   
   /**
    * Locate and load the archive
    * 
    * @return the archive, never <code>null</code>.
    * 
    * @throws PSDeployException if an error occirs.
    */
   private PSArchive getArchive() throws PSDeployException
   {
      return new PSArchive(m_archiveFile);
   }
   
   /**
    * Creates import context.
    * 
    * @param info the archive info, assumed not <code>null</code>.
    * 
    * @return the context, never <code>null</code>
    * 
    * @throws PSDeployException if an error occurs.
    */
   private PSImportCtx createImportContext(PSArchiveInfo info)
      throws PSDeployException
   {
      PSDeploymentHandler dh = PSDeploymentHandler.getInstance();
      PSLogHandler lh = dh.getLogHandler();
      PSImportCtx ctx = new PSImportCtx(getUserId(), info.getRepositoryInfo(),
            PSDbmsMapManager.getDbmsMap(info.getServerName()), dh
                  .getIdMapMgr(), lh, dh.getAppPolicySettings());

      PSDbmsInfo sourceRepository = ctx.getSourceRepository();
      ctx.setCurrentIdMap(ctx.getIdMapMgr().getIdmap(
            sourceRepository.getDbmsIdentifier()));

      return ctx;
   }
   
   /**
    * Installs the package elements from the specified archive.
    * 
    * @param archive the archive that contains the installed elements,
    * assumed not <code>null</code>.
    * @param ctx the import context, assumed not <code>null</code>.
    * @param info the archive info, assumed not <code>null</code>.
    * @param expDesc the package descriptor, assumed not <code>null</code>.
    * 
    * @throws Exception if an error occurs.
    */
   private void installPkgElements(PSArchive archive, PSImportCtx ctx,
         PSArchiveInfo info, PSExportDescriptor expDesc) throws Exception
   {
      PSArchiveHandler ah = null;
      boolean hasPkgs = expDesc.getPackages().hasNext();
      if (hasPkgs)
      {
         ResourceBundle bundle = PSDeploymentManager.getBundle();
         PSDeploymentHandler dh = PSDeploymentHandler.getInstance();
         PSLogHandler lh = dh.getLogHandler();

         // create the archive log
         int archiveLogId = lh.createArchiveLog(info, archive
                  .getArchiveManifest());
         ctx.setArchiveLogId(archiveLogId);

         // get the ordered package elements
         PSDependencyManager dm = dh.getDependencyManager();
         List<PSImportPackage> importList = m_descriptor.getImportPackageList();
         List<PSImportPackage> pkgs = dm.reorderDeployedElements(importList);

         // install the elements
         ah = new PSArchiveHandler(archive);
         for (PSImportPackage pkg : pkgs)
         {
            if (isCancelled())
               break;

            // create a package log for the current package
            int pkgLogId = lh.createPackageLog(archiveLogId, pkg, ctx);
            ctx.setPackageLogId(pkgLogId);

            // install the package file
            PSDeployableElement de = pkg.getPackage();

            // set the package's validation results on the ctx
            ctx.setCurrentValidationResults(pkg.getValidationResults());

            String msg = MessageFormat.format(bundle.getString("processing"),
                     new Object[]
                     {de.getDisplayIdentifier()});
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
               log.error("Package deploy exception",ex);
               status = PSArchivePackage.STATUS_ABORTED;
               restoreEx = ex;

            }

            lh.updatePackageStatus(archiveLogId, pkgLogId, de.getObjectType(),
                     de.getDependencyId(), status);
            // Register
            if (restoreEx != null)
               throw restoreEx;
         }
         
      }
      // install the configure files if there is any. 
      installConfigFiles(archive, expDesc);
      if(hasPkgs)
         ah.close();
   }
   
   /**
    * This is called after the package has been installed. Do nothing if user
    * has canceled the installation.
    * 
    * @param pkgInfo the installed package info, assumed not <code>null</code>
    * @param expDesc the descriptor of the installed package, assumed not 
    * <code>null</code>.
    */
   private void postInstallPackage(PSPkgInfo pkgInfo,
         PSExportDescriptor expDesc)
   {
      if (isCancelled())
         return;
      
      ResourceBundle bundle = PSDeploymentManager.getBundle();
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
            .getPkgInfoService();

      // Set Package complete info
      PSPkgInfo pkgInfoModify = pkgService.loadPkgInfoModifiable(pkgInfo
            .getGuid());
      pkgInfoModify.setLastActionStatus(PackageActionStatus.SUCCESS);
      pkgService.savePkgInfo(pkgInfoModify);

      // make the baseline for the version info of all package elements 
      PSPkgHelper.updatePkgElementVersions(pkgInfo
            .getPackageDescriptorName());

      // set community visibility
      PSPackageVisibility pkgVis = new PSPackageVisibility();
      pkgVis.setPkgCommunities(pkgInfo);
      
      // Notify configure service
      IPSConfigService cfgSrvc = PSConfigServiceLocator
            .getConfigService();
      IPSConfigRegistrationMgr regMgr = cfgSrvc
            .getConfigRegistrationMgr();
      if (StringUtils.isNotBlank(expDesc.getLocalConfigFile())
            && StringUtils.isNotBlank(expDesc.getConfigDefFile()))
      {
         String[] configNames = { expDesc.getName() };
         cfgSrvc.applyConfiguration(configNames, false);
         regMgr.register(expDesc.getName());
      }
      else
      {
         regMgr.unregister(expDesc.getName());
      }
      
      // need to apply all communitues?
      if (m_isApplyToAllComms)
      {
         PSPackageServiceHelper.updatePkgCommunities(expDesc.getName(), null,
               true);
      }
   }
   
   /**
    * Creates a package info from the supplied description, context, and archive
    * info.  The existing package info with the same name (if exist) will be
    * deleted.
    * 
    * @param expDesc the package description, assumed not <code>null</code>.
    * @param ctx the Context, assumed not <code>null</code>.
    * @param info the archive info, assumed not <code>null</code>.
    * 
    * @return the created package info, never <code>null</code>.
    */
   private PSPkgInfo createPkgInfo(PSExportDescriptor expDesc, PSImportCtx ctx,
         PSArchiveInfo info)
   {
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      
      PSPkgInfo pkgInfo = pkgService.createPkgInfo(expDesc.getName());
      PSPkgInfo existingInfo = pkgService.findPkgInfo(expDesc.getName());
      pkgInfo.setPublisherName(expDesc.getPublisherName());
      pkgInfo.setPublisherUrl(expDesc.getPublisherUrl());
      pkgInfo.setPackageDescription(expDesc.getDescription());
      pkgInfo.setPackageVersion(expDesc.getVersion());
      pkgInfo.setShippedConfigDefinition(expDesc.getConfigDefFile());
      pkgInfo.setLastActionDate(new Date());
      pkgInfo.setLastActionByUser(ctx.getUserId());
      pkgInfo.setLastActionStatus(PackageActionStatus.FAIL);
      pkgInfo.setLastAction(PackageAction.INSTALL_CREATE);
      pkgInfo.setType(PackageType.PACKAGE);
      pkgInfo.setEditable(info.isEditable());
      pkgInfo.setPackageDescriptorName(expDesc.getName());
      pkgInfo.setPackageDescriptorGuid(new PSGuid(new Long(expDesc.getId())));
      pkgInfo.setCmVersionMinimum(expDesc.getCmsMinVersion());
      pkgInfo.setCmVersionMaximum(expDesc.getCmsMaxVersion());
      pkgInfo.setCategory(PSPkgInfo.PackageCategory.valueOf(
            info.getCategory()));
      pkgService.savePkgInfo(pkgInfo);

      if (existingInfo != null)
      {
         //delete the old one after a new one successfully created
         pkgService.deletePkgInfo(existingInfo.getGuid());
      }

      return pkgInfo;
   }
   
   /**
    * Install the configure files for the specified archive and descriptor
    * 
    * @param archive the archive that may contain the configure files.
    * assumed not <code>null</code>.
    * @param expDesc the descriptor, assumed not <code>null</code>.
    * 
    * @throws IOException if an I/O exception occurs.
    * @throws PSDeployException if any other error occurs.
    */
   private void installConfigFiles(PSArchive archive,
         PSExportDescriptor expDesc) throws PSDeployException, IOException
   {
      if(StringUtils.isNotBlank(expDesc.getLocalConfigFile()))
      {
         copyConfigFile(archive, IPSConfigService.ConfigTypes.LOCAL_CONFIG,
                  expDesc.getName(), false);
         copyConfigFile(archive, IPSConfigService.ConfigTypes.DEFAULT_CONFIG,
                  expDesc.getName(), true);
      }
      
      if(StringUtils.isNotBlank(expDesc.getConfigDefFile()))
      {           
         copyConfigFile(archive, IPSConfigService.ConfigTypes.CONFIG_DEF,
                  expDesc.getName(), true);
      }
      
      IPSConfigService srv = 
         PSConfigServiceLocator.getConfigService();
      srv.initVisibility(expDesc.getName());
   }
   
   /**
    * Creates the package dependency entries for the supplied package info
    * object. Clears the old dependencies if exists, gets the new dependencies
    * from supplied expDesc.
    * 
    * @param pkgInfo The {@link PSPkgInfo} object for which the dependencies
    * needs to be created, assumed not <code>null</code>.
    * @param expDesc The export descriptor from which the new dependencies are
    * extracted, assumed not <code>null</code>.
    */
   private void createPkgDependencies(PSPkgInfo pkgInfo,
         PSExportDescriptor expDesc)
   {
      List<Map<String, String>> newDeps = expDesc.getPkgDepList();
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      List<PSPkgDependency> oldDeps = pkgService.loadPkgDependencies(pkgInfo
            .getGuid(), true);
      // Clear old dependencies if exists.
      if (!oldDeps.isEmpty())
      {
         for (PSPkgDependency oldDep : oldDeps)
         {
            pkgService.deletePkgDependency(oldDep.getId());
         }
      }
      // Add new dependencies
      if ((newDeps == null || newDeps.isEmpty()))
         return;
      for (Map<String, String> depMap : newDeps)
      {
         String pkgName = depMap.get(PSDescriptor.XML_PKG_DEP_NAME);
         String implied = depMap.get(PSDescriptor.XML_PKG_DEP_IMPLIED);
         PSPkgInfo depPkgInfo = pkgService.findPkgInfo(pkgName);
         if (depPkgInfo == null)
         {
            LogFactory.getLog(getClass()).info(
                  "Skipping package dependency " + "entry for package "
                        + pkgInfo.getPackageDescriptorName()
                        + " with dependent " + pkgName
                        + ", due to failure to find the "
                        + "package info object with that name.");
            continue;
         }
         PSPkgDependency pkgDep = pkgService.createPkgDependency();
         pkgDep.setOwnerPackageGuid(pkgInfo.getGuid());
         pkgDep.setDependentPackageGuid(depPkgInfo.getGuid());
         pkgDep.setImpliedDep(Boolean.valueOf(implied));
         pkgService.savePkgDependency(pkgDep);
      }
   }

   /**
    * Copies the configuration file from the archive supplied to its appropriate
    * location on the server based on configuration file type and package name.
    * Also creates parent directory structure if it does not yet exist.
    * 
    * @param archive the archive where the config files reside. Assumed not
    * <code>null</code>.
    * @param type the configuration file type.
    * @param packageName the package name, assumed not <code>null</code> or
    * empty.
    * @param overwrite flag indicating that the file should be overwritten if it
    * already exists.
    * @throws PSDeployException
    * @throws IOException
    */
   private void copyConfigFile(PSArchive archive,
         IPSConfigService.ConfigTypes type, String packageName,
         boolean overwrite) throws PSDeployException, IOException
   {
      InputStream is = null;
      FileWriter fw = null;
      IPSConfigService configService = PSConfigServiceLocator
            .getConfigService();
      try
      {
         File path = configService.getConfigFile(type, packageName);
         if (!overwrite && path.exists())
            return;
         // Create parent directories if they do not exist
         File pDir = path.getParentFile();
         if (!pDir.exists())
            pDir.mkdirs();

         is = archive.getFile(PSDescriptor.getConfigArchiveEntryPath(type));
         String contents = IOTools.getContent(is);
         if (path.exists())
            path.delete();
         fw = new FileWriter(path);
         fw.write(contents);
      }
      finally
      {
         if (is != null)
            is.close();
         if (fw != null)
            fw.close();
      }
   }

   /**
    * In case of any failures, a transaction log entry for failure is created
    * for the package
    * 
    * @param ctx the import context never <code>null</code>
    * @param ex the exception that was thrown cannot be <code>null</code>
    */
   private void addTransactionLogForFailure(PSImportCtx ctx, Exception ex)
   {
      if (ctx == null)
         throw new IllegalArgumentException("Import context may not be null");

      int logId = ctx.getPackageLogid();
      PSDependency dep = ctx.getCurrentDependency();
      // do nothing if dependency is not found
      if (dep == null)
         return;

      String depString = dep.getObjectTypeName() + " \""
            + dep.getDisplayName() + "\" (" + dep.getDependencyId() + ")";
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
   @Override
   protected String getJobType()
   {
      return "Install Package Job";
   }

   /**
    * The id of the publisher lock which must be acquired by the
    * <code>init()</code> method and released by the <code>run()</code>
    * method.
    */
   private int m_pubLockId = -1;

   /**
    * The import descriptor supplied to the <code>init()</code> method, never
    * <code>null</code> or modified after that.
    */
   private PSImportDescriptor m_descriptor;

   /**
    * Indicates if need to apply all communities to the imported package.
    */
   private boolean m_isApplyToAllComms = false;
   
   /**
    * Reference to the archive file to install.
    */
   private File m_archiveFile;
   
   /**
    * Indicates if an exception should be saved for later processing
    */
   boolean storeException = false;
   
   /**
    * Saved for later processing
    */
   private Exception runException = null;
}
