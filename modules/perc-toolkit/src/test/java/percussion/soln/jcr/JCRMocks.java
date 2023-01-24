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

package percussion.soln.jcr;

import javax.jcr.Node;
import javax.jcr.Property;

import org.jmock.Expectations;
import org.jmock.Mockery;

public class JCRMocks {
    
    Mockery mockery;
    
    
    public JCRMocks(Mockery mockery) {
        super();
        this.mockery = mockery;
    }
    
    public Property expectProperty(
            String mockName, 
            final Node node, 
            final String name, 
            final String value)
        throws Exception {
        final Property property = mockery.mock(Property.class, mockName);
        mockery.checking(new Expectations() {{ 
            allowing(node).getProperty(name);
            will(returnValue(property));
            
            allowing(property).getString();
            will(returnValue(value));
        }});
        
        return property;
    }
    
    public Property expectProperty(
            String mockName,
            final Node node, 
            final String name,
            final long value) throws Exception {
        final Property property = mockery.mock(Property.class, mockName);
        mockery.checking(new Expectations() {
            {
                allowing(node).getProperty(name);
                will(returnValue(property));

                allowing(property).getLong();
                will(returnValue(value));
            }
        });

        return property;
    }

}
