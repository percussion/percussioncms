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

package com.percussion.taxonomy.service;

import org.hibernate.HibernateException;
import java.util.Collection;
import org.springframework.dao.DataAccessException;

import com.percussion.taxonomy.domain.Attribute_lang;
import com.percussion.taxonomy.repository.Attribute_langDAO;
import com.percussion.taxonomy.repository.Attribute_langServiceInf;
import com.percussion.taxonomy.service.Attribute_langService;

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
