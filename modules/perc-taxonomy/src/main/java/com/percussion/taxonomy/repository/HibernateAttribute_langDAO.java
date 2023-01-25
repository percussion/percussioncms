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

import com.percussion.taxonomy.domain.Attribute_lang;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import java.util.Collection;

public class HibernateAttribute_langDAO extends HibernateDaoSupport implements Attribute_langDAO {

    public Attribute_lang getAttribute_lang(int id) {
        return (Attribute_lang) getHibernateTemplate().get(Attribute_lang.class, new Integer(id));
    }

    public Collection getAllAttribute_langs() {
        //Optional: Add order by to query
        return getHibernateTemplate().find("from Attribute_lang att");
    }

    public void saveAttribute_lang(Attribute_lang attribute_lang) {
        getHibernateTemplate().saveOrUpdate(attribute_lang);
    }

    public void removeAttribute_lang(Attribute_lang attribute_lang) {
        getHibernateTemplate().delete(attribute_lang);
    }
}
