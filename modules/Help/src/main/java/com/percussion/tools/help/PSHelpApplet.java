/******************************************************************************
 *
 * [ PSHelpApplet.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.tools.help;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Applet Class for launching JavaHelp viewer from browser.
 */
public class PSHelpApplet extends JApplet
{
   /**
    * Init function for applet. Gets helpset file url and help topic id
    * from applet parameters.
    */
   public void init()
   {
      //Get the JavaHelp helpset file and attach the protocol based on its
      //location.
      String helpFile = getParameter(HELPSETFILE);
      String helpSetURL =
         PSJavaHelp.getHelpSetURL(helpFile, true, getCodeBase().toString());
      m_help = PSJavaHelp.getInstance();
      m_help.clearBroker();
      if(helpSetURL != null && helpSetURL.trim().length() != 0)
         m_help.setHelpSet(helpSetURL);

      m_helpID = getParameter(HELPTOPICID);

      JLabel iconLabel = null;
      URL iconURL = null;
      ImageIcon helpIcon = null;

      // Get the icon
      String iconHref = getParameter(HELPICON);

      if(iconHref != null && iconHref.length() > 0)
      {
         iconURL = getCorrectedURL(iconHref);

         helpIcon = new ImageIcon(
             getImage(iconURL)
            );
      }

      if(null == iconURL ||
         helpIcon.getImageLoadStatus() == MediaTracker.ABORTED ||
            helpIcon.getImageLoadStatus() == MediaTracker.ERRORED)
      {
         // If icon does not exist:
         // Create the iconLabel as a psuedo button from alt text
         // string or default text if alt text is not passed in.
         String altText = null;
         if(null != getParameter(HELPALTTEXT) &&
            getParameter(HELPALTTEXT).length()>0)
         {
            altText = getParameter(HELPALTTEXT);
         }
         else
         {
            altText = DEFAULTHELPTEXT;
         }

         iconLabel = new JLabel(altText,JLabel.CENTER);
         iconLabel.setBackground(Color.lightGray);
         iconLabel.setForeground(Color.black);
         iconLabel.setBorder(BorderFactory.createRaisedBevelBorder());
         iconLabel.setOpaque(true);
      }
      else
      {
         // Create a label to display the icon
         iconLabel = new JLabel(helpIcon,JLabel.CENTER);

      }

      // Create a panel and set color to correct background
      // color.
      JPanel panel = new JPanel();
      panel.setBackground(DEFAULTBKGCOLOR);
      // Add label to panel
      panel.add(iconLabel);
      // Add the panel to the Applet
      getContentPane().add(panel);

      // Add mouse listener to handle mouse events
      iconLabel.addMouseListener(new HelpMouseListener());


   }

   /**
    * Displays help for the current help topic.
    */
   public void showHelp()
   {
      PSJavaHelp.launchHelp(m_helpID, true, null);
   }

   /**
    * Displays help for the help topic id specified.
    * 
    * @param helpId the help topic id to be displayed,
    * may be <code>null</code>. If the value is <code>null</code>
    * then we default to displaying the current help topic.
    */
   public void showHelp(String helpId)
   {
      if(helpId == null)
         showHelp();
      PSJavaHelp.launchHelp(helpId, true, null);
   }

   /**
    * Returns the context corrected image url
    * @param imageURLString the image url string
    *  passed in. Must not be <code>null</code>.
    * @returns url corrected to be in the correct context.
    */
    private URL getCorrectedURL(String imageURLString)
    {
       URL theURL = null;

       try
       {
         URL base = getDocumentBase();
         String context = base.getPath().
            substring(0,base.getPath().indexOf("/",1));

         theURL = new URL("http://" +
                          base.getHost()+
                          ":"+
                          base.getPort()+
                          context+
                          imageURLString
                         );


       }
       catch(MalformedURLException e)
       {
          e.printStackTrace();
       }

       return theURL;
    }


   /**
    * Inner class to handle mouse events
    * for this Applet
    */
   class HelpMouseListener extends MouseAdapter
    {
        /**
        * Invokes showHelp method when mouse is clicked
        * @param evt the MouseEvent passed in
        */
        public void mouseClicked(MouseEvent evt)
        {
            // Invoke showHelp method when icon is clicked
            showHelp();
        }
        /**
        * Sets cursor to hand cursor when we mouse over icon
        * @param evt the MouseEvent passed in
        */
        public void mouseEntered(MouseEvent evt)
        {
           // Set to hand cursor when we mouse over icon
           setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        /**
        * Sets cursor to default cursor when we mouse exit the icon
        * @param evt the MouseEvent passed in
        */
        public void mouseExited(MouseEvent evt)
        {
           // Reset to default cursor when our cursor exits the
           // icon label component
           setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }



   /**
    * The help topic id to be displayed, gets initialized when the applet
    * is initialized.
    */
   private String m_helpID = null;

   /**
    * The singleton instance of java help so that it is not garbage collected,
    * initialized in <code>init()</code> and never <code>null</code> or modified
    * after that.
    */
   private PSJavaHelp m_help;

   /**
    * The name of the parameter which provides helpset file.
    */
   private static final String HELPSETFILE = "helpset_file";

   /**
    * The name of the parameter which defines help topic id to be displayed.
    */
   private static final String HELPTOPICID = "helpid";

   /**
    * The name of the parameter that provides the correct help icon
    * to display
    */
   private static final String HELPICON = "helpicon";

   /**
    * The name of the parameter that provides the correct help alt
    * text
    */
   private static final String HELPALTTEXT = "helpalt";

   /**
    * The default background color for this applet
    */
   private static final Color DEFAULTBKGCOLOR = new Color(190,197,231);

   /**
    * The default help message if no icon and no alt message exist
    */
   private static final String DEFAULTHELPTEXT = "Help (?)";


}

