/*[ QAObjectRegistry.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class QAObjectRegistry
{

    public QAObjectRegistry()
    {
        m_objs = new ConcurrentHashMap();
    }

    public QAServerObject getObject(Object key)
    {
        return (QAServerObject)m_objs.get(key);
    }

    public void putObject(Object key, QAServerObject value)
    {
        m_objs.put(key, value);
    }

    private Map m_objs;
}
