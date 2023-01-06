/**[ PSProcessMonitor.java ]****************************************************
 *
 * COPYRIGHT (c) 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 ******************************************************************************/
package com.percussion.cx;

import com.percussion.cx.objectstore.PSNode;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.text.MessageFormat;
/**
 * The class to use as a monitor for a process to keep track of the process
 * status. This class is thread safe to set or get the status.
 */
public class PSProcessMonitor
{
   /**
    * Constructs this object to monitor the processing of supplied number of
    * nodes.
    *
    * @param total the total number of nodes to process, may not be <= 0.
    *
    * @throws IllegalArgumentException if total is invalid.
    */
   public PSProcessMonitor(int total, PSContentExplorerApplet applet)
   {
      if(total <= 0)
         throw new IllegalArgumentException("total may not be <= 0");
      
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      m_applet = applet;

      m_total = total;
   }

   /**
    * Sets the dialog to be updated whenever this monitor status gets updated.
    *
    * @param dlg the dialog to be updated, may not be <code>null</code>
    */
   public void setStatusDialog(PSContentExplorerStatusDialog dlg)
   {
      if(dlg == null)
         throw new IllegalArgumentException("dlg may not be null.");

      m_dlg = dlg;
   }

   /**
    * Updates this monitor with the current node processing. If the status
    * dialog is set with this monitor, the dialog's status will be updated.
    *
    * @param current the current node processing, must be >= 0 and <= total
    * number of nodes processing.
    * @param processingNode the node being processed, used to display status
    * message.
    *
    * @throws IllegalArgumentException if any parameter is invalid
    */
   public void updateStatus(int current, PSNode processingNode)
   {
      if(current > m_total || current <= 0)
         throw new IllegalArgumentException(
            "current may not be <= 0 or > actual total");

      if(processingNode == null)
         throw new IllegalArgumentException("processingNode may not be null.");

      m_curProcessNode = processingNode;
      m_current = current;

      if(m_dlg != null)
      {
         String msg = MessageFormat.format(
               m_applet.getResourceString(getClass(),
            "Processing the {0} <{1}>"),
            new String[] { m_curProcessNode.getType(),
            m_curProcessNode.getLabel() });

         int percentDone = (int)((m_current-1)*100/m_total);
         m_dlg.updateStatus(msg, percentDone);
      }
   }

   /**
    * Gets the current status of the process.
    *
    * @return status, one of the <code>STATUS_xxx</code> values.
    */
   public int getStatus()
   {
      synchronized(m_syncObject)
      {
         return m_status;
      }
   }

   /**
    * Sets the status of the monitor. If the status is completed, informs the
    * status dialog that the process is completed.
    *
    * @param currentStatus the current status of the process, must be one of the
    * <code>STATUS_xxx</code> values.
    *
    * @throws IllegalArgumentException if any currentStatus is invalid
    */
   public synchronized void setStatus(int currentStatus)
   {
      if(currentStatus < STATUS_INIT || currentStatus > STATUS_COMPLETE )
         throw new IllegalArgumentException("invalid status");

      synchronized(m_syncObject)
      {
         m_status = currentStatus;
         if(m_status == STATUS_COMPLETE && m_dlg != null)
            m_dlg.processCompleted();
      }
   }

   /**
    * Displays the supplied error message to the user. Passes the message to its
    * container dialog if one exists or diplays it tight here is not.
    *
    * @param message the error message to be displayed, may be
    *    <code>null</code> or empty.
    * @param component the parent component that shows the error dialog,
    *    may be <code>null</code>.
    */
   public void showError(Component parent, String message)
   {
      if (m_dlg != null)
         m_dlg.showError(message);
      else
      {
         String label = m_applet.getResourceString(
            getClass(), "Do you want to continue?");
         String title = m_applet.getResourceString(
            getClass(), "Error");
         int selection = PSContentExplorerStatusDialog.displayErrorDialog(
            parent,title,label,message);
         if (selection == JOptionPane.NO_OPTION)
            setStatus(PSProcessMonitor.STATUS_STOP);
         else
            setStatus(PSProcessMonitor.STATUS_RUN);
      }
   }

   /**
    * The dialog that need to be updated with the processing status, <code>null
    * </code> until a call to <code>
    * setStatusDialog(PSContentExplorerStatusDialog)</code>.
    */
   private PSContentExplorerStatusDialog m_dlg;

   /**
    * The constant to indicate that the process is initialized.
    */
   public static final int STATUS_INIT = 1;

   /**
    * The constant to indicate that the process is running.
    */
   public static final int STATUS_RUN = 2;

   /**
    * The constant to indicate that the process is paused to do user interaction.
    */
   public static final int STATUS_PAUSE = 3;

   /**
    * The constant to indicate that the process is stopped by the user.
    */
   public static final int STATUS_STOP = 4;

   /**
    * The constant to indicate that the process is completed.
    */
   public static final int STATUS_COMPLETE = 5;

   /**
    * The status of the process, initialized to <code>STATUS_INIT</code> and
    * gets modified through calls to <code>setStatus(int)</code>
    */
   private int m_status = STATUS_INIT;

   /**
    * The current node count being processed, initialized to <code>0</code> and
    * gets updated through calls to <code>updateStatus(int, PSNode)</code>
    */
   private int m_current = 0;

   /**
    * The total number of nodes being processed, initialized in the ctor and
    * never modified after that.
    */
   private int m_total = 0;

   /**
    * The current node being processed, initialized to <code>null</code> and
    * gets updated through calls to <code>updateStatus(int, PSNode)</code>
    */
   private PSNode m_curProcessNode = null;

   /**
    * The monitor object to use for synchronized access of status.
    */
   private final Object m_syncObject = new Object();
   

   /**
    * A reference back to the applet.
    */
   private PSContentExplorerApplet m_applet;
}