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
