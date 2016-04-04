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
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentRequest;
import com.amazonaws.services.elasticbeanstalk.model.CreateEnvironmentResult;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsResult;
import com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest;

public class AWSElasticBeanstalkCreateEnvironmentTask extends ConventionTask {

  @Getter
  @Setter
  private String appName;

  @Getter
  @Setter
  private String envName;

  @Getter
  @Setter
  private String envDesc = "";

  @Getter
  @Setter
  private String cnamePrefix = java.util.UUID.randomUUID().toString();

  @Getter
  @Setter
  private String templateName;

  @Getter
  @Setter
  private String versionLabel;

  @Getter
  @Setter
  private Tier tier = Tier.WebServer;

  public AWSElasticBeanstalkCreateEnvironmentTask() {
    setDescription("Create/Migrate ElasticBeanstalk Environment.");
    setGroup("AWS");
  }

  @TaskAction
  public void createEnvironment() {
    // to enable conventionMappings feature
    String appName = getAppName();
    String envName = getEnvName();
    String envDesc = getEnvDesc();
    String cnamePrefix = getCnamePrefix();
    String templateName = getTemplateName();
    String versionLabel = getVersionLabel();
    Tier tier = getTier();

    AwsBeanstalkPluginExtension ext = getProject().getExtensions().getByType(AwsBeanstalkPluginExtension.class);
    AWSElasticBeanstalk eb = ext.getClient();

    DescribeEnvironmentsResult der = eb.describeEnvironments(new DescribeEnvironmentsRequest()
        .withApplicationName(appName)
        .withEnvironmentNames(envName)
        .withIncludeDeleted(false));

    if (der.getEnvironments() == null || der.getEnvironments().isEmpty()) {
      CreateEnvironmentRequest req = new CreateEnvironmentRequest()
          .withApplicationName(appName)
          .withEnvironmentName(envName)
          .withDescription(envDesc)
          .withTemplateName(templateName)
          .withVersionLabel(versionLabel)
          .withTier(tier.toEnvironmentTier());
      if (tier == Tier.WebServer) {
        req.withCNAMEPrefix(cnamePrefix);
      }
      CreateEnvironmentResult result = eb.createEnvironment(req);
      getLogger().info("environment " + envName + " @ " + appName + " (" + result.getEnvironmentId() + ") created");
    }
    else {
      String environmentId = der.getEnvironments().get(0).getEnvironmentId();

      eb.updateEnvironment(new UpdateEnvironmentRequest()
          .withEnvironmentId(environmentId)
          .withEnvironmentName(envName)
          .withDescription(envDesc)
          .withTemplateName(templateName)
          .withVersionLabel(versionLabel)
          .withTier(tier.toEnvironmentTier()));
      getLogger().info("environment " + envName + " @ " + appName + " (" + environmentId + ") updated");
    }
  }
}
