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
package com.percussion.services.assembly.impl.plugin;

import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.services.assembly.IPSAssembler;
import com.percussion.services.assembly.IPSAssemblyErrors;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.assembly.IPSAssemblyResult;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSSlotContentFinder;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.assembly.IPSAssemblyResult.Status;
import com.percussion.services.assembly.impl.PSAssemblyJexlEvaluator;
import com.percussion.services.assembly.impl.PSAssemblyService;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSUrlUtils;
import com.percussion.utils.exceptions.PSExceptionHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jexl.PSJexlEvaluator;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.PropertyDefinition;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The debug assembler outputs text/html that has the input binding data,
 * associated slots and information about the passed content item. This assembly
 * is never selected directly in the workbench - instead it is implicitly chosen
 * for the user if the assembler/debug path is used to invoke the assembly
 * engine. The output of the debug assembler helps to understand what data is
 * available to the target assembly plugin.
 * 
 * @author dougrand
 * 
 */
public class PSDebugAssembler implements IPSAssembler, IPSExtension
{
   /**
    * Logger for the debug assembler
    */
   private static Log ms_log = LogFactory.getLog(PSDebugAssembler.class);

   /**
    * Counter used to create ids for the output document. The ids are used for
    * javascript to control the visibility of the output sections.
    */
   private static AtomicLong ms_counter = new AtomicLong(1);

   /**
    * Outputs some data, changing MDO and MDC characters to entities. It
    * recognizes and trims leading whitespace and limits the output data to a
    * set length.
    * 
    * @param pw the printwriter, never <code>null</code>
    * @param data the data, may be <code>null</code> or empty
    */
   protected void showOutput(PrintWriter pw, String data)
   {
      if (pw == null)
      {
         throw new IllegalArgumentException("pw may not be null");
      }
      if (StringUtils.isBlank(data))
      {
         pw.print("[[empty]]");
      }
      else
      {
         if (Character.isWhitespace(data.charAt(0)))
         {
            data = data.trim();
            data = "[[leading whitespace]]" + data;
         }
         // Fix any HTML or XML in the output
         data = data.replace("<", "&lt;").replace(">", "&gt;");
         String abbr = StringUtils.abbreviate(data, 250);
         pw.print(abbr);
      }
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.services.assembly.IPSAssembler#assemble(java.util.List)
    */
   public List<IPSAssemblyResult> assemble(List<IPSAssemblyItem> items)
   {
      List<IPSAssemblyResult> results = new ArrayList<IPSAssemblyResult>();
      for (IPSAssemblyItem item : items)
      {
         try
         {
            results.add(assembleSingle(item));
         }
         catch (Exception e)
         {
            ms_log.error("Problem during assembly", e);
            item.setStatus(Status.FAILURE);
            if (e.getLocalizedMessage() != null)
            {
               item.setResultData(e.getLocalizedMessage().getBytes());
            }
            else
            {
               item.setResultData(e.getClass().getName().getBytes());
            }
            item.setMimeType("text/plain");
         }
      }

      return results;
   }
   
   public void preProcessItemBinding(IPSAssemblyItem item, PSAssemblyJexlEvaluator eval) throws PSAssemblyException
   {
      // do nothing by default
   }


   /**
    * Assemble one item
    * 
    * @param item the item, assumed not <code>null</code>
    * @return a result, never <code>null</code>
    * @throws Exception
    */
   private IPSAssemblyResult assembleSingle(IPSAssemblyItem item)
         throws Exception
   {
      Writer w = new StringWriter(2000);
      PrintWriter pw = new PrintWriter(w);

      outputHeader(pw, item.getId());
      pw.println("<body>");

      PSJexlEvaluator eval = new PSJexlEvaluator(item.getBindings());
      Object templ = eval.evaluate(PSJexlEvaluator
            .createExpression("$sys.template"));
      if (templ != null
            && item.getTemplate().getAssembler().endsWith("dispatchAssembler"))
      {
         Map<String, String> params = new HashMap<String, String>();
         String template = templ.toString();
         params.put(IPSHtmlParameters.SYS_TEMPLATE, template);
         if (item.getFolderId() > 0)
         {
            params.put(IPSHtmlParameters.SYS_FOLDERID, Integer.toString(item
                  .getFolderId()));
         }
         if (item.getSiteId() != null)
         {
            params.put(IPSHtmlParameters.SYS_SITEID, Long.toString(item
                  .getSiteId().longValue()));
         }
         params.put(IPSHtmlParameters.SYS_ITEMFILTER, item.getFilter()
               .getName());
         PSLegacyGuid lg = (PSLegacyGuid) item.getId();
         params.put(IPSHtmlParameters.SYS_CONTENTID, Integer.toString(lg
               .getContentId()));
         params.put(IPSHtmlParameters.SYS_REVISION, Integer.toString(lg
               .getRevision()));
         params.put(IPSHtmlParameters.SYS_CONTEXT, item.getParameterValue(
               IPSHtmlParameters.SYS_CONTEXT, "0"));
         String url = PSUrlUtils.createUrl("./debug", params.entrySet()
               .iterator(), null);
         pw.print("<p><a href='");
         pw.print(url);
         pw.print("'>Click here to view template ");
         pw.print(template);
         pw.print("'s bindings</a>");
      }

      outputNavTree(item, pw);
      outputBindingErrors(pw, item.getBindings());
      outputMap("Bindings", pw, item.getBindings());
      outputSlots(pw, item);
      pw.println("</body>\n</html>");
      pw.close();
      w.close();
      item.setResultData(w.toString().getBytes());
      item.setMimeType("text/html; charset=utf-8");
      item.setStatus(Status.SUCCESS);
      return (IPSAssemblyResult) item;
   }

   /**
    * 
    * @param item
    * @param pw
    * @throws Exception
    */
   private void outputNavTree(IPSAssemblyItem item, PrintWriter pw)
         throws Exception
   {
      // Check to see if this is a navigation node
      PSJexlEvaluator eval = new PSJexlEvaluator(item.getBindings());
      Node root = (Node) eval.evaluate(PSJexlEvaluator
            .createExpression("$nav.root"));
      if (root != null)
      {
         pw.println("<div><span class='heading'>Nav tree</span>");
         outputNavNode(pw, root);
         pw.println("</div>");
      }
   }

   /**
    * Output the navigation node, and it's subnodes
    * 
    * @param pw the printwriter, assumed not null
    * @param navNode the current node, assumed not null
    * @throws RepositoryException
    * @throws UnsupportedRepositoryOperationException
    * @throws ValueFormatException
    * @throws PathNotFoundException
    */
   private void outputNavNode(PrintWriter pw, Node navNode)
         throws RepositoryException, UnsupportedRepositoryOperationException,
         ValueFormatException, PathNotFoundException
   {
      NodeIterator niter = navNode.getNodes("nav:submenu");

      if (niter.hasNext())
      {
         pw.println("<span class='heading'>Submenu:</span>");
      }
      while (niter.hasNext())
      {
         Node n = niter.nextNode();
         StringBuilder b = new StringBuilder();
         b.append("<span class='node'>");
         b.append(n.getName());
         b.append(" (");
         b.append(n.getUUID());
         b.append(") '");
         b.append(n.getProperty("nav:axis").getString());
         b.append("'</span><br/>");
         outputToggler(pw, b.toString(), true);
         outputItem(pw, n, true);
         outputNavNode(pw, n);
         outputEndToggler(pw);
      }
      niter = navNode.getNodes("nav:image");
      if (niter.hasNext())
      {
         pw.println("<span class='heading'>Images:</span>");
      }
      while (niter.hasNext())
      {
         Node n = niter.nextNode();
         StringBuilder b = new StringBuilder();
         b.append("<span class='node'>");
         b.append(n.getName());
         b.append(" (");
         b.append(n.getUUID());
         b.append(")</span><br/>");
         outputToggler(pw, b.toString(), true);
         outputItem(pw, n, false);
         outputEndToggler(pw);
      }
   }

   /**
    * Output errors recorded in the bindings. As the bindings are evaluated,
    * errors are stored in a special variable. This method does nothing if no
    * errors were recorded.
    * 
    * @param pw the print writer for output, assumed not <code>null</code>
    * @param bindings bindings, assumed not <code>null</code>
    */
   @SuppressWarnings("unchecked")
   private void outputBindingErrors(PrintWriter pw, Map<String, Object> bindings)
   {
      Map<String, Throwable> problems = (Map<String, Throwable>) bindings
            .get(PSAssemblyService.ERROR_VAR);
      if (problems != null)
      {
         pw.append("<h2>Problems during binding evaluation</h2>");
         pw.append("<div>");

         boolean first = true;
         for (String varname : problems.keySet())
         {
            if (!first)
            {
               pw.append("<hr>");
            }
            first = false;
            Throwable th = problems.get(varname);
            th = PSExceptionHelper.findRootCause(th, true);
            pw.append("<span class='var'>");
            pw.append(varname);
            pw.append("</span>=<span class='expression'>");
            pw.append(th.getLocalizedMessage());
            pw.append("</span><br/>");
         }
         pw.append("</div>");
      }
   }

   /**
    * Output slot information for the assembly item. This walks the associated
    * slots for the item's template and calls the content finder. Since this is
    * used outside of a template, the output may be incomplete or incorrect as
    * the template can override parameters for the finder.
    * 
    * @param pw the printwriter, assumed never <code>null</code>
    * @param item the assembly item, assumed never <code>null</code>
    * @throws PSAssemblyException
    */
   private void outputSlots(PrintWriter pw, IPSAssemblyItem item)
         throws PSAssemblyException
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();
      IPSAssemblyTemplate t = item.getTemplate();
      if (t.getSlots().size() == 0)
         return;

      pw.println("<h3>Slots, warning - finders called without parameters. "
            + "Results in assembled output may vary</h3>");
      pw.println("<div>");
      for (IPSTemplateSlot slot : t.getSlots())
      {
         outputToggler(pw, "<span class='slot'>" + slot.getName() + "</span>",
               true);
         pw.println("<table border='0'>");

         // Get and run each finder, and show the contents of the slot
         String findername = slot.getFinderName();
         if (findername == null)
         {
            ms_log.warn("No finder defined for slot " + slot.getName()
                  + " defaulting to sys_RelationshipContentFinder");
            findername = "Java/global/percussion/slotcontentfinder/sys_RelationshipContentFinder";
         }
         IPSSlotContentFinder finder = asm.loadFinder(findername);
         if (finder == null)
            throw new PSAssemblyException(IPSAssemblyErrors.MISSING_FINDER,
                  finder);
         try
         {
            List<IPSAssemblyItem> relitems = finder.find(item, slot,
                  new HashMap<String, Object>());
            pw.println("<tr><th>Id</th><th>Title</th><th>Template</th></tr>");
            List<IPSGuid> ids = new ArrayList<IPSGuid>();
            for (IPSAssemblyItem ritem : relitems)
            {
               ids.clear();
               ids.add(ritem.getId());
               List<Node> nodes = cmgr.findItemsByGUID(ids, null);
               Node node = nodes.get(0);
               pw.print("<tr><td class='cell'>");
               pw.print(node.getUUID());
               pw.print("</td><td class='cell'>");
               pw.print(node.getProperty("rx:sys_title").getString());
               pw.print("</td><td class='cell'>");
               if (ritem.getTemplate() != null)
               {
                  pw.print(ritem.getTemplate().getLabel());
               }
               pw.println("</td></tr>");
            }
         }
         catch (Exception e)
         {
            pw.println("<tr><th>Problem while getting slot contents for ");
            pw.println(slot.getName());
            pw.println("</th></tr>");
            pw.println("<tr><td>");
            pw.println(e.getLocalizedMessage());
            pw.println("</td></tr>");
         }
         finally
         {
            pw.println("</table>");
            outputEndToggler(pw);
            pw.println("<br/>");
         }
      }
      pw.println("</div>");
   }

   /**
    * Generate a toggle span followed by a div
    * 
    * @param pw the print writer used for the output, assumed never 
    *   <code>null</code>
    * @param tag the name of the thing being output, assumed never 
    *   <code>null</code> or empty 
    * @param hide if this is <code>true</code> then the block should start
    *   hidden.
    */
   private void outputToggler(PrintWriter pw, String tag, boolean hide)
   {
      long id = ms_counter.addAndGet(1);

      pw.print("<span class='toggle' onClick='toggle(this,\"");
      pw.print("id_" + id);
      if (hide)
      {
         pw.print("\")'>show</span>&nbsp;");
      }
      else
      {
         pw.print("\")'>hide</span>&nbsp;");
      }
      pw.print(tag);
      pw.print("<div id='id_" + id);
      if (hide)
         pw.print("' style='display: none'>");
      else
         pw.print("' style='display: block'>");
   }

   /**
    * End the toggled div
    * 
    * @param pw
    */
   private void outputEndToggler(PrintWriter pw)
   {
      pw.println("</div>");
   }

   /**
    * Output the data associated with the content item itself.
    * 
    * @param pw the printwriter, assumed never <code>null</code>
    * @param node the data node, assumed never <code>null</code>
    * @param toggleprops if <code>true</code> then put the properties in a div
    *           that starts out hidden. Used to display the nav tree without
    *           properties.
    * 
    * @throws RepositoryException
    */
   private void outputItem(PrintWriter pw, Node node, boolean toggleprops)
         throws RepositoryException
   {
      if (toggleprops)
         outputToggler(pw, "<b>Properties:</b>", true);
      PropertyIterator iter = node.getProperties();
      Set<String> alphabetized = new TreeSet<String>();
      while (iter.hasNext())
      {
         Property p = iter.nextProperty();
         if (p == null) continue;
         alphabetized.add(p.getName());
      }
      for(String pname : alphabetized)
      {
         Property p = node.getProperty(pname);
         pw.print("<span class='var'>");
         pw.print(p.getName());
         pw.print(" [javax.jcr.Property of type: ");
         pw.print(PropertyType.nameFromValue(p.getType()));
         pw.print("]</span><span class='value'>");
         PropertyDefinition pd = null;
         try
         {
            pd = p.getDefinition();
         }
         catch(PathNotFoundException e)
         {
            // Ignore, this is some pseudo property
         }
         if (pd != null && pd.isMultiple())
         {
            Value v[] = p.getValues();
            for (int i = 0; i < v.length; i++)
            {
               if (i > 0)
               {
                  pw.print(", ");
               }
               showOutput(pw, v[i].getString());
            }
         }
         else
         {
            showOutput(pw, p.getString());
         }
         pw.println("</span>");
         if (iter.hasNext())
            pw.println("<hr/>");
      }
      NodeIterator niter = node.getNodes();
      boolean close = false;
      if (niter.hasNext())
      {
         close = true;
         outputToggler(pw, "<div><h3>Children:</h3>", true);
      }
      while (niter.hasNext())
      {
         Node n = niter.nextNode();
         pw.print("<span class='var'>");
         pw.print(n.getName());
         pw.print("</span>");
         outputItem(pw, n, true);
      }
      if (close)
      {
         outputEndToggler(pw);
         pw.println("</div>");
      }

      if (toggleprops)
         outputEndToggler(pw);
   }

   /**
    * Primitive that outputs a map's data with a title
    * 
    * @param title the title, assumed not <code>null</code>
    * @param pw the print writer, assumed never <code>null</code>
    * @param data the map data, may be <code>null</code> or empty
    */
   @SuppressWarnings("unchecked")
   private void outputMap(String title, PrintWriter pw, Map data)
   {
      pw.print("<h2>" + title + "</h2>");
      outputMapNoHeader(pw, data);
   }

   /**
    * Output the map data without a header. Called from
    * {@link #outputMap(String, PrintWriter, Map)} to do the body of the work.
    * 
    * @param pw the print writer, assumed never <code>null</code>
    * @param data the map data, may be <code>null</code> or empty
    */
   @SuppressWarnings("unchecked")
   private void outputMapNoHeader(PrintWriter pw, Map data)
   {
      if (data == null)
      {
         pw.println("[[empty map]]");
         return;
      }
      Iterator<Map.Entry> iter = data.entrySet().iterator();
      while (iter.hasNext())
      {
         Map.Entry p = iter.next();

         String name = p.getKey().toString();
         Object value = p.getValue();
         if (name.equals(PSAssemblyService.ERROR_VAR))
            continue; // Skip errors here
         boolean collapse = name.equals("$nav") || name.equals("$rx")
               || name.equals("$user") || name.equals("$tools");

         String header = "<span class='var'>" + name + "</span>";
         if (value != null && !(value instanceof Map))
         {
            StringBuilder b = new StringBuilder();
            b.append("<span class='var'>");
            b.append(name);
            b.append(" [");
            String iname = null;
            for (Class c : value.getClass().getInterfaces())
            {
               // Find the first interface (if any) that isn't a java.lang
               // marker
               String cname = c.getCanonicalName();
               if (!cname.startsWith("java.lang.")
                     && !cname.startsWith("java.io."))
               {
                  iname = cname;
                  break;
               }
            }
            if (StringUtils.isBlank(iname))
            {
               iname = value.getClass().getCanonicalName();
            }
            b.append(iname);
            b.append("]</span>");
            header = b.toString();
         }
         if (collapse)
            outputToggler(pw, header, true);
         else
            pw.println(header);

         if (value instanceof Map)
         {
            if (!collapse)
               pw.println("<div>");
            outputMapNoHeader(pw, (Map<String, Object>) value);
            if (!collapse)
               pw.println("</div>");
         }
         else if (value instanceof Node)
         {
            try
            {
               pw.println("<div>");
               outputItem(pw, (Node) value, false);
               pw.println("</div>");
            }
            catch (RepositoryException e)
            {
               pw.println("Error: " + e.getLocalizedMessage());
            }
         }
         else
         {
            pw.print("<span class='value'>");
            if (value == null)
               pw.print("[[null]]");
            else if (value instanceof Object[])
            {
               Object[] values = (Object[]) value;
               pw.print("[");
               for (int i = 0; i < values.length; i++)
               {
                  showOutput(pw, values[i].toString());
                  if ((values.length - i) > 1)
                  {
                     pw.print(",");
                  }
               }
               pw.print("]");
            }
            else
            {
               showOutput(pw, value.toString());
            }
            pw.print("</span>");
         }
         if (collapse)
            outputEndToggler(pw);
         if (iter.hasNext())
            pw.println("<hr/>");
      }
   }

   /**
    * Output the page header
    * 
    * @param pw the printwriter, assumed never <code>null</code>
    * @param guid the id of the item, assumed not <code>null</code>
    */
   private void outputHeader(PrintWriter pw, IPSGuid guid)
   {
      pw.println("<html><head>");
      pw.println("<title>Debug assembly output for item: ");
      pw.println(guid.toString());
      pw.println("</title>");
      pw.print("<script type='text/javascript' src='");
      pw.print("../sys_resources/js/assembly.js");
      pw.println("'>;</script>");
      pw.print("<link rel='stylesheet' type='text/css' href='");
      pw.print("../sys_resources/css/assembly.css");
      pw.println("'/>");
      pw.println("</head>");
   }

   /**
    * (non-Javadoc)
    * 
    * @see com.percussion.extension.IPSExtension#init(com.percussion.extension.IPSExtensionDef,
    *      java.io.File)
    */
   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
         throws PSExtensionException
   {
      // No initialization required
   }

}
