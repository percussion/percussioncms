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
