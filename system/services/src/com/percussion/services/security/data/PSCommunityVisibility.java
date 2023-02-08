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
package com.percussion.services.security.data;

import com.percussion.services.catalog.IPSCatalogItem;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * This class is a container for all visible design objects for a specific
 * community.
 */
public class PSCommunityVisibility implements Serializable, IPSCatalogItem
{
   /**
    * Compiler generated serial version ID used for serialization.
    */
   private static final long serialVersionUID = 421234159861071968L;

   /**
    * The community id for which this shows all visible design object summaries.
    */
   private long id;
   
   /**
    * A set with all design object summries that are visible for the referenced
    * community, never <code>null</code>, may be empty.
    */
   private Set<PSObjectSummary> visibleObjects = 
      new HashSet<>();
   
   /**
    * The bean pattern requires an empty constructor, for internal use only.
    */
   public PSCommunityVisibility()
   {
   }
   
   /**
    * Construct a new community visiblity for the supplied community id.
    * 
    * @param id the community id for which to construct this, not 
    *    <code>null</code>.
    */
   public PSCommunityVisibility(IPSGuid id)
   {
      setGUID(id);
   }
   
   /**
    * Get the object summaries of all visible objects for the defined community.
    * 
    * @return a set of visible object summaries, never <code>null</code>, 
    *    may be empty.
    */
   public Set<PSObjectSummary> getVisibleObjects()
   {
      return visibleObjects;
   }
   
   /**
    * Set a new set of visible objects summaries for this defined community.
    * 
    * @param visibleObjects the new set of visible object summaries, may be
    *    <code>null</code> or empty.
    */
   public void setVisibleObjects(Set<PSObjectSummary> visibleObjects)
   {
      if (visibleObjects == null)
         this.visibleObjects = new HashSet<>();
      else
         this.visibleObjects = visibleObjects;
   }
   
   /**
    * Add a new visible object summary.
    * 
    * @param summary the summary of a visible design object, not 
    *    <code>null</code>.
    */
   public void addVisibleObject(PSObjectSummary summary)
   {
      if (summary == null)
         throw new IllegalArgumentException("summary cannot be null");
      
      visibleObjects.add(summary);
   }
   
   /**
    * Add all supplied visible objects.
    * 
    * @param summaries the visible objects to add, not <code>null</code>, 
    *    may be empty.
    */
   public void addAllVisibleObjects(List<PSObjectSummary> summaries)
   {
      if (summaries == null)
         throw new IllegalArgumentException("summaries cannot be null");
    
      visibleObjects.addAll(summaries);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#getGUID()
    */
   public IPSGuid getGUID()
   {
      return new PSGuid(PSTypeEnum.COMMUNITY_DEF, id);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#setGUID(IPSGuid)
    */
   public void setGUID(IPSGuid newguid) throws IllegalStateException
   {
      if (newguid == null)
         throw new IllegalArgumentException("newguid cannot be null");
      
      if (newguid.getType() != PSTypeEnum.COMMUNITY_DEF.getOrdinal())
         throw new IllegalArgumentException(
            "newguid must be an id of a community");
      
      id = newguid.getUUID();
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#fromXML(String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSCommunityVisibility)) return false;
      PSCommunityVisibility that = (PSCommunityVisibility) o;
      return id == that.id && Objects.equals(getVisibleObjects(), that.getVisibleObjects());
   }

   @Override
   public int hashCode() {
      return Objects.hash(id, getVisibleObjects());
   }

   @Override
   public String toString() {
      final StringBuffer sb = new StringBuffer("PSCommunityVisibility{");
      sb.append("id=").append(id);
      sb.append(", visibleObjects=").append(visibleObjects);
      sb.append('}');
      return sb.toString();
   }
}

