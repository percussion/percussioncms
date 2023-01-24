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
import org.apache.commons.lang.StringUtils;

/**
 * Indicates that an invalid complex child field name has been specified.
 */
public class PSInvalidChildTypeException extends PSException
{
   /**
    * For java serialization
    */
   private static final long serialVersionUID = 1L;

   /**
    * Construct the exception with the default message.
    * 
    * @param childName The invalid child type name, may not be <code>null</code> 
    * or empty.
    * @param contentType The content type name or id, may not be 
    * <code>null</code> or empty.
    */
   public PSInvalidChildTypeException(String childName, String contentType)
   {
      super(IPSCmsErrors.INVALID_CHILD_TYPE, 
         new String[] {childName, contentType});
      
      if (StringUtils.isBlank(childName))
         throw new IllegalArgumentException(
            "childName may not be null or empty");
      if (StringUtils.isBlank(contentType))
         throw new IllegalArgumentException(
            "contentType may not be null or empty");
   }
}

