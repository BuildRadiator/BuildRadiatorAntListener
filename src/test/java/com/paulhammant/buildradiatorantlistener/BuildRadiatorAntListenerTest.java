package com.paulhammant.buildradiatorantlistener;


import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BuildRadiatorAntListenerTest {

    BuildRadiatorAntListener listener;
    StringBuilder posts;

    @Before
    public void setuo() {
        posts = new StringBuilder();
    }

    @Test
    public void testBuildRadiatorPropertiesCanBeRetrievedFromAntProject() {
        listener = new BuildRadiatorAntListener();

        Hashtable props = testProperties();

        // nothing there to start
        assertThat(listener.trace, equalTo(false));
        assertThat(listener.steps.size(), equalTo(0));
        assertThat(listener.stepMap.size(), equalTo(0));

        listener.determineSteps(props);

        // settings retrieved
        assertThat(listener.trace, equalTo(true));
        assertThat(listener.steps.toString(), equalTo("[init, compile, package]"));
        assertThat(listener.stepMap.getProperty("init"), equalTo("*"));
        assertThat(listener.stepMap.getProperty("compile"), equalTo("bootstrap/compile"));
        assertThat(listener.stepMap.getProperty("package"), equalTo("*"));

    }

    private Hashtable testProperties() {
        Hashtable props = new Hashtable();

        props.put("buildradiator.trace", "true");
        props.put("buildradiator.0", "init=*");
        props.put("buildradiator.1", "compile=bootstrap/compile");
        props.put("buildradiator.2", "package=*");
        return props;
    }

    @Test
    public void testASequenceOfBuildTargetsIsRun() {

        listener = new BuildRadiatorAntListener() {
            @Override
            BuildRadiatorInterop makeBuildRadiatorInterop() {
                return new BuildRadiatorInterop("buildId", "bootstrap", "Rcode", "Rsecret", steps, stepMap, "http://foo", trace, "bootstrap") {
                    @Override
                    protected String postUpdate(URL url, String urlParameters) throws IOException {
                        posts.append(url).append(" ").append(urlParameters).append("\n");
                        return "OK";
                    }
                };
            }
        };

        BuildEvent be = mock(BuildEvent.class);
        Project project = mock(Project.class);
        when(be.getProject()).thenReturn(project);
        Target target = mock(Target.class);
        when(be.getTarget()).thenReturn(target);

        Hashtable props = testProperties();

        when(project.getProperties()).thenReturn(props);
        when(project.getName()).thenReturn("bootstrap");
        when(target.getName()).thenReturn("ivy:init");

        listener.targetStarted(be);
        listener.targetFinished(be);

        when(target.getName()).thenReturn("compile");

        listener.targetStarted(be);
        listener.targetFinished(be);

        when(target.getName()).thenReturn("jar");

        listener.targetStarted(be);
        listener.targetFinished(be);
        listener.buildFinished("bootstrap");

        verify(be, times(8)).getProject();
        verify(be, times(6)).getTarget();
        verify(be, times(3)).getException();
        verify(project).getProperties();
        verify(project, times(7)).getName();
        verify(target, times(6)).getName();

        verifyNoMoreInteractions(be, project, target);

        assertThat(posts.toString(), equalTo("http://foo/r/Rcode/startStep build=buildId&step=init&secret=Rsecret\n" +
                "http://foo/r/Rcode/stepPassedAndStartStep build=buildId&step=compile&secret=Rsecret&pStep=init\n" +
                "http://foo/r/Rcode/stepPassedAndStartStep build=buildId&step=package&secret=Rsecret&pStep=compile\n" +
                "http://foo/r/Rcode/stepPassed build=buildId&step=package&secret=Rsecret\n"));

    }

    @Test
    public void testAnotherSequenceOfBuildTargetsIsRun() {

        listener = new BuildRadiatorAntListener() {
            @Override
            BuildRadiatorInterop makeBuildRadiatorInterop() {
                return new BuildRadiatorInterop("buildId", "bootstrap", "Rcode", "Rsecret", steps, stepMap, "http://foo", trace, "bootstrap") {
                    @Override
                    protected String postUpdate(URL url, String urlParameters) throws IOException {
                        posts.append(url).append(" ").append(urlParameters).append("\n");
                        return "OK";
                    }
                };
            }
        };

        BuildEvent be = mock(BuildEvent.class);
        Project project = mock(Project.class);
        when(be.getProject()).thenReturn(project);
        Target target = mock(Target.class);
        when(be.getTarget()).thenReturn(target);

        Hashtable props = testProperties();

        when(project.getProperties()).thenReturn(props);
        when(project.getName()).thenReturn("bootstrap");
        when(target.getName()).thenReturn("ivy:init");

        listener.targetStarted(be);
        listener.targetFinished(be);

        when(target.getName()).thenReturn("compile");

        listener.targetStarted(be);
        listener.targetFinished(be);

        when(target.getName()).thenReturn("jar");

        listener.targetStarted(be);

        when(be.getException()).thenReturn(new RuntimeException("jar barfed"));

        listener.targetFinished(be);
        listener.buildFinished("bootstrap");

        verify(be, times(8)).getProject();
        verify(be, times(6)).getTarget();
        verify(be, times(3)).getException();
        verify(project).getProperties();
        verify(project, times(7)).getName();
        verify(target, times(6)).getName();

        verifyNoMoreInteractions(be, project, target);

        assertThat(posts.toString(), equalTo("http://foo/r/Rcode/startStep build=buildId&step=init&secret=Rsecret\n" +
                "http://foo/r/Rcode/stepPassedAndStartStep build=buildId&step=compile&secret=Rsecret&pStep=init\n" +
                "http://foo/r/Rcode/stepPassedAndStartStep build=buildId&step=package&secret=Rsecret&pStep=compile\n" +
                "http://foo/r/Rcode/stepFailed build=buildId&step=package&secret=Rsecret\n"));

    }

    @Test
    public void testYetAnotherSequenceOfBuildTargetsIsRun() {

        listener = new BuildRadiatorAntListener() {
            @Override
            BuildRadiatorInterop makeBuildRadiatorInterop() {
                return new BuildRadiatorInterop("buildId", "bootstrap", "Rcode", "Rsecret", steps, stepMap, "http://foo", trace, "bootstrap") {
                    @Override
                    protected String postUpdate(URL url, String urlParameters) throws IOException {
                        posts.append(url).append(" ").append(urlParameters).append("\n");
                        return "OK";
                    }
                };
            }
        };

        BuildEvent be = mock(BuildEvent.class);
        Project project = mock(Project.class);
        when(be.getProject()).thenReturn(project);
        Target target = mock(Target.class);
        when(be.getTarget()).thenReturn(target);

        Hashtable props = testProperties();

        when(project.getProperties()).thenReturn(props);
        when(project.getName()).thenReturn("bootstrap");
        when(target.getName()).thenReturn("ivy:init");

        listener.targetStarted(be);
        listener.targetFinished(be);

        when(target.getName()).thenReturn("compile");

        listener.targetStarted(be);
        when(be.getException()).thenReturn(new RuntimeException("compile barfed"));
        listener.targetFinished(be);

        listener.buildFinished("bootstrap");

        verify(be, times(6)).getProject();
        verify(be, times(4)).getTarget();
        verify(be, times(2)).getException();
        verify(project).getProperties();
        verify(project, times(5)).getName();
        verify(target, times(4)).getName();

        verifyNoMoreInteractions(be, project, target);

        assertThat(posts.toString(), equalTo("http://foo/r/Rcode/startStep build=buildId&step=init&secret=Rsecret\n" +
                "http://foo/r/Rcode/stepPassedAndStartStep build=buildId&step=compile&secret=Rsecret&pStep=init\n" +
                "http://foo/r/Rcode/stepFailed build=buildId&step=compile&secret=Rsecret\n"));

    }


}
