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
package com.percussion.services.assembly;

import com.percussion.rx.delivery.IPSDeliveryItem;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Each assembler creates an assembly result, one or more per content item being
 * assembled. An assembly result is a self contained unit that both contains and
 * describes the result of the assembly process.
 * <p>
 * For textual assemblers the mime type will normally be <i>text/html</i> or
 * <i>text/xml</i>. Binary assemblers may vary considerably, with typical
 * result types being <i>image/jpeg</i> or <i>application/pdf</i>.
 * <p>
 * While result data may be internally held in either the file system or memory,
 * it is at the choice of the caller whether to retrieve the contents via 
 * {@link #getResultData()} or {@link #getResultStream()}.
 * 
 * @author dougrand
 */
public interface IPSAssemblyResult extends IPSAssemblyItem, IPSDeliveryItem
{
   /**
    * The status values indicate whether this assembly was a failure or success.
    * If the result is a failure then the failure description is returned
    * as the result data.
    */
   public enum Status {
      /**
       * Assembly was successful for the given output result
       */
      SUCCESS, 
      /**
       * Assembly failed for the given output result
       */
      FAILURE
   }
   
   /**
    * Get the status of the result
    * 
    * @return the status value, never <code>null</code>
    * 
    * @deprecated use {@link IPSDeliveryItem#isSuccess()} instead.
    */
   Status getStatus();
   
   /**
    * Get the result data in stream form. This is preferred for large output
    * data to avoid having too much data in memory at any one moment. The result
    * data's format is dependent on the assembler used, and may be either binary
    * or text. The recipient may need to coerce the result to a particular format. 
    * <p>
    * After the returned stream is closed, any underlying data will be cleared.
    * So calling this method should be the last thing done with the assembly
    * results.
    * 
    * @return the result stream, the contents are entirely dependent on the
    *         assembler and may be either text or binary data. If the status is
    *         <code>FAILURE</code> then the result stream may be
    *         <code>null</code>, but should contain a user readable error
    *         message in UTF-8 encoding. The stream must be closed by the
    *         caller.
    */
   InputStream getResultStream();
   
   /**
    * Internally we need to obtain the results as a string for use in 
    * slot results. The returned string assumes that the character encoding
    * in the result's mimetype should be used in converting from the result
    * byte array back to a string.
    * 
    * @return a string representation of the result data, never 
    * <code>null</code>. 
    * @throws IllegalStateException if this is not a textual result
    * @throws UnsupportedEncodingException if the charset isn't supported
    */
   String toResultString() throws  IllegalStateException, 
      UnsupportedEncodingException;

   /**
    * Get the length in bytes of the result data. 
    * 
    * @return the length in bytes of the result data or <code>-1</code> if the
    * length cannot be determined.
    */
   long getResultLength();
   
   /**
    * Tells the caller whether this assembly item is paginated. Paginated items
    * are not processed, but rather queried and cloned. The item should define
    * the binding value $sys.pagecount, which will tell the system how many
    * clones to create. The clones will each have the binding $sys.page set to
    * inform the assembly system what page is being assembled.
    * @return <code>true</code> if this result should be paginated.
    */
   boolean isPaginated();
}
