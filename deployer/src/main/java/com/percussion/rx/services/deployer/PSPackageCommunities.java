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
    * @param pkgComm the package to add, cannot be <code>null</code>.
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
      new ArrayList<>();
}
