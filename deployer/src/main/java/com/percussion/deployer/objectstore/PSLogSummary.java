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
 * Encapsulates a log summary information.
 */
public class PSLogSummary  implements IPSDeployComponent
{

   /**
    * Constructing an object from the given parameters.
    *
    * @param    pkg The package object, which may not be <code>null</code>.
    * @param    archSummary The archive summary object, which may not be
    * <code>null</code>.
    *
    * @throws IllegalArgumentException If any parameter is invalid.
    */
   public PSLogSummary(PSDeployableElement pkg, PSArchiveSummary archSummary)
   {
      if ( pkg == null )
         throw new IllegalArgumentException("pkg parameter should not be null");
      if ( archSummary == null )
         throw new IllegalArgumentException(
            "archSummary parameter should not be null");

      m_pkg = pkg;
      m_archiveSummary = archSummary;
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
   public PSLogSummary(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Gets this summary's Id.
    *
    * @return the id, may be <code>-1</code> if this summary has not been saved
    * to the database.
    */
   public int getId()
   {
      return m_id;
   }

   /**
    * Gets the package object.
    *
    * @return The package object, which will never be <code>null</code>.
    */
   public PSDeployableElement getPackage()
   {
      return m_pkg;
   }

   /**
    * Gets the log detail.
    *
    * @return The log detail object, may be <code>null</code> if it has not
    * been set.
    */
   public PSLogDetail getLogDetail()
   {
      return m_detail;
   }

   /**
    * Sets the log detail from a given <code>PSLogDetail</code> object.
    *
    * @param    detail The to be seted <code>PSLogDetail</code> object. It may
    * not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>detail</code> is
    * <code>null</code>
    */
   public void setLogDetail(PSLogDetail detail)
   {
      if (detail == null)
         throw new IllegalArgumentException("detail may not be null");

      m_detail = detail;
   }

   /**
    * Sets the summary id.
    *
    * @param    id The summary id, which may not be less than 0.
    */
   public void setId(int id)
   {
      if (id < 0)
         throw new IllegalArgumentException("id may not be less than 0");

      m_id = id;
   }

   /**
    * Gets the archive summary.
    *
    * @return The archive summary object, which will never to <code>null</code>
    */
   public PSArchiveSummary getArchiveSummary()
   {
      return m_archiveSummary;
   }

   /**
    * Determines if archive file has been deleted
    *
    * @return <code>true</code> if archive file has been deleted;
    * <code>false</code> otherwise.
    */
   public boolean doesArchiveExist()
   {
      return m_archiveExist;
   }

   /**
    * Sets the archive existents.
    *
    * @param    exists <code>true</code> if archive file has been deleted;
    * <code>false</code> otherwise.
    */
   public void setDoesArchiveExist(boolean exists)
   {
      m_archiveExist = exists;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXLogSummary (PSXDeployableElement, PSXArchiveSummary,
    *    PSXLogDetail?)>
    * &lt;!ATTLIST PSXLogSummary
    *    id CDATA #REQUIRED
    *    archiveExist (Yes | No) #REQUIRED
    * >
    * </code></pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      Element root = doc.createElement(XML_NODE_NAME);

      root.setAttribute(XML_ATTR_ID, Integer.toString(m_id));
      root.setAttribute(XML_ATTR_ARCHIVE_EXIST,
         (m_archiveExist ? XML_VALUE_TRUE : XML_VALUE_FALSE));

      root.appendChild(m_pkg.toXml(doc));
      root.appendChild(m_archiveSummary.toXml(doc));

      if ( m_detail != null )
         root.appendChild(m_detail.toXml(doc));

      return root;
   }

   // see IPSDeployComponent interface
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
      // get attributes
      String sAttrValue = PSDeployComponentUtils.getRequiredAttribute(
         sourceNode, XML_ATTR_ARCHIVE_EXIST);
      m_archiveExist = sAttrValue.equals(XML_VALUE_TRUE);
      sAttrValue = PSDeployComponentUtils.getRequiredAttribute(sourceNode,
         XML_ATTR_ID);
      m_id = Integer.parseInt(sAttrValue);

      // get child elements
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);

      Element childEl = PSDeployComponentUtils.getNextRequiredElement(tree,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN,
         PSDeployableElement.XML_NODE_NAME);
      m_pkg = new PSDeployableElement(childEl);

      childEl = PSDeployComponentUtils.getNextRequiredElement(tree,
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS,
         PSArchiveSummary.XML_NODE_NAME);
      m_archiveSummary = new PSArchiveSummary(childEl);

      childEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      if ( childEl != null )
         m_detail = new PSLogDetail(childEl);
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj parameter should not be null");
      if (!(obj instanceof PSLogSummary))
         throw new IllegalArgumentException(
            "obj wrong type, expecting PSLogSummary");

      PSLogSummary other = (PSLogSummary) obj;
      m_archiveExist = other.m_archiveExist;
      m_detail = other.m_detail;
      m_id = other.m_id;
      m_pkg = other.m_pkg;
      m_archiveSummary = other.m_archiveSummary;
   }

   // see IPSDeployComponent interface
   public int hashCode()
   {
      return (m_detail == null ? 0 : m_detail.hashCode()) + m_id +
         (m_archiveExist ? 1 : 0) + m_pkg.hashCode() +
         m_archiveSummary.hashCode();
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean bEqual = false;

      if (obj instanceof PSLogSummary)
      {
         PSLogSummary other = (PSLogSummary) obj;
         if ( m_detail == null && other.m_detail == null )
         {
            bEqual = (m_id == other.m_id) &&
                     (m_archiveExist == other.m_archiveExist) &&
                     m_pkg.equals(other.m_pkg) &&
                     m_archiveSummary.equals(other.m_archiveSummary);
         }
         else if ( m_detail != null && other.m_detail != null )
         {
            bEqual = (m_id == other.m_id) &&
                     (m_archiveExist == other.m_archiveExist) &&
                     m_pkg.equals(other.m_pkg) &&
                     m_detail.equals(other.m_detail) &&
                     m_archiveSummary.equals(other.m_archiveSummary);
         }
      }
      return bEqual;
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXLogSummary";

   // Private XML attribute names and values
   private static final String XML_ATTR_ARCHIVE_EXIST = "archiveExist";
   private static final String XML_ATTR_ID = "id";
   private static final String XML_VALUE_TRUE = "Yes";
   private static final String XML_VALUE_FALSE = "No";

   /**
    * The deployment element (or package). Initialized by constructor, it will
    * never be <code>null</code> after that.
    */
   private PSDeployableElement m_pkg;

   /**
    * This summary's Id, default to -1.
    */
   private int m_id = -1;

   /**
    * <code>true</code> if archive file has been deleted; <code>false</code>
    * otherwise.
    */
   private boolean m_archiveExist = false;

   /**
    * The log detail object. Default to be <code>null</code>.
    */
   private PSLogDetail m_detail = null;

   /**
    * The archive summary object. Initialized by constructor, it will never be
    * <code>null</code> after that.
    */
   private PSArchiveSummary m_archiveSummary = null;
}
