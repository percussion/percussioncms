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

