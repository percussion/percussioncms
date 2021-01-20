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
package com.percussion.sitemanage.service;

import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.data.PSPublishingAction;
import com.percussion.sitemanage.data.PSSitePublishResponse;

import java.util.List;

/**
 * The service used for site publishing operations.
 */
public interface IPSSitePublishService
{
    
    public static final String FULL_EDITION_SUFFIX = "_FULL";
    
   /**
    * The publishing action type. Used to indicate which edition will be
    * invoked.
    */
   public enum PubType {
      /**
       * Publishes all public site content.
       */
      FULL,
      /**
       * Publishes all public non-binary site content.
       */
      FULL_NONBINARY,
      /**
       * Incremental publish of all public site content.
       */
      INCREMENTAL,
      
      /**
       * Incremental publish of all staging site content.
       */
      STAGING_INCREMENTAL,
      
      /**
       * Publishes selected items immediately.
       */
      PUBLISH_NOW,
      /**
       * Takes selected items down from live site immediately.
       */
      TAKEDOWN_NOW,
      
      /**
       * Publishes selected item to staging server immediately
       */
      STAGE_NOW,
      
      /**
       * Removes selected item from staging server immediately 
       */
      REMOVE_FROM_STAGING_NOW;

      /**
       * Lookup value by ordinal
       * 
       * @param ordinal the ordinal
       * @return the matching enum value, or Full as a default
       */
      public static PubType valueOf(int ordinal)
      {
         for (PubType t : values())
         {
            if (t.ordinal() == ordinal)
            {
               return t;
            }
         }
         return FULL;
      }
   }

   /**
    * Helper method that calls {@link #publish(String, PubType, String, boolean, String)} by passing the right pub type.
    * @param siteName the site to be published or parent site of the page to be published.  Can be <code>null</code> for
    * resources.
    * @param id of the item to be published.
    * @param isResource <code>true</code> if the item is a resource (image, file), <code>false</code> otherwise.
    * @param server The name of the server, must not be blank for PubType {@link PubType.FULL}, ignored the passed in
    * server for other PubTypes and system uses the default server set on the site  
    * 
    * @return response object including the status of the completed publishing
    *  job.  See {@link PSSitePublishResponse} for details.
    * 
    * @throws PSSitePublishException if the requested publishing edition could
    *  not be found or if an invalid request is supplied for demand publishing.
    */
   public PSSitePublishResponse publishIncremental(String siteName, String id, boolean isResource, String server)
		      throws PSSitePublishException;

    /**
     * Helper method that calls {@link #publish(String, PubType, String, boolean, String)} by passing the right pub type.
     * @param siteName the site to be published or parent site of the page to be published.  Can be <code>null</code> for
     * resources.
     * @param id of the item to be published.
     * @param isResource <code>true</code> if the item is a resource (image, file), <code>false</code> otherwise.
     * @param server The name of the server, must not be blank for PubType {@link PubType.FULL}, ignored the passed in
     * server for other PubTypes and system uses the default server set on the site
     * @param itemsToApprove List of Items needs to be approved before publish
     *
     * @return response object including the status of the completed publishing
     *  job.  See {@link PSSitePublishResponse} for details.
     *
     * @throws PSSitePublishException if the requested publishing edition could
     *  not be found or if an invalid request is supplied for demand publishing.
     */
    public PSSitePublishResponse publishIncrementalWithApproval(String siteName, String id, boolean isResource, String server,String itemsToApprove)
            throws PSSitePublishException;
   
   /**
    * Invokes publishing for the specified site, type, and item.
    * 
    * @param siteName the site to be published or parent site of the page to be published.  Can be <code>null</code> for
    * resources.
    * @param type of publishing to perform.
    * @param id of the item to be published.
    * @param isResource <code>true</code> if the item is a resource (image, file), <code>false</code> otherwise.
    * @param server The name of the server, must not be blank for PubType {@link PubType.FULL}, ignored the passed in
    * server for other PubTypes and system uses the default server set on the site  
    * 
    * @return response object including the status of the completed publishing
    *  job.  See {@link PSSitePublishResponse} for details.
    * 
    * @throws PSSitePublishException if the requested publishing edition could
    *  not be found or if an invalid request is supplied for demand publishing.
    */
   public PSSitePublishResponse publish(String siteName, PubType type, String id, boolean isResource, String server)
      throws PSSitePublishException;
   /**
    * Returns the list of PSPublishingActionProperties available for the logged in user for the supplied item.
    * @param id The guid representation of the item id.
    * @return List of PSPublishingActionProperties
    * @throws PSSitePublishException, in case of an error.
    */
   public List<PSPublishingAction> getPublishingActions(String id)
   throws PSSitePublishException;
   
   /**
    * Get a paged list of items that are queued for incremental publish
    * 
    * @param siteName The name of the site that will be published, not <code>null<code/> or empty.
    * @param serverName The name of the server that will be published to, not <code>null<code/> or empty.
    * @param startIndex The starting index into the list to determine the page to return, if <= 0,  
    * the first page of results is returned
    * @param pageSize The max number of items to return per page, if <=0, all items will be returned in a single page
    * 
    * @return A paged item list with the specified page of items, not <code>null</code>, may be empty.
    * 
    * @throws PSSitePublishException If there are any errors.
    */
   PSPagedItemList getQueuedIncrementalContent(String siteName, String serverName, int startIndex, int pageSize) throws PSSitePublishException;
   
   PSPagedItemList getQueuedIncrementalRelatedContent(String siteName, String serverName, int startIndex, int pageSize) throws PSSitePublishException;
   
   /**
    * PSPublishException is thrown when an error occurs attempting to publish a
    * site.
    */
   public static class PSSitePublishException extends PSDataServiceException
   {

      /**
       * 
       */
      private static final long serialVersionUID = 1L;

      /**
       * Default constructor.
       */
      public PSSitePublishException()
      {
         super();
      }

      /**
       * Constructs an exception with the specified detail message and the
       * cause.
       * 
       * @param message the specified detail message.
       * @param cause the cause of the exception.
       */
      public PSSitePublishException(String message, Throwable cause)
      {
         super(message, cause);
      }

      /**
       * Constructs an exception with the specified detail message.
       * 
       * @param message the specified detail message.
       */
      public PSSitePublishException(String message)
      {
         super(message);
      }

      /**
       * Constructs an exception with the specified cause.
       * 
       * @param cause the cause of the exception.
       */
      public PSSitePublishException(Throwable cause)
      {
         super(cause);
      }
   }

}
