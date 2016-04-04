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
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest;

public class AWSElasticBeanstalkDeleteApplicationVersionTask extends ConventionTask {

  @Getter
  @Setter
  private String applicationName;

  @Getter
  @Setter
  private String versionLabel;

  @Getter
  @Setter
  private String bucketName;

  @Getter
  @Setter
  private boolean deleteSourceBundle = true;

  public AWSElasticBeanstalkDeleteApplicationVersionTask() {
    setDescription("Delete ElasticBeanstalk Application Version.");
    setGroup("AWS");
  }

  @TaskAction
  public void deleteVersion() {
    // to enable conventionMappings feature
    String applicationName = getApplicationName();
    String versionLabel = getVersionLabel();
    boolean deleteSourceBundle = isDeleteSourceBundle();

    AwsBeanstalkPluginExtension ext = getProject().getExtensions().getByType(AwsBeanstalkPluginExtension.class);
    AWSElasticBeanstalk eb = ext.getClient();

    eb.deleteApplicationVersion(new DeleteApplicationVersionRequest()
        .withApplicationName(applicationName)
        .withVersionLabel(versionLabel)
        .withDeleteSourceBundle(deleteSourceBundle));
    getLogger().info("version " + versionLabel + " deleted");
  }
}
