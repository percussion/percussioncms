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

package com.percussion.data.jdbc.sqlparser;

public class ASTColumnReference extends SimpleNode {
  public ASTColumnReference(int id) {
    super(id);
  }

  public ASTColumnReference(SQLParser p, int id) {
    super(p, id);
  }

  public void setTable(String table)
  {
     m_table = table;
  }

  public String getTable()
  {
     return m_table;
  }

  public void setColumn(String column)
  {
     m_column = column;
  }

  public String getColumn()
  {
     return m_column;
  }

  private String m_column = null;
  private String m_table = null;

  /** Accept the visitor. **/
  public Object jjtAccept(SQLParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
