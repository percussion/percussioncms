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

import com.percussion.taxonomy.domain.Node_status;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import java.util.Collection;

public class HibernateNode_statusDAO extends HibernateDaoSupport implements Node_statusDAO {

    public Node_status getNode_status(int id) {
        return (Node_status) getHibernateTemplate().get(Node_status.class, new Integer(id));
    }

    public Collection getAllNode_statuss() {
        //Optional: Add order by to query
        return getHibernateTemplate().find("from Node_status nod");
    }

    public void saveNode_status(Node_status node_status) {
        getHibernateTemplate().saveOrUpdate(node_status);
    }

    public void removeNode_status(Node_status node_status) {
        getHibernateTemplate().delete(node_status);
    }
}
