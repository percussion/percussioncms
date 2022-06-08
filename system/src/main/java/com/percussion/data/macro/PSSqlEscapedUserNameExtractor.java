package com.percussion.data.macro;

import com.percussion.data.PSDataExtractionException;
import com.percussion.data.PSExecutionData;
import com.percussion.security.PSUserEntry;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.PSRequest;
import com.percussion.server.PSUserSession;
import com.percussion.util.PSSqlHelper;

public class PSSqlEscapedUserNameExtractor implements IPSMacroExtractor
{

   @Override
   public Object extract(PSExecutionData data) throws PSDataExtractionException
   {
      try
      {
         String result = "";
         PSRequest request = data.getRequest();
         PSUserSession sess = (request == null) ? null : request.getUserSession();
         if (sess != null)
         {
            PSUserEntry[] users = sess.getAuthenticatedUserEntries();
            if (users.length > 0)
            {
               result = PSSqlHelper.escapeQueryParamValue(users[0].getName());
            }
         }
         
         return result;
      }
      catch (Exception e)
      {
         throw new PSDataExtractionException(IPSServerErrors.RAW_DUMP, e.getLocalizedMessage());
      }
   }

}
