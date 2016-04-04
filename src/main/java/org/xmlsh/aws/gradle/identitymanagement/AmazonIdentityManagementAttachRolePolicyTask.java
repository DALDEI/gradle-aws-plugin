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
package org.xmlsh.aws.gradle.identitymanagement;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.AttachRolePolicyRequest;

public class AmazonIdentityManagementAttachRolePolicyTask extends ConventionTask {

  @Getter
  @Setter
  private String path = "/";

  @Getter
  @Setter
  private String roleName;

  @Getter
  @Setter
  private List<String> policyArns = new ArrayList<>();

  public AmazonIdentityManagementAttachRolePolicyTask() {
    setDescription("Attach managed policies to role.");
    setGroup("AWS");
  }

  @TaskAction
  public void attachRolePolicy() {
    // to enable conventionMappings feature
    String roleName = getRoleName();

    if (roleName == null)
      throw new GradleException("roleName is required");

    AmazonIdentityManagementPluginExtension ext = getProject().getExtensions().getByType(
        AmazonIdentityManagementPluginExtension.class);
    AmazonIdentityManagement iam = ext.getClient();

    policyArns.stream().forEach(policyArn -> {
      iam.attachRolePolicy(new AttachRolePolicyRequest()
          .withRoleName(roleName)
          .withPolicyArn(policyArn));
      getLogger().info("Attach Managed policy {} to Role {} requested", policyArn, roleName);
    });
  }
}
