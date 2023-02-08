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

package com.percussion.taxonomy.repository;

import com.percussion.taxonomy.domain.Node_editor;
import org.springframework.orm.hibernate5.support.HibernateDaoSupport;

import java.util.Collection;

public class HibernateNode_editorDAO extends HibernateDaoSupport implements Node_editorDAO {

    public Node_editor getNode_editor(int id) {
        return (Node_editor) getHibernateTemplate().get(Node_editor.class, new Integer(id));
    }

    public Collection getAllNode_editors() {
        //Optional: Add order by to query
        return getHibernateTemplate().find("from Node_editor nod");
    }

    public void saveNode_editor(Node_editor node_editor) {
        getHibernateTemplate().saveOrUpdate(node_editor);
    }

    public void removeNode_editor(Node_editor node_editor) {
        getHibernateTemplate().delete(node_editor);
    }
    
    public void removeNode_editors(Collection<Node_editor> node_editors) {
        getHibernateTemplate().deleteAll(node_editors);
    }
}
