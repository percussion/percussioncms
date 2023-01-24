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

package com.percussion.soln.p13n.tracking;

/**
 * 
 * Indicates an error in the tracking system
 * and the tracking system is aware of it.
 * This exception can chain other exceptions
 * so make sure to look at the root cause.
 * @author adamgent
 * 
 */
public class VisitorTrackingException extends Exception {

    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 2531970121715959707L;

    public VisitorTrackingException(String arg0) {
        super(arg0);
    }

    public VisitorTrackingException(Throwable arg0) {
        super(arg0);
    }

    public VisitorTrackingException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
