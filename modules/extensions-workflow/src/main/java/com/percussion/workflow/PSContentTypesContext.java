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
package com.percussion.workflow;

import com.percussion.extension.IPSExtensionErrors;
import com.percussion.util.PSPreparedStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PSContentTypesContext implements IPSContentTypesContext
{
  private boolean invokedStandalone = false;

  private int contentTypeID = 0;
  private PreparedStatement statement = null;
  private Connection connection = null;
  private ResultSet rs = null;

  String m_sContentTypeName = "";
  String m_sContentTypeDescrition = "";
  String m_sContentTypeNewRequest = "";
  String m_sContentTypeQueryRequest = "";
  String m_sContentTypeUpdateRequest = "";

  public PSContentTypesContext(Connection connection, int contentTypeID)
     throws SQLException, PSEntryNotFoundException
  {
    this.contentTypeID = contentTypeID;
    try
    {
      statement =
         PSPreparedStatement.getPreparedStatement(connection, QRYSTRING);
      statement.clearParameters();
      statement.setInt(1, contentTypeID);
      rs = statement.executeQuery();

      if(false == rs.next())
      {
        close();
        throw new PSEntryNotFoundException(IPSExtensionErrors.NO_RECORDS);
      }

    m_sContentTypeName = rs.getString("CONTENTTYPENAME");
    m_sContentTypeDescrition = rs.getString("CONTENTTYPEDESC");
    m_sContentTypeNewRequest = rs.getString("CONTENTTYPENEWREQUEST");
    m_sContentTypeQueryRequest = rs.getString("CONTENTTYPEQUERYREQUEST");
    m_sContentTypeUpdateRequest = rs.getString("CONTENTTYPEUPDATEREQUEST");

    }
    catch(SQLException e)
    {
      close();
      throw e;
    }
  }

   public String getContentTypeQueryRequest() throws SQLException
  {
    return m_sContentTypeQueryRequest;
  }

   public String getContentTypeUpdateRequest() throws SQLException
  {
    return m_sContentTypeUpdateRequest;
  }

   public String getContentTypeNewRequest() throws SQLException
  {
    return m_sContentTypeNewRequest;
  }

   public String getContentTypeName() throws SQLException
  {
    return m_sContentTypeName;
  }

   public String getContentTypeDescription() throws SQLException
  {
    return m_sContentTypeDescrition;
  }

   public void close()
  {
    //release resouces
    try
    {
      if(null != connection && false == connection.getAutoCommit())
        connection.setAutoCommit(true);
    }
    catch(SQLException e)
    {
    }
    try
    {
      if(null!=rs)
        rs.close();
      rs = null;

      if(null!=statement)
        statement.close();
      statement = null;
    }
    catch(SQLException e)
    {
    }
  }
  
  /**
   * static constant string that represents the qualified table name.
   */
  static private String TABLE_CTC =
     PSConnectionMgr.getQualifiedIdentifier("CONTENTTYPES");

  private static final String QRYSTRING = 
   "SELECT " + 
   TABLE_CTC + ".CONTENTTYPENAME," + 
   TABLE_CTC + ".CONTENTTYPEDESC," +
   TABLE_CTC + ".CONTENTTYPENEWREQUEST," +
   TABLE_CTC + ".CONTENTTYPEQUERYREQUEST," +
   TABLE_CTC + ".CONTENTTYPEUPDATEREQUEST " +
   "FROM " +
   TABLE_CTC +
   " WHERE (" +
   TABLE_CTC + ".CONTENTTYPEID=?)";

}

