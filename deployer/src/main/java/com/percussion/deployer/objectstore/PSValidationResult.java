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
import com.percussion.util.PSIteratorUtils;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Encapsulates the result of a dependency object.
 */
public class PSValidationResult  implements IPSDeployComponent
{

   /**
    * Constructing the object with given parameters. The constructed object is
    * default not to skip installation.
    *
    * @param dep The dependency object. It may not be <code>null</code>
    * @param isError Determines an error for this object. <code>true</code> if
    * it is an error, <code>false</code> to indicate a warning.
    * @param message The message of this object. It may not be
    * <code>null</code> or empty.
    * @param isAllowSkip <code>true</code> if allow skip install;
    * <code>false</code> if not allow skip install.
    *
    * @throws IllegalArgumentException If <code>dep</code> is <code>null</code>
    */
   public PSValidationResult(PSDependency dep, boolean isError,
      String message, boolean isAllowSkip)
   {
      if ( dep == null )
         throw new IllegalArgumentException("dep may not be null");
      if ( message == null || message.trim().length() == 0 )
         throw new IllegalArgumentException("message may not be null or empty");

      // keep a copy of the dependency with no chilren or ancestors
      m_dep = (PSDependency)dep.clone();
      m_dep.setDependencies(PSIteratorUtils.emptyIterator());
      m_dep.setAncestors(PSIteratorUtils.emptyIterator());
      
      m_isError = isError;
      m_isAllowSkip = isAllowSkip;
      m_skipInstall = false;
      m_message = message;
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
   public PSValidationResult(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the dependency object.
    *
    * @return The dependency object, it will never be <code>null</code>.
    */
   public PSDependency getDependency()
   {
      return m_dep;
   }

   /**
    * Get the message of the object.
    *
    * @return The message of the object, it will never be <code>null</code>,
    * or empty.
    */
   public String getMessage()
   {
      return m_message;
   }

   /**
    * Determines if the object is an error or warning.
    *
    * @return <code>true</code> if it is an error, <code>false</code> if it is
    * a warning otherwise
    */
   public boolean isError()
   {
      return m_isError;
   }

   /**
    * Determines if user can choose to skip installing this dependency if it is
    * included.  Should only be <code>true</code> if dependency is included and
    * may optionally be excluded
    *
    * @return <code>true</code> if it is allowed to skip install;
    * <code>false</code> otherwise.
    */
   public boolean allowSkip()
   {
      return m_isAllowSkip;
   }

   /**
   * Determines if this the dependency of this object is to be installed or
   * skipped.
   *
   * @param skipInstall If <code>true</code>, dependency should not be
   * installed; <code>false</code> otherwise.
   *
   * @throws IllegalArgumentException if <code>skipInstall</code> is
   * <code>true</code> and
   * {@link #allowSkip()} returns <code>false</code>.
   */
   public void skipInstall(boolean skipInstall)
   {
      if ( (!m_isAllowSkip) && skipInstall )
         throw  new IllegalArgumentException(
            "skipInstall cannot be true, due to allowSkip() is false");

      m_skipInstall = skipInstall;
   }

   /**
    * Determines if the dependency should be skipped for installation.
    *
    * @return <code>true</code> if the dependency is skipped;
    * <code>false</code> otherwise.
    */
   public boolean isSkip()
   {
      return m_skipInstall;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXValidationResult
    *    (PSXDeployableElement | PSXDeployableObject | PSXUserDependency)
    * &lt;!ATTLIST PSXValidationResult
    *    message CDATA #REQUIRED
    *    isError (Yes | No) #REQUIRED
    *    isAllowSkip (Yes | No) #REQUIRED
    *    skipInstall (Yes | No) #REQUIRED
    * >
    * </code>/<pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc should not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_IS_ERROR,
         m_isError ? XML_VALUE_TRUE : XML_VALUE_FALSE);
      root.setAttribute(XML_ATTR_ALLOWSKIP,
         m_isAllowSkip ? XML_VALUE_TRUE : XML_VALUE_FALSE);
      root.setAttribute(XML_ATTR_SKIPINSTALL,
         m_skipInstall ? XML_VALUE_TRUE : XML_VALUE_FALSE);
      root.setAttribute(XML_ATTR_MSG,
         m_message == null ? "" : m_message);

      root.appendChild(m_dep.toXml(doc));

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

      m_message = sourceNode.getAttribute(XML_ATTR_MSG);
      if (m_message == null)
      {
         Object[] args = {sourceNode.getTagName(), XML_ATTR_MSG, "null"};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      m_isError = getRequiredBoolAttr(sourceNode, XML_ATTR_IS_ERROR);
      m_isAllowSkip = getRequiredBoolAttr(sourceNode, XML_ATTR_ALLOWSKIP);
      m_skipInstall = getRequiredBoolAttr(sourceNode, XML_ATTR_SKIPINSTALL);

      // get m_dep at last
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element depEl = tree.getNextElement(firstFlags);

      // need to have at least one source element
      if (depEl == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, PSDependency.XML_NODE_NAME);
      }

      if ( depEl.getNodeName().equals(PSDeployableObject.XML_NODE_NAME) )
         m_dep = new PSDeployableObject(depEl);
      else if ( depEl.getNodeName().equals(PSDeployableElement.XML_NODE_NAME) )
         m_dep = new PSDeployableElement(depEl);
      else if ( depEl.getNodeName().equals(PSUserDependency.XML_NODE_NAME) )
         m_dep = new PSUserDependency(depEl);
      else
      {
         Object[] args = {
         "(PSXDeployableElement | PSXDeployableObject | PSXUserDependency)",
         depEl.getNodeName()};
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
   }

   /**
    * Get a boolean value for a given XML attribute.
    *
    * @param srcNode The XML node, assuming not <code>null</code>
    * @param attrName The name of the XML attribute, assuming it is not
    * <code>null</code> or empty.
    *
    * @return <code>true</code> if the attribute value is "Yes",
    * <code>false</code> otherwise.
    *
    * @throws PSUnknownNodeTypeException if the <code>srcNode</code> does not
    * have <code>attrName</code> attribute.
    */
   private boolean getRequiredBoolAttr(Element srcNode, String attrName)
      throws PSUnknownNodeTypeException
   {
      String sAttrValue = PSDeployComponentUtils.getRequiredAttribute(srcNode,
         attrName);
      return sAttrValue.equals(XML_VALUE_TRUE);
   }

   // see IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");

      if ((obj instanceof PSValidationResult))
         throw new IllegalArgumentException("obj is not PSValidationResult");

      PSValidationResult obj2 = (PSValidationResult) obj;
      m_dep = obj2.m_dep;
      m_isAllowSkip = obj2.m_isAllowSkip;
      m_isError = obj2.m_isError;
      m_skipInstall = obj2.m_skipInstall;
      m_message = obj2.m_message;
   }

   // see IPSDeployComponent interface
   public int hashCode()
   {
      return m_dep.hashCode() + (m_isAllowSkip ? 1 : 0) +
         (m_skipInstall ? 1 : 0) + (m_isError ? 1 : 0) +
         m_message.hashCode();
   }

   // see IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean isEqual = false;

      if ((obj instanceof PSValidationResult))
      {
         PSValidationResult obj2 = (PSValidationResult) obj;
         isEqual = m_dep.equals(obj2.m_dep) &&
                   m_message.equals(obj2.m_message) &&
                   (m_isAllowSkip == obj2.m_isAllowSkip) &&
                   (m_skipInstall == obj2.m_skipInstall) &&
                   (m_isError == obj2.m_isError);
      }
      return isEqual;
    }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXValidationResult";

   /**
    * Private XML node and attribute names
    */
   private static final String XML_ATTR_MSG = "message";
   private static final String XML_ATTR_IS_ERROR = "isError";
   private static final String XML_ATTR_ALLOWSKIP = "isAllowSkip";
   private static final String XML_ATTR_SKIPINSTALL = "skipInstall";
   private static final String XML_VALUE_TRUE = "Yes";
   private static final String XML_VALUE_FALSE = "No";


   /**
    * The dependency object, it will never be <code>null</code>. Initialized
    * by constructor, only modified by {@link #copyFrom(Object)}.
    */
   private PSDependency  m_dep;
   /**
    * <code>true</code> if the object represent an error; <code>false</code>
    * otherwise. Initialized by constructor, modified by
    * {@link #copyFrom(Object)}.
    */
   private boolean m_isError;
   /**
    * The message of the object. It will never to be <code>null</code> or empty.
    * Initialized by constructor, only modified by {@link #copyFrom(Object)}
    * and {@link #fromXml(Element sourceNode)}.
    */
   private String m_message;
   /**
    * <code>true</code> if allow skip installation; <code>false</code>
    * otherwise. Initialized by constructor, modified by
    * {@link #copyFrom(Object)} and {@link #fromXml(Element sourceNode)}.
    */
   private boolean m_isAllowSkip;
   /**
    * <code>true</code> if the installation of the dendency should be skipped;
    * <code>false</code> otherwise. Default to <code>false</code>, modified
    * by {@link #copyFrom(Object)}, {@link #skipInstall(boolean)} and
    * {@link #fromXml(Element sourceNode)}
    */
   private boolean m_skipInstall = false;

}
