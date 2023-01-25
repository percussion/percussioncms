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

import com.percussion.HTTPClient.HttpURLConnection;

import com.percussion.util.PSHtmlBodyInputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 *  This pre-exit is a derivative of the {@link PSGetBase64Encoded} with the
 *  only difference in that it only returns Base64 encoded <code>BODY</code>
 *  portion of the fetched HTML document.
 */
public class PSGetBase64EncodedBody extends PSGetBase64Encoded
{
   /**
    * Overridden method of the base class. Returns an InputStream
    * that only returns the data found in the <code>BODY</code> portion
    * of the HTTP response.
    *
    * @param conn HTTP connection, never <code>null</code>.
    * @return input stream with the response data, never <code>null</code>.
    * @throws IOException if for any reason it fails to get the input stream.
    */
   protected InputStream getInputStream(HttpURLConnection conn)
      throws IOException
   {
      if (conn==null)
         throw new IllegalArgumentException("conn may not be null");
      try(InputStream in = conn.getInputStream()) {
         return new PSHtmlBodyInputStream(in);
      }
   }
}
