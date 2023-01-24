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

import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.PersistentIdentifierGenerator;
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
public class PSNextNumberHibernateGenerator implements IdentifierGenerator, Configurable
{
   /**
    * table Name extracted from configuration in hbm.xml file
    */
   private String tableName;

   /**
    * Generate a new identifier.
    *
    * @param session The session from which the request originates
    * @param object  the entity or collection (idbag) for which the id is being generated
    * @return a new identifier
    * @throws HibernateException Indicates trouble generating the identifier
    */
   @Override
   public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      return gmgr.createId("HIB_"+tableName);
   }


   @Override
   public void configure(Type type, Properties properties, ServiceRegistry serviceRegistry) throws MappingException {
      tableName = properties.getProperty(PersistentIdentifierGenerator.TABLE);
   }


}
