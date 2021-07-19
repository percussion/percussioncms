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
package com.percussion.soln.linkback.servlet;

import com.percussion.soln.linkback.codec.LinkbackTokenCodec;
import com.percussion.soln.linkback.codec.impl.StringLinkBackTokenImpl;
import com.percussion.soln.linkback.utils.LinkbackUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.text.MessageFormat.format;

/**
 * Generic Linkback Controller. Expects a linkback token as request parameter.
 * 
 * Though mainly used as base class for specific controllers, it can be
 * configured as a bean by itself. The default behavior is to redirect to
 * <code>redirectPath</code> with parameters decoded from the linkback codec.
 * The following property can be configured in the bean xml file:
 * <ul>
 * <li>redirectPath - mandatory if this class is used to configure a bean;
 * optional if subclasses are used, in which case, it depends on the subclass
 * implementation</li>
 * <li>helpViewName (optional) - help view name</li>
 * <li>linkbackCodec (optional) - implementation of {@link LinkbackTokenCodec}</li>
 * </ul>
 */
public class GenericLinkbackController extends AbstractController {

    private static final Logger log = LogManager.getLogger(GenericLinkbackController.class);

    private String linkbackParameterName = LinkbackUtils.LINKBACK_PARAM_NAME;

    private String redirectPath = "";

    private String helpViewName = "";
    
    private String errorViewName = "";

    private LinkbackTokenCodec linkbackCodec = null;

    private List<String> requiredParameterNames = new ArrayList<>();

    private List<String> optionalParameterNames = new ArrayList<>();

    private Map<String, String> additionalParameters = new HashMap<>();

    /**
     * If linkback token is not blank, this method calls
     * handleLinkBackRedirect() to create a ModelAndView object; otherwise,
     * return the helpview if exists.
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
             {
        initCodec();

        String linkbackToken = request.getParameter(linkbackParameterName);
        log.debug("linkbackToken={}" , linkbackToken);

        if (linkbackToken == null) {

            return createHelpView();
        }
        else if(StringUtils.isBlank(linkbackToken)) {
            return createErrorView(format("Linkback param ({}) is an empty string.", getLinkbackParameterName()));
        }

        Map<String, String> params = linkbackCodec.decode(linkbackToken);
        log.debug("map: {}" , params);
        return  handleLinkBackRedirect(params);

    }

    /**
     * Return a ModelAndView object, using a RedirectView and the map as the
     * model. Subclasses override this method to determine how the linkback
     * should be handled.
     * 
     * @param tokenParams map of parameter values
     * @return ModelAndView
     */
    protected ModelAndView handleLinkBackRedirect(Map<String, String> tokenParams) {
        Map<String, String> params = new HashMap<>();
        if (!checkAndCopyRequiredParams(tokenParams, params)) {
            String message = "Required Parameter missing, required parameters are " + getRequiredParameterNames();
            return createErrorView(message);
        }
        copyOptionalParameters(tokenParams, params);
        params.putAll(getAdditionalParameters());
        modifyParameterMap(params);
        String path = getRedirectPath();
        log.debug("redirect path: {}",  path);
        return new ModelAndView(new RedirectView(path, true), params);
    }

    /**
     * Check that required parameters exist and copy them to the output map. The
     * parameters must not be blank and must be numeric.
     * 
     * @param inParams
     *            the input parameter map.
     * @param outParams
     *            the map to copy parameters to.
     * @return true if the required parameters exist.
     */
    protected boolean checkAndCopyRequiredParams(Map<String, String> inParams, Map<String, String> outParams) {
        for (String pname : getRequiredParameterNames()) {
            String pvalue = inParams.get(pname);
            if (StringUtils.isBlank(pvalue) || !StringUtils.isNumeric(pvalue)) {
                return false;
            }
            outParams.put(pname, pvalue);
        }
        return true;
    }

    /**
     * Copy the optional parameters.
     * 
     * @param inParams
     *            the incoming parameters
     * @param outParams
     *            the parameter map to copy to.
     */
    protected void copyOptionalParameters(Map<String, String> inParams, Map<String, String> outParams) {
        for (String pname : getOptionalParameterNames()) {
            String pvalue = inParams.get(pname);
            if (StringUtils.isNotBlank(pvalue)) {
                outParams.put(pname, pvalue);
            }
        }
    }

    /**
     * Modify the parameter map. This is intended for subclasses that need
     * special parameters.
     * 
     * @param params
     *            the parameter map.
     */
    protected void modifyParameterMap(Map<String, String> params) {
        // do nothing for now.
    }

    /**
     * Create a help view if {@link #helpViewName} is defined in the bean config;
     * otherwise, null;
     * 
     * @return ModelAndView
     */
    protected ModelAndView createHelpView() {
        if (StringUtils.isBlank(getHelpViewName())) {
            return null;
        }
        return new ModelAndView(getHelpViewName(), "message", null);
    }

    /**
     * Create a error view if {@link #errorViewName} is defined in the bean config;
     * otherwise, null;
     * 
     * @param message
     *            custom message
     * @return ModelAndView
     */
    protected ModelAndView createErrorView(String message) {
        if (StringUtils.isBlank(getErrorViewName())) {
            return null;
        }
        return new ModelAndView(getErrorViewName(), "message", message);
    }
    

    private void initCodec() {
        if (linkbackCodec == null) {
            // create a default codec, if not specified in the bean config
            linkbackCodec = new StringLinkBackTokenImpl();
        }
    }

    public String getRedirectPath() {
        return redirectPath;
    }

    public void setRedirectPath(String redirectPath) {
        this.redirectPath = redirectPath;
    }

    public String getHelpViewName() {
        return helpViewName;
    }

    public void setHelpViewName(String helpViewName) {
        this.helpViewName = helpViewName;
    }

    public LinkbackTokenCodec getLinkbackCodec() {
        return linkbackCodec;
    }

    public void setLinkbackCodec(LinkbackTokenCodec linkbackCodec) {
        this.linkbackCodec = linkbackCodec;
    }

    /**
     * @return the linkbackParameterName
     */
    public String getLinkbackParameterName() {
        return linkbackParameterName;
    }

    /**
     * @param linkbackParameterName
     *            the linkbackParameterName to set
     */
    public void setLinkbackParameterName(String linkbackParameterName) {
        this.linkbackParameterName = linkbackParameterName;
    }

    /**
     * @return the requiredParameterNames
     */
    public List<String> getRequiredParameterNames() {
        return requiredParameterNames;
    }

    /**
     * @param requiredParameterNames
     *            the requiredParameterNames to set
     */
    public void setRequiredParameterNames(List<String> requiredParameterNames) {
        this.requiredParameterNames = requiredParameterNames;
    }

    /**
     * @return the optionalParameterNames
     */
    public List<String> getOptionalParameterNames() {
        return optionalParameterNames;
    }

    /**
     * @param optionalParameterNames
     *            the optionalParameterNames to set
     */
    public void setOptionalParameterNames(List<String> optionalParameterNames) {
        this.optionalParameterNames = optionalParameterNames;
    }

    /**
     * @return the additionalParameters
     */
    public Map<String, String> getAdditionalParameters() {
        return additionalParameters;
    }

    /**
     * @param additionalParameters
     *            the additionalParameters to set
     */
    public void setAdditionalParameters(Map<String, String> additionalParameters) {
        this.additionalParameters = additionalParameters;
    }

    
    public String getErrorViewName() {
        return errorViewName;
    }

    
    public void setErrorViewName(String errorViewName) {
        this.errorViewName = errorViewName;
    }
}
