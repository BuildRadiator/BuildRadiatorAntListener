package com.paulhammant.buildradiatorantlistener;
/**
 * Copyright (c) 2012 to original author or authors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BuildRadiatorInterop {

    private final String buildIdEnvVar;
    private final String buildingThisProject;
    private final String radiatorCodeEnvVar;
    private final String radiatorSecretEnvVar;
    private String rootProject;
    private boolean trace;
    private java.util.List<String> steps = new ArrayList<>();
    private Properties stepMap = new Properties();
    private String buildRadiatorURL;
    private int currStep = -1;
    private String lastStep = "";
    private boolean env_var_warning = false;
    private boolean hasStarted = false;
    private boolean hasFailed = false;
    private boolean failureNotified= false;

    BuildRadiatorInterop(String buildIdEnvVar, String buildingThisProject,
                         String radiatorCodeEnvVar, String radiatorSecretEnvVar, List<String> steps, Properties stepMap, String buildRadiatorURL, boolean trace, String rootProject) {
        this.buildIdEnvVar = buildIdEnvVar;
        this.buildingThisProject = buildingThisProject;
        this.radiatorCodeEnvVar = radiatorCodeEnvVar;
        this.radiatorSecretEnvVar = radiatorSecretEnvVar;
        this.steps = steps;
        this.stepMap = stepMap;
        this.buildRadiatorURL = buildRadiatorURL;
        this.trace = trace;
        this.rootProject = rootProject;
    }

    void visitTarget(String currentProjectId, String target, String status) {

        String projectAndTarget = currentProjectId + "/" + target;

        if (trace) {
            systemErr().println("Project/Target: " + projectAndTarget
                    + " (" +  status + ")");
        }

        if (steps.size() == 0) {
            return;
        }
        String nextStep;
        String nextProjectAndTarget;
        if (currStep +1 < steps.size()) {
            nextStep = steps.get(currStep + 1);
            nextProjectAndTarget = stepMap.getProperty(nextStep);
        } else {
            nextStep = "";
            nextProjectAndTarget = "";
        }

        if (nextProjectAndTarget.equals(projectAndTarget) || nextProjectAndTarget.equals("*")) {
            if (status.equals("failed")) {
                stepFailedNotification(lastStep);
                hasFailed = true;
                failureNotified = true;
            } else {
                startStepNotification(nextStep);
                lastStep = nextStep;
                hasStarted = true;
                currStep++;
            }
        } else if (status.equals("failed")) {
            hasFailed = true;

        }
    }

    void projectEnd(String currentProjectId) {
        if (hasFailed) {
            if (!failureNotified) {
                stepFailedNotification(lastStep);
            }
        } else {
            stepPassedNotification(lastStep);
        }
    }

    private void stepPassedNotification(String step) {
        stepNotification(step, "stepPassed");
    }

    private void startStepNotification(String step) {
        stepNotification(step, "startStep");
    }

    private void stepFailedNotification(String step) {
        stepNotification(step, "stepFailed");
    }

    private void stepNotification(String step, String stateChg) {

        if (varsAreMissing() || !this.buildingThisProject.equals(this.rootProject)) {
            if (!env_var_warning) {
                systemErr().println("BuildRadiatorEventSpy: 'buildingThisProject', 'buildId', 'radiatorCode' and 'radiatorSecret' all " +
                        "have to be set as environmental variables before Maven is invoked, if you want " +
                        "your radiator to be updated. Additionally, 'buildingThisProject' needs to match the root " +
                        "artifact being built. Note: This technology is for C.I. daemons only, not developer workstations!");
                systemErr().println("  buildingThisProject (env var): " + buildingThisProject);
                systemErr().println("  buildId (env var): " + buildIdEnvVar);
                systemErr().println("  radiatorCode (env var): " + radiatorCodeEnvVar);
                if (radiatorSecretEnvVar == null) {
                    systemErr().println("  radiatorSecret (env var): null");
                } else {
                    systemErr().println("  radiatorSecret (env var): REDACTED (len:" + radiatorSecretEnvVar.length() + ")");
                }
            }
            env_var_warning = true;
            return;
        }
        try {
            String urlParameters = "build=" + this.buildIdEnvVar + "&step=" + step + "&secret=" + this.radiatorSecretEnvVar;
            String op = postUpdate(new URL(buildRadiatorURL + "/r/" + this.radiatorCodeEnvVar + "/" + stateChg), urlParameters);
            if (!op.equals("OK")) {
                systemErr().println("POST to buildradiator.org failed with " + op);
            }
        } catch (IOException e) {
            systemErr().println("POST to buildradiator.org failed with " + e.getMessage());
            e.printStackTrace();
        }
    }

    protected String postUpdate(URL url, String urlParameters) throws IOException {
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setUseCaches(false);
        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(postData);
        }

        BufferedReader reader= new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while(reader.ready())
        {
            return reader.readLine();
        }
        throw new UnsupportedOperationException();

    }

    protected PrintStream systemErr() {
        return System.err;
    }

    private boolean varsAreMissing() {
        return this.radiatorCodeEnvVar == null || this.buildingThisProject == null || this.buildIdEnvVar == null || this.radiatorSecretEnvVar == null
                || this.radiatorCodeEnvVar.equals("") || this.buildingThisProject.equals("") || this.buildIdEnvVar.equals("") || this.radiatorSecretEnvVar.equals("");
    }

}
