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
 * Encapsulates all policy settings.
 */
public class PSAppPolicySettings  implements IPSDeployComponent
{

   /**
    * Default constructor with default settings.
    */
   public PSAppPolicySettings()
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
   public PSAppPolicySettings(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Gets the app enabled policy object.
    *
    * @return The app enabled policy object, never <code>null</code>.
    */
   public PSAppEnabledPolicySetting getEnabledSetting()
   {
      return m_appEnabledPolicy;
   }

   /**
    * Gets the trace policy object.
    *
    * @return The trace policy object, never <code>null</code>.
    */
   public PSTracePolicySetting getTraceSetting()
   {
      return m_tracePolicy;
   }

   /**
    * Gets the log policy object.
    *
    * @return The log policy object, never <code>null</code>.
    */
   public PSLogPolicySetting getLogSetting()
   {
      return m_logPolicy;
   }


   /**
    * Serializes this object's state to its XML representation.  Format is:
    *
    * <pre><code>
    *    &lt;!ELEMENT PSXAppPolicySettings (PSXAppEnabledPolicySetting,
    *    PSXTracePolicySetting, PSXLogPolicySetting)
    *    >
    * </code>/<pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc should not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.appendChild(m_appEnabledPolicy.toXml(doc));
      root.appendChild(m_tracePolicy.toXml(doc));
      root.appendChild(m_logPolicy.toXml(doc));

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

      // get child elements
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element childEl = PSDeployComponentUtils.getNextRequiredElement(tree,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN,
         PSAppEnabledPolicySetting.XML_NODE_NAME);
      m_appEnabledPolicy = new PSAppEnabledPolicySetting(childEl);

      childEl = PSDeployComponentUtils.getNextRequiredElement(tree,
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS,
         PSTracePolicySetting.XML_NODE_NAME);
      m_tracePolicy = new PSTracePolicySetting(childEl);

      childEl = PSDeployComponentUtils.getNextRequiredElement(tree,
         PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS,
         PSLogPolicySetting.XML_NODE_NAME);
      m_logPolicy = new PSLogPolicySetting(childEl);
   }

   // See IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj parameter should not be null");

      if (!(obj instanceof PSAppPolicySetting))
         throw new IllegalArgumentException(
            "obj wrong type, expecting PSAppPolicySetting");

      PSAppPolicySettings other = (PSAppPolicySettings) obj;

      m_appEnabledPolicy = other.m_appEnabledPolicy;
      m_tracePolicy = other.m_tracePolicy;
      m_logPolicy = other.m_logPolicy;

   }

   // See IPSDeployComponent interface
   public int hashCode()
   {
      return m_appEnabledPolicy.hashCode() + m_tracePolicy.hashCode() +
         m_logPolicy.hashCode();
   }

   // See IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean result = false;

      if (obj instanceof PSAppPolicySettings)
      {
         PSAppPolicySettings other = (PSAppPolicySettings) obj;
         result = other.m_appEnabledPolicy.equals(m_appEnabledPolicy) &&
            other.m_tracePolicy.equals(m_tracePolicy) &&
            other.m_logPolicy.equals(m_logPolicy);
      }
      return result;
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXAppPolicySettings";

   /**
    * The app enabled policy, initialized to default setting,
    * never <code>null</code>.
    */
   private PSAppEnabledPolicySetting m_appEnabledPolicy =
      new PSAppEnabledPolicySetting();

   /**
    * The trace policy, initialized to default setting,
    * never <code>null</code>.
    */
   private PSTracePolicySetting m_tracePolicy = new PSTracePolicySetting();

   /**
    * The log policy, initialized to default setting,
    * never <code>null</code>.
    */
   private PSLogPolicySetting m_logPolicy = new PSLogPolicySetting();
}
