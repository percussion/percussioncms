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

import com.percussion.taxonomy.domain.Relationship_type;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import java.util.Collection;

public class HibernateRelationship_typeDAO extends HibernateDaoSupport implements Relationship_typeDAO
{

   public Relationship_type getRelationship_type(int id)
   {
      return (Relationship_type) getHibernateTemplate().get(Relationship_type.class, new Integer(id));
   }

   public Collection getAllRelationship_types()
   {
      Session session = this.currentSession();

      // Optional: Add order by to query
      return session.createQuery("from Relationship_type rel").list();
   }

   public void saveRelationship_type(Relationship_type relationship_type)
   {
      Session session = this.currentSession();

         session.saveOrUpdate(relationship_type);


   }

   public void removeRelationship_type(Relationship_type relationship_type)
   {
      Session session = this.currentSession();

         session.delete(relationship_type);

   }
}
