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

import java.util.Properties;
import java.io.OutputStream;
import java.io.IOException;

public class PropsLoader implements DataLoader
{
   public PropsLoader(Properties props, Object ob)
   {
      m_props = props;
   }

   public DataLoader getChildLoader(String name, Object ob)
   {
      return new TypedDataLoader(this, name, ob);
   }

   public long getLong(String name)
   {
      long ret = 0L;
      String prop = m_props.getProperty(name);
      if (prop != null)
         ret = Long.parseLong(prop);
      return ret;
   }

   public void setLong(String name, long val)
   {
      m_props.setProperty(name, "" + val);
   }

   public double getDouble(String name)
   {
      double ret = 0.0;
      String prop = m_props.getProperty(name);
      if (prop != null)
         ret = Double.parseDouble(prop);
      return ret;
   }

   public void setDouble(String name, double val)
   {
      m_props.setProperty(name, "" + val);
   }

   public String getString(String name)
   {
      String ret = "";
      String prop = m_props.getProperty(name);
      if (prop != null)
         ret = prop;
      return ret;
   }

   public void setString(String name, String val)
   {
      m_props.setProperty(name, val);
   }

   public void write(OutputStream stream) throws IOException
   {
      m_props.store(stream, null);
   }

   private Properties m_props;
}
