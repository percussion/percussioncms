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
package com.percussion.pagemanagement.service.impl;

import com.percussion.metadata.data.PSMetadata;
import com.percussion.metadata.service.IPSMetadataService;
import com.percussion.pagemanagement.dao.IPSWidgetDao;
import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSWidgetPackageInfo;
import com.percussion.pagemanagement.data.PSWidgetPackageInfoRequest;
import com.percussion.pagemanagement.data.PSWidgetPackageInfoResult;
import com.percussion.pagemanagement.data.PSWidgetSummary;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.pkginfo.IPSPkgInfoService;
import com.percussion.services.pkginfo.data.PSPkgElement;
import com.percussion.services.pkginfo.data.PSPkgInfo;
import com.percussion.services.pkginfo.utils.PSIdNameHelper;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.validation.PSAbstractPropertiesValidator;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.sf.json.JSONArray;


@Component("widgetService")
public class PSWidgetService implements IPSWidgetService {
    
    private IPSWidgetDao widgetDao;
    private IPSPkgInfoService pkgInfoSvc;
    private IPSMetadataService mdService;
    
    
    @Value("${widgetService.baseTemplate:perc.widget}")
    private String baseTemplate;
    
    private PSAbstractPropertiesValidator<PSWidgetItem> widgetUserPropertiesValidator = new PSWidgetUserPropertiesValidator(this);
    private PSAbstractPropertiesValidator<PSWidgetItem> widgetCssPropertiesValidator = new PSWidgetCssPropertiesValidator(this);
    
    //Private data variable initialized in getWidgetType method.
    private Map<String,String> widgetTypeMap = null;
    
    @Autowired
    public PSWidgetService(IPSWidgetDao widgetDao, IPSPkgInfoService pkgInfoSvc, IPSMetadataService mdService)
    {
        super();
        this.widgetDao = widgetDao;
        this.pkgInfoSvc = pkgInfoSvc;
        this.mdService = mdService;
    }

    
    public PSSpringValidationException validateWidgetItem(PSWidgetItem widgetItem)
    {
        PSSpringValidationException e = widgetUserPropertiesValidator.validate(widgetItem);
        widgetCssPropertiesValidator.validate(widgetItem, e);
        return e;
        
    }
    

    /**
     * Prepares the widget item for assembly.
     * Sets default values.
     * @param item never <code>null</code>.
     */
    public void normalizeWidgetItem(PSWidgetItem item) {
        PSWidgetDefinition def = load(item.getDefinitionId());
        PSWidgetUtils.setDefaultValuesFromDefinition(item, def);
    }

    public PSWidgetSummary find(String id) throws com.percussion.share.service.IPSDataService.DataServiceLoadException
    {
        PSWidgetDefinition full = load(id);
        if (full == null) throw new DataServiceLoadException("Cannot find widget for id: " + id);
        PSWidgetSummary summary = createWidgetSummary();
        convertFullToSummary(full, summary);
        return summary;
    }
    
    public List<PSWidgetSummary> findAll() {
        return findByType("All");
    }

    @Override
    public List<PSWidgetSummary> findByType(String type) {
        return findByType(type, null);
    }
    
    public List<PSWidgetSummary> findByType(String type, String filterDisabledWidgets) {
    	if(StringUtils.isBlank(type))
    		type = "All";
    	List<String> disabledWidgets = new ArrayList<String>();
    	boolean filter = StringUtils.isNotBlank(filterDisabledWidgets) && filterDisabledWidgets.equalsIgnoreCase("yes");
    	//If filter get the disabled widgets from metadata service
    	if(filter){
    		PSMetadata md = mdService.find("percwidgetconfiguration");
    		if(md != null){
    			String data = md.getData();
	    		if(StringUtils.isNotBlank(data)){
	    			JSONArray jsonArray =  JSONArray.fromObject(data);
	    			@SuppressWarnings("unchecked")
					Iterator<String> iter = jsonArray.iterator();
	    			while(iter.hasNext()){
	    				disabledWidgets.add(iter.next());
	    			}
	    		}
    		}
    	}
        
        List<PSWidgetSummary> summaries = new ArrayList<PSWidgetSummary>();
        List<PSWidgetDefinition> fulls = widgetDao.findAll();
        for (PSWidgetDefinition full : fulls) {
            PSWidgetSummary sum = createWidgetSummary();
            convertFullToSummary(full, sum);
            if(type.equalsIgnoreCase("All") || type.equalsIgnoreCase(sum.getType())){
            	if(!filter || !disabledWidgets.contains(sum.getId()))
            		summaries.add(sum);
            }
        }
        Collections.sort(summaries, summaryComp);
        return summaries;
    }
    
    //TODO: A PSWidgetDefinition should be a subclass of PSWidgetSummary
    private void convertFullToSummary(PSWidgetDefinition full, PSWidgetSummary summary) {
        if (full.getWidgetPrefs() != null) {
            summary.setId(full.getId());
            summary.setLabel(full.getWidgetPrefs().getTitle());
            summary.setName(full.getWidgetPrefs().getContenttypeName());
            summary.setIcon(full.getWidgetPrefs().getThumbnail());
            summary.setHasUserPrefs(!full.getUserPref().isEmpty());
            summary.setHasCssPrefs(!full.getCssPref().isEmpty());
            summary.setType(getWidgetType(full.getWidgetPrefs().getTitle()));
            summary.setCategory(full.getWidgetPrefs().getCategory());
            summary.setDescription(full.getWidgetPrefs().getDescription());
            summary.setResponsive(full.getWidgetPrefs().isResponsive());
        }
        else {
            log.error("Widget definition does not have user prefs, definitionId: " + full.getId());
        }
    }
    
    private PSWidgetSummary createWidgetSummary()
    {
        return new PSWidgetSummary();
    }
    
    /**
     * Helper method to get the widget type for the supplied widget name. If the
     * widgetTypeMap is <code>null</code>, then initializes it by loading
     * WidgetRegistry.xml. If the supplied widget is not a registered widget
     * then returns the type as "Custom".
     * 
     * @param widgetName The name of the widget for which the type needs to be
     *            found, assumed not blank.
     * @return The widget type, never <code>null</code>, will be "Custom" if the
     *         widget is not found in the registry.
     */
    private String getWidgetType(String widgetName)
    {
        // Load the map if needed
        if (widgetTypeMap == null)
        {
            widgetTypeMap = loadWidgetTypeMap();
        }

        String widgetType = widgetTypeMap.get(widgetName);
        if (widgetType == null)
            widgetType = "Custom";
        return widgetType;
    }

    /**
     * Helper method that loads the WidgetRegistry.xml and creates a map of widget name as key and 
     * widget type as value.
     * @return Map of widget name and type, never <code>null</code> may be empty.
     */
    private Map<String, String> loadWidgetTypeMap()
    {
        Map<String, String> widgetTypeMap = new HashMap<String, String>();
        InputStream in = null;
        in = this.getClass().getClassLoader()
                .getResourceAsStream("com/percussion/pagemanagement/service/impl/WidgetRegistry.xml");
        try
        {
            Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
            NodeList groupElems = doc.getElementsByTagName("group");
            for (int i = 0; i < groupElems.getLength(); i++)
            {
                Element groupElem = (Element) groupElems.item(i);
                String groupName = groupElem.getAttribute("name");
                NodeList widgetElems = groupElem.getElementsByTagName("widget");
                for (int j = 0; j < widgetElems.getLength(); j++)
                {
                    Element widgetElem = (Element) widgetElems.item(j);
                    String wdgName = widgetElem.getAttribute("name");
                    widgetTypeMap.put(wdgName, groupName);
                }
            }
        }
        catch (IOException e)
        {
            // This should not happen as we are reading the file from JAR
            // incase if it happens logging it and returning empty widget
            // map.
            log.error("Failed to load WidgetRegistry.xml file:", e);

        }
        catch (SAXException e)
        {
            // This should not happen as we are reading the file from JAR
            // incase if it happens logging it and returning empty widget
            // map.
            log.error("Failed to parse WidgetRegistry.xml file:", e);
        }
        return widgetTypeMap;
    }
    
    public void delete(String id) throws com.percussion.share.service.IPSDataService.DataServiceDeleteException
    {
        throw new UnsupportedOperationException("delete is not yet supported");
    }


    public PSWidgetDefinition load(String id) throws com.percussion.share.service.IPSDataService.DataServiceLoadException
    {
        PSWidgetDefinition wd =  widgetDao.find(id);
        if (wd == null) throw new DataServiceLoadException("No widget found for id: " + id);
        return wd;
    }

    @Override
    public PSWidgetPackageInfoResult findWidgetPackageInfo(PSWidgetPackageInfoRequest request)
    {
        PSWidgetPackageInfoResult results = new PSWidgetPackageInfoResult();
        
        for (String widgetName : request.getWidgetNames())
        {
            PSPkgInfo info = findPackageInfo(widgetName);
            if (info == null)
                continue;
            
            PSWidgetPackageInfo result = new PSWidgetPackageInfo();
            result.setWidgetName(widgetName);
            result.setProviderUrl(info.getPublisherUrl());
            result.setVersion(info.getPackageVersion());
            results.getPackageInfoList().add(result);
        }
        
        return results;
    }


    /**
     * Find the package info for the specified widget
     * 
     * @param widgetName The name of the widget, not <code>null</code>.
     * 
     * @return  The info, or null if not found.
     */
    private PSPkgInfo findPackageInfo(String widgetName)
    {
        PSPkgInfo pkgInfo = null;

        String filepath = widgetDao.getBaseConfigDir() + "/" + widgetName + ".xml";
        PSPkgElement pkgElem = pkgInfoSvc.findPkgElementByObject(PSIdNameHelper.getGuid(filepath, PSTypeEnum.USER_DEPENDENCY));

        if (pkgElem != null)
        {
            IPSGuid pkgGuid = pkgElem.getPackageGuid();
            try
            {
                pkgInfo = pkgInfoSvc.loadPkgInfo(pkgGuid);
            }
            catch (Exception e)
            {
                // noop, fall thru
            }
        }

        return pkgInfo;
    }


    public PSWidgetDefinition save(PSWidgetDefinition object) throws PSBeanValidationException,
            com.percussion.share.service.IPSDataService.DataServiceSaveException
    {
        throw new UnsupportedOperationException("save is not yet supported");
    }

    public PSValidationErrors validate(PSWidgetDefinition object)
    {
        // TODO Auto-generated method stub
        //return null;
        throw new UnsupportedOperationException("validate is not yet supported");
    }

   /*
    * (non-Javadoc)
    * @see com.percussion.pagemanagement.service.IPSWidgetService#getBaseTemplate()
    */
   public String getBaseTemplate()
   {
      return baseTemplate;
   }

   /*
    * (non-Javadoc)
    * @see com.percussion.pagemanagement.service.IPSWidgetService#setBaseTemplate(java.lang.String)
    */
   public void setBaseTemplate(String baseTemplate)
   {
      this.baseTemplate = baseTemplate;
   }
   
   /**
    * Used for sorting of {@link PSWidgetSummary} objects.  Sorts alphabetically by label (case-sensitive).
    */
   private static class SummaryComparator implements Comparator<PSWidgetSummary>
   {
       public int compare(PSWidgetSummary s1, PSWidgetSummary s2)
       {
           if(s1==null && s2 == null)
               return 0;
           else if(s1 == null && s2 != null)
               return -1;
           else if(s1 != null && s2==null)
               return 1;

           if(s1.getLabel()==null && s2.getLabel()==null)
               return 0;
           else if(s1.getLabel()!= null && s2.getLabel()!=null)
                return s1.getLabel().compareTo(s2.getLabel());
           else if(s1.getLabel()!= null && s2.getLabel()==null)
               return 1;
           else
               return -1;
       }
   }
   
   /**
    * Used for widget summary sorting.  Never <code>null</code>.
    */
   private SummaryComparator summaryComp = new SummaryComparator();

   /**
 * The log instance to use for this class, never <code>null</code>.
 */
   private static final Log log = LogFactory.getLog(PSWidgetService.class);


    

}
