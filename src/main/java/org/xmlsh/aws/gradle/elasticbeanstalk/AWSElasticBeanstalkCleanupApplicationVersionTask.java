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

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.DeleteApplicationVersionRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;

public class AWSElasticBeanstalkCleanupApplicationVersionTask extends ConventionTask {

  @Getter
  @Setter
  private String appName;

  @Getter
  @Setter
  private boolean deleteSourceBundle = true;

  public AWSElasticBeanstalkCleanupApplicationVersionTask() {
    setDescription("Cleanup unused SNAPSHOT ElasticBeanstalk Application Version.");
    setGroup("AWS");
  }

  @TaskAction
  public void deleteVersion() {
    // to enable conventionMappings feature
    String appName = getAppName();
    boolean deleteSourceBundle = isDeleteSourceBundle();

    AwsBeanstalkPluginExtension ext = getProject().getExtensions().getByType(AwsBeanstalkPluginExtension.class);
    AWSElasticBeanstalk eb = ext.getClient();

    DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
        .withApplicationName(appName));
    List<String> usingVersions = der.getEnvironments().stream().map(ed -> ed.getVersionLabel())
        .collect(Collectors.toList());

    DescribeApplicationVersionsResult davr = eb.describeApplicationVersions(new DescribeApplicationVersionsRequest()
        .withApplicationName(appName));
    List<String> versionLabelsToDelete = davr.getApplicationVersions().stream().filter(avd ->
        usingVersions.contains(avd.getVersionLabel()) == false && avd.getVersionLabel().contains("-SNAPSHOT-")
        ).map(avd -> avd.getVersionLabel()).collect(Collectors.toList());

    versionLabelsToDelete.forEach(versionLabel -> {
      getLogger().info("version " + versionLabel + " deleted");
      eb.deleteApplicationVersion(new DeleteApplicationVersionRequest()
          .withApplicationName(appName)
          .withVersionLabel(versionLabel)
          .withDeleteSourceBundle(deleteSourceBundle));
    });
  }
}
