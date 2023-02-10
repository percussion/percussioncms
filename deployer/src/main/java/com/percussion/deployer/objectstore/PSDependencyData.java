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


package com.percussion.deployer.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.tablefactory.PSJdbcDataTypeMap;
import com.percussion.tablefactory.PSJdbcTableData;
import com.percussion.tablefactory.PSJdbcTableFactoryException;
import com.percussion.tablefactory.PSJdbcTableSchema;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A container class to encapsulate a schema and a table data for a
 * dependency object.
 */
public class PSDependencyData implements IPSDeployComponent
{

   /**
    * Constructing the object with the given parameters.
    *
    * @param    sourceId The source ID, may not be <code>null</code> or empty.
    * @param    sourceName The name of the source, may not be <code>null</code>
    * or empty.
    * @param    objectType The type of the object, may not be <code>null</code>
    * or empty.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */

   /**
    * Constructing the object with the given parameters.
    *
    * @param schema The schema object, may not be <code>null</code>.
    * @param data The table data object, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException If any param is invalid.
    */
   public PSDependencyData(PSJdbcTableSchema schema, PSJdbcTableData data)
   {
      if (schema == null)
         throw new IllegalArgumentException("schema may not be null or empty");
      if (data == null)
         throw new IllegalArgumentException("data may not be null or empty");

      m_schema = schema;
      m_data= data;
   }


   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    * @param typeMap The data type mapping object, may not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSDependencyData(Element source, PSJdbcDataTypeMap typeMap)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");
      if (typeMap == null)
         throw new IllegalArgumentException("typeMap may not be null");

      fromXml(source, typeMap);
   }

   /**
    * Serializes this object's state to its XML representation.  Format is:
    *
    * <pre><code>
    *    &lt;!ELEMENT PSXDependencyData (table, table)>
    * </code>/<pre>
    *
    * Where the first <code>table</code> is the schema element,
    * the second <code>table</code> is the table data element.
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc should not be null");

      Element root = doc.createElement(XML_NODE_NAME);

      root.appendChild(m_schema.toXml(doc));
      root.appendChild(m_data.toXml(doc));

      return root;
   }

   /**
    * Gets the table data of this object.
    *
    * @return The table data object, will never be <code>null</code>.
    */
   public PSJdbcTableData getData()
   {
      return m_data;
   }

   /**
    * Gets the table schema of this object.
    *
    * @return The table schema object, will never be <code>null</code>.
    */
   public PSJdbcTableSchema getSchema()
   {
      return m_schema;
   }

   /**
    * Overriden the base method. This method is not supported.
    *
    * @throws UnsupportedOperationException if be called.
    */
   public void fromXml(Element sourceNode) throws UnsupportedOperationException
   {
      throw new UnsupportedOperationException("method not supported");
   }

   // See IPSDeployComponent interface
   public void fromXml(Element sourceNode, PSJdbcDataTypeMap typeMap)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      Element schemaEl = PSDeployComponentUtils.getNextRequiredElement(tree,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN,
         PSJdbcTableSchema.NODE_NAME);

      Element dataEl = PSDeployComponentUtils.getNextRequiredElement(tree,
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS,
         PSJdbcTableData.NODE_NAME);

      m_schema = null;
      try
      {
         m_schema = new PSJdbcTableSchema(schemaEl, typeMap);
         m_data = new PSJdbcTableData(dataEl);
      }
      catch (PSJdbcTableFactoryException e)
      {
         Object[] args;
         if (m_schema == null)
            args = new Object[] { XML_NODE_NAME, schemaEl.getNodeName() };
         else
            args = new Object[] { XML_NODE_NAME, dataEl.getNodeName() };

         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

   }

   // See IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj parameter should not be null");

      if (!(obj instanceof PSDependencyData))
         throw new IllegalArgumentException(
            "obj wrong type, expecting PSDependencyData");

      PSDependencyData obj2 = (PSDependencyData) obj;
      m_schema = obj2.m_schema;
      m_data = obj2.m_data;
   }

   // See IPSDeployComponent interface
   public int hashCode()
   {
      return m_schema.hashCode() + m_data.hashCode();
   }

   // See IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean result = false;

      if ((obj instanceof PSDependencyData))
      {
         PSDependencyData obj2 = (PSDependencyData) obj;
         result = m_schema.equals(obj2.m_schema) &&
                  m_data.equals(obj2.m_data);
      }

      return result;
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXDependencyData";

   /**
    * The scehma object, initialized by constructor, never be
    * <code>null</code> after that.
    */
   private PSJdbcTableSchema m_schema;

   /**
    * The table data, initialized by constructor never be
    * <code>null</code>.
    */
   private PSJdbcTableData m_data;

}
