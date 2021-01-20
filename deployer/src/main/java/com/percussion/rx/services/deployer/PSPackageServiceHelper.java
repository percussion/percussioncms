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

import com.percussion.rx.config.IPSConfigService;
import com.percussion.rx.config.PSConfigServiceLocator;
import com.percussion.rx.config.PSConfigValidation;
import com.percussion.rx.services.deployer.PSPkgUiResponse.PSPkgUiResponseType;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageAction;
import com.percussion.services.pkginfo.utils.PSPkgHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Static helper class for package service.
 * 
 * @author bjoginipally
 * 
 */
public class PSPackageServiceHelper
{
   /**
    * Checks for the package validation and returns the results as
    * {@link PSPkgUiResponse}.
    * 
    * @param pkgName The name of the package that needs to be validated.
    * @return Either failure or success {@link PSPkgUiResponse} object.
    */
   public static PSPkgUiResponse getValidationResults(String pkgName)
   {
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      PSPkgInfo pkgInfo = pkgService.findPkgInfo(pkgName);
      if (pkgInfo == null)
      {
         PSPkgUiResponse resp = new PSPkgUiResponse(
               PSPkgUiResponseType.FAILURE,
               "Failed to find the package with the supplied name" + pkgName);
         return resp;
      }
      List<String> missingPkgs = getMissingPackages(pkgInfo);
      String message = "";
      if (!missingPkgs.isEmpty())
      {
         message = "<b>Missing Packages</b><br/> The following "
               + "dependent packages are either not installed successfully "
               + "or uninstalled.<br/>";
         for (String pkg : missingPkgs)
         {
            message += "<br/>" + pkg;
         }
      }
      Set<String> results = PSPkgHelper.validatePackage(pkgInfo.getGuid());
      if (!results.isEmpty())
      {
         message = "<b>Modified Design Objects</b><br/> The following objects "
               + "have been modified outside of allowed configuration.<br/>";
         for (String obj : results)
         {
            message += "<br/>" + obj;
         }
      }
      IPSConfigService cfgSrvc = PSConfigServiceLocator.getConfigService();
      List<PSConfigValidation> cfgValErrors = cfgSrvc
            .validateConfiguartion(pkgName);
      if(!cfgValErrors.isEmpty())
      {
         message = "<b>Configuration Verification Results</b><br/>";
         for (PSConfigValidation obj : cfgValErrors)
         {
            message += "<br/>" + obj.getValidationMsg();
         }
      }
      
      PSPkgUiResponse resp = new PSPkgUiResponse(PSPkgUiResponseType.SUCCESS,
            "No conflicts found during the package verification.");
      if (StringUtils.isNotBlank(message))
      {
         resp = new PSPkgUiResponse(PSPkgUiResponseType.FAILURE, message);
      }

      return resp;
   }

   /**
    * Creates a list missing dependent packages for the supplied package and
    * returns them.
    * 
    * @param pkgInfo The object {@link PSPkgInfo} for which the dependencies
    * needs to to be calculated.
    * @return List of the names of dependent package never <code>null</code>,
    * may be empty.
    * 
    */
   private static List<String> getMissingPackages(PSPkgInfo pkgInfo)
   {
      List<String> missingPkgs = new ArrayList<String>();
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      List<IPSGuid> depGuids = pkgService.findDependentPkgGuids(pkgInfo
            .getGuid());
      for (IPSGuid guid : depGuids)
      {
         PSPkgInfo info = pkgService.loadPkgInfo(guid);
         if (!info.isSuccessfullyInstalled()
               || info.getLastAction().equals(PackageAction.UNINSTALL))
         {
            missingPkgs.add(info.getPackageDescriptorName());
         }
      }
      return missingPkgs;
   }

   /**
    * Applies the package visibility on the supplied packages. Returns either a
    * successful {@link PSPkgUiResponse} object or failure object based on the
    * errors occurred while applying the visibility.
    * 
    * @param packageNames must not be <code>null</code>.
    * @return A successful {@link PSPkgUiResponse} object or failure object,
    * never <code>null</code>.
    */
   public static PSPkgUiResponse applyPackageVisibility(String packageNames)
   {
      PSPkgUiResponse response = new PSPkgUiResponse(
            PSPkgUiResponseType.SUCCESS,
            "Successfully reapplied the visibility settings.");
      String[] pkgNames = packageNames.split(PSPackageService.NAME_SEPARATOR);
      PSPair<List<PSPkgInfo>, List<String>> pkgsPair = getValidPackages(pkgNames);
      List<String> invalidPkgs = pkgsPair.getSecond();
      for (String pkgname : invalidPkgs)
      {
         ms_logger.warn("No package info object exists with the name "
               + pkgname + ". Skipping reapplying of the package visibility");
      }
      List<PSPkgInfo> pkgInfos = pkgsPair.getFirst();
      List<String> errorPkgs = new ArrayList<String>();
      PSPackageVisibility pkgVis = new PSPackageVisibility();
      for (PSPkgInfo pinfo : pkgInfos)
      {
         if (!pinfo.isSuccessfullyInstalled()
               || pinfo.getLastAction().equals(PackageAction.UNINSTALL))
            continue;
         String errMsg = pkgVis.setPkgCommunities(pinfo);
         if (errMsg != null)
            errorPkgs.add(pinfo.getPackageDescriptorName());
      }
      if (!errorPkgs.isEmpty())
      {
         String msg = "Failed to reapply the visibility settings for the following "
               + "packages.<br/>";
         for (String string : errorPkgs)
         {
            msg += "<br/>" + string;
         }
         response.setMessage(msg);
         response.setType(PSPkgUiResponseType.FAILURE);
      }
      return response;

   }

   /**
    * Applies the package configuration on the supplied packages. Returns either
    * a successful {@link PSPkgUiResponse} object or failure object based on the
    * errors occurred while applying the configuration.
    * 
    * @param packageNames must not be <code>null</code>.
    * @return A successful {@link PSPkgUiResponse} object or failure object,
    * never <code>null</code>.
    */
   public static PSPkgUiResponse applyConfiguartion(String packageNames)
   {
      if (StringUtils.isBlank(packageNames))
         throw new IllegalArgumentException("packageNames must not be blank");
      PSPkgUiResponse response = new PSPkgUiResponse(
            PSPkgUiResponseType.SUCCESS,
            "Successfully reapplied the configuration settings.");
      String[] pkgNames = packageNames.split(PSPackageService.NAME_SEPARATOR);
      PSPair<List<PSPkgInfo>, List<String>> pkgsPair = getValidPackages(pkgNames);
      List<String> invalidPkgs = pkgsPair.getSecond();
      for (String pkgname : invalidPkgs)
      {
         ms_logger.warn("No package info object exists with the name "
               + pkgname + ". Skipping reapplying of the package visibility");
      }
      List<PSPkgInfo> pkgInfos = pkgsPair.getFirst();
      List<String> validPkgs = new ArrayList<String>();
      for (PSPkgInfo pinfo : pkgInfos)
      {
         validPkgs.add(pinfo.getPackageDescriptorName());
      }
      IPSConfigService cfgSrvc = PSConfigServiceLocator.getConfigService();
      List<PSPair<String, Exception>> errors = cfgSrvc.applyConfiguration(
            validPkgs.toArray(new String[validPkgs.size()]), false);
      if (!errors.isEmpty())
      {
         String msg = "Failed to reapply the configuration settings for the "
               + "following packages.<br/>";
         for (PSPair pair : errors)
         {
            msg += "<br/>" + pair.getFirst();
         }
         response.setMessage(msg);
         response.setType(PSPkgUiResponseType.FAILURE);
      }
      return response;
   }

   /**
    * Finds the {@link PSPkgInfo} objects for the supplied names and returns a
    * {@link PSPair}, with list of PSPkgInfo objects as the first element and
    * list of package names for which the {@link PSPkgInfo} is not found as
    * second element.
    * 
    * @param pkgNames The String array of names of packages for which the
    * {@link PSPkgInfo} objects needs to be found, assumed not <code>null</code>.
    * @return PSPair of list of packages found and list of package names for
    * which the packages are not found. The pair and lists are never
    * <code>null</code> may be empty.
    */
   private static PSPair<List<PSPkgInfo>, List<String>> getValidPackages(
         String[] pkgNames)
   {
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      List<PSPkgInfo> pkgInfos = new ArrayList<PSPkgInfo>();
      List<String> invalidPkgs = new ArrayList<String>();
      PSPair<List<PSPkgInfo>, List<String>> result = new PSPair<List<PSPkgInfo>, List<String>>(
            pkgInfos, invalidPkgs);
      for (String pkgname : pkgNames)
      {
         if (StringUtils.isNotBlank(pkgname))
         {
            PSPkgInfo info = pkgService.findPkgInfo(pkgname);
            if (info == null)
            {
               invalidPkgs.add(pkgname);
               continue;
            }
            pkgInfos.add(info);
         }
      }
      return result;
   }

   /**
    * Creates the community package objects and returns them. Loops through all
    * the successfully installed packages and gets the communities for them.
    * Creates a reverse map of community and packages from them and then creates
    * the {@link PSCommunityPackage} objects and adds them to the return list.
    * Gets all the communities in the system and for the communities that are
    * not covered by the packages creates a PSCommunityPackage object with empty
    * string for packages.
    * 
    * @return PSCommunityPackages object never <code>null</code>.
    */
   public static PSCommunityPackages getCommunityPackages()
   {
      PSCommunityPackages commPkgs = new PSCommunityPackages();
      IPSConfigService srv = PSConfigServiceLocator.getConfigService();

      Map<IPSGuid, String> pkgInfomap = getPkgGuidNameMap();
      Map<String, Collection<String>> pkgCommsMap = new HashMap<String, Collection<String>>();
      for (String pkg : pkgInfomap.values())
      {
         Collection<String> comms = srv.loadCommunityVisibility(pkg);
         pkgCommsMap.put(pkg, comms);
      }
      Map<String, List<String>> commPkgsMap = new HashMap<String, List<String>>();

      for (String pkgName : pkgCommsMap.keySet())
      {
         Collection<String> comms = pkgCommsMap.get(pkgName);
         for (String comm : comms)
         {
            if (StringUtils.isBlank(comm))
               continue;
            List<String> pkgs = commPkgsMap.get(comm);
            if (pkgs == null)
            {
               pkgs = new ArrayList<String>();
               commPkgsMap.put(comm, pkgs);
            }
            pkgs.add(pkgName);
         }
      }

      Set<String> comms = commPkgsMap.keySet();
      for (String comm : comms)
      {
         commPkgs.add(new PSCommunityPackage(comm,
               getStringFromList(commPkgsMap.get(comm))));
      }
      PSPackageVisibility pkgVis = new PSPackageVisibility();
      List<String> allComms = pkgVis.getAllCommunities();
      allComms.removeAll(comms);
      for (String comm : allComms)
      {
         commPkgs.add(new PSCommunityPackage(comm, ""));
      }
      return commPkgs;
   }

   /**
    * Returns a map of package guid and name map of successfully installed
    * packages.
    * 
    * @return never <code>null</code>, may be empty.
    */
   public static Map<IPSGuid, String> getPkgGuidNameMap()
   {
      List<PSPkgInfo> pInfos = getPkgService().findAllPkgInfos();
      Map<IPSGuid, String> pkgInfomap = new HashMap<IPSGuid, String>();
      for (PSPkgInfo pinfo : pInfos)
      {
         if (pinfo.isSuccessfullyInstalled()
               && !pinfo.getLastAction().equals(PackageAction.UNINSTALL))
         {
            pkgInfomap.put(pinfo.getGuid(), pinfo.getPackageDescriptorName());
         }
      }
      return pkgInfomap;
   }

   /**
    * Helper method to return NAME_SEPARATOR string of items in supplied list of
    * Strings
    * 
    * @param strList if <code>null</code> or empty returns empty String.
    * @return concatenated String, never <code>null</code> may be blank.
    */
   public static String getStringFromList(Collection<String> strList)
   {
      if (strList == null || strList.isEmpty())
         return "";

      StringBuffer result = new StringBuffer();
      for (String str : strList)
      {
         if (result.length() > 0)
            result.append(PSPackageService.NAME_SEPARATOR);
         result.append(str);
      }
      return result.toString();

   }

   /**
    * Applies the supplied communities on to the package and package elements.
    * 
    * @param pkgName the name of an existing package. It may not be blank.
    * @param commList a list of community names. If this is <code>null</code>,
    * then apply all existing communities to the given package.
    * @param clearOtherEntries if <code>true</code> clears other community
    * entries from the objects and applies the supplied communities. Otherwise
    * leaves the current communities as is.
    */
   public static void updatePkgCommunities(String pkgName,
         Collection<String> commList, boolean clearOtherEntries)
   {
      if (StringUtils.isBlank(pkgName))
         throw new IllegalArgumentException("pkgName may not be blank.");
      
      PSPackageVisibility pkgVis = new PSPackageVisibility();
      if (commList == null)
         commList = pkgVis.getAllCommunities();
      
      PSPkgInfo pInfo = getPkgService().findPkgInfo(pkgName);
      if (pInfo == null)
      {
         throw new RuntimeException("Invalid package: \"" + pkgName + "\".");
      }
      
      IPSGuid pkgGuid = pInfo.getGuid();
      pkgVis.setPackageCommunities(pkgGuid, commList, clearOtherEntries);

      // save the community visibility
      IPSConfigService srv = PSConfigServiceLocator.getConfigService();
      srv.saveCommunityVisibility(commList, pkgName, clearOtherEntries);
   }
   
   /**
    * Returns the package info service.
    * @return the service, never <code>null</code>.
    */
   private static IPSPkgInfoService getPkgService()
   {
      return PSPkgInfoServiceLocator.getPkgInfoService();
   }


   /**
    * The logger for this class.
    */
   private static Logger ms_logger = Logger
         .getLogger("PSPackageServiceHelper");

}
