/*[ QAObjectType.java ]********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.autotest.framework;

import java.io.Serializable;

public final class QAObjectType
    implements Serializable
{

    private QAObjectType()
    {
    }

    public static final QAObjectType MUTEX = new QAObjectType();
    public static final QAObjectType EVENT = new QAObjectType();

}
