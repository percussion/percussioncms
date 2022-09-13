package com.percussion.soln.rx.assembly;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.impl.plugin.PSAssemblerBase;

public abstract class ObjectAssembler<T> extends PSAssemblerBase {

    
    @Override
    public IPSAssemblyResult assembleSingle(IPSAssemblyItem assemblyItem) {
        T object = createObject(assemblyItem);
        return new XStreamAssemblyResult(assemblyItem, object);
    }

    
    public abstract T createObject(IPSAssemblyItem assemblyItem);
}
