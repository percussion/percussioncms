/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

