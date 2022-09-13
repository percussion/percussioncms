package com.percussion.soln.rx.assembly;

import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.thoughtworks.xstream.XStream;

import java.nio.charset.StandardCharsets;

public class XStreamAssemblyResult extends MutableAssemblyResult
        implements IPSAssemblyResult {
    
    private static XStream xstream;
    private Object data;
    
    public XStreamAssemblyResult(IPSAssemblyItem assemblyItem, Object data) {
        super(assemblyItem, null, "application/xml");
        if (data == null) throw new IllegalArgumentException("Data object to be serialized cannot be null");
        this.data = data;
    }
    
    private XStream getXStream() {
        if (xstream == null) {
            xstream = new XStream();            
        }

        return xstream;
    }
    

    public byte[] getResultData() {
        if (super.getResultData() == null) {
            super.setResultData(getXStream().toXML(getData()).getBytes(StandardCharsets.UTF_8));
        }
        return super.getResultData();
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

}
