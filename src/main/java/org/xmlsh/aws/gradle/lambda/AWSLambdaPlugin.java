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
package org.xmlsh.aws.gradle.lambda;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.xmlsh.aws.gradle.AwsPlugin;

public class AWSLambdaPlugin implements Plugin<Project> {

  public void apply(Project project) {
    project.getPluginManager().apply(AwsPlugin.class);
    project.getExtensions().create(AWSLambdaPluginExtension.NAME, AWSLambdaPluginExtension.class, project);
  }
}
