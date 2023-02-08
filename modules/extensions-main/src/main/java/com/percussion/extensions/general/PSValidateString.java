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

import org.apache.commons.lang.StringUtils;

import com.percussion.data.PSConversionException;
import com.percussion.extension.PSSimpleJavaUdfExtension;
import com.percussion.server.IPSRequestContext;


/**
 * Checks if a string is empty, null, or whitespace.
 */
public class PSValidateString extends PSSimpleJavaUdfExtension
{
  /* ************ IPSUdfProcessor Interface Implementation ************ */

  /**
   * Checks if the supplied string is empty, null, or whitespace. It
   * does this by calling <code>toString()</code> on the supplied object first.
   *
   * @param params A single parameter that will be converted to a String
   *   with the toString method, then <code>trim()</code> is called on this string.
   *
   * @param request Not used.
   *
   * @return An Object of type Boolean which is <code>false</code> if the string 
   * is invalid (empty, null, or whitespace), <code>true</code> otherwise.
   *
   * @throws  PSConversionException Never thrown.
   */
   public Object processUdf(Object[] params, IPSRequestContext request)
      throws PSConversionException
   {
      return params[0] != null && StringUtils.isNotBlank(params[0].toString());
   }
}
