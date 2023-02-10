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


/**
 * This class represents the community and the associated packages.
 * @author bjoginipally
 *
 */
public class PSCommunityPackage
{
   /**
    * ctor
    *
    */
   PSCommunityPackage()
   {
      
   }
   
   /**
    * ctor
    * @param name @see {@link #setCommunity(String)} for details.
    * @param packages @see {@link #setPackages(String)} for details.
    */ 
   PSCommunityPackage(String name, String packages)
   {
      m_community = name;
      m_packages = packages;
   }
   
   /**
    * @return the community never <code>null</code>
    */
   @XmlElement(name = "community")
   public String getCommunity()
   {
      return m_community;
   }
   
   /**
    * 
    * @param community must not be <code>blank</code>.
    */
   public void setCommunity(String community)
   {
      if (community == null || community.trim().length()<1)
         throw new IllegalArgumentException("community must not be blank");
      m_community = community;
   }
   
   /**
    * 
    * @return packages associated with the community, never <code>null</code>, may be
    * empty. {@link PSPackageService#NAME_SEPARATOR} separated list.
    */
   public String getPackages()
   {
      return m_packages;
   }

   /**
    * @param packages, May be <code>null</code> or empty. If
    * <code>null</code> sets it to empty string. If set must be a
    * {@link PSPackageService#NAME_SEPARATOR} separated list.
    */
   public void setPackages(String packages)
   {
      m_packages = packages;
   }
   
   
   private String m_community;
   private String m_packages;
   

}
