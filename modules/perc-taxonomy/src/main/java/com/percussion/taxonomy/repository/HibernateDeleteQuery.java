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

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate5.HibernateCallback;

/**
 * @author Steffen Gates May 6, 2011
 */
public class HibernateDeleteQuery implements HibernateCallback {
    private String query;

    public HibernateDeleteQuery(String query) {
        this.query = query;
    }

    public Object doInHibernate(Session session) throws HibernateException {
        Query q = session.createQuery(query);
        q.executeUpdate();
        return null;
    }
}
