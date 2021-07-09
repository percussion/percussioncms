/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.taxonomy.repository;

import com.percussion.taxonomy.domain.Taxonomy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class HibernateTaxonomyDAO extends HibernateDaoSupport implements TaxonomyDAO
{

   public Taxonomy getTaxonomy(int id)
   {
      return (Taxonomy) getHibernateTemplate().get(Taxonomy.class, new Integer(id));
   }

   public List<Taxonomy> getTaxonomy(String name)
   {
      Session sess = getSession();
      try
      {
         Criteria c = sess.createCriteria(Taxonomy.class); 
         c.add(Restrictions.ilike("Name", name));
         c.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
         return c.list();
      }
      finally
      {
         releaseSession(sess);
      }
   } 
   
   public List<Integer> getTaxonomyIdForName(String name)
   {
    
      Session sess = getSession();
      try
      {
         Query query = sess.createQuery("select id from Taxonomy where name like :name");
         query.setString("name", name);
         @SuppressWarnings("unchecked")
         List<Integer> taxIds = query.list();
         return taxIds;
      }
      finally
      {
         releaseSession(sess);
      }
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
