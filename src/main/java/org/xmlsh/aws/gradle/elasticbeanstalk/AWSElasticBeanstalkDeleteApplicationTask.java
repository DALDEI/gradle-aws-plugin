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
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationRequest;

public class AWSElasticBeanstalkDeleteApplicationTask extends ConventionTask {

  @Getter
  @Setter
  private String appName;

  public AWSElasticBeanstalkDeleteApplicationTask() {
    setDescription("Delete ElasticBeanstalk Application.");
    setGroup("AWS");
  }

  @TaskAction
  public void deleteApplication() {
    // to enable conventionMappings feature
    String appName = getAppName();

    AwsBeanstalkPluginExtension ext = getProject().getExtensions().getByType(AwsBeanstalkPluginExtension.class);
    AWSElasticBeanstalk eb = ext.getClient();

    eb.deleteApplication(new DeleteApplicationRequest(appName));
    getLogger().info("application " + appName + " deleted");
  }
}
