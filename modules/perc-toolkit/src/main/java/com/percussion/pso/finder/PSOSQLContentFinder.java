/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.finder;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.pso.utils.PSOItemSummaryFinder;
import com.percussion.pso.utils.PSOSimpleSqlQuery;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSSlotContentFinder;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.impl.finder.PSBaseSlotContentFinder;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;

/**
 * Slot finder that uses a raw SQL Query.
 * <h2>Parameters</h2>  
 * There are 5 parameters: <code>query</code>, <code>sqlparams</code>, 
 * <code>template</code>, <code>max_results</code> and 
 * <code>order_by</code>.  
 * 
 * <h3>query</h3>
 * The SQL Query to execute.  This must be a valid SQL query for the 
 * backend repository.  It may contain ? placeholder parameters as defined
 * in <code>java.sql.PreparedStatement</code>. This parameter is
 * mandatory. Never <code>null</code> or <code>empty</code>. 
 * <h3>sqlparams</h3>
 * A <code>List<Object></code> of parameters to substitute in the SQL Query. These parameters 
 * must be of the appropriate type for the query, and the number of elements
 * in the list of must match the number of parameters. 
 * <h3>template</h3>
 * The template parameter provides the default template to return in the slot 
 * should the query not return a template as described below.
 * <h3>max_results</h3>
 * This parameter is handled by the superclass. Note that this parameter does
 * not control result set processing.  In the event of very large result sets, it 
 * is possible to run out of memory even if this parameter is specified properly.
 * The query should adjusted to return only the appropriate results.  
 * <h3>order_by</h3>
 * This parameter is handled by the superclass. Note that this parameter will cause
 * a JSR 170 query, and the syntax defines how the result set is resorted. 
 * Consider instead adding an <code>order by</code> clause to the <code>query</code>
 * parameter. 
 *  
 * <p>
 * Note that only the "standard" parameters defined in the base class
 * (<code>query</code>, <code>template</code>, <code>max_results</code>
 * and <code>order_by</code>)
 * can be specified in the slot definition. Any values for these parameters 
 * that are specified in the JEXL bindings will override values defined in the slot
 * definition. The <code>sqlparams</code> parameter  
 * must be defined in the JEXL bindings and passed to the 
 * <code>#slot</code> macro.
 * </p>  
 * <h2>Result Set</h2>
 * The <code>query</code> select may include up to 3 columns: 
 * <ol>
 * <li> The content id - mandatory. 
 * <li> The folder id - optional, must be <code>Numeric</code>
 * <li> The template name or id. - optional
 * </ol>
 * The folder and template will be added to the <code>SlotItem</code> generated
 * for the result row. Note that if the template is <code>null</code> the
 * superclass will provide a default template from the parameters. 
 * 
 * <h2>Example</h2>
 * The parameter passing for SQL queries is different from JSR-170 queries. 
 * Instead of a Map of named parameters, the caller must provide a List of 
 * parameter values.  In the JEXL bindings that can be done like this: 
 * <pre>
 * $slotparams.query = "select G.CONTENTID  from RXS_CT_GENERIC G, CONTENTSTATUS C where G.USAGE = ? and ... "
 * $sql[0]="Y" 
 * $sql[1]=100
 * $slotparams.sqlparams = $sql
 * </pre>
 *
 * In the Velocity template, refer to these parameters by referencing 
 * the binding variable. 
 * <pre> 
 * #slot("myslot" "header" "before" "after" "footer" $slotparams ) 
 * </pre>
 * Note the lack of quotes on the last parameter.  It is not possible 
 * to pass SQL parameters using the String form of parameters 
 * commonly used with JCR Queries.   
 *
 *
 * @see java.sql.PreparedStatement
 * @author DavidBenua
 *
 */
public class PSOSQLContentFinder extends PSBaseSlotContentFinder
      implements
         IPSSlotContentFinder
{
   
   private static IPSGuidManager gmgr = null; 
   private static IPSAssemblyService asm = null; 
   
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSOSQLContentFinder.class);

   /**
    * Default Constructor
    */
   public PSOSQLContentFinder()
   {
      super();
   }
   
   
   /**
    * @see com.percussion.services.assembly.impl.finder.PSBaseSlotContentFinder#getSlotItems(com.percussion.services.assembly.IPSAssemblyItem, com.percussion.services.assembly.IPSTemplateSlot, java.util.Map)
    */
   @Override
   @SuppressWarnings("unchecked")
   protected Set<SlotItem> getSlotItems(IPSAssemblyItem sourceItem,
         IPSTemplateSlot slot, Map<String, Object> selectors)
         throws RepositoryException, PSFilterException, PSAssemblyException
   {
      initServices();
      log.debug("Selectors are " + selectors); 
      
      Map<String, String> slotArgs = slot.getFinderArguments();
      LinkedHashSet<SlotItem> items = new LinkedHashSet<SlotItem>();
      
      String emsg;
      int sortRank = 1;
      
      String sqlQuery = super.getValue(slotArgs, selectors, PARAM_QUERY, null);
      if(StringUtils.isBlank(sqlQuery))
      {
         emsg = "The SQL Query must not be blank or empty";
         log.error(emsg);
         throw new IllegalArgumentException(emsg);
      }
      log.debug("SQL Query is " + sqlQuery); 
      
      List<? extends Object> sqlParams = (List<? extends Object>)selectors.get(PARAM_SQLPARAMS); 
      if(sqlParams == null)
      {
         sqlParams = new ArrayList<Object>(); 
      }
      else
      {
         log.debug("SQL Parameters are " + sqlParams);    
      }
      
      String cslink = getValue(slotArgs, selectors,
              PARAM_MAY_HAVE_CROSS_SITE_LINKS, null);
        boolean includeSiteId = StringUtils.isNotBlank(cslink)
              && cslink.equalsIgnoreCase("true");
        
      try
      {
         List<Object[]> queryResults = PSOSimpleSqlQuery.doQuery(sqlQuery, sqlParams);
         log.debug("Query returned " + queryResults.size() + " results");  
         for(Object[] result : queryResults)
         {            
            if(result.length < 1)
            {
               throw new RuntimeException("The query results must include a content id"); 
            }      
            //first column is content id
            Number cid = (Number)result[0];
            if(cid.intValue() < 1)
            {  
               emsg = "The content id must be larger than 0";
               log.error(emsg); 
               throw new RuntimeException(emsg); 
            }
            PSLocator loc = PSOItemSummaryFinder.getCurrentOrEditLocator(cid.intValue()); 
            IPSGuid itemGuid =  gmgr.makeGuid(loc); 
            log.debug("item guid is " + itemGuid); 
            
            IPSGuid folderGuid = null; 
            if(result.length > 1)
            { //second column is folder id
               if(result[1] != null)
               {
                  if(result[1] instanceof Number)
                  {
                     Number fid = (Number)result[1]; 
                     if(fid.intValue() > 1)
                     {
                        PSLocator folderLoc = new PSLocator(fid.intValue(), 0); 
                        folderGuid = gmgr.makeGuid(folderLoc);
                        log.debug("adding folder id " + folderGuid);
                     }
                  }
                  else
                  {
                     log.warn("Folder ID Result column must be numeric " + result[1]);
                  }
               }
            }
            
            IPSGuid templateGuid = null; 
            if(result.length > 2)
            { //Third column is template
               Object itemtemplate = result[2];
               templateGuid = findTemplateFromIdOrName(itemtemplate); 
               log.debug("Adding template id " + templateGuid);
            }
            SlotItem item = new SlotItem(itemGuid, templateGuid, sortRank++);
            if(folderGuid != null)
            {
               item.setFolderId(folderGuid); 
            }
            if(includeSiteId)
            {
               setSiteFolderId(item, true);
            }
            items.add(item); 
         }
         
      } catch (Exception ex)
      {
         emsg = "Unexpected Exception " + ex.getMessage();    
         log.error(emsg,ex);
         throw new RuntimeException(emsg,ex);
      }
      
      return items;
   }
   /**
    * This finder is an AUTOSLOT finder. 
    * @see com.percussion.services.assembly.IPSSlotContentFinder#getType()
    */
   public Type getType()
   {      
      return IPSSlotContentFinder.Type.AUTOSLOT;
   }
   
   
   /**
    * Find the template given a name or id. If the supplied parameter 
    * is a <code>Number</code> or a string consisting solely of 
    * numeric digits, then this is assumed to be a legacy "template id". 
    * If the parameter is anything else, it is assumed to be a template
    * name. 
    * @param template the template name or id. 
    * @return the template GUID or <code>null</code> if the template was
    * not found. 
    * @throws PSAssemblyException
    */
   protected IPSGuid findTemplateFromIdOrName(Object template) 
   throws PSAssemblyException
     
   {
      IPSGuid templateGuid = null; 
      if(template != null)
      {
         if(template instanceof Number)
         {
            Number tid = (Number)template;
            templateGuid = gmgr.makeGuid(tid.intValue(),PSTypeEnum.TEMPLATE);  
         }
         else if(template instanceof String)
         {  
            String tname = template.toString();
            if(StringUtils.isNumeric(tname))
            {
               int tempId = Integer.parseInt(tname);
               templateGuid = gmgr.makeGuid(tempId, PSTypeEnum.TEMPLATE); 
            }
            else
            {
            templateGuid = asm.findTemplateByName(tname).getGUID();
            }
         }
      }
      return templateGuid;
   }
   
   /**
    * Initialize Java Services pointers. 
    */
   private static void initServices()
   {
      if(gmgr == null)
      {
         gmgr = PSGuidManagerLocator.getGuidMgr(); 
         asm = PSAssemblyServiceLocator.getAssemblyService();          
      }
   }
   public static final String PARAM_SQLPARAMS = "sqlparams";
   
   private static final String PARAM_MAY_HAVE_CROSS_SITE_LINKS = 
		      "mayHaveCrossSiteLinks";
}
