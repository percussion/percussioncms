package com.percussion;

import java.util.HashMap;
import java.util.Map;

public class SecureHeaderCheckResponse {
    private boolean failedCheck = false;

    private Map<String, Boolean> checks = new HashMap<>();

    public Map<String, Boolean> getChecks() {
        return checks;
    }

    public void setChecks(Map<String, Boolean> checks) {
        this.checks = checks;
    }

    /**
     * When true at least one check failed.
     * @return
     */
    public boolean isFailedCheck() {
        return failedCheck;
    }

    public void setFailedCheck(boolean failedCheck) {
        this.failedCheck = failedCheck;
    }
}
