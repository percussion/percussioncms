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
package com.percussion.fastforward.managednav;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.util.PSPreparedStatement;
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
   private static final Logger log = LogManager.getLogger(PSNavComponentSummary.class);
   
   /**
    * Name of this component
    */
   private String m_name = "";
   
   /**
    * Content Type id 
    */
   private long m_typeid = 0; 
   
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
