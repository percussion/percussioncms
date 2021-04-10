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
import java.util.Collection;

import com.percussion.taxonomy.domain.*;

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
