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
