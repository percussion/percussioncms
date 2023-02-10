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

package com.percussion.share.service.exception;

/**
 * This is an un-checked exception, for extracting HTML erros.
 * 
 * @author YuBingChen
 */
public class PSExtractHTMLException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an create asset exception with the specified cause.
     * 
     * @param cause the actual exception that caused the failure, not <code>null</code>.
     */
    public PSExtractHTMLException(Throwable cause)
    {
        super(cause);
    }
    
    /**
     * Constructs an create asset exception with a specified message.
     *  
     * @param message the failure message, may be <code>null</code> or empty.
     */
    public PSExtractHTMLException(String message)
    {
        super(message);
    }

    /**
     * Constructs an create asset exception with a specified message and specified cause.
     *  
     * @param message the failure message, may be <code>null</code> or empty.
     * @param cause the actual exception that caused the failure, not <code>null</code>.
     */
    public PSExtractHTMLException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
