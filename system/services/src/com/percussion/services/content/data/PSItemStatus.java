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
package com.percussion.services.content.data;

import com.percussion.services.utils.xml.PSXmlSerializationHelper;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.xml.sax.SAXException;

/**
 * This object represents a single item status at the time it was prepared for
 * edit. This object may be used to release the item later.
 */
public class PSItemStatus
{
   /**
    * The content id.
    */
   private int contentId;
   
   /**
    * See {@link #isDidCheckout()} for it description. 
    * Default to <code>false</code>.
    */
   private boolean didCheckout = false;

   /**
    * See {@link #isDidTransition()} for its description.
    * Default to <code>false</code>.
    */
   private boolean didTransition = false;

   /**
    * See {@link #getFromState()} for its description.
    * Default to <code>null</code>.
    */
   private java.lang.String fromState = null;

   /**
    * See {@link #getFromStateId()} for its description.
    * Default to <code>null</code>.
    */
   private Long fromStateId = null;
   
   /**
    * See {@link #getToState()} for its description.
    * Default to <code>null</code>.
    */
   private java.lang.String toState = null;
   
   /**
    * See {@link #getToStateId()} for its description.
    * Default to <code>null</code>.
    */
   private Long toStateId = null;
   
   /**
    * Creates an instance with the specified id of a item.
    * 
    * @param contentId the id of the item.
    */
   public PSItemStatus(int contentId)
   {
      this.contentId = contentId;
   }

   /**
    * Creates an instance from the specified parameters.
    * 
    * @param contentId the id of the item, may not be <code>null</code>.
    * @param didCheckout <code>true</code> if the item was, <code>false</code> 
    *    otherwise. see {@link #isDidCheckout()} for more info.
    * @param didTransition <code>true</code> if the item was, <code>false</code> 
    *    otherwise. see {@link #isDidTransition()} for more info.
    * @param fromStateId the id of the from-state, see {@link #getFromStateId()}
    *    for more info. It may be <code>null</code>.
    * @param fromState the name of the from-state, see {@link #getFromState()}
    *    for more info. It may be <code>null</code> or empty.
    * @param toStateId the id of the to-state, see {@link #getToStateId()}
    *    for more info. It may be <code>null</code>.
    * @param toState the name of the to-state, see {@link #getToState()}
    *    for more info. It may be <code>null</code> or empty.
    */
   public PSItemStatus(int contentId, boolean didCheckout, boolean didTransition,
         Long fromStateId, String fromState, Long toStateId, String toState)
   {
      this(contentId);
      this.didCheckout = didCheckout;
      this.didTransition = didTransition;
      this.fromStateId = fromStateId;
      this.fromState = fromState;
      this.toStateId = toStateId;
      this.toState = toState;
   }

   /**
    * @return the id of the item.
    */
   public int getId()
   {
      return contentId;
   }
   
   /**
    * Was the item checked out to prepare it for edit?
    * 
    * @return <code>true</code> if it was, <code>false</code> otherwise.
    */
   public boolean isDidCheckout()
   {
      return didCheckout;
   }
   
   /**
    * Set if the item was checked out to prepare it for edit.
    * 
    * @param didCheckout <code>true</code> if it was, <code>false</code>
    *    otherwise.
    */
   public void setDidCheckout(boolean didCheckout)
   {
      this.didCheckout = didCheckout;
   }
   
   /**
    * Was the item transitioned to prepare it for edit?
    * 
    * @return <code>true</code> if it was, <code>false</code> otherwise.
    */
   public boolean isDidTransition()
   {
      return didTransition;
   }
   
   /**
    * Set if the item ws transitioned to prepare it for edit.
    * 
    * @param didTransition <code>true</code> if it was, <code>false</code>
    *    otherwise.
    */
   public void setDidTransition(boolean didTransition)
   {
      this.didTransition = didTransition;
   }

   /**
    * Get the name of the workflow state in which the item was before it was 
    * prepared for edit. This is only available if the item had to be 
    * transitioned to bring it into edit mode.
    * 
    * @return the item state before it was prepared for edit, may be
    *    <code>null</code>, never empty.
    */
   public String getFromState()
   {
      return fromState;
   }
   
   /**
    * Set the name of the workflow state in which the item was before it was 
    * prepared for edit.
    * 
    * @param fromState the new item state before it was prepared for edit, 
    *    may be <code>null</code>, not empty.
    */
   public void setFromState(String fromState)
   {
      if (StringUtils.isEmpty(fromState))
         throw new IllegalArgumentException("fromState cannot be empty");
      
      this.fromState = fromState;
   }

   /**
    * Get the name of the workflow state to which the item was transitioned 
    * to prepare it for edit. This is only available if the item had to be 
    * transitioned to bring it into edit mode.
    * 
    * @return the item state to which the it was transitioned to prepared it 
    *    for edit, may be <code>null</code>, never empty.
    */
   public String getToState()
   {
      return toState;
   }
   
   /**
    * Set the name of the workflow state to which the item was transitioned 
    * to prepare it for edit.
    * 
    * @param toState the new item state to which the it was transitioned to 
    *    prepared it for edit, may be <code>null</code>, not empty.
    */
   public void setToState(String toState)
   {
      if (StringUtils.isEmpty(toState))
         throw new IllegalArgumentException("toState cannot be empty");
      
      this.toState = toState;
   }

   /**
    * Get the id of the workflow state in which the item was before it was 
    * prepared for edit. This is only available if the item had to be 
    * transitioned to bring it into edit mode.
    * 
    * @return the state id described above, may be <code>null</code>.
    */
   public java.lang.Long getFromStateId() 
   {
       return fromStateId;
   }


   /**
    * Set the id of the workflow state in which the item was before it was 
    * prepared for edit.
    * 
    * @param fromStateId the id of new item state before it was prepared for 
    *    edit, may be <code>null</code>.
    */
   public void setFromStateId(java.lang.Long fromStateId) 
   {
       this.fromStateId = fromStateId;
   }


   /**
    * Get the id of the workflow state to which the item was transitioned 
    * to prepare it for edit. This is only available if the item had to be 
    * transitioned to bring it into edit mode.
    * 
    * @return the state id to which the item was transitioned to prepared it 
    *    for edit, may be <code>null</code>.
    */
   public java.lang.Long getToStateId() 
   {
       return toStateId;
   }


   /**
    * Set the id of the workflow state to which the item was transitioned 
    * to prepare it for edit.
    * 
    * @param toStateId the new state id to which the item was transitioned to 
    *    prepared it for edit, may be <code>null</code>.
    */
   public void setToStateId(java.lang.Long toStateId) 
   {
       this.toStateId = toStateId;
   }

   @Override
   public boolean equals(Object b)
   {
      return EqualsBuilder.reflectionEquals(this, b);
   }

   @Override
   public int hashCode()
   {
      return HashCodeBuilder.reflectionHashCode(this);
   }

   @Override
   public String toString()
   {
      return ToStringBuilder.reflectionToString(this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#fromXML(String)
    */
   public void fromXML(String xmlsource) throws IOException, SAXException
   {
      PSXmlSerializationHelper.readFromXML(xmlsource, this);
   }

   /* (non-Javadoc)
    * @see IPSCatalogItem#toXML()
    */
   public String toXML() throws IOException, SAXException
   {
      return PSXmlSerializationHelper.writeToXml(this);
   }
}

