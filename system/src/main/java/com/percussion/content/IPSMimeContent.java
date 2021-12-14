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
