package integrationtest.p13n.segment;

import static integrationtest.spring.SpringSetup.getBean;
import static integrationtest.spring.SpringSetup.loadXmlBeanFiles;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.percussion.soln.segment.ISegmentService;
import com.percussion.soln.segment.Segment;

public class SegmentServiceTest {
    private static ISegmentService segmentService;
    
    
    @BeforeClass
    public static void setupSpring() throws Exception {
        loadXmlBeanFiles("file:ds/webapp/WEB-INF/applicationContext.xml",
                "file:ds/webapp/WEB-INF/spring/ds/applicationContext-ds.xml",
                "file:ds/webapp/WEB-INF/track-servlet.xml",
                "file:ds/webapp/WEB-INF/spring/ds/data-beans.xml",
                "classpath:META-INF/p13n/spring/**/*.xml",
                "classpath:integrationtest/p13n/ds/test-beans.xml");
        segmentService = getBean("segmentService", ISegmentService.class);
    }
    
    @Test
    public void testFindByPath() throws Exception {
        List<? extends Segment> segments = 
            segmentService.retrieveSegments(asList("//Folders/Segments/Regions/Western Hemisphere", 
                    "//Folders/Segments/Regions/Western Hemisphere/North America")).getList();
        assertThat(segments.get(0).getId(), is("3"));
        assertThat(segments.get(1).getId(), is("5"));
    }

}
