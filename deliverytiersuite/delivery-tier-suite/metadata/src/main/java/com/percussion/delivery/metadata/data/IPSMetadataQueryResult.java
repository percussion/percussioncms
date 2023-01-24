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
package com.percussion.delivery.metadata.data;


import java.util.Date;
import java.util.Set;

/**
 * @author erikserating
 *
 */
public interface IPSMetadataQueryResult
{
   /**
    * Retrieves the data for the specified property as a string. This will
    * work for any data type.
    * @param key the property key that specifies the data to be returned, Cannot
    * be <code>null</code> or empty. The key is case insensitive.
    * @return the string representation of the data. May be <code>null</code> or
    * empty.
    */
   public String getString(String key);
   
   /**
    * Retrieves the data for the specified property as an Integer. This will
    * only work for Integer, Float and Long dataTypes. Float and Long will be converted
    * to Integers and truncation may occur. 
    * @param key the property key that specifies the data to be returned, Cannot
    * be <code>null</code> or empty.  The key is case insensitive.
    * @return the int representation of the data. 
    * @throws PSQueryResultException if the dataType is not a numeric
    * type.
    */
   public int getNumber(String key);
   
   /**
    * Retrieves the data for the specified property as a Date. This will
    * only work for Date dataTypes. 
    * @param key the property key that specifies the data to be returned, Cannot
    * be <code>null</code> or empty.  The key is case insensitive.
    * @return the date representation of the data. May be <code>null</code>. 
    * @throws PSQueryResultException if the dataType is not a date
    * type.
    */
   public Date getDate(String key); 
   
   /**
    * Retrieves the data for the specified property as a Double. This will
    * only work for Integer, Float, Double and Long dataTypes. Integer, Float and Long will be converted
    * to Double and truncation may occur. 
    * @param key the property key that specifies the data to be returned, Cannot
    * be <code>null</code> or empty. The key is case insensitive.
    * @return the Double representation of the data. 
    * @throws PSQueryResultException if the dataType is not a numeric
    * type.
    */
   public float getDouble(String key);
   
   /**
    * Retrieves the data for the specified property as a Float. This will
    * only work for Integer, Float and Long dataTypes. Integer and Long will be converted
    * to Float and truncation may occur. 
    * @param key the property key that specifies the data to be returned, Cannot
    * be <code>null</code> or empty. The key is case insensitive.
    * @return the Float representation of the data. 
    * @throws PSQueryResultException if the dataType is not a numeric
    * type.
    */
   public float getFloat(String key);
   
   /**
    * Retrieves the data for the specified property as a Long. This will
    * only work for Integer, Float and Date dataTypes. Float and Integer will be converted
    * to Long and truncation may occur. Dates will be returned in their Long date/time representation.
    * @param key the property key that specifies the data to be returned, Cannot
    * be <code>null</code> or empty. The key is case insensitive.
    * @return the Long representation of the data. 
    * @throws PSQueryResultException if the dataType is not a numeric or date
    * type.
    */
   public long getLong(String key);
   
   /**
    * Retrieves the data type of the data property specified.
    * @param key the field key that specifies the data to be returned, Cannot
    * be <code>null</code> or empty. The key is case insensitive.
    * @return the dataType enum value or <code>null</code> if the the specified
    * key does not exist.
    */
   public DataType getDataType(String key);
   
   /**
    * Indicates that the specified property has a value, in other words
    * it is not <code>null</code>.
    * @param key the field key that specifies the data to be returned, Cannot
    * be <code>null</code> or empty. The key is case insensitive.
    * @return <code>true</code> if the field value is not <code>null</code>.
    */
   public boolean hasValue(String key);
   
   /**
    * The set of keys for each property in the result.
    * @return the key set, never <code>null</code>, may be empty.
    */
   public Set<String> keySet();
   
   /**
    * The data type enumeration.
    */
   public enum DataType
   {
      DATE,
      FLOAT,
      DOUBLE,
      LONG,
      INT,
      STRING      
   }
}
