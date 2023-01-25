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

import com.percussion.taxonomy.domain.Language;
import com.percussion.taxonomy.repository.LanguageDAO;
import com.percussion.taxonomy.repository.LanguageServiceInf;
import com.percussion.taxonomy.service.LanguageService;

public class LanguageService implements LanguageServiceInf {

    public LanguageDAO languageDAO;

    public Collection getAllLanguages() {
        try {
            return languageDAO.getAllLanguages();
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public Language getLanguage(int id) {
        try {
            return languageDAO.getLanguage(id);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void removeLanguage(Language language) {
        try {
            languageDAO.removeLanguage(language);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void saveLanguage(Language language) {
        try {
            languageDAO.saveLanguage(language);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void setLanguageDAO(LanguageDAO languageDAO) {
        this.languageDAO = languageDAO;
    }
}
