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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.delivery;

import com.percussion.comments.data.PSComment;
import com.percussion.comments.data.PSCommentModeration;
import com.percussion.comments.data.PSSiteComments;
import com.percussion.comments.service.IPSCommentsService;
import com.percussion.comments.service.impl.PSCommentsService;
import com.percussion.delivery.data.PSDeliveryInfo;
import com.percussion.delivery.data.DeliveryServicesContent.CommentService;
import com.percussion.delivery.data.DeliveryServicesContent.CommentService.Comments.Comment;
import com.percussion.delivery.service.IPSDeliveryInfoService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.sitemanage.dao.IPSiteDao;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.WebApplicationException;

import com.percussion.utils.testing.IntegrationTest;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * @author miltonpividori
 *
 */
@RunWith(JMock.class)
@Ignore("There are no Tomcat running in the continuos machine. If you want to " +
        "run these unit tests, adjust the SERVER_URL constant and start your Tomcat " +
        "server. Be sure build and install the latest delivery services code.")
@Category(IntegrationTest.class)
public class PSDeliveryContentGeneratorTest
{
    private static final String SERVER_URL = "http://localhost:9970";
    private static final String SECURE_SERVER_URL = "https://localhost:8443";
    //Replace with admin user here to test. Left blank for security reasons.
    private static final String SECURE_ADMIN_USER = "";
    //Replace with admin user password here to test. Left blank for security reasons.
    private static final String SECURE_ADMIN_PASSWORD = "";

    private static final String SITE1 = "Site1";
    private static final String PAGEPATH1 = "folder/page";

    private static final String SITE2 = "Site2";
    private static final String PAGEPATH2 = "folder/page2";

    private static final String INVALID_XML_FILE = "data/invalidCommentsXml.xml";
    private static final String VALID_XML_FILE = "data/validCommentsXml.xml";
    private static final String VALID_XML_FILE_COUNT_EQUALS_TO_ONE = "data/validCommentsXml_CountEqualsToOne.xml";
    private static final String VALID_XML_FILE_COUNT_LESS_THAN_ONE = "data/validCommentsXml_CountLessThanOne.xml";
    private static final String VALID_XML_FILE_MULTIPLE_COMMENTS_NODES = "data/validCommentsXml_MultipleComments.xml";
    private static final String VALID_XML_FILE_MEMBERSHIPS = "data/membershipAccountsXml.xml";
    private static final String LICENSE_ID="1";

    /**
     * Contains comments from the same site and pagepath present in VALID_XML_FILE.
     */
    private static final String VALID_XML_FILE2 = "data/validCommentsXml2.xml";

    private Mockery context = new JUnit4Mockery();

    private IPSCommentsService commentService;

    private Comment[] expectedValidComments = new Comment[] {
            createComment("Comment 1",
                    "Comment 1 - Email",
                    "Comment 1 - Body",
                    "Comment 1 - Username",
                    "Comment 1 - Url"),
            createComment("Comment 2",
                    "Comment 2 - Email",
                    "Comment 2 - Body",
                    "Comment 2 - Username",
                    "Comment 2 - Url"),
            createComment("Comment 3",
                    "Comment 3 - Email",
                    "Comment 3 - Body",
                    "Comment 3 - Username",
                    "Comment 3 - Url")
    };

    private PSDeliveryContentGenerator contentGenerator;

    private Pattern pattern = Pattern.compile("Copy ([0-9]+)");

    public PSDeliveryContentGeneratorTest()
    {
        IPSDeliveryInfoService deliveryInfoService =
                new IPSDeliveryInfoService()
                {

                    @Override
                    public List<PSDeliveryInfo> findAll()
                    {
                        PSDeliveryInfo ds = new PSDeliveryInfo(SERVER_URL);
                        ds.setUsername(SECURE_ADMIN_USER);
                        ds.setPassword(SECURE_ADMIN_PASSWORD);
                        ds.setAdminUrl(SECURE_SERVER_URL);
                        ds.setAllowSelfSignedCertificate(true);

                        ArrayList<PSDeliveryInfo> list = new ArrayList<PSDeliveryInfo>();
                        list.add(ds);

                        return list;
                    }

                    @Override
                    public PSDeliveryInfo findByService(String service)
                    {
                        List<PSDeliveryInfo> servers = findAll();
                        return servers.isEmpty() ? null : servers.get(0);
                    }

                    @Override
                    public String findBaseByServerType(String type) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public PSDeliveryInfo findByService(String service, String type) {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    @Override
                    public PSDeliveryInfo findByService(String service, String type, String adminURL) {
                        return null;
                    }

                    @Override
                    public PSDeliveryInfo findByURL(String arg0) throws MalformedURLException
                    {
                        // TODO Auto-generated method stub
                        return null;
                    }
                };

        commentService = new PSCommentsService(deliveryInfoService, context.mock(IPSPageService.class),
                context.mock(IPSFolderHelper.class), context.mock(IPSiteDao.class));
    }

    private Comment createComment(String title, String email, String body, String username, String url)
    {
        Comment comment = new Comment();

        comment.setTitle(title);
        comment.setEmail(email);
        comment.setBody(body);
        comment.setUsername(username);
        comment.setUrl(url);

        return comment;
    }

    private void assertIsExpectedComment(Comment actualComment, Comment[] expectedComments)
    {
        boolean expectedCommentFound = false;

        for (Comment anExpectedComment : expectedComments)
        {
            if (actualComment.getTitle().equals(anExpectedComment.getTitle()))
            {
                assertEquals("comment email", anExpectedComment.getEmail(), actualComment.getEmail());
                assertEquals("comment body", anExpectedComment.getBody(), actualComment.getBody());
                assertEquals("comment username", anExpectedComment.getUsername(), actualComment.getUsername());
                assertEquals("comment url", anExpectedComment.getUrl(), actualComment.getUrl());

                expectedCommentFound = true;
            }
        }

        if (!expectedCommentFound)
            fail("invalid comment title");
    }

    private List<PSComment> getAllComments()
    {
        List<PSComment> allComments = new ArrayList<PSComment>();

        try
        {
            List<PSComment> temp;

            temp = commentService.getCommentsOnPage(SITE1, PAGEPATH1, 0, 0);
            if (temp != null)
                allComments.addAll(temp);

            try
            {
                temp = commentService.getCommentsOnPage(SITE2, PAGEPATH2, 0, 0);
                if (temp != null)
                    allComments.addAll(temp);
            }
            catch (Exception ex)
            {

            }

            return allComments;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private List<PSComment> getAllComments(String site, String pagepath)
    {
        try
        {
            return commentService.getCommentsOnPage(site, pagepath, 0, 0);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private void deleteAllCommentsInRemoteServer()
    {
        List<PSComment> comments = null;
        try
        {
            comments = getAllComments();
        }
        catch (WebApplicationException ex)
        {
            return;
        }

        if (comments == null)
            return;

        PSCommentModeration moderation = new PSCommentModeration();
        String sitename="";
        for (PSComment com : comments)
        {
            PSSiteComments siteComments = new PSSiteComments();
            sitename= com.getSiteName();
           siteComments.setSite(sitename);
            siteComments.getComments().add(com.getCommentId());

            moderation.getDeletes().add(siteComments);
        }

        commentService.moderate(sitename,moderation);
    }

    private String getPlainCommentTitle(String commentTitle)
    {
        return commentTitle.substring(0, commentTitle.length() - getCopyN(commentTitle).length());
    }

    private String getCopyN(String commentTitle)
    {
        Matcher matcher = pattern.matcher(commentTitle);

        if (!matcher.find())
            return StringUtils.EMPTY;

        Integer copy = Integer.parseInt(matcher.group(1));

        return " Copy " + copy;
    }

    private InputStream getDataFile(String filePath) throws FileNotFoundException
    {
        return getClass().getResourceAsStream(filePath);
    }

    @Before
    public void setUp()
    {
        deleteAllCommentsInRemoteServer();
    }

    @Test
    public void testContentGenerator_LoadsXmlFile_XmlIsValid() throws Exception
    {
        InputStream xmlFile = getDataFile(VALID_XML_FILE);
        contentGenerator = new PSDeliveryContentGenerator(SERVER_URL, xmlFile);
        contentGenerator.cleanup();

        assertTrue("data is valid", contentGenerator.dataSuccessfullyLoaded());

        CommentService commentsService = contentGenerator.getRootData().getCommentService();
        assertNotNull("comment service not null", commentsService);
        assertNotNull("comments element not null", commentsService.getComments());
        assertEquals("comments element size", 1, commentsService.getComments().size());
        assertEquals("comments count", 3, commentsService.getComments().get(0).getComment().size());

        for (Comment com : commentsService.getComments().get(0).getComment())
        {
            if (StringUtils.isBlank(com.getTitle()))
                fail("Comment's title must not be null");

            assertIsExpectedComment(com, expectedValidComments);
        }
    }

    @Test
    public void testContentGenerator_LoadsXmlFile_XmlIsInvalid() throws Exception
    {
        InputStream xmlFile = getDataFile(INVALID_XML_FILE);
        contentGenerator = new PSDeliveryContentGenerator(SERVER_URL, xmlFile);

        // Try to generate something, so you
        contentGenerator.generateContent();

        assertFalse("data is invalid", contentGenerator.dataSuccessfullyLoaded());
    }

    @Test
    @Ignore
    public void testContentGenerator_AddComments_CountGreaterThan1() throws Exception
    {
        int count = 10;

        InputStream xmlFile = getDataFile(VALID_XML_FILE);
        contentGenerator = new PSDeliveryContentGenerator(SERVER_URL, xmlFile);

        contentGenerator.generateContent();

        // Check that the comments were added in the delivery server
        List<PSComment> allComments = getAllComments();

        assertNotNull("comments not null", allComments);
        assertEquals("comment count", count * 3, allComments.size());

        List<String> expectedTitles = new ArrayList<String>();
        for (int i=1; i<=count; i++)
        {
            expectedTitles.add("Comment 1 Copy " + i);
            expectedTitles.add("Comment 2 Copy " + i);
            expectedTitles.add("Comment 3 Copy " + i);
        }

        for (int i=0; i<allComments.size(); i++)
        {
            PSComment com = allComments.get(i);
            String plainCommentTitle = getPlainCommentTitle(com.getCommentTitle());

            assertEquals("comment site", SITE1, com.getSiteName());
            assertEquals("comment pagepath", "/" + PAGEPATH1, com.getPagePath());

            assertTrue("comment - expected title, current: " + com.getCommentTitle(),
                    expectedTitles.contains(com.getCommentTitle()));

            String copyN = getCopyN(com.getCommentTitle());
            assertEquals("comment body", plainCommentTitle + " - Body" + copyN, com.getCommentText());
            assertEquals("comment username", plainCommentTitle + " - Username" + copyN, com.getUserName());
            assertEquals("comment email", plainCommentTitle + " - Email" + copyN, com.getUserEmail());
            assertEquals("comment url", plainCommentTitle + " - Url" + copyN, com.getUserLinkUrl());

            // The current comment is no longer expected
            expectedTitles.remove(com.getCommentTitle());
        }

        assertEquals("pending expected titles should be zero", 0, expectedTitles.size());
    }

    @Test
    @Ignore
    public void testContentGenerator_AddComments_CountEqualsThan1() throws Exception
    {
        InputStream xmlFile = getDataFile(VALID_XML_FILE_COUNT_EQUALS_TO_ONE);
        contentGenerator = new PSDeliveryContentGenerator(SERVER_URL, xmlFile);

        contentGenerator.generateContent();

        // Check that the comments were added in the delivery server
        List<PSComment> allComments = getAllComments();

        assertNotNull("comments not null", allComments);
        assertEquals("comment count", 3, allComments.size());

        List<String> expectedTitles = new ArrayList<String>();
        expectedTitles.add("Comment 1");
        expectedTitles.add("Comment 2");
        expectedTitles.add("Comment 3");

        for (int i=0; i<allComments.size(); i++)
        {
            PSComment com = allComments.get(i);
            String plainCommentTitle = getPlainCommentTitle(com.getCommentTitle());

            assertEquals("comment site", SITE1, com.getSiteName());
            assertEquals("comment pagepath", "/" + PAGEPATH1, com.getPagePath());

            assertTrue("comment - expected title, current: " + com.getCommentTitle(),
                    expectedTitles.contains(com.getCommentTitle()));

            String copyN = StringUtils.EMPTY; // Don't add a "Copy N", as the count is equals to 1
            assertEquals("comment body", plainCommentTitle + " - Body" + copyN, com.getCommentText());
            assertEquals("comment username", plainCommentTitle + " - Username" + copyN, com.getUserName());
            assertEquals("comment email", plainCommentTitle + " - Email" + copyN, com.getUserEmail());
            assertEquals("comment url", plainCommentTitle + " - Url" + copyN, com.getUserLinkUrl());

            // The current comment is no longer expected
            expectedTitles.remove(com.getCommentTitle());
        }

        assertEquals("pending expected titles should be zero", 0, expectedTitles.size());
    }

    @Test
    @Ignore
    public void testContentGenerator_AddComments_CountLessThan1() throws Exception
    {
        InputStream xmlFile = getDataFile(VALID_XML_FILE_COUNT_LESS_THAN_ONE);
        contentGenerator = new PSDeliveryContentGenerator(SERVER_URL, xmlFile);

        contentGenerator.generateContent();

        // Check that the comments were added in the delivery server
        List<PSComment> allComments = getAllComments();

        assertNotNull("comments not null", allComments);
        assertEquals("comment count", 3, allComments.size());

        List<String> expectedTitles = new ArrayList<String>();
        expectedTitles.add("Comment 1 Copy 1");
        expectedTitles.add("Comment 2 Copy 1");
        expectedTitles.add("Comment 3 Copy 1");

        for (int i=0; i<allComments.size(); i++)
        {
            PSComment com = allComments.get(i);
            String plainCommentTitle = getPlainCommentTitle(com.getCommentTitle());

            assertEquals("comment site", SITE1, com.getSiteName());
            assertEquals("comment pagepath", "/" + PAGEPATH1, com.getPagePath());

            assertTrue("comment - expected title, current: " + com.getCommentTitle(),
                    expectedTitles.contains(com.getCommentTitle()));

            String copyN = " Copy 1";
            assertEquals("comment body", plainCommentTitle + " - Body" + copyN, com.getCommentText());
            assertEquals("comment username", plainCommentTitle + " - Username" + copyN, com.getUserName());
            assertEquals("comment email", plainCommentTitle + " - Email" + copyN, com.getUserEmail());
            assertEquals("comment url", plainCommentTitle + " - Url" + copyN, com.getUserLinkUrl());

            // The current comment is no longer expected
            expectedTitles.remove(com.getCommentTitle());
        }

        assertEquals("pending expected titles should be zero", 0, expectedTitles.size());
    }

    @Test
    @Ignore
    public void testContentGenerator_AddComments_MultipleCommentsNodes() throws Exception
    {
        InputStream xmlFile = getDataFile(VALID_XML_FILE_MULTIPLE_COMMENTS_NODES);
        contentGenerator = new PSDeliveryContentGenerator(SERVER_URL, xmlFile);

        contentGenerator.generateContent();

        /*
         * Site1 comments
         */

        // Check that the comments were added in the delivery server
        List<PSComment> allComments = getAllComments(SITE1, PAGEPATH1);

        assertNotNull("comments not null", allComments);
        assertEquals("comment count", 3, allComments.size());

        List<String> expectedTitles = new ArrayList<String>();
        expectedTitles.add("MultiComments Comment 1");
        expectedTitles.add("MultiComments Comment 2");
        expectedTitles.add("MultiComments Comment 3");

        for (int i=0; i<allComments.size(); i++)
        {
            PSComment com = allComments.get(i);
            String plainCommentTitle = getPlainCommentTitle(com.getCommentTitle());

            assertEquals("comment site", SITE1, com.getSiteName());
            assertEquals("comment pagepath", "/" + PAGEPATH1, com.getPagePath());

            assertTrue("comment - expected title, current: " + com.getCommentTitle(),
                    expectedTitles.contains(com.getCommentTitle()));

            String copyN = StringUtils.EMPTY; // Don't add a "Copy N", as the count is equals to 1
            assertEquals("comment body", plainCommentTitle + " - Body" + copyN, com.getCommentText());
            assertEquals("comment username", plainCommentTitle + " - Username" + copyN, com.getUserName());
            assertEquals("comment email", plainCommentTitle + " - Email" + copyN, com.getUserEmail());
            assertEquals("comment url", plainCommentTitle + " - Url" + copyN, com.getUserLinkUrl());

            // The current comment is no longer expected
            expectedTitles.remove(com.getCommentTitle());
        }

        assertEquals("pending expected titles should be zero", 0, expectedTitles.size());



        /*
         * Site2 comments
         */

        // Check that the comments were added in the delivery server
        int count = 5;

        allComments = getAllComments(SITE2, PAGEPATH2);

        assertNotNull("comments not null", allComments);
        assertEquals("comment count", 3 * count, allComments.size());

        expectedTitles = new ArrayList<String>();
        for (int i=1; i<=count; i++)
        {
            expectedTitles.add("MultiComments Comment 4 Copy " + i);
            expectedTitles.add("MultiComments Comment 5 Copy " + i);
            expectedTitles.add("MultiComments Comment 6 Copy " + i);
        }

        for (int i=0; i<allComments.size(); i++)
        {
            PSComment com = allComments.get(i);
            String plainCommentTitle = getPlainCommentTitle(com.getCommentTitle());

            assertEquals("comment site", SITE2, com.getSiteName());
            assertEquals("comment pagepath", "/" + PAGEPATH2, com.getPagePath());

            assertTrue("comment - expected title, current: " + com.getCommentTitle(),
                    expectedTitles.contains(com.getCommentTitle()));

            String copyN = getCopyN(com.getCommentTitle());
            assertEquals("comment body", plainCommentTitle + " - Body" + copyN, com.getCommentText());
            assertEquals("comment username", plainCommentTitle + " - Username" + copyN, com.getUserName());
            assertEquals("comment email", plainCommentTitle + " - Email" + copyN, com.getUserEmail());
            assertEquals("comment url", plainCommentTitle + " - Url" + copyN, com.getUserLinkUrl());

            // The current comment is no longer expected
            expectedTitles.remove(com.getCommentTitle());
        }

        assertEquals("pending expected titles should be zero", 0, expectedTitles.size());
    }

    @Test
    @Ignore
    public void testContentGenerator_Cleanup() throws Exception
    {
        // Generate some comments from different XML files.
        InputStream xmlFile = getDataFile(VALID_XML_FILE);
        contentGenerator = new PSDeliveryContentGenerator(SERVER_URL, xmlFile);

        contentGenerator.generateContent();

        List<PSComment> allComments = getAllComments();

        assertNotNull("comments not null", allComments);
        assertEquals("comment count", 30, allComments.size());

        // Another XML file. This one has comments for the same site and
        // page path than VALID_XML_FILE. In this way I can test that only
        // comments in there are removed.
        xmlFile = getDataFile(VALID_XML_FILE2);
        contentGenerator = new PSDeliveryContentGenerator(SERVER_URL, xmlFile);

        contentGenerator.generateContent();

        allComments = getAllComments();

        assertNotNull("comments not null", allComments);
        assertEquals("comment count", 40, allComments.size()); //  30 + 10 = 40

        // Remove comments from VALID_XML_FILE
        xmlFile = getDataFile(VALID_XML_FILE);
        contentGenerator = new PSDeliveryContentGenerator(SERVER_URL, SECURE_SERVER_URL, true, xmlFile, SECURE_ADMIN_USER, SECURE_ADMIN_PASSWORD,LICENSE_ID );

        contentGenerator.cleanup();

        allComments = getAllComments();

        assertNotNull("comments not null", allComments);
        assertEquals("comment count", 10, allComments.size());

        List<String> expectedTitles = new ArrayList<String>();
        for (int i=1; i<=5; i++)
        {
            expectedTitles.add("First comment Copy " + i);
            expectedTitles.add("Second comment Copy " + i);
        }

        for (int i=0; i<allComments.size(); i++)
        {
            PSComment com = allComments.get(i);
            String plainCommentTitle = getPlainCommentTitle(com.getCommentTitle());

            assertEquals("comment site", SITE1, com.getSiteName());
            assertEquals("comment pagepath", "/" + PAGEPATH1, com.getPagePath());

            assertTrue("comment - expected title, current: " + com.getCommentTitle(),
                    expectedTitles.contains(com.getCommentTitle()));

            String copyN = getCopyN(com.getCommentTitle());
            assertEquals("comment body", plainCommentTitle + " - Body" + copyN, com.getCommentText());
            assertEquals("comment username", plainCommentTitle + " - Username" + copyN, com.getUserName());
            assertEquals("comment email", plainCommentTitle + " - Email" + copyN, com.getUserEmail());
            assertEquals("comment url", plainCommentTitle + " - Url" + copyN, com.getUserLinkUrl());

            // The current comment is no longer expected
            expectedTitles.remove(com.getCommentTitle());
        }

        assertEquals("pending expected titles should be zero", 0, expectedTitles.size());
    }

    @Test
    @Ignore
    public void testContentGenerator_Cleanup_MultipleCommentsNodes() throws Exception
    {
        // Generate some comments from different XML files.
        InputStream xmlFile = getDataFile(VALID_XML_FILE);
        contentGenerator = new PSDeliveryContentGenerator(SERVER_URL, SECURE_SERVER_URL, true, xmlFile, SECURE_ADMIN_USER, SECURE_ADMIN_PASSWORD, LICENSE_ID);

        contentGenerator.generateContent();

        List<PSComment> allComments = getAllComments();

        assertNotNull("comments not null", allComments);
        assertEquals("comment count", 30, allComments.size());

        // Generate comments for several sites with only one XML file.
        xmlFile = getDataFile(VALID_XML_FILE_MULTIPLE_COMMENTS_NODES);
        contentGenerator = new PSDeliveryContentGenerator(SERVER_URL, SECURE_SERVER_URL, true, xmlFile, SECURE_ADMIN_USER, SECURE_ADMIN_PASSWORD, LICENSE_ID);

        contentGenerator.generateContent();

        allComments = getAllComments();

        assertNotNull("comments not null", allComments);
        assertEquals("comment count", 48, allComments.size()); //  30 + 18 = 48

        // Remove comments from VALID_XML_FILE_MULTIPLE_COMMENTS_NODES
        xmlFile = getDataFile(VALID_XML_FILE_MULTIPLE_COMMENTS_NODES);
        contentGenerator = new PSDeliveryContentGenerator(SERVER_URL, SECURE_SERVER_URL, true, xmlFile, SECURE_ADMIN_USER, SECURE_ADMIN_PASSWORD, LICENSE_ID);
        contentGenerator.cleanup();

        allComments = getAllComments();

        assertNotNull("comments not null", allComments);
        assertEquals("comment count", 30, allComments.size());

        List<String> expectedTitles = new ArrayList<String>();
        for (int i=1; i<=10; i++)
        {
            expectedTitles.add("Comment 1 Copy " + i);
            expectedTitles.add("Comment 2 Copy " + i);
            expectedTitles.add("Comment 3 Copy " + i);
        }

        for (int i=0; i<allComments.size(); i++)
        {
            PSComment com = allComments.get(i);
            String plainCommentTitle = getPlainCommentTitle(com.getCommentTitle());

            assertEquals("comment site", SITE1, com.getSiteName());
            assertEquals("comment pagepath", "/" + PAGEPATH1, com.getPagePath());

            assertTrue("comment - expected title, current: " + com.getCommentTitle(),
                    expectedTitles.contains(com.getCommentTitle()));

            String copyN = getCopyN(com.getCommentTitle());
            assertEquals("comment body", plainCommentTitle + " - Body" + copyN, com.getCommentText());
            assertEquals("comment username", plainCommentTitle + " - Username" + copyN, com.getUserName());
            assertEquals("comment email", plainCommentTitle + " - Email" + copyN, com.getUserEmail());
            assertEquals("comment url", plainCommentTitle + " - Url" + copyN, com.getUserLinkUrl());

            // The current comment is no longer expected
            expectedTitles.remove(com.getCommentTitle());
        }

        assertEquals("pending expected titles should be zero", 0, expectedTitles.size());
    }

    @Test
    public void testCreateMemberships() throws Exception
    {
        InputStream xmlFile = getDataFile(VALID_XML_FILE_MEMBERSHIPS);
        contentGenerator = new PSDeliveryContentGenerator(SERVER_URL, SECURE_SERVER_URL, true, xmlFile, SECURE_ADMIN_USER, SECURE_ADMIN_PASSWORD, LICENSE_ID);
        contentGenerator.cleanup();
        contentGenerator.cleanupMemberships();
        contentGenerator.generateMembershipAccounts();
        contentGenerator.cleanupMemberships();
    }

}
