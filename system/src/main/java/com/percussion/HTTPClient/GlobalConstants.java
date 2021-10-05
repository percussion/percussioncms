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

package com.percussion.HTTPClient;


/**
 * This interface defines various global constants.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 * @since	V0.3
 */
@Deprecated
interface GlobalConstants
{
    /** possible http protocols we (might) handle */
    int     HTTP       = 0; 	// plain http
    int     HTTPS      = 1; 	// http on top of SSL
    int     SHTTP      = 2; 	// secure http
    int     HTTP_NG    = 3; 	// http next-generation

    /** some known http versions */
    int     HTTP_1_0   = (1 << 16) + 0;
    int     HTTP_1_1   = (1 << 16) + 1;

    /** Content delimiters */
    int     CD_NONE    = 0; 	// raw read from the stream
    int     CD_HDRS    = 1; 	// reading headers/trailers
    int     CD_0       = 2; 	// no body
    int     CD_CLOSE   = 3; 	// by closing connection
    int     CD_CONTLEN = 4; 	// via the Content-Length header
    int     CD_CHUNKED = 5; 	// via chunked transfer encoding
    int     CD_MP_BR   = 6; 	// via multipart/byteranges
}
