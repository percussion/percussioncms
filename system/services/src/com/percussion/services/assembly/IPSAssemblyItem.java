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
package com.percussion.services.assembly;

import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.services.assembly.impl.nav.PSNavHelper;
import com.percussion.services.filter.IPSItemFilter;
import com.percussion.services.filter.PSFilterException;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.guid.IPSGuid;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import javax.jcr.Node;

/**
 * Each unit of work to be assembled is represented by an assembly item. The
 * item encapsulates all the information necessary. The output of the assembly
 * process is {@link com.percussion.services.assembly.IPSAssemblyResult}, which
 * may or may not be represented by the same underlying object. Note that if
 * multipage rendering is implemented, there could be multiple output results
 * for a single item.
 * <p>
 * If the item is created using {@link IPSAssemblyService#createAssemblyItem()}
 * then you must explicitely call {@link #normalize()} before assembly. Once
 * the item has been normalized, either though the explicit call or the
 * implicit call made by the deprecated creation call, the setters should not
 * be called.
 * 
 * @author dougrand
 */
public interface IPSAssemblyItem extends Cloneable, Serializable
{
   /**
    * The path is of the form
    * <code>//folder1/folder2/.../foldern/itemname</code> and may end in an
    * option <code>#revnumber</code>. If a revision is not specified, then
    * the current revision of the item is used. A path that only contains
    * numbers is taken as a direct reference to a particular content id, and may
    * also be modified by a revision number. Child items can be referenced below
    * the item as <code>.../folder/itemname/childname#nnn</code> where nnn is
    * the index of the particular child item. With revision it becomes
    * <code>.../itemname#revision/childname#nnn</code>
    * <p>
    * Items that are not in a folder may be referenced by a synthentic path that
    * starts with "/" instead of "//". The syntax is /cid#revision. For example
    * /301#1 will reference the first revision an item with content id 301.
    * <p>
    * The path may either have been passed in, or may be calculated from
    * parameter values. When calculated from the parameter values, the parameter
    * <code>sys_folderid</code> must be present to get a complete path. If the
    * parameter is missing, then the simplified "/nnn#rr" will be returned
    * instead.
    * 
    * @return the path, never <code>null</code> or empty
    */
   String getPath();

   /**
    * The content node being assembled. If the node is <code>null</code>
    * (normal case) then the assembly engine will load the node from the content
    * manager using the path. If the node is already loaded, as may be the case
    * for certain slot finders, the node will be used.
    * <p>
    * If this is called and the node is not set or loaded by the assembly
    * manager, the method will force the load, which will take care of code that
    * wishes to use the node after the item has been assembled by the legacy
    * plugin.
    * 
    * @return the node, may be <code>null</code> if not yet loaded
    */
   Node getNode();

   /**
    * If the node is loaded or set, then this method will return
    * <code>true</code> otherwise <code>false</code>. This method will not
    * cause the node to be loaded.
    * 
    * @return <code>true</code> if the node is loaded for the assembly item.
    */
   boolean hasNode();

   /**
    * This method allows the caller to change or set the node being referenced
    * by the assembly item. This is normally called internally to the assembly
    * service in order to set the node being processed, but can also be called
    * in conjunction with calling {@link Object#clone()}.
    * <p>
    * The valid use case for this is generally in slot processing, where the
    * slot content finder returns references to the slot contents. The source
    * assembly item is cloned and then the new node (and other new information)
    * is placed in the cloned assembly items.
    * 
    * @param node the node, may be <code>null</code> to reset the node. Also
    *           modifies the id stored. The id will be set to <code>null</code>
    *           for a <code>null</code> node
    */
   void setNode(Node node);

   /**
    * Get the item filter, which can be used to limit the results from various
    * slot finder and elsewhere. The item filter will be derived from the
    * authtype or filter parameters, as available.
    * 
    * @return Returns the filter, if <code>null</code> then no filtering will
    *         occur.
    * @throws PSFilterException if there was a filter specified in the
    *            parameters, but it was not found
    */
   IPSItemFilter getFilter() throws PSFilterException;

   /**
    * Parameters apply to the template and item to be assembled. The data in the
    * parameters is dependent on the template and assembler plugin that is used.
    * Coming from the assembly servlet, this map will also contain all passed
    * HTML parameters. Repeated parameters will have multiple values in the
    * string array.
    * 
    * @return a map of parameters, may be empty, but never <code>null</code>
    */
   Map<String, String[]> getParameters();

   /**
    * Get single parameter value.
    * 
    * @param name the name of the parameter, never <code>null</code> or empty
    * @param defaultvalue the default value to return if the parameter is not
    *           defined, may be null or empty
    * @return the value or default value for the given parameter name
    */
   String getParameterValue(String name, String defaultvalue);

   /**
    * Get parameter values.
    * 
    * @param name the name of the parameter, never <code>null</code> or empty
    * @param defaultvalues the default values to return if the parameter is not
    *           defined, may be <code>null</code> or empty
    * @return the values or default values for the given parameter name
    */
   String[] getParameterValues(String name, String defaultvalues[]);

   /**
    * The parameter is present in the set of parameters. It may or may not have
    * a value. Use this method to distinguish if a parameter is present but
    * empty, versus not present
    * 
    * @param name the name of the parameter, never <code>null</code> or empty
    * @return <code>true</code> if the parameter is present, regardless of the
    *         associated value
    */
   boolean hasParameter(String name);

   /**
    * Additional variables to bind when handling the template, may be
    * <code>null</code> or empty. Note that the contents of this entering the
    * assembly service will be the context variables that have been defined.
    * <P>
    * Any additional variables that are bound as part of the assembly process
    * will not be present in this map. See the bindings instead.
    * 
    * @return a map of variables, may be <code>null</code>
    */
   Map<String, String> getVariables();

   /**
    * Any bindings calculated by the assembly service can be retrieved via this
    * method. If no bindings are calculated, this will return <code>null</code>.
    * 
    * @return empty if there are no bindings, otherwise a set of
    *         named values. Each value may contain an object, a sub map or a
    *         list of values.
    */
   Map<String, Object> getBindings();

   /**
    * The template to use in the assembly. Set by the assembly engine before the
    * bindings process begins. Never set for legacy templates.
    * 
    * @return the template, may be <code>null</code>
    */
   IPSAssemblyTemplate getTemplate();

   /**
    * As an item is assembled, the value returned by {@link #getTemplate()} may
    * change. For example, after the inner content is assembled, the template is
    * changed to the global template before processing. Dispatch templates also
    * exhibit this behavior.
    * <p>
    * Some users may need to know what the original template was. This method
    * returns that id. This value is cleared when the item is cloned.
    * 
    * @return The id of the template first set on this work item, or the 
    * current template if this value is currently <code>null</code>. May be
    * <code>null</code> if a template is not set.
    */
   IPSGuid getOriginalTemplateGuid();
   
   /**
    * The reference id identifies a particular assembly request within a given
    * job. The reference id allows a caller to associate assembly results and
    * requests. While reference ids could be reused across preview requests,
    * they will never be repeated for publishing jobs.
    * 
    * @return the id, may be any value, but unique for a given job id
    */
   long getReferenceId();

   /**
    * Get the reference ID that the unpublishing was originated from.
    * @return the reference ID. It may be <code>null</code> if 
    *    {@link #isPublish()} returns <code>true</code>.
    */
   Long getUnpublishRefId();
   
   /**
    * The job id is unique per publishing run and helps a caller associate
    * results and requests for a given run. This id is also used internally to
    * determine if certain cached values can be reused, therefore this value
    * should be changed for each new assembly job, which includes new previews.
    * 
    * @return the job id, may be any value, but different for each run
    */
   long getJobId();

   /**
    * Get the guid for the item specified.
    * 
    * @return the guid from the item specified by the path or parameters. Will
    *         never return <code>null</code> for a valid assembly item.
    *         Calling this method will derive the id from either the
    *         sys_contentid and sys_revision parameters or the path.
    */
   IPSGuid getId();

   /**
    * Get the site id if defined for the assembly item. The site id is extracted
    * from the site id http parameter. Note that if the item is created for a
    * slot, then the site id may be the site id of the referenced site rather 
    * than the original site.
    * 
    * @return the site id, or <code>null</code> if the parameter is missing
    */
   IPSGuid getSiteId();

   /**
    * Get the folder content id if defined for the assembly item. The folder id
    * may be extracted from the folder http parameter or from the path.
    * 
    * @return the folder content id, or <code>0</code> if the folder is not
    *         defined
    */
   int getFolderId();

   /**
    * Indicates if the item should be assembled in debug mode. Debug mode
    * outputs information about the assembly, but doesn't run the plug in. This
    * is inherited by cloned items.
    * 
    * @return <code>true</code> for an item to be debugged.
    */
   boolean isDebug();

   /**
    * Indicates if this assembly is for a publishing or unpublishing case. This
    * will return <code>true</code> for publishing or previews. The value
    * <code>false</code> can be used by the assembly plugin to short circuit
    * processing when appropriate.
    * 
    * @return <code>false</code> for unpublishing
    */
   boolean isPublish();

   /**
    * Get the user name set. The user name informs preview and active assembly
    * who the user is, which allows assembly to display the appropriate versions
    * of items in preview.
    * @return the user name, may be <code>null</code> but never empty.
    */
   String getUserName();
   
   /**
    * As a specific item is assembled, the implementation often will clone the
    * item in order to handle some contained item such as slot content. When
    * this happens, this method allows the implementation to find out what the
    * <q>parent</q> assembly item is. This is set as part of the
    * <code>clone()</code> method.
    * 
    * @return either <code>null</code> for an item that was constructed, or
    * the parent assembly item for an assembly item that was cloned.
    */
   IPSAssemblyItem getCloneParentItem();
   
   /**
    * If this is for a paged item's page, then this will return the page
    * number. Page numbers are <code>1</code> based. If the item contains
    * a page number, it will also contain a parent reference id.
    * 
    * @return the page number or <code>null</code> if there is no page number.
    */
   Integer getPage();
   
   /**
    * Set the page number
    * @param page the page number, may be <code>null</code> 
    */
   void setPage(Integer page);
   
   /**
    * If this is for a paged item's page, then this will return the 
    * original paginated page's reference id so that status update can mark
    * the parent as failed if the child page fails. This is also used so the
    * assembly system knows whether the page should be evaluated for pagination.
    * 
    * @return the parent reference id or <code>null</code> if this is not
    * a page child item.
    */
   Long getParentPageReferenceId();
   
   /**
    * Set the parent reference id
    * @param refid the parent reference id, may be <code>null</code>.
    */
   void setParentPageReferenceId(long refid);
   
   /**
    * Set a new folder id, see {@link #getFolderId()}
    * 
    * @param folderId the new folder id
    */
   void setFolderId(int folderId);

   /**
    * Gets the owner ID of the assembled item.
    * @return the ID. It may be <code>null</code> if unknown.
    */
   IPSGuid getOwnerId();
   
   /**
    * Sets the owner ID of the assembled item.
    * @param ownerId the owner ID. It may be <code>null</code> if unknown.
    */
   void setOwnerId(IPSGuid ownerId);
   
   /**
    * Set a new site id, see {@link #getSiteId()}
    * 
    * @param siteid the new site id
    */
   void setSiteId(IPSGuid siteid);
   
   
   /**
    * Set the publishing server id to use with the delivery item.
    * @param pubserverid the ID of the publishing server.
    * It may be <code>null</code> if the publish-server is unknown.
    */
   void setPubServerId(Long pubserverid);
   
   /**
    * Get the publishing server id that is used for this item.
    * @return publishing server id. It is <code>null</code> if the publish-server is unknown.
    */
   Long getPubServerId();
      
   /**
    * Set new bindings, see {@link #getBindings()}
    * 
    * @param bindings The bindings to set, may be <code>null</code>
    */
   void setBindings(Map<String, Object> bindings);

   /**
    * Set a new id for the referenced content item, see {@link #getId()}
    * 
    * @param id The id to set, may be <code>null</code>
    */
   void setId(IPSGuid id);

   /**
    * Set the new parameters, see {@link #getParameters()}
    * 
    * @param parameters The parameters to set, if <code>null</code> then the
    *           parameters are defaulted to an empty map
    */
   void setParameters(Map<String, String[]> parameters);

   /**
    * Put single value into parameter map, replacing any current value for the
    * named parameter
    * 
    * @param name the parameter name, never <code>null</code> or empty
    * @param value the value, never <code>null</code> or empty
    * @fixme This needs to be reviewed as why empty values needs to be avoided.
    */
   void setParameterValue(String name, String value);
   
   /**
    * Remove any value for the given parameter name
    * 
    * @param name the parameter name, never <code>null</code> or empty
    */
   public void removeParameterValue(String name);

   /**
    * Set a new path, may be <code>null</code>
    * 
    * @param path The path to set, see {@link #getPath()}
    */
   void setPath(String path);

   /**
    * Set a reference id
    * 
    * @param referenceId The referenceId to set, see {@link #getReferenceId()}
    */
   void setReferenceId(long referenceId);
   
   /**
    * Set a new job id
    * 
    * @param jobId The jobId to set, see {@link #getJobId()}.
    */
   public void setJobId(long jobId);
   
   /**
    * Set the publish state. A state of <code>false</code> means that this
    * assembly item is for an unpublish. See {@link #isPublish()} for details.
    * 
    * @param pub the new state
    */
   public void setPublish(boolean pub);

   /**
    * @param variables The variables to set, see {@link #getVariables()}
    */
   void setVariables(Map<String, String> variables);

   /**
    * @param template The template to set, see {@link #getTemplate()}
    */
   void setTemplate(IPSAssemblyTemplate template);

   /**
    * Call this to enable using the edit revision if the item is checked out to
    * the user, and otherwise to use the current revision. Note that if this is 
    * set, the node returned from {@link #getNode()} may not match a node set in
    * {@link #setNode(Node)} as the code in these routines will check the version
    * and replace the node in use if it is not the current or edit reivision.
    * @param userName the user name, may be <code>null</code> but not empty
    */
   void setUserName(String userName);

   /**
    * The item filter, see {@link #getFilter()}
    * 
    * @param filter The filter to set.
    */
   void setFilter(IPSItemFilter filter);

   /**
    * Set if this item is in debug mode. Debug mode means that instead of the
    * assembler set on the template, the debug assembler will be used instead.
    * The debug assembler shows bindings and other information in a friendly
    * fashion for debugging an implementation.
    * 
    * @param isDebug <code>true</code> if it is in debug mode
    */
   void setDebug(boolean isDebug);
   
   /**
    * Handle the dicotomy between the path and sys_contentid, sys_revision and
    * sys_folderid parameters. If the item is created with a path, the three
    * parameters will be filled out (note, no folder id if the item isn't in a
    * folder though). If the item is created with the three parameters, the path
    * will be filled out. In addition the guid of the content item will be
    * calculated and stored.
    * <p>
    * This method must be called before the item is assembled.
    * 
    * @throws PSAssemblyException if the item is invalid.
    */
   void normalize() throws PSAssemblyException;
   
   /**
    * Create a clone of the assembly item
    * @return the clone, never <code>null</code>.
    */
   Object clone();

   /**
    * Create a clone for use in pagination. For this purpose, the cloned items
    * must be sanitized to not contain all the information that would normally
    * be present for two reasons:
    * <ul>
    * <li>The extra data adds to the cost of serializing and posting the item 
    * to the JMS queue
    * <li>Not all the information associated with the work item can be 
    * serialized
    * </ul>
    * @return a pagination clone, very similar to a normal clone.
    */
   @SuppressWarnings("unchecked")
   IPSAssemblyItem pageClone();
   
   
   /**
    * Get the assembly url.
    * @return the assembly url, set by the publishing system. May be 
    * <code>null</code> for some work items.
    */
   public String getAssemblyUrl();
   
   /**
    * Get the delivery context ID as an integer.
    * @return the delivery context, the context used to calculate the delivery
    * location.
    */
   public int getDeliveryContext();
   
   /**
    * Set the delivery path
    * 
    * @param deliveryPath the deliveryPath to set, may be <code>null</code>
    * or empty.
    */
   public void setDeliveryPath(String deliveryPath);

   /**
    * Set delivery type.
    * 
    * @param deliveryType the deliveryType to set, may not be 
    * <code>null</code> or empty.
    */
   public void setDeliveryType(String deliveryType);
 
   /**
    * Set the assembly url.
    * @param assemblyUrl the assembly url, may be <code>null</code> or empty.
    */
   public void setAssemblyUrl(String assemblyUrl);
   
   
   /**
    * Set the delivery context
    * 
    * @param context the context being used for the delivery location
    */
   public void setDeliveryContext(int context);

   /**
    * Set the reference ID that the unpublishing was originated from.
    * @param unpubRefId the reference ID.
    */
   public void setUnpublishRefId(Long unpubRefId);
   
   /**
    * Get the delivery path. The delivery path is really only relevant for
    * the file based publisher plug ins.
    * 
    * @return the path, may be <code>null</code> or empty.
    */
   String getDeliveryPath();

   /**
    * Get the delivery type information. A delivery type is the name of a 
    * delivery handler. Delivery types are configured through the system's
    * publishing design UI. Each handler can have more than one type associated
    * with it, configuring the handler for a specific use. 
    * <p>
    * The delivery type name is interpreted by the delivery manager to determine
    * what delivery handler Spring bean to use.
    * 
    * @return the delivery type, never <code>null</code> or empty.
    */
   String getDeliveryType();
   
   /**
    * Get the time that it took for this work item to be assembled. Set by 
    * the assembly system. Not valid before assembly.
    * @return the time in milliseconds.
    */
   public int getElapsed();

   /**
    * Sets the status of the result.
    * 
    * @param status The status to set.
    */
   public void setStatus(Status status);

   /**
    * Set the time in milliseconds for the assembly.
    * @param elapsed the time in milliseconds.
    */
   public void setElapsed(int elapsed);
   
   /**
    * Store the result data for the assembly. The result data will be stored
    * in a {@link PSPurgableTempFile} if it is larger than 
    * <code>THRESHOLD</code> bytes in size. Smaller result data is simply stored
    * in memory. 
    * <p>
    * Call either this method or {@link #setResultStream(InputStream)}, but not 
    * both. Calling this method will clear any previously stored result data, 
    * either in memory or the file system.
    * 
    * @param resultData The resultData to set. Once this method has been called
    * the passed array is owned by the work item and should not be further 
    * modified. Conversely, there is no guarantee that modifications would
    * be propagated given the possible storage of the data in the file system.
    */
   public void setResultData(byte[] resultData);

   /**
    * Get the delivery context ID of the item.
    * @return the GUID of the context ID, never <code>null</code>.
    */
   public IPSGuid getDeliveryContextId();

   /**
    * Set a new mimetype
    * 
    * @param mimeType The mimeType to set, may be <code>null</code>
    */
   public void setMimeType(String mimeType);
   

   /**
    * The work item holds a navigation helper, which contains information that
    * can be used while assembling any part of an item, either the page, global
    * page or snippets. The reference is cloned to subordinate requests.
    * 
    * @return the nav helper, never <code>null</code>
    */
   public PSNavHelper getNavHelper();
   
   /**
    * Store result data. The result data will be stored in a temporary file
    * in the file system using a {@link PSPurgableTempFile}. 
    * <p>
    * Call either this method or {@link #setResultData(byte[])}, but not both.
    * Calling this method will clear any previously stored result data, either
    * in memory or the file system.
    * 
    * @param is the result data stream, may be null. The input stream should
    * be closed by the caller.
    * 
    * @throws IOException 
    */
   public void setResultStream(InputStream is) throws IOException;
   
   /**
    * Get the assembly context based on the parameters
    * 
    * @return the context value, or <code>-1</code> if the parameter cannot be
    *         found.
    */
   public int getContext();

   /**
    * This method is provided so assemblers can help manage the originating
    * template id. Generally, this method does not need to be called, but there
    * are special circumstances where this class cannot properly manage this
    * value. For example, the velocity assembler clones the item before
    * assembling the global template, which would clear this value. However, it
    * should not be cleared in that case. The velocity assembler should call
    * this method after cloning to reset this value to its state before the
    * clone.
    */
   public void setOriginalTemplateGuid(IPSGuid g);
   
   /**
    * Remove any value for the given parameter name
    * 
    * @param name the parameter name, never <code>null</code> or empty
    */
   public void removeParameter(String name);
   
   /**
    * Set the paginated state
    * @param value <code>true</code> if paginated.
    */
   public void setPaginated(boolean value);

}
