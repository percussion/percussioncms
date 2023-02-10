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
package com.percussion.extensions.general;

import com.percussion.data.PSConversionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;


/**
 * Strips leading and trailing white space from the supplied string.
 */
public class PSStringTrimmerUdf extends PSSimpleJavaUdfExtension
{
   /* ************ IPSUdfProcessor Interface Implementation ************ */

   /**
    * Strips leading and trailing white space from the supplied string. It
    * does this by calling <code>toString()</code> on the supplied object
    * first.
    *
    * @param params A single parameter that will be converted to a String
    *    with the toString method, then trim() is called on this string before
    *    it is returned. If <code>null</code> is supplied, <code>null</code>
    *    is returned.
    *
    * @param request   Not used.
    *
    * @return A copy of the supplied string with all leading and trailing
    *    white space removed.
    *
    * @throws  PSConversionException Never thrown.
    */
   @SuppressWarnings("unused") //param and exception
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      final int size = (params == null) ? 0 : params.length;

      if ( size == 0 || null == params[0] )
         return null;

      return params[0].toString().trim();
   }
}
