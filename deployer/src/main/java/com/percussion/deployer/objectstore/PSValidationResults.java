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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
 * Encapsulates a list of <code>PSValidationResult</code> objects, and a list
 * of absent ancestors (as <code>PSDependency</code>) objects.
 */
public class PSValidationResults  implements IPSDeployComponent
{
   /**
    * Constructing the default object.
    */
   public PSValidationResults()
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
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSValidationResults(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get a list of <code>PSValidationResult</code> objects.
    *
    * @return A list of <code>PSValidationResult</code> objects. It will never
    * be <code>null</code>, but may be empty.
    */
   public Iterator<PSValidationResult> getResults()
   {
      return m_validateResults.iterator();
   }

   /**
    * Get a result for the specified dependency.
    *
    * @param dep The dependency for which a result is to be returned, may not
    * be <code>null</code>.
    * 
    * @return The result, or <code>null</code> if no result has been added for
    * the specified dependency.
    * 
    * @throws IllegalArgumentException if <code>dep</code> is <code>null</code>.
    */
   public PSValidationResult getResult(PSDependency dep)
   {
      if (dep == null)
         throw new IllegalArgumentException("dep may not be null");
         
      PSValidationResult result = null;
      
      String depKey = dep.getKey();
      for (PSValidationResult test : m_validateResults)
      {
         if (test.getDependency().getKey().equals(depKey))
            result = test;
      }
      
      return result;
   }

   /**
    * Adds a <code>PSValidationResult</code> to this object.
    *
    * @param    result The object to be added, it may not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>result</code> is
    * <code>null</code>.
    */
   public void addResult(PSValidationResult result)
   {
      if (result == null)
         throw new IllegalArgumentException("result may not be null");

      m_validateResults.add(result);
   }

   /**
    * Determines if any result object is an error.
    *
    * @return <code>true</code> if any result object is an error;
    * <code>false</code> otherwise.
    */
   public boolean hasErrors()
   {
      for (PSValidationResult vResult : m_validateResults)
      {
         if ( vResult.isError() )
            return true;
      }
      return false;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXValidationResults
    *    (PSXValidationResult*)
    * >
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);

      // Add the PSXValidationResult elements
      for (PSValidationResult vr : m_validateResults)
      {
         Element vrEl = vr.toXml(doc);
         root.appendChild(vrEl);
      }

      return root;
   }

   /**
    * Restores this object's state from its XML representation.  See
    * {@link #toXml(Document)} for format of XML.  See
    * {@link IPSDeployComponent#fromXml(Element)} for more info on method
    * signature.
    *
    * @throws PSUnknownNodeTypeException if <code>sourceNode</code> is
    * malformed XML.
    */
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
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
      Element childEl = tree.getNextElement(FIRST_FLAGS);

      m_validateResults.clear();
      childEl = getValidateResults(childEl, tree);
   }

   /**
    * Get a list of <code>PSValiationResult</code> object from the given
    * parameters if the XML contains any <code>PSValiationResult</code>.
    *
    * @param childEl The current element in the <code>tree</code>, which will
    * be retrieved from. It may not <code>null</code>.
    * @param tree The XML tree, assuming it is not <code>null</code>. The Tree
    * will be left on a <code>null</code> element, or the next element that is
    * not a validastion result.
    *
    * @return The current element in the <code>tree</code>, after the
    * retrieving operation..
    *
    * @throws PSUnknownNodeTypeException if the XML is malformed.
    */
   private Element getValidateResults(Element childEl, PSXmlTreeWalker tree)
      throws PSUnknownNodeTypeException
   {
      while ( childEl != null &&
              childEl.getNodeName().equals(PSValidationResult.XML_NODE_NAME) )
      {
         m_validateResults.add(new PSValidationResult(childEl));
         childEl = tree.getNextElement(NEXT_FLAGS);
      }
      return childEl;
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");

      if ((obj instanceof PSValidationResults))
         throw new IllegalArgumentException(
            "obj is not be PSValidationResults");

      PSValidationResults obj2 = (PSValidationResults) obj;

      m_validateResults.clear();
      m_validateResults.addAll(obj2.m_validateResults);
   }

   // see IPSDeployComponent interface
   @Override
   public int hashCode()
   {
      return m_validateResults.hashCode();
   }

   // see IPSDeployComponent interface
   @Override
   public boolean equals(Object obj)
   {
      boolean isEqual = false;

      if ((obj instanceof PSValidationResults))
      {
         PSValidationResults obj2 = (PSValidationResults) obj;
         isEqual = m_validateResults.equals(obj2.m_validateResults);
      }
      return isEqual;
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXValidationResults";

   /**
    * A list of <code>PSValidationResult</code> objects. It will never be
    * <code>null</code>, but may be empty.
    */
   private List<PSValidationResult> m_validateResults = new ArrayList<PSValidationResult>();

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
