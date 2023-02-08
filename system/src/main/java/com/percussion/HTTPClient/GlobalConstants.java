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
