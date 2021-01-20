/*[ IResponseExit.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.empire.script;

import java.io.InputStream;
import java.util.Properties;

import com.percussion.test.http.HttpHeaders;

/**
 * The auto tester supports plugin modules in various places. One of those
 * places is where the response data is retrieved after a request has been
 * submitted to the server under test. This inteface defines the method
 * that will be called by the auto tester on each response.
 * <p>The class name that implements this interface is registered with the
 * client by specifying it in the test script.
 * <p>Since the processResponse method is called for every request made, it
 * should perform its task as quickly as possible.
 */
public interface IResponseExit
{
   /**
    * The main processing method. This method is called after the response
    * has been received and partially processed. In other words, the headers
    * have already been parsed. The data in the input stream is the body of
    * the response.
    *
    * @param params A set of name/value pairs that were specified in the
    *    script for this exit. Never <code>null</code>.
    *
    * @param ctx The script interpreter context in which the current script
    *    is running. This can be used to add or modify macro values. All
    *    following requests will see any values made using this object.
    *
    * @param headers The headers present in the response. Never <code>null
    *    </code>. Any change made to these headers will be seen by the script
    *    engine.
    *
    * @param input A stream the contains the data returned with the response.
    *    If this stream is read, it must be fully read and a new stream must
    *    be returned unless the stream supports marking. If it does, then it
    *    must be marked and reset before returning.
    *
    * @return A stream that will return the same bytes as the input would
    *    have returned. In general, the input will be returned directly. If
    *    <code>null</code> is returned, an exception will be thrown. If the
    *    data returned by the stream is modified in any way, the resulting
    *    behavior may be unexpected.
    *
    * @throws IllegalArgumentException if ctx, headers or input is <code>null
    *    </code>.
    *
    * @throws ScriptTestErrorException If the exit wants to prematurely
    *    terminate this test. Note that this could stop the entire test
    *    sequence if 'stopOnError' is enabled.
    */
   public InputStream processResponse( Properties params,
         IExecutionContext ctx, HttpHeaders headers, InputStream input )
      throws ScriptTestErrorException;
}
