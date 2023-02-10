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
package com.percussion.tools;

/**
 * Timing statistics for an HTTP request.
 * <P>
 * All timing values specify the actual system time for that event, <B>not</B>
 * durations. It is important for us to keep actual times so that we may
 * compile request statistics from multiple sources, correlating the times.
 *
 */
public class PSHttpRequestTimings implements Cloneable
{
	public PSHttpRequestTimings()
	{
	}

	/**
	 * Gets the before connect time. The before connect time
	 * is the time before the socket has been opened.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/10/25
	 * 
	 * @return	long The before connect time in milliseconds elapsed since
	 * midnight, January 1, 1970 UTC.
	 */
	public long beforeConnect()
	{
		return m_beforeConnect;
	}

	/**
	 * Sets the before connect time. The before connect time
	 * is the time before the socket has been opened.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/10/25
	 * 
	 * @param	time The before connect time in milliseconds elapsed since
	 * midnight, January 1, 1970 UTC.
	 */
	public void beforeConnect(long time)
	{
		m_beforeConnect = time;
	}

	/**
	 * Gets the after connect time.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/10/25
	 * 
	 * @return	long The after connect time, in milliseconds elapsed since
	 * midnight, January 1, 1970 UTC.
	 */
	public long afterConnect()
	{
		return m_afterConnect;
	}

	/**
	 * Sets the after connect time. The after connect time
	 * is the time after the socket has been opened but before any
	 * data has been sent or received.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/10/25
	 * 
	 * @param	time The after connect time in milliseconds elapsed since
	 * midnight, January 1, 1970 UTC.
	 */
	public void afterConnect(long time)
	{
		m_afterConnect = time;
	}

	public long afterRequest()
	{
		return m_afterRequest;
	}

	public void afterRequest(long time)
	{
		m_afterRequest = time;
	}

	public long afterHeaders()
	{
		return m_afterHeaders;
	}

	public void afterHeaders(long time)
	{
		m_afterHeaders = time;
	}

	public long afterContent()
	{
		return m_afterContent;
	}

	public void afterContent(long time)
	{
		m_afterContent = time;
	}

	public long headerBytes()
	{
		return m_headerBytes;
	}

	public void headerBytes(long bytes)
	{
		m_headerBytes = bytes;
	}

	public long contentBytes()
	{
		return m_contentBytes;
	}

	public void contentBytes(long bytes)
	{
		m_contentBytes = bytes;
	}

	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	/** this is set before the socket is opened */
	private long m_beforeConnect = 0L;

	/** this is set after the socket is opened, before any data
	 *  has been sent or received */
	private long m_afterConnect  = 0L;

	/** this is set after the request has been sent, before any
	 *  data has been received */
	private long m_afterRequest  = 0L;

	/** this is set after all the response headers have been
	 *  received, before any body data has been received */
	private long m_afterHeaders  = 0L;

	/** this is set after all of the response content has
	 *  been read */
	private long m_afterContent   = 0L;

	/** this is the number of header bytes returned,
	 *  including the HTTP status line */
	private long m_headerBytes   = 0L;

	/** this is the number of content bytes returned after
	 *  the last header */
	private long m_contentBytes  = 0L;
}
