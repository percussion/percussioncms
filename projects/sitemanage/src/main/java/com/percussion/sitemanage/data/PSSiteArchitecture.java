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
