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
package com.percussion.services.pkginfo.utils;

import com.percussion.rx.config.IPSConfigService;
import com.percussion.rx.config.PSConfigServiceLocator;
import com.percussion.rx.design.PSDesignModelUtils;
import com.percussion.rx.services.deployer.PSPackageVisibility;
import com.percussion.server.PSServer;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.util.IOTools;
import com.percussion.util.PSOsTool;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class which provides support for detecting modifications to packaged
 * elements.
 */
public class PSPkgHelper
{
   /**
    * Detects modifications to packaged elements which have been made outside
    * of configuration, i.e., via the workbench.
    * 
    * @param guid The id of the package info object whose contents will be
    * examined for modifications.  May not be <code>null</code>.
    * 
    * @return A set of design objects which have been modified outside of
    * configuration.  Never <code>null</code>, may be empty.  Each entry in
    * the set will specify the design object, see
    * {@link #validatePkgElement(PSPkgElement, Collection)}.
    */
   public static Set<String> validatePackage(IPSGuid guid)
   {
      if (guid == null)
      {
         throw new IllegalArgumentException("guid may not be null");
      }
      // get community visibility configuration
      Collection<String> comms = getCommunityVisibility(guid);            
      
      // validating each package element
      Set<String> objects = new HashSet<String>();
      List<IPSGuid> pkgElemGuids = getPkgInfoService().findPkgElementGuids(
            guid);
      for (IPSGuid pkgElemGuid : pkgElemGuids)
      {
         PSPkgElement pkgElem = getPkgInfoService().findPkgElement(pkgElemGuid);
         if (pkgElem == null)
         {
            // should never happen
            String msg = "Failed to get the package element for guid {0}";
            Object[] args = { pkgElemGuid.toString() };
            throw new RuntimeException(MessageFormat.format(msg, args));
         }
                
         objects.addAll(validatePkgElement(pkgElem, comms));
      }
      
      return objects;
   }

   /**
    * Updates each element version in the supplied package with the current
    * version of the corresponding design object.
    * 
    * @param pkgName The package name, may not be blank.
    */
   public static void updatePkgElementVersions(String pkgName)
   {
      if (!ms_enabled)
         return;
      
      if (StringUtils.isBlank(pkgName))
      {
         throw new IllegalArgumentException("pkgName may not be blank");
      }
      
      PSPkgInfo pkgInfo = getPkgInfoService().findPkgInfo(pkgName);
      if (pkgInfo == null)
      {
         String msg = "Failed to get the package info for {0}";
         Object[] args = { pkgName };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      
      List<IPSGuid> pkgElemGuids = getPkgInfoService().findPkgElementGuids(
            pkgInfo.getGuid());
      for (IPSGuid pkgElemGuid : pkgElemGuids)
      {
         updatePkgElementVersion(pkgElemGuid, true);
      }
   }
   
   /**
    * Detects modifications to packaged elements which have been made outside
    * of configuration, i.e., via the workbench.
    * 
    * @param pkgName The name of the package.  May not be blank.
    * 
    * @return See {@link #validatePackage(IPSGuid)}.
    */
   public static Set<String> validatePackage(String pkgName)
   {
      if (!ms_enabled)
         return new HashSet<String>();
      
      if (StringUtils.isBlank(pkgName))
      {
         throw new IllegalArgumentException("pkgName may not be blank");
      }
      
      PSPkgInfo pkgInfo = getPkgInfoService().findPkgInfo(pkgName);
      if (pkgInfo == null)
      {
         String msg = "Failed to get the package info for {0}";
         Object[] args = { pkgName };
         throw new RuntimeException(MessageFormat.format(msg, args));
      }
      
      return validatePackage(pkgInfo.getGuid());
   }

   /**
    * Updates the version for the supplied package element.
    * 
    * @param id the ID of the package element, never <code>null</code>.
    * @param forceUpdate if <code>true</code> the version of the supplied
    * package element will always be updated with the current version of the
    * associated design object, otherwise the element version will updated if
    * the design object has not been modified outside of allowed configuration.
    */
   private static void updatePkgElementVersion(IPSGuid id,
      boolean forceUpdate)
   {
      PSPkgElement pkgElem = null;
      try
      {
         pkgElem = getPkgInfoService().loadPkgElementModifiable(id);
      }
      catch (PSNotFoundException e)
      {
         // should never happen
         String msg = "Failed to get the package element for guid {0}";
         Object[] args = { id.toString() };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
      
      Long version = null;
      try
      {
         version = getVersion(pkgElem);
      }
      catch (IOException e)
      {
         String msg = "Failed to get the design object version for package "
            + "element with guid {0}";
         Object[] args = { id.toString() };
         throw new RuntimeException(MessageFormat.format(msg, args), e);
      }
      
      if (version == null)
      {
         // object will not be tracked
         return;
      }
      
      if (pkgElem.getVersion() != OBJECT_MODIFIED_VERSION || forceUpdate)
      {
         pkgElem.setVersion(version);
         getPkgInfoService().savePkgElement(pkgElem);
      }
   }
   
   /**
    * Updates the corresponding element versions for the supplied Design Objects
    * 
    * @param ids the IDs of the Design Objects, never <code>null</code>, may be
    * empty.
    */
   public static void updatePkgElementVersions(Collection<IPSGuid> ids)
   {
      if (!ms_enabled)
         return;
      
      if (ids == null)
         throw new IllegalArgumentException("ids may not be null");

      for (IPSGuid id : ids)
      {
         IPSGuid elemId = id;
         
         PSTypeEnum type = PSTypeEnum.valueOf(id.getType());
         if (PSIdNameHelper.isSupported(type))
         {
            String name = PSDesignModelUtils.getName(id);
            if (name != null)
            {
               // get pkg element id generated by name service
               elemId = PSIdNameHelper.getGuid(name, type);
            }
         }
                    
         PSPkgElement pkgElem = getPkgInfoService().findPkgElementByObject(
               elemId);
         if (pkgElem == null)
            continue; // skip if not tracked by any installed package
                  
         updatePkgElementVersion(pkgElem.getGuid(), false);
      }
   }
   
   /**
    * Determines if the specified package element has been modified outside of
    * allowed configuration.  If the element has been modified outside of
    * allowed configuration it will be flagged as such.
    * 
    * @param pkgElem The package element, may not be <code>null</code>.
    * @param comms the set of community names that should have set to the design
    * object of the package element. It may be <code>null</code> if does not
    * validate the community visibility for the installed package elements.
    * 
    * @return a list of warning messages, which represent the design object
    * represented by the package element if the object has been modified outside
    * of configuration. The design object name and type will be shown as
    * follows: rffSnCallout(Template). It can never be <code>null</code>, but
    * may be empty if the object has not been modified externally.
    */
   public static List<String> validatePkgElement(PSPkgElement pkgElem,
         Collection<String> comms)
   {
      if (pkgElem == null)
      {
         throw new IllegalArgumentException("pkgElem may not be null");
      }
            
      List<String> warnList = new ArrayList<String>();
      
      IPSGuid objGuid = pkgElem.getObjectGuid();
      PSTypeEnum objType = PSTypeEnum.valueOf(pkgElem.getObjectType());
      
      // skip ACL here, the ACL of the design object will be validated through
      // community visibility
      if (objType.equals(PSTypeEnum.ACL))
         return warnList; 
      
      String objName;         
      if (PSIdNameHelper.isSupported(objType))
      {
         objName = PSIdNameHelper.getName(objGuid);
      }
      else
      {
         objName = PSDesignModelUtils.getName(objGuid);
      }
            
      Long version = null;
      try
      {
         version = getVersion(pkgElem);
      }
      catch (IOException e)
      {
         String msg = "Failed to get the design object version for package "
            + "element guid {0}";
         Object[] args = { pkgElem.getGuid().toString() };
         throw new RuntimeException(MessageFormat.format(msg, args), e);            
      }
      
      if (version != null)
      {      
         // compare package element version with current version
         if (!version.equals(pkgElem.getVersion()))
         {
            warnList.add(objName + '(' + objType.getDisplayName() + ')');
         }
      }
      
      // validate community visibility
      if (comms != null)
      {
         String commWarn = validateCommunityVisibility(objGuid, objName,
               objType.getDisplayName(), comms);
         if (commWarn != null)
            warnList.add(commWarn);
      }
      
      if (pkgElem.getVersion() != OBJECT_MODIFIED_VERSION &&
            !warnList.isEmpty())
      {
         // flag as modified
         PSPkgElement pElem = getPkgInfoService().loadPkgElementModifiable(
               pkgElem.getGuid());
         pElem.setVersion(OBJECT_MODIFIED_VERSION);
         getPkgInfoService().savePkgElement(pElem);
      }
      
      return warnList;
   }
   
   /**
    * Validates the community visibility of the given Design Object.
    *  
    * @param id the ID of the design object, assumed not <code>null</code>.
    * @param name the name of the design object, assumed not blank.
    * @param type the type of the design object, assumed not blank.
    * @param commsVisibility the current community visibility configuration,
    * assumed not <code>null</code>, be may empty.
    * 
    * @return a warning if failed the validation; otherwise return 
    * <code>null</code>.
    */
   private static String validateCommunityVisibility(IPSGuid id, String name,
         String type, Collection<String> commsVisibility)
   {
      PSPackageVisibility vis = new PSPackageVisibility();
      if (!vis.validatePkgCommunities(id, commsVisibility))
      {
         return name + "(" + type
               + ") does not match the configuration of community visibility.";
      }
      
      return null;
   }
   
   /**
    * Gets the current version of the design object which corresponds to the
    * supplied package element.
    * 
    * @param pkgElem The package element, assumed not <code>null</code>.
    * 
    * @return The design object version corresponding to the package element.
    * May be <code>null</code> if the object does not support version or is not
    * supported for tracking.
    * 
    * @throws IOException on any error for file design objects. 
    */
   private static Long getVersion(PSPkgElement pkgElem) throws IOException
   {
      Long version = null;
      
      IPSGuid objGuid = pkgElem.getObjectGuid();
      PSTypeEnum objType = PSTypeEnum.valueOf(objGuid.getType());
      if (PSIdNameHelper.isSupported(objType))
      {
         String objName = PSIdNameHelper.getName(objGuid);
         if (objName != null)
         {
            // load version by name
            version = PSDesignModelUtils.getVersion(objType, objName);
            if (version == null)
            {
               // must be a file
               if (objName.startsWith("/") || objName.startsWith("\\"))
               {
                  // strip off leading file separator
                  if (objName.trim().length() > 1)
                     objName = objName.substring(1);
               }
               
               if (PSOsTool.isUnixPlatform())
               {
                  // normalize path
                  objName = objName.replace('\\', '/');
               }
               File file = new File(PSServer.getRxDir(), objName);
               if (file.exists())
                  version = IOTools.getChecksum(file);
            }
         }
      }
      else
      {
         // load version by guid
         version = PSDesignModelUtils.getVersion(objGuid);
      }
      
      return version;
   }
   
   /**
    * Used by config service unit tests to enable/disable various methods of
    * the package helper which should not be run in conjunction with the unit
    * tests.
    * 
    * @param enabled <code>true</code> to enable methods, <code>false</code> to
    * disable.
    */
   public static void setEnabled(boolean enabled)
   {
      ms_enabled = enabled;
   }
     
   /**
    * Get the package info service.  Initialize if necessary.
    * 
    * @return The package info service.  Never <code>null</code>.
    */
   private static IPSPkgInfoService getPkgInfoService()
   {
      if (ms_pkgInfoSvc == null)
      {
         ms_pkgInfoSvc = PSPkgInfoServiceLocator.getPkgInfoService();
      }
      
      return ms_pkgInfoSvc;
   }
   
   /**
    * Loads the community visibility for the supplied package.
    * 
    * @param guid The package id, assumed not <code>null</code>.
    * 
    * @return A collection of community names in which the package is visible.
    * Never <code>null</code>, may be empty.
    */
   private static Collection<String> getCommunityVisibility(IPSGuid guid)
   {
      // get community visibility configuration
      PSPkgInfo pkg = getPkgInfoService().loadPkgInfo(guid);
      IPSConfigService srv = PSConfigServiceLocator.getConfigService();
      return srv.loadCommunityVisibility(pkg.getPackageDescriptorName());  
   }
   
   /**
    * Constant which indicates that a package element has been modified outside
    * of allowed configuration.
    */
   public static long OBJECT_MODIFIED_VERSION = -1L;
   
   /**
    * The package info service, may be <code>null</code>.
    */
   private static IPSPkgInfoService ms_pkgInfoSvc = null;
   
   /**
    * Flag to enable/disable package helper.  Defaults to <code>true</code>.
    */
   private static boolean ms_enabled = true;
}
