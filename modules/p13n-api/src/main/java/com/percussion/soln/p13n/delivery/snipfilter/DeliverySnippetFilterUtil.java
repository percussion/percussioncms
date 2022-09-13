package com.percussion.soln.p13n.delivery.snipfilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.percussion.soln.p13n.delivery.IDeliveryResponseSnippetItem;
import com.percussion.soln.segment.Segment;

/**
 * 
 * Helpful utilities methods for filtering snippets.
 * 
 * @author adamgent
 *
 */
public class DeliverySnippetFilterUtil {
    private static final DeliveryResponseSnippetItemSorter snippetSorter = new DeliveryResponseSnippetItemSorter();

    public static List<? extends Segment> matchingSegments(
            Collection<? extends Segment> segments, 
            List<IDeliveryResponseSnippetItem> snipItems) {
        if(segments == null || snipItems == null) 
            throw new IllegalArgumentException("Arguments cannot be null");
        ArrayList<Segment> matching = new ArrayList<Segment>();
        ArrayList<Segment> snipSegments = new ArrayList<Segment>();
        for(IDeliveryResponseSnippetItem snip : snipItems) { snipSegments.addAll(snip.getSegments()); }
        for(Segment seg: segments) { if (containsSegment(seg, segments)) matching.add(seg); }
        return matching;
    }
    
    public static List<IDeliveryResponseSnippetItem> matchingSnippets(
            Collection<? extends Segment> segments,
            List<IDeliveryResponseSnippetItem> snipItems) {
        List<IDeliveryResponseSnippetItem> matching = new ArrayList<IDeliveryResponseSnippetItem>();
        for(IDeliveryResponseSnippetItem snip : snipItems) { 
            if (containsAnySegment(segments, snip.getSegments())) {
                matching.add(snip);
            }
        }
        return matching;
    }
    
    public static List<IDeliveryResponseSnippetItem> sortSnippets(List<IDeliveryResponseSnippetItem> unSorted) {
        List<IDeliveryResponseSnippetItem> sort = new ArrayList<IDeliveryResponseSnippetItem>(unSorted);
        Collections.sort(sort, snippetSorter);
        return sort;
    }
    
    private static class DeliveryResponseSnippetItemSorter implements Comparator<IDeliveryResponseSnippetItem> {
        
        public int compare(IDeliveryResponseSnippetItem lh, IDeliveryResponseSnippetItem rh) {
            Integer lhI = getIndex(lh);
            Integer rhI = getIndex(rh);
            return lhI.compareTo(rhI);
        }
        
        private int getIndex(IDeliveryResponseSnippetItem item) {
            return item.getSortIndex() <= 0 ? Integer.MIN_VALUE : item.getSortIndex();
        }
    }
    
    public static boolean containsSnippet(IDeliveryResponseSnippetItem snippetToMatch, List<IDeliveryResponseSnippetItem> snippets) {
        if (snippetToMatch == null) return false;
        if (snippets == null) return false;
        for(IDeliveryResponseSnippetItem snippet : snippets) {
            if (snippet != null && 
                    (snippetToMatch == snippet || snippetToMatch.getId().equals(snippet.getId())))
                return true;
        }
        return false;
    }
    
    public static boolean containsAnySegment(
            Collection<? extends Segment> as,
            Collection<? extends Segment> bs ) {
        for(Segment a : as) { if(containsSegment(a, bs)) { return true; } }
        return false;
    }
    
    public static Collection<? extends Segment> intersectSegments(
            Collection<? extends Segment> as,
            Collection<? extends Segment> bs ) {
        ArrayList<Segment> segs = new ArrayList<Segment>();
        for(Segment a : as) { if(containsSegment(a, bs)) { segs.add(a); } }
        return segs;
    }
    
    public static boolean containsSegment(Segment segment, 
            Collection<? extends Segment> segments) {
        if (segment == null) return false;
        return containsSegment(segment.getId(), segments);
    }
    
    public static boolean containsSegment(String id, Collection<? extends Segment> segments) {
        for(Segment s : segments) { if (id.equals(s.getId())) return true; }
        return false;
    }
}
