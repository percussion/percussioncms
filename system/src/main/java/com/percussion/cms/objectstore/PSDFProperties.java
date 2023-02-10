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

/**
 * See base class {@link com.percussion.cms.objectstore.PSDbComponentList}
 * for details. 
 */
public class PSDFProperties extends PSDbComponentCollection
{
   public PSDFProperties()
      throws ClassNotFoundException, PSCmsException
   {              
      super(PSDFMultiProperty.class.getName());
   }
   
   public String getNodeName()
   {
      return XML_NODE_NAME;
   }

   // public static defined
   public static final String XML_NODE_NAME = "PSX_PROPERTIES";
}
