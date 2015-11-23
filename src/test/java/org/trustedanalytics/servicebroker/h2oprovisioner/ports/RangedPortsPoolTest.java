/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trustedanalytics.servicebroker.h2oprovisioner.ports;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RangedPortsPoolTest {

    @Mock
    private PortChecker portChecker;

    @Test
    public void create_validPortNumbers_objectCreated() {
        new RangedPortsPool(portChecker, 8080, 8090);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_invalidPortNumbers1_exceptionThrown() {
        new RangedPortsPool(portChecker, 0, 8090);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_invalidPortNumbers2_exceptionThrown() {
        new RangedPortsPool(portChecker, 65536, 8090);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_invalidPortNumbers3_exceptionThrown() {
        new RangedPortsPool(portChecker, 8080, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_invalidPortNumbers4_exceptionThrown() {
        new RangedPortsPool(portChecker, 8080, 65536);
    }

    @Test(expected = IllegalArgumentException.class)
    public void create_invalidPortNumbers5_exceptionThrown() {
        new RangedPortsPool(portChecker, 1000, 100);
    }

    @Test
    public void getPort_allPortsAvailable_returnsPortsOneAfterAnother() throws IOException {
        RangedPortsPool portsPool = new RangedPortsPool(allPortsAvailable(), 1, 3);
        assertThat(portsPool.getPort(), equalTo(1));
        assertThat(portsPool.getPort(), equalTo(2));
        assertThat(portsPool.getPort(), equalTo(3));
        assertThat(portsPool.getPort(), equalTo(1));
        assertThat(portsPool.getPort(), equalTo(2));
        assertThat(portsPool.getPort(), equalTo(3));
        assertThat(portsPool.getPort(), equalTo(1));
        assertThat(portsPool.getPort(), equalTo(2));
        assertThat(portsPool.getPort(), equalTo(3));
    }

    private PortChecker allPortsAvailable() {
        return port -> true;
    }

    @Test
    public void getPort_oddPortsAvailable_returnsOddPortsOneAfterAnother() throws IOException {
        RangedPortsPool portsPool = new RangedPortsPool(onlyOddPortsAvailable(), 1, 4);
        assertThat(portsPool.getPort(), equalTo(1));
        assertThat(portsPool.getPort(), equalTo(3));
        assertThat(portsPool.getPort(), equalTo(1));
        assertThat(portsPool.getPort(), equalTo(3));
        assertThat(portsPool.getPort(), equalTo(1));
        assertThat(portsPool.getPort(), equalTo(3));
    }

    private PortChecker onlyOddPortsAvailable() {
        return port -> port % 2 != 0;
    }

    @Test
    public void getPort_allPortsUnavailable_checkAllPortsOnlyOnceThenThrowsException() {
        PortChecker portChecker = allPortsUnavailable();
        RangedPortsPool portsPool = new RangedPortsPool(portChecker, 1, 4);

        try {
            portsPool.getPort();
        } catch (IOException e) {
            assertThat(e.getMessage(), equalTo("No port in the pool is available."));
        }

        InOrder inOrder = inOrder(portChecker);
        inOrder.verify(portChecker).isAvailable(1);
        inOrder.verify(portChecker).isAvailable(2);
        inOrder.verify(portChecker).isAvailable(3);
        inOrder.verify(portChecker).isAvailable(4);
    }

    private PortChecker allPortsUnavailable() {
        PortChecker portChecker = mock(PortChecker.class);
        when(portChecker.isAvailable(anyInt())).thenReturn(false);
        return portChecker;
    }
}
