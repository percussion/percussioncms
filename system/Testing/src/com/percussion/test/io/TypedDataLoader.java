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
