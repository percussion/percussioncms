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

import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;


/**
 * The PSDataMapper class provides a way to map XML elements and attributes
 * to their corresponding back-end columns. JavaScript can also be used in
 * lieu of a back-end column. This allows an XML element or attribute to be
 * mapped to a dynamically computed value.
 * <p>
 * PSDataMapper is a collection of PSDataMapping objects, and as such
 * are derived from the PSCollection class. Simply use the methods defined
 * in PSCollection to gain access to the PSDataMapping objects defined for
 * the mapper.
 *
 * @see PSPipe#getDataMapper
 * @see PSDataMapping
 * @see com.percussion.util.PSCollection
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSDataMapper extends PSCollectionComponent
{
   /**
    * Construct a Java object from its XML representation. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @param      sourceNode      the XML element node to construct this
    *                              object from
    *
    * @param      parentDoc      the Java object which is the parent of this
    *                              object
    *
    * @param      parentComponents   the parent objects of this object
    *
    * @exception   PSUnknownNodeTypeException
    *                              if the XML element node is not of the
    *                              appropriate type
    */
   public PSDataMapper(org.w3c.dom.Element sourceNode,
      IPSDocument parentDoc, List parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructs an empty mapper.
    */
   public PSDataMapper()
   {
      super((new PSDataMapping()).getClass());
   }

   /**
    * Performs a shallow copy of the data in the supplied component to this
    * component. Derived classes should implement this method for their data,
    * calling the base class method first.
    *
    * @param mapper a valid PSDataMapper. 
    */
   public void copyFrom( PSDataMapper mapper )
   {
      m_returnEmptyXml = mapper.m_returnEmptyXml;
      copyFrom((PSCollectionComponent) mapper );
      // copy our state
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXDataMapper XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document is:
    * <pre><code>
    *    &lt;!--
    *       PSXDataMapper provides a way to map XML elements and attributes
    *       to their corresponding back-end columns. JavaScript can also be
    *       used in lieu of a back-end column. This allows an XML element or
    *       attribute to be mapped to a dynamically computed value.
    *       PSXDataMapper contains a collection of PSXDataMapping objects.
    *    --&gt;
    *    &lt;!ELEMENT PSXDataMapper (PSXDataMapping*)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXDataMapper XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement (ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));
      root.setAttribute("returnEmptyXml", m_returnEmptyXml ? "yes" : "no");

      int size = size();
      IPSComponent entry;
      for (int i=0; i < size; i++)
      {
         entry = (IPSComponent)get(i);
         root.appendChild(entry.toXml(doc));
      }

      return root;
   }

   /**
    * This method is called to populate a PSDataMapper Java object
    * from a PSXDataMapper XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @exception   PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXDataMapper
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc,
                        List parentComponents)
      throws PSUnknownNodeTypeException
   {
      parentComponents = updateParentList(parentComponents);
      int parentSize = parentComponents.size() - 1;

      try {
         if (sourceNode == null)
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

         if (!ms_NodeType.equals(sourceNode.getNodeName()))
         {
            Object[] args = { ms_NodeType, sourceNode.getNodeName() };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
         }

         PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

         String sTemp = tree.getElementData("returnEmptyXml");

         m_returnEmptyXml = (sTemp != null && sTemp.equals("yes"));

         sTemp = tree.getElementData("id");
         try {
            m_id = Integer.parseInt(sTemp);
         } catch (Exception e) {
            Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
         }

         // clear any mappings, then read in the new mappings
         clear();
         PSDataMapping mapping;
         String curNodeType = PSDataMapping.ms_NodeType;
         int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
         int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
         firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
         nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

         for (   Element curNode = tree.getNextElement(curNodeType, firstFlags);
               curNode != null;
               curNode = tree.getNextElement(curNodeType, nextFlags))
         {
            mapping = new PSDataMapping(
               (Element)tree.getCurrent(), parentDoc, parentComponents);
            add(mapping);
         }
      } finally {
         resetParentList(parentComponents, parentSize);
      }
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSValiditionException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt The validation context.
    *
    * @throws PSSystemValidationException According to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

    if(super.isEmpty())
    {
       cxt.validationError(this,IPSObjectStoreErrors.APP_MAPPER_EMPTY,null);

    }

      super.validate(cxt);
   }


   /** Does this mapper allow an empty xml document to be returned
    *  on a null result set?
    *
    * @return  <code>true</code> if it does or <code>false</code>
    *          if one row containing <code>null</code> columns is desired
    *          in the empty result set condition.
    */
   public boolean allowsEmptyDocReturn()
   {
      return m_returnEmptyXml;
   }

   /** Set whether or not this mapper allows an empty document to
    *   be returned when an empty result set is detected.
    *
    * @param   allow Use <code>true</code> to allow an empty document
    *          to be returned (1.1 behavior) or <code>false</code> to
    *          indicate that on an empty result set, to map one row
    *          containing all <code>null</code> columns.
    */
   public void setAllowEmptyDocReturn(boolean allow)
   {
      m_returnEmptyXml = allow;
   }

   /** Does this mapper allow empty xml to be returned?
       Defaults to <code>false</code>
    */
   private boolean m_returnEmptyXml = false;

   /* package access on this so they may reference each other in fromXml */
   static final String   ms_NodeType            = "PSXDataMapper";

   // NOTE: when adding members, be sure to update the copyFrom method

}
