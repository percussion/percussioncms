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

package com.percussion.cms.objectstore.ws;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSItemDefinition;

/**
 * An object representation of the StandardItem.xsd schema.  Accessible
 * primarily by a remote client.
 */
public class PSClientItem extends PSCoreItem
{

   /**
    * Creates a new <code>PSCoreItem</code> with only definition information and
    * no data information.  This constructor will be used by the remote clients.
    *
    * @param itemDefinition must not be <code>null</code>.
    * @throws IllegalArgumentException if the supplied id is invalid.
    */
   public PSClientItem(PSItemDefinition itemDefinition) throws PSCmsException
   {
       super(itemDefinition);
   }
}
