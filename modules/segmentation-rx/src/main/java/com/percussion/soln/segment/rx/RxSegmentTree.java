package com.percussion.soln.segment.rx;

import static java.text.MessageFormat.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.data.SegmentDataTree;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;

public class RxSegmentTree extends SegmentDataTree {
    
    private IPSContentMgr contentManager;
    private IPSContentWs  contentWs;
    private IPSGuidManager guidManager;
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(RxSegmentTree.class);
    
    public RxSegmentTree(IPSContentMgr contentManager, IPSContentWs contentWs, IPSGuidManager guidManager) {
        super();
        this.contentManager = contentManager;
        this.contentWs = contentWs;
        this.guidManager = guidManager;
    }


    protected Collection<Segment> retrieveSegmentsForItem(int legacyId) 
        throws PSErrorException {
        IPSGuid guid = guidManager.makeGuid(new PSLocator(legacyId, -1));
        String[] paths = contentWs.findFolderPaths(guid);
        List<Segment> segments = new ArrayList<Segment>();
        for (String path : paths) {
            Segment data = getSegmentForPath(path);
            if (data != null) {
                segments.add(data);
            }
        }
        return segments;
    }


    private void errorIfNotSelectingOnField(String jcrQuery, String field) {
        if (!jcrQuery.contains(field)) {
            throw new IllegalArgumentException(
                    "The query string does not select on " + field);
        }
    }
    
    /**
    * Setups this taxonomy object used the inputted jcrQuery.
    */
    protected void make(String jcrQuery, String labelField, String aliasField) {
        if (jcrQuery == null){
            throw new IllegalArgumentException("jcrQuery cannot be null");
        }
        reset();
        errorIfNotSelectingOnField(jcrQuery, "rx:sys_folderid");
        errorIfNotSelectingOnField(jcrQuery, "jcr:path");
        errorIfNotSelectingOnField(jcrQuery, labelField);
        if (aliasField != null)
            errorIfNotSelectingOnField(jcrQuery, aliasField);
        try {
            Query q = contentManager.createQuery(jcrQuery, Query.SQL);
            QueryResult results = contentManager.executeQuery(q, -1, null, null);
            addNodesFromQuery(results, labelField, aliasField);
        } catch (InvalidQueryException e) {
            throw new IllegalArgumentException("Query is invalid: ",e);
        } catch (RepositoryException e) {
            throw new RuntimeException("Something wrong with the repository: ", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void addNodesFromQuery(QueryResult results, String labelField, String aliasField) 
        throws ValueFormatException, IllegalStateException, ItemNotFoundException, RepositoryException {
        //String[] propNames = results.getColumnNames();
        Iterator<Row> riter = results.getRows();
        while (riter.hasNext()) {
            Row r = riter.next();
            /*
            Map<String,Value> segProps = new HashMap<String, Value>();
            for (String prop : propNames) {
                segProps.put(prop, r.getValue(prop));
            }
            */
            int cid = (int) getLongValueOrFail(r,"rx:sys_contentid");
            int fid = (int) getLongValueOrFail(r,"rx:sys_folderid");
            String path = getStringValueOrFail(r,"jcr:path");
            String label = getStringValueOrFail(r,labelField);
            Segment node = createSegment(""+cid, label, path, fid);
            if ( aliasField != null ) {
                Value v = r.getValue(aliasField);
                if (v != null && v.getString() != null) {
                    String aliasesStr = getStringValueOrFail(r, aliasField);
                    List<String> aliases = SegmentAliasUtil.parseAliases(aliasesStr);
                    node.setAliases(new HashSet<String>(aliases));
                }
                else {
                    log.debug(format("Segment id : {0} has a null aliases field ({1})",
                            new Object[] { cid, aliasField } ));
                }
            }
            update(Arrays.asList(node));
            //node.setProperties(segProps);
        }
    }
    
    private Value getValueOrFail(Row r, String name) throws ItemNotFoundException, RepositoryException {
        if(r.getValue(name) == null) {
            throw new IllegalArgumentException("Column " + name + " is null and should not be");
        }
        return r.getValue(name);
    }
    private long getLongValueOrFail(Row r, String name) throws ItemNotFoundException, RepositoryException {
        return getValueOrFail(r, name).getLong();
    }
    
    private String getStringValueOrFail(Row r, String name) throws ItemNotFoundException, RepositoryException {
        return getValueOrFail(r,name).getString();
    }
    
    protected List<Long> valuesToLongs(Collection<Value> values) {
        List<Long> rvalue = new ArrayList<Long>();
        for(Value v : values) {
            try {
                rvalue.add(v.getLong());
            } catch (ValueFormatException e) {
                throw new IllegalArgumentException("Value " + v + " is not a long");
            } catch (IllegalStateException e) {
                throw new RuntimeException(e);
            } catch (RepositoryException e) {
                throw new RuntimeException(e);
            }
        }
        return rvalue;
    }
    
    protected List<Value> getValuesFromQuery(String field, QueryResult results) throws RepositoryException {
        List<Value> ids = new ArrayList<Value>();
        RowIterator riter = results.getRows();
        while (riter.hasNext()) {
            Row r = riter.nextRow();
            Value fid = r.getValue(field);
            ids.add(fid);
        }
        return ids;
    }

    protected Segment createSegment(String id, String name, String path, int folderId) {
        Segment node = new Segment();
        node.setFolderPath(path);
        node.setId(id);
        node.setName(name);
        node.setFolderId(folderId);
        return node;
    }
}
