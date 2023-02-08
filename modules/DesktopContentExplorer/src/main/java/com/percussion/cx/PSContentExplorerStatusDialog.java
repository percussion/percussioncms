/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.cx;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.guitools.PSDialog;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;

/**
 * The status dialog to show the progress bar for the status of the current job
 * set with this dialog. Creates a new thread to poll on the job controller and
 * receive status and updates the progress bar and status message for each job
 * set.
 */
public class PSContentExplorerStatusDialog extends PSDialog
{
   /**
    * Constructs this dialog with specified process monitor.
    *
    * @param parent the parent frame of this dialog, may be <code>null</code>
    * @param processMonitor the process monitor to use to monitor the process
    * executing, may not be <code>null</code>
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public PSContentExplorerStatusDialog(Frame parent, PSProcessMonitor monitor, PSContentExplorerApplet applet)
   {
      super(parent, applet.getResourceString(
         PSContentExplorerStatusDialog.class, "Process Status"));

      if(monitor == null)
         throw new IllegalArgumentException("monitor may not be null");
      
      if (applet == null)
         throw new IllegalArgumentException("applet must not be null");
      
      m_applet = applet;

      m_monitor = monitor;

      initDialog();

      m_monitor.setStatusDialog(this);
   }

   /**
    * Initializes the dialog framework with progress bar and labels for job
    * description. Sets the dialog title also.
    *
    * @param title the title of the dialog, assumed not <code>null</code> or
    * empty.
    */
   private void initDialog()
   {
      JPanel panel = new JPanel();
      getContentPane().add(panel);
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);

      panel.add(m_msgLabel);
      panel.add(Box.createVerticalStrut(5));

      JPanel progressPanel = new JPanel();
      progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.X_AXIS));
      progressPanel.add(new JLabel(
            m_applet.getResourceString(getClass(), "Status"),
         SwingConstants.LEFT));
      progressPanel.add(Box.createHorizontalStrut(10));
      m_progressBar.setStringPainted(true);
      m_progressBar.setPreferredSize(new Dimension(300, 20));
      progressPanel.add(m_progressBar);
      progressPanel.add(Box.createHorizontalGlue());
      progressPanel.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(progressPanel);

      panel.add(Box.createVerticalStrut(15));
      JPanel commandPanel = new JPanel();
      commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.X_AXIS));
      commandPanel.setAlignmentX(LEFT_ALIGNMENT);
      commandPanel.add(Box.createHorizontalGlue());
      m_cancelButton = new UTFixedButton("");
      m_cancelButton.setAction(
         new AbstractAction(m_applet.getResourceString(
         PSContentExplorerStatusDialog.class, "Cancel"))
         {
            public void actionPerformed(ActionEvent e)
            {
               onCancel();
            }
         }
      );
      commandPanel.add(m_cancelButton);
      commandPanel.add(Box.createHorizontalGlue());
      panel.add(commandPanel);
      panel.add(Box.createVerticalStrut(15));
      panel.add(Box.createVerticalGlue());

      pack();
      center();
      setResizable(true);
   }

   /**
    * Action method for 'Cancel' button. Confirms with the user to cancel the
    * job and cancels the job if the button represents 'Cancel' action,
    * otherwise simply closes the dialog by hiding it. See {@link
    * PSDeploymentStatusMonitor#stopMonitor()} for more description on
    * cancelling a job.
    */
   public void onCancel()
   {
      m_monitor.setStatus(PSProcessMonitor.STATUS_PAUSE);
      int option = JOptionPane.showConfirmDialog(this,
            m_applet.getResourceString(
         getClass(), "Are you sure you want to cancel this process?"),
         m_applet.getResourceString(
         getClass(), "Cancel process"),
         JOptionPane.YES_NO_OPTION,
         JOptionPane.INFORMATION_MESSAGE);

      if(option == JOptionPane.YES_OPTION)
      {
         m_monitor.setStatus(PSProcessMonitor.STATUS_STOP);
         super.onCancel();
      }
      else
         m_monitor.setStatus(PSProcessMonitor.STATUS_RUN);
   }

   /**
    * Requests the Swing event dispatch thread to update the progress bar and
    * status message and returns immediately.
    *
    * @param statusMessage the status message to show, may be <code>null</code>
    * or empty.
    * @param percentDone The percent complete to set on the progress bar. Must be
    * between 0 and 100.
    *
    * @throws IllegalArgumentException if percentDone is not valid.
    */
   public void updateStatus(final String statusMessage, final int percentDone)
   {
      if(percentDone < 0 || percentDone > 100)
         throw new IllegalArgumentException(
            "percentDone must be between 1 and 100.");

      SwingUtilities.invokeLater(new Runnable()
      {
         public void run()
         {
            m_msgLabel.setText(statusMessage);
            m_progressBar.setValue(percentDone);
         }
      });
   }

   /**
    * Hides the dialog if it is showing. Should be called when the process
    * monitored by this dialog is completed.
    */
   public void processCompleted()
   {
      if (isShowing())
         setVisible(false);
   }

   /**
    * Displays an error dialog for the supplied message. The message will be
    * displayed to the user and he has the possiblity to continue or abort
    * the current process.
    *
    * @param message the error message to be displayed, may be
    *    <code>null</code> or empty.
    */
   public void showError(String message)
   {
      if (message == null)
         message = "";

      if (!isShowing())
         setVisible(true);
      String label = m_applet.getResourceString(
         getClass(), "Do you want to continue?");
      String title = m_applet.getResourceString(
         getClass(), "Error");
      int selection = displayErrorDialog(this,title,label,message);

      if (selection == JOptionPane.NO_OPTION)
         onCancel();
      else
         m_monitor.setStatus(PSProcessMonitor.STATUS_RUN);
   }
   /**
    * Displays the supplied error message to the user and returns the users
    * selection.
    *
    * @param message the error message to be displayed, may be
    *    <code>null</code> or empty.
    * @param component the parent component that shows the error dialog,
    *    may be <code>null</code>.
    * @return int user's selection on error dialog.
    */
   public static int displayErrorDialog(Component parent, String title,
      String label, String message)
   {
      Object[] errordata = new Object[2];
      errordata[0] = label;

      JEditorPane messagepane = new JEditorPane();
      messagepane.setEditable(false);
      messagepane.setAutoscrolls(true);
      messagepane.setPreferredSize(new Dimension(600, 400));
      int temp = message.indexOf(HTML_OPEN_TAG);
      int temp1 = message.indexOf(HTML_CLOSE_TAG);
      if(temp > -1 && temp1 > temp)
      {
         message = message.substring(temp, temp1 + HTML_OPEN_TAG.length());
         messagepane.setContentType(TEXT_BY_HTML);
      }
      else
      {
         messagepane.setContentType(TEXT_BY_TEXT);
      }

      messagepane.getDocument().putProperty(
         "IgnoreCharsetDirective", Boolean.TRUE);
      messagepane.setText(message);

      JScrollPane pane = new JScrollPane(messagepane);

      errordata[1] = pane;
      JOptionPane optPane = new JOptionPane(errordata,
         JOptionPane.ERROR_MESSAGE,  JOptionPane.YES_NO_OPTION);
      JDialog dlg = optPane.createDialog(parent, title);
      dlg.setSize(new Dimension(600, 400));
      dlg.pack();
      dlg.setResizable(true);
      dlg.show();

      return Integer.parseInt(optPane.getValue().toString());
   }
   /**
    * The progress bar for the currently monitored job. Never <code>null</code>
    * after it is initialized.
    */
   private JProgressBar m_progressBar = new JProgressBar();

   /**
    * The label that represents the current job status message, initialized to
    * an empty label, and gets updated by status monitor of the current job in
    * <code>updateStatus(String, int)</code). Never <code>null</code> after it
    * is initialized.
    */
   private JLabel m_msgLabel = new JLabel();

   /**
    * The button to cancel the current running process, initialized in <code>
    * initDialog()</code> and never <code>null</code> after that.
    */
   private UTFixedButton m_cancelButton;

   /**
    * The process monitor to use to cancel the process and get the progress of
    * the process, initialized in the ctor and never <code>null</code> or
    * modified after that.
    */
   private PSProcessMonitor m_monitor;

   /**
    * Constant for html open tag
    */
   public static final String HTML_OPEN_TAG = "<html>";

   /**
    * Constant for html close tag
    */
   public static final String HTML_CLOSE_TAG = "</html>";
   /**
    * Constant for content type text/html
    */
   public static final String TEXT_BY_HTML = "text/html";

   /**
    * Constant for content type text/text
    */
   public static final String TEXT_BY_TEXT = "text/text";
   
   /**
    * A reference back to the applet that initiated this action manager.
    */
   private PSContentExplorerApplet m_applet;

}
