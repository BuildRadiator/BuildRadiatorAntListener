/**
 * Copyright (c) 2012 to original author or authors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.paulhammant.buildradiatorantlistener;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

public class BuildRadiatorAntListener implements BuildListener {

    List<String> steps = new ArrayList<>();
    Properties stepMap = new Properties();
    String buildRadiatorURL;
    boolean trace;
    private BuildRadiatorInterop buildRadiatorInterop;
    String buildId;
    String buildingThisProject;
    String radiatorCode;
    String radiatorSecret;
    private String rootProject;

    @Override
    public void buildStarted(BuildEvent buildEvent) {

        // can't fully use this - buildEvent.getProject().getProperties() is empty - have to extract properties in a targetStarted event

        buildId = System.getenv("buildId");
        buildingThisProject = System.getenv("buildingThisProject");
        radiatorCode = System.getenv("radiatorCode");
        radiatorSecret = System.getenv("radiatorSecret");

    }

    @Override
    public void buildFinished(BuildEvent buildEvent) {
        buildFinished(buildEvent.getProject().getName());
    }

    void buildFinished(String projName) {
        this.buildRadiatorInterop.projectEnd(projName);
    }

    @Override
    public void targetStarted(BuildEvent buildEvent) {
        if (steps.size() == 0) {
            determineSteps(buildEvent.getProject().getProperties());
            rootProject = buildEvent.getProject().getName();
            buildRadiatorInterop = makeBuildRadiatorInterop();
        }
        targetStarted(buildEvent.getProject().getName(), buildEvent.getTarget().getName());
    }

    void targetStarted(String projName, String targetName) {
        this.buildRadiatorInterop.visitTarget(projName, targetName, "start");
    }

    void determineSteps(Hashtable<String, Object> properties) {
        buildRadiatorURL = (String) properties.get("buildradiator.baseurl");
        if (buildRadiatorURL == null) {
            buildRadiatorURL = "https://buildradiator.org";
        }
        String traceStr = (String) properties.get("buildradiator.trace");
        trace = traceStr != null && Boolean.parseBoolean(traceStr);

        int st = 0;
        while (properties.get("buildradiator." + st) != null) {
            Object stepObj = properties.get("buildradiator." + st);
            String[] stepDef = ((String) stepObj).split("=");
            steps.add(stepDef[0]);
            stepMap.put(stepDef[0], stepDef[1]);
            st++;
        }
    }

    BuildRadiatorInterop makeBuildRadiatorInterop() {
        return new BuildRadiatorInterop(buildId, buildingThisProject, radiatorCode, radiatorSecret, steps, stepMap, buildRadiatorURL, trace, rootProject);
    }

    @Override
    public void targetFinished(BuildEvent buildEvent) {
        targetFinished(buildEvent.getProject().getName(), buildEvent.getTarget().getName(), buildEvent.getException());
    }

    void targetFinished(String projName, String targetName, Throwable exception) {
        if (exception != null) {
            this.buildRadiatorInterop.visitTarget(projName, targetName, "failed");
        } else {
            this.buildRadiatorInterop.visitTarget(projName, targetName, "passed");
        }
    }

    @Override
    public void taskStarted(BuildEvent buildEvent) {
    }

    @Override
    public void taskFinished(BuildEvent buildEvent) {
    }

    @Override
    public void messageLogged(BuildEvent buildEvent) {
    }

}
