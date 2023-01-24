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
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Encapsulates a list of <code>PSTransactionSummary</code> objects
 */
public class PSTransactionLogSummary  implements IPSDeployComponent
{

   /**
    * Constructing a default object.
    */
   public PSTransactionLogSummary()
   {
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSTransactionLogSummary(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Gets the list of transaction objects.
    *
    * @return an iterator over zero or more <code>PSTransactionSummary</code>
    * objects, it will never be <code>null</code>, but may be empty.
    */
   public Iterator getTransactions()
   {
      return m_transax.iterator();
   }

   /**
    * Adds a transaction to this object.
    *
    * @param tranx The transaction to be added, it may not be <code>null</code>
    *
    * @throws IllegalArgumentException If <code>tranx</code> is
    * <code>null</code>.
    */
   public void addTransaction(PSTransactionSummary tranx)
   {
      if ( tranx == null )
         throw new IllegalArgumentException("tranx may not be null");

      m_transax.add(tranx);
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXTransactionLogSummary PSXTransactionSummary*>
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc should not be null");

      Element root = doc.createElement(XML_NODE_NAME);

      Iterator list = m_transax.iterator();
      while (list.hasNext())
      {
         PSTransactionSummary tranx = (PSTransactionSummary) list.next();
         root.appendChild(tranx.toXml(doc));
      }

      return root;
   }

   /**
    * Restores this object's state from its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode should not be null");

      if (!XML_NODE_NAME.equals(sourceNode.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      m_transax.clear();

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element childEl = tree.getNextElement(PSTransactionSummary.XML_NODE_NAME,
         FIRST_FLAGS);
      while ( childEl != null )
      {
         PSTransactionSummary tranx = new PSTransactionSummary(childEl);
         m_transax.add(tranx);

         childEl = tree.getNextElement(PSTransactionSummary.XML_NODE_NAME,
            NEXT_FLAGS);
      }
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");

      if ((obj instanceof PSTransactionLogSummary))
         throw new IllegalArgumentException(
            "obj is not PSTransactionLogSummary");

      PSTransactionLogSummary obj2 = (PSTransactionLogSummary) obj;
      m_transax.clear();
      m_transax.addAll(obj2.m_transax);
   }

   // see IPSDeployComponent interface
   public int hashCode()
   {
      return m_transax.hashCode();
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = false;

      if ((obj instanceof PSTransactionLogSummary))
      {
         PSTransactionLogSummary obj2 = (PSTransactionLogSummary) obj;
         isEqual = m_transax.equals(obj2.m_transax);
      }
      return isEqual;

   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXTransactionLogSummary";

   /**
    * A list of <code>PSTransactionSummary</code> objects. It will never to
    * <code>null</code>, but may be empty.
    */
   private List m_transax = new ArrayList();

   /**
    * flags to walk to a child node of a XML tree
    */
   private static final int FIRST_FLAGS =
      PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
      PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

   /**
    * flags to walk to a sibling node of a XML tree
    */
   private static final int NEXT_FLAGS =
      PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
      PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

}
