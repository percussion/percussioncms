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

import com.percussion.taxonomy.domain.Visibility;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import java.util.Collection;

public class HibernateVisibilityDAO extends HibernateDaoSupport implements VisibilityDAO {

    public Visibility getVisibility(int id) {
        return (Visibility) getHibernateTemplate().get(Visibility.class, new Integer(id));
    }

    public Collection getAllVisibilities() {
        //Optional: Add order by to query
        return getHibernateTemplate().find("from Visibility v");
    }

    public Collection getAllVisibilitiesForTaxonomyId(int taxonomy_id) {
        //Optional: Add order by to query
        return getHibernateTemplate().find("from Visibility v where v.taxonomy.id = " + taxonomy_id);
    }   
    
    public void saveVisibility(Visibility Visibility) {
        getHibernateTemplate().saveOrUpdate(Visibility);
    }

    public void removeVisibility(Visibility Visibility) {
        getHibernateTemplate().delete(Visibility);
    }
    
    public void removeVisibilities(Collection<Visibility> Visibilities) {
        getHibernateTemplate().deleteAll(Visibilities);
    }
}
