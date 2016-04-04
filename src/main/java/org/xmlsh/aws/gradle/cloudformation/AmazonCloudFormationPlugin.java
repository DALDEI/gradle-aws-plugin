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
package org.xmlsh.aws.gradle.cloudformation;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.xmlsh.aws.gradle.AwsPlugin;
import org.xmlsh.aws.gradle.s3.AmazonS3FileUploadTask;
import org.xmlsh.aws.gradle.s3.AmazonS3Plugin;

import com.amazonaws.services.cloudformation.model.Parameter;

public class AmazonCloudFormationPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.getPluginManager().apply(AwsPlugin.class);
    project.getPluginManager().apply(AmazonS3Plugin.class);
    project.getExtensions().create(AmazonCloudFormationPluginExtension.NAME, AmazonCloudFormationPluginExtension.class,
        project);
    applyTasks(project);
  }

  private void applyTasks(Project project) {
    AmazonCloudFormationPluginExtension cfnExt =
        project.getExtensions().findByType(AmazonCloudFormationPluginExtension.class);

    AmazonCloudFormationMigrateStackTask awsCfnMigrateStack = project.getTasks()
        .create("awsCfnMigrateStack", AmazonCloudFormationMigrateStackTask.class, task -> {
          task.setDescription("Create/Migrate cfn stack.");
          task.conventionMapping("stackName", () -> cfnExt.getStackName());
          task.conventionMapping("capabilityIam", () -> cfnExt.isCapabilityIam());
          task.conventionMapping("templateBody", () -> cfnExt.getTemplateBody());
          task.conventionMapping("cfnStackParams", () -> cfnExt.getStackParams().entrySet().stream()
              .map(it -> new Parameter()
                  .withParameterKey(it.getKey().toString())
                  .withParameterValue(it.getValue().toString()))
              .collect(Collectors.toList()));
        });

    AmazonCloudFormationUpdateStackTask awsCfnUpdateStack = project.getTasks()
        .create("awsCfnUpdateStack", AmazonCloudFormationUpdateStackTask.class, task -> {
          task.setDescription("Update cfn stack.");
          task.conventionMapping("stackName", () -> cfnExt.getStackName());
          task.conventionMapping("capabilityIam", () -> cfnExt.isCapabilityIam());
          task.conventionMapping("templateBody", () -> cfnExt.getTemplateBody());
          task.conventionMapping("cfnStackParams", () -> cfnExt.getStackParams().entrySet().stream()
              .map(it -> new Parameter()
                  .withParameterKey(it.getKey().toString())
                  .withParameterValue(it.getValue().toString()))
              .collect(Collectors.toList()));
        });

    AmazonCloudFormationCreateStackTask awsCfnCreateStack = project.getTasks()
        .create("awsCfnCreateStack", AmazonCloudFormationCreateStackTask.class, task -> {
          task.setDescription("Create cfn stack.");
          task.conventionMapping("stackName", () -> cfnExt.getStackName());
          task.conventionMapping("capabilityIam", () -> cfnExt.isCapabilityIam());
          task.conventionMapping("templateBody", () -> cfnExt.getTemplateBody());
          task.conventionMapping("disableRollback", () -> cfnExt.isDisableRollback());
          task.conventionMapping("tags", () -> cfnExt.getTags());
          task.conventionMapping("onFailure", () -> cfnExt.getOnFailure());
          task.conventionMapping("templateBody", () -> cfnExt.getTemplateBody());
          task.conventionMapping("cfnStackParams", () -> cfnExt.getStackParams().entrySet().stream()
              .map(it -> new Parameter()
                  .withParameterKey(it.getKey().toString())
                  .withParameterValue(it.getValue().toString()))
              .collect(Collectors.toList()));
        });

    project.getTasks().create("awsCfnWaitStackReady", AmazonCloudFormationWaitStackStatusTask.class, task -> {
      task.setDescription("Wait cfn stack for *_COMPLETE status.");
      task.mustRunAfter(awsCfnMigrateStack);
      task.conventionMapping("stackName", () -> cfnExt.getStackName());
    });

    AmazonCloudFormationWaitStackStatusTask awsCfnWaitStackComplete =
        project.getTasks().create("awsCfnWaitStackComplete", AmazonCloudFormationWaitStackStatusTask.class,
            task -> {
              task.setDescription("Wait cfn stack for CREATE_COMPETE or UPDATE_COMPLETE status.");
              task.mustRunAfter(awsCfnMigrateStack);
              task.setSuccessStatuses(Arrays.asList("CREATE_COMPLETE", "UPDATE_COMPLETE"));
              task.conventionMapping("stackName", () -> cfnExt.getStackName());
            });

    project.getTasks().create("awsCfnMigrateStackAndWaitCompleted")
        .dependsOn(awsCfnMigrateStack, awsCfnWaitStackComplete)
        .setDescription("Create/Migrate cfn stack, and wait stack for CREATE_COMPETE or UPDATE_COMPLETE status.");

    project.getTasks().create("awsCfnCreateStackAndWaitCompleted")
        .dependsOn(awsCfnCreateStack, awsCfnWaitStackComplete)
        .setDescription("Create cfn stack, and wait stack for CREATE_COMPETE or UPDATE_COMPLETE status.");

    project.getTasks().create("awsCfnUpdateStackAndWaitCompleted")
        .dependsOn(awsCfnUpdateStack, awsCfnWaitStackComplete)
        .setDescription("Update cfn stack, and wait stack for CREATE_COMPETE or UPDATE_COMPLETE status.");

    AmazonCloudFormationDeleteStackTask awsCfnDeleteStack =
        project.getTasks().create("awsCfnDeleteStack", AmazonCloudFormationDeleteStackTask.class, task -> {
          task.setDescription("Delete cfn stack.");
          task.conventionMapping("stackName", () -> cfnExt.getStackName());
        });

    AmazonCloudFormationWaitStackStatusTask awsCfnWaitStackDeleted =
        project.getTasks().create("awsCfnWaitStackDeleted", AmazonCloudFormationWaitStackStatusTask.class,
            task -> {
              task.setDescription("Wait cfn stack for DELETE_COMPLETE status.");
              task.mustRunAfter(awsCfnDeleteStack);
              task.setSuccessStatuses(Arrays.asList("DELETE_COMPLETE"));
              task.conventionMapping("stackName", () -> cfnExt.getStackName());
            });

    project.getTasks().create("awsCfnDeleteStackAndWaitCompleted")
        .dependsOn(awsCfnDeleteStack, awsCfnWaitStackDeleted)
        .setDescription("Delete cfn stack, and wait stack for DELETE_COMPLETE status.");
  }

}
