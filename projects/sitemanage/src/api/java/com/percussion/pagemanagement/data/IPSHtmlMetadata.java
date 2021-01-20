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
package com.percussion.pagemanagement.data;

/**
 * Html Metadata for a Templates and Pages.
 * @author adamgent
 *
 */
public interface IPSHtmlMetadata
{
    /**
     * The custom doc type to be used in the template
     * Eg: <!DOCTYPE html>
     *
     * @author leonardohildt
     * @return never <code>null</code> but maybe empty.
     */
    public PSMetadataDocType getDocType();
    
    /**
     * The custom doc type to be used in the template
     * Eg: <!DOCTYPE html>
     *
     * @author leonardohildt
     * @param docType the doc type of the template
     */
    public void setDocType(PSMetadataDocType docType);
    
    /**
     * The protected region name used to hide content in the delivery published pages
     * Eg: header
     *
     * @author federicoromanelli
     * @return never <code>null</code> but maybe empty.
     */
    public String getProtectedRegion();

    /**
     * The protected region name used to hide content in the delivery published pages
     * Eg: header 
     *
     * @author federicoromanelli
     * @param the name of the protected region.
     */    
    public void setProtectedRegion(String protectedRegion);
    
    /**
     * The text to show when instead of the code in the protected region when user is not logged-in in the delivery
     * Eg: You're not authorized to see this content
     *
     * @author federicoromanelli
     * @return never <code>null</code> but maybe empty.
     */
    public String getProtectedRegionText();
    
    /**
     * The text to show when instead of the code in the protected region when user is not logged-in in the delivery
     * Eg: You're not authorized to see this content
     *
     * @author federicoromanelli
     * @param the text to place instead of the content in the protected region.
     */
    public void setProtectedRegionText(String protectedRegionText);
    
    /**
     * Additional HTML that will go in the &lt;head&gt;&lt;/head&gt;
     * 
     * @return never <code>null</code> but maybe empty.
     */
    public String getAdditionalHeadContent();


    /**
     * Additional HTML that will go in the &lt;head&gt;&lt;/head&gt;
     *
     * @param additionalHeadContent of the Page.
     */
    public void setAdditionalHeadContent(String additionalHeadContent);

    /**
     * Gets the header of the page. The block of text intent to be used within
     * the HTML right after the &lt;body&gt; tag.
     * 
     * @return the header of the page, never <code>null</code> but maybe empty.
     */
    public String getAfterBodyStartContent();

    /**
     * Sets the header of the page.
     * 
     * @param header the new header of the page, may be <code>null</code> or
     *            empty.
     */
    public void setAfterBodyStartContent(String header);

    /**
     * Gets the footer of the page. This is a block of text intent to be used
     * (or placed) right before the &lt;/body&lt; tag in an HTML page.
     * 
     * @return the footer of the page, never <code>null</code> but maybe empty.
     */
    public String getBeforeBodyCloseContent();
    /**
     * Sets the footer of the page.
     * 
     * @param footer the new footer of the page, may be <code>null</code> or
     *            empty.
     */
    public void setBeforeBodyCloseContent(String footer);
    
    /**
     * Sets the description
     * 
     * @param description may be <code>null<code/> or empty.
     */
    public void setDescription(String description);
}

