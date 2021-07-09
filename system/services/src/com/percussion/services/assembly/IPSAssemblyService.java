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
package com.percussion.services.assembly;

import com.percussion.services.catalog.IPSCataloger;
import com.percussion.utils.guid.IPSGuid;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.servlet.http.HttpServletRequest;

/**
 * The assembler service acts as a toplevel assembler that dispatches to
 * component assemblers on the basis of the passed template, and additionally
 * supplies the methods to manipulate templates and slots. See the super
 * interface descriptions for more details.
 * <p>
 * Assembling an item may start with the assembly servlet, or a direct call to
 * the assembly service. The service performs the following steps when
 * assembling items:
 * <ul>
 * <li> Load the content item
 * <li> Bind initial site and other context variables passed in with the
 * assembly item
 * <li> Bind HTTP params passed in with the assembly items
 * <li> Bind extension functions
 * <li> Evalute the template bindings to create a final set of bound variables
 * <li> Bind extra objects for plugin, e.g. $sys.asm to the assembler facade.
 * <li> Invoke the assembly plugin
 * <li> Construct result object and return
 * </ul>
 * <p>
 * The assembly service does not do these steps for one, specific plug-in. The
 * legacy content assembler is simply handed the same data, without binding or
 * calculating a context
 * 
 * @author dougrand
 */
public interface IPSAssemblyService
      extends
         IPSAssembler,
         IPSTemplateService,
         IPSCataloger
{
   /**
    * Method used to create new assembly items. This method must be used to
    * create items that will work with the assembly system. Note that after the
    * item is created, data has been normalized. The normalization process may
    * cause an exception to be thrown if, for example, essential information is
    * missing.
    * <p>
    * You do <em>not</em> need to call {@link IPSAssemblyItem#normalize()} if
    * you call this factory method.
    * 
    * @param path the path of the content item, never <code>null</code> or
    *           empty. May have the form "/cid#revision" for items that are not
    *           in a folder.
    * @param jobid The job identifier, should not repeat for a long time. This
    *           allows the assembly system to cache information for a particular
    *           publishing run.
    * @param refid Identifies a particular request in a publishing run, only
    *           needs to be unique within a run.
    * @param template The template to render this item with, must not be
    *           <code>null</code>
    * @param variables site or other variables that should be made available to
    *           the assembler
    * @param params parameters to the assembly process. When invoked from the
    *           servlet, all HTTP parameters should be bound here
    * @param optionalNode a node to put in the assembly item, may be
    *           <code>null</code>, in which case the assembly service will
    *           load the node using the path supplied to this method
    * @param debug if <code>true</code> the item will be assembled in debug
    *           mode
    * @return an item suitable for the call to
    *         {@link IPSAssembler#assemble(List)}
    * @throws PSAssemblyException if there is a problem creating the item
    * @deprecated use {@link #createAssemblyItem()} and call the setters and
    *             {@link IPSAssemblyItem#normalize()} instead.
    */
   public IPSAssemblyItem createAssemblyItem(String path, long jobid,
         int refid, IPSAssemblyTemplate template, Map<String, String> variables,
         Map<String, String[]> params, Node optionalNode, boolean debug)
         throws PSAssemblyException;
   
   /**
    * Create an assembly item to be used with the assembly service. Once this
    * method is called, use the setters on the assembly item to setup
    * information to be used in assembly. Once these are called, you must call
    * the {@link IPSAssemblyItem#normalize()} method before assembling the item.
    * 
    * @return an uninitialized assembly item, never <code>null</code>
    */
   public IPSAssemblyItem createAssemblyItem();

   /**
    * Process an assembly item using data passed into the assembly servlet.
    * Internally this calls
    * {@link #createAssemblyItem(String, long, int, IPSAssemblyTemplate, Map,
    * Map, Node, boolean)} after looking up the pieces of the request, and then
    * calls {@link IPSAssembler#assemble(List)}.
    * <p>
    * Parameters that may be passed, and the validation rules for them: <table>
    * <tr>
    * <th>sys_path</th>
    * <td>the path to the content item, if this is not <code>null</code> and
    * not empty then it will be used in favor of the contentid, revision and
    * folderid. If this is not present then the other parameters must be 
    * present. The revision may be specified by appending #nnn to the path,
    * where nnn is an integer that corresponds to the desired revision.</td>
    * </tr>
    * <tr>
    * <th>sys_contentid</th>
    * <td>the content id of the content item, may be omitted if path is
    * specified</td>
    * </tr>
    * <tr>
    * <th>sys_revision</th>
    * <td>the revision of the content item, may be omitted if path is specified.
    * If the revision is not specified or is the value <code>-1</code> then
    * the current revision or edit revision will be used. The edit revision will
    * be used if the logged in user has the item checked out (as determined
    * by {@link HttpServletRequest#getRemoteUser()}.</td>
    * </tr>
    * <tr>
    * <th>sys_folderid</th>
    * <td>the folder id of the folder that contains the content item, may be
    * omitted if the path is specified</td>
    * </tr>
    * <tr>
    * <th>sys_filter</th>
    * <td>the name of the item filter to use when evaluating slot content
    * finders. May be omitted if authtype is specified. If specified then
    * authtype is ignored.</td>
    * </tr>
    * <tr>
    * <th>sys_authtype</th>
    * <td>the id of the authtype to use when evaluating slot content finders.
    * Must translate to a registered finder or an error will be thrown. May be
    * omitted or empty if filter is specified.</td>
    * </tr>
    * <tr>
    * <th>sys_siteid</th>
    * <td>the id of the site to use for finding various pieces of information
    * including site variables, global templates, etc. Not required.</td>
    * </tr>
    * <tr>
    * <th>sys_context</th>
    * <td>the context to assemble in. The context controls the expression of
    * links in the assembled output, required.</td>
    * </tr>
    * <tr>
    * <th>sys_mode</th>
    * <td>if this value is "AA" then an internal parameter is set to allow
    * field and slot macros to show active assembly decorations.</td>
    * </tr>
    * <tr>
    * <th>sys_debug</th>
    * <td>if this value is "true" then the assembler will not call the assembly
    * plugin, but will instead return debugging information in html format.</td>
    * </tr>
    * </table>
    * 
    * @param request the original servlet request, the parameters are taken from
    *           this and passed into the assembler, never <code>null</code>
    * @param template the name of the template to use in assembly. The template
    *           provides various pieces of input data, including what assembly
    *           plugin to run and any textual input it requires. May be
    *           <code>null</code> or empty if templateid is specified.
    * @param templateid the numeric id of the template to use, may be
    *           <code>null</code> or empty if template is specified.
    * 
    * @return the result of the assembly, never <code>null</code>
    * @throws PSAssemblyException
    */
   public IPSAssemblyResult processServletRequest(HttpServletRequest request,
         String template, String templateid) throws PSAssemblyException;

   /**
    * Load the named finder via the extensions manager.
    * 
    * @param finder the finder to load, never <code>null</code> or empty,
    *           corresponds to the name that the finder was registered with the
    *           extensions manager.
    * @return a finder instance, never <code>null</code>
    * @throws PSAssemblyException if there is a problem loading the finder
    */
   public IPSSlotContentFinder loadFinder(String finder)
         throws PSAssemblyException;

   /**
    * Calculate a landing page url for the given landing page node. The node was
    * previously loaded using the content manager. This is generally called by
    * the managed nav implementation internally.
    * <p>
    * If the template being referenced is a <i>legacy</i> template then the
    * item is assembled and parsed to find the first anchor tag, and the href
    * attribute is extracted and returned.
    * <p>
    * For new templates, the requirement is that the bindings include a binding
    * for the variable <b>$pagelink</b>. The template is partially assembled,
    * and the value of <b>$pagelink</b> is returned.
    * 
    * @param parentItem the parent assembly item, never <code>null</code>.
    *           The parent item is cloned for the assembly
    * @param landingPage the landing page node being referenced, never
    *           <code>null</code>
    * @param templateId the template id of the landing page snippet, never
    *           <code>null</code>
    * @return the url for the landing page, never <code>null</code> or empty
    * @throws PSAssemblyException if there is a problem
    */
   public String getLandingPageLink(IPSAssemblyItem parentItem,
         Node landingPage, IPSGuid templateId) throws PSAssemblyException;
   
   /**
    * The assembly engine calls this from assemblers and inside the service
    * to set the current assembly item being addressed. This value is stored
    * in thread local storage, and is safe to use within the thread.
    * <p>
    * Assembly plugin implementations that do not use the base assembler
    * implementation should call this method.
    * 
    * @param item an assembly item to store, may be <code>null</code> to 
    * reset the value
    */
   public void setCurrentAssemblyItem(IPSAssemblyItem item);
   
   /**
    * This method allows a caller to discover the current item being assembled.
    * There may be no current item at times, e.g. some code is called outside
    * of the assembly service but may wish to use this call to obtain the 
    * current item when available.
    * 
    * @return the current item or <code>null</code> if undefined.
    */
   public IPSAssemblyItem getCurrentAssemblyItem();
   
   /**
    * Lookup the template for one or more assembly items. The method first
    * creates a map of the content types of the items. Then it runs through the
    * items. The map could be optimized out for cases where only the ids are
    * passed in, but moving toward names this may prove to be the common case.
    * <p>
    * This method is used internally in assembly, and also externally by the
    * publishing engine when processing work items for unpublishing.
    * 
    * @param items the items to look up the templates for, assumed not
    *           <code>null</code>
    * @throws PSAssemblyException
    */
   public void handleItemTemplates(List<IPSAssemblyItem> items) 
      throws PSAssemblyException;

   /**
    * The URL to invoke the assembly service. This is a partial URL from 
    * the context down. 
    */
   public static String ASSEMBLY_URL = "assembler/render";
}
