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
 * The PSExtensionCallSet class represents a group of PSExtensionCall objects.
 * Extension calls are executed to provide external processing at various
 * points in the request process.
 *
 * @see         PSExtensionCall
 *
 * @author      Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSExtensionCallSet extends PSCollectionComponent
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
   public PSExtensionCallSet(Element sourceNode, IPSDocument parentDoc, 
                             List parentComponents)
      throws PSUnknownNodeTypeException
   {
      this();
      fromXml(sourceNode, parentDoc, parentComponents);
   }

   /**
    * Constructs an empty <code>PSExtensionCallSet</code>.
    */
   public PSExtensionCallSet()
   {
      super( PSExtensionCall.class );
   }


   /**
    * @return a deep-copy clone of this vector.
    */
   public synchronized Object clone()
   {
      PSExtensionCallSet copy = new PSExtensionCallSet();

      copy.copyFrom(this);

      return copy;
   }


   /* **************  IPSComponent Interface Implementation ************** */

   /**
    * This method is called to create a PSXExtensionCallSet XML element
    * node containing the data described in this object.
    * <p>
    * The structure of the XML document:
    * <pre><code>
    *    &lt;!--
    *       PSXExtensionCallSet represents a group of PSExtensionCall objects.
    *       Extension calls are executed to provide external processing at various
    *       points in the request process.
    *    --&gt;
    *    &lt;!ELEMENT PSXExtensionCallSet   (PSExtensionCall*)&gt;
    * </code></pre>
    *
    * @return     the newly created PSXExtensionCallSet XML element node
    */
   public Element toXml(Document doc)
   {
      Element   root = doc.createElement(ms_NodeType);
      root.setAttribute("id", String.valueOf(m_id));

      int size = size();
      for (int i=0; i < size; i++)
      {
         IPSComponent exit = (IPSComponent)get(i);
         root.appendChild(exit.toXml(doc));
      }

      return root;
   }

   /**
    * This method is called to populate a PSExtensionCallSet Java object
    * from a PSXExtensionCallSet XML element node. See the
    * {@link #toXml(Document) toXml} method for a description of the XML object.
    *
    * @throws PSUnknownNodeTypeException if the XML element node is not
    *                                        of type PSXExtensionCallSet
    */
   public void fromXml(Element sourceNode, IPSDocument parentDoc, 
                       List parentComponents)
      throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, ms_NodeType);

      if (false == ms_NodeType.equals (sourceNode.getNodeName()))
      {
         Object[] args = { ms_NodeType, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      PSXmlTreeWalker   tree = new PSXmlTreeWalker(sourceNode);

      String sTemp = tree.getElementData("id");
      try {
         m_id = Integer.parseInt(sTemp);
      } catch (Exception e) {
         Object[] args = { ms_NodeType, ((sTemp == null) ? "null" : sTemp) };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ID, args);
      }

      // get all the exit calls
      String curNodeType = PSExtensionCall.ms_NodeType;
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
      firstFlags |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      nextFlags  |= PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      for (   Element curNode = tree.getNextElement(curNodeType, firstFlags);
            curNode != null;
            curNode = tree.getNextElement(curNodeType, nextFlags))
      {
         PSExtensionCall exit = new PSExtensionCall(
            (Element)tree.getCurrent(), parentDoc, parentComponents);
         add(exit);
      }
   }

   /**
    * Validates this object within the given validation context. The method
    * signature declares that it throws PSSystemValidationException, but the
    * implementation must not directly throw any exceptions. Instead, it
    * should register any errors with the validation context, which will
    * decide whether to throw the exception (in which case the implementation
    * of <CODE>validate</CODE> should not catch it unless it is to be
    * rethrown).
    *
    * @param   cxt the validation context.
    *
    * @throws PSSystemValidationException according to the implementation of the
    * validation context (on warnings and/or errors).
    */
   public void validate(IPSValidationContext cxt) throws PSSystemValidationException
   {
      if (!cxt.startValidation(this, null))
         return;

      cxt.pushParent(this);
      try
      {
         super.validate(cxt);
      }
      finally
      {
         cxt.popParent();
      }
   }

   /** Name of the root element of this object's XML representation */
   public static final String ms_NodeType = "PSXExtensionCallSet";
}

