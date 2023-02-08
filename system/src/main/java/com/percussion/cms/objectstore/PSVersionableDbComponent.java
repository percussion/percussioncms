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
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Element;

import javax.persistence.Transient;


/**
 * Base class for all versionable PSDbComponents.
 */
public abstract class PSVersionableDbComponent extends PSDbComponent
{
   /**
    * Empty ctor.
    */
   protected PSVersionableDbComponent()
   {}
   
   /**
    * Construct a versionable db component with key.  See
    * {@link PSDbComponent#PSDbComponent(PSKey)} for details.
    */
   protected PSVersionableDbComponent(PSKey locator)
   {
      super(locator);
   }
   
   @Override
   public void fromXml(Element source) throws PSUnknownNodeTypeException
   {
      super.fromXml(source);
   }
   
   /**
    * Just like {@link #equals(Object)}, except it considers the version.
    */
   @Override
   public boolean equals(Object obj)
   {
      if (!super.equals(obj))
         return false;
      
      PSVersionableDbComponent c = (PSVersionableDbComponent) obj;

      if (!m_version.equals(c.m_version))
         return false;
      
      return true;      
   }

   /**
    * Just like {@link #hashCode()}, except it considers the version.
    */
   @Override
   public int hashCode()
   {
      return super.hashCode() + m_version.hashCode();
   }

   /**
    * Just like {@link #equalsFull(Object)}, except it considers the version.
    */
   @Override
   public boolean equalsFull(Object obj)
   {
      if (!super.equalsFull(obj))
         return false;
      
      PSVersionableDbComponent c = (PSVersionableDbComponent) obj;
            
      if (!m_version.equals(c.m_version))
         return false;
      
      return true;
   }

   /**
    * Just like {@link #hashCodeFull()}, except it considers the version. 
    */
   @Override
   public int hashCodeFull()
   {
      return super.hashCodeFull() + m_version.hashCode();
   }

   /**
    * Get the version of this db component object.
    *
    * @return version of object.  Never <code>null</code>.
    */
   public Integer getVersion()
   {
      return m_version;
   }
   
   /**
    * Sets the version attribute of this object.
    *
    * @param version number, must be >= 0.
    */
   public void setVersion(Integer version)
   {
      if (version == null || version.intValue() < 0)
         throw new IllegalArgumentException("version must be >= 0");

      m_version = version;
   }
   
   /**
    * The version information of this object.
    */
   @Transient
   protected Integer m_version = 0;
}
