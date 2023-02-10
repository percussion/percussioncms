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

package com.percussion.soln.jcr.data;

import java.io.Serializable;
import java.util.Calendar;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

public class ValueData implements Serializable {

    /**
     * Serial id.
     */
    private static final long serialVersionUID = -1441585584706585871L;
    
    Boolean booleanData;
    Calendar dateData;
    Double doubleData;
    Long longData;
    String stringData;
    /**
     * JCR property type.
     */
    int type;
    
    public ValueData() {
        // For general serializers that use the setters and getters.
        type = PropertyType.STRING;
        stringData = "";
    }
    
    public ValueData(boolean data) {
        booleanData = data;
        type = PropertyType.BOOLEAN;
    }

    public ValueData(String data) {
        stringData = data;
        type = PropertyType.STRING;
    }

    public ValueData(Calendar data) {
        dateData = data;
        type = PropertyType.DATE;
    }

    public ValueData(double data) {
        doubleData = data;
        type = PropertyType.DOUBLE;
    }

    public ValueData(long data) {
        longData = data;
        type = PropertyType.LONG;
    }
    
 
    
    public Boolean getBooleanData() {
        return booleanData;
    }
    
    public void setBooleanData(boolean booleanData) {
        this.booleanData = booleanData;
    }
    
    
    public Calendar getDateData() {
        return dateData;
    }
    
    public void setDateData(Calendar dateData) {
        this.dateData = dateData;
    }
    
    public Double getDoubleData() {
        return doubleData;
    }
    
    public void setDoubleData(double doubleData) {
        this.doubleData = doubleData;
    }
    
    public Long getLongData() {
        return longData;
    }
    
    public void setLongData(long longData) {
        this.longData = longData;
    }
    
    public String getStringData() {
        return stringData;
    }
    
    public void setStringData(String stringData) {
        this.stringData = stringData;
    }
    
    public int getType() {
        return type;
    }
    
    /**
     * Sets the type for this property.  See javax.jcr.PropertyType 
     * for a list of types.  Only STRING, DATE, LONG, DOUBLE, and BINARY are supported 
     * at this time. 
     * @param datatype The type to set. 
     * @throws RepositoryException 
     */
    public void setType(int datatype) throws RepositoryException
    {
       switch(datatype)
       {
          case PropertyType.STRING:
          case PropertyType.DATE:
          case PropertyType.LONG: 
          case PropertyType.DOUBLE:
          case PropertyType.BINARY:
          case PropertyType.BOOLEAN:
             this.type = datatype;  
             //log.debug("Setting type " + datatype); 
             break;
          default:
             throw new ValueFormatException("Unsupported Type " + datatype); 
       }
       
    }
    
    

}
