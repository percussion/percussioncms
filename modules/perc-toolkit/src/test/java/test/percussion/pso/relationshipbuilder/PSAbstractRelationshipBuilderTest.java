package test.percussion.pso.relationshipbuilder;

import static java.util.Arrays.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.percussion.error.PSException;
import com.percussion.pso.relationshipbuilder.PSAbstractRelationshipBuilder;
import com.percussion.services.assembly.PSAssemblyException;

public class PSAbstractRelationshipBuilderTest {
    
    PSAbstractRelationshipBuilder builder;

    @Test
    public void testSynchronize() throws Exception {
        testSynchronize(asList(1,2,3,4), asList(2,4,5));
    }
    
    public void testSynchronize(List<Integer> original, List<Integer> desired) throws Exception {
        Set<Integer> expected = new HashSet<Integer>(desired);
        Set<Integer> actual;
        builder = makeBuilder(original);
        builder.synchronize(1, expected);
        actual = new HashSet<Integer>(builder.retrieve(1));
        assertEquals(expected,actual);
    }
    
    public PSAbstractRelationshipBuilder makeBuilder(
            List<Integer> original) {
        
        final List<Integer> ids = new ArrayList<Integer>(original);
        
        PSAbstractRelationshipBuilder builder = new PSAbstractRelationshipBuilder() {
            
            @Override
            public void add(int sourceId, Collection<Integer> targetIds)
                    throws PSAssemblyException, PSException {
                ids.addAll(targetIds);
            }

            @Override
            public void delete(int sourceId, Collection<Integer> targetIds)
                    throws PSAssemblyException, PSException {
                ids.removeAll(targetIds);
                
            }

            public Collection<Integer> retrieve(int sourceId)
                    throws PSAssemblyException, PSException {
                return ids;
            }
        };
        
        return builder;
    }

}
