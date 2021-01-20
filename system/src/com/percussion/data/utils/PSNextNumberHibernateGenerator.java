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
package com.percussion.data.utils;

import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SessionImplementor;
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

   public Serializable generate(SessionImplementor arg0, Object arg1) throws HibernateException
   {
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      int hibid = gmgr.createId("HIB_"+tableName);
      return hibid;
   }


   @Override
   public void configure(Type type, Properties properties, ServiceRegistry serviceRegistry) throws MappingException {
      tableName = properties.getProperty(PersistentIdentifierGenerator.TABLE);
   }
}
