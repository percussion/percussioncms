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

import com.percussion.taxonomy.domain.Related_node;
import org.hibernate.query.Query;
import org.springframework.orm.hibernate5.HibernateCallback;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import java.util.Collection;

public class HibernateRelated_nodeDAO extends HibernateDaoSupport implements Related_nodeDAO {

    public Related_node getRelated_node(int id) {
        return (Related_node) getHibernateTemplate().get(Related_node.class, new Integer(id));
    }

    public Collection getAllRelated_nodes() {
        //Optional: Add order by to query
        String queryString = "from Related_node rn left join fetch rn.relationship";
        return (Collection) getHibernateTemplate().execute((HibernateCallback) session -> {
            Query query = session.createQuery(queryString);
            return query.list();
        });

    }

    public void saveRelated_node(Related_node related_node) {
        getHibernateTemplate().saveOrUpdate(related_node);
    }

    public void removeRelated_node(Related_node related_node) {
        getHibernateTemplate().delete(related_node);
    }
}
