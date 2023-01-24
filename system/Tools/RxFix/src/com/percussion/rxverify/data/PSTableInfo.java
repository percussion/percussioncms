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
package com.percussion.rxverify.data;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.Serializable;

/**
 * @author dougrand
 *
 * Store information from the table definition file in a canonical format.
 */
public class PSTableInfo implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   /**
    * Public empty ctor for serialization
    *
    */
   public PSTableInfo()
   {
      //
   }
   
   /**
    * Ctor and restore from element
    * @param e element, must never be <code>null</code>
    */
   public PSTableInfo(Element e) {
      if (e == null)
      {
         throw new IllegalArgumentException("e must never be null");
      }
      fromXml(e);
   }

   /**
    * @return Returns the table's columns.
    */
   public PSColumnInfo[] getColumns()
   {
      return m_columns;
   }
   /**
    * @return Returns the table name.
    */
   public String getName()
   {
      return m_name;
   }
 
   /*
    *  (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      StringBuilder rval = new StringBuilder();
      
      rval.append("Table ");
      rval.append(m_name);
      rval.append("\n");
      for(int i = 0; i < m_columns.length; i++)
      {
         rval.append(m_columns[i].toString());
         rval.append("\n");
      }
      rval.append(";");
      
      return rval.toString();
   }   
   /**
    * Restore information from an XML element. This assumes the tabledef.dtd
    * format
    * @param el XML element, never <code>null</code>
    */
   public void fromXml(Element el)
   {
      m_name = el.getAttribute("name").toUpperCase();
      NodeList columns = el.getElementsByTagName("column");
      int count = columns.getLength();
      m_columns = new PSColumnInfo[count];
      for(int i = 0; i < count; i++)
      {
         PSColumnInfo cinfo = new PSColumnInfo();
         cinfo.fromXml(columns.item(i));
         m_columns[i] = cinfo;
      }
   }
   
   /**
    * Table name, never <code>null</code> after construction
    */
   private String m_name;
   /**
    * Table columns
    */
   private PSColumnInfo m_columns[];
}
