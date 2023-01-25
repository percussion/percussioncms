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
   private List<PSPackage> m_packages = new ArrayList<>();
}
