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
 * This class encapsulates a single value for the PSActionVisibilityContext
 * multivalued property. Both the property name and value are used to assign
 * the key, so they are immutable.
 *
 * @author Paul Howard
 * @version 1.0
 */
public class PSVisibilityContextEntry extends PSCmsProperty
{
   /**
    * no-args constructor
    */
   
   public PSVisibilityContextEntry()
   {
   }
   
   /**
    * Convenience method that calls {@link #PSVisibilityContextEntry(String,
    * String,String) PSVisibilityContextEntry(name, value, null)}.
    */
   public PSVisibilityContextEntry(String name, String value)
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
   public PSVisibilityContextEntry(String name, String value, String desc)
   {
      super(getKeyDef(), name, value, desc, KEYASSIGN_ALL);
   }


   public PSVisibilityContextEntry(Element src)
      throws PSUnknownNodeTypeException
   {
      super(getKeyDef(), "dummy");
      fromXml(src);
   }


   private static PSKey getKeyDef()
   {
      return new PSKey(
            new String [] {"VISIBILITYCONTEXT", "VALUE", "ACTIONID"}, false);
   }


   //see base class for description
   protected String[] getKeyPartValues(IPSKeyGenerator gen)
   {
      return new String[] {getName(), getValue()};
   }


   /**
    * Because the value is used in key assignment, it cannot be reset.
    *
    * @param value Unused
    *
    * @throws UnsupportedOperationException Always.
    */
   public void setValue(String value)
   {
      throw new UnsupportedOperationException(
            "The value is immutable in this class.");
   }
}
