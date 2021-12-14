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
package com.percussion.extension;

import com.percussion.design.objectstore.IPSComponent;
import org.w3c.dom.Element;

/**
 * The extension param def provides the name, type, and description of a
 * runtime parameter that is used or required by an extension. Parameters
 * are bound at extension invocation time via an extension-specific
 * mechanism.
 */
public interface IPSExtensionParamDef extends IPSComponent
{
   /**
    * Gets the parameter name.
    *
    * @return The parameter name. Never <CODE>null</CODE>.
    */
   public String getName();

   /**
    * Gets the name of the parameter data type. The set of valid type names
    * will be different for each language.
    *
    * @return The data type name. Never <CODE>null</CODE>.
    */
   public String getDataType();

   /**
    * Gets the description of this parameter, which is free-form human
    * readable text used to describe the parameter and possibly its usage and
    * constraints. Can be <CODE>null</CODE>.
    *
    * @return The human readable description, or <CODE>null</CODE> if none
    * is provided.
    */
   public String getDescription();

   /**
    * Serializes this param def into (or under) the given root element.
    *
    * @param root The root element. If root is of the correct type,
    * then the content will be created directly under root. If root
    * is not of the correct type, an additional element of the correct
    * type will be created under root, and the content stored directly beneath
    * the newly created element. Must not be <CODE>null</CODE>.
    *
    * @return The element under which the content was stored directly.
    */
   public Element toXml(Element root);
}
