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

import com.percussion.cms.PSCmsException;

import java.util.Arrays;

/**
 * Base class for all IPSFieldValue.  Implements some of the methods that
 * don't vary across all or many of the components that will be implemented in
 * the CMS layer.  Derived classes should override the abstract methods.
 */
public abstract class PSFieldValue implements IPSFieldValue
{

   /** @see IPSFieldValue */
   public abstract Object getValue();

   /** @see IPSFieldValue */
   public abstract String getValueAsString() throws PSCmsException;

   // see IPSFieldValue#clone() interface for description
   public Object clone()
   {
      PSFieldValue copy = null;
      try
      {
         copy = (PSFieldValue) super.clone();
      }
      catch (CloneNotSupportedException e) {} // cannot happen
      return copy;
   }

   /**
    * Convenience method to build the hash of the object, just checks for
    * <code>null</code> and if objecttoHash is, it ignores and returns 0.
    *
    * @param objectToHash may be <code>null</code>.
    * @return the hashCode of objectToHash
    */
   protected int hashBuilder(Object objectToHash)
   {
      int theHash = 0;
      if(objectToHash != null)
         theHash = objectToHash.hashCode();

      return theHash;
   }

   /** @see IPSFieldValue */
   public abstract boolean equals(Object obj);

   /** @see IPSFieldValue */
   public abstract int hashCode();

   /**
    * Compares objects that implement the <code>equals()</code> method.
    * <code>String</code>s will be compared with case ignored.
    * Are they equal?
    *
    * @return <code>true</code>if they are, otherwise <code>false</code>.
    */
   protected boolean compare(Object a, Object b)
   {
        if(a == null || b == null)
        {
            if(a != null || b != null)
                return false;
        } else
        {
            if(a.getClass().isArray() && b.getClass().isArray())
                return Arrays.equals((Object[])a, (Object[])b);
            if(a instanceof String && b instanceof String)
               return ((String)a).equalsIgnoreCase((String)b);
            if(!a.equals(b))
                return false;
        }
        return true;
    }
}
