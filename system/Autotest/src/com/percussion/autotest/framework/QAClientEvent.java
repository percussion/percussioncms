/*[ QAClientEvent.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

import java.io.Serializable;
import java.util.Date;

public class QAClientEvent
    implements Serializable
{

    public QAClientEvent(String sourceClientName, String msg)
    {
        m_source = sourceClientName;
        if(msg == null)
            m_msg = "";
        else
            m_msg = msg;
        m_time = new Date();
    }

    public String getMessage()
    {
        return m_msg;
    }

    public String getSource()
    {
        return m_source;
    }

    public Date getTime()
    {
        return m_time;
    }

    protected String m_msg;
    protected Date m_time;
    protected String m_source;
}
