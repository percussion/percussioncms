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
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * An abstract class to encapsulate enable or disable an app policy,
 * the ancestor class for all other policy setting subclasses.
 */
public abstract class PSAppPolicySetting  implements IPSDeployComponent
{

   /**
    * Determines if the app policy is enabled.
    *
    * @return <code>true</code> to apply the setting to the application,
    * <code>false</code> to leave the application unmodified by this setting.
    */
   public boolean useSetting()
   {
      return m_enableAppPolicy;
   }

   /**
    * Enables the setting by <code>useSetting</code>.
    * See {@link #useSetting()} for information.
    *
    * @param    useSetting <code>true</code> if enable the app policy;
    * <code>false</code> otherwise.
    */
   public void setUseSetting(boolean useSetting)
   {
      m_enableAppPolicy = useSetting;
   }

   /**
    * Serializes this object's state to its XML representation.  Format is:
    *
    * <pre><code>
    *    &lt;!ELEMENT xmlNodeName EMPTY)>
    *    &lt;!ATTLIST xmlNodeName
    *       useSetting (Yes | No) #REQUIRED
    *    >
    * </code>/<pre>
    *
    * Where, <code>xmlNodeName</code> is one of the
    * <code>PSXxxxPolicySetting.XML_NODE_NAME</code> values of the policy
    * setting subclasses.
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   protected Element toXml(Document doc, String xmlNodeName)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc should not be null");

      Element root = doc.createElement(xmlNodeName);
      if ( useSetting() )
         root.setAttribute(XML_ATTR_USE_SETTING, XML_ATTR_TRUE);
      else
         root.setAttribute(XML_ATTR_USE_SETTING, XML_ATTR_FALSE);

      return root;
   }

   // see IPSDeployComponent interface
   protected void fromXml(Element sourceNode, String xmlNodeName)
      throws PSUnknownNodeTypeException
   {
       if (sourceNode == null)
         throw new IllegalArgumentException("sourceNode may not be null");

      if (!xmlNodeName.equals(sourceNode.getNodeName()))
      {
         Object[] args = { xmlNodeName, sourceNode.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      String sEnableFlag = PSDeployComponentUtils.getRequiredAttribute(
         sourceNode, XML_ATTR_USE_SETTING);

      setUseSetting(sEnableFlag.equals(XML_ATTR_TRUE));
  }

   // See IPSDeployComponent interface
   public void copyFrom(IPSDeployComponent obj)
   {
      if ( obj == null )
         throw new IllegalArgumentException("obj parameter should not be null");

      if (!(obj instanceof PSAppPolicySetting))
         throw new IllegalArgumentException(
            "obj wrong type, expecting PSAppPolicySetting");

      PSAppPolicySetting other = (PSAppPolicySetting) obj;

      m_enableAppPolicy = other.m_enableAppPolicy;
   }

   // See IPSDeployComponent interface
   public int hashCode()
   {
      return super.hashCode();
   }

   // See IPSDeployComponent interface
   public boolean equals(Object obj)
   {
      boolean result = false;

      if (obj instanceof PSAppPolicySetting)
      {
         PSAppPolicySetting other = (PSAppPolicySetting) obj;
         result = other.m_enableAppPolicy == m_enableAppPolicy;
      }
      return result;
   }

   // Various XML attribute and values
   protected static final String XML_ATTR_USE_SETTING = "useSetting";
   protected static final String XML_ATTR_TRUE = "Yes";
   protected static final String XML_ATTR_FALSE = "No";

  /**
    * Determines if the app policy is enabled. Default to <code>true</code>.
    */
   protected boolean m_enableAppPolicy = true;
}
