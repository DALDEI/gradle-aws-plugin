/*
 * Copyright 2015-2016 David A. Lee. <dlee@calldei.com>
 *
 * This is a derived work from Classmethods, Inc.
 *
 * Current License Terms - Dual licensed under the following :
 *
 * "Simplified BSD License" included in license.txt
 *
 * The original work (derived from) license terms:  Apache License, Version 2.0
 * included in copyright.origin/LICENSE.TXT
 */
/*
 * TOTALLY BOGUS
 * Stupid
 */
package org.xmlsh.aws.gradle.elasticbeanstalk;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;

public class AWSElasticBeanstalkWaitEnvironmentStatusTask extends ConventionTask {

  @Getter
  @Setter
  private String appName;

  @Getter
  @Setter
  private String envName;

  @Getter
  @Setter
  private List<String> successStatuses = Arrays.asList(
      "Ready",
      "Terminated"
      );

  @Getter
  @Setter
  private List<String> waitStatuses = Arrays.asList(
      "Launching",
      "Updating",
      "Terminating"
      );

  @Getter
  @Setter
  private int loopTimeout = 900; // sec

  @Getter
  @Setter
  private int loopWait = 10; // sec

  public AWSElasticBeanstalkWaitEnvironmentStatusTask() {
    setDescription("Wait ElasticBeanstalk environment for specific status.");
    setGroup("AWS");
  }

  @TaskAction
  public void waitEnvironmentForStatus() {
    // to enable conventionMappings feature
    String appName = getAppName();
    String envName = getEnvName();
    int loopTimeout = getLoopTimeout();
    int loopWait = getLoopWait();

    if (appName == null)
      throw new GradleException("applicationName is not specified");

    AwsBeanstalkPluginExtension ext = getProject().getExtensions().getByType(AwsBeanstalkPluginExtension.class);
    AWSElasticBeanstalk eb = ext.getClient();

    long start = System.currentTimeMillis();
    while (true) {
      if (System.currentTimeMillis() > start + (loopTimeout * 1000)) {
        throw new GradleException("Timeout");
      }

      try {
        DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
            .withApplicationName(appName)
            .withEnvironmentNames(envName));

        if (der.getEnvironments() == null || der.getEnvironments().isEmpty()) {
          getLogger().info("environment " + envName + " @ " + appName + " not found");
          return;
        }

        EnvironmentDescription ed = der.getEnvironments().get(0);

        if (successStatuses.contains(ed.getStatus())) {
          getLogger().info("Status of environment " + envName + " @ " + appName + " is now " + ed.getStatus() + ".");
          break;
        }
        else if (waitStatuses.contains(ed.getStatus())) {
          getLogger().info("Status of environment " + envName + " @ " + appName + " is " + ed.getStatus() + "...");
          try {
            Thread.sleep(loopWait * 1000);
          } catch (InterruptedException e) {
            throw new GradleException("interrupted");
          }
        }
        else {
          // waitStatusesでもsuccessStatusesないステータスはfailとする
          throw new GradleException("Status of environment " + envName + " @ " + appName + " is " + ed.getStatus()
              + ".  It seems to be failed.");
        }
      } catch (AmazonServiceException e) {
        throw new GradleException(e.getMessage());
      }
    }
  }
}
