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
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest;
import com.amazonaws.services.elasticbeanstalk.model.S3Location;

public class AWSElasticBeanstalkCreateApplicationVersionTask extends ConventionTask {

  @Getter
  @Setter
  private String appName;

  @Getter
  @Setter
  private String versionLabel;

  @Getter
  @Setter
  private String bucketName;

  @Getter
  @Setter
  private String key;

  public AWSElasticBeanstalkCreateApplicationVersionTask() {
    setDescription("Create/Migrate ElasticBeanstalk Application Version.");
    setGroup("AWS");
  }

  @TaskAction
  public void createVersion() {
    // to enable conventionMappings feature
    String appName = getAppName();
    String versionLabel = getVersionLabel();

    AwsBeanstalkPluginExtension ext = getProject().getExtensions().getByType(AwsBeanstalkPluginExtension.class);
    AWSElasticBeanstalk eb = ext.getClient();

    try {
      eb.createApplicationVersion(new CreateApplicationVersionRequest()
          .withApplicationName(appName)
          .withVersionLabel(versionLabel)
          .withSourceBundle(new S3Location(getBucketName(), getKey())));
      getLogger().info("version " + versionLabel + " @ " + appName + " created");
    } catch (AmazonServiceException e) {
      if (e.getMessage().endsWith("already exists.") == false) {
        throw e;
      }
      getLogger().warn("version " + versionLabel + " @ " + appName + " already exists.");
    }
  }
}
