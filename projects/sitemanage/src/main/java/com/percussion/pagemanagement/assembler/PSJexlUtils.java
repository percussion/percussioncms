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
package com.percussion.pagemanagement.assembler;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PSJexlUtils
{

    public static PSJexlEvaluator getBindings(IPSAssemblyItem item) {
        return new PSJexlEvaluator(item.getBindings());
    }
    
    
    @SuppressWarnings("unchecked")
    public static <T> T bindExpression(PSJexlEvaluator eval, IPSScript exp, T value) 
        throws Exception {
        Object original = eval.evaluate(exp);
        T rvalue;
        if (original == null) {
            if (log.isTraceEnabled()) {
                log.trace("Binding expression: " + exp.getSourceText() + " to " + value);
            }
            eval.bind(exp.getSourceText(), value);
            rvalue = value;
        }
        else {
            log.debug(exp.getSourceText() + " is already set to: " + original);
            if ( value != null && ! original.getClass().isInstance(value)) {
                throw new RuntimeException(exp.getSourceText() + " should be of type: " + value.getClass() +
                        "but is type: " + original.getClass());
            }
            rvalue = (T) original;
        }
        return rvalue;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T evalExpression(PSJexlEvaluator eval, IPSScript exp, @SuppressWarnings("unused") Class<T> k) throws Exception {
        return (T) eval.evaluate(exp);
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */

    private static final Logger log = LogManager.getLogger(PSJexlUtils.class);
}
