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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.rxverify.data;

import java.io.Serializable;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
      StringBuffer rval = new StringBuffer();
      
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
