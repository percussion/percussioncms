/*[ QAServer.java ]************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

import java.net.MalformedURLException;
import java.rmi.*;

public interface QAServer
    extends Remote
{
   public abstract boolean lockObject(QARequestContext context, String name, 
      long expiresInMs, long waitMs) throws RemoteException;

   public abstract void recordResults(QATestResults qatestresults)
      throws RemoteException;

   public abstract void registerClient(String s)
      throws RemoteException, MalformedURLException, NotBoundException;

   public abstract void registerObject(QAObjectDescription qaobjectdescription, 
      QARequestContext qarequestcontext) throws RemoteException;

   /**
    * Is this started in regression test mode?
    *
    * @return <code>true</code> if this is a regression test server, 
    *    <code>false</code> otherwise.
    */
   public boolean isRegressionTest() throws RemoteException;;
   
   /**
    * Is this started in performance test mode?
    *
    * @return <code>true</code> if this is a performace test server, 
    *    <code>false</code> otherwise.
    */
   public boolean isPerformanceTest() throws RemoteException;;
   
   /**
    * Is this started in stress test mode?
    *
    * @return <code>true</code> if this is a stress test server, 
    *    <code>false</code> otherwise.
    */
   public boolean isStressTest() throws RemoteException;;
   
   /**
    * Get the currently selected test type (from qaserver.properties). Changes
    * in the qaserver.properties are only picked up through a server restart.
    *
    * @return the currently specified test type, one of TEST_REGRESSION, 
    *    TEST_PERFORMANCE or TEST_STRESS, defaults to TEST_REGRESSION if none 
    *    is specified.
    */
   public int getTestType() throws RemoteException;;
   
   /**
    * Shuts down the server.  Once this method is called, no new scirpts will
    * be dispatched.
    *
    * @param wait If <code>true</code>, server will wait for all queued scripts
    * to be dispatched and all clients to complete their scripts before shutting
    * down each client and then itself.  If <code>false</code>, it will
    * immediately terminate any processing and shutdown.  Clients are not
    * notified in this case.
    */
   public abstract void shutDown(boolean wait)
      throws RemoteException;
      
   /** Indicates the test as regression test */
   public static final int TEST_REGRESSION = 0;
   /** Indicates the test as performace test */
   public static final int TEST_PERFORMANCE = 1;
   /** Indicates the test as stress test */
   public static final int TEST_STRESS = 2;
}
