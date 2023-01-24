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

import com.percussion.data.PSTableChangeEvent;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.exceptions.PSORMException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



/**
 * Update handler that deals with evicting objects from hibernate's secondary
 * cache. Needed to integrate updates in the Rhythmyx application framework to
 * services that are relying on cached data in Hibernate.
 * 
 * @author dougrand
 */
public class PSHibernateEvictionTableUpdateHandler
      extends
         PSTableUpdateHandlerBase
{

   private static final Logger log = LogManager.getLogger(PSHibernateEvictionTableUpdateHandler.class);

   /**
    * The primary keys that correspond to the tables
    */
   private String[] m_keys;

   /**
    * The classes that correspond to the tables
    */
   private Class[] m_classes;

   /**
    * Ctor
    * 
    * @param tables the set of tables of interest. The count of tables, primary
    *           key columns and persistence classes must match and must not be
    *           zero.
    * @param pkcolumns the primary key column names, the names must be non-<code>null</code>
    *           and not empty
    * @param persistenceClasses the persistence classes that correspond one for
    *           one with the tables and primary keys, values may be
    *           <code>null</code> to evict all instances of the matching class 
    *           from the cache, useful when objects specify a compound key.
    */
   public PSHibernateEvictionTableUpdateHandler(String tables[],
         String pkcolumns[], Class persistenceClasses[]) {
      super(tables);
      if (pkcolumns == null)
      {
         throw new IllegalArgumentException("pkcolumns may not be null");
      }
      if (persistenceClasses == null)
      {
         throw new IllegalArgumentException(
               "persistenceClasses may not be null");
      }
      if (tables.length != pkcolumns.length
            || pkcolumns.length != persistenceClasses.length)
      {
         throw new IllegalArgumentException(
               "The set of tables, keys and classes must match in length");
      }
      m_keys = pkcolumns;
      m_classes = persistenceClasses;
   }

   public Iterator getColumns(String tableName, int actionType)
   {
      int i = m_tables.indexOf(tableName.toLowerCase());

      if (i < 0)
         return null;

      List<String> rval = new ArrayList<>();
      if (m_keys[i] != null)
         rval.add(m_keys[i]);
      return rval.iterator();
   }

   public void tableChanged(PSTableChangeEvent e)
   {
      String tableName = e.getTableName().toLowerCase();
      int i = m_tables.indexOf(tableName);

      if (i < 0)
         return;

      Class clazz = m_classes[i];
      String keycol = m_keys[i];      
      Serializable data = null;
      if (keycol != null)
      {
         data = (Serializable) e.getColumns().get(keycol.toUpperCase());
         if (data == null)
         {
            log.error("No data to evict object of class {}  for column {}", clazz, keycol);
            return;
         }         
      }
      
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      try
      {
         cms.handleDataEviction(clazz, data);
      }
      catch (PSORMException e1)
      {
         log.error("Failed to evict instance of class {} id {} ", clazz, data);
      }
      catch(RuntimeException e2)
      {
         log.error("Error handling eviction ", e2.getMessage());
         log.debug(e2.getMessage(),e2);
      }
   }

}
