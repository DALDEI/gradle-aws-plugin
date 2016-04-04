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

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsResult;
import com.amazonaws.services.elasticbeanstalk.model.UpdateApplicationRequest;

public class AWSElasticBeanstalkCreateApplicationTask extends ConventionTask {

  @Getter
  @Setter
  private String appName;

  @Getter
  @Setter
  private String appDesc = "";

  public AWSElasticBeanstalkCreateApplicationTask() {
    setDescription("Create/Migrate ElasticBeanstalk Application.");
    setGroup("AWS");
  }

  @TaskAction
  public void createApplication() {
    // to enable conventionMappings feature
    String appName = getAppName();
    String appDesc = getAppDesc();

    AwsBeanstalkPluginExtension ext = getProject().getExtensions().getByType(AwsBeanstalkPluginExtension.class);
    AWSElasticBeanstalk eb = ext.getClient();

    DescribeApplicationsResult existingApps = eb.describeApplications(new DescribeApplicationsRequest()
        .withApplicationNames(appName));
    if (existingApps.getApplications().isEmpty()) {
      eb.createApplication(new CreateApplicationRequest()
          .withApplicationName(appName)
          .withDescription(appDesc));
      getLogger().info("application " + appName + " (" + appDesc + ") created");
    }
    else {
      eb.updateApplication(new UpdateApplicationRequest()
          .withApplicationName(appName)
          .withDescription(appDesc));
      getLogger().info("application " + appName + " (" + appDesc + ") updated");
    }
  }
}
