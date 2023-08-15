package com.percussion.pagemanagement.data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PSTemplateTest {

    private static final PSMetadataDocType DOC_TYPE = new PSMetadataDocType();
    private static final String HTML_HEADER = "html-header" ;
    private static final String PROTECTED_REGION = "protected-region";
    private static final String ID = "12345";
    private static final String SERVER_VERSION = "8.1.3";
    private static final String DESCRIPTION = "description";
    private static final String THEME = "theme";
    private static final String MIGRATION_VERSION = "1";
    private static final String PROTECTED_REGION_TEXT = "region-text";
    private static final String TYPE = "template";
    private static final String THUMB_PATH = "/images";
    private static final String LABEL = "label";
    private static final String NAME = "name";

    private PSTemplate testTemplate;


    @Test
    public void testCopyConstructor(){

        PSTemplate copy = new PSTemplate(testTemplate);

        //They different objects, but values should be the same.
        assertEquals(copy.getAdditionalHeadContent(),testTemplate.getAdditionalHeadContent());
        assertEquals(copy.getAfterBodyStartContent(),testTemplate.getAfterBodyStartContent());
        assertEquals(copy.getBodyMarkup(),testTemplate.getBodyMarkup());
        assertEquals(copy.getCssRegion(),testTemplate.getCssRegion());
        assertEquals(copy.getBeforeBodyCloseContent(),testTemplate.getBeforeBodyCloseContent());
        assertEquals(copy.getCssOverride(),testTemplate.getCssOverride());
        assertEquals(copy.getDocType(),testTemplate.getDocType());
        assertEquals(copy.getHtmlHeader(),testTemplate.getHtmlHeader());
        assertEquals(copy.getServerVersion(),testTemplate.getServerVersion());
        assertEquals(copy.getSourceTemplateName(), testTemplate.getSourceTemplateName());
        assertEquals(copy.getDescription(), testTemplate.getDescription());
        assertEquals(copy.getType(), testTemplate.getType());
        assertEquals(copy.getLabel(), testTemplate.getLabel());
        assertEquals(copy.getId(), testTemplate.getId());
        assertEquals(copy.getImageThumbPath(), testTemplate.getImageThumbPath());
        assertEquals(copy.getWidgets(), testTemplate.getWidgets());
        assertEquals(copy.getProtectedRegion(),testTemplate.getProtectedRegion());
        assertEquals(copy.getProtectedRegionText(), testTemplate.getProtectedRegionText());
        assertEquals(copy.getTheme(),testTemplate.getTheme());

        //Now check actual values to make sure no fields values are missing
        assertEquals(ADDITIONAL_HEAD, copy.getAdditionalHeadContent());
        assertEquals(AFTER_BODY_START,copy.getAfterBodyStartContent());
        assertEquals(BODY_MARKUP, copy.getBodyMarkup());
        assertEquals(CSS_REGION,copy.getCssRegion());
        assertEquals(BEFORE_BODY_CLOSE, copy.getBeforeBodyCloseContent());
        assertEquals(CSS_OVERRIDE, copy.getCssOverride());
        assertEquals(DOC_TYPE, copy.getDocType());
        assertEquals(HTML_HEADER, copy.getHtmlHeader());
        assertEquals(SERVER_VERSION,copy.getServerVersion());
        assertEquals(SOURCE_TEMPLATE,copy.getSourceTemplateName());
        assertEquals(DESCRIPTION,copy.getDescription());
        assertEquals(TYPE,copy.getType());
        assertEquals(LABEL, copy.getLabel());
        assertEquals(ID, copy.getId());
        assertEquals(THUMB_PATH,copy.getImageThumbPath());
        assertEquals(copy.getWidgets(), testTemplate.getWidgets());
        assertEquals(PROTECTED_REGION,copy.getProtectedRegion());
        assertEquals(PROTECTED_REGION_TEXT,copy.getProtectedRegionText());
        assertEquals(THEME,copy.getTheme());


    }

    @Before
    public void setUp() throws Exception {
        testTemplate = new PSTemplate();
        testTemplate.setSourceTemplateName(SOURCE_TEMPLATE);
        testTemplate.setAdditionalHeadContent(ADDITIONAL_HEAD);
        testTemplate.setAfterBodyStartContent(AFTER_BODY_START);
        testTemplate.setBodyMarkup(BODY_MARKUP);
        testTemplate.setCssOverride(CSS_OVERRIDE);
        testTemplate.setCssRegion(CSS_REGION);
        testTemplate.setDocType(DOC_TYPE);
        testTemplate.setBeforeBodyCloseContent(BEFORE_BODY_CLOSE);
        testTemplate.setHtmlHeader(HTML_HEADER);
        testTemplate.setProtectedRegion(PROTECTED_REGION);
        testTemplate.setProtectedRegionText(PROTECTED_REGION_TEXT);
        testTemplate.setRegionTree( new PSRegionTree());
        testTemplate.setServerVersion(SERVER_VERSION);
        testTemplate.setTheme(THEME);
        testTemplate.setContentMigrationVersion(MIGRATION_VERSION);
        testTemplate.setDescription(DESCRIPTION);
        testTemplate.setId(ID);
        testTemplate.setType(TYPE);
        testTemplate.setImageThumbPath(THUMB_PATH);
        testTemplate.setLabel(LABEL);
        testTemplate.setName(NAME);
    }

    private static final String SOURCE_TEMPLATE="base.plain";
    private static final String ADDITIONAL_HEAD="<script>alert('head');</script>";
    private static final String AFTER_BODY_START="<script>alert('body start');</script>";
    private static final String BODY_MARKUP="<script>alert('body');</script>";
    private static final String BEFORE_BODY_CLOSE="<script>alert('body close');</script>";
    private static final String CSS_OVERRIDE="css-override";
    private static final String CSS_REGION="css-region";



    @After
    public void tearDown() throws Exception {
    }
}