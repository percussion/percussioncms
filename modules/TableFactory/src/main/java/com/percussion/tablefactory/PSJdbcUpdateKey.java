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
 * This class is used to represent a update key in a table schema. Specifies
 * columns to use as keys when updating data instead of using the primary key
 * columns.
 */
public class PSJdbcUpdateKey extends PSJdbcKey
{
   /**
    * Basic constructor for this class.
    *
    * @param names An iterator over one or more column names as Strings to
    *    include in the update key.  May not be <code>null</code>.  May not
    *    contain <code>null</code>, empty or duplicate names.
    *
    * @throws IllegalArgumentException if names is <code>null</code> or does not
    *    contain at least one element.
    * @throws PSJdbcTableFactoryException if names contains any <code>null
    * </code>, empty or duplicate column names, or if there are any other
    * errors.
    */
   public PSJdbcUpdateKey(Iterator<String> names)
      throws PSJdbcTableFactoryException
   {
      super(null, PSJdbcTableComponent.ACTION_CREATE, names, CONTAINER_NAME);
   }


   /**
    * Creates this object from its Xml representation.  See {@link #fromXml(
    * Element) fromXml} for more information.
    *
    * @param sourceNode The element from which this object is to be constructed.
    *    Element must conform to the definition for the updatekey element in
    *    the tabledef.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the Xml definition contains
    *    any empty or duplicate column names, or if there are any other errors.
    */
   public PSJdbcUpdateKey(Element sourceNode)
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
    *    Element must conform to the definition for the updatekey
    *    element in the tabledef.dtd.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if sourceNode is <code>null</code>.
    * @throws PSJdbcTableFactoryException if the Xml definition contains
    *    any empty or duplicate column names, or if there are any other errors.
    */
   public void fromXml(Element sourceNode) throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      super.fromXml(sourceNode, NODE_NAME);
   }

   /**
    * Overrides base class method as name is not required for a primary key.
    *
    * @return <code>true</code> if name is required, <code>false</code> if not.
    */
   protected boolean isNameRequired()
   {
      return false;
   }

   /**
    * The name of this objects root Xml element.
    */
   public static String NODE_NAME = "updatekey";

   /**
    * name of this container for error messages.
    */
   protected static String CONTAINER_NAME = "update key";
}

