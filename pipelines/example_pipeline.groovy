env.organization="SomeOrg"
// Slave Settings
def jenkins_slave_label='abcdef-vmua671.abc.net'

// GIT Settings
def directory_to_checkout_into='library'
def branch_pattern_to_base_checkout_on='*/master'
def predefined_jenkins_credentials_to_use_for_checkout='BBkey'
def git_repo_url='ssh://git@abc-wscm-sourcerepo.somedomain.com:7999/jen/jenkins-functions.git'

// Define Choice Parameters
def my_choice_list='option one\noption two\noption three'

node(jenkins_slave_label) {
  // GIT Checkout based on Variables above
  def checkoutVars=checkout([$class: 'GitSCM', branches: [[name: branch_pattern_to_base_checkout_on ]], 
    doGenerateSubmoduleConfigurations: false, 
    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: directory_to_checkout_into ]], 
    submoduleCfg: [], 
    userRemoteConfigs: [[credentialsId: predefined_jenkins_credentials_to_use_for_checkout, 
      url: git_repo_url ]]])

  env.git_commit = checkoutVars['GIT_COMMIT']
   //env.git_branch = checkoutVars['GIT_BRANCH']
  env.currentGitBranch = sh(returnStdout: true, script: "cd ${directory_to_checkout_into}; git branch|awk '{if (\$1 == \"*\") print \$NF}'").trim()

  // Set Build Choice Parameters
  properties([
    parameters([ 
      booleanParam(defaultValue: false, description: 'Refresh Drop Down Selector and Exit', name: 'refresh_choices'),
      choice(choices: my_choice_list, description: 'Example option selections', name: 'dropdownchoices')
    ]), 
    // If you'd like this job to Poll the GIT repo checked out above, uncomment the following line and comment the one after -
    // pipelineTriggers([pollSCM('')])
    pipelineTriggers([])
  ])
  
  // When reading build parameters from a pipeline changes arent picked up until the next run, so a workaround is to implement a check box that can be ticked so the job will run but not do anything - effectively refreshing the choices for the next run.
  if (refresh_choices == "true") { return }

  //Pipeline jobs can be split into stages, which allow for common tasks to be organised such as 'build', 'publish', 'promote', 'deploy' etc.
  stage("stage one") {
    // run some shell commands on the slave
    sh """#!/bin/bash
        echo 'The branch I checked out was ${git_branch}'
	echo 'The commit was ${git_commit}'
       """
  }
  stage("stage two") {
    // call another job from this pipeline
    build job: '/Red_Hat_Satellite/Content Views/Promote', parameters: [[$class: 'StringParameterValue', name: 'Content_View', value: content_view], [$class: 'StringParameterValue', name: 'Lifecycle_Environment', value: lifecycles[i]]]
  }
}
