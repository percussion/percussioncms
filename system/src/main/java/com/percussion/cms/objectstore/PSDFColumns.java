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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import org.w3c.dom.Element;
/**
 * See {@link PSDbComponentList base class} for details.
 */
public class PSDFColumns extends PSDbComponentList
{
   /**
    * ctor calls base class.
    */
   public PSDFColumns()
      throws ClassNotFoundException, PSCmsException
   {
      super(PSDisplayColumn.class.getName());
   }

   /**
    * Ctor for reserializing. See {@link
    * PSDbComponentList#PSDbComponentList(Element) base ctor} for more details.
    */
   public PSDFColumns(Element src)
      throws PSUnknownNodeTypeException
   {
      super(src);
   }

   public static final String XML_NODE_NAME = "PSXDFColumns";
}
