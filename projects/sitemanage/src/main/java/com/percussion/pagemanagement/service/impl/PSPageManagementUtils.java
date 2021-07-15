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

package com.percussion.pagemanagement.service.impl;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitemanage.importer.helpers.IPSImportHelper;
import com.percussion.sitemanage.importer.utils.PSManagedTagsUtils;
import com.percussion.utils.types.PSPair;
import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PSPageManagementUtils
{
    private static final String CONTENT_ATTR_NAME = "content";
    private final static String NAME_SEPARATOR = "-";
    public static final String TEMPLATE_NAME = "Template";
    public static final String PAGE_NAME = "Page";
    public static final String UNASSIGNED_WIDGET_NAME = "perc-unassigned.widget";
    /**
     * Takes name and appends count at the end if necessary. Uses a name
     * separator.
     * 
     * @param name The initial name to which a number will be appended.
     * @param count The number that will be appended at the end of the name.
     * @return The new name, consisting of initial name, a separator and a count
     *         modifier. Example: "New_Template-3".
     */
    public static String getNameForCount(String name, int count)
    {
        String nameForCount = name;

        if (count != 0)
        {
            nameForCount += NAME_SEPARATOR + count;
        }

        return nameForCount;
    }

    /**
     * Create a Raw HTML widget item, and its corresponding asset, with the
     * given widget slot id (or widget id). The asset is saved into the system.
     * 
     * @param slotid {@link String} with the widget id. May be blank.
     * @return {@link PSPair}<{@link PSWidgetItem}, {@link PSAsset}> never
     *         <code>null</code>, contains the widget item in the first place,
     *         and the asset in the second place.
     */
    public static PSWidgetItem createRawHtmlWidgetItem(String slotid)
    {
        PSWidgetItem widget = new PSWidgetItem();
        widget.setDefinitionId("percRawHtml");
        widget.setName(PSPageManagementUtils.UNASSIGNED_WIDGET_NAME);
        widget.setId(slotid);
        return widget;
    }

    /**
     * Extracts scripts after body start and before body end from the document
     * body in memory and sets them to afterBodyStart and beforeBodyClose in
     * pageContent. Removes title from the head of imported document in memory
     * and updates headContent in pageContent.
     * 
     * @param pageContent Current page content in memory to process
     * @param logger {@link IPSSiteImportLogger} to log the commented tags.
     *            Assumed not <code>null</code>.
     */
    public static void extractMetadata(PSPageContent pageContent, IPSSiteImportLogger logger)
    {
        String headContent = commentOutManagedTags(pageContent, logger);
        pageContent.setHeadContent(StringEscapeUtils.unescapeHtml(headContent));

        Element docBody = pageContent.getSourceDocument().body();
        Elements bodyElems = docBody.children();

        StringBuilder afterBodyStart = extractAfterBodyStartContent(bodyElems, logger);
        StringBuilder beforeBodyClose = extractBeforeBodyClose(bodyElems, logger);

        pageContent.setAfterBodyStart(StringEscapeUtils.unescapeHtml(afterBodyStart.toString()));
        pageContent.setBeforeBodyClose(StringEscapeUtils.unescapeHtml(beforeBodyClose.toString()));
        pageContent.setBodyContent(StringEscapeUtils.unescapeHtml(docBody.html()));
    }

    /**
     * Extracts the Before Body Close content from the body elements.
     * 
     * @param bodyElems {@link Elements} with the elements that belong to the
     *            body. Assumed not <code>null</code>.
     * @param logger {@link IPSSiteImportLogger} to use. Assumed not
     *            <code>null</code>.
     * @return {@link StringBuilder}, never <code>null</code> but may be empty.
     */
    private static StringBuilder extractBeforeBodyClose(Elements bodyElems, IPSSiteImportLogger logger)
    {
        Elements beforeBodyCloseElems = new Elements();
        for (int i = bodyElems.size(); i > 0; i--)
        {
            Element element = bodyElems.get(i - 1);
            if (!element.tagName().equalsIgnoreCase("script"))
                break;
            beforeBodyCloseElems.add(element);
        }

        StringBuilder beforeBodyClose = new StringBuilder();
        for (int j = beforeBodyCloseElems.size(); j > 0; j--)
        {
            Element element = beforeBodyCloseElems.get(j - 1);

            if (PSManagedTagsUtils.isManagedJSReference(element))
            {
                beforeBodyClose.append(PSManagedTagsUtils.commentTagText(element.outerHtml()));
                logger.appendLogMessage(PSLogEntryType.STATUS, IPSImportHelper.COMMENTED_JS_REFERENCE_FROM_BODY, element.toString());
            }
            else
            {
                beforeBodyClose.append(element.outerHtml());
            }
            element.remove();
        }
        return beforeBodyClose;
    }

    /**
     * Extracts the After Body Start Content from the body elements.
     * 
     * @param bodyElems {@link Elements} with the elements that belong to the
     *            body. Assumed not <code>null</code>.
     * @param logger {@link IPSSiteImportLogger} to use. Assumed not
     *            <code>null</code>.
     * @return {@link StringBuilder}, never <code>null</code> but may be empty.
     */
    private static StringBuilder extractAfterBodyStartContent(Elements bodyElems, IPSSiteImportLogger logger)
    {
        StringBuilder afterBodyStart = new StringBuilder();
        for (Element element : bodyElems)
        {
            if (!element.tagName().equalsIgnoreCase("script"))
                break;

            if (PSManagedTagsUtils.isManagedJSReference(element))
            {
                afterBodyStart.append(PSManagedTagsUtils.commentTagText(element.outerHtml()));
                logger.appendLogMessage(PSLogEntryType.STATUS, IPSImportHelper.COMMENTED_JS_REFERENCE_FROM_BODY, element.toString());
            }
            else
            {
                afterBodyStart.append(element.outerHtml());
            }
            element.remove();

        }
        return afterBodyStart;
    }

    /**
     * Process the header element to comment out those tags that are managed by
     * CM1. See {@link #isManagedMetadataTag(Element)}.
     * 
     * @param pageContent The page content being modified.. Assumed not
     *            <code>null</code>.
     * @param logger {@link IPSSiteImportLogger} to log the commented tags.
     *            Assumed not <code>null</code>.
     * @return {@link String} with the html code from the header element. Never
     *         <code>null</code> but may be empty.
     */
    private static String commentOutManagedTags(PSPageContent pageContent, IPSSiteImportLogger logger)
    {
        Element docHead = pageContent.getSourceDocument().head();
        
        // first comment the title tag
        for (Element title : docHead.select("title"))
        {
            logger.appendLogMessage(PSLogEntryType.STATUS, IPSImportHelper.COMMENTED_OUT_ELEMENT, title.toString());

            // this should only happen once
            PSManagedTagsUtils.commentTag(docHead, title);
        }

        commentMetadataTags(pageContent, docHead, logger);
        commentManagedJSReferences(docHead, logger);
        return docHead.html();
    }

    /**
     * Comments out the managed js referenced from the element passed as
     * parameter.
     * 
     * @param element {@link Element} to comment the references from. Assumed
     *            not <code>null</code>.
     * @param logger {@link IPSSiteImportLogger} to append the message. Assumed
     *            not <code>null</code>.
     */
    private static void commentManagedJSReferences(Element element, IPSSiteImportLogger logger)
    {
        Elements scriptTags = element.select("script");
        for (Element scriptTag : scriptTags)
        {
            if (PSManagedTagsUtils.isManagedJSReference(scriptTag))
            {
                logger.appendLogMessage(PSLogEntryType.STATUS, IPSImportHelper.COMMENTED_JS_REFERENCE_FROM_HEAD, scriptTag.toString());
                PSManagedTagsUtils.commentTag(element, scriptTag);
            }
        }
    }

    /**
     * Comments out the managed metadata tags from the head element.
     * 
     * @param pageContent The page content being modified, assumed not <code>null</code>. 
     * 
     * @param docHead {@link Element} to comment the references from. Assumed
     *            not <code>null</code>.
     * @param logger {@link IPSSiteImportLogger} to append the message. Assumed
     *            not <code>null</code>.
     */
    private static void commentMetadataTags(PSPageContent pageContent, Element docHead, IPSSiteImportLogger logger)
    {
        Elements metaTags = docHead.select("meta");
        for (Element metaTag : metaTags)
        {
            if (PSManagedTagsUtils.isManagedMetadataTag(metaTag))
            {
                if (PSManagedTagsUtils.isDescriptionMetaTag(metaTag))
                    pageContent.setDescription(metaTag.attr(CONTENT_ATTR_NAME));
                
                logger.appendLogMessage(PSLogEntryType.STATUS, IPSImportHelper.COMMENTED_OUT_ELEMENT, metaTag.toString());
                PSManagedTagsUtils.commentTag(docHead, metaTag);
            }
        }
    }
}
