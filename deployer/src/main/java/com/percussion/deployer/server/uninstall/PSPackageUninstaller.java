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
package com.percussion.deployer.server.uninstall;

import com.percussion.deployer.server.PSDependencyManager;
import com.percussion.deployer.server.PSDeploymentHandler;
import com.percussion.deployer.server.uninstall.IPSUninstallResult.PSUninstallResultType;
import com.percussion.rx.config.IPSConfigService;
import com.percussion.rx.config.PSConfigServiceLocator;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.pkginfo.IPSIdNameService;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSIdNameServiceLocator;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgDependency;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageAction;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageActionStatus;
import com.percussion.services.pkginfo.utils.PSIdNameHelper;
import com.percussion.util.PSOsTool;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Uninstalls elements of the supplied package and updates the status of package
 * info.
 */
public class PSPackageUninstaller
{
    /**
     * Uninstalls all the elements of a package and updates the package info
     * service with the uninstalled status.  SYSTEM packages will not be
     * uninstalled.
     * 
     * @param packageNames must not be <code>null</code>.
     * @return list of uninstall results, returns a success message if the
     * uninstall succeeds, otherwise one message for the failure of each package
     * element.
     */
    public List<IPSUninstallResult> uninstallPackages(List<String> packageNames) throws PSNotFoundException {
        return uninstallPackages(packageNames, false);
    }

   /**
    * Uninstalls all the elements of a package and updates the package info
    * service with the uninstalled status.  SYSTEM packages will not be
    * uninstalled.
    * 
    * @param packageNames must not be <code>null</code>.
    * @param isRevertEntry <code>true</code> if the package marked for REVERT in InstallPackages.xml
    * @return list of uninstall results, returns a success message if the
    * uninstall succeeds, otherwise one message for the failure of each package
    * element.
    */
   public List<IPSUninstallResult> uninstallPackages(List<String> packageNames, boolean isRevertEntry) throws PSNotFoundException {
      if (packageNames == null)
         throw new IllegalArgumentException("packageNames must not be null");
      PSPair<List<PSPkgInfo>, List<IPSUninstallResult>> pkgPair =
         loadPackages(packageNames);
      List<IPSUninstallResult> messages = new ArrayList<>(pkgPair.getSecond());
      List<PSPkgInfo> pkgInfos = pkgPair.getFirst();
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      for (PSPkgInfo pkgInfo : pkgInfos)
      {
         List<IPSUninstallResult> msgs = uninstallPackage(pkgInfo);

         if (msgs.isEmpty())
         {
            PSUninstallResult result = new PSUninstallResult(pkgInfo
                  .getPackageDescriptorName(), PSUninstallResultType.SUCCESS);
            result.setMessage("Uninstalled successfully.");
            messages.add(result);
         }
         else
         {
            messages.addAll(msgs);
         }
         PSPkgInfo pkgInfo1 = pkgService.loadPkgInfoModifiable(pkgInfo
               .getGuid());
         
         // if the message results contains the name of the package to uninstall,
         // it means there was an error and the package should not be uninstalled
         // if it is marked for REVERT.  We mark it as INSTALLED.
         for(IPSUninstallResult msg : msgs) {
             if(msg.getPackageName().equals(pkgInfo1.getPackageDescriptorName()) && !isRevertEntry) {
                 pkgInfo1.setLastAction(PackageAction.INSTALL_CREATE);
                 pkgInfo1.setLastActionDate(new Date());
                 pkgInfo1.setLastActionStatus(PackageActionStatus.SUCCESS);
                 pkgService.savePkgInfo(pkgInfo1);
                 return messages;
             }
         }

         pkgInfo1.setLastAction(PackageAction.UNINSTALL);
         pkgInfo1.setLastActionDate(new Date());
         pkgInfo1.setLastActionStatus(msgs.isEmpty() 
               ? PackageActionStatus.SUCCESS
               : PackageActionStatus.FAIL);
         pkgService.savePkgInfo(pkgInfo1);
         List<PSPkgDependency> deps = pkgService.loadPkgDependencies(pkgInfo1
               .getGuid(), true);
         for (PSPkgDependency dep : deps)
         {
            pkgService.deletePkgDependency(dep.getId());
         }
      }
      return messages;
   }

   /**
    * Loads the packages and makes {@link IPSUninstallResult} objects for the
    * packages that do not have the {@link PSPkgInfo} objects as well as
    * for SYSTEM packages. Returns the result as {@link PSPair} of package info
    * objects and result objects.
    * 
    * @param packageNames list of package names for which the package info
    * objects needs to be loaded, assumed not <code>null</code>.
    * @return The pair of list of {@link PSPkgInfo}and list of
    * {@link IPSUninstallResult} object, either the first or second list may
    * be empty but never <code>null</code>.
    */
   private PSPair<List<PSPkgInfo>, List<IPSUninstallResult>> loadPackages(
         List<String> packageNames)
   {
      List<IPSUninstallResult> messages = new ArrayList<>();
      List<PSPkgInfo> pkgInfos = new ArrayList<>();
      PSPair<List<PSPkgInfo>, List<IPSUninstallResult>> result = 
         new PSPair<>(
            pkgInfos, messages);
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      for (String pkgname : packageNames)
      {
         PSPkgInfo pInfo = pkgService.findPkgInfo(pkgname);
         if (pInfo == null)
         {
            String errMsg = "Skipped uninstalling of the supplied package as no"
                  + " package exists with the name: " + pkgname;
            PSUninstallResult msg = new PSUninstallResult(pkgname,
                  PSUninstallResultType.INFO);
            msg.setMessage(errMsg);
            messages.add(msg);
         }
         else if (pInfo.isSystem())
         {
            String warnMsg = pkgname + " is a SYSTEM package.  SYSTEM"
                  + " packages cannot be uninstalled.";
            PSUninstallResult msg = new PSUninstallResult(pkgname,
                  PSUninstallResultType.WARN);
            msg.setMessage(warnMsg);
            messages.add(msg);
         }
         else
         {
            pkgInfos.add(pInfo);
         }
      }
      return result;
   }

   /**
    * Helper method to uninstall one package at a time.
    * 
    * @param pkgInfo assumed not <code>null</code>.
    * @return List of IPSUninstallResult objects may be empty, never
    * <code>null</code>. The message objects are filled in properly by
    * appropriate action.
    */
   private List<IPSUninstallResult> uninstallPackage(PSPkgInfo pkgInfo) throws PSNotFoundException {
      IPSConfigService cfgSrvc = PSConfigServiceLocator.getConfigService();
      cfgSrvc.deApplyConfiguration(pkgInfo.getPackageDescriptorName());
      boolean wasContentTypeDeleted = false;

      List<IPSUninstallResult> messages = new ArrayList<>(deletePackageElements(pkgInfo));
      
      // 'skipped deletion' warnings were previously errors.  now being flagged
      // as warnings but need to be careful not to change behavior
      // on other warning messages
      for(IPSUninstallResult res : messages) {
          if(res.getResultType() == PSUninstallResultType.WARN
              && res.getMessage().contains("Skipped deletion of package")) {
              wasContentTypeDeleted = true;
              break;
          }
      }
      
      if(!wasContentTypeDeleted)
          messages.addAll(deleteConfigFiles(pkgInfo));
      return messages;
   }

   /**
    * Deletes the config files for that are associated with the supplied package
    * and if there are any errors deleting the files, returns them as list of
    * {@link IPSUninstallResult} objects.
    * 
    * @param pkgInfo the package info object whose config files need to be
    * deleted, assumed not <code>null</code>.
    * @return list of {@link IPSUninstallResult}s containing error for each
    * config file that can't be deleted.
    */
   public List<IPSUninstallResult> deleteConfigFiles(PSPkgInfo pkgInfo)
   {
      if (pkgInfo == null)
         throw new IllegalArgumentException("pckInfo must not be null");
      
      List<IPSUninstallResult> messages = new ArrayList<>();
      IPSConfigService cfgSrvc = PSConfigServiceLocator.getConfigService();
      String configName = pkgInfo.getPackageDescriptorName();
      Map<File, Exception> cfgErrors = cfgSrvc
            .uninstallConfiguration(configName);
      for (File file : cfgErrors.keySet()) {
         PSUninstallResult res = new PSUninstallResult(configName,
                 PSUninstallResultType.WARN);
         res.setMessage("Failed to uninstall configuration file "
                 + file.getName());
         res.setException(cfgErrors.get(file));
      }
      return messages;
   }

   /**
    * Deletes the elements of the package, if there are any errors wraps them
    * inside the {@link IPSUninstallResult} objects as warnings and returns the
    * list.
    * 
    * @param pkgInfo assumed not <code>null</code>.
    * @return List of IPSUninstallResult objects may be empty, never
    * <code>null</code>.
    */
   private List<IPSUninstallResult> deletePackageElements(PSPkgInfo pkgInfo) throws PSNotFoundException {
      List<IPSUninstallResult> messages = new ArrayList<>();
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      List<IPSGuid> pkgElems = pkgService.findPkgElementGuids(pkgInfo
            .getGuid());
      String returnMsg = null;

      /**
       * before doing a delete of each package/content type, we can check to see
       * if any content types (if they exist) in the package have dependents.
       * If the content type does have dependents, we don't want to delete any
       * of the elements associated with the content type, and in return, 
       * we don't want to delete any of the config files executed in the 
       * uninstallPackages(PSPkgInfo) method.
       */
      for(IPSGuid guid : pkgElems) {
          
          PSPkgElement pkgElem = pkgService.loadPkgElement(guid);
          IPSGuid objGuid = pkgElem.getObjectGuid();
          
          if(objGuid.getType() == PSTypeEnum.NODEDEF.getOrdinal()) {
              returnMsg = PSDesignModelUtils.checkDependencies(objGuid);
          }
          
          // here we log these as warnings as per CMS-3561
          if(StringUtils.isNotBlank(returnMsg)) {
              PSUninstallResult res = new PSUninstallResult(pkgInfo.getPackageDescriptorName(),
                      PSUninstallResultType.WARN);
                res.setPackageGuid(pkgInfo.getGuid());
                res.setMessage("Skipped deletion of package " + res.getPackageName() + 
                    " as the content type " + objGuid + " was in use.");
                res.setObjectGuid(objGuid);
                returnMsg = null;
                messages.add(res);
          }
      }
      
      if(!messages.isEmpty())
          return messages;

      for (IPSGuid guid : pkgElems)
      {
         PSPkgElement pkgElem = pkgService.loadPkgElement(guid);

         IPSGuid objGuid = pkgElem.getObjectGuid();
         if (!canIgnoreForUninstall(objGuid))
         {
            IPSUninstallResult res = deleteElement(pkgInfo, objGuid);
            if (res != null)
            {
               messages.add(res);
            }
            else
            {
               pkgService.deletePkgElement(guid);
            }
         }
      }
      return messages;
   }

   /**
    * Helper method to delete the objects, if a design model exists for the
    * supplied object guid type, then uses the design model to delete the
    * object. Deletes the file if the supplied object is file type.
    * 
    * @param pkgInfo assumed not <code>null</code>.
    * @param objGuid assumed not <code>null</code>.
    * @return The uninstall result, may be <code>null</code>.
    */
   private IPSUninstallResult deleteElement(PSPkgInfo pkgInfo, IPSGuid objGuid)
   {
      PSUninstallResult er = null;
      if (isFileType(objGuid))
      {
         er = deleteFile(pkgInfo, objGuid);
      }
      else
      {
         IPSDesignModel model = getDesignModel(objGuid);
         if (model != null)
         {
            try
            {
               if (PSIdNameHelper.isSupported(PSTypeEnum.valueOf(objGuid
                     .getType())))
               {
                  model.delete(PSIdNameHelper.getName(objGuid));
               }
               else
               {
                  model.delete(objGuid);
               }
            }
            catch (Exception e)
            {
               er = new PSUninstallResult(pkgInfo.getPackageDescriptorName(),
                     PSUninstallResultType.ERROR);
               er.setPackageGuid(pkgInfo.getGuid());
               er.setMessage(e.getLocalizedMessage());
               er.setObjectGuid(objGuid);
               er.setException(e);
            }
         }
         else
         {
            er = new PSUninstallResult(pkgInfo.getPackageDescriptorName(),
                  PSUninstallResultType.ERROR);
            er.setPackageGuid(pkgInfo.getGuid());
            er.setMessage("Failed to delete the element with guid "
                  + objGuid
                  + " failed to find a handler to delete the object.");
            er.setObjectGuid(objGuid);

         }
      }
      return er;
   }

   /**
    * Helper method to delete the files.
    * 
    * @param objGuid assumed not <code>null</code>.
    * @return result will be <code>null</code> in case of success.
    */
   private PSUninstallResult deleteFile(PSPkgInfo pkgInfo, IPSGuid objGuid)
   {
      PSUninstallResult er = null;
      IPSIdNameService nmSrvc = PSIdNameServiceLocator.getIdNameService();
      String fileName = nmSrvc.findName(objGuid);
      if (fileName == null)
         return null;
      fileName = getNormalizedFileName(fileName);
      File file = new File(PSServer.getRxDir(), fileName);
      if (file.exists())
      {
         try
         {
            file.delete();
            if(file.exists())
               throw new SecurityException(
                  "File could not be deleted. Another process may be using this file.");
         }
         catch (Exception e)
         {
            er = new PSUninstallResult(pkgInfo.getPackageDescriptorName(),
                  PSUninstallResultType.WARN);
            er.setPackageGuid(pkgInfo.getGuid());
            er.setMessage("Failed to delete file: \n" + fileName + ".");
            er.setObjectName(fileName);
            er.setException(e);
         }
      }
      return er;
   }

   /**
    * helper method to normalize file name.
    * 
    * @param fileName assumed not <code>null</code>.
    * @return normalized file name.
    */
   private String getNormalizedFileName(String fileName)
   {
      if (fileName.startsWith("/") || fileName.startsWith("\\"))
      {
         // strip off leading file separator
         if (fileName.trim().length() > 1)
            fileName = fileName.substring(1);
      }

      if (PSOsTool.isUnixPlatform())
      {
         // normalize path
         fileName = fileName.replace('\\', '/');
      }
      return fileName;
   }

   /**
    * Checks whether the supplied guid os of file type.
    * 
    * @param objGuid assumed not <code>null</code>.
    * @return <code>true</code> if it is file type, otherwise
    * <code>false</code>.
    */
   private boolean isFileType(IPSGuid objGuid)
   {
      PSTypeEnum objType = PSTypeEnum.valueOf(objGuid.getType());
      return objType == PSTypeEnum.CONFIGURATION
            || objType == PSTypeEnum.IMAGE_FILE
            || objType == PSTypeEnum.USER_DEPENDENCY;
   }

   /**
    * Returns the design model corresponding to the the supplied guid. May be
    * <code>null</code>. Ignores the error and returns <code>null</code> if
    * the design model is not found.
    * 
    * @param objGuid assumed not <code>null</code>.
    * @return design model or <code>null</code> if not found.
    */
   private IPSDesignModel getDesignModel(IPSGuid objGuid)
   {
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      IPSDesignModel model = null;
      try
      {
         model = factory.getDesignModel(PSTypeEnum.valueOf(objGuid.getType()));
      }
      catch (Exception e)
      {
         // Ignore
      }
      return model;
   }

   /**
    * Checks whether the supplied guid can be ignored for deletion.
    * 
    * @param guid assumed not <code>null</code>.
    * @return <code>true</code> if it can be ignored for uninstall, otherwise
    * <code>false</code>.
    */
   private boolean canIgnoreForUninstall(IPSGuid guid)
   {
      PSDeploymentHandler dh = PSDeploymentHandler.getInstance();
      PSDependencyManager dm = (PSDependencyManager) dh.getDependencyManager();
      List<String> ignoreTypes = dm.getUninstallIgnoreTypes();
      PSTypeEnum type = PSTypeEnum.valueOf(guid.getType());
      return type == null || ignoreTypes.contains(type.name());
   }
}
