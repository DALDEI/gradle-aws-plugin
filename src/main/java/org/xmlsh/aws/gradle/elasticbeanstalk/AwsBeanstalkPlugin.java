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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.WarPlugin;
import org.gradle.api.tasks.bundling.War;
import org.xmlsh.aws.gradle.AwsPlugin;
import org.xmlsh.aws.gradle.s3.AmazonS3Plugin;
import org.xmlsh.aws.gradle.s3.AmazonS3ProgressiveFileUploadTask;

import com.google.common.base.Strings;

/**
 * A plugin which configures a AWS Elastic Beanstalk project.
 */
public class AwsBeanstalkPlugin implements Plugin<Project> {

  public void apply(Project project) {
    project.getPluginManager().apply(AwsPlugin.class);
    project.getPluginManager().apply(AmazonS3Plugin.class);
    project.getExtensions().create(AwsBeanstalkPluginExtension.NAME, AwsBeanstalkPluginExtension.class, project);
    applyTasks(project);
  }

  private void applyTasks(final Project project) {
    AwsBeanstalkPluginExtension ebExt = project.getExtensions().findByType(AwsBeanstalkPluginExtension.class);

    AWSElasticBeanstalkCreateApplicationTask awsEbMigrateApplication = project.getTasks()
        .create("awsEbMigrateApplication", AWSElasticBeanstalkCreateApplicationTask.class, task -> {
          task.doFirst(t -> {
            task.setAppName(ebExt.getAppName());
            task.setAppDesc(ebExt.getAppDesc());
          });
        });

    AmazonS3ProgressiveFileUploadTask awsUploadWar = project.getTasks()
        .create("awsEbUploadBundle", AmazonS3ProgressiveFileUploadTask.class, task -> {
          WarPlugin war = project.getPlugins().findPlugin(WarPlugin.class);
          War warTask = war == null ? null : (War) project.getTasks().getByName(WarPlugin.WAR_TASK_NAME);
          if (war != null) {
            task.dependsOn(warTask);
          }
          task.onlyIf(t -> ebExt.getVersion().getFile() != null || war != null);
          task.doFirst(t -> {
            task.setBucketName(ebExt.getVersion().getBucket());
            task.setKey(ebExt.getVersion().getKey());
            if (warTask != null && ebExt.getVersion().getFile() == null) {
              task.setFile(warTask.getArchivePath());
            }
            else {
              task.setFile(ebExt.getVersion().getFile());
            }
          });
        });

    AWSElasticBeanstalkCreateApplicationVersionTask awsEbCreateApplicationVersion = project.getTasks()
        .create("awsEbCreateApplicationVersion", AWSElasticBeanstalkCreateApplicationVersionTask.class, task -> {
          task.dependsOn(awsEbMigrateApplication, awsUploadWar);
          task.doFirst(t -> {
            task.setAppName(ebExt.getAppName());
            task.setVersionLabel(ebExt.getVersion().getLabel());;
            task.setBucketName(ebExt.getVersion().getBucket());;
            task.setKey(ebExt.getVersion().getKey());;
          });
        });

    AWSElasticBeanstalkCreateConfigurationTemplateTask awsEbMigrateConfigurationTemplates = project.getTasks()
        .create("awsEbMigrateConfigurationTemplates", AWSElasticBeanstalkCreateConfigurationTemplateTask.class,
            task -> {
              task.dependsOn(awsEbMigrateApplication);
              task.doFirst(t -> {
                task.setAppName(ebExt.getAppName());
                task.setConfigurationTemplates(ebExt.getConfigurationTemplates());;
              });
            });

    AWSElasticBeanstalkCreateEnvironmentTask awsEbMigrateEnvironment = project.getTasks()
        .create("awsEbMigrateEnvironment", AWSElasticBeanstalkCreateEnvironmentTask.class, task -> {
          task.dependsOn(awsEbMigrateConfigurationTemplates, awsEbCreateApplicationVersion);
          task.doFirst(t -> {
            task.setAppName(ebExt.getAppName());
            task.setEnvName(ebExt.getEnvironment().getEnvName());;
            task.setEnvDesc(ebExt.getEnvironment().getEnvDesc());
            task.setTemplateName(ebExt.getEnvironment().getTemplateName());
            task.setVersionLabel(ebExt.getEnvironment().getVersionLabel());
            task.setTier(ebExt.getTier() != null ? ebExt.getTier() : Tier.WebServer);
            if (Strings.isNullOrEmpty(ebExt.getEnvironment().getCnamePrefix()) == false) {
              task.setCnamePrefix(ebExt.getEnvironment().getCnamePrefix());
            }
          });
        });

    AWSElasticBeanstalkTerminateEnvironmentTask awsEbTerminateEnvironment = project.getTasks()
        .create("awsEbTerminateEnvironment", AWSElasticBeanstalkTerminateEnvironmentTask.class, task -> {
          task.doFirst(t -> {
            task.setAppName(ebExt.getAppName());
            task.setEnvName(ebExt.getEnvironment().getEnvName());;
          });
        });

    project.getTasks().create("awsEbWaitEnvironmentReady", AWSElasticBeanstalkWaitEnvironmentStatusTask.class,
        task -> {
          task.mustRunAfter(awsEbMigrateEnvironment);
          task.doFirst(t -> {
            task.setAppName(ebExt.getAppName());
            task.setEnvName(ebExt.getEnvironment().getEnvName());;
          });
        });

    AWSElasticBeanstalkWaitEnvironmentStatusTask awsEbWaitEnvironmentTerminated = project.getTasks()
        .create("awsEbWaitEnvironmentTerminated", AWSElasticBeanstalkWaitEnvironmentStatusTask.class, task -> {
          task.mustRunAfter(awsEbTerminateEnvironment);
          task.doFirst(t -> {
            task.setAppName(ebExt.getAppName());
            task.setEnvName(ebExt.getEnvironment().getEnvName());
            task.setSuccessStatuses(Arrays.asList("Terminated"));
            task.setWaitStatuses(Arrays.asList(
                "Launching",
                "Updating",
                "Terminating",
                "Ready"
                ));
          });
        });

    Task awsEbTerminateEnvironmentAndWaitTerminated = project.getTasks()
        .create("awsEbTerminateEnvironmentAndWaitTerminated")
        .dependsOn(awsEbTerminateEnvironment, awsEbWaitEnvironmentTerminated);

    project.getTasks().create("awsEbCleanupApplicationVersions",
        AWSElasticBeanstalkCleanupApplicationVersionTask.class, task -> {
          task.doFirst(t -> {
            task.setAppName(ebExt.getAppName());
          });
        });

    project.getTasks().create("awsEbDeleteApplication", AWSElasticBeanstalkDeleteApplicationTask.class, task -> {
      task.dependsOn(awsEbTerminateEnvironmentAndWaitTerminated);
      task.doFirst(t -> {
        task.setAppName(ebExt.getAppName());
      });
    });
  }
}
