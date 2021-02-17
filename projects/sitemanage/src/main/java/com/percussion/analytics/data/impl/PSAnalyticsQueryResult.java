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
package com.percussion.analytics.data.impl;

import com.percussion.analytics.data.IPSAnalyticsQueryResult;
import com.percussion.analytics.error.PSAnalyticsQueryResultException;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

/**
 * @author erikserating
 *
 */
public class PSAnalyticsQueryResult implements IPSAnalyticsQueryResult
{
    
   public PSAnalyticsQueryResult()
   {
      
   }
   
   public PSAnalyticsQueryResult(Map<String, Object> vals)
   {
      putAll(vals);
   }
   
   /* (non-Javadoc)
    * @see com.percussion.analytics.data.IPSAnalyticsQueryResult#getDataType(java.lang.String)
    */
   public DataType getDataType(String key)
   {
      if(StringUtils.isBlank(key))
         throw new IllegalArgumentException("key cannot be null or empty.");
      return types.get(key);
   }

   /* (non-Javadoc)
    * @see com.percussion.analytics.data.IPSAnalyticsQueryResult#getDate(java.lang.String)
    */
   public Date getDate(String key)
   {
      if(StringUtils.isBlank(key))
         throw new IllegalArgumentException("key cannot be null or empty.");
      key = key.toLowerCase();
      DataType type = getDataType(key);
      if(!hasValue(key))
         return null;
      if(type == null)
         throw new PSAnalyticsQueryResultException("No data type defined for specified field.");
      if(type != DataType.DATE)
         throw new PSAnalyticsQueryResultException("Type cannot be converted to a Date");
      return (Date)values.get(key);
   }

   /* (non-Javadoc)
    * @see com.percussion.analytics.data.IPSAnalyticsQueryResult#getFloat(java.lang.String)
    */
   public float getFloat(String key)
   {
      if(StringUtils.isBlank(key))
         throw new IllegalArgumentException("key cannot be null or empty.");
      key = key.toLowerCase();
      DataType type = getDataType(key);
      if(!hasValue(key))
         return -1;
      if(type == null)
         throw new PSAnalyticsQueryResultException("No data type defined for specified field.");
      if(type == DataType.STRING || type == DataType.DATE)
         throw new PSAnalyticsQueryResultException("Type cannot be converted to a Float");
      if(type == DataType.INT)
         return ((Integer)values.get(key)).floatValue();
      if(type == DataType.LONG)
         return ((Long)values.get(key)).floatValue();
      return (Float)values.get(key);
   }

   /* (non-Javadoc)
    * @see com.percussion.analytics.data.IPSAnalyticsQueryResult#getInt(java.lang.String)
    */
   public int getInt(String key)
   {
      if(StringUtils.isBlank(key))
         throw new IllegalArgumentException("key cannot be null or empty.");
      key = key.toLowerCase();
      DataType type = getDataType(key);
      if(!hasValue(key))
         return -1;
      if(type == null)
         throw new PSAnalyticsQueryResultException("No data type defined for specified field.");
      if(type == DataType.STRING || type == DataType.DATE)
         throw new PSAnalyticsQueryResultException("Type cannot be converted to a Integer");
      if(type == DataType.FLOAT)
         return ((Float)values.get(key)).intValue();
      if(type == DataType.LONG)
         return ((Long)values.get(key)).intValue();
      return (Integer)values.get(key);
   }

   /* (non-Javadoc)
    * @see com.percussion.analytics.data.IPSAnalyticsQueryResult#getLong(java.lang.String)
    */
   public long getLong(String key)
   {
      if(StringUtils.isBlank(key))
         throw new IllegalArgumentException("key cannot be null or empty.");
      key = key.toLowerCase();
      DataType type = getDataType(key);
      if(!hasValue(key))
         return -1;
      if(type == null)
         throw new PSAnalyticsQueryResultException("No data type defined for specified field.");
      if(type == DataType.STRING)
         throw new PSAnalyticsQueryResultException("Type cannot be converted to a Long");
      if(type == DataType.DATE)
         return ((Date)values.get(key)).getTime(); 
      if(type == DataType.INT)
         return ((Integer)values.get(key)).longValue();
      if(type == DataType.FLOAT)
         return ((Float)values.get(key)).longValue();
      return (Long)values.get(key);
   }

   /* (non-Javadoc)
    * @see com.percussion.analytics.data.IPSAnalyticsQueryResult#getString(java.lang.String)
    */
   public String getString(String key)
   {
      if(StringUtils.isBlank(key))
         throw new IllegalArgumentException("key cannot be null or empty.");
      key = key.toLowerCase();
      DataType type = getDataType(key);
      if(values.get(key) == null)
         return null;
      if(type == DataType.DATE)
      {
         DateFormat df = DateFormat.getInstance();
         return df.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL).format((Date)values.get(key));
      }
      return values.get(key).toString();
   }
   
   /* (non-Javadoc)
    * @see com.percussion.analytics.data.IPSAnalyticsQueryResult#hasValue(java.lang.String)
    */
   public boolean hasValue(String key)
   {
      if(StringUtils.isBlank(key))
         throw new IllegalArgumentException("key cannot be null or empty.");
      return values.get(key.toLowerCase()) != null;
   }

   /* (non-Javadoc)
    * @see com.percussion.analytics.data.IPSAnalyticsQueryResult#keySet()
    */
   public Set<String> keySet()
   {
      return values.keySet();
   }
   
   /**
    * Put all items in the passed in map into the query result.
    * @param values map of key value pairs, cannot be <code>null</code>,
    * may be empty. 
    */
   public void putAll(Map<String, Object> vals)
   {
      if(vals == null)
         throw new IllegalArgumentException("values cannot be null or empty.");
      for(String key : vals.keySet())
      {
         put(key, vals.get(key));
      }
   }
   
   /**
    * Put an item in the result set.
    * @param key the field key that specifies the data to be returned, Cannot
    * be <code>null</code> or empty. The key is case insensitive.
    * @param value
    */
   public void put(String key, Object value)
   {
      if(StringUtils.isBlank(key))
         throw new IllegalArgumentException("key cannot be null or empty.");
      key = key.toLowerCase();
      if(value == null)
         throw new IllegalArgumentException("Value cannot be null.");
      if(value.getClass() == String.class)
      {
         values.put(key, value);
         types.put(key, DataType.STRING);
      }
      else if(value.getClass() == Date.class)
      {
         values.put(key, value);
         types.put(key, DataType.DATE);
      }
      else if(value.getClass() == Float.class)
      {
         values.put(key, value);
         types.put(key, DataType.FLOAT);
      }
      else if(value.getClass() == Integer.class)
      {
         values.put(key, value);
         types.put(key, DataType.INT);  
      }
      else if(value.getClass() == Long.class)
      {
         values.put(key, value);
         types.put(key, DataType.LONG);
      }
      else
      {
         throw new PSAnalyticsQueryResultException(
            "Class type is not supported.");
      }
   }   
   
   
   /**
    * Value map for this query result. Never <code>null</code>, may be empty.
    */
   private Map<String, Object> values = new java.util.HashMap<>();
   
   /**
    * Type map for this query result. Never <code>null</code>, may be empty.
    */
   private Map<String, DataType> types = 
      new java.util.HashMap<>();

}
