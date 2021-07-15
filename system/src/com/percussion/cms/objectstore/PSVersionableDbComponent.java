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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.cms.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import javax.persistence.Transient;

import org.w3c.dom.Element;


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
