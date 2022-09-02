package com.percussion.soln.segment.ds;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.SegmentException;
import com.percussion.soln.segment.Segments;
import com.percussion.soln.segment.data.ISegmentDataService;
import com.percussion.soln.segment.data.SegmentDataTree;


/**
 * Delivery Side Segmentation Service.
 * @author adamgent
 *
 */
public class DSSegmentService implements ISegmentService, ISegmentDataService {
    
    private SegmentDataTree segmentTree;
    private ISegmentDataService segmentDao;
    private String rootPath = "//";

    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(DSSegmentService.class);
    
    public DSSegmentService() {
        super();
        log.info("Started Delivery Side Segment Service");
    }

    public Segments retrieveAllSegments()
            throws SegmentException {
        SegmentDataTree tree = getSegmentTree();
        return createSegments(tree.getSegments());
    }

    public Segments retrieveSegments(List<String> ids)
            throws SegmentException {
        if (ids == null) { throw new IllegalArgumentException("Ids cannot be null"); }
        SegmentDataTree tree = getSegmentTree();
        List<Segment> nodes = new ArrayList<Segment>();
        for(String id : ids) {
            nodes.add(tree.getSegmentForId(id));
        }
        return createSegments(nodes);
    }

    @Override
    public Segments retrieveSegmentsForFolderIds(List<String> ids)
            throws SegmentException {
        throw new UnsupportedOperationException(
                "retrieveSegmentsForFolderIds is not yet supported");
    }

    public Segments retrieveSegmentsForItem(int legacyId)
            throws SegmentException {
        throw new UnsupportedOperationException(
                "retrieveSegmentsForItem is not yet supported");
    }

    public synchronized void resetSegmentTree(boolean clear, String rootPath) {
        log.info("Resetting the tree");
        if (rootPath != null) setRootPath(rootPath);
        if (clear) {
            log.info("Clearing the tree");
            segmentTree = null;
            segmentDao.resetSegmentTree(true, getRootPath());
        }
        else {
            log.info("Loading the tree from the repository");
            segmentTree = new SegmentDataTree();
            segmentTree.setRootPath(getRootPath());
            segmentTree.update(segmentDao.retrieveAllSegmentData().getList());
        }
    }

    public synchronized void updateSegmentTree(Segments data) {
        log.trace("Updating tree");
        segmentDao.updateSegmentTree(data);
        SegmentDataTree tree = getSegmentTree();
        tree.update(data.getList());
    }

    public synchronized SegmentDataTree getSegmentTree() {
        if (segmentTree == null) {
            resetSegmentTree(false, null);
        }
        return segmentTree;
    }

    public synchronized void setSegmentTree(SegmentDataTree segmentTree) {
        this.segmentTree = segmentTree;
    }

    public Segment retrieveRootSegment() throws SegmentException {
        return getSegmentTree().getRootSegment();
    }

    public Segments retrieveSegmentChildren(String id) throws SegmentException {
        Segment parentSegment = getSegmentTree().getSegmentForId(id);
        if (parentSegment == null) throw new SegmentException("No segment for id: " + id);
        return createSegments(getSegmentTree().getChildren(parentSegment));
    }
    
    public Segments retrieveSegmentAncestors(String id)
            throws SegmentException {
        Segment seg = getSegmentTree().getSegmentForId(id);
        return createSegments(getSegmentTree().getAncestors(seg));
    }
    
    public Segment retrieveSegmentDataForId(String id) {
        return getSegmentTree().getSegmentForId(id);
    }
    
    public String getSegmentContentType() {
        throw new UnsupportedOperationException("getSegmentContentType is not yet supported");
    }

    public Segments retrieveAllSegmentData() {
        return segmentDao.retrieveAllSegmentData();
    }

    public void setSegmentDao(ISegmentDataService segmentDao) {
        this.segmentDao = segmentDao;
    }

    public Segments retrieveSegmentsWithNameOrAlias(
            String alias) {
        return createSegments(getSegmentTree().retrieveSegmentsWithNameOrAlias(alias));
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
    
    protected Segments createSegments(Collection<? extends Segment> segments) {
        List<Segment> segs = new ArrayList<Segment>(segments);
        return new Segments(segs);
    }

}
