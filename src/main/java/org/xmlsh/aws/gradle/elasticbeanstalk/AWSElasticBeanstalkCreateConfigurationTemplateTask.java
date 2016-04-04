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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalk;
import com.amazonaws.services.elasticbeanstalk.model.ApplicationDescription;
import com.amazonaws.services.elasticbeanstalk.model.ConfigurationOptionSetting;
import com.amazonaws.services.elasticbeanstalk.model.CreateConfigurationTemplateRequest;
import com.amazonaws.services.elasticbeanstalk.model.DeleteConfigurationTemplateRequest;
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationsRequest;
import com.amazonaws.services.elasticbeanstalk.model.UpdateConfigurationTemplateRequest;

public class AWSElasticBeanstalkCreateConfigurationTemplateTask extends ConventionTask {

  @Getter
  @Setter
  private String appName;

  @Getter
  @Setter
  private Collection<EbConfigurationTemplateExtension> configurationTemplates = new ArrayList<>();

  @Getter
  @Setter
  private String defaultSolutionStackName = "64bit Amazon Linux 2013.09 running Tomcat 7 Java 7";

  public AWSElasticBeanstalkCreateConfigurationTemplateTask() {
    setDescription("Create / Migrate ElasticBeanstalk Configuration Templates.");
    setGroup("AWS");
  }

  @TaskAction
  public void createTemplate() {
    // to enable conventionMappings feature
    String appName = getAppName();

    AwsBeanstalkPluginExtension ext = getProject().getExtensions().getByType(AwsBeanstalkPluginExtension.class);
    AWSElasticBeanstalk eb = ext.getClient();

    configurationTemplates.forEach(config -> {
      String templateName = config.getName();
      String templateDesc = config.getDesc();
      String solutionStackName = config.getSolutionStackName() != null ? config.getSolutionStackName()
          : getDefaultSolutionStackName();
      boolean deleteTemplateIfExists = config.isRecreate();

      try {
        List<ConfigurationOptionSetting> optionSettings = loadConfigurationOptions(config.getOptionSettings());
        List<ApplicationDescription> existingApps = eb.describeApplications(new DescribeApplicationsRequest()
            .withApplicationNames(appName)).getApplications();
        if (existingApps.isEmpty()) {
          throw new IllegalArgumentException("App with name '" + appName + "' does not exist");
        }

        if (existingApps.get(0).getConfigurationTemplates().contains(templateName)) {
          if (deleteTemplateIfExists) {
            eb.deleteConfigurationTemplate(new DeleteConfigurationTemplateRequest()
                .withApplicationName(appName)
                .withTemplateName(templateName));
            getLogger().info("configuration template {} @ {} deleted", templateName, appName);
          }
          else {
            eb.updateConfigurationTemplate(new UpdateConfigurationTemplateRequest()
                .withApplicationName(appName)
                .withTemplateName(templateName)
                .withDescription(templateDesc)
                .withOptionSettings(optionSettings));
            getLogger().info("configuration template {} @ {} updated", templateName, appName);
            return;
          }
        }

        eb.createConfigurationTemplate(new CreateConfigurationTemplateRequest()
            .withApplicationName(appName)
            .withTemplateName(templateName)
            .withDescription(templateDesc)
            .withSolutionStackName(solutionStackName)
            .withOptionSettings(optionSettings));
        getLogger().info("configuration template {} @ {} created", templateName, appName);
      } catch (IOException e) {
        getLogger().error("IOException", e);
      }
    });
  }

  List<ConfigurationOptionSetting> loadConfigurationOptions(String json) {
    List<ConfigurationOptionSetting> options = new ArrayList<>();
    @SuppressWarnings("unchecked")
    Collection<Map<String, Object>> c = (Collection<Map<String, Object>>) new groovy.json.JsonSlurper().parseText(json);
    c.forEach(it ->
        options.add(new ConfigurationOptionSetting((String) it.get("Namespace"), (String) it.get("OptionName"),
            (String) it.get("Value")))
        );
    return options;
  }
}
