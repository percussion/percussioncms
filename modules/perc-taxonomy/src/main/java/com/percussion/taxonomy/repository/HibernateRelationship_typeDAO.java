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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.taxonomy.repository;

import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.percussion.taxonomy.domain.*;

import java.util.Collection;

public class HibernateRelationship_typeDAO extends HibernateDaoSupport implements Relationship_typeDAO
{

   public Relationship_type getRelationship_type(int id)
   {
      return (Relationship_type) getHibernateTemplate().get(Relationship_type.class, new Integer(id));
   }

   public Collection getAllRelationship_types()
   {
      Session session = getSession();
      try
      {
         // Optional: Add order by to query
         return session.createQuery("from Relationship_type rel").list();
      }
      finally
      {
         releaseSession(session);
      }
   }

   public void saveRelationship_type(Relationship_type relationship_type)
   {
      Session session = getSession();
      try
      {
         session.saveOrUpdate(relationship_type);
      }
      finally
      {
         releaseSession(session);
      }
   }

   public void removeRelationship_type(Relationship_type relationship_type)
   {
      Session session = getSession();
      try
      {
         session.delete(relationship_type);
      }
      finally
      {
         releaseSession(session);
      }
   }
}
