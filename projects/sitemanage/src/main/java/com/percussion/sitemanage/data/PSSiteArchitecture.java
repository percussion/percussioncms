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
package com.percussion.sitemanage.data;

import com.percussion.share.data.PSAbstractPersistantObject;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;

@XmlRootElement(name="SiteArchitecture")
public class PSSiteArchitecture extends PSAbstractPersistantObject
{
   /*
    * (non-Javadoc)
    * @see com.percussion.share.data.PSAbstractPersistantObject#getId()
    */
   @Override
   public String getId()
   {
      return getName();
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.share.data.PSAbstractPersistantObject#setId(java.io.Serializable)
    */
   @Override
   public void setId(String id)
   {
      setName(id);
   }

   /**
    * @return The name of the site, never blank.
    */
   public String getName()
   {
      return name;
   }

   /**
    * @param name of the site must not be blank.
    */
   public void setName(String name)
   {
      if(StringUtils.isBlank(name))
         throw new IllegalArgumentException("name must not be blank");
      this.name = name;
   }

   /**
    * @return the sub sections of site, may be <code>null</code> or empty.
    */
   public List<PSSiteSection> getSections()
   {
      return sections;
   }

   /**
    * @param sections sub sections of site, may be <code>null</code> or empty.
    */
   public void setSections(List<PSSiteSection> sections)
   {
      this.sections = sections;
   }

   /**
    * The name of the site.
    */
   private String name;
   
   /**
    * List of {@link PSSiteSection} objects that are under the site.
    */
   private List<PSSiteSection> sections;
   
   /**
    * 
    */
   private static final long serialVersionUID = 8249374630117416709L;



}
