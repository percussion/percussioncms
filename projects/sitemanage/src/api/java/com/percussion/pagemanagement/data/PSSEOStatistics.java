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

import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import org.apache.commons.lang.StringUtils;

/**
 * Encapsulates search engine optimization statistics for a Page.  This includes the issues which have been detected
 * for the Page as well as an indication of the overall severity.
 */
@XmlRootElement(name = "SEOStatistics")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PSSEOStatistics
{

    /**
     *  Safe to serialize
     */
    private static final long serialVersionUID = 3197862964060713693L;

    /**
     * See {@link #getPageSummary()}.
     */
    @NotNull
    private PSPageSummary pageSummary;
    
    /**
     * See {@link #getPath()}.
     */
    @NotNull
    @NotBlank
    private String path;

    /**
     * See {@link #getIssues()}.
     */
    @NotNull
    private Set<SEO_ISSUE> issues = new HashSet<SEO_ISSUE>();
    
    /**
     * See {@link #getSeverity()}.
     */
    private int severity;
    
    /**
     * See {@link #getKeyword()}.
     */
    private String keyword;
        
    /**
     * See {@link #isKeywordPresent()}.
     */
    private boolean keywordPresent;
    
    private String summary;
    
    /**
     * For serialization.
     */
    public PSSEOStatistics()
    {        
    }
    
    /**
     * Constructs a statistics object from a page summary.
     * 
     * @param pageSummary the summary for which seo statistics will be generated, may not be <code>null</code>.
     * @param path the finder path of the page, may not be blank.
     * @param keyword the keyword to use in analysis.
     */
    public PSSEOStatistics(PSPageSummary pageSummary, String path, String keyword)
    {
        notNull(pageSummary);
        notEmpty(path);
        
        setPageSummary(pageSummary);
        setPath(path);
        setKeyword(keyword);
        analyze();        
    }
    
    /**
     * The path represents the finder path of the Page.
     * 
     * @return the path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @param path the finder path to set for the Page.
     */
    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * Get the seo issues discovered for the Page.
     * 
     * @return the issues, never <code>null</code>.
     */
    public Set<SEO_ISSUE> getIssues()
    {
        return issues;
    }

    /**
     * @param issues the seo issues to set for the Page.
     */
    public void setIssues(Set<SEO_ISSUE> issues)
    {
        this.issues = issues;
    }

    /**
     * Percentage of seo issues discovered for the Page based on the issues defined by {@link SEO_ISSUE}.
     * 
     * @return the severity
     */
    public int getSeverity()
    {
        return severity;
    }

    /**
     * @param severity the percentage of seo issues to set for the Page.
     */
    public void setSeverity(int severity)
    {
        this.severity = severity;
    }

    /**
     * @return the summary of the Page
     */
    public PSPageSummary getPageSummary()
    {
        return pageSummary;
    }

    /**
     * @param pageSummary the summary of the Page.
     */
    public void setPageSummary(PSPageSummary pageSummary)
    {
        this.pageSummary = pageSummary;
    }
    
    /**
     * @return the keyword to use in analysis.
     */
    public String getKeyword()
    {
        return keyword;
    }

    /**
     * @param keyword the keyword to use in analysis.
     */
    public void setKeyword(String keyword)
    {
        this.keyword = keyword;
    }

    /**
     * @return <code>true</code> if the keyword is present in the page summary (description, link, title),
     * <code>false</code> otherwise.
     */
    public boolean isKeywordPresent()
    {
        return keywordPresent;
    }

    /**
     * @param keywordPresent <code>true</code> if the keyword is present in the page summary (description, link, title),
     * <code>false</code> otherwise.
     */
    public void setKeywordPresent(boolean keywordPresent)
    {
        this.keywordPresent = keywordPresent;
    }
    
    /**
     * @return the summary
     */
    public String getSummary()
    {
        return summary;
    }

    /**
     * @param summary the summary to set
     */
    public void setSummary(String summary)
    {
        this.summary = summary;
    }
    
    /**
     * @return the seo severity level {@link SEO_SEVERITY} of the Page, never <code>null</code>.
     */
    public SEO_SEVERITY getSeverityLevel()
    {
        if (severity == 100)
        {
            return SEO_SEVERITY.SEVERE;
        }
        else if (severity >= 75)
        {
            return SEO_SEVERITY.HIGH;
        }
        else if (severity >= 50)
        {
            return SEO_SEVERITY.MEDIUM;
        }
        else if (severity >= 25)
        {
            return SEO_SEVERITY.MODERATE;
        }
        else
        {
            return SEO_SEVERITY.ALL;
        }
    }
    
    /**
     * Performs an analysis of the encapsulated Page to determine its seo rating.  Issues are stored and a severity is
     * computed based on the number of issues found versus the total number of possible issues.
     */
    private void analyze()
    {
        if (pageSummary != null)
        {
            analyzeKeyword();
            
            String sDescr = pageSummary.getDescription();
            String sTitle = pageSummary.getTitle(); 
                             
            if (sTitle.equalsIgnoreCase(pageSummary.getLinkTitle()))
            {
                issues.add(SEO_ISSUE.DEFAULT_TITLE);
            }

            if (sTitle.length() > 70)
            {
                issues.add(SEO_ISSUE.TITLE_TOO_LONG);
            }

            
            if (StringUtils.isBlank(sDescr))
            {
                issues.add(SEO_ISSUE.MISSING_DESCRIPTION);
            }
            else
            {
                if (sDescr.length() > 150)
                {
                    issues.add(SEO_ISSUE.DESCRIPTION_TOO_LONG);
                }
            }
        }
                
        double issuesLength = issues.size();
        double total = getTotalIssueCount();
        if (keywordPresent)
        {
            // subtract one possible keyword issue
            total--;
        }
        else
        {
            // subtract all possible keyword issues
            total -= ms_keywordIssues.size();
        }
        severity = (int) Math.round((issuesLength / total) * 100);
    }
    
    /**
     * Calculates the number of possible issues for a Page if it has not been set.  Used in {@link #analyze()} to
     * compute the severity level.
     *  
     * @return the total number of issues which could be encountered for the Page.
     */
    private double getTotalIssueCount()
    {
        if (ms_totalIssueCount == -1)
        {
            int singleWeightIssueCount = 0;
            Iterator<Set<SEO_ISSUE>> iter = ms_singleWeightIssues.iterator();
            while (iter.hasNext())
            {
                singleWeightIssueCount += iter.next().size();
            }
            ms_totalIssueCount = SEO_ISSUE.values().length - singleWeightIssueCount + ms_singleWeightIssues.size();
        }
                
        return ms_totalIssueCount;
    }
    
    /**
     * Analyzes the page summary for the correct usage of the keyword.  Currently checks the page description, link,
     * and title to ensure that if the keyword exists in one of the fields, it exists in all fields, otherwise, an
     * issue is added for each field from which the keyword is missing.  For page links, the keyword is searched exact
     * as well as with spaces replaced with underscores, and dashes.
     */
    private void analyzeKeyword()
    {
        if (pageSummary == null)
        {
            return;
        }
        
        if (StringUtils.isNotBlank(keyword))
        {
            String sDescr = pageSummary.getDescription();
            String sLink = pageSummary.getLinkTitle();
            String sTitle = pageSummary.getTitle(); 
            
            boolean inDescr = containsKeyword(sDescr);
            boolean inLink = containsKeyword(sLink) || containsKeyword(sLink, keyword.replaceAll(" ", "_"), '_') ||
                    containsKeyword(sLink, keyword.replaceAll(" ", "-"), '-');
            boolean inTitle = containsKeyword(sTitle);
            
            if (inDescr || inLink || inTitle)
            {
                keywordPresent = true;
                
                if (inDescr)
                {
                    if (!inLink)
                    {
                        issues.add(SEO_ISSUE.MISSING_KEYWORD_LINK);
                    }
                    
                    if (!inTitle)
                    {
                        issues.add(SEO_ISSUE.MISSING_KEYWORD_TITLE);
                    }
                }
                
                if (inLink)
                {
                    if (!inDescr)
                    {
                        issues.add(SEO_ISSUE.MISSING_KEYWORD_DESCRIPTION);
                    }
                    
                    if (!inTitle)
                    {
                        issues.add(SEO_ISSUE.MISSING_KEYWORD_TITLE);
                    }
                }
                
                if (inTitle)
                {
                    if (!inDescr)
                    {
                        issues.add(SEO_ISSUE.MISSING_KEYWORD_DESCRIPTION);
                    }
                    
                    if (!inLink)
                    {
                        issues.add(SEO_ISSUE.MISSING_KEYWORD_LINK);
                    }
                }
            }
            else
            {
                keywordPresent = false;
            }
        }
    }
    
    /**
     * Convenience method that calls {@link #containsKeyword(String, String, char)} as
     * <code>containsKeyword(str, keyword, ' ')</code>.  
     */
    private boolean containsKeyword(String str)
    {
        return containsKeyword(str, keyword, ' ');
    }
    
    /**
     * Determines if the specified string contains the keyword.  This is a whole word comparison, case-insensitive.
     * 
     * @param str the string to check.
     * @param key the keyword, assumed not blank.
     * @param separator the pad character used to separate the keyword from other words in the string.
     * 
     * @return <code>true</code> if the string contains the keyword, <code>false</code> otherwise.
     */
    private boolean containsKeyword(String str, String key, char separator)
    {
        if (StringUtils.isBlank(str))
        {
            return false;
        }
        
        String lowerStr = str.toLowerCase();
        String lowerKey = key.toLowerCase();
        return (lowerStr.equals(lowerKey) || lowerStr.startsWith(lowerKey + separator)
                || lowerStr.endsWith(separator + lowerKey) || lowerStr.indexOf(separator + lowerKey + separator) != -1);
    }
    
    /**
     * Defines the possible seo issues which could be discovered for a Page.
     */
    public static enum SEO_ISSUE
    {
        /**
         * Unmodified browser title.
         */
        DEFAULT_TITLE,
        
        /**
         * Missing meta tag description.
         */
        MISSING_DESCRIPTION,
        
        /**
         * Browser title is too long.
         */
        TITLE_TOO_LONG,
        
        /**
         * Description is too long.
         */
        DESCRIPTION_TOO_LONG,
        
        /**
         * Missing keyword in meta tag description.
         */
        MISSING_KEYWORD_DESCRIPTION,
        
        /**
         * Missing keyword in page title.
         */
        MISSING_KEYWORD_TITLE,
        
        /**
         * Missing keyword in page link text.
         */
        MISSING_KEYWORD_LINK;
    }
    
    /**
     * Defines the levels of severity of seo non-compliance.
     */
    public static enum SEO_SEVERITY
    {
        /**
         * Indicates a severity >= 0%.
         */
        ALL,
        
        /**
         * Indicates a severity >= 25%.
         */
        MODERATE,
        
        /**
         * Indicates a severity >= 50%.
         */
        MEDIUM,
        
        /**
         * Indicates a severity >= 75%.
         */
        HIGH,
        
        /**
         * Indicates a severity == 100%.
         */
        SEVERE;
    }
   
    /**
     * The set of issues related to the page title which only count as one issue for the Page.
     */
    private static Set<SEO_ISSUE> ms_titleIssues = new HashSet<SEO_ISSUE>();
    
    /**
     * The set of issues related to the page description which only count as one issue for the Page.
     */
    private static Set<SEO_ISSUE> ms_descriptionIssues = new HashSet<SEO_ISSUE>();
    
    /**
     * The set of issues related to keywords.
     */
    private static Set<SEO_ISSUE> ms_keywordIssues = new HashSet<SEO_ISSUE>();
    
    /**
     * The set of sets of issues which are to be counted as a single issue for the Page.
     */
    private static Set<Set<SEO_ISSUE>> ms_singleWeightIssues = new HashSet<Set<SEO_ISSUE>>();
    
    /**
     * See {@link #getTotalIssueCount()}.
     */
    private static double ms_totalIssueCount = -1;
    
    static
    {
        ms_titleIssues.add(SEO_ISSUE.DEFAULT_TITLE);
        ms_titleIssues.add(SEO_ISSUE.TITLE_TOO_LONG);
        ms_descriptionIssues.add(SEO_ISSUE.MISSING_DESCRIPTION);
        ms_descriptionIssues.add(SEO_ISSUE.DESCRIPTION_TOO_LONG);
        ms_keywordIssues.add(SEO_ISSUE.MISSING_KEYWORD_TITLE);
        ms_keywordIssues.add(SEO_ISSUE.MISSING_KEYWORD_DESCRIPTION);
        ms_keywordIssues.add(SEO_ISSUE.MISSING_KEYWORD_LINK);
        
        //ms_singleWeightIssues.add(ms_titleIssues);
        ms_singleWeightIssues.add(ms_descriptionIssues);
    }
  
}
