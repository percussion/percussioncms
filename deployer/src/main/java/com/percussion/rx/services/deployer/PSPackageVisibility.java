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
import com.percussion.rx.config.PSConfigException;
import com.percussion.rx.config.PSConfigServiceLocator;
import com.percussion.services.catalog.IPSCatalogSummary;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclEntry;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSPermissions;
import com.percussion.services.security.PSTypedPrincipal;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.security.IPSTypedPrincipal;
import com.percussion.utils.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.webservices.security.IPSSecurityDesignWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.acl.AclEntry;
import java.security.acl.NotOwnerException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class handles the visibility of packages and package elements with the
 * communities.
 * 
 * @author bjoginipally
 * 
 */
public class PSPackageVisibility
{
   /**
    * Gets the communities of the supplied guids.
    * 
    * @param guids must not be <code>null</code>.
    * @return Map of supplied guid and associated communities.
    */
   public Map<IPSGuid, String> getCommunities(List<IPSGuid> guids)
   {
      if (guids == null)
         throw new IllegalArgumentException("guids must not be null");
      Map<IPSGuid, String> objComms = new HashMap<IPSGuid, String>();
      List<IPSAcl> acls = loadAcl(guids);
      for (int i = 0; i < acls.size(); i++)
      {
         IPSAcl acl = acls.get(i);
         String comms = "";
         if (acl != null)
         {
            comms = getCommunitiesFromAcl(acl);
         }
         objComms.put(guids.get(i), comms);
      }
      return objComms;
   }

   /**
    * Returns the collection of community names associated to the supplied
    * object guid through acls.
    * 
    * @param id The guid of the object, must not be <code>null</code>.
    * @return Collection of community names never <code>null</code>, may be
    * empty.
    */
   public Collection<String> getCommunities(IPSGuid id)
   {
      if (id == null)
         throw new IllegalArgumentException("id must not be null");
      Set<String> comms = new HashSet<String>();
      List<IPSAcl> acls = loadAcl(Collections.singletonList(id));
      if (acls.get(0) != null)
      {
         comms.addAll(getCommunityListFromAcl(acls.get(0)));
      }
      return comms;
   }

   /**
    * Converts the supplied guids and then loads the ACLs for the converted
    * guids. See {@link #getConvertedGuids(List)}.
    * 
    * @param ids list of guids for which the acls needs to be loaded, assumed
    * not <code>null</code>.
    * 
    * @return One ACL for each corresponding object id. Some of the entries may
    * be <code>null</code> if the object does not have an ACL. The results are
    * in the same order as the supplied ids.
    */
   private List<IPSAcl> loadAcl(List<IPSGuid> ids)
   {
      List<IPSGuid> guids = getConvertedGuids(ids);
      IPSAclService aclServ = PSAclServiceLocator.getAclService();
      List<IPSAcl> acls = new ArrayList<IPSAcl>();
      try
      {
         acls = aclServ.loadAclsForObjects(guids);
         return acls;
      }
      catch (Exception e)
      {
         throw new PSConfigException("Error loading acls",e);
      }
   }

   /**
    * Helper method to extract communities from the supplied Acl. If the acl has
    * any community entry then just return that community.
    * 
    * @param acl assumed not <code>null</code>.
    * @return Comma separated string of communities. May be empty never
    * <code>null</code>.
    */
   private String getCommunitiesFromAcl(IPSAcl acl)
   {
      StringBuffer comms = new StringBuffer();
      for (String c : getCommunityListFromAcl(acl))
      {
         if (comms.length() > 0)
            comms.append(PSPackageService.NAME_SEPARATOR);
         comms.append(c);
      }
      return comms.toString();
   }

   /**
    * Helper method to extract communities from the supplied Acl. If the acl has
    * any community entry then just return that community.
    * 
    * @param acl assumed not <code>null</code>.
    * 
    * @return a list of community names. May be empty never <code>null</code>.
    */
   private Collection<String> getCommunityListFromAcl(IPSAcl acl)
   {
      Set<String> comms = new HashSet<String>();
      Enumeration<AclEntry> entries = acl.entries();
      while (entries != null && entries.hasMoreElements())
      {
         IPSAclEntry aclEntry = (IPSAclEntry) entries.nextElement();
         if (aclEntry.getTypedPrincipal().isCommunity())
         {
            String comName = aclEntry.getPrincipal().getName();
            if (comName.equals(PSTypedPrincipal.ANY_COMMUNITY_ENTRY))
            {
               comms.add(PSTypedPrincipal.ANY_COMMUNITY_ENTRY);
               break;
            }
            comms.add(aclEntry.getPrincipal().getName());
         }
      }
      return comms;
   }

   /**
    * Returns the list of all community names.
    * 
    * @return all communities list never <code>null</code>.
    */
   public List<String> getAllCommunities()
   {
      List<String> allComms = new ArrayList<String>();
      IPSSecurityDesignWs secWs = PSSecurityWsLocator
            .getSecurityDesignWebservice();
      List<IPSCatalogSummary> sums = secWs.findCommunities(null);
      for (IPSCatalogSummary sum : sums)
      {
         allComms.add(sum.getName());
      }
      return allComms;
   }

   /**
    * Set the community entries for the supplied object guid.
    * 
    * @param objectGuid Object guid must not be <code>null</code>.
    * @param communityNames must not be <code>null</code>. and Must be a
    * valid community list.
    * @param clearOtherCommEntries if <code>true</code> clears other community
    * entries on the design object.
    */
   public void setCommunities(IPSGuid objectGuid,
         Collection<String> communityNames, boolean clearOtherCommEntries)
   {
      if (objectGuid == null)
         throw new IllegalArgumentException("objectGuid must not be null");
      if (communityNames == null)
         throw new IllegalArgumentException("communityNames must not be null");
      if (!communityNames.isEmpty() && !areValidCommunities(communityNames))
         throw new IllegalArgumentException(
               "supplied communityNames are invalid.");
      if (!isVisibilitySupportedType(objectGuid))
         return;
      objectGuid = getConvertedGuids(Collections.singletonList(objectGuid))
            .get(0);

      IPSAclService aclServ = PSAclServiceLocator.getAclService();
      List<IPSAcl> acls = new ArrayList<IPSAcl>();
      try
      {
         acls = aclServ.loadAclsForObjectsModifiable(Collections
               .singletonList(objectGuid));
         IPSAcl acl = acls.get(0);
         if (acl == null)
         {
            IPSTypedPrincipal owner = new PSTypedPrincipal(
                  PSTypedPrincipal.DEFAULT_USER_ENTRY,
                  IPSTypedPrincipal.PrincipalTypes.USER);
            acl = aclServ.createAcl(objectGuid, owner);
            acls.clear();
            acls.add(acl);
         }

         if (clearOtherCommEntries)
            clearAclEntries(acl, null);

         for (String comm : communityNames)
         {
            IPSAclEntry entry = acl.createEntry(new PSTypedPrincipal(comm,
                  PrincipalTypes.COMMUNITY));
            entry.addPermission(PSPermissions.RUNTIME_VISIBLE);
            acl.addEntry(acl.getFirstOwner(), entry);
         }
         aclServ.saveAcls(acls);
      }
      catch (Exception e)
      {
         ms_log.debug("Error setting communities...",e);
      }
   }

   /**
    * Validates the community visibility for the given object ID, see if it
    * matches the supplied communities.
    * 
    * @param objId the object ID, it may not be <code>null</code>.
    * @param commsVisibility the list of community names to validate with, never
    * <code>null</code>, may be empty.
    * 
    * @return <code>true</code> if the community visibility of the given
    * object matches the supplied communities or the does not have community
    * visibility ACLs; <code>false</code> otherwise.
    */
   public boolean validatePkgCommunities(IPSGuid objId,
         Collection<String> commsVisibility)
   {
      if (objId == null)
         throw new IllegalArgumentException("objId may not be null.");

      if (!isVisibilitySupportedType(objId))
         return true;

      Collection<String> comms = getCommunities(objId);
      return comms.equals(commsVisibility);
   }

   /**
    * Load and set the package communities to the specified package.
    * 
    * @param pkg the package info, it may not be <code>null</code>.
    * 
    * @return <code>null</code> if successful load and set the communities;
    * otherwise return the error message should an error occurs.
    */
   public String setPkgCommunities(PSPkgInfo pkg)
   {
      if (pkg == null)
         throw new IllegalArgumentException("package may not be null.");

      try
      {
         IPSConfigService srv = PSConfigServiceLocator.getConfigService();
         Collection<String> comms = srv.loadCommunityVisibility(pkg
               .getPackageDescriptorName());
         IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
               .getPkgInfoService();
         List<PSPkgElement> pkgElems = pkgService.findPkgElements(pkg
               .getGuid());
         for (PSPkgElement element : pkgElems)
         {
            IPSGuid guid = element.getObjectGuid();
            setCommunities(guid, comms, true);
         }
         return null;
      }
      catch (Exception e)
      {
         ms_log.error("Failed to apply community visibility for package: \""
               + pkg.getPackageDescriptorName() + "\"", e);
         if (StringUtils.isBlank(e.getLocalizedMessage()))
            return e.toString();
         else
            return e.getLocalizedMessage();
      }
   }

   /**
    * Checks whether the supplied community list is valid communities are not.
    * 
    * @param communities assumed not <code>null</code>.
    * @return <code>true</code> if all the communities inside the supplied
    * list are valid communities, otherwise <code>false</code>.
    */
   private boolean areValidCommunities(Collection<String> communities)
   {
      List<String> comms = getAllCommunities();
      return comms.containsAll(communities);
   }

   /**
    * Clears the supplied community acl entry if exists from the supplied object
    * guids.
    * 
    * @param communityName must not be blank and must be a valid community.
    * @param objectGuids must not be <code>null</code>.
    */
   public void clearCommunity(String communityName, List<IPSGuid> objectGuids)
   {
      if (StringUtils.isBlank(communityName))
         throw new IllegalArgumentException("communityName must not be blank");
      if (!areValidCommunities(Collections.singletonList(communityName)))
         throw new IllegalArgumentException(
               "supplied communityName is invalid.");
      if (objectGuids == null)
         throw new IllegalArgumentException("objectGuids must not be null");
      IPSAclService aclServ = PSAclServiceLocator.getAclService();
      List<IPSGuid> guids = new ArrayList<IPSGuid>();
      for (IPSGuid guid : objectGuids)
      {
         if (isVisibilitySupportedType(guid))
            guids.add(guid);
      }
      guids = getConvertedGuids(guids);
      List<IPSAcl> acls = new ArrayList<IPSAcl>();
      try
      {
         acls = aclServ.loadAclsForObjectsModifiable(guids);
         List<IPSAcl> aclsToSave = new ArrayList<IPSAcl>();
         for (IPSAcl acl : acls)
         {
            if (acl == null)
               continue;
            aclsToSave.add(acl);
            clearAclEntries(acl, communityName);
         }
         aclServ.saveAcls(aclsToSave);
      }
      catch (Exception e)
      {
         throw new PSConfigException(e);
      }
   }

   /**
    * Returns <code>true</code> if the supplied guid type supports visibility
    * setting.
    * 
    * @param objectGuid assumed not <code>null</code>.
    * @return <code>true</code> if the supplied guid type supported by
    * security list, otherwise <code>false</code>.
    */
   private boolean isVisibilitySupportedType(IPSGuid objectGuid)
   {
      return ms_visibilitySupportedTypes.contains(PSTypeEnum
            .valueOf(objectGuid.getType()));
   }

   /**
    * Clears supplied community entry or all community acl entries if the
    * communityName is null, from the supplied acl.
    * 
    * @param acl assumed not <code>null</code>.
    * @param communityName if <code>null</code> all entries are removed, if
    * not specified community entry is removed.
    * @throws NotOwnerException
    */
   private void clearAclEntries(IPSAcl acl, String communityName)
      throws NotOwnerException
   {
      Enumeration<AclEntry> entries = acl.entries();
      List<IPSAclEntry> currEntries = new ArrayList<IPSAclEntry>();
      while (entries != null && entries.hasMoreElements())
      {
         IPSAclEntry aclEntry = (IPSAclEntry) entries.nextElement();
         if (aclEntry.getTypedPrincipal().isCommunity())
         {
            if (communityName == null)
               currEntries.add(aclEntry);
            else if (communityName.equals(aclEntry.getPrincipal().getName()))
               currEntries.add(aclEntry);
         }
      }
      for (IPSAclEntry entry : currEntries)
      {
         acl.removeEntry(acl.getFirstOwner(), entry);
      }
   }

   /**
    * Utility method to convert supplied guids into guids without host string.
    * 
    * @param guids List of guids to convert, must not be <code>null</code>.
    * @return List of converted guids, never <code>null</code>.
    */
   public List<IPSGuid> getConvertedGuids(List<IPSGuid> guids)
   {
      if (guids == null)
         throw new IllegalArgumentException("guids must not be null");
      List<IPSGuid> results = new ArrayList<IPSGuid>();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      for (IPSGuid guid : guids)
      {
         if (guid.getHostId() == 0)
         {
            results.add(guid);
            continue;
         }
         IPSGuid g = gmgr.makeGuid(Long.parseLong(guid.getUUID() + ""),
               PSTypeEnum.valueOf(guid.getType()));
         results.add(g);
      }
      return results;
   }

   /**
    * Set the community entries for the specified package. It sets the
    * communities for all the element in the given package.
    * 
    * @param pkgGuid the ID of the package, must not be <code>null</code>.
    * @param communities community names to be set to, must not be
    * <code>null</code>. and Must be a valid community list.
    * @param clearOtherCommEntries if <code>true</code> clears other community
    * entries on the design object.
    */
   public void setPackageCommunities(IPSGuid pkgGuid,
         Collection<String> communities, boolean clearOtherCommEntries)
   {
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      for (PSPkgElement element : pkgService.findPkgElements(pkgGuid))
      {
         IPSGuid guid = element.getObjectGuid();
         setCommunities(guid, communities, clearOtherCommEntries);
      }
   }

   /**
    * List of type {@link PSTypeEnum} types that support visibility.
    */
   public static List<PSTypeEnum> ms_visibilitySupportedTypes = new ArrayList<PSTypeEnum>();
   static
   {
      ms_visibilitySupportedTypes.add(PSTypeEnum.NODEDEF);
      ms_visibilitySupportedTypes.add(PSTypeEnum.DISPLAY_FORMAT);
      ms_visibilitySupportedTypes.add(PSTypeEnum.ACTION);
      ms_visibilitySupportedTypes.add(PSTypeEnum.SEARCH_DEF);
      ms_visibilitySupportedTypes.add(PSTypeEnum.SITE);
      ms_visibilitySupportedTypes.add(PSTypeEnum.TEMPLATE);
      ms_visibilitySupportedTypes.add(PSTypeEnum.VIEW_DEF);
      ms_visibilitySupportedTypes.add(PSTypeEnum.WORKFLOW);
   }

   /**
    * Logger for the assembler.
    */
   public static Log ms_log = LogFactory.getLog("PSPackageVisibility");
}
