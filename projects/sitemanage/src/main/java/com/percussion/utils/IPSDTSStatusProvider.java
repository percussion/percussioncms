package com.percussion.utils;

import com.percussion.integritymanagement.data.IPSIntegrityTask;
import com.percussion.utils.types.PSPair;

import java.util.Map;

public interface IPSDTSStatusProvider {
    /**
     * Returns Health status of DTS and all services - No Services are
     * represented if DTS is not running Services are represented as key values
     * in a map with a PSPair representing Status (success or failed) in the
     * first element and the response message in the second element. The first
     * element of the PSPair will always represent Status.
     */
     Map<String, PSPair<IPSIntegrityTask.TaskStatus, String>> getDTSStatusReport();
}
