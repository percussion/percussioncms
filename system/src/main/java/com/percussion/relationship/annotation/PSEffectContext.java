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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.relationship.annotation;

import com.percussion.relationship.IPSExecutionContext;

/**
 * This class provides enum values to be used in PSHandlesEffectContext
 * annotations added IPSEffect classes. IPSExecutionContext provides constant
 * values that are not used and this enum restricts the value and provides
 * compile time checking.
 */
public enum PSEffectContext {

    PRE_CONSTRUCTION(IPSExecutionContext.RS_PRE_CONSTRUCTION), 
    PRE_DESTRUCTION(IPSExecutionContext.RS_PRE_DESTRUCTION), 
    PRE_WORKFLOW(IPSExecutionContext.RS_PRE_WORKFLOW), 
    POST_WORKFLOW(IPSExecutionContext.RS_POST_WORKFLOW), 
    PRE_CHECKIN(IPSExecutionContext.RS_PRE_CHECKIN), 
    POST_CHECKOUT(IPSExecutionContext.RS_POST_CHECKOUT), 
    PRE_UPDATE(IPSExecutionContext.RS_PRE_UPDATE), 
    PRE_CLONE(IPSExecutionContext.RS_PRE_CLONE), 
    ALL(-1);

    /**
     * This code maps to the original value of IPSExcutionContext that is used
     * in the relationship configuration
     */
    private final int code;

    /**
     * @param code The integer representing this enum value
     */
    private PSEffectContext(int code)
    {
        this.code = code;
    }

    /**
     * Get the integer code that maps to the values in IPSExecutionContext
     * 
     * @return the code
     */
    public int getCode()
    {
        return code;
    }

}
