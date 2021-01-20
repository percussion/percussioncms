/*[ QAClientPoolEvent.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

import java.io.Serializable;
import java.util.Date;

/**
 * This class specifies client pool events.
 */
public class QAClientPoolEvent implements Serializable
{
   /**
    * Constructs a new client pool event.
    *
    * @param source the source the server causing the event, not
    *    <code>null</code>.
    * @param msg an event message describing the event, may be 
    *    <code>null</code>.
    * @throws IllegalArgumentException if the provided source is 
    *    <code>null</code>.
    */
   public QAClientPoolEvent(QAServer source, String msg)
   {
      if (source == null)
         throw new IllegalArgumentException("the event source cannot be null");
      
     m_source = source;
     
     if (msg == null)
         m_msg = "";
     else
         m_msg = msg;
     
     m_time = new Date();
   }

   /**
    * Get the event message.
    *
    * @return the event message, never <code>null</code>, might be empty.
    */
   public String getMessage()
   {
     return m_msg;
   }

   /**
    * Get the event source.
    *
    * @return the event source server, never <code>null</code>.
    */
   public QAServer getSource()
   {
     return m_source;
   }

   /**
    * Get the event time.
    *
    * @return the date/time when the event occurred, never <code>null</code>.
    */
   public Date getTime()
   {
     return m_time;
   }

   /**
    * The source autotest server that caused this event, initialized during
    * construction, never <code>null</code> after that.
    */
   protected QAServer m_source = null;

   /**
    * An event message, initialized during construction, never 
    * <code>null</code> after that.
    */
   protected String m_msg = null;

   /**
    * The time when the event occurred, initialized during construction, never
    * <code>null</code> after that.
    */
   protected Date m_time = null;
}
