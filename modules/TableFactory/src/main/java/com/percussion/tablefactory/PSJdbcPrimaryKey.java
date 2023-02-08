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

package com.percussion.tablefactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Iterator;

/**
 * This class is used to represent a primary key in a table schema, and
 * the action to perform when that table schema is used to create or modify a
 * table.
 */
public class PSJdbcPrimaryKey extends PSJdbcKey
{
   /**
    * Convenience method for calling {@link #PSJdbcPrimaryKey(String,
    * Iterator, int)} with the pkName parameter as <code>null</code>.
    * See that method for parameter descriptions.
    */
   public PSJdbcPrimaryKey(Iterator<String> names, int action)
      throws PSJdbcTableFactoryException
   {
      this( null, names, action );
   }

   /**
    * Initializes a newly created <code>PSJdbcPrimaryKey</code> object, from
    * the specified parameters.
    *
    * @param pkName the name to use for this constraint's identifier, may be
    * <code>null</code> or empty to let the backend assign a identifier.
    * @param names An iterator over one or more column names as Strings to
    * include in the primary key.  May not be <code>null</code>.  May not
    * contain <code>null</code>, empty or duplicate names.
    * @param action One of the <code>PSJdbcTableComponent.ACTION_xxx</code>
    * constants.
    * @throws IllegalArgumentException if names is <code>null</code> or does not
    * contain at least one element or if action is not valid.
    * @throws PSJdbcTableFactoryException if names contains any <code>null
    * </code>, empty or duplicate column names, or if there are any other
    * errors.
    */
   public PSJdbcPrimaryKey(String pkName, Iterator<String> names, int action)
      throws PSJdbcTableFactoryException
   {
      super( pkName, action, names, CONTAINER_NAME );
   }

   /**
    * Creates this object from its Xml representation.  See {@link #fromXml(
    * Element) fromXml} for more information.
    *
    * @param sourceNode The element from which this object is to be constructed.
    *    Element must conform to the definition for the primarykey element in
    *    the tabledef.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the Xml definition contains
    *    any empty or duplicate column names, or if there are any other errors.
    */
   public PSJdbcPrimaryKey(Element sourceNode)
      throws PSJdbcTableFactoryException
   {
      super(sourceNode, NODE_NAME, CONTAINER_NAME);
   }

   /**
    * Serializes this object's state to Xml conforming with the tabledef.dtd.
    *
    * @param doc The document to use when creating elements.  May not be <code>
    *    null</code>.
    *
    * @return The element containing this object's state, never <code>
    *    null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      return super.toXml(doc, NODE_NAME);
   }

   /**
    * Restore this object from an Xml representation.
    *
    * @param sourceNode The element from which to get this object's state.
    *    Element must conform to the definition for the primarykey
    *    element in the tabledef.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the Xml definition contains
    *    any empty column names, or if there are any other errors.
    */
   public void fromXml(Element sourceNode) throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      super.fromXml(sourceNode, NODE_NAME);
   }

   /**
    * Overides base class method as name is not required for a primary key.
    *
    * @return <code>true</code> if name is required, <code>false</code> if not.
    */
   protected boolean isNameRequired()
   {
      return false;
   }

   /**
    * Compares this primary key to another object.
    * See {@link PSJdbcTableComponent#compare(Object, int)} for values returned
    * by this method.
    *
    * @param obj the object to compare, may be <code>null</code>
    * @param flags one or more <code>COMPARE_XXX</code> values OR'ed
    * together
    *
    * @return a code indicating the type of match/mismatch between this object
    * and the specified object <code>obj</code>
    */
   public int compare(Object obj, int flags)
   {
      int match = IS_GENERIC_MISMATCH;
      // dummy do...while loop to avoid many return statements
      do
      {
         if (!(obj instanceof PSJdbcPrimaryKey))
         {
            match = IS_CLASS_MISMATCH;
            break;
         }
         PSJdbcPrimaryKey other = (PSJdbcPrimaryKey)obj;
         return super.compare(other, flags);
      }
      while (false);
      return match;
   }

   /**
    * The name of this objects root Xml element.
    */
   public static String NODE_NAME = "primarykey";

   /**
    * name of this container for error messages.
    */
   public static String CONTAINER_NAME = "primary key";

}

