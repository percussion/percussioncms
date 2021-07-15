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

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.percussion.taxonomy.domain.*;

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
