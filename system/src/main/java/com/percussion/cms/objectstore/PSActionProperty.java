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

public class PSActionProperty
   extends PSCmsProperty
{
   /**
    * no-args constructor
    */
   
   public PSActionProperty()
   {
   }
   
   /**
    * Convenience method that calls {@link #PSActionProperty(String,
    * String,String) PSActionProperty(name, value, null)}.
    */
   public PSActionProperty(String name, String value)
   {
      this(name, value, null);
   }


   /**
    *
    * @param name Never <code>null</code> or empty.
    *
    * @param value May be <code>null</code>. If so, "" is used.
    *
    * @param desc May be <code>null</code>. If so, "" is used.
    */
   public PSActionProperty(String name, String value, String desc)
   {
      super(getKeyDef(), name, value, desc, KEYASSIGN_NAME_AS_KEYPART);
   }


   /**
    * Create an object from a previously serialized one.
    *
    * @param src Never <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException
    */
   public PSActionProperty(Element src)
      throws PSUnknownNodeTypeException
   {
      super(getKeyDef(), "dummy");
      fromXml(src);
   }


   /**
    * Creates a key containing the proper definition for this object.
    *
    * @return Never <code>null</code>.
    */
   private static PSKey getKeyDef()
   {
      return new PSKey(new String [] {"PROPNAME", "ACTIONID"}, false);
   }

   //see base class for description
   protected String[] getKeyPartValues(IPSKeyGenerator gen)
   {
      return new String[] {getName()};
   }
}
