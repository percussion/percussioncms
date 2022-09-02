package com.percussion.soln.p13n.tracking.ds.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DummyController {

    private String view;
    
    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    /*
     * Wrapper for ProfileMail.jsp
     */
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return new ModelAndView(getView());
    }

}
