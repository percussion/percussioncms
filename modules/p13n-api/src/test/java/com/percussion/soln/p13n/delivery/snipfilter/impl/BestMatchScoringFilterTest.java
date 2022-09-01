package test.percussion.soln.p13n.delivery.snipfilter.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.snipfilter.impl.BestMatchScoringFilter;


public class BestMatchScoringFilterTest extends FilterTestCase {
    
    @Test
    public void testScoring() throws Exception {
        List<IDeliveryResponseSnippetItem> actual = filter.filter(snippetFilterContext, snippets);
        assertEquals(0, (int) actual.get(0).getScore());
        assertEquals(1, (int) actual.get(1).getScore());
        assertEquals(2, (int) actual.get(2).getScore());
    }

    @Override
    protected IDeliverySnippetFilter createFilter() {
        return new BestMatchScoringFilter();
    }

}
