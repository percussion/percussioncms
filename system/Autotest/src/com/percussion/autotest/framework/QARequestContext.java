/*[ QARequestContext.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

import java.io.Serializable;

public class QARequestContext
    implements Serializable
{

    public QARequestContext(String clientURL, String scriptName)
        throws IllegalArgumentException
    {
        if(clientURL == null)
            throw new IllegalArgumentException("null == clientURL");
        if(scriptName == null)
        {
            throw new IllegalArgumentException("null == scriptName");
        }
        else
        {
            m_clientURL = clientURL;
            m_scriptName = scriptName;
            return;
        }
    }

    public String getClientURL()
    {
        return m_clientURL;
    }

    public String getScriptName()
    {
        return m_scriptName;
    }

    private String m_clientURL;
    private String m_scriptName;
}
