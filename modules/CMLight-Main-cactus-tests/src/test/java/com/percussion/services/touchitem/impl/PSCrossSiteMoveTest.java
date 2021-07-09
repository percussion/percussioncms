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

package com.percussion.services.touchitem.impl;


import static java.util.Arrays.asList;

import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSErrorException;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;

/**
 * <h1>MOVE MATRIX</h1>
 * Use Eclipse view Javadoc to see the table below.
 * <h3> The test only covers the first row right now (siteid and folderid set). </h3>
 * <p> 
<TABLE WIDTH=639 CELLPADDING=7 CELLSPACING=1>
    <COL WIDTH=135>
    <COL WIDTH=94>
    <COL WIDTH=90>
    <COL WIDTH=118>
    <COL WIDTH=124>
    <TR VALIGN=TOP>
        <TD WIDTH=135 STYLE="border-top: none; border-bottom: 1px solid #000000; border-left: none; border-right: none; padding: 0in">
            <P><BR>
            </P>
        </TD>
        <TD COLSPAN=4 WIDTH=472 STYLE="border: 1px solid #000000; padding: 0in 0.08in">
            <P ALIGN=CENTER>Action</P>
        </TD>
    </TR>
    <TR VALIGN=TOP>
        <TD WIDTH=135 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P>Item is a dependent of a relationship with�</P>
        </TD>
        <TD WIDTH=94 STYLE="border-top: 1px solid #000000; border-bottom: 1.10pt double #000000; border-left: 1.10pt double #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in"><U>1. Reorganize</U></P>
            <P>Move to folder within same site</P>
        </TD>
        <TD WIDTH=90 STYLE="border-top: 1px solid #000000; border-bottom: 1.10pt double #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in"><U>2. Reclassify</U></P>
            <P>Move to folder on different site</P>
        </TD>
        <TD WIDTH=118 STYLE="border-top: 1px solid #000000; border-bottom: 1.10pt double #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in"><U>3. Stage/Archive</U></P>
            <P>Move from site folder to non-site folder</P>
        </TD>
        <TD WIDTH=124 STYLE="border-top: 1px solid #000000; border-bottom: 1.10pt double #000000; border-left: 1px solid #000000; border-right: 1px solid #000000; padding: 0in 0.08in">
            <P STYLE="margin-bottom: 0in"><U>4. Deploy/Recover</U></P>
            <P>Move from non-site folder to site folder</P>
        </TD>
    </TR>
    <TR VALIGN=TOP>
        <TD WIDTH=135 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in">�both sys_siteid and sys_folderid</P>
            <P>(full cross-site)</P>
        </TD>
        <TD WIDTH=94 STYLE="border-top: 1.10pt double #000000; border-bottom: 1px solid #000000; border-left: 1.10pt double #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in"><BR>
            </P>
            <P STYLE="margin-bottom: 0in">Keep siteid.</P>
            <P>Update folderid.</P>
        </TD>
        <TD WIDTH=90 BGCOLOR="#e6e6e6" STYLE="border-top: 1.10pt double #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in"><BR>
            </P>
            <P STYLE="margin-bottom: 0in"><A NAME="_Ref129587822"></A>Update
            siteid<A CLASS="sdfootnoteanc" NAME="sdfootnote1anc" HREF="#sdfootnote1sym" SDFIXED><SUP>�</SUP></A>.</P>
            <P>Update folderid.</P>
        </TD>
        <TD WIDTH=118 STYLE="border-top: 1.10pt double #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in"><BR>
            </P>
            <P STYLE="margin-bottom: 0in">Keep siteid.</P>
            <P>Update folderid.</P>
        </TD>
        <TD WIDTH=124 STYLE="border-top: 1.10pt double #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: 1px solid #000000; padding: 0in 0.08in">
            <P STYLE="margin-bottom: 0in"><BR>
            </P>
            <P STYLE="margin-bottom: 0in">Keep (if valid) or update siteidError: Reference source not found.</P>
            <P>Update folderid.</P>
        </TD>
    </TR>
    <TR VALIGN=TOP>
        <TD WIDTH=135 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in">�only sys_folderid</P>
            <P>(disambiguation)</P>
        </TD>
        <TD WIDTH=94 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1.10pt double #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in"><BR>
            </P>
            <P>Update folderid.</P>
        </TD>
        <TD WIDTH=90 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in"><BR>
            </P>
            <P>Update folderid.</P>
        </TD>
        <TD WIDTH=118 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in"><BR>
            </P>
            <P>Update folderid.</P>
        </TD>
        <TD WIDTH=124 STYLE="border: 1px solid #000000; padding: 0in 0.08in">
            <P STYLE="margin-bottom: 0in"><BR>
            </P>
            <P>Update folderid.</P>
        </TD>
    </TR>
    <TR VALIGN=TOP>
        <TD WIDTH=135 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in">�only sys_siteid 
            </P>
            <P>(partial cross-site)</P>
        </TD>
        <TD WIDTH=94 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1.10pt double #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in"><BR>
            </P>
            <P>Keep siteid.</P>
        </TD>
        <TD WIDTH=90 BGCOLOR="#e6e6e6" STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in">If last on site, 
            </P>
            <P>Update siteidError: Reference source not found.</P>
        </TD>
        <TD WIDTH=118 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in"><BR>
            </P>
            <P>Keep siteid.</P>
        </TD>
        <TD WIDTH=124 STYLE="border: 1px solid #000000; padding: 0in 0.08in">
            <P STYLE="margin-bottom: 0in"><BR>
            </P>
            <P>Keep (if valid) or update siteidError: Reference source not found.
                        </P>
        </TD>
    </TR>
    <TR VALIGN=TOP>
        <TD WIDTH=135 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P>�no properties<BR>(normal)</P>
        </TD>
        <TD WIDTH=94 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1.10pt double #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in"><BR>
            </P>
            <P>Keep no props.</P>
        </TD>
        <TD WIDTH=90 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in">Error: Reference source not found</P>
            <P>Keep no props.</P>
        </TD>
        <TD WIDTH=118 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
            <P STYLE="margin-bottom: 0in"><BR>
            </P>
            <P>Keep no props.</P>
        </TD>
        <TD WIDTH=124 STYLE="border: 1px solid #000000; padding: 0in 0.08in">
            <P STYLE="margin-bottom: 0in"><BR>
            </P>
            <P>Keep no props.</P>
        </TD>
    </TR>
</TABLE> 
 * 
 *
 * 
 * @author adamgent
 * 
 */
@Category(IntegrationTest.class)
public class PSCrossSiteMoveTest extends PSCrossSiteTestCase
{
   
   private String sourcePath;
   private String targetPath;
   private List<Integer> items = new ArrayList<Integer>();
   
   
   public void setUp() throws Exception {
      super.setUp();
   }
   
   public void tearDown() throws Exception {
      move(targetPath, sourcePath, items);
      super.tearDown();
   }
   

   
   /*
    * Reorganize
    */
   public void testMoveItemWithinSameSite() throws Exception {
       

      PSRelationshipFilter f = new PSRelationshipFilter();
      f.setDependentId(459);
      f.setOwnerId(551);
      
      sourcePath = "//Sites/CorporateInvestments/Images/People";
      targetPath = "//Sites/CorporateInvestments/Files";
      items = asList(459);
      List<PSRelationship> rs = rservice.findByFilter(f);
      assertEquals(2, rs.size());
      
      assertRel(rs, 8, 303, getFolderId(sourcePath));
      assertRel(rs, 7, 303, getFolderId(sourcePath));
      
      move();
      
      rs = rservice.findByFilter(f);
      assertEquals(2, rs.size());
      assertRel(rs, 8, 303, getFolderId(targetPath));
      assertRel(rs, 7, 303, getFolderId(targetPath));
      
   }
   
   /*
    * Reorganize
    */
   public void testMoveFolderWithinSameSite() throws Exception {
      

      PSRelationshipFilter f = new PSRelationshipFilter();
      f.setDependentId(459);
      f.setOwnerId(551);
      int folderId = getFolderId("//Sites/CorporateInvestments/Images/People");
      sourcePath = "//Sites/CorporateInvestments/Images";
      targetPath = "//Sites/CorporateInvestments/Files";
      
      items = asList(folderId);
      List<PSRelationship> rs = rservice.findByFilter(f);
      assertEquals(2, rs.size());
      
      assertRel(rs, 8, 303, folderId);
      assertRel(rs, 7, 303, folderId);
      
      move();
      
      rs = rservice.findByFilter(f);
      assertEquals(2, rs.size());
      assertRel(rs, 8, 303, folderId);
      assertRel(rs, 7, 303, folderId);
      
   }
   
   /*
    * Reclassify
    */
   public void testMoveFolderToDifferentSite() throws Exception {
      
      PSRelationshipFilter f = new PSRelationshipFilter();
      int depId = 442;
      int folderId = 441;
      f.setDependentId(depId);
      f.setOwnerId(634);
      
      sourcePath = "//Sites/EnterpriseInvestments/Images";
      targetPath = "//Sites/CorporateInvestments/Files";
      items = asList(folderId);
      List<PSRelationship> rs = rservice.findByFilter(f);
      assertEquals(2, rs.size());

      assertRel(rs, 2, 301, folderId);
      
      move();
      
      rs = rservice.findByFilter(f);
      assertEquals(2, rs.size());

      assertRel(rs, 2, 303, folderId);
      
   }
   
   /*
    * Reclassify
    */
   public void testMoveItemToDifferentSite() throws Exception {
      

      PSRelationshipFilter f = new PSRelationshipFilter();
      int depId = 442;
      f.setDependentId(depId);
      f.setOwnerId(634);
      
      sourcePath = "//Sites/EnterpriseInvestments/Images/CreditCard";
      targetPath = "//Sites/CorporateInvestments/Files";
      items = asList(depId);
      List<PSRelationship> rs = rservice.findByFilter(f);
      assertEquals(2, rs.size());

      assertRel(rs, 2, 301, getFolderId(sourcePath));
      
      move();
      
      rs = rservice.findByFilter(f);
      assertEquals(2, rs.size());

      assertRel(rs, 2, 303, getFolderId(targetPath));
      
   } 
   
   private void move() {
      log.info("Sourcepath: " + sourcePath + " targetPath: " + targetPath + " items: " + items);
      move(sourcePath,targetPath, items);
   }

   private void move(String s, String t, List<Integer> childItems)
   {
      List<IPSGuid> guids = new ArrayList<IPSGuid>();
      for (Integer i : childItems) {
         guids.add(g.makeGuid(new PSLocator(i)));
      }
      
      try
      {
         c.moveFolderChildren(s, t, guids);
      }
      catch (PSErrorException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   private static final Logger log = LogManager.getLogger(PSCrossSiteMoveTest.class);

}
