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
package com.percussion.services.publisher;

import com.percussion.services.catalog.IPSCatalogIdentifier;
import com.percussion.utils.guid.IPSGuid;

public interface IPSEditionContentList extends IPSCatalogIdentifier
{
   /**
    * Gets the Edition ID.
    * @return the Edition ID, never <code>null</code>.
    */
   IPSGuid getEditionId();
   
   /**
    * Gets the ContentList ID
    * @return the Content List ID, never <code>null</code>.
    */
   IPSGuid getContentListId();

   /**
    * @return the authtype
    */
   Integer getAuthtype();

   /**
    * @param authtype the authtype to set
    */
   void setAuthtype(Integer authtype);

   /**
    * Gets the delivery context ID.
    * @return the delivery context ID, never <code>null</code>.
    */
   IPSGuid getDeliveryContextId();

   /**
    * Sets the ID of the delivery context.
    * @param context the new context ID, never <code>null</code>.
    */
   void setDeliveryContextId(IPSGuid context);
   
   /**
    * Gets the assembly context ID
    * @return the context ID, may be <code>null</code> if not defined.
    */
   IPSGuid getAssemblyContextId();

   /**
    * Sets the assembly context ID
    * @param context the new context ID, it may be <code>null</code>.
    */
   void setAssemblyContextId(IPSGuid context);   

   /**
    * @return the seq
    */
   Integer getSequence();

   /**
    * @param seq the seq to set
    */
   void setSequence(Integer seq);

   boolean equals(Object b);

   int hashCode();

   String toString();

   /**
    * Copy properties of the given Edition/ContentList association to this 
    * object excluding repository IDs.
    */
   void copy(IPSEditionContentList other);
}
