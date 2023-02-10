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
