/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
/*
 * com.percussion.pso.jexl PSONavTools.java
 * 
 * @COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * @author DavidBenua
 *
 */
package com.percussion.pso.jexl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.Value;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummaries;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.cms.objectstore.PSRelationshipProcessorProxy;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSJexlExpression;
import com.percussion.extension.IPSJexlMethod;
import com.percussion.extension.IPSJexlParam;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSJexlUtilBase;
import com.percussion.pso.relationships.IPSOParentFinder;
import com.percussion.pso.relationships.PSOParentFinder;
import com.percussion.pso.utils.PSORequestContext;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.impl.nav.PSNavConfig;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;

/**
 * Binding functions usefule for Navigation.
 * These functions are intended to be called from JEXL.   
 *
 * @author DavidBenua
 *
 */
public class PSONavTools extends PSJexlUtilBase implements IPSJexlExpression
{
   
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSONavTools.class);   
   
  
   private static IPSGuidManager gmgr = null; 
   private static PSNavConfig  navConfig = null; 
   private static IPSOObjectFinder objFinder = null;
   private static IPSOParentFinder parFinder = null; 
   private static PSRelationshipProcessorProxy proxy = null;
   /**
    * 
    */
   public PSONavTools()
   {
      super();
   }
  
   
   private static void initServices()
   {
      if(gmgr == null)
      {
         gmgr = PSGuidManagerLocator.getGuidMgr();
         navConfig = PSNavConfig.getInstance(); 
         objFinder = new PSOObjectFinder();
         parFinder = new PSOParentFinder();
      }
      if(proxy == null)
      {
         try
         {
            IPSRequestContext requestContext = new PSORequestContext();
            proxy = new PSRelationshipProcessorProxy(
                  PSRelationshipProcessorProxy.PROCTYPE_SERVERLOCAL,requestContext);
         } catch (PSCmsException ex)
         {
            log.error("Unexpected Exception initializing proxy " + ex,ex);
         }
      }
        
      
   }
   /**
    * Builds a list of ancestor nodes.  
    * The first node in the list will be the <code>Root</code> node
    * and the last node will be the <code>Self</code> node. If
    * the <code>Self</code> node is the <code>Root</code> node, 
    * there will be only one node in the list.
    * <p>
    * This function can be used to generate breadcrumbs. Here is
    * a simple example: 
    * <pre>
    * &lt;div class="BreadCrumbs">
    *   #set($bpath = $user.psoNavTools.getAncestors($nav.self))      
    *   #foreach($bcrumb in $bpath)b
    *       #set($landing_page = $bcrumb.getProperty("nav:url").String)
    *       #set($title = $bcrumb.getProperty("rx:displaytitle").String)
    *       #if( $landing_page )
    *          &lt;a href="$landing_page">$title&lt;/a>
    *       #else
    *          $title
    *       #end
    *   #end    
    * &lt;/div>
    * </pre>
    * 
    * @param selfNode the self node, usually <code>$nav.self</code>
    * @return The list of ancestors, including the self node. 
    * Never <code>null</code> or <code>empty</code>.
    * @throws PSExtensionProcessingException
    */
   @IPSJexlMethod(description="get the ancestors for this node", 
         params={@IPSJexlParam(name="selfNode", description="the current item")})
   public List<Node> getAncestors(Node selfNode) 
      throws PSExtensionProcessingException
   {
      if(selfNode == null)
      {
         String emsg = "Self Node cannot be null"; 
         log.error(emsg);
         throw new IllegalArgumentException(emsg); 
      }
      ArrayList<Node> ancestors = new ArrayList<Node>(); 
      Node node = selfNode; 
      
      try
      {
         while(node != null)
         {
            ancestors.add(0, node);
            log.trace("adding node " + node.getName() + " depth " + node.getDepth());
            if(node.getDepth() == 0)
            {
               log.trace("Depth is 0"); 
               break;
            }
            node = node.getParent(); 
         }
      /*
       * The JCR Javadoc says that calling getParent() from the root will
       * throw this exception.  However, the Percussion CMS implementation will
       * just return NULL instead. 
       */
      } catch (ItemNotFoundException ie)
      { 
        log.trace("item not found, might be root");
        //not really an error: we are done 
      } catch (Exception e)
      {
        String emsg = "Unexpected Exception " + e.getMessage(); 
        log.error(emsg, e); 
        throw new PSExtensionProcessingException("PSONavTools", e); 
      }
      
      return ancestors;
   }
 
   /**
    * Finds the navon or navtree in a given folder. The navon node is a regular
    * content node, it is not a nav proxy node: the extra nav attributes are missing. 
    * @param folderid the folder id
    * @return the navon as a node. Will return <code>null</code> if the navon cannot 
    * be found. 
    * @throws Exception
    */
   @IPSJexlMethod(description="find the navon node based on the folder id",
         params={@IPSJexlParam(name="folderid", description="folder id")})   
   public IPSNode findNavNodeForFolder(String folderid) throws Exception
   {
      initServices();
      PSRelationshipFilter filter = new PSRelationshipFilter();
      filter.setOwner(new PSLocator(folderid, "0")); 
      filter.setName(PSRelationshipFilter.FILTER_NAME_FOLDER_CONTENT);
      Set<Long> typeIds = new HashSet<Long>();
      typeIds.add(new Long(navConfig.getNavonType().getUUID()));
      typeIds.add(new Long(navConfig.getNavTreeType().getUUID())); 
      filter.setDependentContentTypeIds(typeIds);
      PSComponentSummaries summs = proxy.getSummaries(filter, false); 
      if(summs.size() > 1)
      {
         log.error("More than one Navon found in Folder. Possible invalid tree.");
         return null; 
      }
      if(summs.size() == 1)
      {
         PSComponentSummary s = (PSComponentSummary)summs.getSummaries().next();
         IPSGuid nguid = gmgr.makeGuid(s.getCurrentLocator()); 
         return objFinder.getNodeByGuid(nguid);
      }
      log.debug("no navons found in folder " + folderid); 
      return null;
      
   }
   
   /**
    * Finds the for a current navon.
    * Convenience method for findParentNavonNode(PSLocator). 
    * Note that the Node contains the content data only, the extra Navon proxy properties are 
    * not included in this node.    
    * @param navonId the content id of the current navon
    * @return the parent navon node, or <code>null</code> if the navon has no parent. 
    * @throws Exception
    */
   @IPSJexlMethod(description="finds the parent of a Navon", 
         params={@IPSJexlParam(name="navonId", description="Content id of current navon")})
   public IPSNode findParentNavonNode(String navonId) throws Exception
   {
      PSLocator loc = new PSLocator(navonId);
      return findParentNavonNode(loc);
   }

   /**
    * Finds the for a current navon.
    * Convenience method for findParentNavonNode(PSLocator). 
    * Note that the Node contains the content data only, the extra Navon proxy properties are 
    * not included in this node.    
    * @param navonGuid the GUID of the current navon. 
    * @return the parent navon node, or <code>null</code> if the navon has no parent. 
    * @throws Exception
    */
   @IPSJexlMethod(description="finds the parent of a Navon", 
         params={@IPSJexlParam(name="navonGuid", description="Guid of current navon")})
   public IPSNode findParentNavonNode(IPSGuid navonGuid) throws Exception
   {
      PSLocator loc = gmgr.makeLocator(navonGuid);
      return findParentNavonNode(loc);        
   }
   
   /**
    * Finds the for a current navon. 
    * Note that the Node contains the content data only, the extra Navon proxy properties are 
    * not included in this node.   
    * @param navonLoc the locator for the current navon.
    * @return the parent navon node, or <code>null</code> if the navon 
    * has no parent. 
    * @throws Exception
    */
   @IPSJexlMethod(description="finds the parent of a Navon", 
         params={@IPSJexlParam(name="navonLoc", description="Locator of current navon")})
   public IPSNode findParentNavonNode(PSLocator navonLoc) throws Exception
   {
      initServices();
      Set<PSLocator> parents = parFinder.findParents(navonLoc, navConfig.getSubmenuRelationship(),false);
      if(parents.size() > 1)
      {
         log.warn("Navon has more than one parent. Possible invalid tree");
         return null; 
      }
      if(parents.size() == 1)
      {
         PSLocator ploc = parents.iterator().next();
         IPSGuid pguid = gmgr.makeGuid(ploc); 
         return objFinder.getNodeByGuid(pguid); 
      }
      return null; 
   }
   
   /**
    * Gets the nearest property value for a named property.  
    * This method checks each navon for a value which is non-null and not empty. If the value is defined
    * on the current navon is it is returned, and if not, the parent navon is examined, and so on until a 
    * non-empty value is encountered.  This method will return <code>null</code> if no values are defined.  
    * @param folderid the folder id where the current navon is located.
    * @param propertyName the property name.
    * @return the nearest value, or <code>null</code> if no values can be located. 
    * @throws Exception
    */
   @IPSJexlMethod(description="finds the nearest non-empty value for a property",
         params={@IPSJexlParam(name="folderid",description="content id of folder"),
          @IPSJexlParam(name="propertyName",description="name of property")})
   public Value findNearestNavonPropertyValue(String folderid, String propertyName) throws Exception
   {  
      IPSNode navonNode = findNavNodeForFolder(folderid); 
      while(navonNode != null)
      {
         if(navonNode.hasProperty(propertyName))
         {
              Value val = navonNode.getProperty(propertyName).getValue();
              if(val != null && val.getString().trim().length() > 0)
              {
                 return val; 
              }

         }
         navonNode = this.findParentNavonNode(navonNode.getGuid());
      }
      return null; 
   }
    /**
    * @param gmgr the gmgr to set
    */
   public static void setGmgr(IPSGuidManager gmgr)
   {
      PSONavTools.gmgr = gmgr;
   }


   /**
    * @param navConfig the navConfig to set
    */
   public static void setNavConfig(PSNavConfig navConfig)
   {
      PSONavTools.navConfig = navConfig;
   }


   /**
    * @param objFinder the objFinder to set
    */
   public static void setObjFinder(IPSOObjectFinder objFinder)
   {
      PSONavTools.objFinder = objFinder;
   }


   /**
    * @param parFinder the parFinder to set
    */
   public static void setParFinder(IPSOParentFinder parFinder)
   {
      PSONavTools.parFinder = parFinder;
   }


   /**
    * @param proxy the proxy to set
    */
   public static void setProxy(PSRelationshipProcessorProxy proxy)
   {
      PSONavTools.proxy = proxy;
   }
   
   
   
}
