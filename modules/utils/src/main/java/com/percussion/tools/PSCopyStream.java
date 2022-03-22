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

package com.percussion.tools;

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

