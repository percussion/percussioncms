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

package com.percussion.HTTPClient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * This is a simple logger for the HTTPClient. It defines a number of
 * "facilities", each representing one or more logically connected classes.
 * Logging can be enabled or disabled on a per facility basis. Furthermore, the
 * logging output can be redirected to any desired place, such as a file or a
 * buffer; by default all logging goes to <var>System.err</var>.
 *
 * <P>All log entries are preceded by the name of the currently executing
 * thread, enclosed in {}'s, and the current time in hours, minutes, seconds,
 * and milliseconds, enclosed in []'s. Example:
 * <tt>{Thread-5} [20:14:03.244] Conn:  Sending Request</tt>
 *
 * <P>When the class is loaded, two java system properties are read:
 * <var>HTTPClient.log.file</var> and <var>HTTPClient.log.mask</var>. The first
 * one, if set, causes all logging to be redirected to the file with the given
 * name. The second one, if set, is used for setting which facilities are
 * enabled; the value must be the bitwise OR ('|') of the values of the desired
 * enabled facilities. E.g. a value of 3 would enable logging for the
 * HTTPConnection and HTTPResponse, a value of 16 would enable cookie related
 * logging, and a value of 8 would enable authorization related logging; a
 * value of -1 would enable logging for all facilities. By default logging is
 * disabled.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 * @since	V0.3-3
 */
@Deprecated
public class Log
{
	private static final Logger log = LogManager.getLogger(Log.class);

    /** The HTTPConnection facility (1) */
    public static final int CONN  = 1 << 0;
    /** The HTTPResponse facility (2) */
    public static final int RESP  = 1 << 1;
    /** The StreamDemultiplexor facility (4) */
    public static final int DEMUX = 1 << 2;
    /** The Authorization facility (8) */
    public static final int AUTH  = 1 << 3;
    /** The Cookies facility (16) */
    public static final int COOKI = 1 << 4;
    /** The Modules facility (32) */
    public static final int MODS  = 1 << 5;
    /** The Socks facility (64) */
    public static final int SOCKS = 1 << 6;
    /** The ULRConnection facility (128) */
    public static final int URLC  = 1 << 7;
    /** All the facilities - for use in <code>setLogging</code> (-1) */
    public static final int ALL   = ~0;

    private static final String NL     = System.getProperty("line.separator");
    private static final long   TZ_OFF;

    private static int     facMask     = 0;
    private static boolean closeWriter = false;


    static
    {
	Calendar now = Calendar.getInstance();
	TZ_OFF = TimeZone.getDefault().getOffset(now.get(Calendar.ERA),
						 now.get(Calendar.YEAR),
						 now.get(Calendar.MONTH),
						 now.get(Calendar.DAY_OF_MONTH),
						 now.get(Calendar.DAY_OF_WEEK),
						 now.get(Calendar.MILLISECOND));

	try
	{
	    String file = System.getProperty("com.percussion.HTTPClient.log.file");
	    if (file != null)
	    {
		try
		    { setLogWriter(new FileWriter(file), true); }
		catch (IOException ioe)
		{
		    System.err.println("failed to open file log stream `" +
				       file + "': " + ioe);
		}
	    }
	}
	catch (Exception e)
	    { }

	try
	{
	    facMask = Integer.getInteger("com.percussion.HTTPClient.log.mask", 0).intValue();
	}
	catch (Exception e)
	    { }
    }


    // Constructors

    /**
     * Not meant to be instantiated
     */
    private Log()
    {
    }


    // Methods

    /**
     * Write the given message to the current log if logging for the given facility is
     * enabled.
     *
     * @param facility  the facility which is logging the message
     * @param msg       the message to log
     */
    @Deprecated
    public static void write(int facility, String msg)
    {
		log.info(msg);
    }

    /**
     * Write the stack trace of the given exception to the current log if logging for the
     * given facility is enabled.
     *
     * @param facility  the facility which is logging the message
     * @param prefix    the string with which to prefix the stack trace; may be null
     * @param t         the exception to log
     */
    @Deprecated
    public static void write(int facility, String prefix, Throwable t)
    {
    	log.info(t.getMessage());
    }

    /**
     * Write the contents of the given buffer to the current log if logging for
     * the given facility is enabled.
     *
     * @param facility  the facility which is logging the message
     * @param prefix    the string with which to prefix the buffer contents;
     *                  may be null
     * @param buf       the buffer to dump
     */
    @Deprecated
    public static void write(int facility, String prefix, ByteArrayOutputStream buf)
    {
    	//If we are writing bytebuffers assume this is debug code
		log.debug(buf);
    }

    /**
     * Write a log line prefix of the form
     * <PRE>
     *  {thread-name} [time]
     * </PRE>
     */
    @Deprecated
    private static final void writePrefix() throws IOException {
		//Do nada
    }

    private static final String fill2(int num) {
	return ((num < 10) ? "0" : "") + num;
    }

    private static final String fill3(int num) {
	return ((num < 10) ? "00" : (num < 100) ? "0" : "") + num;
    }

    /**
     * Check whether logging for the given facility is enabled or not.
     *
     * @param facility  the facility to check
     * @return true if logging for the given facility is enable; false otherwise
     */
    @Deprecated
    public static boolean isEnabled(int facility)
    {
	return ((facMask & facility) != 0);
    }

    /**
     * Enable or disable logging for the given facilities.
     *
     * @param facilities the facilities for which to enable or disable logging.
     *                   This is bitwise OR ('|') of all the desired
     *                   facilities; use {@link #ALL ALL} to affect all facilities
     * @param enable     if true, enable logging for the chosen facilities; if
     *                   false, disable logging for them.
     */
    @Deprecated
    public static void setLogging(int facilities, boolean enable)
    {
	if (enable)
	    facMask |= facilities;
	else
	    facMask &= ~facilities;
    }

    /**
     * Set the writer to which to log. By default, things are logged to
     * <var>System.err</var>.
     *
     * @param log           the writer to log to; if null, nothing is changed
     * @param closeWhenDone if true, close this stream when a new stream is set
     *                      again
     */
    @Deprecated
    public static void setLogWriter(Writer log, boolean closeWhenDone)
    {
    	//Do nothing
    }
}
