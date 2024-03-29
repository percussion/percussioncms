/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.soln.linkback.servlet;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.error.PSException;
import com.percussion.soln.linkback.utils.ItemSummaryFinder;
import com.percussion.util.IPSHtmlParameters;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

/**
 * Linkback controller to redirect to Rhythmyx Active Assembly. Use the latest
 * revision (current or edit) of the item. The redirect path is internal (hard
 * coded), so there is no need to specify <code>redirectPath</code> in bean
 * configuration. Recommend to set <code>helpViewName</code>.
 */
public class ActiveAssemblyLinkbackController extends GenericLinkbackController {

    private static final Logger log = LogManager.getLogger(ActiveAssemblyLinkbackController.class);

    private static final String REDIRECT_PATH = "/assembler/render";

    private boolean useVariantId = true;

    private ItemSummaryFinder itemSummaryFinder;

    /**
     * Return a ModelAndView to the active assembly. Verify sys_contentid,
     * sys_template, sys_folderid, and sys_siteid are in the map. Use the latest
     * revision of the item, which may or may not be checked out by current
     * user.
     */
    @Override
    protected ModelAndView handleLinkBackRedirect(Map<String, String> params) {
        String sys_contentid = params.get(IPSHtmlParameters.SYS_CONTENTID);
        String sys_template = params.get(IPSHtmlParameters.SYS_TEMPLATE);
        String sys_folderid = params.get(IPSHtmlParameters.SYS_FOLDERID);
        String sys_siteid = params.get(IPSHtmlParameters.SYS_SITEID);

        log.debug("sys_contentid=" + sys_contentid + ", sys_folderid=" + sys_folderid + ", sys_template="
                + sys_template + ", sys_siteid=" + sys_siteid);

        if (isNotNumeric(sys_contentid) || isNotNumeric(sys_template) || isNotNumeric(sys_folderid)
                || isNotNumeric(sys_siteid)) {
            return super.createErrorView("Missing contentid, templateid, folderid, or siteid");
        }

        // get latest revision
        String sys_revision;
        try {
            sys_revision = getLatestRevision(sys_contentid);

        } catch (PSException e) {
            // otherwise, use what is in the map
            sys_revision = params.get(IPSHtmlParameters.SYS_REVISION);

            if (isNotNumeric(sys_revision)) {
                log.debug("cannot get revision");
                return super.createErrorView("Fail to get revision");
            }
        }

        // build new map
        Map<String, String> nmap = new HashMap<>();
        nmap.put(IPSHtmlParameters.SYS_CONTENTID, sys_contentid);
        nmap.put(IPSHtmlParameters.SYS_FOLDERID, sys_folderid);
        nmap.put(IPSHtmlParameters.SYS_SITEID, sys_siteid);
        nmap.put(IPSHtmlParameters.SYS_REVISION, sys_revision);

        // active assembly expects "sys_variantid", not "sys_template" in the
        // URL...
        if (isUseVariantId()) {
            log.debug("use " + IPSHtmlParameters.SYS_VARIANTID);
            nmap.put(IPSHtmlParameters.SYS_VARIANTID, sys_template);
        } else {
            log.debug("use " + IPSHtmlParameters.SYS_TEMPLATE);
            nmap.put(IPSHtmlParameters.SYS_TEMPLATE, sys_template);
        }

        // these values are always the same for AA
        nmap.put(IPSHtmlParameters.SYS_ITEMFILTER, "preview");
        nmap.put(IPSHtmlParameters.SYS_CONTEXT, "0");
        nmap.put(IPSHtmlParameters.SYS_COMMAND, "editrc");

        // return a RedirectView relative to current servlet context
        return new ModelAndView(new RedirectView(REDIRECT_PATH, true), nmap);
    }

    private String getLatestRevision(String contentid) throws PSException {
        PSLocator loc = getItemSummaryFinder().getCurrentOrEditLocator(contentid);
        log.debug("latest revision=" + loc.getRevision());

        return Integer.toString(loc.getRevision());
    }

    private boolean isNotNumeric(String str) {
        return (StringUtils.isBlank(str) || !StringUtils.isNumeric(str));
    }

    public boolean isUseVariantId() {
        return useVariantId;
    }

    /**
     * Set this property in the bean config. If "true" (default), use
     * IPSHtmlParameters.SYS_VARIANTID to submit the template ID; otherwise, use
     * IPSHtmlParameters.SYS_TEMPLATE.
     * 
     * @param useVariantId
     */
    public void setUseVariantId(boolean useVariantId) {
        this.useVariantId = useVariantId;
    }

    public ItemSummaryFinder getItemSummaryFinder() {
        return itemSummaryFinder;
    }

    public void setItemSummaryFinder(ItemSummaryFinder itemSummaryFinder) {
        this.itemSummaryFinder = itemSummaryFinder;
    }
}
