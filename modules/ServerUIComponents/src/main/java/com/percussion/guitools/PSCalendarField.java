/******************************************************************************
 *
 * [ PSCalendarField.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.guitools;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Calendar field appears as a text field with a button 
 * ({@link PSCalendarButton}) that when hit brings up a calendar dialog that the 
 * user can point and click to select a date. Can then return a formatted date 
 * string.
 * Formats are based on the date format rules for
 * <code>java.text.SimpleDateFormat</code>.
 */
public class PSCalendarField extends JPanel implements ActionListener
{
   /**
    * Constructs a new <code>PSCalendarField</code>
    * @param date the date to set the field to, based on the inputFormat. Can be
    * <code>null</code>.
    * @param inputFormat the date's initial input format string
    * (i.e MM/dd/yyyy). If <code>null</code> then defaults to MM/dd/yyyy.
    * @param outputFormat the date's format string (i.e MM/dd/yyyy). If
    * <code>null</code> then defaults to MM/dd/yyyy.
    */
   public PSCalendarField(String date, String inputFormat, String outputFormat)
   {
      setOutputFormat(outputFormat);
      init(transformDate(date, inputFormat));
   }

   /**
    * Constructs a new <code>PSCalendarField</code>
    * @param date the date to set the field to Can be <code>null</code>.
    * @param outputFormat the date's format string (i.e MM/dd/yyyy). If
    * <code>null</code> then defaults to MM/dd/yyyy.
    */
   public PSCalendarField(Date date, String outputFormat)
   {
      setOutputFormat(outputFormat);
      init(date);
   }

   /**
    * Constructs a new empty PSCalendarField
    */
   public PSCalendarField()
   {
       this(null, null);
   }

   /**
    * Initializes the gui
    */
   private void init(Date initialDate)
   {
      m_dateTextField = new JTextField(toString(initialDate), DEFAULT_TEXTFIELD_SIZE);
      m_dateTextField.getDocument().addDocumentListener( 
         new DocumentListener()
         {           

            public void insertUpdate(DocumentEvent e)
            {
               fireValueChangedEvent(); 
            }

            public void removeUpdate(DocumentEvent e)
            {
               fireValueChangedEvent();               
            }

            public void changedUpdate(DocumentEvent e)
            {
               fireValueChangedEvent();
            }            
         });
      m_dateTextField.setBackground(Color.white);

      m_calendarButton = createCalendarButton(getOwnerFrame(this), fieldToDate());
      m_calendarButton.addDateChangedListener(this);
      add(m_dateTextField);
      add(m_calendarButton);
   }

   /**
    * Creates calendar button.
    */
   protected PSCalendarButton createCalendarButton(final Frame frame, final Date date)
   {
      return new PSCalendarButton(frame, date);
   }

   /**
    * Set size of date text field. The default size is 12 characters.
    * @param size size of text field
    */
   public void setDateTextFieldSize(int size)
   {
      m_dateTextField.setColumns(size);
   }

   /**
    * Return the date for this CalendarField object.
    * @return the date, May be <code>null</code>.
    */
   public Date getDate()
   {
      return fieldToDate();
   }

   public String getDateString()
   {
      return toString();
   }

   public void setHeightFixed(boolean fixed)
   {
      m_fixedHeight = fixed;
   }

   /**
    * Overridden to return the preferred size for the control.
    */
   public Dimension getPreferredSize()
   {
      if(m_fixedHeight)
      {
         return new Dimension(super.getPreferredSize().width, FIXED_HEIGHT);
      }
      else
      {
         return super.getPreferredSize();
      }
   }

   /**
    * If setHeightFixed is <code>true</code>then override to return the
    * min size of the control, as defined by
    * IUTConstants.MIN_CONTROL_SIZE. When used with the Box layout mgr, this
    * provides the behavior of the control not shrinking beyond a default
    * width, while maintaining a nice height as the container is resized.
    */
   public Dimension getMinimumSize()
   {
      if(m_fixedHeight)
      {
         return new Dimension(40, FIXED_HEIGHT);
      }
      else
      {
         return super.getMinimumSize();
      }
   }

   /**
    * If setHeightFixed is <code>true</code>then override to
    * return the max size of the control, as defined by
    * IUTConstants.MAX_CONTROL_SIZE. When used with the Box layout mgr, this
    * provides the behavior of taking up all the width, but maintaining a nice
    * height as the container is resized.
    */
   public Dimension getMaximumSize()
   {
      if(m_fixedHeight)
      {
         return new Dimension(10000, FIXED_HEIGHT);
      }
      else
      {
         return super.getMaximumSize();
      }
   }

   /**
    * Return the date for this CalendarField object as
    * a formatted string based on the outputFormat
    * @return formatted date string, Never <code>null</code>. May be empty.
    */
   public String toString()
   {
      return toString(fieldToDate());
   }
   
   /**
    * Return the date passed in as
    * a formatted string based on the outputFormat
    * @return formatted date string, Never <code>null</code>. May be empty.
    */
   public String toString(Date date)
   {
      if(null == date)
         return "";
      return new SimpleDateFormat(m_outputFormat).format(date);
   }

   /**
    * Sets the date for this CalendarField Object.
    * @param date the date to be set. Can be <code>null</code>.
    */
   public void setDate(Date date)
   {
     
      if (m_dateTextField != null)
      {
         final String newText = toString(date);
         // change content of the text field only if text is different
         // from existing text to break infinite loop when
         // change to text field calls listener which updates date value
         // in turn causing the field update
         if (!newText.equals(m_dateTextField.getText()))
         {
            m_dateTextField.setText(newText);
         }
      }
   }

   /**
    * Sets the date for this CalendarField Object.
    * @param date the date string to be set. Can be <code>null</code>.
    * @param inputFormat the date's format string (i.e MM/dd/yyyy). If
    * <code>null</code> then defaults to MM/dd/yyyy.
    */
   public void setDate(String date, String inputFormat)
   {      
      setDate(transformDate(date, inputFormat));
   }
   
   /**
    * Transforms the string date using the specified input
    * format.
    * @param date the date string to be set. Can be <code>null</code>.
    * @param inputFormat the date's format string (i.e MM/dd/yyyy). If
    * <code>null</code> then defaults to MM/dd/yyyy.
    * @return transformed date
    */
   private Date transformDate(String date, String inputFormat)
   {
      if(null == date)
         date = "";
      if(null == inputFormat || inputFormat.length() == 0)
         inputFormat = DEFAULT_DATE_FORMAT;
      SimpleDateFormat sdf = new SimpleDateFormat(inputFormat);
     return sdf.parse(date, new ParsePosition(0));
   }

   /**
    * If set to <code>true</code> the user can enter the
    * date via the textfield. If set to <code>false</code>
    * then the user can only enter the date via the popup
    * calendar dialog.
    * @param canEdit boolean indicating that the date field can
    * be edited.
    */
   public void setEditable(boolean canEdit)
   {
      m_dateTextField.setEditable(canEdit);
   }

  /**
   * Sets the output format string
   * @param format the date format string (i.e MM/dd/yyyy).
   * Can be <code>null</code>.
   */
   public void setOutputFormat(String format)
   {
      if(null == format || format.length() == 0)
         format = DEFAULT_DATE_FORMAT;
      m_outputFormat = format;
   }

   /**
    * Attempts to parse the string date in <code>m_dateTextField</code>
    * for a valid date and if so sets the date to that date. Sets the date to
    * <code>null</code> if the text field is empty or we can't parse
    * a valid date.
    */
   private Date fieldToDate()
   {
      return convertStringToDate(m_dateTextField.getText().trim());
   }

   /**
    * Converts the supplied string date to <code>Date</code>. The date format 
    * of the string is expected to be one of the values in 
    * {@link #ms_datePatternArray}.
    * 
    * @param textDate The to be converted string. It may be <code>null</code>
    *    or empty.
    * 
    * @return The converted date. <code>null</code> if 
    *    <code>textDate</code> is <code>null</code> or empty. It may also be 
    *    <code>null</code> if failed to parse all the patterns in 
    *    {@link #ms_datePatternArray}.
    */
   public static Date convertStringToDate(String textDate)
   {
      Date date = null;
      
      if (textDate != null && textDate.trim().length() > 0)
      {
         for (int i=0; i < ms_datePatternArray.length; i++)
         {
            SimpleDateFormat sdf = new SimpleDateFormat(ms_datePatternArray[i]);
            date = sdf.parse(textDate,  new ParsePosition(0));
            if(date != null)
            {
               break;
            }
         }
      }
      
      return date;
   }

   /**
    * Handles setting the intial date when the calendar button is hit, and
    * retrieving the selected date once the pop-up is closed.
    * 
    * @param event the <code>ActionEvent</code> caught. Never <code>null</code>.
    */
   public void actionPerformed(ActionEvent event)
   {
      Object source = event.getSource();
      if(source == m_calendarButton)
      {
         if (event.getActionCommand().equals(
            PSCalendarButton.BUTTON_PRESSED_CMD))
         {            
            m_calendarButton.setDate(fieldToDate());
         }
         else if (event.getActionCommand().equals( 
            PSCalendarButton.DATE_UPDATED_CMD))
         {
            setDate(m_calendarButton.getDate());
         }
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
    * Adds a value changed listener to this dialog
    * @param listener cannot be <code>null</code>.
    */
   public void addValueChangedListener(IPSValueChangedListener listener)   
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(!m_valueChangedListeners.contains(listener))
      {
         m_valueChangedListeners.add(listener);
      }
   }
   
   /**
    * Removes the specified value changed listener to this dialog
    * @param listener cannot be <code>null</code>.
    */
   public void removeValueChangedListener(IPSValueChangedListener listener)   
   {
      if(listener == null)
         throw new IllegalArgumentException("listener cannot be null.");
      if(m_valueChangedListeners.contains(listener))
      {
         m_valueChangedListeners.remove(listener);
      }
   }
   
   /**
    * Handles notification for all registered listeners of a value
    * changed event.
    */
   private void fireValueChangedEvent()
   {
      PSValueChangedEvent event = 
         new PSValueChangedEvent(this, Event.ACTION_EVENT);
      Iterator it = m_valueChangedListeners.iterator();
      while(it.hasNext())
      {
         IPSValueChangedListener listener = (IPSValueChangedListener)it.next();
         listener.valueChanged(event);
      }
   }


   // Main test class
   public static void main(String[] args)
   {
      JFrame frame = new JFrame("Test");
      PSCalendarField control =
         new PSCalendarField(null,null,"yyyy-MM-dd");
      frame.getContentPane().add(control);
      frame.pack();
      frame.setVisible(true);

   }

   /**
    * The output format string. Set in ctor, never <code>null</code> after
    * that.
    */
   private String m_outputFormat;

   /**
    * The button to invoke the calendar dialog. Initialized in {@link #init()}
    * Never <code>null</code> after that.
    */
   private PSCalendarButton m_calendarButton;

   /**
    * The date text field. Initialized in {@link #init()}
    * Never <code>null</code> after that.
    */
   private JTextField m_dateTextField;

   /**
    * Flag indicating that the height should remain fixed.
    */
   private boolean m_fixedHeight;
   
   /**
    * List of all registered value changed listeners
    */
   private final List<IPSValueChangedListener> m_valueChangedListeners =
         new ArrayList<IPSValueChangedListener>();

   //private static List ms_formats = new ArrayList();

   /**
    * The supported date pattern, mostly from 
    * {@link com.percussion.util.PSDataTypeConverter}.
    */
   private static String[] ms_datePatternArray = {
      // Accurate ones should be listed first
      "yyyy-MMMM-dd 'at' hh:mm:ss aaa",
      "yyyy-MMMM-dd HH:mm:ss",
      "yyyy.MMMM.dd 'at' hh:mm:ss aaa",
      "yyyy.MMMM.dd HH:mm:ss",
      "yyyyMMdd HH:mm:ss",
      "yyyy.MMMM.dd 'at' hh:mm aaa",
      "yyyy-MM-dd G 'at' HH:mm:ss",
      "yyyy-MM-dd HH:mm:ss.SSS",
      "yyyy-MM-dd HH:mm:ss",
      "yyyy.MM.dd G 'at' HH:mm:ss",
      "yyyy.MM.dd HH:mm:ss.SSS",
      "yyyy.MM.dd HH:mm:ss",
      "yyyy/MM/dd G 'at' HH:mm:ss",
      "yyyy/MM/dd HH:mm:ss.SSS",
      "yyyy/MM/dd HH:mm:ss",
      "yyyy/MM/dd HH:mm",
      "yyyy-MM-dd",           // the default date pattern
      "yyyy.MM.dd",
      "yyyy/MM/dd",
      "yyyy-MMMM-dd",
      "yyyy.MMMM.dd",
      "EEE, d MMM yyyy HH:mm:ss",
      "EEEE, MMM d, yyyy",
      "MMM d, yyyy",
      "MMM yyyy",
      "yyyy",
      "HH:mm:ss",
      "HH:mm" };

   /**
    * The default date format string
    */
   private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

   /**
    * The default date text field column size
    */
   private static final int DEFAULT_TEXTFIELD_SIZE = 12;

   /**
    * The fixed height for the this component
    */
   private static final int FIXED_HEIGHT = 28;

}
