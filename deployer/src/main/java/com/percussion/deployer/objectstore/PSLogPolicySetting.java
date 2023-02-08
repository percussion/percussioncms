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


import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * Encapsulates log policy setting
 */
public class PSLogPolicySetting  extends PSAppPolicySetting
{
   /**
    * Default constructor. Default to disable the log policy,
    * {@link #isLoggingEnabled()} return <code>false</code>.
    */
   public PSLogPolicySetting()
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
   public PSLogPolicySetting(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Determines if the log policy is enabled.
    *
    * @return <code>true</code> if the log policy is enabled;
    * <code>false</code> otherwise.
    */
   public boolean isLoggingEnabled()
   {
      return false; // no specific setting for this class yet, hard code for now
   }

   /**
    * Sets to enable or disable the log policy.
    *
    * @param    enabled <code>true</code> if enable log; <code>false</code>
    * otherwise.
    */
   public void setIsLoggingEnabled(boolean enabled)
   {
      // no specific setting for this class yet, no op for now
   }

   /**
    * Serializes this object's state to its XML representation.  Format is:
    *
    * <pre><code>
    *    &lt;!ELEMENT PSXLogPolicySetting EMPTY)>
    *    &lt;!ATTLIST PSXLogPolicySetting
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
   public static final String XML_NODE_NAME = "PSXLogPolicySetting";
}
