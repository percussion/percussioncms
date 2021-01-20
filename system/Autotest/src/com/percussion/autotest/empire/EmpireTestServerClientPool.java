/******************************************************************************
 *
 * [ EmpireTestServerClientPool.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.autotest.empire;

import com.percussion.autotest.framework.*;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class EmpireTestServerClientPool extends UnicastRemoteObject
   implements Serializable, QAClientListener, Runnable
{
   class ClientState implements Serializable
   {

      public QAClient getClient()
      {
         return m_client;
      }

      public String getName()
      {
         return m_clientName;
      }

      public boolean isBusy()
      {
         return m_busy;
      }

      public void setBusy()
      {
         m_busy = true;
      }

      public void setNotBusy()
      {
         m_busy = false;
      }

      private QAClient m_client;
      private boolean m_busy;
      private String m_clientName;

      public ClientState(QAClient client, boolean busy) throws RemoteException
      {
         if (client == null)
         {
            throw new NullPointerException("client == null");
         }
         else
         {
            m_client = client;
            m_clientName = client.getName();
            m_busy = busy;
            return;
         }
      }
   }

   private abstract class ThreadTask
   {
      public abstract void execute();

      public ThreadTask()
      {
      }
   }

   public EmpireTestServerClientPool(EmpireTestServer server) 
      throws RemoteException
   {
      m_clientStateList = new Vector<ClientState>();
      m_tasks = new LinkedList<ThreadTask>();
      m_server = server;
      m_tasks.add(new ThreadTask()
      {
         public void execute()
         {
            synchronized(m_clientStateList)
            {
               for (int i=0; i<m_clientStateList.size(); i++)
               {
                  ClientState state = (ClientState)m_clientStateList.elementAt(i);
                  try
                  {
                     state.getClient().getName();
                  }
                  catch (RemoteException e)
                  {
                     System.out.println("Client " + state.getName() + 
                        " has either gone down unexpectedly or changed its name. Removing it from the pool.");
                     m_clientStateList.remove(state);
                  }
               }

            }
         }
      });
   }

   public boolean addClient(QAClient client) throws RemoteException
   {
      synchronized(m_clientStateList)
      {
         for (int i=0; i<m_clientStateList.size(); i++)
         {
            ClientState state = (ClientState)m_clientStateList.elementAt(i);
            if (state.getName().equals(client.getName()))
            {
               boolean flag = true;
               return flag;
            }
         }

         client.addClientListener(this);
         m_clientStateList.add(new ClientState(client, false));
         m_clientStateList.notify();
      }
      
      return false;
   }
   
   /**
    * Get the number of clients currently available. This returns all clients
    * in the pool, no matter whether they are busy or idle.
    *
    * @return the number of available clients.
    */
   public int getAvailableClients()
   {
      return m_clientStateList.size();
   }

   /**
    * Sends the supplied test script to the first client that is not
    * currently executing a script. If there isn't a free client, <code>
    * false</code> is returned. If the script is dispatched, this method
    * doesn't return until the entire script has been run (or an error
    * occurrs).
    *
    * @param doc The test script that needs to be executed. Never <code>null
    *    </code>.
    *
    * @return <code>true</code> if a client that wasn't busy was found in the
    *    pool and the script was dispatched, <code>false</code> otherwise.
    *
    * @throws IllegalArgumentException If doc is <code>null</code>.
    */
   public boolean dispatchScriptToFreeClient(QAScriptDocument doc)
   {
      if ( null == doc )
         throw new IllegalArgumentException( "doc can't be null" );

      if (m_shutdown)
         return false;

      synchronized(m_clientStateList)
      {
         for (int i=0; i<m_clientStateList.size(); i++)
         {
            ClientState state = (ClientState) m_clientStateList.elementAt(i);
            if (!state.isBusy())
            {
               System.out.println("Client pool dispatching script " + 
                  doc.getName() + " to client " + state.getName());
               try
               {
                  state.setBusy();
                  state.getClient().executeScript(doc);
                  return true;
               }
               catch (RemoteException e)
               {
                  e.printStackTrace();
               }
               state.setNotBusy();
               removeClient(state.getName());
            }
         }
      }

      return false;
   }
   
   /**
    * Waits until all clients in this pool are available, then sends the
    * supplied test script to each one of them in succession. We wait for all
    * clients in the pool to make sure no other clients are producing server
    * load for a different test.
    * After each has been initialized with the script, all clients begin 
    * execution at approximately the same time. The method doesn't return 
    * until the last client has finished executing and posting its results.
    *
    * @param doc The test script that needs to be executed. Never <code>null
    *    </code>.
    * @param requiredClients the number of clients needed for the current test,
    *    must be greater than 0 and smaller than the maximal number of clients
    *    available.
    * @param timeoutMs How long to wait for all clients to become available
    *    before returning, in milliseconds.
    * @return <code>true</code> if the script was successfully sent to all
    *    clients, <code>false</code> if the timeout period was reached before
    *    all clients became free or if client pool is in the process of shutting
    *    down.
    * @throws IllegalArgumentException If doc is <code>null</code> or the
    *    requiredClients is out of range.
    */
   public boolean dispatchScriptToRequiredClients(QAScriptDocument doc,
      int requiredClients, long timeoutMs) throws InterruptedException
   {
      if (null == doc)
         throw new IllegalArgumentException("doc can't be null");

      if (m_shutdown)
         return false;

      if (requiredClients <= 0 || requiredClients > m_clientStateList.size())
         throw new IllegalArgumentException(
            "number of clients out of available range");

      long waited = 0;
      long startTime = System.currentTimeMillis();
      boolean busy = true;
      try
      {
         while (waited < timeoutMs && busy)
         {
            synchronized(m_clientStateList)
            {
               busy = false;
               for (int i=0; i<m_clientStateList.size() && !busy; i++)
               {
                  ClientState state =
                     (ClientState) m_clientStateList.elementAt(i);
                  if (state.isBusy())
                     busy = true;
               }
               
               if (!busy)
               {
                  // all clients ready to go
                  for (int i=0; i<requiredClients; i++)
                  {
                     ClientState state =
                        (ClientState) m_clientStateList.elementAt(i);
                     System.out.println("Script " + doc.getName() + 
                        " being prepared by " + state.getName());
                     state.setBusy();
                     state.getClient().prepareScript(doc);
                  }

                  for (int i=0; i<requiredClients; i++)
                  {
                     ClientState state =
                        (ClientState) m_clientStateList.elementAt(i);
                     state.getClient().executePreparedScript();
                     System.out.println("Script execution for " + 
                        doc.getName() + " begun by " + state.getName());
                  }
               }
               else
               {
                  m_clientStateList.wait(timeoutMs - waited);
                  waited = System.currentTimeMillis() - startTime;
               }
            }
         }
         
         return waited < timeoutMs;
      }
      catch (RemoteException e)
      {
         System.out.println(e.getLocalizedMessage());
         e.printStackTrace();
         throw new RuntimeException(
            "Client failed during script preparation or execution.");
      }
   }
   
   /**
    * Waits until all clients in this pool are available, then sends the
    * supplied test script to each one of them in succession. After each has
    * been initialized with the script, all clients begin execution at
    * approximately the same time. The method doesn't return until the last
    * client has finished executing and posting its results.
    *
    * @param doc The test script that needs to be executed. Never <code>null
    *    </code>.
    *
    * @param timeoutMs How long to wait for all clients to become available
    *    before returning, in milliseconds.
    *
    * @return <code>true</code> if the script was successfully sent to all
    *    clients, <code>false</code> if the timeout period was reached before
    *    all clients became free of if client pool is in the process of shutting
    *    down.
    *
    * @throws IllegalArgumentException If doc is <code>null</code>.
    */
   public boolean dispatchScriptToAllClients(QAScriptDocument doc,
      long timeoutMs) throws InterruptedException
   {
      return dispatchScriptToRequiredClients(doc, m_clientStateList.size(), 
         timeoutMs);
   }


   public String getClientStatusString(String linePrefix)
   {
      String clientList = "";
      synchronized(m_clientStateList)
      {
         for(int i = 0; i < m_clientStateList.size(); i++)
         {
            ClientState state = (ClientState)m_clientStateList.elementAt(i);
            clientList = clientList + linePrefix + state.getName();
            if(state.isBusy())
               clientList = clientList + " (BUSY)\n";
            else
               clientList = clientList + " (FREE)\n";
         }

      }
      return clientList;
   }

   protected void makeNotBusy(String clientName)
   {
      synchronized(m_clientStateList)
      {
         for(int i = 0; i < m_clientStateList.size(); i++)
         {
            ClientState state = (ClientState)m_clientStateList.elementAt(i);
            if(!state.getName().equals(clientName))
               continue;
            state.setNotBusy();
            m_clientStateList.notify();
            break;
         }

      }
   }

   public void notifyClientShutdown(QAClientEvent e) throws RemoteException
   {
      status("Client " + e.getSource() + " is shutting down." + e.getMessage());
      removeClient(e.getSource());
   }

   public void notifyScriptCompleted(QAClientEvent e) throws RemoteException
   {
      status("Client " + e.getSource() + " finished executing." + e.getMessage());
   }

   public void notifyScriptInterrupted(QAClientEvent e) throws RemoteException
   {
      status("Client " + e.getSource() + " was interrupted." + e.getMessage());
   }

   public void notifyScriptStarted(QAClientEvent e) throws RemoteException
   {
      status("Client " + e.getSource() + " started executing." + e.getMessage());
   }

   public void notifyResultsRecorded(QAClientEvent e) throws RemoteException
   {
      status("Client " + e.getSource() + " results recorded." + e.getMessage());
      makeNotBusy(e.getSource());
   }

   public boolean removeClient(String clientName)
   {
      if (clientName == null)
         throw new IllegalArgumentException("clientName cannot be null");
      
      synchronized(m_clientStateList)
      {
         System.out.println("Removing client " + clientName + " from pool.");
         for (int i=0; i<m_clientStateList.size(); i++)
         {
            ClientState state = (ClientState) m_clientStateList.elementAt(i);
            if (state.getName().equals(clientName))
            {
               m_clientStateList.remove(state);
               return state.isBusy();
            }
         }

      }

      throw new IllegalStateException("client " + clientName + " was not a member");
   }

   public void run()
   {
      System.out.println("Started client pool checker thread " + 
         Thread.currentThread().toString());
      while (true)
      {
         try
         {
            Thread.sleep(1000);
            
            synchronized(m_clientStateList)
            {
               boolean busy = false;
               for (int i=0; i<m_clientStateList.size(); i++)
               {
                  ClientState state = 
                     (ClientState) m_clientStateList.elementAt(i);
                  try
                  {
                     state.getClient().getName();
                  }
                  catch (RemoteException e)
                  {
                     System.out.println("Client " + state.getName() +
                        " has either gone down unexpectedly or changed its name. Removing it from the pool.");
                     m_clientStateList.remove(state);
                  }
                  
                  if (state.isBusy())
                     busy = true;
               }
               
               if (!busy)
                  publishAllClientsDone(null);
            }

            do
            {
               Thread.sleep(20000L);
            }
            while(m_tasks.size() == 0);
            
            ThreadTask t = null;
            synchronized(m_tasks)
            {
               t = (ThreadTask) m_tasks.removeLast();
            }
            if (t != null)
               t.execute();
            synchronized(m_tasks)
            {
               m_tasks.addFirst(t); // move this periodic task back to the front
            }
         }
         catch(InterruptedException e)
         {
            return;
         }
      }
   }

   private void status(String s)
   {
      System.out.println(s);
   }

   public void waitForNewClient(@SuppressWarnings("unused") long timeoutMs)
      throws InterruptedException
   {
      synchronized(m_clientStateList)
      {
         for(int i = 0; i < m_clientStateList.size(); i++)
         {
            ClientState state = (ClientState)m_clientStateList.elementAt(i);
            if(!state.isBusy())
            {
               return;
            }
         }

         status("Waiting for a client...");
         m_clientStateList.wait();
      }
   }
   
   /**
    * Subscribes the provided listener for client pool events.
    * 
    * @param listener a listener interested in client pool events, may be
    *    <code>null</code>.
    */
   public void addListener(QAClientPoolListener listener)
   {
      if (listener != null)
         m_clientPoolListener.add(listener);
   }
   
   /**
    * Removes the provided listener from the list of client pool event 
    * subscribers.
    *
    * @param listener the listener to be removed from the client pool event
    *    subscribers list, may be <code>null</code>.
    */
   public void removeListener(QAClientPoolListener listener)
   {
      if (listener != null)
         m_clientPoolListener.remove(listener);
   }

   /**
    * Publish that all clients are done executing scripts and all results
    * have been recorded.
    *
    * @param msg a message to send to all listeners of this event, might be
    *    <code>null</code> or empty.
    */
   public void publishAllClientsDone(String msg)
   {
      synchronized(m_clientPoolListener)
      {
         for (int i=0; i<m_clientPoolListener.size(); i++)
         {
            QAClientPoolListener listener = 
               (QAClientPoolListener) m_clientPoolListener.get(i);
            listener.allClientsDone(new QAClientPoolEvent(m_server, msg));
         }
      }
   }

   /**
    * Shuts down all clients in the pool.  Will wait for each client to finish
    * it's currently running script before shutting it down.  Once this method
    * is called, the pool will not accept any new requests for script execution.
    */
   public void shutdown()
   {
      // already shutting down
      if (m_shutdown)
         return;

      // this should keep any new scripts from being dispatched
      m_shutdown = true;
      status(
         "Client pool is shutting down. No more scripts will be dispatched.");
      try
      {
         /* wait until all clients are free.  Remove all free clients from the
          * pool and shut them down.
          */
         boolean busy = true;
         while (busy)
         {
            synchronized(m_clientStateList)
            {
               busy = false;
               // walk down list since we'll be removing
               for (int i=m_clientStateList.size()-1; i >= 0 && !busy; i--)
               {
                  ClientState state =
                     (ClientState) m_clientStateList.elementAt(i);
                  if (state.isBusy())
                     busy = true;
                  else
                  {
                     // call shutdown
                     QAClient client = state.getClient();
                     status("Shutting down client " + client.getName());
                     client.removeClientListener(this);
                     m_clientStateList.remove(i);
                     try
                     {
                        client.shutDown(true);
                     }
                     catch (java.rmi.UnmarshalException e)
                     {
                        /* since client program exits at the end of the shutdown
                         * we get an excpetion trying to get the method return
                         * header back via rmi.  Just ignore it.
                         */
                     }
                  }
               }
               
               if (busy)
               {
                  try
                  {
                     m_clientStateList.wait();
                  }
                  catch (InterruptedException e)
                  {
                  }
               }
            }

         }

         status("Client pool shutdown completed");
      }
      catch (RemoteException e)
      {
         e.printStackTrace();
         throw new RuntimeException(
            "Error shutting down client pool.");
      }

   }

   /**
    * List of ClientState objects, each representing the state of a client in
    * this pool, and containing a reference to that client.  Never <code>null
    * </code>, may be emtpy.
    */
   private Vector<ClientState> m_clientStateList;
   private QAServer m_server;
   private LinkedList<ThreadTask> m_tasks;

   /**
    * A list of all subscribers of client pool events, never <code>null</code>,
    * might be empty.
    */
   private List<QAClientPoolListener> m_clientPoolListener =
         new Vector<QAClientPoolListener>();

   /**
    * Flag to indicate if the pool is in the process of shutting down.  If
    * <code>true</code>, then no new script requests will be dispatched.  If
    * <code>false</code>, then the pool will process dispatches as usual.
    */
   private boolean m_shutdown = false;
}
