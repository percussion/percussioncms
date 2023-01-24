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
package com.percussion.taxonomy.repository;

import com.percussion.taxonomy.domain.Taxonomy;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import java.util.Collection;
import java.util.List;

public class HibernateTaxonomyDAO extends HibernateDaoSupport implements TaxonomyDAO
{

   public Taxonomy getTaxonomy(int id)
   {
      return (Taxonomy) getHibernateTemplate().get(Taxonomy.class, new Integer(id));
   }

   public List<Taxonomy> getTaxonomy(String name)
   {
      Session sess = this.currentSession();
         Criteria c = sess.createCriteria(Taxonomy.class); 
         c.add(Restrictions.ilike("Name", name));
         c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
         return c.list();
   } 
   
   public List<Integer> getTaxonomyIdForName(String name)
   {
    
      Session sess = this.currentSession();

         Query query = sess.createQuery("select id from Taxonomy where name like :name");
         query.setString("name", name);
         @SuppressWarnings("unchecked")
         List<Integer> taxIds = query.list();
         return taxIds;
   }
   
   public Collection getAllTaxonomys()
   {
      // Optional: Add order by to query
      return (Collection) getHibernateTemplate().execute(new HibernateCallbackHandler());
   }

   public void saveTaxonomy(Taxonomy taxonomy)
   {
      getHibernateTemplate().saveOrUpdate(taxonomy);
   }

   public void removeTaxonomy(Taxonomy taxonomy)
   {
      getHibernateTemplate().delete(taxonomy);
   }

   class HibernateCallbackHandler implements HibernateCallback
   {

      private int taxonomy_id;

      public HibernateCallbackHandler()
      {
      }

      public HibernateCallbackHandler(int taxonomy_id)
      {
         this.taxonomy_id = taxonomy_id;
      }

      public int getTaxonomy_id()
      {
         return taxonomy_id;
      }

      public void setTaxonomy_id(int taxonomy_id)
      {
         this.taxonomy_id = taxonomy_id;
      }

      public Object doInHibernate(Session session) throws HibernateException
      {
         Query query = session.createQuery("from Taxonomy tax order by lower(name) asc");
         return query.list();
      }
   }
}
