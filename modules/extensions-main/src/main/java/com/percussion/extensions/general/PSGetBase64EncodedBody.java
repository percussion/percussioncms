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

      return new PSHtmlBodyInputStream(conn.getInputStream());
   }
}
