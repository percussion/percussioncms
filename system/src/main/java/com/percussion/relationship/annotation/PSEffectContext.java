/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
