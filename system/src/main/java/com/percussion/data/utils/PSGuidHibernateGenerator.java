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
package com.percussion.data.utils;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.util.Properties;
/**
 * Hibernate ID Generator that uses the Percussion Next Number table to get Id
 * Key is based upon table name.
 * 
 * @author Stephen Bolton
 */
public class PSGuidHibernateGenerator implements IdentifierGenerator, Configurable
{
   public static final String GUID_TYPE = "guidType";
   
   private PSTypeEnum guidType = PSTypeEnum.INTERNAL;
   private IPSGuidManager gmgr = null;
   private String entityName;
   
   public Serializable generate(SessionImplementor session, Object obj) throws HibernateException
   {
         // If we want to allow the id to be set manually can get current value using following
         // otherwise any value stored before persistance will be overwritten.
         // final Serializable hibid = session.getEntityPersister( entityName, obj ).getIdentifier( obj, session.getEntityMode() );
      
         if (gmgr==null) gmgr = PSGuidManagerLocator.getGuidMgr();
         final Serializable hibid = gmgr.createGuid(guidType).longValue();
         return hibid;

   }

   @Override
   public void configure(Type type, Properties properties, ServiceRegistry serviceRegistry) throws MappingException {
      entityName = properties.getProperty(ENTITY_NAME);

      String param = properties.getProperty(GUID_TYPE);
      if (param!=null)
      {
         guidType = PSTypeEnum.valueOf(param);
      }
   }
}
