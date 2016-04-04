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
import com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest;

public class AWSElasticBeanstalkDeleteConfigurationTemplateTask extends ConventionTask {

  @Getter
  @Setter
  private String applicationName;

  @Getter
  @Setter
  private String templateName;

  public AWSElasticBeanstalkDeleteConfigurationTemplateTask() {
    setDescription("Delete ElasticBeanstalk Configuration Templates.");
    setGroup("AWS");
  }

  @TaskAction
  public void deleteTemplate() {
    // to enable conventionMappings feature
    String applicationName = getApplicationName();
    String templateName = getTemplateName();

    AwsBeanstalkPluginExtension ext = getProject().getExtensions().getByType(AwsBeanstalkPluginExtension.class);
    AWSElasticBeanstalk eb = ext.getClient();

    eb.deleteConfigurationTemplate(new DeleteConfigurationTemplateRequest()
        .withApplicationName(applicationName)
        .withTemplateName(templateName));

    getLogger().info("configuration template " + templateName + " @ " + applicationName + " deleted");
  }
}
