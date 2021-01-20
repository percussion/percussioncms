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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "Packages")
public class PSPackageCommunities
{
   /**
    * 
    */
   public PSPackageCommunities()
   {
      super();
   }

   /**
    * @param packages
    */
   public PSPackageCommunities(List<PSPackageCommunity> packages)
   {
      super();
      if (packages != null)
         m_packageCommunities = packages;
   }

   /**
    * @return the packages
    */
   @XmlElement(name = "package")
   public List<PSPackageCommunity> getPackages()
   {
      return m_packageCommunities;
   }

   /**
    * @return all communities
    */
   @XmlElement(name = "allcommunities")
   public String getAllCommunities()
   {
      String result = "";
      PSPackageVisibility vis = new PSPackageVisibility();
      List<String> comms = vis.getAllCommunities();
      for (String comm : comms)
      {
         result += comm + PSPackageService.NAME_SEPARATOR;
      }
      if (result.endsWith(PSPackageService.NAME_SEPARATOR))
         result = result.substring(0, result.length() - 1);
      return result;
   }

   /**
    * @param packages the packages to set
    */
   public void setPackages(List<PSPackageCommunity> packages)
   {
      m_packageCommunities = packages;
   }

   /**
    * Adds a packagecommunity object to the collection.
    * 
    * @param pkg the package to add, cannot be <code>null</code>.
    */
   public void add(PSPackageCommunity pkgComm)
   {
      if (pkgComm == null)
         throw new IllegalArgumentException("pkg cannot be null.");
      m_packageCommunities.add(pkgComm);
   }

   /**
    * Removes the specified package community object from the collection if it
    * exists.
    * 
    * @param pkgComm the package community object to be removed. May be
    * <code>null</code>.
    */
   public void remove(PSPackageCommunity pkgComm)
   {
      m_packageCommunities.remove(pkgComm);
   }

   /**
    * Removes all the packages community objects from the collection.
    */
   public void clear()
   {
      m_packageCommunities.clear();
   }

   /**
    * The list of package community objects, never <code>null</code>, may be
    * empty.
    */
   private List<PSPackageCommunity> m_packageCommunities = 
      new ArrayList<PSPackageCommunity>();
}
