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

package com.percussion.utils.container.jetty;

import com.percussion.utils.container.IPSHibernateDialectConfig;

import java.util.HashMap;
import java.util.Map;

public class PSJettyDialectConfig implements IPSHibernateDialectConfig
{
   
   /**
    * Map of jdbc driver name to hibernate sql dialect, never <code>null</code>,
    * may be empty. Modified by calls to {@link #setDialects(Map)}.
    */
   private static volatile Map<String, String> m_sqlDialects = new HashMap<String, String>();
   // Refactor to pick up dialects from jetty
   static {
      m_sqlDialects.put("jtds:sqlserver", "org.hibernate.dialect.SQLServerDialect");
      m_sqlDialects.put("inetdae7", "org.hibernate.dialect.SQLServerDialect");
      m_sqlDialects.put("oracle:thin", "org.hibernate.dialect.DB2Dialect");
      m_sqlDialects.put("mysql", "org.hibernate.dialect.MySQLInnoDBDialect");
  }

   @Override
   public Map<String, String> getDialects()
   {
      return m_sqlDialects;
   }

   @Override
   public String getDialectClassName(String driverName)
   {
      return m_sqlDialects.get(driverName);
   }

   @Override
   public void setDialect(String driverName, String dialectClassName)
   {
      m_sqlDialects.put(driverName, dialectClassName);
   }

   @Override
   public void setDialects(Map<String, String> dialects)
   {
      m_sqlDialects = dialects;
   }

}
