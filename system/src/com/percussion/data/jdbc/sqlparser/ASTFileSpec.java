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

public class ASTFileSpec extends SimpleNode {
   public ASTFileSpec(int id) {
      super(id);
   }
   
   public ASTFileSpec(SQLParser p, int id) {
      super(p, id);
   }
   
   
   /** Accept the visitor. **/
   public Object jjtAccept(SQLParserVisitor visitor, Object data) {
      return visitor.visit(this, data);
   }
   
   public void setValue(String filePath)
   {
      // remove double quotes from beginning and end
      filePath = filePath.substring(1, filePath.length() - 1);
      if (filePath.endsWith("*"))
      {
         m_isRecursive = true;
         // remove * from end
         filePath = filePath.substring(0, filePath.length() - 1);
      }
      m_filePath = filePath;
   }
   
   public String getValue() { return m_filePath; }
   
   public boolean isRecursive() { return m_isRecursive; }
   
   private String m_filePath;
   private boolean m_isRecursive = false;
}
