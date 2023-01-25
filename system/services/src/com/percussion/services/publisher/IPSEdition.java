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
import com.percussion.services.publisher.data.PSEditionType;
import com.percussion.utils.guid.IPSGuid;

/**
 * An edition is a collection of content lists and tasks for a site that 
 * embodies a unit of publishing work. An edition is "run" and first the
 * pre-tasks associated with the edition are executed. Then the content lists
 * are evaluated and the content published to the delivery engine. Finally,
 * the post-tasks associated with the edition are executed.
 * <p>
 * Any particular edition will only be run singly, but multiple editions can be
 * run simultaneously on a given site.
 *  
 * @author dougrand
 */
public interface IPSEdition extends IPSCatalogIdentifier
{
   /**
    * Priorities of the Edition.
    */
   enum Priority
   {
      /**
       * The highest priority
       */
      HIGHEST(5),
      
      /**
       * High priority, but less than the {@link #HIGHEST}
       */
      HIGH(4),
      
      /**
       * Medium priority.
       */
      MEDIUM(3),
      
      /**
       * Low priority, but higher than {@link #LOWEST}.
       */
      LOW(2),
      
      /**
       * The lowest priority.
       */
      LOWEST(1);
      
      /**
       * Constructs a priority from a value.
       * @param value the value of the priority.
       */
      Priority(int value)
      {
         m_value = value;
      }
      
      /**
       * Gets the value of the priority.
       * @return the value of the priority.
       */
      public int getValue()
      {
         return m_value;
      }
      
      /**
       * The value of the priority. Set by constructor.
       */
      private int m_value;
   }
   
   /**
    * Get the unique name of this edition. The name has limitations on allowed
    * chars. 
    * 
    * @return Never <code>null</code> or empty. 
    */
   String getName();
   
   /**
    * Set the name of this edition.
    * @param name the name may be <code>null</code>
    */
   void setName(String name);

   /**
    * Get the display title, which is shown in the user interface.
    * @return May be <code>null</code>, never empty.
    */
   String getDisplayTitle();

   /**
    * Set the display title
    * @param displayTitle the display title may be <code>null</code>
    */
   void setDisplayTitle(String displayTitle);

   /**
    * Get the comment about the edition
    * @return the comment, may be <code>null</code> 
    */
   String getComment();

   /**
    * set the comment
    * @param comment the comment, may be <code>null</code>
    */
   void setComment(String comment);

   /**
    * Get the edition type 
    * @return the edition type, never <code>null</code>
    */
   PSEditionType getEditionType();

   /**
    * Set the edition type
    * @param editionType the edition type, never <code>null</code>
    */
   void setEditionType(PSEditionType editionType);

   /**
    * Get the destination site id
    * @return the destination site id, may be <code>null</code>
    */
   IPSGuid getSiteId();

   /**
    * Set the destination site id
    * @param siteId the destination site id, may be <code>null</code>
    */
   void setSiteId(IPSGuid siteId);

   /**
    * Gets the publish sever ID if it is not <code>null</code>; otherwise gets the site id.
    * @return the publish server or site ID.
    */
   IPSGuid getPubServerOrSiteId();
   
   /**
    * Get the destination site id
    * @return the destination site id, may be <code>null</code>
    */
   IPSGuid getPubServerId();

   /**
    * Set the destination site id
    * @param siteId the destination site id, may be <code>null</code>
    */
   void setPubServerId(IPSGuid serverId);

   /**
    * Get the priority.
    * @return the priority, never <code>null</code>.
    */
   Priority getPriority();

   /**
    * Set the priority
    * @param priority the priority, not <code>null</code>.
    */
   void setPriority(Priority priority);

   /** (non-Javadoc)
    * @see java.lang.Object#equals(java.lang.Object)
    */
   boolean equals(Object b);

   /** (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   int hashCode();

   /** (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   String toString();

   /**
    * Make a copy of this edition
    * @return the copy, never <code>null</code>
    */
   Object clone() throws CloneNotSupportedException;
   
   /**
    * Copy all properties from a given Edition, except its internal ID and 
    * version number if there is any.
    *  
    * @param other the to be copied Edition, never <code>null</code>. 
    */
   public void copy(IPSEdition other);

}
