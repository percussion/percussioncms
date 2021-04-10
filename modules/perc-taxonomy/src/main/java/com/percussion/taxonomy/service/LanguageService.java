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
