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


import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * <h1>REMOVE MATRIX</h1>
 * Use Eclipse view Javadoc to see the table below.
 * <h3> The test only covers the first column right now (removing item from only site folder). </h3>
 * <p> 
 * <TABLE WIDTH=639 CELLPADDING=7 CELLSPACING=1>
    <COL WIDTH=113>
    <COL WIDTH=114>
    <COL WIDTH=112>
    <COL WIDTH=112>
    <COL WIDTH=111>
    <THEAD>
        <TR VALIGN=TOP>
            <TD WIDTH=113 STYLE="border-top: none; border-bottom: 1px solid #000000; border-left: none; border-right: none; padding: 0in">
                <P><BR>
                </P>
            </TD>
            <TD COLSPAN=4 WIDTH=494 STYLE="border: 1px solid #000000; padding: 0in 0.08in">
                <P ALIGN=CENTER><B>Action</B></P>
            </TD>
        </TR>
        <TR VALIGN=TOP>
            <TD WIDTH=113 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P>Item is a dependent of AA relationship…</P>
            </TD>
            <TD WIDTH=114 STYLE="border-top: 1px solid #000000; border-bottom: 1.10pt double #000000; border-left: 1.10pt double #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P><B>1. Removing item from its only site folder</B></P>
            </TD>
            <TD WIDTH=112 STYLE="border-top: 1px solid #000000; border-bottom: 1.10pt double #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P><A NAME="OLE_LINK16"></A><A NAME="OLE_LINK15"></A><B>2.
                Removing item from one folder, but it is in other folders in same
                site</B></P>
            </TD>
            <TD WIDTH=112 STYLE="border-top: 1px solid #000000; border-bottom: 1.10pt double #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P><B>3. Removing item from only folder on site, but exists in
                folders on another site</B></P>
            </TD>
            <TD WIDTH=111 STYLE="border-top: 1px solid #000000; border-bottom: 1.10pt double #000000; border-left: 1px solid #000000; border-right: 1px solid #000000; padding: 0in 0.08in">
                <P><B>4. Removing item from any non-site folder</B></P>
            </TD>
        </TR>
    </THEAD>
    <TBODY>
        <TR VALIGN=TOP>
            <TD WIDTH=113 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P>… with <B>folderid</B> and <B>siteid</B> (full cross-site)</P>
            </TD>
            <TD WIDTH=114 BGCOLOR="#e6e6e6" STYLE="border-top: 1.10pt double #000000; border-bottom: 1px solid #000000; border-left: 1.10pt double #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P STYLE="margin-bottom: 0in"><BR>
                </P>
                <P STYLE="margin-bottom: 0in">Keep siteid.</P>
                <P><A NAME="_Ref129770019"></A>Remove folderid.<FONT FACE="Symbol, serif"><A CLASS="sdfootnoteanc" NAME="sdfootnote1anc" HREF="#sdfootnote1sym" SDFIXED><SUP></SUP></A></FONT></P>
            </TD>
            <TD WIDTH=112 BGCOLOR="#e6e6e6" STYLE="border-top: 1.10pt double #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P STYLE="margin-bottom: 0in"><BR>
                </P>
                <P>Remove folderid.</P>
            </TD>
            <TD WIDTH=112 BGCOLOR="#e6e6e6" STYLE="border-top: 1.10pt double #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P STYLE="margin-bottom: 0in"><BR>
                </P>
                <P STYLE="margin-bottom: 0in">Keep siteid</P>
                <P>Remove folderid.Error: Reference source not found</P>
            </TD>
            <TD WIDTH=111 STYLE="border-top: 1.10pt double #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: 1px solid #000000; padding: 0in 0.08in">
                <P STYLE="margin-bottom: 0in"><BR>
                </P>
                <P>Remove folderid.</P>
            </TD>
        </TR>
        <TR VALIGN=TOP>
            <TD WIDTH=113 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P><A NAME="OLE_LINK4"></A><A NAME="OLE_LINK3"></A>… with
                <B>folderid</B> but without siteid (disambiguation)</P>
            </TD>
            <TD WIDTH=114 BGCOLOR="#e6e6e6" STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1.10pt double #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P STYLE="margin-bottom: 0in"><A NAME="OLE_LINK25"></A><A NAME="OLE_LINK6"></A><A NAME="OLE_LINK5"></A>
                <BR>
                </P>
                <P STYLE="margin-bottom: 0in">Remove folderid.Error: Reference source not found</P>
                <P><BR>
                </P>
            </TD>
            <TD WIDTH=112 BGCOLOR="#e6e6e6" STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P STYLE="margin-bottom: 0in"><A NAME="OLE_LINK18"></A><A NAME="OLE_LINK17"></A><A NAME="OLE_LINK11"></A><A NAME="OLE_LINK10"></A>
                <BR>
                </P>
                <P>Remove folderid.</P>
            </TD>
            <TD WIDTH=112 BGCOLOR="#e6e6e6" STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P STYLE="margin-bottom: 0in"><BR>
                </P>
                <P>Remove folderid.Error: Reference source not found</P>
            </TD>
            <TD WIDTH=111 STYLE="border: 1px solid #000000; padding: 0in 0.08in">
                <P STYLE="margin-bottom: 0in"><A NAME="OLE_LINK31"></A><A NAME="OLE_LINK30"></A>
                <BR>
                </P>
                <P>Remove folderid.</P>
            </TD>
        </TR>
        <TR VALIGN=TOP>
            <TD WIDTH=113 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P>… without folderid but with <B>siteid</B> (partial
                cross-site)</P>
            </TD>
            <TD WIDTH=114 BGCOLOR="#e6e6e6" STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1.10pt double #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P STYLE="margin-bottom: 0in"><A NAME="OLE_LINK39"></A><A NAME="OLE_LINK38"></A>
                <BR>
                </P>
                <P>Do nothing to relationships.Error: Reference source not found</P>
            </TD>
            <TD WIDTH=112 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P STYLE="margin-bottom: 0in"><A NAME="OLE_LINK35"></A><A NAME="OLE_LINK34"></A>
                <BR>
                </P>
                <P>Do nothing to relationships.</P>
            </TD>
            <TD WIDTH=112 BGCOLOR="#e6e6e6" STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P STYLE="margin-bottom: 0in"><BR>
                </P>
                <P>Do nothing to relationships.Error: Reference source not found</P>
            </TD>
            <TD WIDTH=111 STYLE="border: 1px solid #000000; padding: 0in 0.08in">
                <P STYLE="margin-bottom: 0in"><BR>
                </P>
                <P>Do nothing to relationships.</P>
            </TD>
        </TR>
        <TR VALIGN=TOP>
            <TD WIDTH=113 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P><A NAME="OLE_LINK2"></A><A NAME="OLE_LINK1"></A>… without
                folderid or siteid (normal)</P>
            </TD>
            <TD WIDTH=114 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1.10pt double #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P STYLE="margin-bottom: 0in"><A NAME="OLE_LINK9"></A><A NAME="OLE_LINK8"></A><A NAME="OLE_LINK7"></A>
                <BR>
                </P>
                <P>Do nothing to relationships.<A CLASS="sdfootnoteanc" NAME="sdfootnote2anc" HREF="#sdfootnote2sym"><SUP>1</SUP></A></P>
            </TD>
            <TD WIDTH=112 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P STYLE="margin-bottom: 0in"><BR>
                </P>
                <P>Do nothing to relationships.</P>
            </TD>
            <TD WIDTH=112 STYLE="border-top: 1px solid #000000; border-bottom: 1px solid #000000; border-left: 1px solid #000000; border-right: none; padding-top: 0in; padding-bottom: 0in; padding-left: 0.08in; padding-right: 0in">
                <P STYLE="margin-bottom: 0in"><BR>
                </P>
                <P>Do nothing to relationships.</P>
            </TD>
            <TD WIDTH=111 STYLE="border: 1px solid #000000; padding: 0in 0.08in">
                <P STYLE="margin-bottom: 0in"><BR>
                </P>
                <P>Do nothing to relationships.</P>
            </TD>
        </TR>
    </TBODY>
</TABLE>
 * 
 * @author adamgent
 *
 */
@Category(IntegrationTest.class)
public class PSCrossSiteRemoveTest extends PSCrossSiteTestCase
{

   private String sourcePath;
   private String subPath;
   private  List<PSCoreItem> items;
   protected List<ItemBuilder> builders = new ArrayList<ItemBuilder>();
   
   public void setUp() throws Exception {
      super.setUp();
      sourcePath = "//Sites/CorporateInvestments/Files/RemoveTest";
      subPath = sourcePath + "/" + "sub";
      c.addFolderTree(subPath);
      items = c.createItems("rffBrief", 2);
   }
   
   public void tearDown() throws Exception {
      /*
       * Wait for the touch queue to finish.
       */
      sleep();
      for (ItemBuilder b : builders) {
         b.delete();
      }
      String p = sourcePath;
      deleteFolder(p);
      super.tearDown();
   }

   /*
    * First Column (using folder instead of item)
    */
   public void testRemoveFolderWithDependentInOnlyOneSiteWithSiteIdAndFolderIdSet() throws Exception {
      ItemBuilder owner = new ItemBuilder(items.get(0))
         .fillItem("Owner").save().addToPath("//Sites/EnterpriseInvestments");
      builders.add(owner);
      ItemBuilder dep = new ItemBuilder(items.get(1))
         .fillItem("Dep").save().addToPath(subPath).addToPath("//Folders");
      owner.addDependent(dep.getGuid(), 301, getFolderId(subPath));
      builders.add(dep);
      PSRelationshipFilter f = new PSRelationshipFilter();
      f.setDependentId(dep.getContentId());
      f.setOwnerId(owner.getContentId());
      List<PSRelationship> rs = rservice.findByFilter(f);
      assertRel(rs, 1, 301, getFolderId(subPath));
      
      /*
       * Now remove
       */
      assertTrue("Folder should be removed", removeFolderLikeCX(sourcePath));
      sleep();
      rs = rservice.findByFilter(f);
      assertRel(rs, 1, 301, null);
   }
   
   public void testRemoveFolderWithDepInOneSiteWithFolderIdSet() throws Exception {
      ItemBuilder owner = new ItemBuilder(items.get(0))
         .fillItem("Owner").save().addToPath("//Sites/CorporateInvestments/Files");
      builders.add(owner);
      ItemBuilder dep = new ItemBuilder(items.get(1))
         .fillItem("Dep").save().addToPath(subPath).addToPath("//Folders");
      owner.addDependent(dep.getGuid(), null, getFolderId(subPath));
      builders.add(dep);
      PSRelationshipFilter f = new PSRelationshipFilter();
      f.setDependentId(dep.getContentId());
      f.setOwnerId(owner.getContentId());
      assertRel(f, 1, null, getFolderId(subPath));
      
      /*
       * Now remove
       */
      assertTrue("Folder should be removed", removeFolderLikeCX(sourcePath));
      sleep();
      assertRel(f, 1, null, null);
   }
   
   public void testRemoveFolderWithDepInOneSiteWithSiteIdSet() throws Exception {
      ItemBuilder owner = new ItemBuilder(items.get(0))
         .fillItem("Owner").save().addToPath("//Sites/CorporateInvestments/Files");
      builders.add(owner);
      ItemBuilder dep = new ItemBuilder(items.get(1))
         .fillItem("Dep").save().addToPath(subPath).addToPath("//Folders");
      owner.addDependent(dep.getGuid(), 301, null);
      builders.add(dep);
      PSRelationshipFilter f = new PSRelationshipFilter();
      f.setDependentId(dep.getContentId());
      f.setOwnerId(owner.getContentId());
      assertRel(f, 1, 301, null);
      
      /*
       * Now remove
       */
      assertTrue("Folder should be removed", removeFolderLikeCX(sourcePath));
      sleep();
      assertRel(f, 1, 301, null);
   }
   
   /*
    * Second Column (using item instead of folder)
    * Second Column First Row.
    */
   public void testRemoveDepItemFromOneFolderButInMultipleFoldersInSiteWithSiteIdAndFolderIdSet() throws Exception {
      ItemBuilder owner = new ItemBuilder(items.get(0))
         .fillItem("Owner").save()
         .addToPath("//Sites/CorporateInvestments/Files");
      builders.add(owner);
      ItemBuilder dep = new ItemBuilder(items.get(1))
         .fillItem("Dep").save()
         .addToPath(subPath)
         .addToPath("//Sites/CorporateInvestments/Files");
      owner.addDependent(dep.getGuid(), 301, getFolderId(subPath));
      builders.add(dep);
      PSRelationshipFilter f = new PSRelationshipFilter();
      f.setDependentId(dep.getContentId());
      f.setOwnerId(owner.getContentId());
      assertRel(f, 1, 301, getFolderId(subPath));
      
      /*
       * Now remove
       */
      dep.removeFromPathLikeCX(subPath);
      sleep();
      assertRel(f, 1, 301, null);
   }

}
