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

import com.percussion.error.PSExceptionUtils;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.impl.plugin.PSDispatchAssembler;
import com.percussion.utils.jexl.PSJexlEvaluator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractExtendedDispatchAssembler extends PSDispatchAssembler {
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory
            .getLog(AbstractExtendedDispatchAssembler.class);

    @Override
    public IPSAssemblyResult assembleSingle(IPSAssemblyItem item) {
        AbstractAssemblyHelper helper = getAssemblyHelper();
        PSJexlEvaluator eval;
        try {
            eval = helper.doBindings(item);
        } catch (Exception e) {
            log.error(PSExceptionUtils.getMessageForLog(e));
            throw new RuntimeException(e);
        }
        IPSAssemblyResult result = super.assembleSingle(item);

        try {
            return helper.doResults(eval, result);
        } catch (Exception e) {
            log.error(e);
            throw new RuntimeException(e);
        }        

    }
    

    protected abstract AbstractAssemblyHelper getAssemblyHelper();

}
