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
package org.xmlsh.aws.gradle.rds;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.gradle.api.GradleException;
import org.gradle.api.internal.ConventionTask;
import org.gradle.api.tasks.TaskAction;

import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.ModifyDBInstanceRequest;

public class AmazonRDSModifyDBInstanceTask extends ConventionTask {

  @Getter
  @Setter
  private String dbInstanceIdentifier;

  @Getter
  @Setter
  private Integer allocatedStorage;

  @Getter
  @Setter
  private String dbInstanceClass;

  @Getter
  @Setter
  private String masterUserPassword;

  @Getter
  @Setter
  private List<String> vpcSecurityGroupIds;

  @Getter
  @Setter
  private String preferredMaintenanceWindow;

  @Getter
  @Setter
  private String dbParameterGroupName;

  @Getter
  @Setter
  private Integer backupRetentionPeriod;

  @Getter
  @Setter
  private String preferredBackupWindow;

  @Getter
  @Setter
  private Boolean multiAZ;

  @Getter
  @Setter
  private String engineVersion;

  @Getter
  @Setter
  private Boolean autoMinorVersionUpgrade;

  @Getter
  @Setter
  private Integer iops;

  @Getter
  @Setter
  private String optionGroupName;

  @Getter
  @Setter
  private String storageType;

  @Getter
  @Setter
  private String tdeCredentialArn;

  @Getter
  @Setter
  private String tdeCredentialPassword;

  @Getter
  private DBInstance dbInstance;

  public AmazonRDSModifyDBInstanceTask() {
    setDescription("Modify RDS instance.");
    setGroup("AWS");
  }

  @TaskAction
  public void modifyDBInstance() {
    // to enable conventionMappings feature
    String dbInstanceIdentifier = getDbInstanceIdentifier();

    if (dbInstanceIdentifier == null)
      throw new GradleException("dbInstanceIdentifier is required");

    AmazonRDSPluginExtension ext = getProject().getExtensions().getByType(AmazonRDSPluginExtension.class);
    AmazonRDS rds = ext.getClient();

    ModifyDBInstanceRequest request = new ModifyDBInstanceRequest()
        .withDBInstanceIdentifier(dbInstanceIdentifier)
        .withAllocatedStorage(getAllocatedStorage())
        .withDBInstanceClass(getDbInstanceClass())
        .withMasterUserPassword(getMasterUserPassword())
        .withVpcSecurityGroupIds(getVpcSecurityGroupIds())
        .withPreferredMaintenanceWindow(getPreferredMaintenanceWindow())
        .withDBParameterGroupName(getDbParameterGroupName())
        .withBackupRetentionPeriod(getBackupRetentionPeriod())
        .withPreferredBackupWindow(getPreferredBackupWindow())
        .withMultiAZ(getMultiAZ())
        .withEngineVersion(getEngineVersion())
        .withAutoMinorVersionUpgrade(getAutoMinorVersionUpgrade())
        .withIops(getIops())
        .withOptionGroupName(getOptionGroupName())
        .withStorageType(getStorageType())
        .withTdeCredentialArn(getTdeCredentialArn())
        .withTdeCredentialPassword(getTdeCredentialPassword());
    dbInstance = rds.modifyDBInstance(request);
    getLogger().info("Modify RDS instance requested: {}", dbInstance.getDBInstanceIdentifier());
  }
}
