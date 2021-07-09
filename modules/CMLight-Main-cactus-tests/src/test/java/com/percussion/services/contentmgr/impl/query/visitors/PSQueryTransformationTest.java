/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.contentmgr.impl.query.visitors;

import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.data.PSQuery;
import com.percussion.services.contentmgr.impl.legacy.PSTypeConfiguration;
import com.percussion.services.contentmgr.impl.query.IPSFolderExpander;
import com.percussion.services.contentmgr.impl.query.IPSPropertyMapper;
import com.percussion.services.contentmgr.impl.query.SqlLexer;
import com.percussion.services.contentmgr.impl.query.SqlParser;
import com.percussion.services.contentmgr.impl.query.XpathLexer;
import com.percussion.services.contentmgr.impl.query.XpathParser;
import com.percussion.services.contentmgr.impl.query.nodes.IPSQueryNode;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.timing.PSStopwatch;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.cactus.ServletTestCase;
import org.junit.experimental.categories.Category;

/**
 * Test aspects of the transformation engine
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSQueryTransformationTest extends ServletTestCase
{
   
   List<IPSGuid> pathGuids = new ArrayList<IPSGuid>();
   
   public void setUp() throws Exception
   {
      super.setUp();
      pathGuids.add(new PSGuid(PSTypeEnum.INTERNAL, 301));
      pathGuids.add(new PSGuid(PSTypeEnum.INTERNAL, 302));
   }
   
   /**
    * Mock expander for testing purposes
    */
   class TestFolderExpander implements IPSFolderExpander
   {

      /**
       * (non-Javadoc)
       * 
       * @see com.percussion.services.contentmgr.impl.query.IPSFolderExpander#expandPath(java.lang.String)
       */
      public List<IPSGuid> expandPath(String path)
      {
         return pathGuids;
      }

   }

   /**
    * Mock property mapper for test purposes
    */
   static class TestPropertyMapper implements IPSPropertyMapper
   {
      /**
       * (non-Javadoc)
       * 
       * @see com.percussion.services.contentmgr.impl.query.IPSPropertyMapper#translateProperty(java.lang.String)
       */
      public String translateProperty(String propname)
      {
         // Do nothing for testing purposes
         return propname;
      }

   }

   /**
    * Test data
    */
   static String result1 = "qn-conjunction("
         + "qn-compare(id(f.owner_id),IN,[0-0-301, 0-0-302]),"
         + "AND,qn-conjunction("
         + "qn-conjunction("
         + "qn-compare(id(sys_componentsummary.m_contentTypeId),EQ,316),"
         + "AND,qn-compare(id(rx:pr_type),NE,x)),OR,qn-compare(id(rx:keywords),EQ,foo)))";
   /**
    * Test data
    */
   static String where1 = "(f.owner_id in (301,302) AND " +
        "((sys_componentsummary.m_contentTypeId = :p0 AND " +
        "c0.pr_type != :p1) OR c0.keywords = :p2))";
   
   static String resultNoFolders = "qn-conjunction("
      + "false,"
      + "AND,qn-conjunction("
      + "qn-conjunction("
      + "qn-compare(id(sys_componentsummary.m_contentTypeId),EQ,316),"
      + "AND,qn-compare(id(rx:pr_type),NE,x)),OR,qn-compare(id(rx:keywords),EQ,foo)))";

   /**
    * Note that the example doesn't need a valid path. All paths will expand to
    * one folder id (301).
    */
   static String example = "select * from rx:rffpressrelease "
         + "where jcr:path like '/sites/enterpriseinvestments/%' and ( "
         + "jcr:primaryType = 'rx:rffpressrelease' and "
         + "rx:pr_type <> 'x' or rx:keywords = 'foo' ) order by rx:displaytitle asc";

   /**
    * @throws Exception
    */
   public void testSqlSimpleExpression() throws Exception
   {
      Reader reader = new StringReader(example);
      SqlLexer lexer = new SqlLexer(reader);
      SqlParser parser = new SqlParser(lexer);

      PSCmsObjectMgrLocator.getObjectManager();

      PSStopwatch sw = new PSStopwatch();
      sw.start();

      PSQuery q = parser.start_rule();
      System.err.println("Parsing: " + sw);

      checkResults(q, result1, where1, sw);
   }
   
   /**
    * Make it so the folder expander finds no paths.
    * @throws Exception
    * @author adamgent
    */
   public void testNoFoldersFound() throws Exception
   {
      pathGuids = new ArrayList<IPSGuid>();
      Reader reader = new StringReader(example);
      SqlLexer lexer = new SqlLexer(reader);
      SqlParser parser = new SqlParser(lexer);

      PSCmsObjectMgrLocator.getObjectManager();

      PSStopwatch sw = new PSStopwatch();
      sw.start();

      PSQuery q = parser.start_rule();
      System.err.println("Parsing: " + sw);
      

      //Where clause gets eliminated.
      checkResults(q, resultNoFolders, "", sw);
   }   

   /**
    * Test data
    */
   static String xexample = "/sites/enterpriseinvestments/element(*,rx:rffpressrelease)"
         + "[@jcr:primaryType = 'rx:rffpressrelease'"
         + " and @rx:pr_type != 'x'"
         + " or @rx:keywords = 'foo'] order by @rx:displaytitle ascending";

   /**
    * @throws Exception
    */
   public void testXpathSimpleExpression() throws Exception
   {
      Reader reader = new StringReader(xexample);
      XpathLexer lexer = new XpathLexer(reader);
      XpathParser parser = new XpathParser(lexer);

      PSCmsObjectMgrLocator.getObjectManager();

      PSStopwatch sw = new PSStopwatch();
      sw.start();

      PSQuery q = parser.start_rule();
      System.err.println("Parsing: " + sw);

      checkResults(q, result1, where1, sw);
   }

   /**
    * Test data
    */
   static String xexample2 = "/sites/enterpriseinvestments/element(*,rx:rffpressrelease)"
         + "[jcr:like(@rx:sys_title, 'foo%bar') and @rx:keywords = 'foo']";

   /**
    * Test data
    */
   static String result2 = "qn-conjunction("
         + "qn-compare(id(f.owner_id),IN,[0-0-301, 0-0-302]),"
         + "AND,qn-conjunction(qn-compare(id(rx:sys_title),LIKE,foo%bar),"
         + "AND,qn-compare(id(rx:keywords),EQ,foo)))";

   /**
    * Test data
    */
   static String where2 = "(f.owner_id in (301,302) AND"
         + " (upper(cs.m_name) like upper(:p0) AND"
         + " c0.keywords = :p1))";

   /**
    * @throws Exception
    */
   public void testJcrpathExpression() throws Exception
   {
      Reader reader = new StringReader(xexample2);
      XpathLexer lexer = new XpathLexer(reader);
      XpathParser parser = new XpathParser(lexer);

      PSCmsObjectMgrLocator.getObjectManager();

      PSStopwatch sw = new PSStopwatch();
      sw.start();

      PSQuery q = parser.start_rule();
      System.err.println("Parsing: " + sw);

      checkResults(q, result2, where2, sw);
   }

   /**
    * Does the bulk of the work for the test. This method is called repeatedly
    * by the different cases. It starts with the specific query, then looks at
    * the result of the transformation in a couple of spots.
    * 
    * @param q the query object that is used as a starting place
    * @param res the expected node tree that should result from the transformer
    * @param wres the expected where clause for the query
    * @param sw a stopwatch instance
    * @throws Exception
    */
   @SuppressWarnings("unchecked")
   public void checkResults(PSQuery q, String res, String wres, PSStopwatch sw)
         throws Exception
   {
      IPSDatasourceManager dbMgr = (IPSDatasourceManager) PSBaseServiceLocator
            .getBean("sys_datasourceManager");
      boolean isDerbyDatabase = dbMgr.getConnectionDetail(null).getDriver().equals("derby");
      
      PSItemDefManager mgr = PSItemDefManager.getInstance();
      PSItemDefinition def = mgr.getItemDef("rffpressrelease", 1001);
      PSTypeConfiguration type = new PSTypeConfiguration(def, null, isDerbyDatabase);
      IPSQueryNode n = q.getWhere();

      PSQueryPropertyType typesetter = new PSQueryPropertyType();
      typesetter.setConfig(type);
      PSQueryTransformer transformer = new PSQueryTransformer(
            new TestFolderExpander(), new HashMap(), null);

      IPSQueryNode tf = n.accept(typesetter);
      sw.pause();
      System.err.println("Type set: " + sw);

      sw.cont();
      tf = tf.accept(transformer);
      sw.pause();
      System.err.println("Transform: " + sw);

      String rep = tf.toString();

      assertEquals(res, rep);

      sw.cont();
      PSQueryNodeVisitor where = new PSQueryWhereBuilder(type, null);

      tf.accept(where);

      String whereclause = where.toString();
      sw.stop();
      System.err.println("Where: " + sw);
      assertEquals(wres, whereclause);
   }
}
