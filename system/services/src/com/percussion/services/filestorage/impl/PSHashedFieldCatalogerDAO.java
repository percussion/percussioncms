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

package com.percussion.services.filestorage.impl;

import com.percussion.services.filestorage.IPSHashedFieldCatalogerDAO;
import com.percussion.services.filestorage.data.PSHashedColumn;
import com.percussion.services.filestorage.data.PSHashedColumn.HashedColumnsPK;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;

public class PSHashedFieldCatalogerDAO implements IPSHashedFieldCatalogerDAO
{


   /**
    * The hibernate session factory injected by spring
    */
   private SessionFactory sessionFactory;
   
   /**
    * The default empty constructor
    */
   public PSHashedFieldCatalogerDAO() {
      
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFieldCatalogerDAO#saveAll(java.util.Set)
    */
   @Override
   public void saveAll(Set<PSHashedColumn> columns)
   {
      for (PSHashedColumn column : columns) {
         save(column);
      }
   }
   
   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFieldCatalogerDAO#getStoredColumns()
    */
   @Override
   public Set<PSHashedColumn> getStoredColumns()
   {
      Criteria crit = getSession().createCriteria(PSHashedColumn.class);
      crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
      return new HashSet<PSHashedColumn>(crit.list());

   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFieldCatalogerDAO#remove(com.percussion.services.filestorage.data.PSHashedColumn)
    */
   @Override
   public void remove(PSHashedColumn col)
   {
      getSession().delete(col);
      getSession().flush();
   }

   /* (non-Javadoc)
    * @see com.percussion.services.filestorage.IPSHashedFieldCatalogerDAO#save(com.percussion.services.filestorage.data.PSHashedColumn)
    */
   @Override
   public void save(PSHashedColumn newCol)
   {
      HashedColumnsPK pk = new PSHashedColumn.HashedColumnsPK(newCol.getTablename(), newCol.getColumnName());
      PSHashedColumn dbCol = (PSHashedColumn) getSession().get(PSHashedColumn.class, pk);
      if (dbCol == null)
         getSession().saveOrUpdate(newCol);
   }

   /**
    * The hibernate session factory injected by spring
    * @param sessionFactory
    */
   public void setSessionFactory(SessionFactory sessionFactory)
   {
      this.sessionFactory = sessionFactory;
   }

   /**
    * @return The hibernate session factory injected by spring
    */
   public Session getSession()
   {
      return sessionFactory.getCurrentSession();
   }
}
