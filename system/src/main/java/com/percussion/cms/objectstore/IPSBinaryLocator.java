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
