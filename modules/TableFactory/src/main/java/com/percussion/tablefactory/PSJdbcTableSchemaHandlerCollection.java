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

import com.percussion.util.PSCollection;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is a container for a list of <code>PSJdbcTableSchemaHandler</code>
 * objects, enabling them to be serialized as a collection to and from Xml.
 */
public class PSJdbcTableSchemaHandlerCollection extends PSCollection
{
   /**
    * Constructor
    */
   public PSJdbcTableSchemaHandlerCollection()
   {
      super(PSJdbcTableSchemaHandler.class);
   }

   /**
    * Creates this object from its Xml representation.  See {@link #fromXml(
    * Element) fromXml} for more information.
    *
    * @param sourceNode The element from which to get this object's state,
    * may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public PSJdbcTableSchemaHandlerCollection(Element sourceNode)
      throws PSJdbcTableFactoryException
   {
      this();
      fromXml(sourceNode);
   }

   /**
    * Restore this object from an Xml representation conforming with the
    * tabledef.dtd.
    *
    * @param sourceNode The element from which to get this object's state.
    * May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>sourceNode</code> is
    * <code>null</code>.
    * @throws PSJdbcTableFactoryException if there are any errors.
    */
   public void fromXml(Element sourceNode)
      throws PSJdbcTableFactoryException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!sourceNode.getNodeName().equals(NODE_NAME))
      {
         Object[] args = {NODE_NAME, sourceNode.getNodeName()};
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      clear();

      PSXmlTreeWalker walker = new PSXmlTreeWalker(sourceNode);
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      Element schemaHandler = walker.getNextElement(
         PSJdbcTableSchemaHandler.NODE_NAME, firstFlags);

      if (schemaHandler == null)
      {
         throw new PSJdbcTableFactoryException(
            IPSTableFactoryErrors.XML_ELEMENT_NULL,
            PSJdbcTableSchemaHandler.NODE_NAME);
      }

      while (schemaHandler != null)
      {
         add(new PSJdbcTableSchemaHandler(schemaHandler));
         schemaHandler = walker.getNextElement(
            PSJdbcTableSchemaHandler.NODE_NAME, nextFlags);
      }
   }

   /**
    * Serializes this object's state to Xml conforming with the tabledef.dtd.
    *
    * @param doc The document to use when creating elements.  May not be <code>
    * null</code>.
    *
    * @return The element containing this object's state, never <code>
    * null</code>.
    *
    * @throws IllegalArgumentException if doc is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // create the root element
      Element   root = doc.createElement(NODE_NAME);
      for (Object o : this) {
         PSJdbcTableSchemaHandler schemaHandler = (PSJdbcTableSchemaHandler) o;
         root.appendChild(schemaHandler.toXml(doc));
      }
      return root;
   }

   /**
    * Returns a table schema handler object of the specified type if it exists
    * in this collection, otherwise returns <code>null</code>.
    *
    * @param schemaHandlerType The type of the table schema handler to locate.
    *
    * @return a table schema handler object of the specified type if it exists
    * in this collection, otherwise returns <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>schemaHandlerType</code>
    * is not one of the following values:
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_NO_ALTER_TABLE_STMT</code>
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_TO_BACKUP</code>
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_FROM_BACKUP</code>
    * <code>PSJdbcTableSchemaHandler.TYPE_INT_NO_ALTER_TABLE_STMT</code>
    */
   public PSJdbcTableSchemaHandler getTableSchemaHandler(int schemaHandlerType)
   {
      PSJdbcTableSchemaHandler.checkValidSchemaHandlerType(schemaHandlerType);
      PSJdbcTableSchemaHandler schemaHandler = null;
      for (Object o : this) {
         schemaHandler = (PSJdbcTableSchemaHandler) o;
         if (schemaHandler.getType() == schemaHandlerType)
            return schemaHandler;
      }
      return null;
   }

   /**
    * The name of this objects root Xml element.
    */
   public static String NODE_NAME = "schemaHandlers";
}

