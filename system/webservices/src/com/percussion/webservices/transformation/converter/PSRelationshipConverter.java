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

