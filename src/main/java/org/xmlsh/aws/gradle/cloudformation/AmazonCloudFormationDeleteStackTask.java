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

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;

public class AmazonCloudFormationDeleteStackTask extends ConventionTask {

  @Getter
  @Setter
  private String stackName;

  public AmazonCloudFormationDeleteStackTask() {
    setDescription("Delete cfn stack.");
    setGroup("AWS");
  }

  @TaskAction
  public void deleteStack() {
    // to enable conventionMappings feature
    String stackName = getStackName();

    if (stackName == null)
      throw new GradleException("stackName is not specified");

    AmazonCloudFormationPluginExtension ext = getProject().getExtensions().getByType(
        AmazonCloudFormationPluginExtension.class);
    AmazonCloudFormation cfn = ext.getClient();

    cfn.deleteStack(new DeleteStackRequest().withStackName(stackName));
    getLogger().info("delete stack " + stackName + " requested");
  }
}
