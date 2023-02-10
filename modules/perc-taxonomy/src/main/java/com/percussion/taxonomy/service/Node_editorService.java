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
