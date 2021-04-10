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

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.percussion.taxonomy.domain.*;

import java.util.Collection;

public class HibernateRelated_nodeDAO extends HibernateDaoSupport implements Related_nodeDAO {

    public Related_node getRelated_node(int id) {
        return (Related_node) getHibernateTemplate().get(Related_node.class, new Integer(id));
    }

    public Collection getAllRelated_nodes() {
        //Optional: Add order by to query
        String queryString = "from Related_node rn left join fetch rn.relationship";
        return (Collection) getHibernateTemplate().execute(new HibernateQuery(queryString));
    }

    public void saveRelated_node(Related_node related_node) {
        getHibernateTemplate().saveOrUpdate(related_node);
    }

    public void removeRelated_node(Related_node related_node) {
        getHibernateTemplate().delete(related_node);
    }
}
