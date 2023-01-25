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
