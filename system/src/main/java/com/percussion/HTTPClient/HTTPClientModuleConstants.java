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
 * This interface defines the return codes that the handlers in modules
 * may return.
 *
 * @see HTTPClientModule
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 * @since	V0.3
 */
@Deprecated
public interface HTTPClientModuleConstants
{
    // valid return codes for request handlers

    /** continue processing the request */
    int  REQ_CONTINUE   = 0;

    /** restart request processing with first module */
    int  REQ_RESTART    = 1;

    /** stop processing and send the request */
    int  REQ_SHORTCIRC  = 2;

    /** response generated; go to phase 2 */
    int  REQ_RESPONSE   = 3;

    /** response generated; return response immediately (no processing) */
    int  REQ_RETURN     = 4;

    /** using a new HTTPConnection, restart request processing */
    int  REQ_NEWCON_RST = 5;

    /** using a new HTTPConnection, send request immediately */
    int  REQ_NEWCON_SND = 6;


    // valid return codes for the phase 2 response handlers

    /** continue processing response */
    int  RSP_CONTINUE   = 10;

    /** restart response processing with first module */
    int  RSP_RESTART    = 11;

    /** stop processing and return response */
    int  RSP_SHORTCIRC  = 12;

    /** new request generated; go to phase 1 */
    int  RSP_REQUEST    = 13;

    /** new request generated; send request immediately (no processing) */
    int  RSP_SEND       = 14;

    /** go to phase 1 using a new HTTPConnection */
    int  RSP_NEWCON_REQ = 15;

    /** send request using a new HTTPConnection */
    int  RSP_NEWCON_SND = 16;
}
