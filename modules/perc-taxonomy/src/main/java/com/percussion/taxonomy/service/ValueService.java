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
import java.util.Map;

import com.percussion.taxonomy.domain.Attribute;
import com.percussion.taxonomy.domain.Value;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.repository.ValueDAO;
import com.percussion.taxonomy.repository.ValueServiceInf;
import com.percussion.taxonomy.service.ValueService;

public class ValueService implements ValueServiceInf {

    ///////////////////////////////////////////////////////////////////////////////
   
    public ValueDAO valueDAO;

    ///////////////////////////////////////////////////////////////////////////////

    public Collection<Value> getAllValues() {
        Collection<Value> values = null;
        try {
            values = this.valueDAO.getAllValues();
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
        return values;
    }

    public Value getValue(int id) {
        Value value = null; 
        try {
            value = this.valueDAO.getValue(id);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
        return value;
    }

    ///////////////////////////////////////////////////////////////////////////////

    public void removeValue(Value value) {
        try {
            this.valueDAO.removeValue(value);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    public void saveValue(Value value) {
        try {
            this.valueDAO.saveValue(value);
        } catch (HibernateException e) {
            throw new HibernateException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////

    @Override
    public Map<String, String> saveValuesFromParams(Map<String, String[]> params,
                                                    Collection<Attribute> attributes,
                                                    Node node, 
                                                    int langID, 
                                                    String user_name) {
       Map<String, String> results = null; 
       try {
            results = this.valueDAO.saveValuesFromParams(params, attributes, node, langID, user_name);
       } catch (HibernateException e) {
            throw new HibernateException(e);
       }
       return results;
    }

    ///////////////////////////////////////////////////////////////////////////////

    public void setValueDAO(ValueDAO valueDAO) {
        this.valueDAO = valueDAO;
    }
    
    ///////////////////////////////////////////////////////////////////////////////

}
