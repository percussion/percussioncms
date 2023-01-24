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

package com.percussion.utils.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PSCopyStream
{

   /**
    * Constructor, make it private to make uninstantiable.
    */
   private  PSCopyStream()
   {
   }
  /**
   * Method to copy Java InputStream to PoutputStream.
   * 
   * @param in Input stream tp copy from, never <code>null</code>.
   *
   * @param out Output stream to copy to, never <code>null</code>.
   *
   * @return number bytes copied
   *
   * @throws IOException in case of any error while copying.
   *
   */
/**
* Helper method to copy an input stream to an output stream.
*
*/
	public static long copyStream(InputStream in, OutputStream out) 
      throws IOException
	{
		int nCopied = 0;
      final byte[] buffer = new byte[ DEFAULT_BUFFER_SIZE ];
      int n = 0;
      while( -1 != (n = in.read( buffer )) )
      {
          out.write( buffer, 0, n );
			 nCopied += n;
      }
		return nCopied;
	}
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
}

