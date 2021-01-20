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

/**
 * Collection of package objects.
 * @author erikserating
 *
 */
@XmlRootElement(name = "Packages")
public class PSPackages
{   

   /**
    * 
    */
   public PSPackages()
   {
      super();
   }

   /**
    * @param packages
    */
   public PSPackages(List<PSPackage> packages)
   {
      super();
      if(packages != null)
         m_packages = packages;
   }

   /**
    * @return the packages
    */
   @XmlElement(name = "package")
   public List<PSPackage> getPackages()
   {
      return m_packages;
   }

   /**
    * @param packages the packages to set
    */
   public void setPackages(List<PSPackage> packages)
   {
      m_packages = packages;
   }
   
   /**
    * Adds a package to the collection.
    * @param pkg the package to add, cannot be <code>null</code>.
    */
   public void add(PSPackage pkg)
   {
      if(pkg == null)
         throw new IllegalArgumentException("pkg cannot be null.");
      m_packages.add(pkg);   
   }
   
   /**
    * Removes the specified package from the collection
    * if it exists.
    * @param pkg the package to be removed. May be <code>null</code>.
    */
   public void remove(PSPackage pkg)
   {
      m_packages.remove(pkg);   
   }
   
   /**
    * Removes all the packages from the collection.
    */
   public void clear()
   {
      m_packages.clear();
   }
   
   /**
    * The list of packages, never <code>null</code>, may
    * be empty.
    */
   private List<PSPackage> m_packages = new ArrayList<PSPackage>();
}
