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

package com.percussion.services.filestorage.impl;

import com.percussion.services.filestorage.IPSHashedFieldCatalogerDAO;
import com.percussion.services.filestorage.data.PSHashedColumn;
import com.percussion.services.filestorage.data.PSHashedColumn.HashedColumnsPK;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Repository
@Scope("singleton")
@Transactional
public class PSHashedFieldCatalogerDAO implements IPSHashedFieldCatalogerDAO
{

   private SessionFactory sessionFactory;

   @Autowired
   public void setSessionFactory(SessionFactory sessionFactory) {
      this.sessionFactory = sessionFactory;
   }

   private Session getSession(){

      return sessionFactory.getCurrentSession();

   }

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
      return new HashSet<>(crit.list());

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
      PSHashedColumn dbCol = getSession().get(PSHashedColumn.class, pk);
      if (dbCol == null)
         getSession().saveOrUpdate(newCol);
   }


}
