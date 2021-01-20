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
