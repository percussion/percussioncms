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
package com.percussion.rx.delivery;

import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.guid.IPSGuid;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * The delivery item interface describes the contract with items being given
 * to the delivery handlers. This has overlap with the assembly item and result
 * interfaces. 
 * <p>
 * The item is the result of the assembly process. The result data is from 
 * assembly and {@link #isSuccess()} references what happened during assembly.
 * 
 * TODO: Add methods to allow the recipient to ask for the stored result
 * in the least costly way 
 * 
 * @author dougrand
 *
 */
public interface IPSDeliveryItem
{   
   /**
    * Any bindings calculated by the assembly service can be retrieved via this
    * method. Note that only certain kinds of changes made in velocity templates
    * will appear here. Such changes must be to objects and not to the basic
    * variable bindings. Implementers would be well advised to limit
    * communication from assembly to delivery using bindings to those bindings
    * calculated in the bindings of the template.
    * 
    * @return empty if there are no bindings, otherwise a
    *         set of named values. Each value may contain an object, a sub map
    *         or a list of values.
    */
   Map<String, Object> getBindings();
   
   /**
    * Metadata to be delivered to the metadata service for item.  This
    * is extracted from the "$perc.metadata" binding
    * @return null if there is no metadata defined
    */
   
   Map<String, Object> getMetaData();
   
   /**
    * Get the delivery type information. A delivery type is the name of a 
    * delivery handler. Delivery types are configured through the system's
    * publishing design UI. Each handler can have more than one type associated
    * with it, configuring the handler for a specific use. 
    * <p>
    * The delivery type name is interpreted by the delivery manager to determine
    * what delivery handler Spring bean to use.
    * 
    * @return the delivery type, never <code>null</code> or empty.
    */
   String getDeliveryType();
   
   /**
    * Get the delivery path. The delivery path is really only relevant for
    * the file based publisher plug ins.
    * 
    * @return the path, may be <code>null</code> or empty.
    */
   String getDeliveryPath();
   
   /**
    * Identify which item this result is for. The id is the GUID of the
    * original primary content item. Other items may have been used in the
    * assembly, but will not be included here.
    * 
    * @return the item's id, never <code>null</code>
    */
   IPSGuid getId();

   /**
    * Each delivery item has an associated mime type. The mime type will
    * dictate the content and format of the result data.
    * 
    * @return the mime type of the result, never <code>null</code>
    */
   String getMimeType();

   /**
    * The result data's format is dependent on the assembler used. The recipient
    * may need to coerce the result to a particular format. For example, an XSL
    * assembler will often produce a document or element as the output, whereas
    * a Velocity assembler will produce text.
    * <p>
    * For items being unpublished, the result data holds the unpublishing 
    * information.
    * 
    * @return the result data, the contents are entirely dependent on
    *         the assembler and may be either text or binary data. 
    *         If {@link #isSuccess()} return false then the
    *         result data may be <code>null</code>, but should contain a user
    *         readable error message in UTF-8 encoding.
    */
   byte[] getResultData();  
   
   /**
    * Get the result stored in a temp file. If the result is already in a temp
    * file then that file will be returned. The returned file must be managed
    * by the caller. Multiple calls to this method will return the same
    * file instance.
    * 
    * @return the temp file, never <code>null</code>.
    * @throws IOException if the temp file cannot be written.
    */
   PSPurgableTempFile getResultFile() throws IOException;
   
   /**
    * Get the result data in stream form. This is preferred for large output
    * data to avoid having too much data in memory at any one moment. The result
    * data's format is dependent on the assembler used. The recipient may need
    * to coerce the result to a particular format. For example, an XSL assembler
    * will often produce a document or element as the output, whereas a Velocity
    * assembler will produce text.
    * <p>
    * After the returned stream is closed, any underlying data will be cleared.
    * So calling this method should be the last thing done with the result info.
    * 
    * @return the result stream, the contents are entirely dependent on the
    *         assembler and may be either text or binary data. If the status is
    *         <code>FAILURE</code> then the result stream may be
    *         <code>null</code>, but should contain a user readable error
    *         message in UTF-8 encoding.
    */
   InputStream getResultStream();

   /**
    * Get the time that it took for this work item to be assembled. Set by 
    * the assembly plug in. 
    * 
    * @return the time in milliseconds.
    */
   int getElapsed();
   
   /**
    * This result is a success result.
    * 
    * @return <code>true</code> for a success, <code>false</code> for a failure.
    */
   boolean isSuccess();
   
   /**
    * The reference id identifies a particular assembly request within a given
    * job. The reference id allows a caller to associate assembly results and
    * requests. While reference ids could be reused across preview requests,
    * they will never be repeated for publishing jobs. This id is used for the
    * primary key in the item status table.
    * 
    * @return the id, may be any value, but unique for a given job id
    */
   long getReferenceId();

   /**
    * The job id is unique per publishing run and helps a caller associate
    * results and requests for a given run. This id is also used internally to
    * determine if certain cached values can be reused, therefore this value
    * should be changed for each new assembly job, which includes new previews.
    * The job id is used as a primary key for the job status table.
    * 
    * @return the job id, may be any value, but different for each run
    */
   long getJobId();
   
   /**
    * Get the site id, if defined, for the assembly item. The site id is
    * extracted from the site id http parameter. Note that if the item is
    * created for a slot, then the site id may be the site id of the referenced
    * site rather than the original site.
    * 
    * @return the site id, or <code>null</code> if the parameter is missing
    */
   IPSGuid getSiteId();
   
   
   /**
    * Set the publishing server id to use with the delivery item.
    * @param pubserverid the ID of the publishing server.
    * It may be <code>null</code> if the publish-server is unknown.
    */
   void setPubServerId(Long pubserverid);
   
   /**
    * Get the publishing server id that is used for this item.
    * @return publishing server id. It is <code>null</code> if the publish-server is unknown.
    */
   Long getPubServerId();
         
   /**
    * Indicates if this assembly is for a publishing or unpublishing case. This
    * will return <code>true</code> for publishing or previews. The value
    * <code>false</code> can be used by the assembly plug in to short circuit
    * processing if the delivery type in use does not require assembly for 
    * unpublishing.
    * 
    * @return <code>false</code> for unpublishing
    */
   boolean isPublish();
   
   /**
    * If this is for a paged item's page, then this will return the page
    * number. Page numbers are <code>1</code> based. If the item contains
    * a page number, it will also contain a parent reference id.
    * 
    * @return the page number or <code>null</code> if there is no page number.
    */
   Integer getPage();
   
   /**
    * If this is for a paged item's page, then this will return the 
    * original paginated page's reference id so that status update can mark
    * the parent as failed if the child page fails.
    * 
    * @return the parent reference id or <code>null</code> if this is not
    * a page child item.
    */
   Long getParentPageReferenceId();
   
   /**
    * Sets the temporary directory for storing the result data before
    * deliver to the target location.
    * 
    * @param tmpDir the temporary directory. It may be <code>null</code> if 
    *    uses default temporary directory.
    */
   void setTempDir(File tmpDir);
   
   /**
    * Free up resources. This is called by the publishing engine to free up
    * resources after the item is no longer needed.
    */
   void clearResults();
   
   int getDeliveryContext();

}
