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
package com.percussion.data.utils;

import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
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

   /**
    * Generate a new identifier.
    *
    * @param session The session from which the request originates
    * @param obj  the entity or collection (idbag) for which the id is being generated
    * @return a new identifier
    * @throws HibernateException Indicates trouble generating the identifier
    */
   @Override
   public Serializable generate(SharedSessionContractImplementor session, Object obj) throws HibernateException
   {
         if (gmgr==null)
            gmgr = PSGuidManagerLocator.getGuidMgr();

         return  gmgr.createGuid(guidType).longValue();
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
