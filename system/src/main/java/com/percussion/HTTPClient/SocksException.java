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

import java.io.IOException;

/**
 * Signals that an error was received while trying to set up a connection
 * with the Socks server.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 */
@Deprecated
public class SocksException extends IOException
{
    /**
     * Constructs a SocksException with no detail message.
     * A detail message is a String that describes this particular exception.
     */
    public SocksException()
    {
	super();
    }


    /**
     * Constructs a SocksException with the specified detail message.
     * A detail message is a String that describes this particular exception.
     *
     * @param s the String containing a detail message
     */
    public SocksException(String s)
    {
	super(s);
    }

}
