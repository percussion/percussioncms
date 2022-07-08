/******************************************************************************
 *
 * [ PSCalendarButton.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.guitools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Class to provide a calendar popup from a button and allow the caller to 
 * retrieve the selected date.  Calendar popup is provided by 
 * {@link PSCalendarDialog}.
 * <p>
 * Users of this class should call 
 * {@link #addDateChangedListener(ActionListener)}.  This will call back using
 * {@link ActionListener#actionPerformed(ActionEvent)} twice.  Once when the 
 * button is first clicked but before the pop-up calendar is displayed, to allow 
 * the date and/or owner to be set, and once after the pop-up is closed to allow
 * the selected date to be retrieved.  {@link ActionEvent#getActionCommand()}
 * will return {@link #BUTTON_PRESSED_CMD} and {@link #DATE_UPDATED_CMD}
 * respectively.  
 */
public class PSCalendarButton extends JButton implements ActionListener
{
   /**
    * Ctor that implements the default behavior for this class.  Uses the 
    * standard calendar icon and tooltip text for the button.
    * 
    * @param owner The owning frame, used to make the pop-up dialog modal, may
    * be <code>null</code>.
    * @param date The initial date to use to set the calendar popup when it is
    * first displayed, may be <code>null</code>. 
    */
   public PSCalendarButton(Frame owner, Date date)
   {
      super();
      
      setOwner(owner);
      setDate(date);
      initIcon();
      initToolTip();
      addActionListener(this);      
   }
   
   /**
    * Sets the date to display when this button is pressed.
    * 
    * @param date The date, may be <code>null</code> to use today's date.
    */
   public void setDate(Date date)
   {
      if (date != null)
         m_date = (Date)date.clone();
      else
         m_date = new Date();
   }
   
   /**
    * Sets the owning frame of this button.
    * 
    * @param owner The owning frame, used to make the pop-up dialog modal, may
    * be <code>null</code>.
    */
   public void setOwner(Frame owner)
   {
      if (owner == null)
         owner = getOwnerFrame(this);
         
      m_frame = owner;
   }
   
   /**
    * Initializes the default tool tip for this icon.
    */
   private void initToolTip()
   {
      setToolTipText(
         ms_res.getString("tooltip.calendar.button"));
   }
   
   /**
    * Initializes the icon for this button.
    */
   private void initIcon()
   {
      ImageIcon image =
         new ImageIcon(PSCalendarButton.class.getResource(
            ms_res.getString("gif.calendar.icon")));
      Dimension imageSize =
         new Dimension(image.getIconWidth() + 2, image.getIconHeight() + 3);

      setIcon(image);

      setPreferredSize(imageSize);
      setMaximumSize(imageSize);
   }
   
   /**
    * Handles button click to pop-up calendar window. Informs listeners of the
    * button click so they may set the date and or owner before the pop-up
    * is displayed.  After the pop-up is closed, informs the listeners again
    * so they may retrieve the date value selected. 
    */
   public void actionPerformed(ActionEvent event)
   {
      Object source = event.getSource();
      if(source == this)
      {
         // first let listeners set a new date
         informListeners(BUTTON_PRESSED_CMD);
         
         // now show dialog
         
         PSCalendarDialog dialog = createDialog();
         dialog.setDate(m_date);
         dialog.setLocation(getAdjustedLocation(this, dialog.getSize()));
         dialog.setVisible(true);
         
         // inform listeners of date change
         if(dialog.isDateSelected())
         {
            m_date = dialog.getDate();
            informListeners(DATE_UPDATED_CMD);
         }
         dialog.dispose();
      }   
   }

   /**
    * Creates dialog to show. 
    */
   protected PSCalendarDialog createDialog()
   {
      return new PSCalendarDialog(m_frame);
   }
   
   /**
    * Adjusts location for the calendar dialog popup so that it stays on 
    * screen
    * 
    * @param comp the component which launched the dialog (i.e. the button),
    * assumed not <code>null</code>.
    * @param dim the dimensions of the calendar dialog, assumed not 
    * <code>null</code>
    * @return the point with x, y coordinates adjusted if needed. 
    * Never <code>null</code>.
    */
   private Point getAdjustedLocation(Component comp, Dimension dim)
   {
      Toolkit toolkit = Toolkit.getDefaultToolkit();
      Dimension screenSize = toolkit.getScreenSize();
      Insets insets = toolkit.getScreenInsets(comp.getGraphicsConfiguration());
      Point loc = comp.getLocationOnScreen();
      // Adjust y
      int locY = loc.y + dim.height;
      int screenBottom = screenSize.height - insets.bottom;
      if(locY >= screenBottom)
      {
         loc.y = screenBottom - dim.height;
      }
      // Adjust x
      int locX = loc.x + dim.width;
      int screenRight = screenSize.width - insets.right;
      if(locX >= screenRight)
      {
         loc.x = screenRight - dim.width;
      }
      return loc;
   }
   
   
   /**
    * Register the supplied listener to be notified if this button is clicked 
    * or a new date is selected.
    * 
    * @param listener The listener to inform, may not be <code>null</code>.  
    * When notified, <code>ActionEvent.getSource()</code> will return this
    * button.  One of two commands will be returned by 
    * <code>ActionEvent.getActionCommand()</code>:
    * <ol> 
    * <li>{@link #BUTTON_PRESSED_CMD} - this provides an opportunity for the 
    * listener to call {@link #setDate(Date)} or {@link #setOwner(Frame)}
    * before the calendar pop-up is displayed. </li>
    * <li>{@link #DATE_UPDATED_CMD} - This informs the listener that a date
    * has been selected, and the implementor should then call 
    * {@link #getDate()} to get the date that was selected.</li>
    * </ol>
    */
   public void addDateChangedListener(ActionListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
      
      m_listeners.add(listener);
   }
   
   /**
    * De-registers the supplied listener for event notification.
    *  
    * @param listener The listener to remove, may not be <code>null</code>.
    */
   public void removeDateChangedListener(ActionListener listener)
   {
      if (listener == null)
         throw new IllegalArgumentException("listener may not be null");
      
      m_listeners.remove(listener);
   }

   /**
    * Informs listeners using the specified command.
    * 
    * @param cmd The command to use for the action event, assumed not 
    * <code>null</code> or empty.  
    */
   private void informListeners(String cmd)
   {
      ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, cmd);
      Iterator listeners = m_listeners.iterator();
      while (listeners.hasNext())
      {
         ActionListener listener = (ActionListener)listeners.next();         
         listener.actionPerformed(e);
      }
   }
   
   /**
    * Attempts to find the closest parent frame by
    * looping back through parents
    * @return the owner frame or <code>null</code>
    * if the frame could not be found.
    */
   private Frame getOwnerFrame(Component comp)
   {
      if(null == comp)
         return null;
      if(comp instanceof Frame)
         return (Frame)comp;
      return getOwnerFrame(comp.getParent());
   }
   
   
   /**
    * Get the last date selected.
    * 
    * @return The date, never <code>null</code>.  If no date has been selected,
    * will be the current date.
    */
   public Date getDate()
   {
      return m_date;
   }
   
   /**
    * Command used for action events to inform listeners of date changes.  See
    * {@link #addDateChangedListener(ActionListener)} for more info.
    */
   public static final String DATE_UPDATED_CMD = "DATE_UPDATED";
   
   /**
    * Command used to inform listeners this button has been pressed before the
    * calendar pop-up is displayed.   See
    * {@link #addDateChangedListener(ActionListener)} for more info.
    */
   public static final String BUTTON_PRESSED_CMD = "BUTTON_PRESSED";

   /**
    * The last date selected, never <code>null</code>, initialized to the 
    * current date.
    */
   private Date m_date = new Date();
   
   /**
    * The parent frame containing this button, may be <code>null</code> if one
    * was not supplied during construction.
    */
   private Frame m_frame;
   
   /**
    * Listeners to inform of date selection event, never <code>null</code>, may
    * be empty.
    */
   private List<ActionListener> m_listeners = new ArrayList<ActionListener>();
   
   /**
    * Resource bundle reference. Never <code>null</code> after that.
    */
   private static ResourceBundle ms_res;

   static
   {
      try
      {
         ms_res = ResourceHelper.getResources();
      }
      catch(MissingResourceException ex)
      {
         System.out.println(ex);
         throw ex;
      }
   }
}
