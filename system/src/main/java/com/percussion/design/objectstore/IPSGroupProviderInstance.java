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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An instance of a definition for an
 * {@link com.percussion.security.IPSGroupProvider}.  Used to define a source of
 * group information for security providers to use.
 */

public interface IPSGroupProviderInstance extends IPSComponent
{

   /**
    * @return The name of this provider, never <code>null</code> or empty.
    */
   public String getName();


   /**
    * @return The type of security provider that may use this group provider.
    * One of the PSSecurityProvider.SP_TYPE_xxx types.
    */
   public int getType();


   /**
   * This method is called to create a PSXGroupProviderInstance
   * Xml element node containing the data described in this object.
   * <p>
   * The structure of the xml document is:
   * <pre><code>
   *  &lt;!--
   *        PSXGroupProviderInstance defines a group provider used
   *        to locate groups and determine group membership.
   *        Content is defined by the class implementing this
   *        interface.
   *  --&gt;
   *  &lt;!ELEMENT PSXGroupProviderInstance  (ANY)&gt;
   *
   *  &lt;!--
   *  Attributes associated with a group provider:
   *
   *  id - The internal identifier for this object.
   *  name - The name of this group provider.
   *  type - The security provider type this instance uses
   *  classname - The name of the derived class that should be instantiated to
   *     handle serialization of the content of this element.
   *  --&gt;
   *  &lt;!ATTLIST PSXGroupProviderInstance
   *        id        ID          #REQUIRED
   *        name      CDATA       #REQUIRED
   *        type      CDATA       #REQUIRED
   *        classname CDATA       #REQUIRED
   *  &gt;
   *
   * </code></pre>
   *
   * @return the newly created PSXGroupProviderInstance Xml element node, never
   * <code>null</code> or empty.
   */
   public Element toXml(Document doc);

   /**
    * Name of parent XML element.
    */
   public static final String XML_NODE_NAME = "PSXGroupProviderInstance";


}
