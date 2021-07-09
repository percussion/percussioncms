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

import com.percussion.taxonomy.domain.Node_editor;
import com.percussion.taxonomy.repository.Node_editorDAO;
import com.percussion.taxonomy.repository.Node_editorServiceInf;
import com.percussion.taxonomy.service.Node_editorService;

public class Node_editorService implements Node_editorServiceInf {

    public Node_editorDAO node_editorDAO;

    public Collection getAllNode_editors() {
        try {
            return node_editorDAO.getAllNode_editors();
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public Node_editor getNode_editor(int id) {
        try {
            return node_editorDAO.getNode_editor(id);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void removeNode_editor(Node_editor node_editor) {
        try {
            node_editorDAO.removeNode_editor(node_editor);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void removeNode_editors(Collection<Node_editor> node_editors) {
        try {
            node_editorDAO.removeNode_editors(node_editors);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }
        
    public void saveNode_editor(Node_editor node_editor) {
        try {
            node_editorDAO.saveNode_editor(node_editor);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void setNode_editorDAO(Node_editorDAO node_editorDAO) {
        this.node_editorDAO = node_editorDAO;
    }
}
