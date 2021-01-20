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
package com.percussion.test.io;
import java.io.IOException;
import java.io.OutputStream;

public class TypedDataLoader implements DataLoader
{
   public TypedDataLoader(DataLoader ldr, String name, Object ob)
   {
      m_name = name;
      // m_pfx = getShortName(ob) + "." + m_name + "-";
      m_pfx = m_name + ".";
      m_ldr = ldr;
   }

   public DataLoader getChildLoader(String name, Object ob)
   {
      return new TypedDataLoader(this, name, ob);
   }

   private static String getShortName(Object ob)
   {
      String shortName = ob.getClass().getName();
      int per = shortName.lastIndexOf('.');
      if (per > 0 && per < shortName.length() - 1)
         shortName = shortName.substring(per+1, shortName.length());

      if (shortName.endsWith(";"))
         shortName = shortName.substring(0, shortName.length() - 1);

      return shortName;
   }
   public long getLong(String name)
   {
      return m_ldr.getLong(m_pfx + name);
   }

   public void setLong(String name, long val)
   {
      m_ldr.setLong(m_pfx + name, val);
   }

   public double getDouble(String name)
   {
      return m_ldr.getDouble(m_pfx + name);
   }

   public void setDouble(String name, double val)
   {
      m_ldr.setDouble(m_pfx + name, val);
   }

   public String getString(String name)
   {
      return m_ldr.getString(m_pfx + name);
   }

   public void setString(String name, String val)
   {
      m_ldr.setString(m_pfx + name, val);
   }

   public void write(OutputStream stream) throws IOException
   {
      m_ldr.write(stream);
   }

   private DataLoader m_ldr;
   private String m_name;
   private String m_pfx;
}
