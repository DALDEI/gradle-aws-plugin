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
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.CreateRoleResult;

public class AmazonIdentityManagementCreateRoleTask extends ConventionTask {

  @Getter
  @Setter
  private String path = "/";

  @Getter
  @Setter
  private String roleName;

  @Getter
  @Setter
  private String assumeRolePolicyDocument;

  @Getter
  @Setter
  private List<String> policyArns = new ArrayList<>();

  @Getter
  private CreateRoleResult createRole;

  public AmazonIdentityManagementCreateRoleTask() {
    setDescription("Create Role.");
    setGroup("AWS");
  }

  @TaskAction
  public void createRole() {
    // to enable conventionMappings feature
    String roleName = getRoleName();
    String assumeRolePolicyDocument = getAssumeRolePolicyDocument();

    if (roleName == null)
      throw new GradleException("roleName is required");
    if (assumeRolePolicyDocument == null)
      throw new GradleException("assumeRolePolicyDocument is required");

    AmazonIdentityManagementPluginExtension ext = getProject().getExtensions().getByType(
        AmazonIdentityManagementPluginExtension.class);
    AmazonIdentityManagement iam = ext.getClient();

    CreateRoleRequest request = new CreateRoleRequest()
        .withRoleName(roleName)
        .withPath(getPath())
        .withAssumeRolePolicyDocument(assumeRolePolicyDocument);
    createRole = iam.createRole(request);
    getLogger().info("Create Role requested: {}", createRole.getRole().getArn());
    policyArns.stream().forEach(policyArn -> {
      iam.attachRolePolicy(new AttachRolePolicyRequest()
          .withRoleName(roleName)
          .withPolicyArn(policyArn));
      getLogger().info("Attach Managed policy {} to Role {} requested", policyArn, roleName);
    });
  }
}
