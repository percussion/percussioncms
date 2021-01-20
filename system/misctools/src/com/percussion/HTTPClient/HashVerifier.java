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

package com.percussion.HTTPClient;

import java.io.IOException;

/**
 * This interface defines a hash verifier.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 */
@Deprecated
interface HashVerifier
{
    /**
     * This method is invoked when a digest of a stream has been calculated.
     * It must verify that the hash (or some function of it) is correct and
     * throw an IOException if it is not.
     *
     * @param hash the calculated hash
     * @param len  the number of bytes read from the stream
     * @exception IOException if the verification fails.
     */
    public void verifyHash(byte[] hash, long len)  throws IOException;
}
