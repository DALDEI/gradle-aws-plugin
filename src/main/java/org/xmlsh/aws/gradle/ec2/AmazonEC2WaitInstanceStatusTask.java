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
package org.xmlsh.aws.gradle.ec2;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;

public class AmazonEC2WaitInstanceStatusTask extends ConventionTask {

  @Getter
  @Setter
  private String instanceId;

  @Getter
  @Setter
  private List<String> successStatuses = Arrays.asList(
      "running",
      "stopped",
      "terminated"
      );

  @Getter
  @Setter
  private List<String> waitStatuses = Arrays.asList(
      "pending",
      "shutting-down",
      "stopping"
      );

  @Getter
  @Setter
  private int loopTimeout = 900; // sec

  @Getter
  @Setter
  private int loopWait = 10; // sec

  @Getter
  private boolean found;

  @Getter
  private String lastStatus;

  public AmazonEC2WaitInstanceStatusTask() {
    setDescription("Wait EC2 instance for specific status.");
    setGroup("AWS");
  }

  @TaskAction
  public void waitInstanceForStatus() {
    // to enable conventionMappings feature
    String instanceId = getInstanceId();
    List<String> successStatuses = getSuccessStatuses();
    List<String> waitStatuses = getWaitStatuses();
    int loopTimeout = getLoopTimeout();
    int loopWait = getLoopWait();

    if (instanceId == null)
      throw new GradleException("instanceId is not specified");

    AmazonEC2PluginExtension ext = getProject().getExtensions().getByType(AmazonEC2PluginExtension.class);
    AmazonEC2 ec2 = ext.getClient();

    long start = System.currentTimeMillis();
    while (true) {
      if (System.currentTimeMillis() > start + (loopTimeout * 1000)) {
        throw new GradleException("Timeout");
      }
      try {
        DescribeInstancesResult dir = ec2.describeInstances(new DescribeInstancesRequest()
            .withInstanceIds(instanceId));
        Instance instance = dir.getReservations().get(0).getInstances().get(0);
        if (instance == null) {
          throw new GradleException(instanceId + " is not exists");
        }

        found = true;
        lastStatus = instance.getState().getName();
        if (successStatuses.contains(lastStatus)) {
          getLogger().info("Status of instance {} is now {}.", instanceId, lastStatus);
          break;
        }
        else if (waitStatuses.contains(lastStatus)) {
          getLogger().info("Status of instance {} is {}...", instanceId, lastStatus);
          try {
            Thread.sleep(loopWait * 1000);
          } catch (InterruptedException e) {
            throw new GradleException("Sleep interrupted", e);
          }
        }
        else {
          // fail when current status is not waitStatuses or successStatuses
          throw new GradleException("Status of " + instanceId + " is " + lastStatus + ".  It seems to be failed.");
        }
      } catch (AmazonServiceException e) {
        if (found) {
          break;
        }
        else {
          throw new GradleException("Fail to describe instance: " + instanceId, e);
        }
      }
    }
  }
}
