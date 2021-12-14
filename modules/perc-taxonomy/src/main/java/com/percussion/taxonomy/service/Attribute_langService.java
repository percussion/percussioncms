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

package com.percussion.taxonomy.service;

import com.percussion.taxonomy.domain.Attribute_lang;
import com.percussion.taxonomy.repository.Attribute_langDAO;
import com.percussion.taxonomy.repository.Attribute_langServiceInf;
import org.hibernate.HibernateException;

import java.util.Collection;

public class Attribute_langService implements Attribute_langServiceInf {

    public Attribute_langDAO attribute_langDAO;

    public Collection getAllAttribute_langs() {
        try {
            return attribute_langDAO.getAllAttribute_langs();
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public Attribute_lang getAttribute_lang(int id) {
        try {
            return attribute_langDAO.getAttribute_lang(id);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void removeAttribute_lang(Attribute_lang attribute_lang) {
        try {
            attribute_langDAO.removeAttribute_lang(attribute_lang);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void saveAttribute_lang(Attribute_lang attribute_lang) {
        try {
            attribute_langDAO.saveAttribute_lang(attribute_lang);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void setAttribute_langDAO(Attribute_langDAO attribute_langDAO) {
        this.attribute_langDAO = attribute_langDAO;
    }
}
