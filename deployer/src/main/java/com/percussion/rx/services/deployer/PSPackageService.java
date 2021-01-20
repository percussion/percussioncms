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
import com.percussion.rx.config.data.PSConfigStatus;
import com.percussion.rx.config.data.PSConfigStatus.ConfigStatus;
import com.percussion.rx.services.deployer.PSPkgUiResponse.PSPkgUiResponseType;
import com.percussion.server.PSServer;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageAction;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Business layer package service that calls to the lower level CRUD package
 * service allowing exposure to clients via "web services" which in this case is
 * REST.
 * 
 * @author erikserating
 * 
 */
@Service(value = "packageService")
public class PSPackageService
{
   @GET
   @Path("/packages")
   public PSPackages getAllPackages()
   {
      PSPackages packages = new PSPackages();
      List<PSPkgInfo> pInfos = getPkgService().findAllPkgInfos();
      for (PSPkgInfo pinfo : pInfos)
      {
         if(pinfo.isCreated())
            continue;
         PSPackage pkg = new PSPackage();
         pkg.setName(pinfo.getPackageDescriptorName());
         pkg.setDesc(pinfo.getPackageDescription());
         pkg.setPublisher(pinfo.getPublisherName());
         pkg.setVersion(pinfo.getPackageVersion());
         if (!pinfo.isCreated())
            pkg.setInstalldate(pinfo.getLastActionDate());
         pkg.setInstaller(pinfo.getLastActionByUser());
         pkg.setPackageStatus(getInstalledStatus(pinfo));
         pkg.setConfigStatus(getConfiguredStatus(pinfo));
         pkg.setCategory(pinfo.isSystem() ? SYSTEM : USER);
         boolean isPkgLocked = !pinfo.isEditable();
         pkg.setLockStatus(isPkgLocked?PACKAGE_LOCKED:PACKAGE_UNLOCKED);
         packages.add(pkg);
      }

      return packages;
   }

   @GET
   @Path("/reapplyVisibility")
   public PSPkgUiResponse reapplyVisibility(@QueryParam("packageNames")
   String packageNames)
   {
      if (StringUtils.isBlank(packageNames))
      {
         PSPkgUiResponse response = new PSPkgUiResponse(
               PSPkgUiResponseType.FAILURE,
               "Skipping the reapplying of "
                     + "visibility settings as packageNames parameter value is empty");
         return response;

      }
      PSPkgUiResponse response = null;
      try
      {
         response = PSPackageServiceHelper
               .applyPackageVisibility(packageNames);
      }
      catch (Exception e)
      {
         response = new PSPkgUiResponse(PSPkgUiResponseType.FAILURE, e
               .getLocalizedMessage());
      }
      return response;
   }
   
   @GET
   @Path("/reapplyConfigs")
   public PSPkgUiResponse reapplyConfiguration(@QueryParam("packageNames")
   String packageNames)
   {
      if (StringUtils.isBlank(packageNames))
      {
         PSPkgUiResponse response = new PSPkgUiResponse(
               PSPkgUiResponseType.FAILURE,
               "Skipping the reapplying of "
                     + "configuration settings as packageNames parameter value is empty");
         return response;

      }

      PSPkgUiResponse response = null;
      try
      {
         response = PSPackageServiceHelper.applyConfiguartion(packageNames);
      }
      catch (Exception e)
      {
         response = new PSPkgUiResponse(PSPkgUiResponseType.FAILURE, e
               .getLocalizedMessage());
      }
      return response;
   }

   @GET
   @Path("/packageCommunities")
   public PSPackageCommunities getPackageCommunities()
   {
      
      PSPackageCommunities pkgComms = new PSPackageCommunities();
      Map<IPSGuid, String> pkgInfomap = PSPackageServiceHelper.getPkgGuidNameMap();
      Map<IPSGuid, String> commsMap = getPkgCommsMap(pkgInfomap);
      Iterator<IPSGuid> iter = pkgInfomap.keySet().iterator();
      while (iter.hasNext())
      {
         IPSGuid guid = iter.next();
         PSPackageCommunity pkgComm = new PSPackageCommunity(pkgInfomap
               .get(guid), commsMap.get(guid));
         pkgComms.add(pkgComm);
      }
      return pkgComms;
   }

   /**
    * Gets the package / communities association.
    * 
    * @param pkgInfomap the package ID/name map, assumed not <code>null</code>.
    * 
    * @return the map that maps the package ID to its associated communities.
    * The associated communities is a comma delimited string of community names.
    */
   private Map<IPSGuid, String> getPkgCommsMap(Map<IPSGuid, String> pkgInfomap)
   {
      IPSConfigService srv = PSConfigServiceLocator.getConfigService();
      Map<IPSGuid, String> result = new HashMap<IPSGuid, String>();
      for (Map.Entry<IPSGuid, String> pkg : pkgInfomap.entrySet())
      {
         Collection<String> names = srv.loadCommunityVisibility(pkg.getValue());         
         result.put(pkg.getKey(), PSPackageServiceHelper.getStringFromList(names));
      }
         
      return result;
   }
   
   @GET
   @Path("/communityPackages")
   public PSCommunityPackages getCommunityPackages()
   {
      PSCommunityPackages commPkgs = PSPackageServiceHelper.getCommunityPackages();
      return commPkgs;
   }

   @POST
   @Path("/updatePackageCommunities")
   @Consumes("application/x-www-form-urlencoded")
   public PSPkgUiResponse postUpdatePackageCommunities(
         @QueryParam("packageName")
         String packageName, @QueryParam("selectedComms")
         String selectedComms)
   {
      PSPkgUiResponse response = new PSPkgUiResponse(
            PSPkgUiResponseType.SUCCESS, "");
      try
      {
         List<String> commList = getListFromString(selectedComms);
         updatePkgComms(packageName, commList, true);
      }
      catch (Exception e)
      {
         response.setType(PSPkgUiResponseType.FAILURE);
         response.setMessage(e.getLocalizedMessage());
      }
      return response;
   }

   @POST
   @Path("/updateCommunityPackages")
   @Consumes("application/x-www-form-urlencoded")
   public PSPkgUiResponse postUpdateCommunityPackages(
         @QueryParam("communityName")
         String communityName, @QueryParam("selectedPkgs")
         String selectedPkgs)
   {
      PSPkgUiResponse response = new PSPkgUiResponse(
            PSPkgUiResponseType.SUCCESS, "");
      try
      {
         updateComPkgs(communityName, selectedPkgs);
      }
      catch (Exception e)
      {
         response.setType(PSPkgUiResponseType.FAILURE);
         response.setMessage(e.getLocalizedMessage());
      }
      return response;
   }

   @POST
   @Path("/uninstallPackage")
   @Consumes("application/x-www-form-urlencoded")
   public PSUninstallMessages postUninstallPackage(@QueryParam("packageName")
   String packageNames)
   {
      PSUninstallMessages msgs = new PSUninstallMessages();
      PSPackageUninstall pkgUninstall = new PSPackageUninstall();
      msgs.setMessages(pkgUninstall.uninstallPackages(packageNames));
      return msgs;
   }

   @POST
   @Path("/checkPackageDependencies")
   @Consumes("application/x-www-form-urlencoded")
   public PSUninstallMessages postCheckPackageDependencies(
         @QueryParam("packageName")
         String packageName)
   {
      PSUninstallMessages msgs = new PSUninstallMessages();
      PSPackageUninstall pkgUninstall = new PSPackageUninstall();
      msgs.setMessages(pkgUninstall.checkPackageDepedencies(packageName));
      return msgs;
   }

   @GET
   @Path("/validationResults")
   public PSPkgUiResponse getValidationResults(@QueryParam("packageName")
   String packageName)
   {
      PSPkgUiResponse response = null;
      try
      {
         response = PSPackageServiceHelper.getValidationResults(packageName);
      }
      catch (Exception e)
      {
         response = new PSPkgUiResponse(PSPkgUiResponseType.FAILURE, e
               .getLocalizedMessage());
      }
      return response;
   }

   @GET
   @Path("serverTimeout")
   public PSPkgUiResponse getServerTimeout()
   {
      int sto = PSServer.getServerConfiguration().getUserSessionTimeout();
      PSPkgUiResponse response = new PSPkgUiResponse(
            PSPkgUiResponseType.SUCCESS, sto + "");
      return response;
   }
   
   @POST
   @Path("/convertPackage")
   @Consumes("application/x-www-form-urlencoded")
   public PSPkgUiResponse postConvertPackage(
         @QueryParam("packageName")
         String packageName)
   {
      PSPkgUiResponse msg = new PSPkgUiResponse();
      try
      {
         PSConvertToSource cs = new PSConvertToSource();
         PSPair<Boolean, String> result = cs.convert(packageName);
            
         PSPkgUiResponseType type = result.getFirst() ? PSPkgUiResponseType.SUCCESS
               : PSPkgUiResponseType.FAILURE;
         String message = result.getSecond();
         msg = new PSPkgUiResponse(type,message);
      }
      catch(Exception e)
      {
         msg = new PSPkgUiResponse(PSPkgUiResponseType.FAILURE, e
               .getLocalizedMessage());
         ms_logger.error("error converting package",e);
      }
      return msg;
   }

   
   /**
    * Updates the supplied packages and elements with the given community name.
    * Loops through all other package elements and removes the supplied
    * community entry if exists.
    * 
    * @param communityName name of the community must not be <code>null</code>.
    * @param selectedPkgs {@link #NAME_SEPARATOR} separated list of package
    * names.
    */
   private void updateComPkgs(String communityName, String selectedPkgs)
   {
      List<String> commList = getListFromString(communityName);
      List<String> pkgs = getListFromString(selectedPkgs);
      for (String pkg : pkgs)
      {
         if (StringUtils.isBlank(pkg))
            continue;
         updatePkgComms(pkg, commList, false);
      }
      List<PSPkgInfo> pInfos = getPkgService().findAllPkgInfos();
      List<IPSGuid> objectGuids = new ArrayList<IPSGuid>();
      for (PSPkgInfo info : pInfos)
      {
         if (pkgs.contains(info.getPackageDescriptorName()))
            continue;
         
         IPSGuid pkgGuid = info.getGuid();
         objectGuids.add(pkgGuid);
         List<PSPkgElement> pkgElems = getPkgService().findPkgElements(pkgGuid);
         for (PSPkgElement element : pkgElems)
         {
            objectGuids.add(element.getObjectGuid());
         }
      }
      PSPackageVisibility pkgVis = new PSPackageVisibility();
      pkgVis.clearCommunity(communityName, objectGuids);

   }

   /**
    * Applies the supplied communities on to the package and package elements.
    * 
    * @param pkgName the name of an existing package.
    * @param commList a list of community names. If this is <code>null</code>,
    * then apply all existing communities to the given package.
    * @param clearOtherEntries if <code>true</code> clears other community
    * entries from the objects and applies the supplied communities. Otherwise
    * leaves the current communities as is.
    */
   private void updatePkgComms(String pkgName, Collection<String> commList,
         boolean clearOtherEntries)
   {
      if (commList == null)
         throw new IllegalArgumentException("commList may not be null.");
      
      PSPackageServiceHelper.updatePkgCommunities(pkgName, commList,
            clearOtherEntries);
   }

   /**
    * Converts a comma delimited name list (as string) to a list of strings.
    * 
    * @param commaList the comma delimited name list, assumed not blank.
    * 
    * @return the converted list, never <code>null</code>, may be empty.
    */
   private List<String> getListFromString(String commaList)
   {
      List<String> nameList = new ArrayList<String>();
      if (StringUtils.isNotBlank(commaList))
      {
         String[] commNames = commaList.split(NAME_SEPARATOR);
         for (String commName : commNames)
         {
            nameList.add(commName);
         }
      }
      return nameList;
   }
   
   /**
    * Returns the configuration service.
    * @return the service instance, never <code>null</code>.
    */
   private IPSConfigService getConfigService()
   {
      return PSConfigServiceLocator.getConfigService();
   }
   
   /**
    * Returns the package info service.
    * @return the service, never <code>null</code>.
    */
   private IPSPkgInfoService getPkgService()
   {
      return PSPkgInfoServiceLocator.getPkgInfoService();
   }

   /**
    * Determines install status of the package.
    * 
    * @param info assumed not <code>null</code>.
    * @return the status code.
    */
   private String getInstalledStatus(PSPkgInfo info)
   {
      if (info.getLastAction().equals(PackageAction.UNINSTALL))
         return UNINSTALLED;
      if (info.isSuccessfullyInstalled())
         return SUCCESS;
      return ERROR;
   }

   /**
    * Determines configured status of the package.
    * 
    * @param info assumed not <code>null</code>.
    * @return the status code.
    */
   private String getConfiguredStatus(PSPkgInfo info)
   {
      if (info.isCreated() || !info.isSuccessfullyInstalled()
            || info.getLastAction().equals(PackageAction.UNINSTALL))
         return NONE;

      IPSConfigService cfgService = PSConfigServiceLocator.getConfigService();
      List<PSConfigStatus> cfgs = cfgService.getConfigStatus(info
            .getPackageDescriptorName());
      if (cfgs.isEmpty() && info.isSuccessfullyInstalled())
         return NONE;
      if (cfgs.get(0).getStatus() == ConfigStatus.SUCCESS)
         return SUCCESS;
         
      return ERROR;
   }
   
   // Constants for installed and configured status;
   static final String SUCCESS = "Success";

   static final String ERROR = "Error";

   static final String WARNING = "Warning";

   static final String NONE = "None";

   static final String INFO = "Info";

   static final String UNINSTALLED = "Uninstall";
   
   // Constants for package categories
   static final String SYSTEM = "System";
   
   static final String USER = "User";

   //Constants for package lock status
   static final String PACKAGE_LOCKED = "Locked";
   static final String PACKAGE_UNLOCKED = "Unlocked"; 
   /**
    * Separator used for separating communities.
    */
   public static final String NAME_SEPARATOR = ",";
   
  /**
   * The logger for this class.
   */
  private static Logger ms_logger = Logger.getLogger("PSPackageService");

}
