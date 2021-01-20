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
package com.percussion.cms.objectstore;

import com.percussion.cms.PSCmsException;
import com.percussion.util.PSPurgableTempFile;

/**
 * This interface facilitates the access of binary data from the server.
 */
public interface IPSBinaryLocator extends Cloneable
{
   /**
    * Returns the actual binary data.
    *
    * @return The data as an <code>Object</code>.
    * @throws PSCmsException if content type cannot be located
    * @throws PSInvalidContentTypeException if content type is invalid.
    */
   public Object getData() throws PSCmsException,
      PSInvalidContentTypeException;

   /**
    * Get the temorary file holding the field data.
    * 
    * @return the temporary file, may be <code>null</code> if the field does
    *    not have any data.
    * @throws PSCmsException if content type cannot be located
    * @throws PSInvalidContentTypeException if content type is invalid.
    */
   public PSPurgableTempFile getDataFile() throws PSCmsException, 
      PSInvalidContentTypeException;
   
   /**
    * Implementers should override this method to provide a deep clone. See 
    * {@link Object#clone()} for more info.
    */
   public Object clone();
   
   /**
    * Implementers should override this method to  perform a logical 
    * equivalency, rather than requiring that the 2 refs point to the same 
    * object.  See {@link Object#equals(Object)} for more info.
    */
   public boolean equals(Object o);
   
   /**
    * Implementers should override this method to implement a behavior different
    * from the default implementation as necessary.  See 
    * {@link Object#hashCode()} for more info.
    */   
   public int hashCode();
}