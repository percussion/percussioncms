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
