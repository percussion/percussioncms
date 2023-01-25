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
package com.percussion.webservices.transformation.converter;

import com.percussion.cms.handlers.PSRelationshipCommandHandler;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipPropertyData;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.webservices.common.Relationship;
import com.percussion.webservices.common.RelationshipPropertiesProperty;

import java.util.List;

import org.apache.commons.beanutils.BeanUtilsBean;

/**
 * Converts objects between the classes
 * {@link com.percussion.design.objectstore.PSRelationship} and
 * {@link com.percussion.webservices.system.PSRelationship}
 */
public class PSRelationshipConverter extends PSConverter
{
   /*
    * (non-Javadoc)
    *
    * @see PSConverter#PSConvert(BeanUtilsUtil)
    */
   public PSRelationshipConverter(BeanUtilsBean beanUtils)
   {
      super(beanUtils);
   }

   /*
    * (non-Javadoc)
    *
    * @see PSConverter#convert(Class, Object)
    */
   @SuppressWarnings("deprecation")
   @Override
   public Object convert(Class type, Object value) {
      if (value == null)
         return null;

      if (isClientToServer(value))
      {
         com.percussion.webservices.system.PSRelationship source =
            (com.percussion.webservices.system.PSRelationship) value;

         return getRelationshipFromClient(source);
      }
      else // convert from server to webservice
      {
         PSRelationship source = (PSRelationship) value;
         com.percussion.webservices.system.PSRelationship target =
            new com.percussion.webservices.system.PSRelationship();
         setRelationshipFromServer(source, target);
         
         return target;
      }
   }
   
   /**
    * Copies relationship data from a specified objectstore to a specified
    * webservice object.
    * 
    * @param source the objectstore or source object, never <code>null</code>.
    *    Note, this may be an instance of {@link PSAaRelationship} when it is
    *    called by the derived class (@link PSAaRelationshipConverter}.
    * @param target the webservice or target object.
    */
   protected void setRelationshipFromServer(PSRelationship source, 
      Relationship target)
   {
      long id = new PSDesignGuid(source.getGuid()).getValue();
      target.setId(id);
      long ownerId = new PSDesignGuid(
            new PSLegacyGuid(source.getOwner())).getValue();
      target.setOwnerId(ownerId);
      long dependentId = new PSDesignGuid(
            new PSLegacyGuid(source.getDependent())).getValue();
      target.setDependentId(dependentId);
      target.setPersisted(source.isPersisted());
      target.setType(source.getConfig().getName());
      
      List<PSRelationshipPropertyData> srcProps;
      if (source instanceof PSAaRelationship)
         srcProps = ((PSAaRelationship)source).getAllAaUserProperties();
      else
         srcProps = source.getAllUserProperties();
      
      RelationshipPropertiesProperty[] tgtProps = new RelationshipPropertiesProperty[srcProps
            .size()];
      int i = 0;
      RelationshipPropertiesProperty tgtProp;
      for (PSRelationshipPropertyData srcProp : srcProps)
      {
         tgtProp = new RelationshipPropertiesProperty();
         tgtProp.setName(srcProp.getName());
         tgtProp.set_value(srcProp.getValue());
         tgtProp.setPersisted(srcProp.isPersisted());

         tgtProps[i++] = tgtProp;
      }
      target.setProperties(tgtProps);
   }
   
   /**
    * Converts relationship from webservice to objectstore object.
    *  
    * @param source the source relationship, never <code>null</code>.
    * 
    * @return the converted relationship, never <code>null</code>.
    */
   protected PSRelationship getRelationshipFromClient(Relationship source)
   {
      int id = new PSDesignGuid(source.getId()).getUUID();

      PSLegacyGuid ownerId = new PSLegacyGuid(source.getOwnerId());
      PSLocator owner = new PSLocator(ownerId.getContentId(), ownerId
            .getRevision());
      PSLegacyGuid dependentId = new PSLegacyGuid(source.getDependentId());
      PSLocator dependent = new PSLocator(dependentId.getContentId(),
            dependentId.getRevision());

      PSRelationshipConfig config = PSRelationshipCommandHandler
         .getRelationshipConfig(source.getType());

      PSRelationship target = new PSRelationship(id, owner, dependent,
            config);
      target.setPersisted(source.isPersisted());
      
      PSRelationshipPropertyData tgtProp;
      for (RelationshipPropertiesProperty prop : source.getProperties())
      {
         tgtProp = target.getUserProperty(prop.getName());
         tgtProp.setValue(prop.get_value());
         tgtProp.setPersisted(prop.isPersisted());
      }

      return target;
   }
}

