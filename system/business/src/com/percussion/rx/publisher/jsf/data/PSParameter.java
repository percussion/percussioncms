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
package com.percussion.rx.publisher.jsf.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * Describe one attribute for the parameters. Used for the expander and
 * generator arguments.
 * 
 * @author dougrand
 * 
 */
public class PSParameter implements Comparable<PSParameter>
{
   /**
    * The name of the argument or parameter, never <code>null</code> or empty
    * after construction
    */
   private String m_name;

   /**
    * The description
    */
   private String m_description;

   /**
    * The value
    */
   private String m_value;

   /**
    * Ctor
    * 
    * @param n the name, never <code>null</code> or empty
    * @param d the description
    * @param v the value
    */
   public PSParameter(String n, String d, String v) {
      if (StringUtils.isBlank(n))
      {
         throw new IllegalArgumentException("n may not be null or empty");
      }
      m_name = n;
      m_description = d;
      m_value = v;
   }

   /**
    * @return Returns the description.
    */
   public String getDescription()
   {
      return m_description;
   }

   /**
    * @param description The description to set.
    */
   public void setDescription(String description)
   {
      m_description = description;
   }

   /**
    * @return Returns the name.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * @param name The name to set.
    */
   public void setName(String name)
   {
      m_name = name;
   }

   /**
    * @return Returns the value.
    */
   public String getValue()
   {
      return m_value;
   }

   /**
    * @param value The value to set.
    */
   public void setValue(String value)
   {
      m_value = value;
   }

   /**
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      StringBuilder rval = new StringBuilder();

      rval.append("<Item ");
      rval.append(m_name);
      rval.append(",");
      rval.append(m_value == null ? "null" : m_value.toString());
      rval.append(">");
      return rval.toString();
   }

   public int compareTo(PSParameter o)
   {
      return getName().compareTo(o.getName());
   }

}
