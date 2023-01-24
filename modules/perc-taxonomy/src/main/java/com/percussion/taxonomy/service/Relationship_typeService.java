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

import com.percussion.taxonomy.domain.Relationship_type;
import com.percussion.taxonomy.repository.Relationship_typeDAO;
import com.percussion.taxonomy.repository.Relationship_typeServiceInf;
import com.percussion.taxonomy.service.Relationship_typeService;

public class Relationship_typeService implements Relationship_typeServiceInf {

    public Relationship_typeDAO relationship_typeDAO;

    public Collection getAllRelationship_types() {
        try {
            return relationship_typeDAO.getAllRelationship_types();
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public Relationship_type getRelationship_type(int id) {
        try {
            return relationship_typeDAO.getRelationship_type(id);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void removeRelationship_type(Relationship_type relationship_type) {
        try {
            relationship_typeDAO.removeRelationship_type(relationship_type);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void saveRelationship_type(Relationship_type relationship_type) {
        try {
            relationship_typeDAO.saveRelationship_type(relationship_type);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void setRelationship_typeDAO(Relationship_typeDAO relationship_typeDAO) {
        this.relationship_typeDAO = relationship_typeDAO;
    }
}
