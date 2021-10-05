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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
