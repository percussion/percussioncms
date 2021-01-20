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



/**
 * Encapsulates log detail information.
 */
public class PSLogDetail implements IPSDeployComponent
{

   /**
    * Constructing the object from the given paramaters.
    *
    * @param    validationResults The validation results object, which may not
    * be <code>null</code>.
    * @param    idMap The IdMap object, it may be <code>null</code>.
    * @param    dbmsMap The DBMS-Map object, it may be <code>null</code>.
    * @param    txnLog The transaction log summary object, which may not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException If any parameter is invalid.
    */
   public PSLogDetail(PSValidationResults validationResults, PSIdMap idMap,
      PSDbmsMap dbmsMap, PSTransactionLogSummary txnLog)
   {
      if ( validationResults == null )
         throw new IllegalArgumentException(
            "validationResults parameter should not be null");
      if ( txnLog == null )
         throw new IllegalArgumentException(
            "txnLog parameter should not be null");

      m_validationResults = validationResults;
      m_idMap = idMap;
      m_dbmsMap = dbmsMap;
      m_txnLog = txnLog;
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
   public PSLogDetail(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the validation results object.
    *
    * @return The validation results object, which will never be
    * <code>null</code>.
    */
   public PSValidationResults getValidationResults()
   {
      return m_validationResults;
   }

   /**
    * Get the IdMap object.
    *
    * @return The IdMap object, it may be <code>null</code>.
    */
   public PSIdMap getIdMap()
   {
      return m_idMap;
   }

   /**
    * Get the DBMS-Map object.
    *
    * @return The DBMS-Map object, it may be <code>null</code>.
    */
   public PSDbmsMap getDbmsMap()
   {
      return m_dbmsMap;
   }

   /**
    * Get the transaction log summary object.
    *
    * @return The transaction log summary object, which will never be
    * <code>null</code>.
    */
   public PSTransactionLogSummary getTransactionLog()
   {
      return m_txnLog;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXLogDetail (PSXDbmsMap,
    *     PSXTransactionLogSummary, PSXValidationResults, PSXIdMap?)>
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.appendChild(m_dbmsMap.toXml(doc));
      root.appendChild(m_txnLog.toXml(doc));
      root.appendChild(m_validationResults.toXml(doc));
      if ( m_idMap != null )
         root.appendChild(m_idMap.toXml(doc));

      return root;
   }

   // see IPSDeployComponent interface
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

      Element childEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      checkNullXmlELement(childEl, PSDbmsMap.XML_NODE_NAME);
      m_dbmsMap = new PSDbmsMap(childEl);

      childEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      checkNullXmlELement(childEl, PSTransactionLogSummary.XML_NODE_NAME);
      m_txnLog = new PSTransactionLogSummary(childEl);

      childEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      checkNullXmlELement(childEl, PSValidationResults.XML_NODE_NAME);
      m_validationResults = new PSValidationResults(childEl);

      childEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      if ( childEl != null )
         m_idMap = new PSIdMap(childEl);
   }

   /**
    * Validating an XML element, make sure it is not <code>null</code>
    *
    * @param element The XML element, may be <code>null</code>.
    * @param nodeName The name of the expected XML node, may not be
    * <code>null</code> or empty
    *
    * @throws PSUnknownNodeTypeException if the <code>element</code> is
    * <code>null</code>.
    */
   private void checkNullXmlELement(Element element, String nodeName)
      throws PSUnknownNodeTypeException
   {
      if ( element == null )
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, nodeName);
      }
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj parameter should not be null");

      if (!(obj instanceof PSLogDetail))
         throw new IllegalArgumentException(
            "obj wrong type, expecting PSLogDetail");

      PSLogDetail other = (PSLogDetail) obj;

      m_dbmsMap = other.m_dbmsMap;
      m_idMap = other.m_idMap;
      m_txnLog = other.m_txnLog;
      m_validationResults = other.m_validationResults;
   }

   // see IPSDeployComponent interface
   public int hashCode()
   {

      return m_dbmsMap.hashCode() + m_txnLog.hashCode() +
         m_validationResults.hashCode() +
         ((m_idMap == null) ? 0 : m_idMap.hashCode());
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean bEqual = false;

      if (obj instanceof PSLogDetail)
      {
         PSLogDetail other = (PSLogDetail) obj;
         if ( m_idMap == null && other.m_idMap == null )
         {
            bEqual = m_validationResults.equals(other.m_validationResults) &&
               m_txnLog.equals(other.m_txnLog) &&
               m_dbmsMap.equals(other.m_dbmsMap);
         }
         else if ( m_idMap != null && other.m_idMap != null )
         {
            bEqual = m_idMap.equals(other.m_idMap) &&
               m_validationResults.equals(other.m_validationResults) &&
               m_txnLog.equals(other.m_txnLog) &&
               m_dbmsMap.equals(other.m_dbmsMap);
         }
      }

      return bEqual;
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXLogDetail";

   /**
    * The validation results object. Initialized by constructor, it will never
    * be <code>null</code> after that.
    */
   private PSValidationResults m_validationResults;
   /**
    * The IdMap object. Initialized by constructor, it may be <code>null</code>.
    */
   private PSIdMap m_idMap;
   /**
    * The DBMS-Map object. Initialized by constructor, it may be
    * <code>null</code>.
    */
   private PSDbmsMap m_dbmsMap;
   /**
    * The transaction log summary object. Initialized by constructor, it will
    * never be <code>null</code> after that.
    */
   private PSTransactionLogSummary m_txnLog;

}
