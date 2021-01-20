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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.delivery.metadata.impl.utils;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * This class holds a generic pair of objects
 * 
 * @author dougrand
 */
public class PSPair<A, B>
{
    private A m_first;

    private B m_second;

    /**
     * Default ctor
     */
    public PSPair()
    {
        //
    }

    /**
     * Ctor to create an instance
     * 
     * @param first the first element, may be <code>null</code>
     * @param second the second element, may be <code>null</code>
     */
    public PSPair(A first, B second)
    {
        m_first = first;
        m_second = second;
    }

    /**
     * @return Returns the first.
     */
    public A getFirst()
    {
        return m_first;
    }

    /**
     * @return Returns the second.
     */
    public B getSecond()
    {
        return m_second;
    }

    /**
     * @param first The first to set.
     */
    public void setFirst(A first)
    {
        m_first = first;
    }

    /**
     * @param second The second to set.
     */
    public void setSecond(B second)
    {
        m_second = second;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof PSPair))
            return false;
        PSPair<A, B> b = (PSPair<A, B>) obj;
        return new EqualsBuilder().append(m_first, b.m_first).append(m_second, b.m_second).isEquals();

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        HashCodeBuilder hc = new HashCodeBuilder();
        return hc.append(m_first).append(m_second).toHashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("first", m_first)
                .append("second", m_second).toString();
    }

}
