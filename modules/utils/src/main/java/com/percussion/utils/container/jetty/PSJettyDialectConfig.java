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
