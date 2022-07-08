/******************************************************************************
 *
 * [ PSAboutDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.guitools;

import javax.swing.*;
import java.applet.AppletContext;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * About dialog displays the Percussion Logo, Rhythmyx version info and has a
 * clickable link to the percussion website.
 */
public class PSAboutDialog extends JDialog
{
   /**
    * Initialies the dialog with supplied parameters.
    *
    * @param title The string to be displayed at the title bar. It must not be
    *    <code>null</code> or empty.
    *
    * @param clientVersion The current client version. It must not be
    *    <code>null</code> or empty.
    */
   public PSAboutDialog(Frame parent, String title, String clientVersion)
   {
      super(parent);

      if (title == null || title.trim().length() == 0)
         throw new IllegalArgumentException("title may not be null or empty");
      if (clientVersion == null || clientVersion.trim().length() == 0)
         throw new IllegalArgumentException(
            "clientVersion may not be null or empty");

      initDialog(title, new String[] {clientVersion}, null);
   }

   /**
    * Initialies the dialog with supplied parameters.
    *
    * @param title The string to be displayed at the title bar. It must not be
    *    <code>null</code> or empty.
    *
    * @param versions It must contain 3 strings. Assume the 1st element is the
    *    client version label, the 2nd element is the server name label, the
    *    3nd element is the server version label.
    */
   public PSAboutDialog(Frame parent, String title, String[] versions)
   {
      super(parent);

      if (title == null || title.trim().length() == 0)
         throw new IllegalArgumentException("title may not be null or empty");
      if (versions.length != 3)
         throw new IllegalArgumentException("versions must contains 3 strings");

      initDialog(title, versions, null);
   }

   /**
    * Initialies the dialog with supplied parameters.
    *
    * @param title The string to be displayed at the title bar. It must not be
    *    <code>null</code> or empty.
    *
    * @param versions It must contain 3 strings. Assume the 1st element is the
    *    client version label, the 2nd element is the server name label, the
    *    3nd element is the server version label.
    *
    * @param copyRight3Party The copy right for 3nd party software. It may be
    *    <code>null</code>.
    */
   public PSAboutDialog(Frame parent, String title, String[] versions,
      String copyRight3Party)
   {
      super(parent);

      if (title == null || title.trim().length() == 0)
         throw new IllegalArgumentException("title may not be null or empty");
      if (copyRight3Party == null)
         throw new IllegalArgumentException("copyRight3Party may not be null");
      if (versions.length != 3)
         throw new IllegalArgumentException("versions must contains 3 strings");

      initDialog(title, versions, copyRight3Party);
   }

   /**
    * internal for creating the controls and initializing the dialog.
    *
    * @param title The string to be displayed at the title bar. Assume it is
    *    never <code>null</code> or empty.
    *
    * @param versions Assume the array length is either 1 or 3. Assume the
    *    1st element is the client version label. If the length is 3, then the
    *    2nd element is the server name label, the 3nd element is the server
    *    version label.
    *
    * @param copyRight3Party The copy right for 3nd party software. It may be
    *    <code>null</code>.
    */
   private void initDialog(String title, String[] versions,
      String copyRight3Party)
   {
      this.setFocusable(true);
      this.getAccessibleContext().setAccessibleName("title");
      this.getAccessibleContext().setAccessibleName("Version information dialog.  Escape to exit.");
      String imagePath;
      if (copyRight3Party == null)
         imagePath = (versions.length == 1) ? SHORT_IMAGE : MID_IMAGE;
      else
         imagePath = LONG_IMAGE;

      ImageIcon image = new ImageIcon(getClass().getResource(imagePath));

      int width = image.getIconWidth();
      int height = image.getIconHeight();

      getContentPane().setLayout(null);
      getContentPane().setBackground(Color.white);
      setSize(width + 6, height + 30);

      // set window title
      setTitle(title);

      JLabel labelVersion = new JLabel(versions[0], JLabel.CENTER);
      labelVersion.setFocusable(true);
      //add the URL label
      Font defaultFont = labelVersion.getFont();
      Font urlFont = new Font(defaultFont.getFontName(),
                            defaultFont.getStyle(),
                            defaultFont.getSize() + 4);
      m_labelUrl = new JLabel(PERCUSSION_URL, JLabel.CENTER);
      m_labelUrl.setFocusable(true);
      m_labelUrl.setFont(urlFont);
      int yPos = 154;
      m_labelUrl.setBounds(0, yPos, width, 30);
      m_labelUrl.setForeground(Color.red.darker());
      m_labelUrl.addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         {
            onMouseClick();
         }
         public void mouseEntered(MouseEvent e)
         {
            onMouseEnter();
         }
         public void mouseExited(MouseEvent e)
         {
            onMouseExit();
         }
      });
      yPos += 10;
      getContentPane().add(m_labelUrl);

      //add the client version label
      yPos += 15;
      labelVersion.setBounds(0, yPos, width, 30);
      getContentPane().add(labelVersion);

      // add the server version label if any
      if (versions.length == 3)
      {
         yPos += 25;
         JLabel labelServerName = new JLabel(versions[1], JLabel.CENTER);
         labelServerName.setFocusable(true);
         labelServerName.setBounds(0, yPos, width, 30);
         getContentPane().add(labelServerName);

         yPos += 25;
         JLabel labelServerVersion = new JLabel(versions[2], JLabel.CENTER);
         labelServerVersion.setFocusable(true);
         labelServerVersion.setBounds(0, yPos, width, 30);
         getContentPane().add(labelServerVersion);
      }

      //add the copyright label
      yPos += 25;
      JLabel labelCopyright = new JLabel(RX_COPY_RIGHT, JLabel.CENTER);
      labelCopyright.setFocusable(true);
      labelCopyright.setBounds(0, yPos, width, 30);
      getContentPane().add(labelCopyright);

      //add the copyright for 3nd party if any
      if (copyRight3Party != null)
      {
         JTextArea otherCopyrights = new JTextArea(copyRight3Party);
         otherCopyrights.setEditable(false);
         otherCopyrights.setLineWrap(true);
         otherCopyrights.setWrapStyleWord(true);
         otherCopyrights.setFont(new Font(null, Font.PLAIN, 10));

         yPos += 35;
         otherCopyrights.setBounds(25, yPos, width - 50, 60);
         getContentPane().add(otherCopyrights);
      }

       //add the image
      JLabel imageLabel = new JLabel(image);
      imageLabel.getAccessibleContext().setAccessibleName("Percussion Logo");
      imageLabel.setOpaque(false);
      imageLabel.setBounds(0, 0, width, height);
      getContentPane().add(imageLabel);

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension size = getSize();
      setLocation(( screenSize.width - size.width ) / 2,
            ( screenSize.height - size.height ) / 2 );

      addKeyListener(new KeyListener(){

         public void keyTyped(KeyEvent e)
         {
            // TODO Auto-generated method stub

         }

         public void keyPressed(KeyEvent e)
         {
            if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
            {
               JDialog dlg = (JDialog) e.getSource();
               dlg.setVisible(false);
               dlg.dispose();
            }
         }

         public void keyReleased(KeyEvent e)
         {
            // TODO Auto-generated method stub

         }});

      setModal(true);
      setResizable(false);
   }

   /**
    * Set the applet context.
    *
    * @param appletContext The current applet context, it may not be
    *    <code>null</code>.
    */
   public void setAppletContext(AppletContext appletContext)
   {
      if (appletContext == null)
         throw new IllegalArgumentException("appletContext may not be null");

      m_appContext = appletContext;
   }

  /**
   * Handler for mouse exiting the URL label for percussion. Sets the color to
   * darker shade of blue and mouse cursor to default cursor.
   */
   protected void onMouseExit()
   {
          m_labelUrl.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          m_labelUrl.setForeground(Color.red.darker());
   }

  /**
   * Handler for when mouse is over the URL label for percussion. Sets the
   * color to blue and mouse cursor to hand cursor.
   */
   protected void onMouseEnter()
   {
          m_labelUrl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
          m_labelUrl.setForeground(Color.red.brighter());
   }

  /**
   * Handler for mouse click on the URL. Starts up the default browser and
   * displays the percussion web page if the mouse click point is within the
   * bounds of label url.
   */
   protected void onMouseClick()
   {
	   openInSystemBrowser(PERCUSSION_URL);
               }
   
   private void openInSystemBrowser(String url) {
		if (Desktop.isDesktopSupported()) {
		     // Windows
		     try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (IOException e) {
				// Not critical if error, just print stack and return
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
               }
		 } else {
		     // Ubuntu
		     Runtime runtime = Runtime.getRuntime();
		     try {
				runtime.exec("/usr/bin/firefox -new-window " + url);
			} catch (IOException e) {
				
				e.printStackTrace();
            }
      }
   }
   /**
    * The applet context of the owner applet, it is <code>null</code> if the
    * owner is not an applet. Default to <code>null</code>, but can be set by
    * setAppletContext(), never modified after that. Used to launch new browser
    * window for company url.
    */
   private AppletContext m_appContext = null;

  /**
   * The label for URL of Percussion website, initialized in <code>initDialog
   * </code> and never <code>null</code> or modified after that.
   */
   private JLabel m_labelUrl;

   /**
    * The constant for percussion home page url.
    */
   public static final String PERCUSSION_URL = "https://www.percussion.com";

   /**
    * Various public constant strings
    */
   public final static String LONG_IMAGE  = "images/about_rx_long.gif";
   public final static String MID_IMAGE   = "images/about_rx_mid.gif";
   public final static String SHORT_IMAGE = "images/about_rx_short.gif";
   public final static String RX_COPY_RIGHT  =
      "Copyright \u00a9 "
      + "1999-"
      + ResourceHelper.getResources().getString("copyrightyear")
      + " by Percussion Software, Inc.";
}
