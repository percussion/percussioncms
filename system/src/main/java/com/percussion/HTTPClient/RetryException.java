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
 * Signals that an exception was thrown and caught, and the request was
 * retried.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 */
@Deprecated
class RetryException extends IOException
{
    /** the request to retry */
    Request     request  = null;

    /** the response associated with the above request */
    Response    response = null;

    /** the start of the liked list */
    RetryException first = null;

    /** the next exception in the list */
    RetryException next  = null;

    /** the original exception which caused the connection to be closed. */
    IOException exception = null;

    /** was this exception generated because of an abnormal connection reset? */
    boolean conn_reset = true;

    /** restart processing? */
    boolean restart = false;


    /**
     * Constructs an RetryException with no detail message.
     * A detail message is a String that describes this particular exception.
     */
    public RetryException()
    {
	super();
    }


    /**
     * Constructs an RetryException class with the specified detail message.
     * A detail message is a String that describes this particular exception.
     *
     * @param s the String containing a detail message
     */
    public RetryException(String s)
    {
	super(s);
    }


    // Methods

    /**
     * Inserts this exception into the list.
     *
     * @param re the retry exception after which to add this one
     */
    void addToListAfter(RetryException re)
    {
	if (re == null)  return;

	if (re.next != null)
	    this.next = re.next;
	re.next = this;
    }
}
