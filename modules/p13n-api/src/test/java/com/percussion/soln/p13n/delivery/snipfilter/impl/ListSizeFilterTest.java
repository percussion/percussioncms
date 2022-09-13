package test.percussion.soln.p13n.delivery.snipfilter.impl;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter;
import com.percussion.soln.p13n.delivery.IDeliverySnippetFilter.DeliverySnippetFilterException;
import com.percussion.soln.p13n.delivery.snipfilter.impl.ListSizeFilter;


public class ListSizeFilterTest extends FilterTestCase {
    private ListSizeFilter filter;

    @Override
    protected IDeliverySnippetFilter createFilter() {
        filter = new ListSizeFilter();
        return filter;
    }
    
    
    private void setRange(int min, int max) {
        listItem.getProperties().put(filter.getMinCountPropertyName(), ""+min);
        listItem.getProperties().put(filter.getMaxCountPropertyName(), ""+max);
    }
    
    private void testRange(int min, int max, int expectedSize) {
        setRange(min,max);
        List<IDeliveryResponseSnippetItem> actual = filter.filter(snippetFilterContext, snippets);
        assertEquals(expectedSize, actual.size());
    }
    
    private void testRange(int min, int max, 
            List<IDeliveryResponseSnippetItem> filterSnippets,
            List<IDeliveryResponseSnippetItem> expectedSnippets
            ) {
        setRange(min,max);
        List<IDeliveryResponseSnippetItem> actual = filter.filter(snippetFilterContext, filterSnippets);
        assertEquals(expectedSnippets, actual);
    }
    
    @Test
    public void testMissingProperties() {
        //No set range.
        List<IDeliveryResponseSnippetItem> actual = filter.filter(snippetFilterContext, snippets);
        assertEquals(snippets.size(), actual.size());
    }
    
    
    @Test
    public void testEmptyProperties() {
        //No set range.
        listItem.getProperties().put(filter.getMinCountPropertyName(), "");
        listItem.getProperties().put(filter.getMaxCountPropertyName(), "");
        List<IDeliveryResponseSnippetItem> actual = filter.filter(snippetFilterContext, snippets);
        assertEquals(snippets.size(), actual.size());
    }
    
    @Test(expected=DeliverySnippetFilterException.class)
    public void testBadProperties() {
        //No set range.
        listItem.getProperties().put(filter.getMinCountPropertyName(), "BLAH");
        listItem.getProperties().put(filter.getMaxCountPropertyName(), "NOT A NUMBER");
        List<IDeliveryResponseSnippetItem> actual = filter.filter(snippetFilterContext, snippets);
        assertEquals(snippets.size(), actual.size());
    }
    
    
    
    @Test
    public void testRangeFor1To2() throws Exception {
        testRange(1,2,2);
    }
    
    @Test
    public void testRangeFor2To2() throws Exception {
        testRange(2,2,2);
    }
    
    @Test
    public void testRangeForNegative1To2() throws Exception {
        testRange(-1,2,2);
    }
    
    @Test
    public void testRangeForNegative1To4() throws Exception {
        testRange(-1,4,3);
    }
    
    
    @Test
    public void testBelowMinRange1ToNegative1() throws Exception {
        testRange(1, -1, 
                snippets(), 
                snippets(snipA));
    }
    
    @Test
    public void testBelowMinRange2To2() throws Exception {
        testRange(2, 2, 
                snippets(snipC), 
                snippets(snipC,snipA));
    }
    
    @Test
    public void testBelowMinRange3To2() throws Exception {
        testRange(3, 2, 
                snippets(snipB), 
                snippets(snipB,snipA,snipC));
    }
    
    
    @Test
    public void testBelowMinRange3UseSortOrder() throws Exception {
        snipA.setSortIndex(2);
        snipC.setSortIndex(1);
        //Snip C should be fore snip A because of its sort order.
        testRange(3, 100, 
                snippets(snipB), 
                snippets(snipB,snipC,snipA));
    }
    
    @Test
    public void testAboveMaxRange1To1() throws Exception {
        testRange(1, 1, 
                snippets(snipB,snipC), 
                snippets(snipB));
    }
    
    @Test
    public void testNoRange() throws Exception {
        testRange(-1, -100, 
                snippets(snipB,snipC,snipA), 
                snippets(snipB,snipC,snipA));
    }
    
    
    
    
    

}
