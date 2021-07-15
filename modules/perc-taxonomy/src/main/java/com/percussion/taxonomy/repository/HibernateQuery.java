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

import org.springframework.orm.hibernate3.HibernateCallback;

import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.HibernateException;

import java.util.HashMap;

/**
 * You can pass in a simple Query, or you can pass one in with substitutions defined, and
 * then supply a map of substitution values to be inserted on execute
 */
public class HibernateQuery implements HibernateCallback {

    private String query;
    private HashMap<String, String> substitutions;

    public HibernateQuery(String query) {
        this.query = query;
        this.substitutions = null;
    }

    public HibernateQuery(String query, HashMap<String, String> substitutions) {
        this.query = query;
        this.substitutions = substitutions;
    }

    public Object doInHibernate(Session session) throws HibernateException {
        Query q = session.createQuery(query);
        if (substitutions != null) {
            for (String name : substitutions.keySet()) {
                q.setText(name, substitutions.get(name));
            }
        }
        return q.list();
    }
}
