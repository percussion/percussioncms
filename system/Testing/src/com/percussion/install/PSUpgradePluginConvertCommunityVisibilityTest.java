/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.install;

import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.datasource.PSDatasourceMgrLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.security.IPSAcl;
import com.percussion.services.security.IPSAclService;
import com.percussion.services.security.PSAclServiceLocator;
import com.percussion.services.security.PSSecurityException;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jdbc.IPSDatasourceManager;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionInfo;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.timing.PSStopwatchStack;
import com.percussion.xml.PSXmlTreeWalker;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Test the upgrade plugin by running it against the upgrade data
 * 
 * @author dougrand
 */
@Category(IntegrationTest.class)
public class PSUpgradePluginConvertCommunityVisibilityTest
{
   /**
    * This is the lower limit on the ids used. The same ids are used every time.
    */
   private static final int MIN_TEST_OID = 10000000;

   /**
    * This is the count of objects to use. Tune for a reasonable time.
    */
   private static final int OID_COUNT = 100;

   /**
    * Everything is joined to these.
    */
   private static final int communities[] =
   {1001, 1003};
   
   /**
    * The connection details, used to qualify the table name
    */
   private PSConnectionDetail m_details;

   private IPSDatasourceManager m_dbm;

   private PSConnectionInfo m_info;

   @BeforeClass
   public static void setUp() throws Exception
   {
      // Forces spring to initialize with standard junit configuration
      PSAclServiceLocator.getAclService();
   }

   @Test
   public void testPlugin() throws Exception
   {
      m_dbm = PSDatasourceMgrLocator.getDatasourceMgr();
      String defaultds = m_dbm.getRepositoryDatasource();
      m_info = new PSConnectionInfo(defaultds);
      m_details = m_dbm.getConnectionDetail(m_info);
      
      // Create a bunch of data to do the upgrade with. This uses a dummy
      // table that is accessed using the upgrade configuration. The created
      // acls are removed at the end of this process.
      SessionFactory factory = (SessionFactory) PSBaseServiceLocator
            .getBean("sys_sessionFactory");

      Session s = factory.openSession();
      Transaction t = s.beginTransaction();
      PSStopwatchStack sws = PSStopwatchStack.getStack();
      sws.start("test");
      try
      {
         String tablename = PSSqlHelper.qualifyTableName(
               "RXTESTVARIANTCOMMUNITY",
               m_details.getDatabase(), m_details.getOrigin(), 
               m_details.getDriver());
         sws.start("setup");
         // Create dummy data

         
         try
         {
            s.createQuery("drop table " + tablename).executeUpdate();
         }
         catch(Exception e)
         {
            // Ignore since the table may not exist
         }
         s.createQuery("create table " + tablename +
                    " (variantid int not null," +
                    "communityid int not null, " +
                    "primary key (variantid, communityid))").executeUpdate();

         Query query = s.createQuery("insert into " + tablename +
                " (variantid, communityid) values (?, ?)");
         for(int vid = MIN_TEST_OID; vid < (MIN_TEST_OID + OID_COUNT); vid++)
         {
            for(int cid : communities)
            {
               query.setParameter(0, vid);
               query.setParameter(1, cid);
               query.executeUpdate();
            }
         }
         t.commit(); // Commit test data
         t.begin();
         cleanAclEntries();
         sws.stop();
         sws.start("plugin load doc");
         PSUpgradePluginConvertCommunityVisibility v = new PSUpgradePluginConvertCommunityVisibility();
         InputStream stream = new FileInputStream(
               "UnitTestResources/com/percussion/rxupgrade/rxupgrade.xml");

         DocumentBuilderFactory f = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
                 false);

         DocumentBuilder builder = f.newDocumentBuilder();
         Document doc = builder.parse(stream);
         PSXmlTreeWalker walker = new PSXmlTreeWalker(doc);
         boolean found = false;
         Element e = null;
         while (!found)
         {
            e = walker.getNextElement("plugin");
            if (e == null)
               throw new Exception("Couldn't find expected entry.");
            else if (e.getAttribute("name")
                  .equals("ConvertCommunityVisibility"))
               found = true;
         }
         e = walker.getNextElement("data",
               PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
         sws.stop();
         sws.start("process entries");
         v.process(null, e);
         sws.stop();
      }
      finally
      {
         sws.start("cleanup");
         cleanAclEntries();
         sws.stop();
         if (t != null && t.getStatus() == TransactionStatus.ACTIVE)
         {
            sws.start("commit");
            t.commit();
            sws.stop();
         }
         s.close();
      }
      System.out.println(sws.getStats());
   }

   /**
    * Remove acls created during the test
    * @throws PSSecurityException
    */
   @AfterClass
   public void cleanAclEntries() throws PSSecurityException
   {
      IPSAclService acl = PSAclServiceLocator.getAclService();
      IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
      
      for(long vid = MIN_TEST_OID; vid < (MIN_TEST_OID + OID_COUNT); vid++)
      {
         IPSGuid oguid = gmgr.makeGuid(vid, PSTypeEnum.TEMPLATE);
         IPSAcl oacl = acl.loadAclForObject(oguid);
         if (oacl != null)
         {
            acl.deleteAcl(oacl.getGUID());
         }
      }
   }
}
