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

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.EnvironmentDescription;
import com.amazonaws.services.elasticbeanstalk.model.TerminateEnvironmentRequest;

public class AWSElasticBeanstalkTerminateEnvironmentTask extends ConventionTask {

  @Getter
  @Setter
  private String appName;

  @Getter
  @Setter
  private String envName;

  @Getter
  @Setter
  private String envId;

  public AWSElasticBeanstalkTerminateEnvironmentTask() {
    setDescription("Terminate(Delete) ElasticBeanstalk Environment.");
    setGroup("AWS");
  }

  @TaskAction
  public void terminateEnvironment() {
    // to enable conventionMappings feature
    String appName = getAppName();
    String envName = getEnvName();
    String envId = getEnvId();

    AwsBeanstalkPluginExtension ext = getProject().getExtensions().getByType(AwsBeanstalkPluginExtension.class);
    AWSElasticBeanstalk eb = ext.getClient();

    if (envId == null) {
      DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
          .withApplicationName(appName)
          .withEnvironmentNames(envName));

      if (der.getEnvironments() == null || der.getEnvironments().isEmpty()) {
        getLogger().warn("environment " + envName + " @ " + appName + " not found");
        return;
      }

      EnvironmentDescription ed = der.getEnvironments().get(0);
      envId = ed.getEnvironmentId();
    }

    try {
      eb.terminateEnvironment(new TerminateEnvironmentRequest()
          .withEnvironmentId(envId)
          .withEnvironmentName(envName));
      getLogger().info("environment " + envName + " (" + envId + ") @ " + appName + " termination requested");
    } catch (AmazonServiceException e) {
      if (e.getMessage().contains("No Environment found") == false) {
        throw e;
      }
      getLogger().warn("environment " + envName + " (" + envId + ") @ " + appName + " not found");
    }
  }
}
