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

