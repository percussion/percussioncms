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

import com.percussion.taxonomy.domain.Node_status;
import com.percussion.taxonomy.repository.Node_statusDAO;
import com.percussion.taxonomy.repository.Node_statusServiceInf;
import com.percussion.taxonomy.service.Node_statusService;

public class Node_statusService implements Node_statusServiceInf {

    public Node_statusDAO node_statusDAO;

    public Collection getAllNode_statuss() {
        try {
            return node_statusDAO.getAllNode_statuss();
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public Node_status getNode_status(int id) {
        try {
            return node_statusDAO.getNode_status(id);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void removeNode_status(Node_status node_status) {
        try {
            node_statusDAO.removeNode_status(node_status);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void saveNode_status(Node_status node_status) {
        try {
            node_statusDAO.saveNode_status(node_status);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void setNode_statusDAO(Node_statusDAO node_statusDAO) {
        this.node_statusDAO = node_statusDAO;
    }
}
