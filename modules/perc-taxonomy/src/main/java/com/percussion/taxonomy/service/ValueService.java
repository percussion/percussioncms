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
