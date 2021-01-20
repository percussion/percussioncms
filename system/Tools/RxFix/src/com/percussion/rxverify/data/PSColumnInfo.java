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
package com.percussion.rxverify.data;

import com.percussion.xml.PSXmlTreeWalker;

import java.io.Serializable;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author dougrand
 * 
 * Store information from the tabledef file in a canonical format
 */
public class PSColumnInfo implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   /**
    * A typemap from the types in the tabledef file to canonical (Jdbc) types
    */
   static private Map<String,Integer> ms_typemap = new HashMap<String,Integer>();

   static
   {
      ms_typemap.put("BIT", new Integer(Types.BIT));
      ms_typemap.put("TINYINT", new Integer(Types.TINYINT));
      ms_typemap.put("SMALLINT", new Integer(Types.SMALLINT));
      ms_typemap.put("INTEGER", new Integer(Types.INTEGER));
      ms_typemap.put("BIGINT", new Integer(Types.BIGINT));
      ms_typemap.put("FLOAT", new Integer(Types.FLOAT));
      ms_typemap.put("REAL", new Integer(Types.REAL));
      ms_typemap.put("DOUBLE", new Integer(Types.DOUBLE));
      ms_typemap.put("NUMERIC", new Integer(Types.NUMERIC));
      ms_typemap.put("DECIMAL", new Integer(Types.DECIMAL));
      ms_typemap.put("CHAR", new Integer(Types.CHAR));
      ms_typemap.put("VARCHAR", new Integer(Types.VARCHAR));
      ms_typemap.put("LONGVARCHAR", new Integer(Types.LONGVARCHAR));
      ms_typemap.put("DATE", new Integer(Types.DATE));
      ms_typemap.put("TIME", new Integer(Types.TIME));
      ms_typemap.put("TIMESTAMP", new Integer(Types.TIMESTAMP));
      ms_typemap.put("BINARY", new Integer(Types.BINARY));
      ms_typemap.put("VARBINARY", new Integer(Types.VARBINARY));
      ms_typemap.put("LONGVARBINARY", new Integer(Types.LONGVARBINARY));
      ms_typemap.put("BLOB", new Integer(Types.BLOB));
      ms_typemap.put("CLOB", new Integer(Types.CLOB));
   }

   
   /**
    * Empty ctor for serialization
    */
   public PSColumnInfo() {
      m_size = 0;
   }

   /**
    * @param node Element to read information from. Obeys the tabledef.dtd
    */
   public void fromXml(Node node)
   {
      Element el = (Element) node;
      PSXmlTreeWalker walker = new PSXmlTreeWalker(el);

      m_name = el.getAttribute("name").toUpperCase();
      Node child;
      // Walk, not allowing parents
      while ((child = walker.getNext(false)) != null)
      {
         if (child instanceof Element)
         {
            Element cel = (Element) child;
            String tag = cel.getTagName();
            String val = walker.getElementData();
            if (tag.equalsIgnoreCase("jdbctype"))
            {
               m_type = val;
            }
            else if (tag.equalsIgnoreCase("allowsnull"))
            {
               m_nullable = val.equalsIgnoreCase("yes");
            }
            else if (tag.equalsIgnoreCase("size"))
            {
               if (val.trim().length() > 0)
               {
                  m_size = Integer.parseInt(val);
               }
            }
         }
      }

   }

   /**
    * @return Returns the column name.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * @return Returns <code>true</code> if the column is nullable.
    */
   public boolean isNullable()
   {
      return m_nullable;
   }

   /**
    * @return Returns the jdbc type.
    */
   public String getType()
   {
      return m_type;
   }
   
   /**
    * Return a translated type value
    * @return the jdbc type of this column
    */
   public int getJdbcType()
   {
      Integer type = (Integer) ms_typemap.get(m_type);
      
      if (type != null)
      {
         return type.intValue();
      }
      else
      {
         return Types.OTHER;
      }
   }

   /**
    * @return the size or zero when not appropriate for the given type
    */
   public int getSize()
   {
      return m_size;
   }
   
   /*
    * (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      StringBuffer rval = new StringBuffer();
      
      rval.append("Column ");
      rval.append(m_name);
      rval.append(" ");
      rval.append(m_type);
      if (isNullable())
      {
         rval.append(" NULL");
      }
      rval.append(";");
      
      return rval.toString();
   }

   /**
    * Name of the column
    */
   private String m_name;

   /**
    * <code>True</code> if this column can contain <code>null</code> values
    */
   private boolean m_nullable;

   /**
    * Size of column or zero when not appropriate.
    */
   private int m_size;

   /**
    * The value of the JDBC data type
    */
   private String m_type;

}
