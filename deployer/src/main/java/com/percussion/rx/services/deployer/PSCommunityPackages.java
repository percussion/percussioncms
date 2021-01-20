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


import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.PSPkgInfoServiceLocator;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.data.PSPkgInfo.PackageAction;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Communities")
public class PSCommunityPackages
{
   /**
    * 
    */
   public PSCommunityPackages()
   {
      super();
   }

   /**
    * @param packages
    */
   public PSCommunityPackages(List<PSCommunityPackage> packages)
   {
      super();
      if (packages != null)
         m_communitiesPackages = packages;
   }

   /**
    * @return the packages
    */
   @XmlElement(name = "community")
   public List<PSCommunityPackage> getPackages()
   {
      return m_communitiesPackages;
   }

   /**
    * @return all packages
    */
   @XmlElement(name = "allpackages")
   public String getAllPackages()
   {
      String result = "";
      IPSPkgInfoService pkgService = PSPkgInfoServiceLocator
            .getPkgInfoService();
      List<PSPkgInfo> pInfos = pkgService.findAllPkgInfos();
      for (PSPkgInfo pinfo : pInfos)
      {
         if (pinfo.isSuccessfullyInstalled()
               && !pinfo.getLastAction().equals(PackageAction.UNINSTALL))
            result += pinfo.getPackageDescriptorName()
                  + PSPackageService.NAME_SEPARATOR;
      }
      if (result.endsWith(PSPackageService.NAME_SEPARATOR))
         result = result.substring(0, result.length() - 1);
      return result;
   }

   /**
    * @param commPkgs the community packages to set
    */
   public void setPackages(List<PSCommunityPackage> commPkgs)
   {
      m_communitiesPackages = commPkgs;
   }

   /**
    * Adds a community package object to the collection.
    * 
    * @param commPkg the package to add, cannot be <code>null</code>.
    */
   public void add(PSCommunityPackage commPkg)
   {
      if (commPkg == null)
         throw new IllegalArgumentException("pkg cannot be null.");
      m_communitiesPackages.add(commPkg);
   }

   /**
    * Removes the specified community package object from the collection if it
    * exists.
    * 
    * @param commPkg the community package object to be removed. May be
    * <code>null</code>.
    */
   public void remove(PSCommunityPackage commPkg)
   {
      m_communitiesPackages.remove(commPkg);
   }

   /**
    * Removes all the community package objects from the collection.
    */
   public void clear()
   {
      m_communitiesPackages.clear();
   }

   /**
    * The list of community package objects, never <code>null</code>, may be
    * empty.
    */
   private List<PSCommunityPackage> m_communitiesPackages = new ArrayList<PSCommunityPackage>();
}
