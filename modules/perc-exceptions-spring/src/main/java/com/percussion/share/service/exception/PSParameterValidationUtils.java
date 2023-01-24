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

import com.percussion.share.validation.PSValidationErrors;
import com.percussion.share.validation.PSValidationErrorsBuilder;

public class PSParameterValidationUtils
{

    public static void rejectIfNull(String method, String field, Object value) throws PSValidationException {
        throwIfErrors(new PSValidationErrorsBuilder(method).rejectIfNull(field, value).build());
    }
    
    public static void rejectIfBlank(String method, String field, String value) throws PSValidationException {
        throwIfErrors(new PSValidationErrorsBuilder(method).rejectIfBlank(field, value).build());
    }
    
    public static PSValidationErrorsBuilder validateParameters(String method) {
        return new PSValidationErrorsBuilder(method);
    }
    public static void throwIfErrors(PSValidationErrors pve) throws PSValidationException {
        new PSParametersValidationException(pve).throwIfInvalid();
    }
}
