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
