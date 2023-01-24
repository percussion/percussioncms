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
 * This interface represents the read-only interface of an http request.
 * It is the compile-time type passed to various handlers which might
 * need the request info but musn't modify the request.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 */
@Deprecated
public interface RoRequest
{
    /**
     * @return the HTTPConnection this request is associated with
     */
    public HTTPConnection getConnection();

    /**
     * @return the request method
     */
    public String getMethod();

    /**
     * @return the request-uri
     */
    public String getRequestURI();

    /**
     * @return the headers making up this request
     */
    public NVPair[] getHeaders();

    /**
     * @return the body of this request
     */
    public byte[] getData();

    /**
     * @return the output stream on which the body is written
     */
    public HttpOutputStream getStream();

    /**
     * @return true if the modules or handlers for this request may popup
     *         windows or otherwise interact with the user
     */
    public boolean allowUI();
}
