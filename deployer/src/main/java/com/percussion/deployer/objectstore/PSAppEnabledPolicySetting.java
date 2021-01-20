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

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;




/**
 * Encapsulates app enable policy
 */
public class PSAppEnabledPolicySetting  extends PSAppPolicySetting
{
   /**
    * Default constructor. Default to disable the log policy,
    * {@link #isAppEnabled()} return <code>false</code>.
    */
   public PSAppEnabledPolicySetting()
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
   public PSAppEnabledPolicySetting(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Determines if the app policy is enabled.
    *
    * @return <code>true</code> if the app policy is enabled;
    * <code>false</code> otherwise.
    */
   public boolean isAppEnabled()
   {
      return true; // no specific setting for this class yet, hard code for now
   }

   /**
    * Sets to enable or disable the app policy.
    *
    * @param    enabled <code>true</code> if set to enable the app policy;
    * <code>false</code> otherwise.
    */
   public void setAppEnabled(boolean isEnabled)
   {
      // no specific setting for this class yet, no op for now
   }

   /**
    * Serializes this object's state to its XML representation.  Format is:
    *
    * <pre><code>
    *    &lt;!ELEMENT PSXAppEnabledPolicySetting EMPTY)>
    *    &lt;!ATTLIST PSXAppEnabledPolicySetting
    *       useSetting (Yes | No) #REQUIRED
    *    >
    * </code>/<pre>
    *
    * See {@link IPSDeployComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      return toXml(doc, XML_NODE_NAME);
   }

   // See IPSDeployComponent interface
   public void fromXml(Element sourceNode) throws PSUnknownNodeTypeException
   {
      fromXml(sourceNode, XML_NODE_NAME);
   }

   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXAppEnabledPolicySetting";

}
