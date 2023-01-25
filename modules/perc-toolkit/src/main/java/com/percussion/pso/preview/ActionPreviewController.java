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
package com.percussion.pso.preview;

import com.percussion.cms.objectstore.PSAction;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.timing.PSStopwatch;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * 
 * 
 * @author DavidBenua
 * 
 */
public class ActionPreviewController extends AbstractMenuController implements
		Controller {

	private static final Logger log = LogManager.getLogger(ActionPreviewController.class);

	private String snippetTargetStyle = null;

	private UrlBuilder urlBuilder = null;

	/**
	 * Default constructor
	 */
	public ActionPreviewController() {
		super();
	}

	/**
	 * @see org.springframework.web.servlet.mvc.AbstractController#handleRequestInternal(HttpServletRequest,
	 *      HttpServletResponse)
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		PSStopwatch timer = new PSStopwatch();

		timer.start();
		List<PSOAction> actions = new ArrayList<PSOAction>();
		Map<String, Object> urlParams = new HashMap<String, Object>();
		ModelAndView mav = super.handleRequestInternal(request, response);
		boolean useMultipleSites = false;
		Locale locale = request.getLocale();
		String contentid = StringUtils.defaultString(request
				.getParameter(IPSHtmlParameters.SYS_CONTENTID));
		String revision = StringUtils.defaultString(request
				.getParameter(IPSHtmlParameters.SYS_REVISION));
		String folderid = StringUtils.defaultString(request
				.getParameter(IPSHtmlParameters.SYS_FOLDERID));
		String siteid = StringUtils.defaultString(request
				.getParameter(IPSHtmlParameters.SYS_SITEID));

		if (StringUtils.isBlank(contentid)) {
			String emsg = "content id cannot be blank, check configuration";
			log.error(emsg);
			RuntimeException exp = new RuntimeException(emsg);
			Document errResult = PSXmlDocumentBuilder.createErrorDocument(exp,
					request.getLocale());
			mav.addObject("result", errResult);
			timer.stop();
			log.debug("elapsed time is {}", timer.elapsed());
			return mav;
		}

		String refreshHint = request.getParameter("refreshHint");
		String target = request.getParameter("target");
		String targetStyle = request.getParameter("targetStyle");
		String launchesWindow = request.getParameter("launchesWindow");
		Properties properties = new Properties();
		if (StringUtils.isNotBlank(refreshHint)) {
			properties.setProperty("refreshHint", refreshHint);
		}
		if (StringUtils.isNotBlank(target)) {
			properties.setProperty("target", target);
		}
		if (StringUtils.isNotBlank(targetStyle)) {
			properties.setProperty("targetStyle", targetStyle);
		}
		if (StringUtils.isNotBlank(launchesWindow)) {
			properties.setProperty("launchesWindow", launchesWindow);
		}

		urlParams.put(IPSHtmlParameters.SYS_CONTENTID, contentid);
		urlParams.put(IPSHtmlParameters.SYS_REVISION, revision);
		urlParams.put(IPSHtmlParameters.SYS_LANG, locale.toString());

		PSStopwatch tm = new PSStopwatch();
		tm.start();
		List<SiteFolderLocation> locations = this.siteFolderFinder
				.findSiteFolderLocations(contentid, folderid, siteid);
		tm.stop();
		log.debug("Time to fetch locations {}", tm.elapsed());
		SiteFolderLocation loc;
		log.debug("there are {} locations",locations.size());
		if (locations.size() == 1) {
			loc = locations.get(0);
		} else if (locations.size() > 1) {
			loc = null;
			useMultipleSites = true;
		} else {
			loc = null;
		}

		Set<IPSSite> sites = findSitesFromLocations(locations);
		List<IPSAssemblyTemplate> templates = findVisibleTemplates(contentid,
				sites);
		log.debug("found {} visible templates",templates.size());
		actions = makeActionsFromTemplates(actions, templates, properties,
				urlParams, loc, useMultipleSites);
		Collections.sort(actions);
		Document result = buildActionListXml(actions);
		mav.addObject("result", result);

		timer.stop();
		log.debug("Elapsed time is {}", timer.elapsed());

		return mav;
	}

	protected List<PSOAction> makeActionsFromTemplates(List<PSOAction> actions,
			List<IPSAssemblyTemplate> templates, Properties properties,
			Map<String, Object> urlParams, SiteFolderLocation location,
			boolean useMulti) throws Exception {
		initServices();
		PSOAction action;
		for (IPSAssemblyTemplate template : templates) {

			log.debug("processing template {}", template.getName());

			action = new PSOAction();
			action.setHandler(PSAction.HANDLER_SERVER);
			action.setType(PSAction.TYPE_MENUITEM);
			action.setName(template.getName());
			action.setLabel(template.getLabel());
			action.setUrl(urlBuilder.buildUrl(template, urlParams, location,
					useMulti));

			// make a copy
			Properties newProperties = new Properties(properties);
			if (template.getOutputFormat() == IPSAssemblyTemplate.OutputFormat.Snippet
					&& StringUtils.isNotBlank(this.snippetTargetStyle)) {
				log.debug("adding targetStyle");
				newProperties.setProperty("targetStyle",
						this.snippetTargetStyle);
			} else {
				log.debug("adding targetStyle - CM 7.x requires targetStyle");
				newProperties.setProperty("targetStyle",
						this.snippetTargetStyle);
			}
			action.setProperties(newProperties);
			actions.add(action);
		}

		return actions;
	}

	protected Document buildActionListXml(List<PSOAction> actions) {
		Document output = PSXmlDocumentBuilder.createXmlDocument();
		Element root = PSXmlDocumentBuilder.createRoot(output, "ActionList");
		for (PSOAction action : actions) {
			Element el = action.toXml(output);
			root.appendChild(el);
		}
		return output;
	}

	/**
	 * @return the snippetTargetStyle
	 */
	public String getSnippetTargetStyle() {
		return snippetTargetStyle;
	}

	/**
	 * @param snippetTargetStyle
	 *            the snippetTargetStyle to set
	 */
	public void setSnippetTargetStyle(String snippetTargetStyle) {
		this.snippetTargetStyle = snippetTargetStyle;
	}

	/**
	 * @return the urlBuilder
	 */
	public UrlBuilder getUrlBuilder() {
		return urlBuilder;
	}

	/**
	 * @param urlBuilder
	 *            the urlBuilder to set
	 */
	public void setUrlBuilder(UrlBuilder urlBuilder) {
		this.urlBuilder = urlBuilder;
	}

}
