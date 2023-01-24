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
