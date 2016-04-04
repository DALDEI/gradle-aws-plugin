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

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;

public class AmazonEC2AuthorizeSecurityGroupIngressTask extends AbstractAmazonEC2SecurityGroupPermissionTask {

  @Getter
  @Setter
  private String groupId;

  @Getter
  @Setter
  private Object ipPermissions;

  public AmazonEC2AuthorizeSecurityGroupIngressTask() {
    setDescription("Authorize security group ingress.");
    setGroup("AWS");
  }

  @TaskAction
  public void authorizeIngress() {
    // to enable conventionMappings feature
    String groupId = getGroupId();
    Object ipPermissions = getIpPermissions();

    if (groupId == null)
      throw new GradleException("groupId is not specified");
    if (ipPermissions == null)
      throw new GradleException("ipPermissions is not specified");

    AmazonEC2PluginExtension ext = getProject().getExtensions().getByType(AmazonEC2PluginExtension.class);
    AmazonEC2 ec2 = ext.getClient();

    try {
      ec2.authorizeSecurityGroupIngress(new AuthorizeSecurityGroupIngressRequest()
          .withGroupId(groupId)
          .withIpPermissions(parse(ipPermissions)));
    } catch (AmazonServiceException e) {
      if (e.getErrorCode().equals("InvalidPermission.Duplicate")) {
        getLogger().warn(e.getMessage());
      }
      else {
        throw e;
      }
    }
  }
}
