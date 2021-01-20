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
package com.percussion.rx.config.data;

import com.percussion.deployer.client.IPSDeployConstants;
import com.percussion.deployer.objectstore.PSDependency;
import com.percussion.deployer.objectstore.PSDescriptor;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.deployer.objectstore.PSUserDependency;
import com.percussion.utils.collections.PSMultiValueHashMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author erikserating
 *
 */
public class PSDescriptorSummaryReport
{
    public PSDescriptorSummaryReport()
    {
       
    }
    
    public String getReport(PSExportDescriptor desc)
    {
       handleElements(desc);
       StringBuilder sb = new StringBuilder();
       createHeader(sb);
       createInfo(sb, desc);
       createSelectedDesignObjects(sb);
       createSelectedFileResources(sb);
       createPackagesSections(sb, desc);       
       createDependencies(sb);
       //createAssociations(sb);
       return sb.toString();
    }
    
    private void createHeader(StringBuilder sb)
    {
       sb.append(SEPARATOR);
       sb.append("Package Descriptor Summary -- ");
       SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
       sb.append(formatter.format(new Date()));
       sb.append(NEWLINE);
       sb.append(SEPARATOR);
       sb.append(NEWLINE);       
    }
    
    private void createInfo(StringBuilder sb, PSExportDescriptor desc)
    {
       sb.append("Package Name: ");
       sb.append(desc.getName());
       sb.append(NEWLINE);
       sb.append("Version: ");
       sb.append(desc.getVersion());
       sb.append(NEWLINE);
       sb.append("Publisher: ");
       sb.append(StringUtils.defaultString(desc.getPublisherName()));
       sb.append(NEWLINE);
       sb.append("Cms Minimum Version: ");
       sb.append(desc.getCmsMinVersion());
       sb.append(NEWLINE);
       sb.append("Cms Maximum Version: ");
       sb.append(desc.getCmsMaxVersion());
       sb.append(NEWLINE);
       sb.append(NEWLINE);
       sb.append("Description:");
       sb.append(NEWLINE);
       sb.append(indent(WordUtils.wrap(
          StringUtils.defaultString(desc.getDescription()), 70)));
       sb.append(NEWLINE);
    }
    
    /**
     * 
     * @param sb
     */
    private void createSelectedDesignObjects(StringBuilder sb)
    {
       createSectionFromMultiMap(
          "Selected Design Objects",
          m_designObjects, sb);       
    }
    
    /**
     * 
     * @param sb
     */
    private void createSelectedFileResources(StringBuilder sb)
    {
       sb.append("Selected File Resources");
       sb.append(NEWLINE);
       sb.append(SEPARATOR);       
              
       if(m_files != null && !m_files.isEmpty())
       {
          for(String file : m_files)
          {
             sb.append(BULLET);
             sb.append(SPACE);
             sb.append(file);
             sb.append(NEWLINE);
          }
       }
       else
       {
          sb.append("None");
          sb.append(NEWLINE);
       }
       sb.append(NEWLINE);
    }
    
    /**
     * 
     * @param sb
     * @param desc
     */
    private void createPackagesSections(StringBuilder sb,
       PSExportDescriptor desc)
    {
       Set<String> iDeps = new TreeSet();
       Set<String> aDeps = new TreeSet();
       for(Map<String, String> entry : desc.getPkgDepList())
       {
          String name = entry.get(PSDescriptor.XML_PKG_DEP_NAME);
          String version = entry.get(PSDescriptor.XML_PKG_DEP_NAME);
          boolean isImplied = Boolean.valueOf(
             entry.get(PSDescriptor.XML_PKG_DEP_IMPLIED));
          String display = BULLET + SPACE + name + 
             " (" + version + ")" + NEWLINE;
          if(isImplied)
          {
             iDeps.add(display);
          }
          else
          {
             aDeps.add(display);
          }
       }
       sb.append("Dependent Packages for Selected Objects");
       sb.append(NEWLINE);
       sb.append(SEPARATOR);
       
       if(!iDeps.isEmpty())
       {
          for(String dep :iDeps)
             sb.append(dep);
       }
       else
       {
          sb.append("None");
          sb.append(NEWLINE);
       }
       sb.append(NEWLINE);
       
       sb.append("Additional Required Packages");
       sb.append(NEWLINE);
       sb.append(SEPARATOR);
       
       if(!aDeps.isEmpty())
       {
          for(String dep :aDeps)
             sb.append(dep);
       }
       else
       {
          sb.append("None");
          sb.append(NEWLINE);
       }
       sb.append(NEWLINE);
    }    
    
    /**
     * 
     * @param sb
     */
    private void createDependencies(StringBuilder sb)
    {
       createSectionFromMultiMap(
          "Shared Dependencies",
          m_dependsMap, sb); 
    }
    
    /**
     * 
     * @param sb
     */
    private void createAssociations(StringBuilder sb)
    {
       createSectionFromMultiMap(
          "Associations",
          m_assocMap, sb); 
    }
    
    /**
     * Helper method to create a report section from the values in a
     * multi value hash map.
     * @param title
     * @param map
     * @param sb
     */
    private void createSectionFromMultiMap(
       String title, PSMultiValueHashMap map, StringBuilder sb)
    {
       sb.append(title);
       sb.append(NEWLINE);
       sb.append(SEPARATOR);       
       
       StringBuilder buff = new StringBuilder();
       for(String cat : m_cats)
       {
          List<String> obs = map.get(cat);
          if(!obs.isEmpty())
          {
             Set<String> sorted = new TreeSet<String>(obs);
             buff.append(cat);
             buff.append(NEWLINE);
             for(String o : sorted)
             {
                buff.append(INDENT);
                buff.append(BULLET);
                buff.append(SPACE);
                buff.append(o);
                buff.append(NEWLINE);
             }
          }
       }
       if(buff.length() > 0)
       {
          sb.append(buff);
       }
       else
       {
          sb.append("None");
          sb.append(NEWLINE);
       }
       sb.append(NEWLINE);
    }
    
    /**
     * Helper to indent all lines of a string passed in.
     * @param content assumed not <code>null</code>.
     * @return never <code>null</code>.
     */
    private String indent(String content)
    {
       StringBuilder sb = new StringBuilder();
       for(String line : content.split(NEWLINE))
       {
          sb.append(INDENT);
          sb.append(line);
          sb.append(NEWLINE);          
       }
       return sb.toString();
    }
    
    private void handleElements(PSExportDescriptor desc)
    {
       m_designObjects =  new PSMultiValueHashMap<String, String>();
       m_dependsMap = new PSMultiValueHashMap<String, String>();
       m_assocMap = new PSMultiValueHashMap<String, String>();
       m_cats = new TreeSet<String>();
       
       Iterator<? extends PSDependency> elements = desc.getPackages();
       while(elements.hasNext())
       {
          PSDependency depend = elements.next();
          if(depend.getObjectType().equals(
             IPSDeployConstants.DEP_OBJECT_TYPE_CUSTOM) &&
             depend.getDependencyId().equals("sys_UserDependency"))
          {
                  m_files = handleFileResources(depend);
          }
          else
          {
             depend = getActualDependency(depend);
             m_cats.add(depend.getObjectTypeName());
             m_designObjects.put(depend.getObjectTypeName(), depend.getDisplayName());
             handleDepends(depend, m_dependsMap, m_assocMap);
          }
          
       }
    }
    
    /**
     * Helper method to return the actual dependency, meaning
     * if "Custom" then we need to retrieve the "wrapped"
     * dependency.
     * @param dep assumed not <code>null</code>.
     * @return the actual dependency, never <code>null</code>.
     */
    private PSDependency getActualDependency(PSDependency dep)
    {
       if(dep.getObjectType().equals("Custom"))
       {
          Iterator it = dep.getDependencies();
          if(it != null && it.hasNext())
          {
             return (PSDependency)it.next();
          }          
       }
       return dep;
    }
    
    private void handleDepends(PSDependency depend,
       PSMultiValueHashMap<String, String> dependmap,
       PSMultiValueHashMap<String, String> assocmap)
    {
       Iterator<PSDependency> children = 
          (Iterator<PSDependency>)depend.getDependencies();
       if(children == null)
          return;
       while(children.hasNext())
       {
          PSDependency dep = getActualDependency(children.next());
          if(!dep.isIncluded() && !dep.isAssociation())
             continue;
          if(dep.isAssociation())
          {
             m_cats.add(dep.getObjectTypeName());
             assocmap.put(dep.getObjectTypeName(), dep.getDisplayName());
          }
          else
          {
             if(dep.getDependencyType() == PSDependency.TYPE_SHARED)
             {   
                m_cats.add(dep.getObjectTypeName());
                dependmap.put(dep.getObjectTypeName(), dep.getDisplayName());
             }
             handleDepends(dep, dependmap, assocmap);
          }
       }
    }
    
    /**
     * Gets the file resource paths from the passed in user dependency
     * if specified. If not it just set file resources to an empty Set.
     * @param userDepend may be <code>null</code>.
     */
    private Set handleFileResources(PSDependency userDepend)
    {
       TreeSet results = new TreeSet<String>();
       if(userDepend != null)
       {
          Iterator it = userDepend.getDependencies();
          while(it.hasNext())
          {
             PSUserDependency dep = (PSUserDependency)it.next();
             results.add(dep.getPath().getPath());
          }
       }
       return results;
    }
    
    
    protected PSMultiValueHashMap<String, String>  m_designObjects;
    protected PSMultiValueHashMap<String, String> m_dependsMap;
    protected PSMultiValueHashMap<String, String> m_assocMap;
    protected Set<String> m_cats;
    protected Set<String> m_files;
    
    
    private static final String NEWLINE = "\r\n";
    
    private static final String SEPARATOR = 
       "====================================" + 
       "===================================" +
       NEWLINE;
    
    private static final String BULLET = "*";
    
    private static final String SPACE = " ";
    
    private static final String INDENT = 
       SPACE + SPACE + SPACE;
    
   
    
   
    
    
}
