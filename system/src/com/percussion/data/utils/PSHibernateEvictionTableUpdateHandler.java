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

import com.percussion.data.PSTableChangeEvent;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.exceptions.PSORMException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
   /**
    * Commons logger 
    */
   private static Log ms_log = LogFactory
         .getLog(PSHibernateEvictionTableUpdateHandler.class);

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

      List<String> rval = new ArrayList<String>();
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
            ms_log.error("No data to evict object of class " + clazz
                  + " for column " + keycol);
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
         ms_log.error("Failed to evict instance of class " + clazz + " id "
               + data);
      }
      catch(RuntimeException e2)
      {
         ms_log.error("Error handling eviction", e2);
      }
   }

}
