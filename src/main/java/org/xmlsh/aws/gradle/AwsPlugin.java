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
package org.xmlsh.aws.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AwsPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.getExtensions().create(AwsPluginExtension.NAME, AwsPluginExtension.class, project);
  }
}
