/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.trustedanalytics.servicebroker.h2oprovisioner.ports;

import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public class RangedPortsPool implements PortsPool {

  private final PortChecker portChecker;
  private final Queue<Integer> ports;

  public RangedPortsPool(PortChecker portChecker, int lowerBound, int upperBound) {
    this.portChecker = portChecker;
    validateArguments(lowerBound, upperBound);
    this.ports = initializePool(lowerBound, upperBound);
  }

  private void validateArguments(int lowerBound, int upperBound) {
    Preconditions.checkArgument(upperBound > lowerBound,
        "upperBound must be greater or equal than lowerBound");
    validatePortValue(lowerBound);
    validatePortValue(upperBound);
  }

  private void validatePortValue(int port) {
    Preconditions.checkArgument(port > 0, "port value is out of range: " + port);
    Preconditions.checkArgument(port <= 0xFFFF, "port value is out of range: " + port);
  }

  private Queue<Integer> initializePool(int lowerBound, int upperBound) {
    Queue<Integer> portsPool = new LinkedList<>();
    for (int i = lowerBound; i <= upperBound; ++i) {
      portsPool.add(i);
    }
    return portsPool;
  }

  @Override
  public synchronized int getPort() throws IOException {
    int firstPort = popPortAndEnqueueAgain();
    int port = firstPort;
    do {
      if (portChecker.isAvailable(port)) {
        return port;
      }
    } while ((port = popPortAndEnqueueAgain()) != firstPort);
    throw new IOException("No port in the pool is available.");
  }

  private int popPortAndEnqueueAgain() {
    int port = ports.remove();
    ports.add(port);
    return port;
  }
}
