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

import com.percussion.taxonomy.domain.Attribute;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import java.util.Collection;

public class HibernateAttributeDAO extends HibernateDaoSupport implements AttributeDAO {

    public Collection getAttribute(int id) {
        String queryString = "from Attribute a left join fetch a.taxonomy left join fetch a.attribute_langs where a.id = " + id;
        return (Collection) getHibernateTemplate().execute(new HibernateQuery(queryString));
    }

    /**
     * Return all Attributes
     */
    public Collection getAllAttributes(int taxonomy_id, int langID) {
        String queryString = "from Attribute a left join fetch a.taxonomy left join fetch a.attribute_langs al join fetch al.language where a.taxonomy.id = " + taxonomy_id + " and al.language.id = "+langID;
        return (Collection) getHibernateTemplate().execute(new HibernateQuery(queryString));
    }

    /**
     * Return all Attribute names and IDs
     */
    public Collection getAttributeNames(int taxonomy_id, int language_id) {
        String queryString = "select al.Name, a.id from Attribute a, Attribute_lang al where al.attribute.id = a.id and a.taxonomy.id = " + taxonomy_id + " and al.language.id = " + language_id + " order by al.id";
        return (Collection) getHibernateTemplate().execute(new HibernateQuery(queryString));
    }

    public void saveAttribute(Attribute attribute) {
        getHibernateTemplate().saveOrUpdate(attribute);
    }

    public void removeAttribute(Attribute attribute) {
        getHibernateTemplate().delete(attribute);
    }
}
