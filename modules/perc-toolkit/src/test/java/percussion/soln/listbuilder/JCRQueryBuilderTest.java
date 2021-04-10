package percussion.soln.listbuilder;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.percussion.soln.listbuilder.JCRQueryBuilder;


public class JCRQueryBuilderTest {
    
    private JCRQueryBuilder builder;
    
    @Before
    public void setUp() {
        builder = new JCRQueryBuilder();
    }
    
    
    @Test
    public void testGetQuery() throws Exception {
        String query = builder.getQuery();
        assertThat(query, notNullValue());
        assertEquals("select rx:sys_contentid, rx:sys_folderid, jcr:path from nt:base", query);
    }
    
    @Test
    public void testGetQueryWithDateRange() throws Exception {
        
        builder.setTitleContains("News");
        builder.setStartDate("2010");
        
        String query = builder.getQuery();
        
        assertThat(query, notNullValue());
        assertEquals("select rx:sys_contentid, rx:sys_folderid, jcr:path from nt:base where " +
        		"(('2010' < rx:sys_contentstartdate ) and rx:displaytitle like '%News%' )", query.replaceAll("  ", " "));
    }
    
    @Test
    public void testGetQueryWithPaths() throws Exception {
        builder.setFolderPaths(asList("//Sites/a", "//Sites/b"));
        String query = builder.getQuery();
        assertThat(query, notNullValue());
        assertEquals("select rx:sys_contentid, rx:sys_folderid, jcr:path from nt:base where (( jcr:path like '//Sites/a/%'  or  jcr:path like '//Sites/b/%' ))", query);
    }
    
    

}
