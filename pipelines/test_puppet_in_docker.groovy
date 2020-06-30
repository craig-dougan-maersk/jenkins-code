def repositoryName='pit_ibm_install_manager'
def bitbucket_project_code='apmw'
def bitbucket_hostname='abc-wscm-sourcerepo.somedomain.com'
def bitbucket_ssh_port='7999'
def cloneURL="ssh://git@${bitbucket_hostname}:${bitbucket_ssh_port}/${bitbucket_project_code}/${repositoryName}.git"
def jenkinsCredentials='rhel7_docker_puppet_tester'
def jenkinsSlave='abcdef-vmua671.abc.net'
def jenkinsPipelineTestingURL='jenkins://a001010101001342343s/Red_Hat_Satellite/Puppet/Build/puppet_pipeline_pullreq'

properties([pipelineTriggers([pollSCM('')])])

node(jenkinsSlave) {
    stage('Check out Code') {
      checkoutVars = checkout([$class: 'GitSCM', branches: [[name: '*/feature/*']],
        doGenerateSubmoduleConfigurations: false,
        extensions: [
          [$class: 'RelativeTargetDirectory', relativeTargetDir: repositoryName]
        ],
        submoduleCfg: [],
        userRemoteConfigs: [[credentialsId: jenkinsCredentials,url: cloneURL]]])
        
      //env.git_commit = sh(returnStdout: true, script: "cd ${repositoryName};git rev-parse HEAD").trim()
      env.git_commit = checkoutVars['GIT_COMMIT']
      env.git_branch = checkoutVars['GIT_BRANCH']
      //env.git_branch = sh(returnStdout: true, script: "cd ${repositoryName};git status| head -1|cut -d' ' -f3-").trim()
    }
    stage('Run remote job') {
      def mybuild = triggerRemoteJob mode: 
        [
          $class: 'TrackProgressAwaitResult',
          scheduledTimeout: [timeoutStr: ''],
          startedTimeout: [timeoutStr: ''],
          timeout: [timeoutStr: '1d'],
          whenFailure: [$class: 'ContinueAsFailure'],
          whenScheduledTimeout: [$class: 'ContinueAsIs'],
          whenStartedTimeout: [$class: 'ContinueAsIs'],
          whenTimeout: [$class: 'ContinueAsFailure'],
          whenUnstable: [$class: 'ContinueAsUnstable']
        ],
        parameterFactories: 
        [
          [
            $class: 'EvaluatedString',
            expression: "${cloneURL}",
            name: 'git_url'
          ],
          [
            $class: 'EvaluatedString',
            expression: "${git_commit}",
            name: 'git_commit'
          ],
          [
            $class: 'EvaluatedString',
            expression: "${git_branch}",
            name: 'git_branch'
          ]
        ],
        remotePathMissing: [$class: 'ContinueAsIs'],
        remotePathUrl: jenkinsPipelineTestingURL
        env.build_result = mybuild.result
    }
    stage('Update Bitbucket with success/failure') {
        
    if (build_result != 'FAILURE') {
        run_curl = sh(returnStdout: false, script: "curl -k -u jenkins_puppet_test:jenkins -H \"Content-Type: application/json\" -X POST https://${bitbucket_hostname}/rest/build-status/1.0/commits/${git_commit} -d @failure.json")
        print 'THIS WORKED OK!!!!!!!!!!!!!!!!!!!'
    } else {
        print 'THIS FAILED :((((((((('
    }
    }
}

