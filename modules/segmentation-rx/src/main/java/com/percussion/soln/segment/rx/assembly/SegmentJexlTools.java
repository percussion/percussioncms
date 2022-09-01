package com.percussion.soln.segment.rx.assembly;

import static java.util.Collections.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionException;
import com.percussion.services.PSMissingBeanConfigurationException;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.SegmentException;
import com.percussion.soln.segment.SegmentServiceHelper;
import com.percussion.soln.segment.rx.SegmentServiceLocator;


/**
 * Jexl gateway to segmentation.
 *
 * @author Stephen Bolton
 * @author Adam Gent
 */ 
public class SegmentJexlTools extends SegmentServiceHelper implements IPSJexlExpression 
{
    

    public SegmentJexlTools() {
        super();
    }


    public SegmentJexlTools(ISegmentService segmentService) {
        super(segmentService);
    }


    @IPSJexlMethod(description = "Get the segment aliases in alpha order associated to a content item in alpha order.", 
            params = { @IPSJexlParam(name = "itemId", description = "the content id of the item") },
            returns = "A List of segment names")
    @Override
    public List<String> getSegmentAliases(Number id) {
        return super.getSegmentAliases(id);
    }


    @Override
    @IPSJexlMethod(description = "Get the segment names and their ancestors for a given content item in alpha order.", 
            params = { @IPSJexlParam(name = "itemId", description = "the content id of the item") },
            returns = "A List of segment names")
    public List<String> getSegmentAncestorsNames(Number id) {
        return super.getSegmentAncestorsNames(id);
    }

    
    @IPSJexlMethod(description = "Get the segments by name in alpha order associated to a content item.", 
             params = { @IPSJexlParam(name = "itemId", description = "the content id of the item") },
             returns = "A List of segment names")
    @Override
    public List<String> getSegmentNames(Number id) {
        return super.getSegmentNames(id);
    }


    @IPSJexlMethod(description = "get the segments for a particular content item as " +
            "a string to be used for tracking", 
            params = { @IPSJexlParam(name = "item", description = "$sys.item") })
    public String getSegmentString(Node item) throws RepositoryException {
        return getSegmentString(item.getProperty("rx:sys_contentid").getLong());
     }

    
    public List<? extends Segment> fromAssemblyItems(List<IPSAssemblyItem> assemblyItems) throws SegmentException, ValueFormatException, PathNotFoundException, RepositoryException {
        List<Node> nodes = new ArrayList<Node>();
        for(IPSAssemblyItem a : assemblyItems) { nodes.add(a.getNode()); }
        return fromContentNodes(nodes);
    }
    
    public List<? extends Segment> fromContentNodes(List<Node> contentNodes) throws SegmentException, ValueFormatException, PathNotFoundException, RepositoryException {
        return getSegmentService().retrieveSegments(idsFromNodes(contentNodes)).getList();
    }
    
    private String idFromContentNode(Node node) throws ValueFormatException, PathNotFoundException, RepositoryException {
        return node.getProperty("rx:sys_contentid").getString();
    }
    
    private List<String> idsFromNodes(List<Node> contentNodes) throws ValueFormatException, PathNotFoundException, RepositoryException {
        List<String> ids = new ArrayList<String>();
        for(Node n : contentNodes) {
            ids.add(idFromContentNode(n));
        }
        
        return ids;
    }
    


    
    @IPSJexlMethod(description = "get a map of segments weights for a particular content id", 
            params = { @IPSJexlParam(name = "itemId", description = "the content id of the item") })
    public Map<String, Integer> getSegmentWeightMap(Number item) {
        if (item == null) throw new IllegalArgumentException("Item id cannot be null");
        Map<String, Integer> segmentIds = new HashMap<String, Integer>();
        for (Segment seg : getSegments(item)) {
            segmentIds.put(seg.getId(), 1);
        }
        return segmentIds;

    }
    

    @IPSJexlMethod(description = "get the segments for a particular content item as " +
            "a string to be used for tracking", 
            params = { @IPSJexlParam(name = "itemId", description = "the content id of the item") })         
    public String getSegmentString(Number item)  {
        String segmentString = "";
        Map<String,Integer> m = getSegmentWeightMap(item);
        Set<String> tmp = m.keySet();
        ArrayList<String> ids = new ArrayList<String>(tmp);
        sort(ids);
        Iterator<String> it = ids.iterator();
        while (it.hasNext()) {
            String id = it.next();
            segmentString += id;
            Integer v = m.get(id);
            if (v > 1)
                segmentString += ":" + v;
            if (it.hasNext())
                segmentString += ",";
        }
        return segmentString;
    }
    

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(SegmentJexlTools.class);
    

	public void init(IPSExtensionDef extDef, File file)
			throws PSExtensionException {
        if (getSegmentService() == null) {
            log.info("Using BaseServiceLocator to wire exit: " + getClass().getName());
            try {
                setSegmentService(SegmentServiceLocator.getSegmentService());
            } catch (PSMissingBeanConfigurationException e) {
                log.warn("Could not find segmentation service. This may happen on install in which case ignore this warning.");
            }
        }
	}



}

