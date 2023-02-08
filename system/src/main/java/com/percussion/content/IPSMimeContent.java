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
package com.percussion.content;

import java.io.InputStream;

/**
 * A self-describing content. This interface extends IPSMimeContentDescriptor,
 * which means it has all of the MIME typing, but with the added semantics
 * of representing open (or openable) resources.
 *
 */
public interface IPSMimeContent extends IPSMimeContentDescriptor
{
   /**
    * Returns the byte stream for this content. Transfers responsibility
    * for closing/cleaning up the stream to the caller. It is up to
    * the particular implementation to decide whether it is valid to
    * call this method more than once (getting more than one input stream),
    * and implementations should document accordingly.
    * <P>
    * Any transfer decoding that needs to be done should be done TO the
    * bytes returned from this stream.
    * 
    * This method must never return null. If the object is not properly
    * initialized, it must throw an IllegalStateException.
    *
    * @return   InputStream
    */
   public InputStream getContent() throws IllegalStateException;

   /**
    * Returns the approximate length of this content in bytes,
    * or -1 if not known.
    * <p>
    * Note that this return value is merely a hint for performance
    * optimization reasons, and it is <B>not</B> an error to read
    * more bytes from the stream than this length, if the bytes are
    * available.
    * 
    * @return   long The approximate content length if known, or -1
    * if not known.
    */
   public long getContentLength();

   /**
    * Returns the name of this content, or <CODE>null</CODE> if the
    * content has no name.
    * <p>
    * Note that the interpretation of the name depends on the context
    * in which this content was created. It may be a file name, or
    * it may simply be an unstructured descriptive name (such as
    * "My Blob").
    *
    * @return The name of the content. May be <CODE>null</CODE>.
    */
   public String getName();
}
