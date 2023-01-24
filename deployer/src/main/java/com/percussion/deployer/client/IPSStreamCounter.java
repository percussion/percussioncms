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

package com.percussion.deployer.client;

/**
 * Wrapper for a stream that tracks the number of bytes written or read to or
 * from the stream.
 */
public interface IPSStreamCounter
{
   /**
    * Get the number of bytes written or read.
    * 
    * @return The number of bytes, always greater than or equal to zero.
    */
   public int getByteCount();
   
   /**
    * Attempts to close the underlying stream.  May be called while the stream
    * is being written to or read from, in order to abort that process, as long
    * as any exceptions resulting from the close are handled by the owner of the
    * stream.
    */
   public void closeStream();

}
