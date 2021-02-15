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

package com.percussion.theme.service.impl;

import static com.percussion.theme.data.PSRegionCSS.REGION_CLASS;
import static com.percussion.utils.tools.IPSUtilsConstants.RX_STANDARD_ENC;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionTree;
import com.percussion.share.service.IPSDataService.PSThemeNotFoundException;
import com.percussion.theme.data.PSRegionCSS;
import com.phloc.css.ECSSVersion;
import com.phloc.css.decl.CSSDeclaration;
import com.phloc.css.decl.CSSSelector;
import com.phloc.css.decl.CSSSelectorSimpleMember;
import com.phloc.css.decl.CSSStyleRule;
import com.phloc.css.decl.CascadingStyleSheet;
import com.phloc.css.decl.ECSSSelectorCombinator;
import com.phloc.css.decl.ICSSSelectorMember;
import com.phloc.css.reader.CSSReader;
import com.phloc.css.writer.CSSWriterSettings;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PSRegionCSSFileService
{
    /**
     * Finds the specified region CSS from a file. 
     * @param outerRegion the most outer region name of the specified region, not blank.
     * @param region the region name, not blank.
     * @param filePath the file path, not blank.
     * @return the specified region CSS. It may be <code>null</code>.
     */
    public PSRegionCSS findRegionCSS(String outerRegion, String region, String filePath) throws PSThemeNotFoundException {
        notEmpty(outerRegion);
        notEmpty(region);
        notEmpty(filePath);
        
        List<PSRegionCSS> regions = read(filePath);
        return findRegionCSS(outerRegion, region, regions);
    }

    private PSRegionCSS findRegionCSS(String outerRegion, String region, List<PSRegionCSS> regions)
    {
        for (PSRegionCSS r : regions)
        {
            if (StringUtils.equals(region, r.getRegionName()) &&
                StringUtils.equals(outerRegion, r.getOuterRegionName()))
            {
                return r;
            }
        }
        return null;
    }
    
    /**
     * Saves the specific region CSS into the specified file.
     * The region CSS will be added into the file if not exist;
     * otherwise it will replace the existing one.
     * @param regionCSS the region CSS, never <code>null</code>.
     * @param filePath the file path, not blank.
     */
    public void save(PSRegionCSS regionCSS, String filePath) throws PSThemeNotFoundException {
        notNull(regionCSS);
        notEmpty(filePath);
        
        List<PSRegionCSS> regions = read(filePath);
        PSRegionCSS r = findRegionCSS(regionCSS.getOuterRegionName(), regionCSS.getRegionName(), regions);
        if (r != null)
        {
            regions.remove(r);
            regions.add(regionCSS);
        }
        else
        {
            regions.add(regionCSS);
        }
        write(filePath, regions);
    }
    
    /**
     * Deletes the specific region CSS from the specified file.
     * @param outerRegion the most outer region name of the specified region, not blank.
     * @param region the region name, not blank.
     * @param filePath the file path, not blank.
     */
    public void delete(String outerRegion, String region, String filePath) throws PSThemeNotFoundException {
        notEmpty(outerRegion);
        notEmpty(region);
        notEmpty(filePath);
        
        List<PSRegionCSS> regions = read(filePath);
        PSRegionCSS r = findRegionCSS(outerRegion, region, regions);
        if (r != null)
        {
            regions.remove(r);
            write(filePath, regions);
        }
    }
    
    /**
     * Gets all region CSS from the specified file path;
     * @param filePath the file path, not blank.
     * @return the list of region CSS that contains in the file, never <code>null</code>, may be empty.
     */
    public List<PSRegionCSS> read(String filePath) throws PSThemeNotFoundException {
        notEmpty(filePath);
        
        String contents = getContentFromFile(filePath);
        if (contents == null)
            return new ArrayList<>();

        return readFromString(contents);
    }

    private List<PSRegionCSS> readFromString(String contents)
    {
        List<PSRegionCSS> regions = new ArrayList<>();
        
        CascadingStyleSheet aCSS = CSSReader.readFromString (contents, RX_STANDARD_ENC, ECSSVersion.CSS30);

        if(aCSS !=null) {
            List<CSSStyleRule> rules = aCSS.getAllStyleRules();
                for (CSSStyleRule rule : rules) {
                    PSRegionCSS r = getRegionCSS(rule);
                    if (r != null) {
                        regions.add(r);
                    }
                }
            }
        return regions;
    }

    /**
     * Writes a list of region CSS to the specified file. 
     * The list will be sorted then saved into the specified file.
     * 
     * @param filePath the specified file path, not blank.
     * @param cssList the list of region CSS, not <code>null</code>, may be empty.
     */
    @SuppressWarnings("unchecked")
    public void write(String filePath, List<PSRegionCSS> cssList) throws PSThemeNotFoundException {
        Collections.sort(cssList);
        
        StringBuilder buffer = new StringBuilder();
        for (PSRegionCSS r : cssList)
        {
            buffer.append(r.getAsCSSString());
        }
        
        writeContent(filePath, buffer.toString());
    }
    
    /**
     * Merge the specified source file to the specified target file.
     * The merge process overrides the region-css from source to target
     * for those regions that exist in the specified region tree. 
     * 
     * @param tree the region tree, not <code>null</code>.
     * @param srcPath the source file, not blank.
     * @param targetPath the target file, not blank.
     */
    public void mergeFile(PSRegionTree tree, String srcPath, String targetPath) throws PSThemeNotFoundException {
        notNull(tree);
        notEmpty(srcPath);
        notEmpty(targetPath);
        
        List<PSRegionCSS> regions = getRegionCssFromTreeAndSource(tree, srcPath);
        if (regions == null || regions.isEmpty())
            return;

        mergeRegions(regions, targetPath);
    }

    private void mergeRegions(List<PSRegionCSS> srcRegions, String targetPath) throws PSThemeNotFoundException {
        // create the target file if not exist
        getTargetFile(targetPath);

        List<PSRegionCSS> targetRegions = read(targetPath);
        removeSourceFromTarget(srcRegions, targetRegions);
        
        List<PSRegionCSS> result = new ArrayList<>();
        result.addAll(srcRegions);
        result.addAll(targetRegions);
        
        write(targetPath, result);
    }
    
    private void removeSourceFromTarget(List<PSRegionCSS> src, List<PSRegionCSS> target)
    {
        for (PSRegionCSS r : src)
        {
            PSRegionCSS t = findRegionCSS(r.getOuterRegionName(), r.getRegionName(), target);
            if (t != null)
            {
                target.remove(t);
            }
        }
    }
    
    /**
     * Gets the region css from the source for the regions that exist in the specified tree.
     * @param tree the region tree, assumed not <code>null</code>.
     * @param srcPath the source file path, assumed not blank.
     * @return list of region css. It may be <code>null</code> if the region tree is empty
     * or the source file is empty (or not exist).
     */
    private List<PSRegionCSS> getRegionCssFromTreeAndSource(PSRegionTree tree, String srcPath) throws PSThemeNotFoundException {
        List<String> names = getRegionNames(tree);
        if (names.isEmpty())
            return null;
        
        String content = getContentFromFile(srcPath);
        if (content == null || isBlank(content))
            return null;
        
        List<PSRegionCSS> regions = readFromString(content);
        if (regions.isEmpty())
            return null;
        
        List<PSRegionCSS> results = new ArrayList<>();
        for (PSRegionCSS r : regions)
        {
            if (matchRegionNames(r, names))
            {
                results.add(r);
            }
        }
        return results;
    }
    
    private boolean matchRegionNames(PSRegionCSS r, List<String> regionNames)
    {
        String outer = regionNames.get(0);
        for (String name : regionNames)
        {
            if (StringUtils.equals(outer, r.getOuterRegionName()) &&
                    StringUtils.equals(name, r.getRegionName()))
            {
                return true;
            }
        }
        return false;
    }
    
    private List<String> getRegionNames(PSRegionTree tree)
    {
        List<String> names = new ArrayList<>();
        for (PSRegion r : tree.getDescendentRegions())
        {
            names.add(r.getRegionId());
        }
        return names;
    }
    
    /**
     * Copy the specified source file to the target file. 
     * This will create an empty target file if the source file is not specified.
     * 
     * @param srcPath the path of the source file. It may be <code>null</code> if wants to create an empty target file.
     * @param targetPath the path of the target file. It may not be empty.
     */
    public void copyFile(String srcPath, String targetPath) throws PSThemeNotFoundException {
        notEmpty(targetPath);
        
        File srcFile = getSourceFile(srcPath);
        File target = getTargetFile(targetPath);
        
        copyFile(srcPath, targetPath, srcFile, target);
    }

    private void copyFile(String srcPath, String targetPath, File srcFile, File target) throws PSThemeNotFoundException {

        try(OutputStream out = new FileOutputStream(target))
        {
            if (srcFile != null)
            {
                try(InputStream in = new FileInputStream(srcFile)) {
                    IOUtils.copy(in, out);
                }
            }
            else
            {
                IOUtils.write(
                        ".percDummyRule{/* Dummy rule for correct HTML's LINK tag rendering during editing a template */}",
                        out,
                        StandardCharsets.UTF_8);
                
            }
        }
        catch (IOException e)
        {
            if (srcFile != null)
                throw new PSThemeNotFoundException("Failed to copy region CSS file, from source '" + srcPath + "' to target file '" + targetPath + "'.");
            else
                throw new PSThemeNotFoundException("Failed to create empty region CSS file, '" + targetPath + "'.");
        }

    }

    private File getTargetFile(String targetPath)
    {
        File target = new File(targetPath);
        File parent = target.getParentFile();
        if (!parent.exists())
        {
            parent.mkdirs();
        }
        return target;
    }

    private File getSourceFile(String srcPath) throws PSThemeNotFoundException {
        File srcFile = null;
        
        if (srcPath != null)
        {
            srcFile = new File(srcPath);
            if (!srcFile.exists())
            {
                throw new PSThemeNotFoundException("Failed to copy region CSS file, cannot find source file: " + srcPath);
            }
        }
        return srcFile;
    }
    
    private void writeContent(String filePath, String content) throws PSThemeNotFoundException {

        
        try( OutputStream out = new FileOutputStream(filePath))
        {
            IOUtils.write(content, out, StandardCharsets.UTF_8);
        }
        catch (FileNotFoundException fe)
        {
            throw new PSThemeNotFoundException("Cannot find file: " + filePath, fe);
        }
        catch (IOException e)
        {
            throw new PSThemeNotFoundException("Failed to write file: " + filePath, e);
        }
    }

    private String getContentFromFile(String filePath) throws PSThemeNotFoundException {

       try
        {
            File file = new File(filePath);
            if (!file.exists())
                return null;

            try(InputStream in  = new FileInputStream(file)) {
               return IOUtils.toString(in, StandardCharsets.UTF_8);
            }
        }
        catch (FileNotFoundException fe)
        {
            throw new PSThemeNotFoundException("Cannot find file: " + filePath, fe);
        }
        catch (IOException e)
        {
            throw new PSThemeNotFoundException("Failed to read file: " + filePath, e);
        }

    }
    
    private PSRegionCSS getRegionCSS(CSSStyleRule rule)
    {
        PSRegionCSS regionCSS = getRegionNames(rule);
        if (regionCSS != null)
        {
            setProperties(rule.getAllDeclarations(), regionCSS);
        }
        return regionCSS;
    }
    
    private void setProperties(List<CSSDeclaration> decs, PSRegionCSS css)
    {
        CSSWriterSettings wsettings = new CSSWriterSettings(ECSSVersion.CSS30, true);
        List<PSRegionCSS.Property> props = new ArrayList<>();
        
        for (CSSDeclaration dec : decs)
        {
            PSRegionCSS.Property prop = new PSRegionCSS.Property();
            prop.setName(dec.getProperty());
            prop.setValue(dec.getExpression().getAsCSSString(wsettings, 0));
            props.add(prop);
        }
        css.setProperties(props);
    }
    
    private PSRegionCSS getRegionNames(CSSStyleRule rule)
    {
        if (rule.getAllSelectors().size() != 1)
        {
            return null;
        }
        
        CSSSelector selector = rule.getAllSelectors().get(0);
        
        List<ICSSSelectorMember> members = selector.getAllMembers();
        if (members.size() != 2 && members.size() != 5) 
        {
            return null;
        }
        
        return getRegionCSS(members);
    }
    
    private PSRegionCSS getRegionCSS(List<ICSSSelectorMember> members)
    {
        String outerRegion = getRegionName(members.get(0));
        if (outerRegion == null)
            return null;
        
        if (!REGION_CLASS.equals(getValue(members.get(1))))
            return null;
        
        if (members.size() == 2)
        {
            return new PSRegionCSS(outerRegion, outerRegion);
        }
        
        if (!isBlankMember(members.get(2)))
            return null;
        
        String region = getRegionName(members.get(3));
        if (region == null)
            return null;
        
        if (!REGION_CLASS.equals(getValue(members.get(4))))
            return null;
        
        return new PSRegionCSS(outerRegion, region);
    }
    
    private String getRegionName(ICSSSelectorMember member)
    {
        String value = getValue(member);
        if ( value == null || (!value.startsWith("#")) || value.length() == 1)
            return null;
        
        return value.substring(1);
    }

    private String getValue(ICSSSelectorMember member)
    {
        if (!(member instanceof CSSSelectorSimpleMember))
            return null;
        
        return ((CSSSelectorSimpleMember) member).getValue();
    }
 
    private boolean isBlankMember(ICSSSelectorMember member)
    {
        if (! (member instanceof ECSSSelectorCombinator))
            return false;
        
        ECSSSelectorCombinator combinator = (ECSSSelectorCombinator) member;
        return (combinator == ECSSSelectorCombinator.BLANK);
    }
}
