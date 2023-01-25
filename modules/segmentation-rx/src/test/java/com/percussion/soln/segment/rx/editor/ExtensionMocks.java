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

package com.percussion.soln.segment.rx.editor;

import java.util.Map;
import java.util.Map.Entry;

import org.jmock.Expectations;
import org.jmock.Mockery;
import com.percussion.design.objectstore.IPSReplacementValue;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.server.IPSRequestContext;

public class ExtensionMocks {
    private Mockery context;
    
    public ExtensionMocks(Mockery context) {
        this.context = context;
    }
    
    public IPSExtensionDef makeExtensionDef(final String ... names) {
        final IPSExtensionDef extensionDef = context.mock(IPSExtensionDef.class);
        context.checking(new Expectations() {{ 
            one(extensionDef).getRuntimeParameterNames();
            will(returnIterator(names));
        }});
        return extensionDef;
    }
    
    public IPSRequestContext makeRequest(final Map<String,String> parameters) {
        final IPSRequestContext request = context.mock(IPSRequestContext.class);
        context.checking(new Expectations() {{
            for (Entry<String, String> entry : parameters.entrySet()) {
                atMost(1).of(request).getParameter(entry.getKey());
                will(returnValue(entry.getValue()));
            }
            
            allowing(request).getParametersIterator();
            will(returnValue(parameters.entrySet().iterator()));
        }});
        return request;
    }
    
    
    public Object[] makeExtensionParams(final String ... params) throws Exception{
        final Object [] rvalue = new Object[params.length];
        context.checking(new Expectations() {{
            for (int i = 0; i < params.length; i++) {
                IPSReplacementValue irv = context.mock(IPSReplacementValue.class);
                one(irv).getValueText();
                will(returnValue(params[i]));
                rvalue[i] = irv;
            }
        }});
        return rvalue;
    }
}
