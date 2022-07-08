/******************************************************************************
 *
 * [ ErrorDialogs.java ]
 *
 * COPYRIGHT (c) 1999 - 2012 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.guitools;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;


/**
 * Generic class to display error dialogs for exceptions or fatal errors using
 * <code>JOptionPane</code>. Caller can instantiate this object with the parent
 * window for the error dialog or can use some of the static methods to display
 * the error dialog.
 */
public class ErrorDialogs
{
   /**
    * Constructs the object.
    *
    * @param parent the parent window of the error dialog, may not be <code>null
    * </code>
    *
    * @throws IllegalArgumentException if parent is <code>null</code>
    */
   public ErrorDialogs(Window parent)
   {
      if(parent == null)
         throw new IllegalArgumentException("parent may not be null.");
      m_parent = parent;
   }

   /**
    * Displays an error dialog using <code>JOptionPane</code> with a text area.
    *
    * @param errorBody the message body of the dialog, may be <code>null</code>
    * or empty.
    * @param errorTitle the title of the dialog, if <code>null</code> uses
    * default error title.
    * @param type the type of the message, must be one of the
    * <code>JOptionPane.xxx_MESSAGE</code> types.
    *
    * @throws IllegalArgumentException if the type is not valid.
    */
   public void showErrorDialog(String errorBody, String errorTitle, int type)
   {
      showErrorDialog(m_parent, errorBody, errorTitle, type);
   }

   /**
    * Convenience constructor, calls {@link #showErrorDialog(Component, String,
    * String, int) showErrorDialog(Component, MessageFormat.format(String,
    * Object[]), String, int)}
    */
   public static void showErrorDialog(Component parent, String errorMsg,
      Object[] msgArgs, String errorTitle, int type)
   {
      showErrorDialog(parent, MessageFormat.format(errorMsg, msgArgs),
         errorTitle, type);
   }

   /** A error dialog &quot;wrapper&quot; method that calls upon
    * JOptionPane.showMessageDialog with a text area for the message.
    *
    * @param parent The owner Window of this error dialog, may be <code>null
    * </code>.
    * @param errorBody The error text to be displayed in the body of the dialog,
    * may be <code>null</code> or empty.
    * @param errorTitle The title text of the dialog, if <code>null</code> uses
    * default error title.
    * @param type The message type of <code>JOptionPane</code>(usually
    * <code>JOptionPane.ERROR_MESSAGE</code>, but one of the XXX_MESSAGE types).
    *
    * @throws IllegalArgumentException if type is not one of the <code>
    * JOptionPane</code> message types.
    */
   public static void showErrorDialog( Component parent, String errorBody,
      String errorTitle, int type)
   {
      if(type != JOptionPane.INFORMATION_MESSAGE &&
         type != JOptionPane.ERROR_MESSAGE &&
         type != JOptionPane.PLAIN_MESSAGE &&
         type != JOptionPane.QUESTION_MESSAGE &&
         type != JOptionPane.WARNING_MESSAGE)
         throw new IllegalArgumentException("type is invlaid.");

      if ( null == errorTitle )
      {
         if(null != m_res)
         {
            try {
               errorTitle = m_res.getString( "ErrorTitle" );
            }
            catch(MissingResourceException e)
            {
               errorTitle = "Error";
            }
         }
         else
            errorTitle = "Error";
      }
      showRenderedErrorDialog(parent, errorBody, errorTitle, type);
   }

   /**
    * Displays the supplied error message to the user, if the message is html
    * then it will be rendered.
    * @param parent The owner Window of this error dialog, may be <code>null
    * </code>.
    * @param message the error message to be displayed, may be
    *    <code>null</code> or empty.
    * @param title the title of the error dialog box
    * @param type The message type of <code>JOptionPane</code>(usually
    * <code>JOptionPane.ERROR_MESSAGE</code>, but one of the XXX_MESSAGE types).
    */
   private static void showRenderedErrorDialog(Component parent, String message,
      String title, int type)
   {
      Component editor;
      int temp = message.indexOf(HTML_OPEN_TAG);
      int temp1 = message.indexOf(HTML_CLOSE_TAG);
      if(temp > -1 && temp1 > temp)
      {
         JEditorPane messagepane = new JEditorPane();
         messagepane.setEditable(false);
         messagepane.setAutoscrolls(true);
         message = message.substring(temp, temp1 + HTML_CLOSE_TAG.length());
         messagepane.setContentType(TEXT_BY_HTML);
         messagepane.setPreferredSize(new Dimension(600, 400));
         messagepane.getDocument().putProperty(
            "IgnoreCharsetDirective", Boolean.TRUE);
         messagepane.setText(message);
         editor = messagepane;
      }
      else
      {
         JTextArea area = new JTextArea();
         area.setEditable(false);
         area.setAutoscrolls(true);
         area.setPreferredSize(new Dimension(600, 200));
         area.setLineWrap(true);
         area.setWrapStyleWord(true);
         area.setText(message);
         editor = area;
      }

      JScrollPane pane = new JScrollPane(editor);

      JOptionPane optPane = new JOptionPane(pane, type);
      JDialog dlg = optPane.createDialog(parent, title);

      dlg.setResizable(true);
      dlg.setVisible(true);
   }

   /**
    * Displays error dialog for the exception with the supplied title.
    *
    * @param e the exception, may not be <code>null</code>. If there is no
    * message for the exception, it uses the class name of the exception for the
    * message of the dialog.
    * @param bPrintCallStack if <code>true</code> prints the call stack to the
    * console, after displaying the dialog.
    * @param title the title of the dialog, if <code>null</code> uses
    * default error title.
    *
    * @throws IllegalArgumentException if e is <code>null</code>
    */
   public void showError(Exception e, boolean bPrintCallStack, String title)
   {
      if(e == null)
         throw new IllegalArgumentException("e may not be null.");

      String displayText = e.getMessage();
      if ( null == displayText || 0 == displayText.length())
      {
         if(m_res != null)
         {
            displayText = MessageFormat.format(
               m_res.getString("NoExceptionMsg"),
               new String[]{e.getClass().getName()} );
         }
         else {
            displayText = "An exception occurred, but no text is available. " +
               "The exception class was: " + e.getClass().getName();
         }
      }
      else
         displayText = displayText.trim();

      if(bPrintCallStack)
         e.printStackTrace();

      showErrorDialog(displayText, title, JOptionPane.ERROR_MESSAGE);
   }

   /**
    * This method prints a message to the screen, indicating that the error
    * will terminate the program. After the user clicks OK, the program is
    * terminated.
    *
    * @param strMsg this text is shown in the dialog. It should describe the
    * problem that caused the error, may not be <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if the strMsg is <code>null</code> or
    * empty.
    */
   public static void FatalError(String strMsg)
   {
      if(strMsg == null || strMsg.trim().length() == 0)
         throw new IllegalArgumentException("strMsg may not be null or empty.");

      String strFormat = null;
      String strFatalDlgTitle = null;
      if (null != m_res)
      {
         try
         {
            strFormat = m_res.getString("FatalException");
            strFatalDlgTitle = m_res.getString("FatalDlgTitle");
         }
         catch (MissingResourceException e)
         {
            // ignore this, the strings will be set below
         }
      }
      if (null == strFormat)
         strFormat = "A fatal exception has occurred with the following "
            + "message:\n {0}. The program cannot continue.";
      if (null == strFatalDlgTitle)
         strFatalDlgTitle = "Fatal Error";

      String [] astrParams =
      {
         strMsg
      };

      showErrorMessage( null, MessageFormat.format(strFormat, astrParams),
         strFatalDlgTitle );
      System.out.println( "Fatal error, program terminating" );
      System.exit(-1);
   }

   /**
    * Displays the supplied message to the user w/ the supplied title, or a
    * default title if <code>null</code>.
    *
    * @param parent Will be passed in as the parent of the dialog displaying
    * the error message, may be <code>null</code>
    * @param msg The text to display, may be <code>null</code>.
    * @param title The title for the dialog. If <code>null</code>, a default
    * title is used.
   **/
   public static void showErrorMessage(Window parent, String msg, String title)
   {
      if ( null == title )
      {
         if(null != m_res)
            title = m_res.getString( "ExceptionTitle" );
         else
            title = "Exception Caught";
      }
      if(msg.indexOf(HTML_OPEN_TAG) > -1 &&
         msg.indexOf(HTML_OPEN_TAG) < msg.indexOf(HTML_CLOSE_TAG))
         showRenderedErrorDialog(parent, msg, title, JOptionPane.ERROR_MESSAGE);
      else
      {
          String message=cropErrorMessage(msg);
          JOptionPane pane=new JOptionPane(message,JOptionPane.ERROR_MESSAGE);
          JDialog dialog=pane.createDialog(parent,title);
          setAccessibleNameToOkButton(pane,message);
          dialog.setVisible(true);    
      }
   }
   
   /**
    * Sets accessible name to JOptionPane Error Message button.
    * By gaining accessible context and then setting accessible name to the error message button
    * allows to gain focus to JOptanPane's error message dialog which is necessary for
    * assistive technologies like JAWS to read the error message and the button label.
    * For IE message need to be set in the accessible conentect
    * @param pane will be passed in as the JOptionPane (Message type - Error) object for setting accessible name to its ok button.
    */
   
   public static void setAccessibleNameToOkButton(JOptionPane pane, String message)
   {
       Component comps[]=pane.getComponents();
       for(int i=0;i<comps.length;i++)
       {
           if(comps[i] instanceof JPanel)
           {
               Component[] children=((JPanel)comps[i]).getComponents();
               for(int j=0;j<children.length;j++)
               {
                   if(children[j] instanceof JButton)
                   {
                       JButton button=(JButton)children[j];
                       button.getAccessibleContext().setAccessibleName(message+button.getText());
                   }
               }
           }
       }
   }

   /**
    * Displays the error message to the user w/ the supplied title, or a
    * default title if <code>null</code>.
    * @param msg The text to display, may be <code>null</code> or empty.
    * @param title The title for the dialog. If <code>null</code>, a default
    * title is used, can be empty.
    */
   public void showErrorMessage( String msg, String title )
   {
      showErrorMessage(m_parent, msg, title);
   }

   /**
    * This method crops the message passed in to a readable size (80 chars per
    * line) by adding \n(end of line) character into specified line lengths.
    *
    * @param msg The message sent in for cropping. If <code>null</code>, <code>
    * null</code> will be returned.
    *
    * @return The message with \n characters added in.
   */
   public static String cropErrorMessage(String msg)
   {
      if(msg == null)
         return null;

      String errorMsg = "";
      String[] lines = msg.split("\n");
      for (String line : lines)
      {
         // put newline back if not first line
         if (errorMsg.length() > 0)
            errorMsg += "\n";
         
         StringTokenizer tokenString = new StringTokenizer(
            line, new String(" "), true);
         int lineCount = 0;
         
         while(tokenString.hasMoreTokens())
         {
            String s = tokenString.nextToken();
            if(s == null)
            break;

            lineCount += s.length();

            if (80 > lineCount)
              errorMsg = errorMsg + s;
            else
            {
              // reached eol so add eol char and reset lineCount
              errorMsg = errorMsg + "\n" + s;
              lineCount = s.length();
            }
         }
      }

      return errorMsg;
   }

   /**
    * The parent window to show the error dialogs, initialized in the
    * constructor and never <code>null</code> or modified after that.
    */
   Window m_parent;

   /**
    * The resource bundle with common error titles, initialized statically and
    * never <code>null</code> or modified after that. This resource bundle is
    * common to all files in this package and is initialized in the first call
    * to {@link ResourceHelper#getResources() getResources()}.
    */
   static ResourceBundle m_res = ResourceHelper.getResources();

   /**
    * Constant for html open tag
    */
   public static final String HTML_OPEN_TAG = "<html";

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
}
