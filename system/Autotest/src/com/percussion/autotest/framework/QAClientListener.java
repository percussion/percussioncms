/*[ QAClientListener.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

// Referenced classes of package com.percussion.autotest.framework:
//            QAClientEvent
public interface QAClientListener extends Serializable, Remote
{
   public abstract void notifyClientShutdown(QAClientEvent qaclientevent)
      throws RemoteException;

   public abstract void notifyScriptCompleted(QAClientEvent qaclientevent)
      throws RemoteException;

   public abstract void notifyScriptInterrupted(QAClientEvent qaclientevent)
      throws RemoteException;

   public abstract void notifyScriptStarted(QAClientEvent qaclientevent)
      throws RemoteException;

   /**
    * Notify all listeners if this client's results have been recorded.
    *
    * @param qaclientevent the client event, never <code>null</code>.
    * @throws RemoteException if anything went wrong during the notification.
    */
   public abstract void notifyResultsRecorded(QAClientEvent qaclientevent)
      throws RemoteException;
}
