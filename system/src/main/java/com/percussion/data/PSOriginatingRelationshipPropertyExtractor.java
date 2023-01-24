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
package com.percussion.data;

import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSOriginatingRelationshipProperty;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.util.IPSHtmlParameters;

/**
 * Extracts relationship properties from the originating relationship
 * available from the execution data. The originating relationship is the one
 * that initiated the current request.
 */
public class PSOriginatingRelationshipPropertyExtractor extends PSDataExtractor
{
   /**
    * Creates a new <code>IPSReplacementValue</code> extractor for the 
    * supplied relationship property.
    * 
    * @param source the relationship property to construct the 
    *    IPSReplacementValue for, may be <code>null</code>.
    */
   public PSOriginatingRelationshipPropertyExtractor(
      PSOriginatingRelationshipProperty source)
   {
      super(source);
   }

   /**
    * Extract the relationship property source from the supplied execution data.
    * 
    * @param data the execution data to extract the property from, may be
    *    <code>null</code>.
    * @return the extracted relationship property as <code>String</code>, may 
    *    be <code>null</code>.
    */
   public Object extract(PSExecutionData data)
   {
      return extract(data, null);
   }

   /**
    * Extract the relationship property source from the supplied execution data
    * for the currently processed relationship.
    * 
    * @param data the execution data to extract the property from, may be
    *    <code>null</code>.
    * @param defValue the default value to be returned if the property cannot 
    *    be found, may be <code>null</code>.
    * @return the extracted relationship property as <code>String</code> or 
    *    the supplied default value if the property cannot be found, may 
    *    be <code>null</code>.
    */
   public Object extract(PSExecutionData data, Object defValue)
   {
      if (data == null)
         return defValue;
      
      PSRelationship relationship = data.getOriginatingRelationship();
      if (relationship == null)
         return defValue;
         
      IPSReplacementValue source = getSingleSource();
         
      if (source == null)
         return defValue;
         
      String name = source.getValueText();
      
      final String ownerPrefix =
         PSRelationshipConfig.XML_ATTR_OWNER + "/";
      
      final String propertyPrefix =
         PSRelationshipConfig.XML_ATTR_PROPERTY + "/";
      
      if (name.equalsIgnoreCase(PSRelationshipConfig.XML_ATTR_NAME))
         return relationship.getConfig().getName();
      else if(name.equalsIgnoreCase(PSRelationshipConfig.XML_ATTR_CATEGORY))
         return relationship.getConfig().getCategory();
      else if (name.indexOf(ownerPrefix) >= 0)
      {
         PSLocator ownerLocator = relationship.getOwner();
         
         if (ownerLocator==null)
            return defValue;
         
         String subName = name.substring(ownerPrefix.length());
         
         if (subName.equals(IPSHtmlParameters.SYS_CONTENTID))
            return "" + ownerLocator.getId();
         else if (subName.equals(IPSHtmlParameters.SYS_REVISION))
            return "" + ownerLocator.getRevision();
      }
      else if (name.indexOf(propertyPrefix) >= 0)
      {
         String propName = name.substring(propertyPrefix.length());
         
         String property = relationship.getProperty(propName);
         if (property != null)
            return property;
      }

      return defValue;
   }
}
