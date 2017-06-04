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

    @Override
    public void buildStarted(BuildEvent buildEvent) {
        prt(buildEvent, "buildStarted ");
    }

    @Override
    public void buildFinished(BuildEvent buildEvent) {
        prt(buildEvent, "buildFinished ");

    }

    @Override
    public void targetStarted(BuildEvent buildEvent) {
        if (steps.size() == 0) {
            determineSteps(buildEvent.getProject().getProperties());
        }
        prt(buildEvent, "targetStarted ");

    }

    private void determineSteps(Hashtable<String, Object> properties) {
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

    @Override
    public void targetFinished(BuildEvent buildEvent) {
        prt(buildEvent, "targetFinished ");

    }

    @Override
    public void taskStarted(BuildEvent buildEvent) {
    }

    private void prt(BuildEvent buildEvent, String typ) {
//        buildEvent.getProject().log("HAM: " + typ + "/" + buildEvent.getProject().getName()
//                + getTargetName(buildEvent)
//                + getTaskName(buildEvent) + " : " + buildEvent.getProject().getProperty("version"));
    }

    private String getTargetName(BuildEvent buildEvent) {
        try {
            String name = buildEvent.getTarget().getName();
            if (name.equals("")) {
                return "";
            } else {
                return "/" + name;
            }
        } catch (NullPointerException e) {
            return "";
        }
    }

    private String getTaskName(BuildEvent buildEvent) {
        try {
            return "/" + buildEvent.getTask().getTaskName();
        } catch (NullPointerException e) {
            return "";
        }
    }

    @Override
    public void taskFinished(BuildEvent buildEvent) {
    }

    @Override
    public void messageLogged(BuildEvent buildEvent) {
        //prt(buildEvent, "messageLogged ");

    }

}
