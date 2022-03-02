package com.percussion.utils;

import com.percussion.integritymanagement.data.PSIntegrityTask;
import com.percussion.utils.types.PSPair;

import java.util.Map;

public interface IPSDTSStatusProvider {
    Map<String, PSPair<PSIntegrityTask.TaskStatus, String>> getDTSStatusReport();
}
