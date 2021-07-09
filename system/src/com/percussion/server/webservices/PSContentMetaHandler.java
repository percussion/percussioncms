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

package com.percussion.server.webservices;

import com.percussion.error.PSException;
import com.percussion.server.PSRequest;

import org.w3c.dom.Document;

/**
 * This class is used to handle all content data related operations for 
 * webservices. These operations are specified in the "ContentMeta" port in the
 * <code>WebServices.wsdl</code>.
 *
 * @See {@link com.percussion.hooks.webservices.PSWSContentMeta}.
 */
public class PSContentMetaHandler extends PSWebServicesBaseHandler
{
   /**
    * Return the content status of the specified content item.
    * 
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   void contentStatusAction(PSRequest request, Document parent)
      throws PSException
   {
      // check for existence first, throws error if not found
      // also sets the request with the content id and revision
      validateContentKey(request, true, false);

      processInternalRequest(request, CMS_CONTENT_STATUS, parent);
   }

   /**
    * Return the revision list of the specified content item.
    * 
    * @param request The original request for the operation, 
    *    assumed not <code>null</code>
    * @param parent The parent document to add the response element to,
    *    assumed not <code>null</code> and it will already contain the correct
    *    base element for the response
    * 
    * @throws PSException
    */
   void revisionListAction(PSRequest request, Document parent)
      throws PSException
   {
      processContentIdAction(WS_REVISIONLIST, request, parent);
   }

   /**
    * Name of the content status resource.
    */
   private static final String CMS_CONTENT_STATUS =
      "sys_psxCms/contentStatus.xml";

   /**
    * action string constants
    */
   private static final String WS_REVISIONLIST = "revisionList";
}
