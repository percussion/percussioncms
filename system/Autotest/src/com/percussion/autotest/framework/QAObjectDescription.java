/*[ QAObjectDescription.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

import java.io.Serializable;
import java.util.Hashtable;

public class QAObjectDescription
    implements Serializable
{

    public QAObjectDescription(String name, QAObjectType type, int scope)
        throws IllegalArgumentException
    {
        this(name, type, scope, null);
    }

    public QAObjectDescription(String name, QAObjectType type, int scope, Hashtable params)
        throws IllegalArgumentException
    {
        if(name == null)
            throw new IllegalArgumentException("null == name");
        if(name.length() == 0)
            throw new IllegalArgumentException("name.length() == 0");
        if(type == null)
            throw new IllegalArgumentException("null == type");
        if(scope < 0)
        {
            throw new IllegalArgumentException("invalid scope: " + scope);
        }
        else
        {
            m_name = name;
            m_type = type;
            m_scope = scope;
            m_params = params;
            return;
        }
    }

    public String getName()
    {
        return m_name;
    }

    public String getParam(String paramName)
    {
        if(m_params == null)
            return null;
        else
            return (String)m_params.get(paramName);
    }

    public int getScope()
    {
        return m_scope;
    }

    public QAObjectType getType()
    {
        return m_type;
    }

    private String m_name;
    private QAObjectType m_type;
    private int m_scope;
    private Hashtable m_params;
}
