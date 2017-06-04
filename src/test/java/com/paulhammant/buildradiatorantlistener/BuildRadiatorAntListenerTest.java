package com.paulhammant.buildradiatorantlistener;


import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Project;
import org.junit.Test;

import java.util.Hashtable;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BuildRadiatorAntListenerTest {

    @Test
    public void testBuildRadiatorPropertiesCanBeRetrievedFromAntProject() {
        BuildRadiatorAntListener listener = new BuildRadiatorAntListener();

        BuildEvent be = mock(BuildEvent.class);
        Project project = mock(Project.class);
        when(be.getProject()).thenReturn(project);
        Hashtable props = new Hashtable();

        props.put("buildradiator.trace", "true");
        props.put("buildradiator.0", "aaa=bbb");
        props.put("buildradiator.1", "ccc=ddd");
        props.put("buildradiator.2", "eee=fff");
        when(project.getProperties()).thenReturn(props);

        // nothing there to start
        assertThat(listener.trace, equalTo(false));
        assertThat(listener.steps.size(), equalTo(0));
        assertThat(listener.stepMap.size(), equalTo(0));

        listener.targetStarted(be);

        verify(be).getProject();
        verify(project).getProperties();

        verifyNoMoreInteractions(be, project);

        // settings retrieved
        assertThat(listener.trace, equalTo(true));
        assertThat(listener.steps.toString(), equalTo("[aaa, ccc, eee]"));
        assertThat(listener.stepMap.getProperty("aaa"), equalTo("bbb"));
        assertThat(listener.stepMap.getProperty("ccc"), equalTo("ddd"));
        assertThat(listener.stepMap.getProperty("eee"), equalTo("fff"));

    }

}
