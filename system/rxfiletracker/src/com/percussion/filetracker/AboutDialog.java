/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.filetracker;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 * Taken from XSpLit and modified slightly.<p/>
 * About dialog displays the Percussion Logo, version info and has a
 * clickable link to the percussion website.
 */
public class AboutDialog extends JDialog
{

   /**
    * constructor that takes the parent frame.
    */
   AboutDialog(JFrame parent, String title)
   {
      super(parent);
      this.setTitle(title);
      this.setResizable(false);
      getContentPane().setLayout(null);
      getContentPane().setBackground(Color.white);
      // 23 is the size of the title bar on Windows L&F
      setSize(m_width, m_height+23);
      initDialog();
      // center it
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension size = getSize();
      setLocation(( screenSize.width - size.width ) / 2,
            ( screenSize.height - size.height ) / 2 );
   }

   /**
    * internal for creating the controls and initializing the dialog.
    */
   private void initDialog()
   {
      JLabel labelVersion = new JLabel(MainFrame.getVersionString() +
                  " Copyright 2001, Percussion Software Inc.", JLabel.CENTER);

      //add the URL label
      Font defaultFont = labelVersion.getFont();
      Font urlFont = new Font(defaultFont.getFontName(),
                                          defaultFont.getStyle(),
                                          defaultFont.getSize() + 4);
      m_labelUrl = new JLabel(PERCUSSION_URL, JLabel.CENTER);
      m_labelUrl.setFont(urlFont);
      FontMetrics fm = m_labelUrl.getFontMetrics( urlFont );
      /* 
      * The height of the white area at the bottom is 90 pixels. Divide the 
      * area into 3 rows. Put the version/copywrite in the last row. Put the 
      * URL centered between the first 2 rows. 
      */
      final int WHITESPACE = 90;
      int yPos = (m_height - WHITESPACE) + WHITESPACE/3 - fm.getHeight()/2 ;
      m_labelUrl.setBounds(0, yPos, m_width, 30);
      m_labelUrl.setForeground(Color.red.darker());
      m_labelUrl.addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent e)
         {
            Rectangle rect = m_labelUrl.getBounds();
            e.translatePoint((int)rect.getX(), (int)rect.getY());
            onMouseClick(e.getPoint());
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
      getContentPane().add(m_labelUrl);

      //add the copyright label
      fm = labelVersion.getFontMetrics(labelVersion.getFont());
      yPos = (m_height - WHITESPACE) + 2*WHITESPACE/3;

      labelVersion.setBounds(0, yPos, m_width, 30);
      getContentPane().add(labelVersion);

      //add the image
      JLabel imageLabel = new JLabel(m_icon);
      imageLabel.setOpaque(false);
      imageLabel.setBounds(0, 0, m_width, m_height);
      getContentPane().add(imageLabel);
   }

   /**
    * Handler for mouse exiting the URL label for percussion. Sets the color to
    * darker shade of blue and mouse cursor to default cursor.
    */
   private void onMouseExit()
   {
      m_labelUrl.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      m_labelUrl.setForeground(Color.red.darker());
   }

   /**
    * Handler for when mouse is over the URL label for percussion. Sets the 
    * color to blue and mouse cursor to hand cursor.
    */
   private void onMouseEnter()
   {
      m_labelUrl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      m_labelUrl.setForeground(Color.red.brighter());
   }

   /**
    * Handler for mouse click on the URL. Starts up the default browser and 
    * displays the percussion web page.
    */
   private void onMouseClick(Point p)
   {
      Rectangle rect = m_labelUrl.getBounds();
      if(rect != null)
      {
         if(rect.contains(p))
         {
            UTBrowserControl.displayURL(PERCUSSION_URL);
         }
      }
   }

   /**
    * For testing the dialog.
    */
/*
   public static void main(String[] args)
   {
      final JFrame frame = new JFrame("Test About Dialog");
      frame.addWindowListener(new BasicWindowMonitor());
      try
      {
         String strLnFClass = UIManager.getSystemLookAndFeelClassName();
         LookAndFeel lnf = (LookAndFeel) Class.forName(strLnFClass).newInstance();
         UIManager.setLookAndFeel( lnf );

         JButton startButton = new JButton("Open Dialog");
         frame.getContentPane().add(startButton);
         startButton.addActionListener(new ActionListener()
         {
            public void actionPerformed(ActionEvent e)
            {
               AboutDialog dialog = new AboutDialog(frame);
               dialog.setLocationRelativeTo(frame);
               dialog.setVisible(true);
            }
         });

         frame.setSize(400, 300);
         frame.setVisible(true);
       }
       catch (Exception e)
       {
         System.out.println(e);
       }
   }
*/
   /**
    * the label for URL of Percussion website.
    */
   JLabel m_labelUrl = null;

   public static final String PERCUSSION_URL = "http://www.percussion.com";


   //Image for to display in the dialog box.
   private ImageIcon m_icon = new
                  ImageIcon(getClass().getResource("images/aboutfudmgr.gif"));
   //width of the icon
   private int m_width = m_icon.getIconWidth();
   //height of the icon
   private int m_height = m_icon.getIconHeight();
}
