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

package com.percussion.services.publisher.data;

import static org.apache.commons.lang.Validate.notEmpty;

import com.percussion.share.data.PSAbstractDataObject;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Encapsulates revision information for a page or asset including revision id, last time it was modified,
 * who modified it last, and its current state
 */
@XmlRootElement(name="ItemPublishingHistory")
public class PSItemPublishingHistory extends PSAbstractDataObject
{

   private String location;

   String server;

   Integer contentId;

   Integer revisionId;

   String errorMessage;

   String status;

   Date publishedDate;

   String operation;

   public String getOperation()
   {
      return operation;
   }

   public void setOperation(String operation)
   {
      this.operation = operation;
   }

   /**
    * Default constructor. For serializers.
    */
   public PSItemPublishingHistory()
   {
   }

   /**
    * Constructs an instance of the class.
    *
    */
   public PSItemPublishingHistory(Integer contentId, Integer revisionId, String server, Date publishedDate, String status, String operation, String errorMessage, String location)
   {
      notEmpty(server, "server");
      notEmpty(status, "status");

      this.publishedDate = publishedDate;
      this.server = server;
      this.errorMessage = errorMessage;
      this.status = status;
      this.operation = operation;
      this.contentId = contentId;
      this.revisionId = revisionId;
      this.location = location;
   }

   /**
    * 
    * @return error message, may be blank
    */
   public String getErrorMessage() {
      return errorMessage;
   }

   /**
    * Sets the error message may be blank.
    * 
    * @param errorMessage may be blank
    */
   public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
   }

   /**
    * Gets the name of the server.
    * 
    * @return server name, not blank.
    */
   public String getServer() {
      return server;
   }

   /**
    * Sets the server name.
    * 
    * @param server the name of the server, not blank.
    */
   public void setServer(String server) {
      this.server = server;
   }

   /**
    * Gets the published date of the item.
    *  
    * @return the published date, not null.
    */
   public Date getPublishedDate() {
      return publishedDate;
   }

   /**
    * Sets the published date of the item.
    * 
    * @param publishedDate the published date, should not be blank for an item.
    */
   public void setPublishedDate(Date publishedDate) {
      this.publishedDate = publishedDate;
   }

   /**
    * Gets the publishing status.
    * 
    * @return status, never blank
    */
   public String getStatus() {
      return status;
   }

   public Integer getContentId() {
      return contentId;
   }

   public void setContentId(Integer contentId) {
      this.contentId = contentId;
   }

   public Integer getRevisionId() {
      return revisionId;
   }

   public void setRevisionId(Integer revisionId) {
      this.revisionId = revisionId;
   }

   public String getLocation() {
      return location;
   }

   public void setLocation(String location) {
      this.location = location;
   }

   /**
    * Sets publishing status.
    * 
    * @param status the publishing status, never blank
    */
   public void setStatus(String status) {
      this.status = status;
   }
}
