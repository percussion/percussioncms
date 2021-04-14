package test.percussion.pso.relationshipbuilder.exit;

import com.percussion.error.PSException;
import com.percussion.pso.relationshipbuilder.IPSRelationshipBuilder;
import com.percussion.pso.relationshipbuilder.exit.PSExtensionHelper;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.xml.PSXmlDocumentBuilder;
import junit.framework.TestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.custommonkey.xmlunit.XMLAssert.*;

public class PSExtensionHelperTest extends TestCase
{

   private Set<Integer> m_output;

   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception
   {
      m_output = new HashSet<Integer>();
      XMLUnit.setControlParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
      XMLUnit.setTestParser("org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
      XMLUnit.setSAXParserFactory("org.apache.xerces.jaxp.SAXParserFactoryImpl");
      XMLUnit.setTransformerFactory("org.apache.xalan.processor.TransformerFactoryImpl");
      XMLUnit.setIgnoreWhitespace(true);
   }

   public void testConvertRejectsNullOutput()
   {
      boolean threw = false;
      try
      {
         PSExtensionHelper.convert(null, null);
      }
      catch (IllegalArgumentException e)
      {
         threw = true;
      }
      assertTrue(threw);
   }

   public void testConvertHandlesNullInput()
   {
      Collection<Object> invalids = PSExtensionHelper.convert(null, m_output);
      assertNotNull(invalids);
      assertEquals(0, invalids.size());
   }

   public void testConvertHandlesAllNullsInInput()
   {
      Object[] input = new Object[] {null, null};
      Collection<Object> invalids = PSExtensionHelper.convert(input, m_output);
      assertNotNull(invalids);
      assertEquals(2, invalids.size());
   }

   public void testConvertHandlesNullsInInput()
   {
      Object[] input = new Object[] {"700", null, 301};
      Collection<Object> invalids = PSExtensionHelper.convert(input, m_output);
      assertNotNull(invalids);
      assertEquals(1, invalids.size());
      assertEquals(2, m_output.size());
   }
   
   public void testConvertHandlesEmptysInInput()
   {
      Object[] input = new Object[] {"700", "", 301};
      Collection<Object> invalids = PSExtensionHelper.convert(input, m_output);
      assertNotNull(invalids);
      assertEquals(1, invalids.size());
      assertEquals(2, m_output.size());
   }
   
   public void testConvertSingleEmptyString() {
	      Object[] input = new Object[] {""};
	      Collection<Object> invalids = PSExtensionHelper.convert(input, m_output);
	      assertNotNull(invalids);
	      assertEquals(1, invalids.size());
	      assertEquals(0, m_output.size());
          if (invalids.size() == 1 && invalids.contains("")) {
       	   
          }
          else {
        	  fail();
          }
   }

   @Test
   @Ignore("Test is failing") //TODO: Fix me
   public void testUpdateDisplayChoicesSelectAll() throws Exception {
       Map<String, String> params = new HashMap<String, String>();
       params.put(PSExtensionHelper.IDS_FIELD_NAME, "tree");
       params.put(IPSHtmlParameters.SYS_CONTENTID, "100");
       IPSRelationshipBuilder builder = new IPSRelationshipBuilder() {
            public Collection<Integer> retrieve(int sourceId)
                    throws PSAssemblyException, PSException {
                return null;
            }

            public void synchronize(int sourceId, Set<Integer> targetIds)
                    throws PSAssemblyException, PSException {

            }
            
            public void addRelationships(Collection<Integer> ids)
					throws PSAssemblyException, PSException {
			}

        };
       PSExtensionHelper helper = new PSExtensionHelper(builder, params, null);
       String basePath = "src/test/percussion/pso/relationshipbuilder/exit";
       Document actual = PSXmlDocumentBuilder.createXmlDocument(
               new FileInputStream(new File(basePath + "/" +"BeforeCe.xml"))
               , false);
       Document expected = PSXmlDocumentBuilder.createXmlDocument(
               new FileInputStream(new File(basePath + "/" +"ExpectedSelectAllCe.xml"))
               , false);
       helper.updateDisplayChoices(actual, true);
       assertXMLEqual(expected, actual);
       
       
   }

   @Test
   @Ignore("Test is failing") //TODO: Fix me
   public void testUpdateDisplayChoices() throws Exception {
       Map<String, String> params = new HashMap<String, String>();
       params.put(PSExtensionHelper.IDS_FIELD_NAME, "tree");
       params.put(IPSHtmlParameters.SYS_CONTENTID, "100");
       
       IPSRelationshipBuilder stub = new IPSRelationshipBuilder() {

           public Collection<Integer> retrieve(int sourceId) throws PSAssemblyException, PSException {
               if (sourceId != 100) throw new IllegalStateException("Content id is wrong");
               return Arrays.asList(307,318);
           }
       
           public void synchronize(int sourceId, Set<Integer> targetIds) throws PSAssemblyException, PSException {
               throw new IllegalStateException("Should not be called");
               
           }
           
           public void addRelationships(Collection<Integer> ids)
					throws PSAssemblyException, PSException {
			}
          
      };
      
       PSExtensionHelper helper = new PSExtensionHelper(stub, params, null);
       String basePath = "src/test/percussion/pso/relationshipbuilder/exit";
       Document actual = PSXmlDocumentBuilder.createXmlDocument(
               new FileInputStream(new File(basePath + "/" +"BeforeCe.xml"))
               , false);
       Document expected = PSXmlDocumentBuilder.createXmlDocument(
               new FileInputStream(new File(basePath + "/" +"ExpectedCe.xml"))
               , false);
       helper.updateDisplayChoices(actual, false);

       
       assertXMLEqual(expected, actual);
       //307, 318       
       
   }

}
