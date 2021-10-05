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

package com.percussion.cms.objectstore;

/**
 * This class uniquely identifies any complex child in an item. It does not
 * contain any parent information, so that must be supplied using the
 * PSLocator.
 * <p>This key is not used in the PSDbComponent scheme.
 *
 * @author paulhoward
 */
public class PSItemChildLocator extends PSKey
{
   /**
    * 
    * @param fieldName The submit name of the complex child. Never <code>null
    * </code> or empty.
    *  
    * @param rowId The numeric value that identifies the row in the child 
    * table that contains the data. Never <code>null</code> or empty.
    */
   public PSItemChildLocator(String fieldName, String rowId)
   {
      super(new String[] {CONTENTTYPE_PARTNAME, ROWID_PARTNAME}); 
      if (null == fieldName || fieldName.trim().length() == 0)
      {
         throw new IllegalArgumentException("fieldName cannot be null or empty");
      }
      if (null == rowId || rowId.trim().length() == 0)
      {
         throw new IllegalArgumentException("rowId cannot be null or empty");
      }
      setPart(CONTENTTYPE_PARTNAME, fieldName);
      setPart(ROWID_PARTNAME, rowId);
   }
   
   /**
    * Returns the fieldName supplied in the ctor. See ctor <code>fieldName
    * </code> parameter description for more details.
    * 
    * @return Never <code>null</code> or empty.
    */
   public String getChildContentType()
   {
      return getPart(CONTENTTYPE_PARTNAME);
   }
   
   /**
    * Returns the rowId supplied in the ctor. See ctor <code>rowId</code>
    * parameter description for more details.
    * 
    * @return Never <code>null</code> or empty.
    */
   public String getChildRowId()
   {
      return super.getPart(ROWID_PARTNAME);
   }
   
   /**
    * The key part name that contains the submit name of the complex child
    * field.
    */
   private static final String CONTENTTYPE_PARTNAME = "FIELDNAME";
   
   /**
    * The key part name that contains the numeric primary key for the child
    * table entry.
    */
   private static final String ROWID_PARTNAME = "ROWID";
}
