package com.percussion.servlets;

import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PSSessionExtendServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getSession().getLastAccessedTime();
        String json ="{\"success\":true}";
        try {
            response.getWriter().write(json);

        } catch (IOException e) {
            log.error("Invalid json object for session extend. Error: {}", PSExceptionUtils.getMessageForLog(e));
            response.sendError(500, "Error occurred while extending the session");
        }

    }
    @Override
    public void init() throws ServletException {
        super.init();
    }
    private static final Logger log = LogManager.getLogger(IPSConstants.SECURITY_LOG);
}
