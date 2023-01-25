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
 * This class is used for 1 part keys.
 *
 * @author Paul Howard
 * @version 1.0
 */
public class PSSimpleKey extends PSKey
{
   /**
    * Create an un-assigned key with a single part.
    *
    * @param keyName The name for the key, typically, the column name of the
    *    primary key of the associated table.
    */
   public PSSimpleKey( String keyName )
   {
      super( new String [] { keyName } );
   }

   /**
    * Create a persisted key with a single part.
    *
    * @param keyName The name for the key, typically, the column name of the
    *    primary key of the associated table.
    */
   public PSSimpleKey( String keyName, String value )
   {
      super( new String [] { keyName }, new String[] { value }, true );
   }

   /**
    * Create an assigned key with a single part. The persisted property is
    * dtermined by parameter <code>persisted</code>.
    *
    * @param keyName The name for the key, typically, the column name of the
    *    primary key of the associated table.
    *
    * @param value The value for the key. It may not be <code>null</code> or
    *    empty.
    *
    * @param persisted A flag to indicate whether this key represents a a
    *    component that exists in the db. Realize that this is a snapshot,
    *    so the component represented by this key could be removed while
    *    this key is instantiated.
    */
   public PSSimpleKey( String keyName, String value, boolean persisted )
   {
      super( new String [] { keyName }, new String[] { value }, persisted );
   }

   /**
    * Create a key with a single part.
    *
    * @param keyName The name for the key, typically, the column name of the
    *    primary key of the associated table.
    */
   public PSSimpleKey( String keyName, int value )
   {
      super( new String [] { keyName }, new int[] { value }, true );
   }


   /**
    * For reserializing.
    *
    * @param src Never <code>null</code>.
    */
   public PSSimpleKey(Element src)
      throws PSUnknownNodeTypeException
   {
      //super validates contract
      super(src);
   }

   /**
    * Get the value of the object.
    *
    * @return The value of the key, never <code>null</code> or empty.
    */
   public String getKeyValue()
   {
      return getPart( getDefinition()[0] );
   }

   /**
    * Get the value of the object in <code>int</code>.
    *
    * @return The value of the key.
    */
   public int getKeyValueAsInt()
   {
      return getPartAsInt(getDefinition()[0]);
   }

   /**
    * Get the key name of the object.
    *
    * @return The key name, never <code>null</code> or empty.
    */
   public String getKeyName()
   {
      return getDefinition()[0];
   }

}
