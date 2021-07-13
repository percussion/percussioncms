/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.taxonomy.web;

import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.taxonomy.TaxonomySecurityHelper;
import com.percussion.taxonomy.domain.Attribute;
import com.percussion.taxonomy.domain.Attribute_lang;
import com.percussion.taxonomy.domain.Language;
import com.percussion.taxonomy.domain.Node;
import com.percussion.taxonomy.domain.Node_status;
import com.percussion.taxonomy.domain.Taxonomy;
import com.percussion.taxonomy.domain.Value;
import com.percussion.taxonomy.domain.Visibility;
import com.percussion.taxonomy.service.AttributeService;
import com.percussion.taxonomy.service.Attribute_langService;
import com.percussion.taxonomy.service.LanguageService;
import com.percussion.taxonomy.service.NodeService;
import com.percussion.taxonomy.service.Node_statusService;
import com.percussion.taxonomy.service.TaxonomyService;
import com.percussion.taxonomy.service.ValueService;
import com.percussion.taxonomy.service.VisibilityService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.Validate.notEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;

@Controller
public class TaxonomyController extends AbstractControllerWithSecurityChecks
{

    protected final Logger logger = LogManager.getLogger(getClass());

    protected TaxonomyService taxonomyService;

    protected LanguageService languageService;

    protected VisibilityService visibilityService;

    protected AttributeService attributeService;

    protected Attribute_langService attribute_langService;

    protected Node_statusService node_statusService;

    protected NodeService nodeService;

    protected ValueService valueService;

    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        TaxonParams tp = new TaxonParams(request,taxonomyService);
        Map<String, Object> myModel = new HashMap<String, Object>();

        Collection<Taxonomy> all = taxonomyService.getAllTaxonomys();

        myModel.put("taxonomys", all);
        myModel.put("langID", languageService.getLanguage(tp.getLangID()).getId());
        myModel.put("permissionsError", !TaxonomySecurityHelper.amITaxonomyAdmin());

        return new ModelAndView("taxonomy/index", "model", myModel);
    }

    public ModelAndView new_taxonomy(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        TaxonomySecurityHelper.raise_error_if_cannot_admin();
        TaxonParams tp = new TaxonParams(request,taxonomyService);
        Map<String, Object> myModel = new HashMap<String, Object>();

        myModel.put("langID", tp.getLangID());
        return new ModelAndView("taxonomy/new", "model", myModel);
    }

    public ModelAndView create(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        TaxonomySecurityHelper.raise_error_if_cannot_admin();
        String taxonomy_name = request.getParameter("taxonomy_name");
        String attr_name = request.getParameter("attr_name");
        String node_value = request.getParameter("node_value");
        String relate_taxon = request.getParameter("relate_taxon");
        boolean is_relate_taxon = StringUtils.isEmpty(relate_taxon) || relate_taxon.equalsIgnoreCase("yes");
        // Check uniqueness taxonomy name.
        if (taxonomyService.doesTaxonomyExists(taxonomy_name))
        {
            Map<String, Object> myModel = new HashMap<String, Object>();
            myModel.put("taxonomy_name", taxonomy_name);
            myModel.put("attr_name", attr_name);
            myModel.put("node_value", node_value);
            myModel.put("errorMessage", "Cannot create an taxonomy with name '" + taxonomy_name
                    + "' because an taxonomy with the same name already exists.");

            return new ModelAndView("taxonomy/new", "model", myModel);
        }

        TaxonParams tp = new TaxonParams(request,taxonomyService);

        // save taxonomy
        Taxonomy tax = new Taxonomy();
        tax.setName(taxonomy_name);
      tax.setHas_related_ui(is_relate_taxon);
        tax.setScheme("n/a");
        tax.setAdmin_role_id(1);
        taxonomyService.saveTaxonomy(tax);

        Language language = languageService.getLanguage(tp.getLangID());

        // save attribute
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        Attribute attr = new Attribute();
        attr.setTaxonomy(tax);
        attr.setIs_multiple(false);
        attr.setIs_node_name(1);
        attr.setIs_required(true);
        attr.setIs_percussion_item(false);
        attr.setCreated_at(ts);
        attr.setCreated_by_id(getUserName(request));
        attr.setModified_at(ts);
        attr.setModified_by_id(getUserName(request));
        attributeService.saveAttribute(attr);

        // save attribute lang
        Attribute_lang attr_lang = new Attribute_lang();
        attr_lang.setAttribute(attr);
        attr_lang.setLanguage(language);
        attr_lang.setName(attr_name);
        attribute_langService.saveAttribute_lang(attr_lang);

        // save node
        Node node = new Node();
        node.setTaxonomy(tax);
        node.setNode_status(node_statusService.getNode_status(Node_status.ACTIVE));
        node.setCreated_at(ts);
        node.setCreated_by_id(getUserName(request));
        node.setModified_at(ts);
        node.setModified_by_id(getUserName(request));
        nodeService.saveNode(node);

        // save node value
        Value value = new Value();
        value.setAttribute(attr);
        value.setLang(language);
        value.setName(node_value);
        value.setNode(node);
        value.setCreated_at(ts);
        value.setCreated_by_id(getUserName(request));
        valueService.saveValue(value);

        return new ModelAndView("redirect:taxonomy.htm?action=edit&taxID=" + tax.getId() + "&langID=" + tp.getLangID()
                + "&dup=0");
    }

    public boolean isUniqueAttributeName(Taxonomy tax, String attr_name)
    {
        for (Attribute attr : tax.getAttributes())
        {
            Collection<Attribute_lang> names = attr.getAttribute_langs();
            if (names == null || names.isEmpty())
                continue;

            String name = names.iterator().next().getName();
            if (attr_name.equalsIgnoreCase(name))
                return false;
        }

        return true;
    }

    public ModelAndView new_attribute(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        TaxonomySecurityHelper.raise_error_if_cannot_admin();
        String attr_name = request.getParameter("attr_name");
        TaxonParams tp = new TaxonParams(request,taxonomyService);
        Taxonomy tax = taxonomyService.getTaxonomy(tp.getTaxID());

        // Check uniqueness attribute name.
        if (!isUniqueAttributeName(tax, attr_name))
        {
            Map<String, Object> messages = new HashMap<String, Object>();
            messages.put("errorAttrName", "Cannot create an attribute with name '" + attr_name
                    + "' because an attribute with the same name already exists.");
            return generic_edit(request, response, messages);
        }

        Language language = languageService.getLanguage(tp.getLangID());

        // save attr
        Attribute attr = new Attribute();
        attr.setTaxonomy(tax);
        if (((String) request.getParameter("attr_is_multiple")).equalsIgnoreCase("yes"))
        {
            attr.setIs_multiple(true);
        }
        else
        {
            attr.setIs_multiple(false);
        }
        if (((String) request.getParameter("attr_is_percussion_item")).equalsIgnoreCase("yes"))
        {
            attr.setIs_percussion_item(true);
        }
        else
        {
            attr.setIs_percussion_item(false);
        }
        if (((String) request.getParameter("attr_is_node_name")).equalsIgnoreCase("yes"))
        {
            attr.setIs_node_name(1);
            // override these options if needed
            attr.setIs_percussion_item(false);
            attr.setIs_multiple(false);
        }
        else
        {
            attr.setIs_node_name(0);
        }
        if (((String) request.getParameter("attr_is_required")).equalsIgnoreCase("yes"))
        {
            attr.setIs_required(true);
        }
        else
        {
            attr.setIs_required(false);
        }

        Timestamp ts = new Timestamp(System.currentTimeMillis());
        attr.setCreated_at(ts);
        attr.setCreated_by_id(getUserName(request));
        attr.setModified_at(ts);
        attr.setModified_by_id(getUserName(request));
        tax.addAttribute(attr);
        attributeService.saveAttribute(attr);

        // save attr lang
        Attribute_lang attr_lang = new Attribute_lang();
        attr_lang.setAttribute(attr);
        attr_lang.setLanguage(language);
        attr_lang.setName(attr_name);
        attribute_langService.saveAttribute_lang(attr_lang);

        return new ModelAndView("redirect:taxonomy.htm?action=edit&taxID=" + tax.getId() + "&langID="
                + language.getId());
    }

    public ModelAndView set_visibility(HttpServletRequest request, HttpServletResponse response) throws Exception
    {

        TaxonomySecurityHelper.raise_error_if_cannot_admin();
        TaxonParams tp = new TaxonParams(request,taxonomyService);

        Taxonomy taxonomy = taxonomyService.getTaxonomy(tp.getTaxID());

        // get existing communities
        Collection<Long> old_ids = new ArrayList<Long>();

        Collection<Visibility> existing_visiblities = visibilityService.getAllVisibilitiesForTaxonomyId(tp.getTaxID());
        for (Visibility visibility : existing_visiblities)
        {
            old_ids.add(visibility.getCommunity_id());
        }

        // get form values and add brand new visibilities
        Collection<Long> new_ids = new ArrayList<Long>();
        if (request.getParameterValues("community_ids") != null)
        {
            String[] ids_as_string = request.getParameterValues("community_ids");
            for (int i = 0; i < ids_as_string.length; i++)
            {
                long new_id = Long.parseLong(ids_as_string[i]);
                new_ids.add(new_id);
                if ((new_id > 0) && (!old_ids.contains(new_id)))
                {
                    // add new db object
                    Visibility v = new Visibility();
                    v.setTaxonomy(taxonomy);
                    v.setCommunity_id(new_id);
                    visibilityService.saveVisibility(v);
                }
            }
        }

        // delete old IDs if not used
        for (Visibility v : existing_visiblities)
        {
            if (!new_ids.contains(v.getCommunity_id()))
            {
                visibilityService.removeVisibility(v);
            }
        }
        Map<String, Object> messages = new HashMap<String, Object>();
        messages.put("setVisibility", "Saved.");

        return generic_edit(request, response, messages);
    }

    public ModelAndView edit(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        return generic_edit(request, response, null);
    }

    public ModelAndView generic_edit(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> messages) throws Exception
    {
        TaxonomySecurityHelper.raise_error_if_cannot_admin();
        TaxonParams tp = new TaxonParams(request,taxonomyService);
        Map<String, Object> myModel = new HashMap<String, Object>();

        Taxonomy tax = taxonomyService.getTaxonomy(tp.getTaxID());
        Language language = languageService.getLanguage(tp.getLangID());

        myModel.put("communities", TaxonomySecurityHelper.getAllCommunities());
        myModel.put("communitiesSelected",
                collection_to_hashmaped_ids(visibilityService.getAllVisibilitiesForTaxonomyId(tp.getTaxID())));
        myModel.put("taxonomy", tax);
        myModel.put("language", language);
        myModel.put("attributes", attributeService.getAllAttributes(tp.getTaxID(), tp.getLangID()));
        if (messages != null)
            myModel.put("messages", messages);

        return new ModelAndView("taxonomy/edit", "model", myModel);
    }

    public ModelAndView delete_attribute(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        TaxonParams tp = new TaxonParams(request,taxonomyService);
        String attributeId = ((String) request.getParameter("attrID"));
        notEmpty(attributeId);
        
        Collection attributes = attributeService.getAttribute(Integer.valueOf(attributeId));
        if(!attributes.isEmpty())
        {
           Attribute attribute = (Attribute) attributes.iterator().next();
           attributeService.removeAttribute(attribute);
        }
        return new ModelAndView("redirect:taxonomy.htm?action=edit&taxID=" + tp.getTaxID() + "&langID="
                + tp.getLangID());
    }

   public ModelAndView delete_taxonomy(HttpServletRequest request, HttpServletResponse response) throws Exception
   {
      TaxonParams tp = new TaxonParams(request,taxonomyService);

    // delete the taxonomy after the above validation
      Taxonomy taxonomy = taxonomyService.getTaxonomy(tp.getTaxID());
      
      // make sure the deleted taxonomy is not used by any content type
      PSItemDefManager defMgr = PSItemDefManager.getInstance();
      
      Map<String, List<String>> contentTypeToFields = new  HashMap<String, List<String>>();
      if (StringUtils.isNotBlank(request.getParameter("taxID"))) {
         contentTypeToFields.putAll(defMgr.getFieldsWithControlProp("taxonomy_name", taxonomy.getName()));
      }
      contentTypeToFields.putAll(defMgr.getFieldsWithControlProp("taxonomy_id", String.valueOf(tp.getTaxID())));
      if (!isEmpty(contentTypeToFields))
      {
         Map<String, Object> messages = new HashMap<String, Object>();
         messages.put("deleteTaxonomyError", taxonomyInUseWarning(contentTypeToFields));
         return generic_edit(request, response, messages);
      }
      
      // delete the taxonomy after the above validation
      if (taxonomy != null)
      {
         taxonomyService.removeTaxonomy(taxonomy);
      }

      return new ModelAndView("redirect:taxonomy.htm?action=index&langID=" + tp.getLangID());
   }

   private String taxonomyInUseWarning(Map<String, List<String>> contentTypeToFields)
   {
      StringBuilder buffer = new StringBuilder();
      buffer.append("The following conent-type [fields], ");
      for (String ctype : contentTypeToFields.keySet())
      {
         buffer.append(ctype);
         buffer.append(" ");
         buffer.append(contentTypeToFields.get(ctype).toString());
         buffer.append(", ");
      }
      buffer.append("use the taxonomy you are trying to delete. If you really want to delete this taxonomy you must first remove these fields. After the taxonomy fields are removed, the related data will not be recoverable.");
      return buffer.toString();
   }
   
    // wired by spring properties
    public void setTaxonomyService(TaxonomyService taxonomyService)
    {
        this.taxonomyService = taxonomyService;
    }

    public void setNode_statusService(Node_statusService nodeStatusService)
    {
        node_statusService = nodeStatusService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public LanguageService getLanguageService()
    {
        return languageService;
    }

    public void setLanguageService(LanguageService languageService)
    {
        this.languageService = languageService;
    }

    public AttributeService getAttributeService()
    {
        return attributeService;
    }

    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }

    public Attribute_langService getAttribute_langService()
    {
        return attribute_langService;
    }

    public void setAttribute_langService(Attribute_langService attributeLangService)
    {
        attribute_langService = attributeLangService;
    }

    public VisibilityService getVisibilityService()
    {
        return visibilityService;
    }

    public void setVisibilityService(VisibilityService visibilityService)
    {
        this.visibilityService = visibilityService;
    }

    public void setValueService(ValueService valueService)
    {
        this.valueService = valueService;
    }

    // private methods

    private static HashMap<Long, Long> collection_to_hashmaped_ids(Collection<Visibility> c)
    {
        HashMap<Long, Long> ret = new HashMap<Long, Long>();
        for (Visibility o : c)
        {
            ret.put(o.getCommunity_id(), o.getCommunity_id());
        }
        return ret;
    }

}
