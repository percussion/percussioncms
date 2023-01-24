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
