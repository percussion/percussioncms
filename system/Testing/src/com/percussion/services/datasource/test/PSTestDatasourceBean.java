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
package com.percussion.services.datasource.test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.sql.SQLException;

/**
 * Simple bean used to test session factories.
 */
public class PSTestDatasourceBean
{
   /**
    * Session facotry to use, <code>null</code> until set by 
    * {@link #setSessionFactory(SessionFactory)}.
    */
   private SessionFactory m_sessionFactory;
   
   /**
    * Setter for the session to use.
    * 
    * @param sessionFactory The
    */
   public void setSessionFactory(SessionFactory sessionFactory)
   {
      if (sessionFactory == null)
         throw new IllegalArgumentException("sessionFactory may not be null");
      
      m_sessionFactory = sessionFactory;
   }
   
   /**
    * Opens a session, gets the jdbc connection and returns a string containing
    * the jdbc url and catalog represented.
    * 
    * @return The jdbc connection info of the session's connection, never
    * <code>null</code> or empty.
    * 
    * @throws SQLException if there are any errors using the connection.
    */
   public String testFactory() throws SQLException
   {
      if (m_sessionFactory == null)
         throw new IllegalStateException("factory is null");
      
      Session sess = m_sessionFactory.openSession();
      try
      {
         return sess.doReturningWork(conn -> conn.getMetaData().getURL() + " - " + conn.getCatalog());
      }
      finally
      {
         sess.close();
      }
   }
}

