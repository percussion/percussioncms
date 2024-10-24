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
package com.percussion.fastforward.managednav;

import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.PSPreparedStatement;
import com.percussion.utils.guid.IPSGuid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * A shortcut to the component summary via JDBC.  Loading the 
 * PSComponentSummary has proven too slow in repetitive operations. 
 * This component summary class has all of the fields used in the
 * Managed Nav package, but without the overhead of the Component
 * Proxy. 
 * 
 * @author DavidBenua
 *
 */
public class PSNavComponentSummary
{
   /**
    * Construct a Component Summary from a locator.
    * Convenience method for PSNavComponentSummary(int). 
    * @param loc
    * @throws PSNavException
    */
   public PSNavComponentSummary(PSLocator loc)
      throws PSNavException
   {
      this(loc.getId());
   }
   
   /**
    * Copy constructor from PSComponentSummary. This method copies the 
    * relevant values from the existing Component Summary.  
    * @param sum1 the Component Summary.
    */
   public PSNavComponentSummary(PSComponentSummary sum1)
   {
      contentTypeGuid = sum1.getContentTypeGUID();
      m_currentLoc = sum1.getCurrentLocator();
      m_name = sum1.getName();
      m_typeid = sum1.getContentTypeId(); 
   }
   
   /**
    * Construct a Component Summary from a content id. This method reads the 
    * information directly from the repository database 
    * @param id the Content Id. 
    * @throws PSNavException
    */
   public PSNavComponentSummary(int id)
      throws PSNavException
   {
      Connection conn = PSNavSQLUtils.connect(); 
      PreparedStatement stmt = null;
      ResultSet rs = null; 
      
      try 
      {
         stmt = PSPreparedStatement.getPreparedStatement(conn,
               getSqlStatement());
         stmt.setInt(1, id);
         rs = stmt.executeQuery();
         boolean valid = rs.next();
         if(valid)
         {            
            m_name = rs.getString(1); 
            int revision = rs.getInt(2);
            m_typeid = rs.getInt(3);
            m_currentLoc = new PSLocator(id,revision);
            contentTypeGuid = new PSGuid(PSTypeEnum.NODEDEF,m_typeid);
         }
         else
         {
            String errmsg = "No contentstatus record for contentid " + id; 
            log.error(errmsg);
            throw new PSNavException(errmsg); 
         }
         
      }catch (PSNavException ex2) 
      {
         throw (PSNavException)ex2.fillInStackTrace();          
      }
      catch (Exception ex)
      {
         log.error("SQL Exception ", ex);
         throw new PSNavException(ex);
      }
      finally
      {
         PSNavSQLUtils.closeout(conn, stmt, rs); 
      }
      
   }
   
   /**
    * Gets the current locator. 
    * @return Returns the currentLocator.
    */
   public PSLocator getCurrentLocator()
   {
      return m_currentLoc;
   }
   /**
    * Gets the name. 
    * @return Returns the name.
    */
   public String getName()
   {
      return m_name;
   }
   /**
    * Gets the content type id.  
    * @return Returns the typeid.
    */
   public long getContentTypeId()
   {
      return m_typeid;
   }
   
   /**
    * Get the query to execute to retrieve the component summary info.
    * 
    * @return The statement, never <code>null</code> or empty.
    */
   private String getSqlStatement()
   {
      String table = PSNavSQLUtils.qualifyTableName(SQL_STATEMENT_TABLE);
      
      return SQL_STATEMENT_START + table + SQL_STATEMENT_END;
   }
   
   /**
    * Logger for debugging purposes.
    */
   private static final Logger log = LogManager.getLogger(IPSConstants.NAVIGATION_LOG);
   
   /**
    * Name of this component
    */
   private String m_name = "";
   
   /**
    * Content Type id 
    */
   private long m_typeid = 0;

   public IPSGuid getContentTypeGuid() {
      return contentTypeGuid;
   }

   public void setContentTypeGuid(IPSGuid contentTypeGuid) {
      this.contentTypeGuid = contentTypeGuid;
   }

   /**
    * Content Type GUID
    */
   private IPSGuid contentTypeGuid = null;

   /**
    * Current contentid and revision. 
    */
   private PSLocator m_currentLoc = null; 

   /**
    * Starting portion of the summary sql query to execute, up to but not 
    * including the table name.
    */
   private static final String SQL_STATEMENT_START = "SELECT TITLE, " +
      "CURRENTREVISION, CONTENTTYPEID FROM ";
   
   /**
    * Table name to use in the summary sql query.
    */
   private static final String SQL_STATEMENT_TABLE = "CONTENTSTATUS";
   
   /**
    * End portion of the summary sql query, starting from but not including
    * the table name.
    */
   private static final String SQL_STATEMENT_END = " WHERE CONTENTID = ?";
}
