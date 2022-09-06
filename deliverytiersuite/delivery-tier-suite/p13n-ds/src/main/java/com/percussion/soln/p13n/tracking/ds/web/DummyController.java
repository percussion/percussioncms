package com.percussion.soln.p13n.tracking.ds.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
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
    @RequestMapping
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) {
        return new ModelAndView(getView());
    }

}
