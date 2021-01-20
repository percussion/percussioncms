/*[ QAClientPoolListener.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

import java.io.Serializable;

/**
 * A listener to report events for a particular client pool.
 */
public interface QAClientPoolListener extends Serializable
{
   /**
    * Notifies all subscribers that all clients running the in the current
    * configuration have finshed the script and recored the results.
    *
    * @param event a client pool event, never <code>null</code>.
    * @throws RemoteException if anything communication to the remote object
    *    goes wrong.
    */
   public void allClientsDone(QAClientPoolEvent event);
}
