import org.springframework.web.context.support.WebApplicationContextUtils;
import com.percussion.soln.segment.*;
import groovy.xml.MarkupBuilder;


ISegmentService segmentService = 
    WebApplicationContextUtils
    .getRequiredWebApplicationContext(context)
    .getBean("segmentService");

SegmentTreeFactory f = new SegmentTreeFactory();
ISegmentTree tree = f.createSegmentTreeFromService(segmentService);

def writer = new StringWriter();
def xml = new MarkupBuilder(writer);

def xmlNode(builder, treeNode) {
	return builder.node(id: treeNode.getId(), label: treeNode.getName()) {
		for(child in treeNode.getChildren()) {
			xmlNode(builder, child);
		}
	}
}

xml.tree() {
	for (child in tree.getRootNode().getChildren()) {
		xmlNode(xml, child);
	}
}
print writer.toString();
response.setContentType("text/xml");

