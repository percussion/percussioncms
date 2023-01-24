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
