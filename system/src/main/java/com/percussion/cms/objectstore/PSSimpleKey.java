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
