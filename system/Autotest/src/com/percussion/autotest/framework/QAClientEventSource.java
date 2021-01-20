/*[ QAClientEventSource.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

import java.rmi.RemoteException;

public interface QAClientEventSource
{

    public abstract void addClientListener(QAClientListener qaclientlistener)
        throws RemoteException;

    public abstract void removeClientListener(QAClientListener qaclientlistener)
        throws RemoteException;
}
