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
package com.percussion.services.legacy;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Date;

/**
 * The minimum information of an content item, which is cached by the server.
 * 
 * @author yubingchen
 */
public interface IPSItemEntry
{
   /**
    * Gets the name of the item, which is the <code>sys_title</code> field of the item.
    * 
    * @return the name of the item.
    */
   String getName();
   
   /**
    * Gets the content ID of the item.
    * 
    * @return the content ID.
    */
   int getContentId();
   
   /**
    * Gets the object type.
    * @return the object type.
    */
   int getObjectType();
   
   /**
    * Gets the community ID of the item.
    * 
    * @return the community ID.
    */
   int getCommunityId();
   
   /**
    * Gets the content type ID of the item.
    * 
    * @return the content type ID.
    */
   int getContentTypeId();
   
   /**
    * Gets the content type label of the item.
    * @return the label, may be <code>null</code> or empty if the content type is not active.
    */
   String getContentTypeLabel();
   
   /**
    * Gets the user name that created the item.
    * @return the user name, may be <code>null</code> or empty.
    */
   String getCreatedBy();
   
   /**
    * Gets the last modified date of the item.
    * @return the last modified date, should not be <code>null</code> if properly configure.
    */
   Date getLastModifiedDate();
   
   /**
    * Get the last modifier user name of the item.
    * 
    * @return The name, should be not <code>null<code/> or empty.
    */
   String getLastModifier();
   
   /**
    * Gets the 1st published date of the item.
    * 
    * @return the date, may be <code>null</code> if has not been published.
    */
   Date getPostDate();
   
   /**
    * Gets the created date of the item.
    * @return the date, may be <code>null</code> if has not been published.
    */
   Date getCreatedDate();
   
   /**
    * Gets the state name of the work-flow
    * @return the state name, may be <code>null</code> or empty if the work-flow and state ID are unknown.
    */
   String getStateName();
   
   /**
    * Gets the work-flow ID.
    * @return the work-flow ID, it may be <code>-1</code> if unknown.
    */
   int getWorkflowAppId();
   
   /**
    * Gets the state ID of the work-flow.
    * @return the state ID, it may be <code>-1</code> if unknown.
    */
   int getContentStateId();

   /**
    * Determines if the item is a folder or not.
    * @return <code>true</code> if the item is a folder.
    */
   boolean isFolder();
   
   /**
    * Get the tip revision of the item.  
    * 
    * @return the revision, <code>-1</code> if a folder or not known.
    */
   int getTipRevision();
   
   int getCurrentRevision();

   int getPublicRevision();

    /**
     * Get the user that has checked out this item
     *
     * @return The user name if the item is currently checkout. May be null or empty
     */
   String getCheckedOutUsername();
   
   /**
    * Returns the XML representation of the objects. The format of the 
    * returned element is following:
    * <pre>
    * &lt;!ELEMENT PSXItemEntry EMPTY&gt;
    * &lt;!ATTLIST PSXItemEntry
    *    name CDATA #REQUIRED
    *    contentId CDATA #REQUIRED
    *    contentTypeId CDATA #REQUIRED
    *    communityId CDATA #IMPLIED
    *    objectType CDATA #REQUIRED
    *    &gt;
    * </pre>
    *   
    * @param doc the docment used to generate the XML, never <code>null</code>.
    * 
    * @return the generated in XML, never <code>null</code>
    */
   Element toXml(Document doc);

  
}
