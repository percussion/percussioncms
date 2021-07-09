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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.ImageListControl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

/** This class creates a panel with a horizontal list of images and labels,
  * scrollable by a button on each side of the panel.  A custom renderer is
  * used to display the image with a JLabel directly below it.  The basic
  * functionality will allow users to select an image cell, then the information
  * related to that image cell will be displayed below.  Programmers using this
  * package should populate the ListModel with ImageListItem objects.  The
  * ImageListItem object can allow the programmer to access the data object
  * associated with the display object by calling (ImageListItem).getData.
  *
  * @see ImageListItem
*/

public class ImageListControl extends JPanel
{
//
// CONSTRUCTORS
//

  public ImageListControl(Object[] array)
  {
    init(array);
  }

  public ImageListControl(Vector vector)
  {
    init(vector.toArray());
  }

//
// PUBLIC METHODS
//

/** @returns JList The JList (ImageListControlList) reference for setting the
  * list configuration. *NOTE* USE THE <CODE>setPreferredSize()</CODE> METHOD
  * FROM THIS CLASS (ImageListControl), NOT FROM THE <CODE>JList</CODE> RETURNED
  * BY THIS METHOD.
*/
  public JList getListData()
  {
    return m_list;
  }

/** Enables/disables the left button.
*/
  public void setLeftButtonEnabled(boolean b)
  {
    m_leftButton.setEnabled(b);
  }

/** Enables/disables the right button.
*/
  public void setRightButtonEnabled(boolean b)
  {
    m_rightButton.setEnabled(b);
  }

/** Retrieves the ImageListItem object at element index.
  *
  * @param index The index of the ImageListItem object to be returned.
  * @return Object The ImageListItem object at index.
*/
  public Object getElementAt(int index)
  {
    return m_list.getModel().getElementAt(index);
  }

/** Instead of using setVisibleRowCount(), this methods is overridden to give
  * programmer more control over the size of this component.
  *
  * @param d The Dimension specified also includes the sizes of the buttons on
  *          the left and the right.
*/
  public void setPreferredSize(Dimension d)
  {
    int leftInset = m_leftButton.getPreferredSize().width;
    int rightInset = m_rightButton.getPreferredSize().width;

    int newWidth = d.width - (leftInset + rightInset);

    Dimension newSize = new Dimension(newWidth, d.height);

    m_list.setViewportSize(newSize);

    m_leftButtonPanel.setPreferredSize(new Dimension(leftInset, d.height));
    m_rightButtonPanel.setPreferredSize(new Dimension(rightInset, d.height));

    m_viewport.revalidate();
    //m_viewport.repaint();
  }

//
// PRIVATE METHODS
//

/** Creates the components withing the ImageListControl panel.
*/
  private void init(Object[] array)
  {
    m_list = new ImageListControlList(array);
    m_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    m_renderer = new ImageListControlRenderer();

    m_list.setCellRenderer(m_renderer);
    m_list.setUI(new ImageListControlUI());
    m_list.setVisibleRowCount(2);
    m_list.setBackground(Color.white);

    m_viewport = new JViewport();
    m_viewport.setView(m_list);

    m_viewport.setExtentSize(m_list.getSize());

    createButtons();

    m_leftButtonPanel = new JPanel(new BorderLayout());
    m_leftButtonPanel.add(m_leftButton, BorderLayout.CENTER);
    m_leftButtonPanel.setPreferredSize(m_leftButton.getPreferredSize());

    m_rightButtonPanel = new JPanel(new BorderLayout());
    m_rightButtonPanel.add(m_rightButton, BorderLayout.CENTER);
    m_rightButtonPanel.setPreferredSize(m_rightButton.getPreferredSize());

    this.setBackground(Color.white);
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    this.add(Box.createHorizontalGlue());
    this.add(m_leftButtonPanel);
    this.add(m_viewport);
    this.add(m_rightButtonPanel);
    this.add(Box.createHorizontalGlue());
    this.setBorder(new BevelBorder(BevelBorder.LOWERED, Color.white,
                                                        Color.lightGray,
                                                        Color.black,
                                                        Color.darkGray));

    int listSize = m_list.getModel().getSize();
    int preferredWidth = m_renderer.getListCellRendererComponent(m_list, m_list.getModel().getElementAt(0), 0, false, false).getPreferredSize().width;
    if (1 < listSize)
      preferredWidth = preferredWidth + preferredWidth / 2;
    else if (1 != listSize)
      preferredWidth = 100;

    setPreferredSize(new Dimension(preferredWidth, m_list.getMaxPreferredHeight()));

  }

/** Creates the 2 buttons, one on the right and the other on the left.
*/
  private void createButtons()
  {
    int height = (int)m_viewport.getExtentSize().getHeight();

    //System.out.println(height);

    m_leftButton = new JButton(new ImageIcon(getClass().getResource("images/leftbutton.gif")));
    m_leftButton.setPreferredSize(new Dimension(20, height));
    m_leftButton.setMinimumSize(m_leftButton.getPreferredSize());
    m_leftButton.setEnabled(false);
    m_leftButton.addActionListener(new ActionListener()
    {
       public void actionPerformed(ActionEvent e)
       {
         int inc = m_list.getScrollableUnitIncrement(m_viewport.getViewRect(),
                                                     SwingConstants.HORIZONTAL,
                                                     0);

         if (inc == 0)
         {
           setLeftButtonEnabled(false);
           setRightButtonEnabled(true);
         }
         else
         {
           setLeftButtonEnabled(true);
           setRightButtonEnabled(true);
         }
         Point spot = m_viewport.getViewPosition();

         spot.x = spot.x - inc;

         m_viewport.setViewPosition(spot);
       }
    });


    m_rightButton = new JButton(new ImageIcon(getClass().getResource("images/rightbutton.gif")));
    m_rightButton.setPreferredSize(new Dimension(20, height));
    m_rightButton.setMinimumSize(m_rightButton.getPreferredSize());
    if (1 == m_list.getModel().getSize())
    {
      m_rightButton.setEnabled(false);
    }
    m_rightButton.addActionListener(new ActionListener()
    {
       public void actionPerformed(ActionEvent e)
       {
         int inc = m_list.getScrollableUnitIncrement(m_viewport.getViewRect(),
                                                     SwingConstants.HORIZONTAL,
                                                     1);
         if (inc == 0)
         {
           setRightButtonEnabled(false);
           setLeftButtonEnabled(true);
         }
         else
         {
           setRightButtonEnabled(true);
           setLeftButtonEnabled(true);
         }

         Point spot = m_viewport.getViewPosition();

         spot.x = spot.x + inc;

         m_viewport.setViewPosition(spot);
       }
    });
  }


//
// MEMBER VARIABLES
//

  private JButton m_leftButton, m_rightButton;
  private JPanel m_leftButtonPanel, m_rightButtonPanel;
  private ImageListControlList m_list;
  private JViewport m_viewport;

  private ImageListControlRenderer m_renderer = null;

  private int m_maxPreferredHeight = 0;
}

