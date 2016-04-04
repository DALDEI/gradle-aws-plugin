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

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;

public class AmazonEC2StopInstanceTask extends ConventionTask {

  @Getter
  @Setter
  private List<String> instanceIds = new ArrayList<>();

  @Getter
  private StopInstancesResult stopInstancesResult;

  public AmazonEC2StopInstanceTask() {
    setDescription("Stop EC2 instance.");
    setGroup("AWS");
  }

  @TaskAction
  public void stopInstance() {
    // to enable conventionMappings feature
    List<String> instanceIds = getInstanceIds();

    if (instanceIds.isEmpty())
      return;

    AmazonEC2PluginExtension ext = getProject().getExtensions().getByType(AmazonEC2PluginExtension.class);
    AmazonEC2 ec2 = ext.getClient();

    stopInstancesResult = ec2.stopInstances(new StopInstancesRequest(instanceIds));
    getLogger().info("Stop EC2 instance {} requested", instanceIds);
  }
}
