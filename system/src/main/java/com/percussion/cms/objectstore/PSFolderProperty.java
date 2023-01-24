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
