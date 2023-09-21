package com.percussion.servlets;
import com.percussion.cms.IPSConstants;
import com.percussion.error.PSExceptionUtils;
import com.percussion.server.PSServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PSSessionCheckServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //super.doGet(req, resp);


            //PSRequest psrequest = PSSecurityFilter.initRequest(request, response);
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Cache-Control", "no-cache,must-revalidate");

            // Careful not to touch session during request which would extend
            //PSUserSession userSession = psrequest.getUserSession();
            long timeout = PSServer.getServerConfiguration().getUserSessionTimeout();
            int warning_s = PSServer.getServerConfiguration().getUserSessionWarning();
            long warning = warning_s > 0 ?  PSServer.getServerConfiguration().getUserSessionWarning()
                    : -1;

            String json ="{\"expiry\":"+timeout+",\"warning\":"+warning+"}";

            try {
                response.getWriter().write(json);

            } catch (IOException e) {
                log.error("Invalid json object for sessioncheck. Error: {}", PSExceptionUtils.getMessageForLog(e));
               response.sendError(500, "Error occurred while checking the session");
            }
    }

    @Override
    public void init() throws ServletException {
        super.init();
    }

    private static final Logger log = LogManager.getLogger(IPSConstants.SECURITY_LOG);
}
