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

package com.percussion.cms.objectstore;

import com.percussion.cms.IPSCmsErrors;
import com.percussion.error.PSException;


/**
 * This class is used when a request for a content type does not exist or may
 * not be visible to the user.
 */
public class PSInvalidContentTypeException extends PSException
{
   /**
    * Creates an exception with text describing the problem.
    *
    * @param contentTypeName Displayed in the error message. If the name is
    *    not available, the id should be used. Never <code>null</code> or
    *    empty.
    */
   public PSInvalidContentTypeException(String contentTypeName)
   {
      //todo: put real error code here, where should it go?
      super( IPSCmsErrors.INVALID_CONTENT_TYPE_ID, contentTypeName);
      if ( null == contentTypeName || contentTypeName.trim().length() == 0  )
         throw new IllegalArgumentException("Type identifier must be supplied");
   }
}
