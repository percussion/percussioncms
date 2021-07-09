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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
