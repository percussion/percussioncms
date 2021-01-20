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
package com.percussion.rx.services.deployer;

import com.percussion.deployer.server.PSDeploymentHandler;
import com.percussion.deployer.server.uninstall.IPSUninstallResult;
import com.percussion.deployer.server.uninstall.IPSUninstallResult.PSUninstallResultType;
import com.percussion.rx.design.IPSDesignModel;
import com.percussion.rx.design.IPSDesignModelFactory;
import com.percussion.rx.design.PSDesignModelFactoryLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.utils.PSIdNameHelper;
import com.percussion.services.system.IPSSystemService;
import com.percussion.services.system.PSSystemServiceLocator;
import com.percussion.services.system.data.PSDependency;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * This class handles uninstallation of the packages.
 * 
 * @author bjoginipally
 * 
 */
public class PSPackageUninstall implements IPSPackageUninstaller
{
	/**
	 * Calls the deployment handler to unintsall the packages. Creates the
	 * uninstall messages with the returned results from uninstallation.
	 * 
	 * @param packageNames The {@link PSPackageService#NAME_SEPARATOR} separated
	 * list of package names.
	 * @return list of uninstall messages, never <code>null</code> may be
	 * empty.
	 */
    @Override
    public List<PSUninstallMessage> uninstallPackages(String packageNames)
    {
        return uninstallPackages(packageNames, false);
    }
    
    @Override
    public List<PSUninstallMessage> uninstallPackages(String packageName,
            boolean isRevertEntry) {
        List<PSUninstallMessage> messages = new ArrayList<PSUninstallMessage>();
        String[] pkgNames = packageName.split(PSPackageService.NAME_SEPARATOR);
        List<String> pkgNameList = new ArrayList<String>();
        for (String pkgname : pkgNames)
        {
            if (StringUtils.isNotBlank(pkgname))
                pkgNameList.add(pkgname);
        }
        if (pkgNameList.isEmpty()) {
             PSUninstallMessage msg = new PSUninstallMessage();
             msg.setPackageName("none");
             msg.setType(PSPackageService.WARNING);
             msg.setBody("No packages are supplied for uninstall.");
             messages.add(msg);
             return messages;
          }
          PSDeploymentHandler dh = PSDeploymentHandler.getInstance();
          List<IPSUninstallResult> results = dh.uninstallPackages(pkgNameList, isRevertEntry);
          for (IPSUninstallResult result : results)
          {
             PSUninstallMessage msg = new PSUninstallMessage();
             msg.setPackageName(result.getPackageName());
             msg.setType(getConvertedType(result.getResultType()));
             msg.setBody(result.getMessage());
             messages.add(msg);
             if (result.getResultType() == 
                IPSUninstallResult.PSUninstallResultType.ERROR)
                ms_logger.error(result.getMessage(), result.getException());
             else if (result.getResultType() == 
                IPSUninstallResult.PSUninstallResultType.WARN)
                ms_logger.warn(result.getMessage(), result.getException());
          }
          return messages;
    }

   /**
    * Helper method to convert PSUninstallResultType value to the UI consumable
    * String. One of the constants defined in Package Service.
    * 
    * @param resultType assumed not <code>null</code>.
    * @return One of the constants defined in Package Service, never
    * <code>null</code>.
    */
   private String getConvertedType(PSUninstallResultType resultType)
   {
      String result = PSPackageService.NONE;
      if(resultType == IPSUninstallResult.PSUninstallResultType.SUCCESS)
         result = PSPackageService.SUCCESS;
      else if(resultType == IPSUninstallResult.PSUninstallResultType.ERROR)
         result = PSPackageService.ERROR;
      else if(resultType == IPSUninstallResult.PSUninstallResultType.WARN)
         result = PSPackageService.WARNING;
      else if(resultType == IPSUninstallResult.PSUninstallResultType.INFO)
         result = PSPackageService.INFO;
      return result;
   }

   /**
    * Checks whether there are any packages that depend on the supplied package
    * and any elements from the package have dependencies. Creates one message
    * for each kind of dependencies found. The message consists of the dependent
    * object names with html line break.
    * 
    * @param packageName the name of the package for which the dependencies
    * needs to be checked.
    * @return list {@link PSUninstallMessage} messages, will be empty if there
    * are no dependencies found.
    */
   public List<PSUninstallMessage> checkPackageDepedencies(String packageName)
   {
      if (StringUtils.isBlank(packageName))
         throw new IllegalArgumentException("packageName must not be blank");
      List<PSUninstallMessage> messages = new ArrayList<PSUninstallMessage>();
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      PSPkgInfo pinfo = pkgService.findPkgInfo(packageName);
      if (pinfo == null)
      {
         PSUninstallMessage msg = new PSUninstallMessage();
         msg.setType(PSPackageService.WARNING);
         msg.setPackageName(packageName);
         msg.setBody("No Package exists with the supplied name: "
               + packageName);
         messages.add(msg);
         return messages;
      }
      PSUninstallMessage depMsg = checkPkgDependencies(pinfo);
      if (depMsg != null)
         messages.add(depMsg);
      depMsg = checkContentDependencies(pinfo);
      if (depMsg != null)
         messages.add(depMsg);
      return messages;
   }

   /**
    * Finds all the dependent packages and creates a {@link PSUninstallMessage}
    * message and adds the dependent package names to the message with html
    * break.
    * 
    * @param pinfo The package info object for which the package dependencies
    * needs to be checked, assumed not <code>null</code>.
    * @return A message corresponding to all the dependent packages, may be
    * <code>null</code>, if no dependencies found.
    */
   private PSUninstallMessage checkPkgDependencies(PSPkgInfo pinfo)
   {
      PSUninstallMessage msg = null;
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      List<IPSGuid> guids = pkgService.findOwnerPkgGuids(pinfo.getGuid());
      if (!guids.isEmpty())
      {
         String depPkgs = "";
         for (IPSGuid guid : guids)
         {
            PSPkgInfo pi = pkgService.loadPkgInfo(guid);
            depPkgs += "<br/>" + pi.getPackageDescriptorName();
         }
         String msgT = "Package ({0}) is a dependency for other packages "
               + "installed on the system  If you remove package ({1}), "
               + "these packages may not work correctly. {2}";
         Object[] args = { pinfo.getPackageDescriptorName(),
               pinfo.getPackageDescriptorName(), depPkgs };
         msg = new PSUninstallMessage();
         msg.setPackageName(pinfo.getPackageDescriptorName());
         msg.setType(PSPackageService.WARNING);
         msg.setBody(MessageFormat.format(msgT, args));
      }
      return msg;
   }

   /**
    * Checks whether the elements of the supplied package have any dependencies,
    * if yes creates a {@link PSUninstallMessage} message and adds the design
    * object names that have the dependencies to the message with html break.
    * 
    * @param pinfo The package info object for which the element dependencies
    * needs to be checked, assumed not <code>null</code>.
    * @return A message corresponding to all the objects that have dependencies,
    * may be <code>null</code>, if no dependencies found.
    */
   private PSUninstallMessage checkContentDependencies(PSPkgInfo pinfo)
   {
      PSUninstallMessage msg = null;
      List<IPSGuid> objGuids = getPackageObjectGuids(pinfo);
      IPSSystemService sysSrvc = PSSystemServiceLocator.getSystemService();
      List<PSDependency> depObjs = sysSrvc.findDependencies(objGuids);
      IPSDesignModelFactory factory = PSDesignModelFactoryLocator
            .getDesignModelFactory();
      String objNames = "";
      for (int i = 0; i < depObjs.size(); i++)
      {
         if (!depObjs.get(i).getDependents().isEmpty())
         {
            IPSGuid objGuid = objGuids.get(i);
            if (PSIdNameHelper.isSupported(PSTypeEnum.valueOf(objGuid
                  .getType())))
            {
               objNames += "<br />" + PSIdNameHelper.getName(objGuid);
            }
            else
            {
               IPSDesignModel model = factory.getDesignModel(PSTypeEnum
                     .valueOf(objGuid.getType()));
               objNames += "<br />" + model.guidToName(objGuid);
            }
         }
      }
      if (StringUtils.isNotBlank(objNames))
      {
         String msgT = "The package {0} includes design objects that are "
               + "currently being used.  These design objects will not be removed "
               + "when the package is uninstalled. {1}";
         Object[] args = { pinfo.getPackageDescriptorName(), objNames };
         msg = new PSUninstallMessage();
         msg.setPackageName(pinfo.getPackageDescriptorName());
         msg.setType(PSPackageService.WARNING);
         msg.setBody(MessageFormat.format(msgT, args));
      }
      return msg;
   }
   
   /**
    * Helper method to return the object guids of the supplied package.
    * 
    * @param pkgInfo assumed not <code>null</code>.
    * @return List of IPSGuids of objects of the supplied package, never
    * <code>null</code>, may be empty.
    */
   private List<IPSGuid> getPackageObjectGuids(PSPkgInfo pkgInfo)
   {
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
      .getPkgInfoService();
      List<IPSGuid> pkgElems = pkgService.findPkgElementGuids(pkgInfo
            .getGuid());
      List<IPSGuid> objGuids = new ArrayList<IPSGuid>();
      for (IPSGuid guid : pkgElems)
      {
         PSPkgElement pkgElem = pkgService.loadPkgElement(guid);
         objGuids.add(pkgElem.getObjectGuid());
      }
      return objGuids;
   }

   /**
    * The logger for this class.
    */
   private static Logger ms_logger = Logger.getLogger("PSPackageUninstall");

}
