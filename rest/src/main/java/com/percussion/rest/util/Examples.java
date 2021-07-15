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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.rest.util;

import com.percussion.rest.Guid;
import com.percussion.rest.LinkRef;
import com.percussion.rest.assets.Asset;
import com.percussion.rest.contentlists.ContentList;
import com.percussion.rest.contexts.Context;
import com.percussion.rest.deliverytypes.DeliveryType;
import com.percussion.rest.editions.Edition;
import com.percussion.rest.folders.Folder;
import com.percussion.rest.folders.SectionInfo;
import com.percussion.rest.folders.SectionLinkRef;
import com.percussion.rest.pages.CalendarInfo;
import com.percussion.rest.pages.CodeInfo;
import com.percussion.rest.pages.Page;
import com.percussion.rest.pages.Region;
import com.percussion.rest.pages.SeoInfo;
import com.percussion.rest.pages.Widget;
import com.percussion.rest.pages.WorkflowInfo;
import com.percussion.rest.roles.Role;
import com.percussion.rest.users.User;
import org.apache.commons.lang3.time.FastDateFormat;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * This class provides example representations that can be used for wadl docs.<br>
 * 
 * @version $Id$
 */
public class Examples
{

	public static final DeliveryType SAMPLE_DELIVERYTYPE = new DeliveryType();
    public static final Edition SAMPLE_EDITION = new Edition();
    public static final Context SAMPLE_CONTEXT = new Context();
    public static final User SAMPLE_USER = new User();
    public static final Role SAMPLE_ROLE = new Role();
    public static final ContentList SAMPLE_CONTENTLIST = new ContentList();
	
    public static final Folder SAMPLE_FOLDER = new Folder();
    public static final Page SAMPLE_PAGE = new Page();
    public static final Asset SAMPLE_ASSET = new Asset();

    static
    {
        SAMPLE_FOLDER.setId("aaaa-aaaa-aaa");
        SAMPLE_FOLDER.setName("subfolder3");
        SAMPLE_FOLDER.setPath("subfolder1/subfolder2");
        SAMPLE_FOLDER.setSiteName("Site1");

        SAMPLE_FOLDER.setWorkflow("default");
        SAMPLE_FOLDER.setAccessLevel(Folder.ACCESS_LEVEL_READ);
        SAMPLE_FOLDER.setEditUsers(new ArrayList<>(Arrays.asList("User1", "User2")));

        SectionInfo info = new SectionInfo();
        info.setDisplayTitle("Section Display Title");
        info.setTemplateName("Template 1");
        LinkRef landingPage = new LinkRef("file1.html", "http://test.com/index.html");

        info.setLandingPage(landingPage);
        info.setNavClass("navclass1");
        info.setTargetWindow("top");
        SAMPLE_FOLDER.setSectionInfo(info);
        List<LinkRef> pages = new ArrayList<>();
        pages.add(landingPage);
        pages.add(new LinkRef("file1.html", "http://test.com/file1.html"));
        pages.add(new LinkRef("file2.html", "http://test.com/file2.html"));
        List<LinkRef> subfolders = new ArrayList<>();
        subfolders.add(new LinkRef("sub1", "http://test.com/file1.html"));
        subfolders.add(new LinkRef("sub2", "http://test.com/file2.html"));

        SAMPLE_FOLDER.setSubfolders(subfolders);

        List<SectionLinkRef> subsections = new ArrayList<>();
        subsections.add(new SectionLinkRef("subsection1", "http://test.com/file1.html", SectionLinkRef.TYPE_EXTERNAL));
        subsections.add(new SectionLinkRef("subsection2", "http://test.com/file2.html", SectionLinkRef.TYPE_INTERNAL));
        subsections.add(new SectionLinkRef("subsection2", "http://test.com/file2.html", SectionLinkRef.TYPE_SUBFOLDER));
       
        SAMPLE_FOLDER.setSubsections(subsections);
        SAMPLE_FOLDER.setSubfolders(subfolders);
        SAMPLE_FOLDER.setPages(pages);
    }

    static
    {
        FastDateFormat dateFormat = FastDateFormat.getInstance("dd-MM-yyyy");
        Date date1 = null;
        Date date2 = null;
        try
        {
            date1 = dateFormat.parse("2010-10-24T04:30:00.000+0000");
            date2 = dateFormat.parse("2010-10-24T04:30:00.000+0000");
        }
        catch (ParseException e)
        {
            throw new RuntimeException("Error creating parsing example date", e);
        }

        SAMPLE_PAGE.setId("id");
 
        SAMPLE_PAGE.setFolderPath("SiteA");
        SAMPLE_PAGE.setFolderPath("folder1/folder2");
        SAMPLE_PAGE.setDisplayName("Display Name");
        SAMPLE_PAGE.setTemplateName("Template1");
        SAMPLE_PAGE.setSummary("Summary");
        SAMPLE_PAGE.setOverridePostDate(date1);
        CalendarInfo calendar = new CalendarInfo();
        calendar.setCalendars(new ArrayList<>(Arrays.asList(new String[]
        {"Caldendar1", "Calendar2"})));
        calendar.setStartDate(date1);
        calendar.setEndDate(date2);
        SAMPLE_PAGE.setCalendar(calendar);
        WorkflowInfo workflow = new WorkflowInfo();
        workflow.setState("Approval");
        workflow.setCheckedOutUser("Admin");
        workflow.setCheckedOut(true);
        SAMPLE_PAGE.setWorkflow(workflow);
        SeoInfo seo = new SeoInfo();
        seo.setBrowserTitle("Browser Title");
        seo.setMetaDescription("Meta Description");
        seo.setCategories(Arrays.asList(new String[]
        {"Category1", "Category2"}));
        seo.setTags(Arrays.asList(new String[]
        {"Tag1", "Tag2"}));
        seo.setHideSearch(false);
        SAMPLE_PAGE.setSeo(seo);
        CodeInfo code = new CodeInfo();
        code.setHead("HeadCode");
        code.setBeforeClose("BeforeCloseCode");
        code.setAfterStart("AfterStartCode");
        SAMPLE_PAGE.setCode(code);
        final Region region = new Region();
        region.setName("region1");

        region.setType("richtext");
        Asset asset1 = new Asset();
        asset1.setFields(new HashMap<String, String>()
        {
            {
                put("Field1", "<a href=\"test\">test<\\a>");
                put("Field2", "<a href=\"test\">test<\\a>");
            }
        });
        Widget widget1 = new Widget();
        widget1.setId("1234");
        widget1.setName("widget1");
        widget1.setEditable(true);
        widget1.setType("widgetType");
        widget1.setAsset(asset1);
        widget1.setScope("local");
        region.setWidgets(new ArrayList<>(Arrays.asList(widget1)));
        SAMPLE_PAGE.setBody(new ArrayList<>(Arrays.asList(region)));

        Guid guid = new Guid();
        guid.setUntypedString("100-100-100");
        SAMPLE_DELIVERYTYPE.setId(guid);
        SAMPLE_DELIVERYTYPE.setBeanName("sys_exampleBean");
        SAMPLE_DELIVERYTYPE.setDescription("Test Description");
        SAMPLE_DELIVERYTYPE.setName("Test Delivery Type");
        
    
    }

}
