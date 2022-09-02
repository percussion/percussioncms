package com.percussion.soln.segment.rx;

import static java.util.Arrays.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.jws.WebService;
import javax.ws.rs.Path;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.SegmentException;
import com.percussion.soln.segment.Segments;
import com.percussion.soln.segment.data.ISegmentDataService;
import com.percussion.utils.timing.PSStopwatch;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import org.springframework.beans.factory.annotation.Autowired;

@WebService(serviceName="SegmentService", endpointInterface = "com.percussion.soln.segment.ISegmentService")
@Path(value = "/segment")
public class RxSegmentService implements ISegmentService, ISegmentDataService {

    @Autowired
    private IPSContentMgr contentManager;
    @Autowired
    private IPSContentWs  contentWs;
    @Autowired
    private IPSGuidManager guidManager;
    
    private String jcrQuery;
    /**
     * The path of where the segment tree starts.
     */
    private String rootPath = "//";
    /**
     * The field name to use for the name of the segment.
     */
    private String labelField = "rx:sys_title";
    /**
     * The field name for a list of space seperated aliases.
     */
    private String aliasesField = null;
    
    private RxSegmentTree rxSegmentTree;
    private Object treeMutex = new Object();
    
    private List<String> fields = asList( 
            "rx:sys_title", 
            "rx:sys_contentid", 
            "rx:sys_folderid", 
            "jcr:path");
    
    private final List<String> requiredFields = asList("rx:sys_title", "rx:sys_folderid", "jcr:path");
    
    private String contentType = "perc_Segment";
    
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(RxSegmentService.class);
    
    /**
     * @see com.percussion.soln.segment.ISegmentService#retrieveSegmentsForItem(int)
     */
    public Segments retrieveSegmentsForItem(int legacyId) 
        throws SegmentException {
        log.debug("Start retrieving segments for item id = " + legacyId);
        PSStopwatch sw = new PSStopwatch();
        sw.start();
        RxSegmentTree t = getSegmentTree();
        Collection<Segment> rvalue;
        try {
            rvalue = t.retrieveSegmentsForItem(legacyId);
        } catch (PSErrorException e) {
            throw new SegmentException("Error getting folders for id = " + legacyId, e);
        }
        sw.stop();
        log.debug("Finish retrieving segments for item id = " + legacyId + " took " + sw);
        return createSegments(rvalue);
    }

    public Segments retrieveSegments(List<String> ids) throws SegmentException {
        if (ids == null) throw new IllegalArgumentException("Ids cannot be null");
        log.debug("Start retrieving segments for segment ids = " + ids);
        PSStopwatch sw = new PSStopwatch();
        sw.start();
        RxSegmentTree t = getSegmentTree();
        ArrayList<Segment> segments = new ArrayList<Segment>();
        for (String id : ids) {
            Segment s = t.getSegmentForId(id);
            if (s == null) throw new SegmentException("There is no segment for Id = " + id);
            segments.add(s);
        }
        sw.stop();
        log.debug("Finish retrieving segments for segment ids = " + ids + " took " + sw);
        return createSegments(segments);
    }
    
    public Segments retrieveAllSegments() throws SegmentException {
        log.debug("Start retrieving all segments");
        PSStopwatch sw = new PSStopwatch();
        sw.start();
        RxSegmentTree tree = getSegmentTree();
        Collection<? extends Segment> segments = tree.getSegments(); 
        log.debug("Finish retrieving all segments");
        return createSegments(segments);
    }
    
    //TODO Unit test retrieveSegmentsForFolderIds
    public Segments retrieveSegmentsForFolderIds(List<String> ids) throws SegmentException {
        if (ids == null) throw new IllegalArgumentException("Ids cannot be null");
        log.debug("Start retrieving segments for folder ids = " + ids);
        PSStopwatch sw = new PSStopwatch();
        sw.start();
        RxSegmentTree t = getSegmentTree();
        ArrayList<Segment> segments = new ArrayList<Segment>();
        for (String id : ids) {
            Segment s = t.getSegmentForFolderId(Integer.parseInt(id));
            if (s == null) throw new SegmentException("There is no segment for folder Id = " + id);
            segments.add(s);
        }
        sw.stop();
        log.debug("Finish retrieving segments for folder ids = " + ids + " took " + sw);
        return createSegments(segments);
    }
    
    //TODO: unit test this
    public RxSegmentTree getSegmentTree() {
        RxSegmentTree tree = rxSegmentTree;
        if (tree == null) {
            synchronized(treeMutex) {
                if (rxSegmentTree != null) {
                    tree = rxSegmentTree;
                }
                else {
                    tree = createSegmentTree();
                    this.rxSegmentTree = tree;
                    this.rxSegmentTree.setRootPath(getRootPath());
                }
            }
        }
        return tree;
    }
    
    public void resetSegmentTree(boolean clear, String rootPath) {
        log.debug("Marking segment tree to be rebuilt.");
        synchronized(treeMutex) {
            if (rootPath != null) setRootPath(rootPath);
            this.rxSegmentTree = null;
        }
    }
    
    protected RxSegmentTree createSegmentTree() {
        log.info("Building Segment Tree.");
        RxSegmentTree tree = createSegmentTree(getJcrQuery());   
        return tree;
    }
    
    protected RxSegmentTree createSegmentTree(String jcrQuery) {
        if (contentManager == null || contentWs == null || guidManager == null) {
            throw new IllegalStateException("Segment service is missing collaborators. " +
                    "Check wiring (spring).");
        }
        RxSegmentTree tree = new RxSegmentTree(contentManager,contentWs,guidManager);
        tree.setRootPath(getRootPath());
        tree.make(jcrQuery, labelField, aliasesField);
        return tree;
    }
    
    public void updateSegmentTree(Segments data) {
        throw new UnsupportedOperationException("updateTree is not supported for this implementation.");
    }

    public Segments retrieveSegmentChildren(String id) throws SegmentException {
        Segment parentSegment = getSegmentTree().getSegmentForId(id);
        return createSegments(getSegmentTree().getChildren(parentSegment));
    }
    
    public Segments retrieveSegmentAncestors(String id)
            throws SegmentException {
        Segment data = getSegmentTree().getSegmentForId(id);
        return createSegments(getSegmentTree().getAncestors(data));
    }

    public Segment retrieveRootSegment() throws SegmentException {
        return getSegmentTree().getRootSegment();
    }

    public Segments retrieveAllSegmentData() {
        return createSegments(getSegmentTree().getSegments());
    }

    public Segments retrieveSegmentsWithNameOrAlias(String alias) {
        RxSegmentTree tree = getSegmentTree();
        return createSegments(tree.retrieveSegmentsWithNameOrAlias(alias));
    }

    public Segment retrieveSegmentDataForId(String id) {
        return getSegmentTree().getSegmentForId(id);
    }
    
    public String getJcrQuery() {      
        
        if (jcrQuery == null && contentType == null) {
            throw new IllegalStateException("jcrQuery and content type cannot both be null.");
        }
        Set<String> queryFields = new TreeSet<String>();
        if (aliasesField != null) queryFields.add(aliasesField);
        if (labelField != null) queryFields.add(labelField);
        if (fields != null) queryFields.addAll(fields);
        queryFields.addAll(requiredFields);
        
        if (jcrQuery == null) {
            String q = "select " 
                + StringUtils.join(queryFields.iterator(), ", ")
                + " from " 
                + contentType
                + " where "
                + " jcr:path like "
                + "'" + rootPath + "%'";
            return q;
        }

        return jcrQuery;
    }

    protected Segments createSegments(Collection<? extends Segment> segs) {
        List<Segment> segList = new ArrayList<Segment>(segs);
        return new Segments(segList);
    }
    
    public IPSContentMgr getContentManager() {
        return contentManager;
    }

    public void setContentManager(IPSContentMgr contentManager) {
        this.contentManager = contentManager;
    }

    public void setJcrQuery(String jcrQuery) {
        this.jcrQuery = jcrQuery;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        log.debug("Setting root path to: " + rootPath);
        this.rootPath = rootPath;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public IPSContentWs getContentWs() {
        return contentWs;
    }

    public void setContentWs(IPSContentWs contentWs) {
        this.contentWs = contentWs;
    }

    public IPSGuidManager getGuidManager() {
        return guidManager;
    }

    public void setGuidManager(IPSGuidManager guidManager) {
        this.guidManager = guidManager;
    }

    public String getSegmentContentType() {
        return contentType;
    }

    public String getLabelField() {
        return labelField;
    }

    public void setLabelField(String labelField) {
        this.labelField = labelField;
    }

    public String getAliasesField() {
        return aliasesField;
    }

    public void setAliasesField(String aliasesField) {
        this.aliasesField = aliasesField;
    }


}
