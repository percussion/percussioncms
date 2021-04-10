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
