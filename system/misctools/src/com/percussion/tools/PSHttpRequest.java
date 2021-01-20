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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.tools;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

/**
 * Encapsulates an HTTP GET or POST request and the results of the request.
 * We use this class because we want more flexibility over the behavior
 * of the HTTP transaction than classes like java.net.HttpURLConnection
 * gives us. For example, we may want to send garbage or unencoded URLs.
 *
 */
public class PSHttpRequest implements IPSHTTPConstants
{

	public PSHttpRequest(URL url)
	{
		this(url.toString(), "GET", null);
	}

	/**
	 * Construct a request to the given URL with the given method. If request
	 * content is non-null, then the request content will be after the request
	 * and the request headers.
	 * <p>
	 * If you happen to know the length (in bytes) of the content, you may want
	 * to set the "Content-length" request header to that value. We will not do it
	 * automatically, and we will <B>not</B> consider the setting of this header
	 * when reading the content stream.
	 * <p>
	 * Also, if you happen to know the character encoding that the content uses,
	 * you may want to set the "Char-encoding" request header. Again, we will not
	 * consider this value when sending the content.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * 
	 * @param	URL
	 * @param	reqMethod
	 * @param	content May be null if no POST data is needed.
	 * 
	 */
	public PSHttpRequest(
		String URL,
		String reqMethod,
		InputStream reqContent)
	{
		init(URL, reqMethod, reqContent);
	}

	private void init(String URL, String reqMethod, InputStream reqContent)
	{
		m_reqURL = URL;
		m_reqMethod = reqMethod;
		m_reqContent = reqContent;
	}

	/**
	 * Sets the outgoing request content for this request. If an existing
	 * content had been specified (and it is not the same content stream
	 * as the argument to this method), the existing content will be
	 * closed first.
	 *
	 * @author	chadloder
	 * 
	 * @version 1.3 1999/11/03
	 * 
	 * @param	content
	 * 
	 */
	public void setRequestContent(InputStream content)
	{
		if (m_reqContent != null && content != m_reqContent)
		{
			try
			{
				m_reqContent.close();
			}
			catch (IOException e)
			{
				/* ignore */
			}
		}
		m_reqContent = content;
	}

	/**
	 * Sets the request's hostname. Usually this value will be extracted from
	 * the URL, but if the host is not available in the URL, then you will to
	 * set the host using this method.
	 * <p>
	 * If this value is set, then it will override any setting in the URL.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * 
	 * @param	hostName
	 * 
	 */
	public void setRequestHost(String hostName)
	{
		m_reqHost = hostName;
	}

	/**
	 * Sets the request's port. Usually this value will be extracted from
	 * the URL, but if the port is not available in the URL, then you will need
	 * to set the port using this method.
	 * <p>
	 * If this value is set, then it will override any setting in the URL.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * 
	 * @param	port
	 * 
	 */
	public void setRequestPort(int port)
	{
		m_reqPort = port;
	}

	/**
	 * Gets the request method, usually "GET" or "POST".
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * @return	String
	 */
	public String getRequestMethod()
	{
		return m_reqMethod;
	}

	/**
	 * Enables tracing status to the given PrintWriter.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * 
	 * @param	logger
	 * 
	 */
	public void enableTrace(LogSink logger)
	{
		m_logger = logger;
	}

	/**
	 * Sets the HTTP version to declare when making a request. The
	 * default is 1.0
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * @param	major
	 * @param	minor
	 * 
	 */
	public void setRequestHttpVersion(int major, int minor)
	{
		m_reqHttpVersion = "" + major + "." + minor;
	}

	/**
	 * Sets the request URL.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * @param	URL
	 * 
	 */
	public void setRequestURL(String URL)
	{
		m_reqURL = URL;
	}

	public void addRequestHeaders(PSHttpHeaders headers)
	{
		m_reqHeaders.addAll(headers);
	}

	/**
	 * Adds a header that will be sent along with the request.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * 
	 * @param	headerName
	 * @param	headerValue
	 * 
	 */
	public void addRequestHeader(String headerName, String headerValue)
	{
		m_reqHeaders.replaceHeader(headerName, headerValue);
	}

	/**
	 * Adds a response header that was present in the response.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * @param	headerName
	 * @param	headerValue
	 * 
	 */
	protected void addResponseHeader(String headerName, String headerValue)
	{
		m_respHeaders.addHeader(headerName, headerValue);
	}

	/**
	 * Returns the response headers.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * @return	Iterator
	 */
	public PSHttpHeaders getResponseHeaders()
	{
		return m_respHeaders;
	}

	/**
	 * Sends the request and parses the response. If request content was
	 * supplied in the constructor, it will be sent.
	 * <P>
	 * The request content stream (if specified in the constructor) is
	 * guaranteed to be closed after this method is called, even if
	 * exceptions are thrown from this method.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * @throws	IOException
	 * 
	 */
	public void sendRequest() throws IOException
	{
		PSHttpRequestTimings timings = new PSHttpRequestTimings();
		sendRequest(timings);
		m_timings = timings;
	}

	/**
	 * Sends the request and parses the response. If request content was
	 * supplied in the constructor, it will be sent.
	 * <P>
	 * The request content stream (if specified in the constructor) is
	 * guaranteed to be closed after this method is called, even if
	 * exceptions are thrown from this method.
	 *
	 * @param timer Can be <CODE>null</CODE>.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * @throws	IOException
	 * 
	 */
	private void sendRequest(PSHttpRequestTimings timings) throws IOException
	{
		try
		{
			// TODO: don't do this if we support keep-alive connections
			if (m_sock != null)
			{
				disconnect();
			}

			String host = m_reqHost;
			if (host == null)
			{
				URL u = new URL(m_reqURL);
				host = u.getHost();
			}

			int port = m_reqPort;
			if (port <= 0)
			{
				URL u = new URL(m_reqURL);
				port = u.getPort();
			}

			if (port <= 0)
			{
				port = 80;
			}

			timings.beforeConnect(System.currentTimeMillis());

			// connect the socket
			connect(host, port);

			timings.afterConnect(System.currentTimeMillis());

			OutputStream out = m_sock.getOutputStream();

			m_reqWriter = new BufferedWriter(
				new OutputStreamWriter(out));

			// send the request line
			sendRequestLine(m_reqWriter);

			// send the request headers
			sendRequestHeaders(m_reqWriter);

			m_reqWriter.flush();

			// if applicable, send the additional content
			if (m_reqContent != null)
			{
				sendReqContent(m_reqContent, out);
				m_reqContent.close();
				m_reqContent = null;
			}

			timings.afterRequest(System.currentTimeMillis());

			// prepare to read the response
			m_respIn = new PSInputStreamReader(m_sock.getInputStream());

			// read the response header
			long hdrBytes = parseResponse(m_respIn);

			timings.afterHeaders(System.currentTimeMillis());
			timings.headerBytes(hdrBytes);
		}
		finally
		{
			if (m_reqContent != null)
			{
				m_reqContent.close();
				m_reqContent = null;
			}
		}	
	}

	protected void connect(String host, int port) throws IOException
	{
		log("Connecting...");
		m_sock = new Socket(host, port);
	}

	protected void sendReqContent(InputStream in, OutputStream out)
		throws IOException
	{
		long bytesSent = PSCopyStream.copyStream(in, out);
		log("Sent " + bytesSent + " bytes of content");
	}

	/**
	 * Gets the approximate number of milliseconds we had to wait
	 * for the first byte of the response to become available from the
	 * server.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * @return	long
	 */
	public long getResponseLatency()
	{
		return m_respLatencyMs;
	}

	public PSHttpRequestTimings getTimings() throws CloneNotSupportedException
	{
		return (PSHttpRequestTimings)m_timings.clone();
	}

	/**
	 * Gets the response content stream, which may be null
	 * or empty if getResponseCode() returns anything other
	 * than 2xx. If we are currently waiting for data to
	 * become available over the connection, this method
	 * will block until either we have timed out or until
	 * data becomes available.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * @return	InputStream
	 */
	public InputStream getResponseContent()
	{
		return m_respIn;
	}

	/**
	 * Gets the HTTP response code.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * @return	int
	 */
	public int getResponseCode()
	{
		return m_respHttpCode;
	}

	/**
	 * Closes the request. Any pending results are discarded,
	 * and the response content is no longer valid.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * 
	 * @throws	Exception;
	 * 
	 */
	public void disconnect() throws IOException
	{
		if (m_respIn != null || m_sock != null)
		{
			log("Disconnecting...");
		}

		if (m_respIn != null)
			m_respIn.close();

		if (m_sock != null)
			m_sock.close();

		m_respIn = null;
		m_sock = null;
	}

	public void finalize() throws Throwable
	{
		super.finalize();
		disconnect();
	}

	/**
	 * Gets the response message, that is any text following the status code on the
	 * response header line.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * @return	String
	 */
	public String getResponseMessage()
	{
		return m_respMsg;
	}

	/** send the HTTP request line to the given writer */
	protected void sendRequestLine(Writer writer) throws IOException
	{
		// this logic allows us to send invalid URLs
		String sendURL = m_reqURL;
		try
		{
			URL u = new URL(m_reqURL);
			sendURL = u.getFile();
		}
		catch (MalformedURLException e)
		{
			// ignrore, just send the invalid URL as is
		}

		String reqLine = m_reqMethod + " " + sendURL + " HTTP/" + m_reqHttpVersion;
		writer.write(reqLine + "\r\n");

		log("Sent request line " + reqLine);
	}

	/** sends the request headers to the given writer, followed by a blank line */
	protected void sendRequestHeaders(Writer writer) throws IOException
	{
		log("Sending request headers...");
		Collection keySet = m_reqHeaders.getHeaderNames();
		for (Iterator i = keySet.iterator(); i.hasNext(); )
		{
			String headerName = i.next().toString();
			for (Iterator j = m_reqHeaders.getHeaders(headerName); j.hasNext(); )
			{
				String val = headerName + ": " + j.next().toString();
				writer.write(val + "\r\n");
				log("\tSent header " + val);
			}
		}

		writer.write("\r\n"); // blank line to terminate the headers
		
		log("Finished sending headers");
	}

	protected long parseResponse(PSInputStreamReader reader)
		throws IOException
	{
		long bytes = parseResponseStatus(reader);
		bytes += parseResponseHeaders(reader);
		return bytes;
	}

	/**
	 * Read the HTTP status code, which looks like "HTTP/1.1 nnn:blah"
	 * where nnn is the code
	 */
	protected long parseResponseStatus(PSInputStreamReader reader)
		throws IOException
	{
		String statusLine = reader.readLine();
		
		log("Server status: " + statusLine);
		
		{
			if (statusLine == null || statusLine.length() < 8)
			{
				throw new IOException("Malformed HTTP status line \"" + statusLine + "\"");
			}

			int spacePos = statusLine.indexOf(' ');
			if (spacePos < 5)
			{
				throw new IOException("Malformed HTTP status line \"" + statusLine + "\"");
			}

			int startCode = spacePos + 1; // points at first char of code
			int endCode = startCode + 3; // points one past the code
			m_respHttpCode = Integer.parseInt(statusLine.substring(startCode, endCode));
			m_respMsg = statusLine.substring(endCode).trim();
		}

		// status line is ASCII bytes (don't forget 2 bytes for CR+LF)
		return statusLine.length() + 2;
	}

	/**
	 * Parses the headers, and positions the reader
	 * on first byte of actual data.
	 *
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 */
	protected long parseResponseHeaders(PSInputStreamReader reader) throws IOException
	{
		long bytes = 0L;

		log("Parsing response headers...");

		// now read each header
		for (String line = reader.readLine(); line != null; line = reader.readLine())
		{
			// line is ASCII bytes (don't forget 2 bytes for CR+LF)
			bytes += line.length() + 2;

			if (line.length() == 0)
			{
				// this is the last (empty) line in the headers
				break;
			}

			int pos = line.indexOf(':');
			if (pos < 1 || pos == line.length())
			{
				throw new IOException("Malformed result header line \"" + line + "\"");
			}

			log("\tParsed header " + line);

			String name = line.substring(0, pos).trim();
			String val = line.substring(pos + 1, line.length()).trim();

			addResponseHeader(name, val);
		}

		log("Finished parsing response headers");

		return bytes;
	}

	public void logException(Throwable t)
	{
		if (m_logger != null)
			m_logger.log(t);
	}

	public void log(String message)
	{
		if (m_logger != null)
			m_logger.log(message);
	}

	protected Socket m_sock;
	protected LogSink m_logger;

	/* response */
	protected long m_respLatencyMs;
	protected String m_respMsg;
	protected PSInputStreamReader m_respIn;
	protected int m_respHttpCode = -1;
	protected PSHttpHeaders m_respHeaders = new PSHttpHeaders();

	/* request */
	protected String m_reqHost;
	protected int m_reqPort = -1;
	protected String m_reqMethod;
	protected InputStream m_reqContent;
	protected Writer m_reqWriter;
	protected PSHttpHeaders m_reqHeaders = new PSHttpHeaders();
	protected String m_reqHttpVersion = "1.0";
	protected String m_reqURL;

	/* statistics */
	protected PSHttpRequestTimings m_timings;
}
