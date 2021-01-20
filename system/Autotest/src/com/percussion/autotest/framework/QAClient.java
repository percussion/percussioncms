/******************************************************************************
 *
 * [ QAClient.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.autotest.framework;

import java.net.MalformedURLException;
import java.rmi.*;

public interface QAClient
   extends Remote, QAClientEventSource
{
   /**
    * Performs initialization that must be done before a test is executed.
    * After this method returns successfully, call {@link
    * #executePreparedScript()} to actually run the test. This method is
    * only useful for performance tests. If you don't care about the setup
    * time, call {@link #executeScript} directly.
    *
    * @param script The test to run. Never <code>null</code>.
    *
    * @throws IllegalArgumentException if script is <code>null</code>.
    *
    * @throws RemoteException If a communication problem occurs.
    */
   public void prepareScript( QAScriptDocument script )
      throws RemoteException;

   /**
    * Runs the test that has already been prepared by a call to {@link
    * #prepareScript(QAScriptDocument) prepareScript}. The method does not
    * return until the test is complete or an error occurs.
    *
    * @throws IllegalStateException if there is not a prepared script ready
    *    to execute. If <code>executeScript</code> is called after <code>
    *    prepareScript</code>, but before this method, any previously prepared
    *    script is lost.
    *
    * @throws RemoteException If a communication problem occurs.
    */
   public void executePreparedScript()
      throws RemoteException;

   /**
    * A convenience method that can be used if you don't care about the time
    * required for preparing a test script for execution. Usually you will
    * only care about this if you are running performance tests. This methdod
    * calls the following 2 methods, in order:
    * <ol>
    *    <li>{@link #prepareScript(QAScriptDocument) prepareScript}</li>
    *    <li>{@link #executePreparedScript() executePreparedScript}</li>
    * </ol>
    * See those 2 methods for all details.
    */
   public void executeScript( QAScriptDocument script )
      throws RemoteException;

   public String getName()
      throws RemoteException;

   public void interrupt(String s)
      throws RemoteException;

   public void selfRegister(String s)
      throws RemoteException, MalformedURLException, NotBoundException;

   /**
    * Instructs the client to shutdown.  Method does not necessarily wait for
    * the shutdown to complete.
    *
    * @param wait If <code>true</code>, client will finish any current work
    * before shutting down. If <code>false</code>, client will immediately
    * terminate any processing and shutdown.
    */
   public abstract void shutDown(boolean wait)
      throws RemoteException;
                     
}
