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

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Stack;

public class AmazonCloudFormationWaitStackStatusTask extends ConventionTask {

  @Getter
  @Setter
  private String stackName;

  @Getter
  @Setter
  private List<String> successStatuses = Arrays.asList(
      "CREATE_COMPLETE",
      "UPDATE_COMPLETE",
      "ROLLBACK_COMPLETE",
      "UPDATE_ROLLBACK_COMPLETE",
      "DELETE_COMPLETE"
      );

  @Getter
  @Setter
  private List<String> waitStatuses = Arrays.asList(
      "CREATE_IN_PROGRESS",
      "ROLLBACK_IN_PROGRESS",
      "DELETE_IN_PROGRESS",
      "UPDATE_IN_PROGRESS",
      "UPDATE_COMPLETE_CLEANUP_IN_PROGRESS",
      "UPDATE_ROLLBACK_IN_PROGRESS",
      "UPDATE_ROLLBACK_COMPLETE_CLEANUP_IN_PROGRESS"
      );

  @Getter
  @Setter
  private int loopTimeout = 900; // sec

  @Getter
  @Setter
  private int loopWait = 10; // sec

  @Getter
  @Setter
  private boolean found;

  @Getter
  @Setter
  private String lastStatus;

  @Getter
  @Setter
  private String lastEvents;

  public AmazonCloudFormationWaitStackStatusTask() {
    setDescription("Wait cfn stack for specific status.");
    setGroup("AWS");
  }

  @TaskAction
  public void waitStackForStatus() throws InterruptedException {
    // to enable conventionMappings feature
    String stackName = getStackName();
    List<String> successStatuses = getSuccessStatuses();
    List<String> waitStatuses = getWaitStatuses();
    int loopTimeout = getLoopTimeout();
    int loopWait = getLoopWait();

    if (stackName == null)
      throw new GradleException("stackName is not specified");

    AmazonCloudFormationPluginExtension ext = getProject().getExtensions().getByType(
        AmazonCloudFormationPluginExtension.class);
    AmazonCloudFormation cfn = ext.getClient();

    long start = System.currentTimeMillis();
    while (true) {
      if (System.currentTimeMillis() > start + (loopTimeout * 1000)) {
        throw new GradleException("Timeout");
      }
      try {
        DescribeStacksResult describeStackResult = cfn.describeStacks(new DescribeStacksRequest()
            .withStackName(stackName));
        Stack stack = describeStackResult.getStacks().get(0);
        if (stack == null) {
          throw new GradleException("stack " + stackName + " is not exists");
        }
        found = true;
        lastStatus = stack.getStackStatus();
        if (successStatuses.contains(lastStatus)) {
          getLogger().info("Status of stack {} is now {}.", stackName, lastStatus);
          printOutputs(stack);
          break;
        }
        else if (waitStatuses.contains(lastStatus)) {
          getLogger().info("Status of stack {} is {}...", stackName, lastStatus);
          Thread.sleep(loopWait * 1000);
        }
        else {
          // waitStatusesでもsuccessStatusesないステータスはfailとする
          throw new GradleException("Status of stack " + stackName + " is " + lastStatus + ".  It seems to be failed.");
        }
      } catch (AmazonServiceException e) {
        if (found) {
          break;
        }
        else {
          throw new GradleException("Fail to describe stack: " + stackName, e);
        }
      }
    }
  }

  private void printOutputs(Stack stack) {
    getLogger().info("==== Outputs ====");
    stack.getOutputs().stream()
        .forEach(o -> getLogger().info("{} ({}) = {}", o.getOutputKey(), o.getDescription(), o.getOutputValue()));
  }
}
