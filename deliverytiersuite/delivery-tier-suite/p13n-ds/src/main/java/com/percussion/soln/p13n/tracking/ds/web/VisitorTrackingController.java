package com.percussion.soln.p13n.tracking.ds.web;

import static com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils.*;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.percussion.soln.p13n.tracking.VisitorTrackingActionRequest;
import com.percussion.soln.p13n.tracking.VisitorTrackingResponse;
import com.percussion.soln.p13n.tracking.web.IVisitorTrackingHttpService;
import com.percussion.soln.p13n.tracking.web.VisitorTrackingWebUtils;

@Controller
@RequestMapping(path={"/track*"})
public class VisitorTrackingController{

	/**
	 * The log instance to use for this class, never <code>null</code>.
	 */
	private static final Log log = LogFactory.getLog(VisitorTrackingController.class);

	@Autowired
	private IVisitorTrackingHttpService visitorTrackingHttpService;

	private boolean returnProfile = false;

	private boolean autoSave = true;
	
	
    private static final byte[] image = {
        71,73,70,56,57,97,01,00,01,00,-128,-1,00,-64,-64,
        00,00,00,33,-7,04,01,00,00,00,00,44,00,00,00,00,
        01,00,01,00,00,02,02,68,01,00,59
    };

	@RequestMapping
	public ModelAndView handleRequest(HttpServletRequest request,
			HttpServletResponse response, Object command,
			BindException errors) throws IOException {

		log.trace("Handling delivery request command : " + command);
		if (errors.hasErrors()) {
			return outputError(errors, response);
		}

		VisitorTrackingActionRequest trackingRequest = (VisitorTrackingActionRequest) command;
		trackingRequest.setAutoSave(isAutoSave());
		IVisitorTrackingHttpService mediator = getVisitorTrackingHttpService();
		
		VisitorTrackingResponse trackResponse = mediator.track(trackingRequest, request, response);
		if ( ! isReturnProfile() ) {
			trackResponse.setVisitorProfile(null);
		}
		if (request.getRequestURI().contains(".gif")) {
			outputImage(response);
		} else {
			log.debug("Generating JSON Response");
			outputJSON(trackResponse, response);
		}
		return null;
	}
	
	protected ModelAndView outputJSON(VisitorTrackingResponse visitorTrackingResponse,
			HttpServletResponse response) throws IOException {
		String jsonResponse = VisitorTrackingWebUtils.responseToJson(visitorTrackingResponse);
	    PrintWriter writer = response.getWriter();
	    writer.print(jsonResponse);
		return null;
	}


	protected ModelAndView outputError(Exception e, HttpServletResponse response) 
	throws IOException {
		VisitorTrackingResponse visitorTrackingResponse = new VisitorTrackingResponse();
		visitorTrackingResponse.setErrorMessage(e.getLocalizedMessage());
		visitorTrackingResponse.setStatus("ERROR");
		log.warn("Bad visitor tracking request: ",e);
		return outputJSON(visitorTrackingResponse, response);
	}

	protected void onBindAndValidate(HttpServletRequest request, 
			Object command, BindException errors) throws Exception {
	    super.onBindAndValidate(request, command, errors);
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "actionName", "field.required"); 
        /*
         * Get the locale, src url, and referrer url.
         */
        VisitorTrackingActionRequest trackingRequest = (VisitorTrackingActionRequest) command;
        convertServletRequestToTrackingRequest(request, trackingRequest);
	}

	protected ModelAndView outputImage(HttpServletResponse response) throws IOException {
		log.debug("Returning single pixel image");
		response.setContentType("image/gif");
		response.getOutputStream().write(image);
		response.flushBuffer();
		return null;
	}
	
    public boolean isAutoSave() {
        return autoSave;
    }
    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }
    
    public boolean isReturnProfile() {
        return returnProfile;
    }
    public void setReturnProfile(boolean returnProfile) {
        this.returnProfile = returnProfile;
    }
    public VisitorTrackingController() {
        //setCommandClass(VisitorTrackingActionRequest.class);
    }

    public IVisitorTrackingHttpService getVisitorTrackingHttpService() {
        return visitorTrackingHttpService;
    }

    public void setVisitorTrackingHttpService(
            IVisitorTrackingHttpService visitorTrackingWebMediator) {
        this.visitorTrackingHttpService = visitorTrackingWebMediator;
    }
}