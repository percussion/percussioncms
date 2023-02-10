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

package com.percussion.soln.rx.assembly;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.jexl.PSJexlEvaluator;
import com.percussion.utils.jexl.PSScript;
import org.apache.commons.jexl3.JxltEngine;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractAssemblyHelper {
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(AbstractAssemblyHelper.class);
    
    public PSJexlEvaluator doBindings(IPSAssemblyItem item) throws Exception {
        PSJexlEvaluator eval = getBindings(item);
        PSJexlEvaluator rval = doBindings(eval, item);
        if (rval == null) throw new IllegalStateException("doBindings should not return null");
        return rval;
    }
    
    protected PSJexlEvaluator doBindings(PSJexlEvaluator eval, IPSAssemblyItem item) throws Exception {
        return eval;
    }
    
    public IPSAssemblyResult doResults(PSJexlEvaluator eval, IPSAssemblyResult result) throws Exception {
        if (isPublishResults(result)) {
            return doPublishResults(eval, result);
        }
        return result;
    }
    
    protected IPSAssemblyResult doPublishResults(PSJexlEvaluator eval, IPSAssemblyResult result) throws Exception {
        return result;
    }
    
    
    public static PSJexlEvaluator getBindings(IPSAssemblyItem item) {
        return new PSJexlEvaluator(item.getBindings());
    }
    

    public static <T> T bindExpression(PSJexlEvaluator eval, JxltEngine.Expression exp, T value)
        throws Exception {

        PSScript script = new PSScript(exp.asString());
        Object original = eval.evaluate(script);
        T rvalue;
        if (original == null) {
            if (log.isTraceEnabled())
                log.trace("Binding expression: {} to {}",exp.asString() , value);
            eval.bind(exp.asString(), value);
            rvalue = value;
        }
        else {
            log.debug("{} is already set to: {}" ,
                    exp,
                    original);
            if ( value != null && ! original.getClass().isInstance(value)) {
                throw new Exception(exp.asString() + " should be of type: " + value.getClass() +
                        "but is type: " + original.getClass());
            }
            rvalue = (T) original;
        }
        return rvalue;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T evalExpression(PSJexlEvaluator eval, JxltEngine.Expression exp, Class<T> k) throws Exception {
        PSScript script = new PSScript(exp.asString());

        return (T) eval.evaluate(script);
    }
    
    public static boolean isPublishResults(IPSAssemblyItem result) {
        String context = result.getParameterValue(IPSHtmlParameters.SYS_CONTEXT, null);
        return ( ! result.isDebug() 
                && context != null
                && ! "0".equals(context)
                && result.getCloneParentItem() == null);
    }

}
