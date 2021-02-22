/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
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
package com.percussion.analytics.data;

import java.util.Date;
import java.util.Set;

/**
 * Represents a "row" of analytics data returned from the analytics provider.
 * @author erikserating
 *
 */
public interface IPSAnalyticsQueryResult
{
   /**
    * Retrieves the data for the specified field as a string. This will
    * work for any data type.
    * @param key the field key that specifies the data to be returned, Cannot
    * be <code>null</code> or empty. The key is case insensitive.
    * @return the string representation of the data. May be <code>null</code> or
    * empty.
    */
   public String getString(String key);

    /**
     * Retrieves the data for the specified field as an Integer. This will only
     * work for Integer, Float and Long dataTypes. Float and Long will be
     * converted to Integers and truncation may occur.
     * 
     * @param key the field key that specifies the data to be returned, Cannot
     * be <code>null</code> or empty. The key is case insensitive.
     * @return the int representation of the data.
     * @throws com.percussion.analytics.error.PSAnalyticsQueryResultException if
     * the dataType is not a numeric type.
     */
   public int getInt(String key);

    /**
     * Retrieves the data for the specified field as a Date. This will only work
     * for Date dataTypes.
     * 
     * @param key the field key that specifies the data to be returned, Cannot
     * be <code>null</code> or empty. The key is case insensitive.
     * @return the date representation of the data. May be <code>null</code>.
     * @throws com.percussion.analytics.error.PSAnalyticsQueryResultException if
     * the dataType is not a date type.
     */
   public Date getDate(String key);

    /**
     * Retrieves the data for the specified field as a Float. This will only
     * work for Integer, Float and Long dataTypes. Integer and Long will be
     * converted to Float and truncation may occur.
     * 
     * @param key the field key that specifies the data to be returned, Cannot
     * be <code>null</code> or empty. The key is case insensitive.
     * @return the Float representation of the data.
     * @throws com.percussion.analytics.error.PSAnalyticsQueryResultException if
     * the dataType is not a numeric type.
     */
   public float getFloat(String key);

    /**
     * Retrieves the data for the specified field as a Long. This will only work
     * for Integer, Float and Date dataTypes. Float and Integer will be
     * converted to Long and truncation may occur. Dates will be returned in
     * their Long date/time representation.
     * 
     * @param key the field key that specifies the data to be returned, Cannot
     * be <code>null</code> or empty. The key is case insensitive.
     * @return the Long representation of the data.
     * @throws com.percussion.analytics.error.PSAnalyticsQueryResultException if
     * the dataType is not a numeric or date type.
     */
   public long getLong(String key);
   
   /**
    * Retrieves the data type of the data field specified.
    * @param key the field key that specifies the data to be returned, Cannot
    * be <code>null</code> or empty. The key is case insensitive.
    * @return the dataType enum value or <code>null</code> if the the specified
    * key does not exist.
    */
   public DataType getDataType(String key);
   
   /**
    * Indicates that the specified field has a value, in other words
    * it is not <code>null</code>.
    * @param key the field key that specifies the data to be returned, Cannot
    * be <code>null</code> or empty. The key is case insensitive.
    * @return <code>true</code> if the field value is not <code>null</code>.
    */
   public boolean hasValue(String key);
   
   /**
    * The set of keys for each data field in the result.
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
      LONG,
      INT,
      STRING      
   }
}
