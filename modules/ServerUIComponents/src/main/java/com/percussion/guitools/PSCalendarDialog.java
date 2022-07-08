/******************************************************************************
 *
 * [ PSCalendarDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.guitools;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Calendar dialog for the user to point and click
 * to enter the date.
 * <p>
 * Example usage:
 * </p>
 * <p>
 * <pre>
 * <code>
 *       PSCalendarDialog dialog = new PSCalendarDialog(ownerFrame);
 *       dialog.setDate(theDate); // Set the dialog to its initial date
 *       dialog.center();         // Center the dialog on the screen
 *       dialog.show();           // Display the dialog
 *
 *       if(dialog.isDateSelected())
 *          setDate(dialog.getDate());
 * </code>
 * </pre>
 * </p>
 * <p>
 *  If the date is not set or is <code>null</code>, then the
 *  calendar will use today's date as the initial date.
 * </p>
 */
public class PSCalendarDialog extends JDialog
{
    /**
     * Creates a new modal CalendarDialog
     * @param owner the frame that owns this dialog
     */
    PSCalendarDialog(Frame owner)
    {
       super(owner, true);
       init();
    }

    /**
     * Initialize the gui components
     */
    private void init()
    {
       setTitle(ms_res.getString("calendar.dialog.title"));
       Container contentPane = getContentPane();
       contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
       m_calendarPanel = new CalendarPanel();
       contentPane.add(m_calendarPanel);
       setResizable(false);
       pack();

    }

    /**
     * Sets the date for this calendar
     * @param date date object. May be <Code>null</code>.
     */
    public void setDate(Date date)
    {
        if(null == date)
           return;
        m_calendarPanel.setCalendarDate(date);
        m_calendarPanel.loadCalendar();
    }

    /**
     * Sets the date for this calendar
     * @param year the year integer ( i.e. 2003)
     * @param month the month integer ( 0 - 11; Example: February = 2)
     * @param date the date integer (1 - 31)
     */
    public void setDate(int year, int month, int date)
    {
        m_calendarPanel.setCalendarDate(
           new GregorianCalendar(year, month, date).getTime());
        m_calendarPanel.loadCalendar();
    }

    /**
     * Returns the date that the Calendar dialog
     * is currently set to.
     * @return the date. Never <code>null</code>.
     */
    public Date getDate()
    {
       return m_calendarPanel.getCalendar().getTime();
    }

   /**
    * Centers the dialog on the screen, based on its current size.
    */
   public void center()
   {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension size = getSize();
      setLocation(( screenSize.width - size.width ) / 2,
            ( screenSize.height - size.height ) / 2 );
   }

   /**
    * Indicates that date was actually selected
    * @return <code>true</code> if date was selected, else
    * <code>false</code>.
    */
   public boolean isDateSelected()
   {
      return m_bDateSelected;
   }

   /**
    * Sets the foreground color of the calendar grid
    * @param color. Never <code>null</code>.
    */
   public void setGridForeground(Color color)
   {
      if(null == color)
         return;
      m_calendarPanel.setGridForeground(color);
   }

   /**
    * Sets the background color of the calendar grid
    * @param color. Never <code>null</code>.
    */
   public void setGridBackground(Color color)
   {
      if(null == color)
         return;
      m_calendarPanel.setGridBackground(color);
   }

   /**
    * Sets the foreground color of the calendar header
    * @param color. Never <code>null</code>.
    */
   public void setHeaderForeground(Color color)
   {
      if(null == color)
         return;
      m_calendarPanel.setHeaderForeground(color);
   }

   /**
    * Sets the background color of the calendar header
    * @param color. Never <code>null</code>.
    */
   public void setHeaderBackground(Color color)
   {
      if(null == color)
         return;
      m_calendarPanel.setHeaderBackground(color);
   }

   /**
    * Sets the current date foreground color.
    * @param color. Never <code>null</code>.
    */
   public void setCurrentDateForeground(Color color)
   {
      if(null == color)
         return;
      m_calendarPanel.m_gridCurrentForeground = color;
   }

   /**
    * Sets the mouseover border color
    * @param color. Never <code>null</code>.
    */
   public void setMouseOverBorderColor(Color color)
   {
      if(null == color)
         return;
      m_calendarPanel.m_mouseOverBorderColor = color;
   }

   /**
    * Sets the mouse over background color
    * @param color. Never <code>null</code>.
    */
   public void setMouseOverBackground(Color color)
   {
      if(null == color)
         return;
      m_calendarPanel.m_mouseOverBackground = color;
   }

   /**
    * Set the year selection popup offset.
    * @param offset the number previous and next years
    * to display in the year selection popup. Defaults to 3
    */
    public void setYearPopupOffset(int offset)
    {
       m_yearPopupOffset = offset;
    }

    /**
     * Panel that represents the calendar grid
     */
    class CalendarPanel extends JPanel
    {

       /**
        * Construct a new calendar panel
        */
       CalendarPanel()
       {
           super();
           init();
           loadCalendar();
       }

       /**
        * Initialize the gui components, build the calendar grid
        */
       public void init()
       {
           CalendarActionListener actionListener = new CalendarActionListener();

           setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
           JPanel panel = new JPanel(new BorderLayout());
           Dimension calSize = new Dimension(340, 160);
           panel.setPreferredSize(calSize);
           panel.setMaximumSize(calSize);
           panel.setBorder(BorderFactory.createEtchedBorder());
           JLabel label = null;
           m_grid = new ArrayList(35);
           m_header = new ArrayList(7);

           // Build calendar header
           JPanel headerpanel = new JPanel(new GridLayout(1, 7, 2, 2));
           for(int i = 0; i < ms_headerText.length; i++)
           {
              label = new JLabel(ms_headerText[i]);
              m_header.add(label);
              label.setBackground(m_headerBackground);
              label.setForeground(m_headerForeground);
              label.setHorizontalAlignment(JLabel.CENTER);
              label.setOpaque(true);
              headerpanel.add(label);
           }
           panel.add(headerpanel, BorderLayout.NORTH);

           // Build calendar grid
           JPanel gridpanel = new JPanel(new GridLayout(6, 7, 2, 2));
           for(int i = 0; i < 42; i++)
           {
              CalendarDayButton button = new CalendarDayButton(JButton.CENTER, JButton.CENTER, true);
              button.addActionListener(actionListener);
              m_grid.add(button);
              gridpanel.add(button);
           }
           panel.add(gridpanel, BorderLayout.CENTER);

           // Build control panel
           JPanel controlPanel = new JPanel();
           controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));

           ImageIcon arrowL = new ImageIcon(getClass().getResource(ms_res.getString("gif.left.arrow")));
           ImageIcon arrowR = new ImageIcon(getClass().getResource(ms_res.getString("gif.right.arrow")));
           m_prevButton = new JButton(arrowL);
           m_prevButton.addActionListener(actionListener);
           m_prevButton.setToolTipText(ms_res.getString("tooltip.prev.month"));

           m_nextButton = new JButton(arrowR);
           m_nextButton.addActionListener(actionListener);
           m_nextButton.setToolTipText(ms_res.getString("tooltip.next.month"));

           // load the drop down menu for the months
           m_monthList = null;
           loadMonthList();
           m_monthList.addActionListener(actionListener);

           // load the drop down menu for the year
           m_yearList = null;
           loadYearList();
           m_yearList.addActionListener(actionListener);

           controlPanel.add(m_prevButton);
           controlPanel.add(Box.createHorizontalGlue());
           controlPanel.add(m_monthList);
           controlPanel.add(Box.createHorizontalStrut(10));
           controlPanel.add(m_yearList);
           controlPanel.add(Box.createHorizontalGlue());
           controlPanel.add(m_nextButton);
           controlPanel.setFocusable(true);

           add(Box.createVerticalStrut(5));
           add(controlPanel);
           add(Box.createVerticalStrut(5));
           add(panel);
           add(Box.createVerticalStrut(10));
         }

        /**
         * Add the items in the years combo box. It initializes m_yearList, so
         * it is never <code>null</code> after this method.
         * 
         */
        private void loadYearList()
        {
           ActionListener listener = null;
           if (m_yearList == null)
           {
               m_yearList = new JComboBox();
           }
           else
           {
              // remove the action listener so the fired event
              // because of the modification of the elements
              listener = m_yearList.getActionListeners()[0];
              m_yearList.removeActionListener(listener);
            
              m_yearList.removeAllItems();
           }
            
           // set uneditable because the user should not be able to type on it
           m_yearList.setEditable(false);
           
           // fill the drop down list
           int year = m_currCalendar.get(Calendar.YEAR);
           for (int i = (year - m_yearPopupOffset), j = 0; i < (year + m_yearPopupOffset + 1); i++, j++)
           {
              m_yearList.addItem(String.valueOf(i));
               if (String.valueOf(m_currCalendar.get(Calendar.YEAR)).equals(String.valueOf(i)))
               {
                   m_yearList.setSelectedIndex(j);
               }
           }
           // add the listener again
           m_yearList.addActionListener(listener);
       }

       /**
        * Add the items in the month combo box. It initializes m_monthList, so
        * it is never <code>null</code> after this method.
        * 
        */
       private void loadMonthList()
       {
          m_monthList = new JComboBox();

          // set uneditable because the user should not be able to type on it
          m_monthList.setEditable(false);

          // fill the drop down list
           for(int i = 0; i < ms_months.length; i++)
           {
             m_monthList.addItem(ms_months[i]);
          }
          // Select the right month
          m_monthList.setSelectedIndex(m_calendar.get(Calendar.MONTH));
           }

       /**
        * Changes the selection of the month list if the user hits on the
        * previous month button or the next month button.
        */
       private void reloadMonthList()
       {
          m_monthList.setSelectedIndex(m_currCalendar.get(Calendar.MONTH));
       }

       /**
        * Returns reference to the <code>Calendar</code> object.
        * @return Calendar object. Never <code>null</code>.
        */
       public Calendar getCalendar()
       {
          return m_calendar;
       }

       /**
        * Loads calendar to represent the month/year currently
        * selected by the user.
        */
       private void loadCalendar()
       {
           m_currCalendar.set(Calendar.DATE, 1);

           int month = m_currCalendar.get(Calendar.MONTH);
           int year = m_currCalendar.get(Calendar.YEAR);

           int firstDay = m_currCalendar.get(Calendar.DAY_OF_WEEK);
           int lastDay = m_currCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

           Iterator it = m_grid.iterator();
           int count = 0;
           Date tempDate = decrementDate(m_currCalendar.getTime(), firstDay);
           while(it.hasNext())
           {
              tempDate = incrementDate(tempDate, 1);
              CalendarDayButton current = (CalendarDayButton) it.next();
              // Set defaults
              current.setForeground(m_gridForeground);
              current.setCurrentSelection(false);
              current.setCalendar(tempDate);

              if(month != current.getCalendar().get(Calendar.MONTH))
              {
                 // This date is not in the current month and
                 // should be grayed out
                 current.setForeground(new Color(127, 127, 127));
              }
              if(calendarDatesEqual(m_calendar, current.getCalendar()))
              {
                 // If this is selected date then  render
                 // selection circle
                 current.setCurrentSelection(true);
                 reloadMonthList();
                 loadYearList();
              }
              if(current.isToday())
              {
                 // If this is today's date then change color
                 // to indicate it is today.
                 current.setForeground(m_gridCurrentForeground);
              }
              count++;
           }

           // set the Accessible information to the calendar. The accessible
           // name will be like this:
           // Calendar Dialog, September, 2011
           AccessibleContext aCtx = getAccessibleContext();
           if (aCtx != null)
           {
              aCtx.setAccessibleName("Calendar Dialog, " + ms_months[m_currCalendar.get(Calendar.MONTH)] + ", "
                         + m_currCalendar.get(Calendar.YEAR));
           }
           setToolTipText("Calendar Dialog, " + ms_months[m_currCalendar.get(Calendar.MONTH)] + ", "
                         + m_currCalendar.get(Calendar.YEAR));
       }

       /**
        * Increment the date by the number of days passed in
        * @param date the date to be incremented. May not be
        * <code>null</code>.
        * @param num number of days to increment the date by.
        * @return a new date instance with the adjusted date. Never
        * <code>null</code>.
        */
       private Date incrementDate(Date date, int num)
       {
           if(null == date)
              throw new IllegalArgumentException("Date cannot be null");
          return new Date(date.getTime() + (ONE_DAY * (long)num));
       }

       /**
        * Decrement the date by the number of days passed in
        * @param date the date to be decremented. May not be
        * <code>null</code>.
        * @param num number of days to decrement the date by.
        * @return a new date instance with the adjusted date. Never
        * <code>null</code>.
        */
       private Date decrementDate(Date date, int num)
       {
           if(null == date)
              throw new IllegalArgumentException("Date cannot be null");
           return new Date(date.getTime() - (ONE_DAY * (long)num));
       }

       /**
        * Compares two calendars for date equality.
        * @param calA calendar to check, May be <code>null</code>.
        * @param calB calendar to check, May be <code>null</code>.
        * @return <code>true</code> if both calendars are set to
        * eqivilent dates, else <code>false</code>.
        */
       private boolean calendarDatesEqual(Calendar calA, Calendar calB)
       {
          if(null == calA || null == calB)
             return false;
          return (calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
             calA.get(Calendar.MONTH) == calB.get(Calendar.MONTH) &&
                calA.get(Calendar.DATE) == calB.get(Calendar.DATE));
       }

       /**
        * Decrement calendar by one month
        */
       private void previousMonth()
       {
          int month = m_currCalendar.get(Calendar.MONTH);
          int year = m_currCalendar.get(Calendar.YEAR);
          if(month < 1)
          {
             month = 11;
             --year;
          }
          else
          {
             --month;
          }
          m_currCalendar.set(Calendar.MONTH, month);
          m_currCalendar.set(Calendar.YEAR, year);

          // reload the lists
          reloadMonthList();
          loadYearList();

          loadCalendar();
       }

       /**
        * Increment calendar by one month
        */
       private void nextMonth()
       {
          int month = m_currCalendar.get(Calendar.MONTH);
          int year = m_currCalendar.get(Calendar.YEAR);
          if(month > 10)
          {
             month = 0;
             ++year;
          }
          else
          {
             ++month;
          }
          m_currCalendar.set(Calendar.MONTH, month);
          m_currCalendar.set(Calendar.YEAR, year);
          
          // reload the lists
          reloadMonthList();
          loadYearList();

          loadCalendar();
       }

       /**
        * Set calendar dates for this panel
        * @param date
        */
       private void setCalendarDate(Date date)
       {
          m_calendar.setTime(date);
          m_currCalendar.setTime(date);
       }

       /**
        * Sets the background color of the calendar header
        * @param color. Never <code>null</code>.
        */
       private void setHeaderBackground(Color color)
       {
          if(null == color)
             return;
          m_headerBackground = color;
          if(null != m_header)
          {
             Iterator it = m_header.iterator();
             while(it.hasNext())
                ((JLabel)it.next()).setBackground(color);
          }
       }

       /**
        * Sets the foreground color of the calendar header
        * @param color. Never <code>null</code>.
        */
       private void setHeaderForeground(Color color)
       {
          if(null == color)
             return;
          m_headerForeground = color;
          if(null != m_header)
          {
             Iterator it = m_header.iterator();
             while(it.hasNext())
                ((JLabel)it.next()).setForeground(color);
          }
       }

       /**
        * Sets the background color of the calendar grid
        * @param color. Never <code>null</code>.
        */
       private void setGridBackground(Color color)
       {
          if(null == color)
             return;
          m_gridBackground = color;
          if(null != m_header)
          {
             Iterator it = m_grid.iterator();
             while(it.hasNext())
                ((JLabel)it.next()).setBackground(color);
          }
       }

       /**
        * Sets the foreground color of the calendar grid
        * @param color. Never <code>null</code>.
        */
       private void setGridForeground(Color color)
       {
          if(null == color)
             return;
          m_gridForeground = color;
          if(null != m_header)
          {
             Iterator it = m_grid.iterator();
             while(it.hasNext())
                ((JLabel)it.next()).setForeground(color);
          }
       }

       /**
        * Action listener class to handle the action performed for all of the
        * controls presents in the Calendar. These controls are the previous and 
        * next month, the one to pick up the month and year and each of the days 
        * in the calendar.
        * 
        * @author Santiago M. Murchio
        * 
        */
       private class CalendarActionListener implements ActionListener
       {
          public void actionPerformed(ActionEvent event)
           {
              Object source = event.getSource();
             if (source == m_monthList)
              {
                // reload the calendar with the selected month
                int data = ((JComboBox) source).getSelectedIndex();
                m_currCalendar.set(Calendar.MONTH, data);
                loadCalendar();
              }
             else if (source == m_yearList)
              {
                // reload the calendar with the selected year
                Object dataString = ((JComboBox) source).getSelectedItem();
                if (dataString != null)
                {
                   int data = Integer.valueOf(((String) dataString).toString());
                   m_currCalendar.set(Calendar.YEAR, data);
                   loadCalendar();
              }
             }
             else if (source == m_prevButton)
              {
                 previousMonth();
              }
             else if (source == m_nextButton)
              {
                 nextMonth();
              }
              else
              {
                CalendarDayButton dayButton = (CalendarDayButton) event.getSource();
                 // Date is considered selected if it has a non null
                 // value and the user clicks on it.
                if (null != dayButton.getText())
                 {
                   m_calendar.setTime(dayButton.getCalendar().getTime());
                    m_bDateSelected = true;
                    dispose();
                 }
             }
              }
           }

           /**
        * This class is the one we will use in the calendar for holding the
        * days. This way the user will be able to navigate through the dialog
        * with the keyboard using the TAB key. Introduced for bug RX-17051.
        * 
        * @author Santiago M. Murchio
        * 
            */
       private class CalendarDayButton extends JButton
              {
           /**
            * The <code>Calendar</code> object for this label. Defaults to the
            * current date/time.
            */
           private Calendar m_cal = new GregorianCalendar();

       /**
            * Flag indicating that this date is the current selected date.
        */
           private boolean m_isCurrentSelection;

           public CalendarDayButton()
          {
              super();
             }

          /**
            * Constructor that builds a default calendar button and then uses
            * the parameters to set its horizontal alignment, its vertical
            * alignment and the opaque.
           */
           public CalendarDayButton(int horizontalAlignment, int verticalAlignment, boolean opaque)
           {
              super();
              setBackground(m_gridBackground);
              setForeground(m_gridForeground);
              setHorizontalAlignment(horizontalAlignment);
              setVerticalAlignment(verticalAlignment);
              setOpaque(opaque);
       }

           public CalendarDayButton(String value)
       {
              super(value);
           }

           /**
            * Construct a new CalendarLabel object setting it to the date in
            * the Calendar passed in.
            * 
            * @param calendar. May not be <code>null</code>.
            */
           public CalendarDayButton(Calendar calendar)
           {
              this();
              setCalendar(calendar);
           }

           /**
            * Construct a new <code>CalendarLabel</code> object setting it to
            * the date passed in.
            * 
            * @param year year as integer
            * @param month month as integer (0 - 11)
            * @param date the date integer (1 - 31)
            */
           public CalendarDayButton(int year, int month, int date)
           {
              this();
              setCalendar(year, month, date);
           }

           /**
            * Returns the date for this label
            * 
            * @return the date. Never <code>null</code>.
            */
           public Date getDate()
           {
              return m_cal.getTime();
           }

           /**
            * Returns a reference to the <code>Calendar</code> object for this
            * label.
            * 
            * @return the calendar object. Never <code>null</code>.
            */
           public Calendar getCalendar()
           {
              return m_cal;
           }

           /**
            * Sets the current selection flag
            * 
            * @param bool if <code>true</code> then a rectangle will be drawn
            *            around the date integer.
            */
           public void setCurrentSelection(boolean bool)
           {
              m_isCurrentSelection = bool;
           }

           /**
            * Indicates if the selection indicator should be drawn
            * 
            * @return if <code>true</code> then a rectangle will be drawn
            *         around the date integer.
            */
           public boolean isCurrentSelection()
           {
              return m_isCurrentSelection;
           }

           /**
            * Set the calendar to the same date as the calendar passed in.
            * 
            * @param calendar. May not be <code>null</code>.
            */
           public void setCalendar(Calendar calendar)
           {
              m_cal.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
              m_cal.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
              m_cal.set(Calendar.DATE, calendar.get(Calendar.DATE));
              setText(String.valueOf(calendar.get(Calendar.DATE)));
              setToolTipText(ms_months[m_cal.get(Calendar.MONTH)] + ", "
                      + String.valueOf(m_cal.get(Calendar.DAY_OF_MONTH)));

              setAccesibleInfo();
           }

           /**
            * Set the calendar to the same date as the integer values passed
            * in.
            * 
            * @param year year as integer
            * @param month month as integer (0 - 11)
            * @param date the date integer (1 - 31)
            */
           public void setCalendar(int year, int month, int date)
           {
              m_cal.set(Calendar.YEAR, year);
              m_cal.set(Calendar.MONTH, month);
              m_cal.set(Calendar.DATE, date);
              setText(String.valueOf(date));
              setToolTipText(ms_months[m_cal.get(Calendar.MONTH)] + ", "
                      + String.valueOf(m_cal.get(Calendar.DAY_OF_MONTH)));

              setAccesibleInfo();
           }

           /**
            * Set the calendar to the date passed in
            * 
            * @param date. May not be <code>null</code>.
            */
           public void setCalendar(Date date)
           {
              m_cal.setTime(date);
              setText(String.valueOf(m_cal.get(Calendar.DAY_OF_MONTH)));
              setToolTipText(ms_months[m_cal.get(Calendar.MONTH)] + ", "
                      + String.valueOf(m_cal.get(Calendar.DAY_OF_MONTH)));

              setAccesibleInfo();
           }

           /**
            * Gets the accessible context of the element and if it is not null,
            * it sets the toolTip text as the accessible name. This way, screen
            * readers can see the difference between two buttons that have the
            * same number, but belong to different months.
            */
           private void setAccesibleInfo()
           {
              AccessibleContext aCtx = getAccessibleContext();
              if (aCtx != null)
              {
                 aCtx.setAccessibleName(getToolTipText());
              }
           }

           /**
            * Is this calendar's date today's date
            * 
            * @return <code>true</code> if this is today, else
            * <code>false</code>.
            */
           public boolean isToday()
           {
              Calendar c = new GregorianCalendar();
              return (m_cal.get(Calendar.YEAR) == c.get(Calendar.YEAR)
                      && m_cal.get(Calendar.MONTH) == c.get(Calendar.MONTH) && m_cal.get(Calendar.DATE) == c
                      .get(Calendar.DATE));
           }

           /**
            * Override the <code>paintComponent</code> method so we can draw
            * the selection indicator if needed.
            * 
            * @param g graphics object. Never <code>null</code>.
            */
           public void paintComponent(Graphics g)
           {
              super.paintComponent(g);
              Dimension size = getSize();
              if(isCurrentSelection())
              {
                 g.setColor(Color.blue);
                 g.drawRect(2, 2, size.width-5, size.height-5);
              }
              if(isToday())
              {
                 g.setColor(Color.black);
                 g.drawLine(5, 16, size.width - 6, 16);
              }
           }

        }

       /**
        * List of label references to the calendar grid. Initialized in the ctor,
        * Never <code>null</code> after that.
        */
       private List m_grid;

       /**
        * List of label references to the calendar header. Initialized in the ctor,
        * Never <code>null</code> after that.
        */
       private List m_header;

       /**
        * Previous month button. Initalized in {@link init()}, Never
        * <code>null</code> after that.
        */
       private JButton m_prevButton;

       /**
        * Next month button. Initalized in {@link init()}, Never
        * <code>null</code> after that.
        */
       private JButton m_nextButton;

       /**
        * Popup menu for month selection. Initalized in {@link init()}, Never
        * <code>null</code> after that.
        */
       private JComboBox m_monthList;

       /**
        * Popup menu for year selection. Initalized in {@link init()}, Never
        * <code>null</code> after that.
        */
       private JComboBox m_yearList;

       /**
        * The calendar to represent the date for the current displayed
        * calendar. Never <code>null</code>. Defaults to the current date/time.
        */
       private GregorianCalendar m_currCalendar = new GregorianCalendar();

       /**
        * The calendar to represent the date passed in. Never <code>null</code>.
        * Defaults to the current date/time.
        */
       private GregorianCalendar m_calendar = new GregorianCalendar();

       /**
        * The header background color. Defaults to blue.
        */
       private Color m_headerBackground = new Color(0, 51, 153);

       /**
        * The header foreground color. Defaults to white.
        */
       private Color m_headerForeground = Color.white;

       /**
        * The grid background color. Defaults to white.
        */
       private Color m_gridBackground = Color.white;

       /**
        * The grid foreground color. Defaults to black.
        */
       private Color m_gridForeground = Color.black;

       /**
        * The grid current date foreground color. Defaults to
        * red
        */
       private Color m_gridCurrentForeground = Color.red;

       /**
        * The mouse over border color. Defaults to
        * black
        */
       private Color m_mouseOverBorderColor = Color.black;

       /**
        * The mouse over background color. Defaults to
        * white
        */
       private Color m_mouseOverBackground = Color.white;


    }

    /**
     * Calendar panel to display the calendar grid. Initialized in
     * the ctor. Never <code>null</code> after that.
     */
    private CalendarPanel m_calendarPanel;

    /**
     * Flag indicating that a date was selected
     */
    private boolean m_bDateSelected;

    /**
     * Year selection popup offset
     */
    private int m_yearPopupOffset = 3;

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

    /**
     * MenuAction data key
     */
     private static final String DATA = "DATA";

     /**
      * Month menu type
      */
     private static final int MONTH_TYPE = 1;

     /**
      * Year menu type
      */
     private static final int YEAR_TYPE = 2;

     /**
      * Seconds in one day
      */
     private static final long ONE_DAY = 86400000L;

     /**
      * The calendar header array
      */
     private static final String[] ms_headerText =
        {
           ms_res.getString("day.abbr.sun"),
           ms_res.getString("day.abbr.mon"),
           ms_res.getString("day.abbr.tues"),
           ms_res.getString("day.abbr.wed"),
           ms_res.getString("day.abbr.thur"),
           ms_res.getString("day.abbr.fri"),
           ms_res.getString("day.abbr.sat")
        };

     /**
      *  The month name array
      */
     private static final String[] ms_months =
        {
           ms_res.getString("month.jan"),
           ms_res.getString("month.feb"),
           ms_res.getString("month.mar"),
           ms_res.getString("month.apr"),
           ms_res.getString("month.may"),
           ms_res.getString("month.june"),
           ms_res.getString("month.jul"),
           ms_res.getString("month.aug"),
           ms_res.getString("month.sep"),
           ms_res.getString("month.oct"),
           ms_res.getString("month.nov"),
           ms_res.getString("month.dec")
        };

     /**
      * Calendar representing Today's date
      */
     private static Calendar ms_today = new GregorianCalendar();



}
