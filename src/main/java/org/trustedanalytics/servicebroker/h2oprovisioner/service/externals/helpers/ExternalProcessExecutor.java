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

package org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class ExternalProcessExecutor {

    private ExternalProcessExecutor() {
    }

    public static int runCommand(String[] command) throws IOException {
        System.out.println("===================");
        System.out.println("Command to invoke:");
        Arrays.asList(command).stream().forEach(System.out::println);
        System.out.println("===================");

        Process pr = Runtime.getRuntime().exec(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        BufferedReader err = new BufferedReader(new InputStreamReader(pr.getErrorStream()));

        new Thread(getPrintOutputJob(in)).start();
        new Thread(getPrintErrorJob(err)).start();

        try {
            pr.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int exitCode = pr.exitValue();
        System.out.println("===================");
        System.out.println("Exit value: " + exitCode);
        System.out.println("===================");
        return exitCode;
    }

    private static Runnable getPrintOutputJob(BufferedReader in) {
        return getPrintJob(in, "      ");
    }

    private static Runnable getPrintErrorJob(BufferedReader err) {
        return getPrintJob(err, "ERROR ");
    }

    private static Runnable getPrintJob(BufferedReader stream, String printPrefix) {
        return () -> {
            String line;
            try {
                while ((line = stream.readLine()) != null) {
                    System.out.println(printPrefix + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
    }
}
