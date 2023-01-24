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

import com.percussion.taxonomy.domain.Related_node;
import com.percussion.taxonomy.repository.Related_nodeDAO;
import com.percussion.taxonomy.repository.Related_nodeServiceInf;
import com.percussion.taxonomy.service.Related_nodeService;

public class Related_nodeService implements Related_nodeServiceInf {

    public Related_nodeDAO related_nodeDAO;

    public Collection getAllRelated_nodes() {
        try {
            return related_nodeDAO.getAllRelated_nodes();
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public Related_node getRelated_node(int id) {
        try {
            return related_nodeDAO.getRelated_node(id);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void removeRelated_node(Related_node related_node) {
        try {
            related_nodeDAO.removeRelated_node(related_node);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void saveRelated_node(Related_node related_node) {
        try {
            related_nodeDAO.saveRelated_node(related_node);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void setRelated_nodeDAO(Related_nodeDAO related_nodeDAO) {
        this.related_nodeDAO = related_nodeDAO;
    }
}
