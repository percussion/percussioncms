package com.percussion.soln.rx.assembly;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.impl.plugin.PSVelocityAssembler;
import com.percussion.utils.jexl.PSJexlEvaluator;

public abstract class AbstractExtendedVelocityAssembler extends PSVelocityAssembler {
    
    @Override
    protected IPSAssemblyResult doAssembleSingle(IPSAssemblyItem item) throws Exception {
        AbstractAssemblyHelper helper = getAssemblyHelper();
        PSJexlEvaluator eval = helper.doBindings(item);
        IPSAssemblyResult result = super.doAssembleSingle(item);
        
        /*
         * Create a rule item assembly result.
         */
        return helper.doResults(eval, result);
    }

    protected abstract AbstractAssemblyHelper getAssemblyHelper();
    
}
