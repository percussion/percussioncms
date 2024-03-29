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

package com.percussion.design.objectstore;

import com.percussion.error.PSDatabaseComponentException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Objects;

/**
 * The PSAttributeValue class is used to store an attribute value.
 *
 * @see PSAttribute
 */
public class PSAttributeValue extends PSDatabaseComponent
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode        the XML element node to construct this
    *                               object from, may not be <code>null</code>
    *
    * @param      parentDoc         the Java object which is the parent of this
    *                               object can be <code>null</code>
    *
    * @param      parentComponents  the parent objects of this object
    *                               can be <code>null</code>
    *
    * @throws  PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type
    */
   PSAttributeValue(Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      // Arraylist is used in this ctor because of the definition
      // of fromXml in IPSComponent
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructor for serialization, fromXml, etc.
    */
   PSAttributeValue()
   {
   }
   
   /**
    * Construct a value from the specified text.
    *
    * @param valueText  The text for this value, if <code>null</code>
    * the value will be set to an empty string.
    */
   PSAttributeValue(String valueText)
   {
      super();
      
      if (valueText == null)
         valueText = "";
         
      m_value = valueText;
   }
   
   /**
    * This method is called to create one or more Action XML elements
    * containing the data described in this object that is used to update
    * the database. The Elements are appended to the root of the passed in doc.
    * <p>
    * The structure of the XML element(s) appended to the document (using a
    * root element called 'root') is:
    * <pre><code>
    * &lt;!ELEMENT root (Action*)&gt;
    * &lt;!ELEMENT Action (PSXAttributeValue)
    * &lt;!ATTLIST Action
    * type (INSERT | UPDATE | DELETE | UNKNOWN) #REQUIRED
    * &gt;
    * &lt;!ELEMENT PSXAttributeValue (value, attributeId)&gt;
    * &lt;!ATTLIST PSXAttributeValue
    *    id CDATA #REQUIRED
    *    DbComponentId CDATA #REQUIRED
    * &gt;
    * &lt;!ELEMENT value (#PCDATA)&gt;
    * &lt;!ELEMENT attributeId (#PCDATA)&gt;
    * </code></pre>
    *
    * @see PSDatabaseComponent#toDatabaseXml
    */
   public void toDatabaseXml(Document doc,
      Element actionRoot, 
      PSRelation relationContext) throws PSDatabaseComponentException
   {
      if (doc == null || actionRoot == null || relationContext == null)
         throw new IllegalArgumentException("one or more params is null");

      // if we are new, generate a new id
      if (isInsert())
         createDBComponentId();

      // Add action element to root
      Element actionElement = getActionElement(doc, actionRoot);
      if (actionElement != null)
      {
         // just toXml ourselves to this root
         Element objEl = toXml(doc);
         actionElement.appendChild(objEl);

         // now add the attribute id of our parent
         //ph: this doesn't seem right given our relation model
         PSAttribute attr = new PSAttribute();
         PSXmlDocumentBuilder.addElement(doc, objEl, XML_ATTRIBUTE_ID_ELEMENT,
            relationContext.getValue(attr.getComponentType()));
      }
   }

   /**
    * Get the value text of this attribute value.
    *
    * @return The value's text represenation.  Never <code>null</code>.
    */
   String getValueText()
   {
      return m_value;
   }
   
   /* **************  IPSComponent Interface Implementation ************** */
   
   /**
    * This method is called to create a PSXAttributeValue XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *
    * &lt;!ELEMENT PSXAttributeValue (value)&gt;
    * &lt;!ATTLIST PSXAttributeValue
    *    id CDATA #REQUIRED
    *    DbComponentId CDATA #REQUIRED
    *    componentState CDATA #REQUIRED
    * &gt;
    * &lt;!ELEMENT value (#PCDATA)&gt;
    *         
    * </code></pre>
    *
    * @param doc The parent document.   May not be <code>null</code>.
    *
    * @return     the newly created PSXAttributeValue XML element node
    * 
    * @throws IllegalArgumentException if doc is <code>null</code>
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      //create PSXAttributeValue element and add type attribute
      Element root = doc.createElement(ms_NodeType);

      //add id attribute
      root.setAttribute("id", String.valueOf(m_id));

      // add our db state
      addComponentState(root);

      // add our value
      PSXmlDocumentBuilder.addElement(doc, root, XML_VALUE_ELEMENT, m_value);

      return root;
   }
   
   /**
    * This method is called to populate a PSAttributeValue Java object
    * from a PSXAttributeValue XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @throws   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXRole
    *
    * @see IPSComponent#fromXml(Element, IPSDocument, List) for the
    * interface description
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc, 
                       List parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
      {
         throw new PSUnknownNodeTypeException(
         IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);
      }

      //make sure we got the AttributeValue type node
      if (false == ms_NodeType.equals(sourceNode.getNodeName()))
      {
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

      String sTemp = tree.getElementData("id");
      try
      {
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e)
      {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      getComponentState(sourceNode);
      
      // get our value
      String value = tree.getElementData(XML_VALUE_ELEMENT);
      if (value == null)
      {
         Object[] args = { ms_NodeType, XML_VALUE_ELEMENT, "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }
      else
         m_value = value;
   }

   // see IPSComponent interface   
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      //no-op
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof PSAttributeValue)) return false;
      PSAttributeValue that = (PSAttributeValue) o;
      return Objects.equals(m_value, that.m_value);
   }

   @Override
   public int hashCode() {
      return Objects.hash(m_value);
   }

   /**
    * The value text.  Never <code>null</code>, may be empty.
    */
   private String m_value = "";

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType = "PSXAttributeValue";

   // private xml element and attribute constants
   private static final String XML_ATTRIBUTE_ID_ELEMENT = "attributeId";
   private static final String XML_VALUE_ELEMENT = "value";
}

