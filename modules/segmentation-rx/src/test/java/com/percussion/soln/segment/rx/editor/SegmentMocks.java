package test.percussion.soln.segment.rx.editor;

import static java.util.Arrays.*;

import org.jmock.Expectations;
import org.jmock.Mockery;

import com.percussion.soln.segment.ISegmentNode;
import com.percussion.soln.segment.ISegmentTree;
import com.percussion.soln.segment.Segment;
import com.percussion.soln.segment.Segments;

public class SegmentMocks {

    private Mockery context;
    
    public  Mockery getMockery() { return this.context; }

    public SegmentMocks(Mockery context) {
        this.context = context;
    }
    


    public ISegmentTree makeTreeStub(final ISegmentNode root) {
        Mockery context = getMockery();
        final ISegmentTree tree = context.mock(ISegmentTree.class);
        context.checking(new Expectations() {{ 
            one(tree).getRootNode();
            will(returnValue(root));
        }});
        return tree;
    }

    public ISegmentNode makeRootSegmentStub(final String name) {
        Mockery context = getMockery();
        final ISegmentNode segment = context.mock(ISegmentNode.class, name);
        context.checking(new Expectations() {{ 
            one(segment).getName();
            will(returnValue(name));
            
        }});
        
        return segment;
    }
    
    public ISegmentNode makeSegmentStub(final int id, final String name) {
        Mockery context = getMockery();
        final ISegmentNode segment = context.mock(ISegmentNode.class, name);
        context.checking(new Expectations() {{ 
            one(segment).getName();
            will(returnValue(name));
            
            one(segment).getId();
            will(returnValue(""+id));
            
        }});
        
        return segment;
    }


    public ISegmentNode makeSegmentStub(final int id, final String name, final String path, final boolean selectable) {
        Mockery context = getMockery();
        final ISegmentNode segment = context.mock(ISegmentNode.class, path);
        context.checking(new Expectations() {{ 
            one(segment).getName();
            will(returnValue(name));
            
            one(segment).getId();
            will(returnValue(""+id));
            
            one(segment).isSelectable();
            will(returnValue(selectable));
            
        }});
        
        return segment;
    }

    public void noChildren(final ISegmentNode segment) {
        Mockery context = getMockery();
        context.checking(new Expectations() {{ 
            one(segment).getChildren();
            will(returnValue(null));
        }});
    }

    public void addChildren(final ISegmentNode segment, final ISegmentNode ... childs) {
        Mockery context = getMockery();
        context.checking(new Expectations() {{ 
            one(segment).getChildren();
            will(returnValue(asList(childs)));
        }});
    }


    public Segment makeSegment(Number id, String name) {
        Segment seg = new Segment();
        seg.setId(""+id);
        seg.setName(name);
        return seg;
    }
    
    public Segments makeSegments(Segment ... segs) {
        return new Segments(asList(segs));
    }

}