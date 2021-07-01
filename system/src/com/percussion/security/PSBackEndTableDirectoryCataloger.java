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
package com.percussion.security;

import com.percussion.design.objectstore.PSAttribute;
import com.percussion.design.objectstore.PSAttributeList;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSGlobalSubject;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.design.objectstore.PSSubject;
import com.percussion.error.PSErrorManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A directory cataloger using a backend table security provider configuration
 * as a directory source.
 */
@SuppressWarnings(value={"unchecked"})
public class PSBackEndTableDirectoryCataloger extends PSDirectoryCataloger
{
   /**
    * Construct a backend directory cataloger for the supplied properties.
    *
    * @param properties the properties with connection and user attribute
    *    information, not <code>null</code> or empty. See
    *    {@link PSBackEndConnection} for mor information.
    */
   public PSBackEndTableDirectoryCataloger(Properties properties)
   {
      m_backendConnection = new PSBackEndConnection(properties);
   }
   
   /**
    * Calls {@link #PSBackEndTableDirectoryCataloger(Properties)}, ignores other 
    * param.
    */
   public PSBackEndTableDirectoryCataloger(Properties properties, 
      PSServerConfiguration config)
   {
      this(properties);
   }

   /** @see IPSDirectoryCataloger */
   public String getObjectAttributeName()
   {
      return m_backendConnection.getUserColumn();
   }

   // see IPSDirectoryCataloger interface 
   public String getCatalogerType()
   {
      return PSBackEndTableProvider.SP_NAME;
   }
   
   // see IPSDirectoryCataloger interface 
   public String getCatalogerDisplayType()
   {
      return PSBackEndTableProviderMetaData.SP_FULLNAME;
   }      
   
   /** @see IPSDirectoryCataloger */
   public String getAttribute(PSSubject user, String attributeName)
   {
      Collection attributeNames = new ArrayList();
      attributeNames.add(attributeName);

      PSSubject subject = getAttributes(user, attributeNames);
      
      PSAttribute attribute = 
         subject.getAttributes().getAttribute(attributeName);
      List values = null;
      if (attribute != null)
         values = attribute.getValues();

      return (values == null || values.isEmpty()) ? 
         null : values.get(0).toString();
   }
   
   /** @see IPSDirectoryCataloger */
   public PSSubject getAttributes(PSSubject user)
   {
      return getAttributes(user, null);
   }

   /** @see IPSDirectoryCataloger */
   public PSSubject getAttributes(PSSubject user, Collection attributeNames)
   {
      if (user == null)
         throw new IllegalArgumentException("user may not be null");
      
      // 1st clear all existing attributes from the supplied subject
      PSAttributeList attributes = user.getAttributes();
      while (attributes.size() > 0)
         attributes.removeElementAt(0);

      // 2nd prepare all requested attributes with null's
      Iterator attrNames = null;
      if (attributeNames == null || attributeNames.isEmpty())
         attrNames = m_backendConnection.getUserAttributeNames();
      else
         attrNames = attributeNames.iterator();
      while (attrNames.hasNext())
         attributes.setAttribute((String) attrNames.next(), null);

      Connection conn = null;
      PreparedStatement stmt = null;
      ResultSet result = null;
      try
      {
         conn = m_backendConnection.getDbConnection();
         stmt = m_backendConnection.getPreparedStatement(user.getName(), conn);
         result = stmt.executeQuery();

         if (result.next())
         {
            Iterator attrs = attributes.iterator();
            while (attrs.hasNext())
            {
               PSAttribute attr = (PSAttribute) attrs.next();

               String colName = m_backendConnection.getUserAttribute(
                  attr.getName());
               if (colName != null)
               {
                  List values = new ArrayList();
                  values.add(result.getString(colName));
                  attr.setValues(values);
               }
            }
         }

         return user;
      }
      catch (SQLException e)
      {
         throw new RuntimeException(PSErrorManager.createMessage(
            IPSSecurityErrors.BETABLE_DIRECTORY_CATALOGER_ERROR, e.toString()));
      }
      finally
      {
         if (result != null)
            try { result.close(); } catch (SQLException e) { /* noop */ }

         if (stmt != null)
            try { stmt.close(); } catch (SQLException e) { /* noop */ }

         if (conn != null)
            try { conn.close(); } catch (SQLException e) { /* noop */ }
      }
   }

   /** @see IPSDirectoryCataloger */
   public Collection findUsers(PSConditional[] criteria,
      Collection attributeNames)
   {
      Collection<PSSubject> subjects = new ArrayList<>();
      
      if (criteria == null || criteria.length == 0)
         criteria = new PSConditional[] {null};
      
      Connection conn = null;
      PreparedStatement stmt = null;
      try
      {
         if (attributeNames == null || attributeNames.isEmpty())
         {
            attributeNames = new ArrayList();
            Iterator attrs = m_backendConnection.getUserAttributeNames();
            while(attrs.hasNext())
               attributeNames.add(attrs.next());
         }
         
         conn = m_backendConnection.getDbConnection();
         
         for (int i = 0; i < criteria.length; i++)
         {
            stmt = m_backendConnection.getPreparedStatement(criteria[i],
               attributeNames, conn);
            if (stmt == null)
               continue;
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
            {
               String name = rs.getString(m_backendConnection.getUserColumn());
               PSAttributeList attList = new PSAttributeList();
               Iterator attrs = attributeNames.iterator();
               while (attrs.hasNext())
               {
                  String attr = (String) attrs.next();

                  List values = null;
                  String colName = m_backendConnection.getUserAttribute(attr);
                  if (colName != null)
                  {
                     values = new ArrayList();
                     values.add(rs.getString(colName));                  
                  }

                  attList.setAttribute(attr, values);
               }
               subjects.add(new PSGlobalSubject(name,
                  PSSubject.SUBJECT_TYPE_USER, attList));
            }
            rs.close();
            stmt.close();
            stmt = null;
         }
      }
      catch (Exception e)
      {
         // can't throw, so log it
         ms_log.error("Error cataloging backend table users", e);
      }
      finally
      {
         if (stmt != null)
         {
            try
            {
               stmt.close();
            }
            catch (SQLException e)
            {
            }
         }
         
         if (conn != null)
         {
            try
            {
               conn.close();
            }
            catch (SQLException e)
            {
            }
         }
      }
      
      return subjects;
   }
   
   /**
    * The backend connection used to do directory cataloging. Initialized in
    * constructor, never <code>null</code> or changed after that.
    */
   private PSBackEndConnection m_backendConnection = null;
   
    private static final Logger ms_log = LogManager.getLogger(
      PSBackEndTableDirectoryCataloger.class);
}
