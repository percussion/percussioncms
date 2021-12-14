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

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Element;

/**
 * This property class is used by the <code>PSFolder</code> class.
 * The <code>PSFolder</code> object represent an item of <code>Folder</code>
 * content type. Which contains a child table with "properties" as the child
 * name. The folder property represent one entry in the child table, or 
 * child entry.
 */
public class PSFolderProperty extends PSCmsProperty
{
   /**
    * Creates an empty object.
    */
   public PSFolderProperty(String name)
   {
      super(new PSSimpleKey(KEY_ID), name);
   }

   /**
    * Creates a new object with the given parameters. This object is not 
    * persisted.
    * 
    * @param name The name of the property, may not be <code>null</code> or
    *    empty.
    *    
    * @param value The value of the property, may be <code>null</code> or empty
    * 
    * @param desc The description of the property, may be <code>null</code> or
    *    empty.
    */
   public PSFolderProperty(String name, String value, String desc)
   {
      super(new PSSimpleKey(KEY_ID), name);
      super.setValue(value);
      super.setDescription(desc);
   }

   /**
    * Creates a persisted object with the given parameters.
    * 
    * @param id The id of this property, may not be less than <code>0</code>
    * 
    * @param name The name of the property, may not be <code>null</code> or
    *    empty.
    *    
    * @param value The value of the property, may be <code>null</code> or empty
    * 
    * @param desc The description of the property, may be <code>null</code> or
    *    empty.
    */
   public PSFolderProperty(int id, String name, String value, String desc)
   {
      super( new PSSimpleKey(KEY_ID, id), name, value, desc, 0);
   }
   
   /**
    * Just like the {@link PSCmsProperty#PSCmsProperty(Element)} except the key
    * part definition is <code>KEY_ID</code>.
    */
   public PSFolderProperty(Element src) throws PSUnknownNodeTypeException
   {
      super(new PSSimpleKey(KEY_ID), "dummy");
      super.fromXml(src);
   }

   /**
    * Override the key because this property uses different key part definition.
    * see {@link PSCmsProperty#createKey(Element)} more detail
    */
   @Override
   protected PSKey createKey(Element el)
      throws PSUnknownNodeTypeException
   {
      PSKey k = new PSSimpleKey(KEY_ID);
      k.fromXml(el);
      
      return k;
   }
      
   /**
    * Get the id of this object.
    *
    * @return <code>-1</code> if it is undefined; otherwise it is >=
    *    <code>0</code>
    */
   public int getId()
   {
      return getLocator().getPartAsInt(KEY_ID);
   }

   /**
    * The key part definition, which is also the column name of the properties
    * table, or the row id of the child entry.
    */
   public static final String KEY_ID = "SYSID";
}
