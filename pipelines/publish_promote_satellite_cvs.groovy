env.organization="SomeOrg"
node('abcdef-vmua671.abc.net') {
checkout([$class: 'GitSCM', branches: [[name: '*/master']], 
  doGenerateSubmoduleConfigurations: false, 
  extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'library']], 
  submoduleCfg: [], 
  userRemoteConfigs: [[credentialsId: 'RHS_BB_CB_key', 
    url: 'ssh://git@abc-wscm-sourcerepo.somedomain.com:7999/jen/jenkins-functions.git']]])
def hammer = load 'library/hammer.groovy'
def consul = load 'library/consul.groovy'
patching_content_view_list=consul.lookupConsul('A1111/Patching/Patching_Content_Views')
lifecycle_list=consul.lookupConsul('A1111/Patching/Patching_Lifecycles')
lifecycles=lifecycle_list.split('\n')

properties([parameters([ booleanParam(defaultValue: false, description: 'Refresh Drop Down Selector and Exit', name: 'refresh_choices'),choice(choices: "${patching_content_view_list}", description: 'Content View to Promote', name: 'content_view')]), pipelineTriggers([])])

if (refresh_choices == "true") { return }

    stage("Publish_Content_View") {
        hammer.publishContentViewHammer(content_view,organization,"abcdef-vmui010.abc.net")
	}
	for (int i=0; i<lifecycles.size(); i++) {
      stage(lifecycles[i]) {
        build job: '/Red_Hat_Satellite/Content Views/Promote', parameters: [[$class: 'StringParameterValue', name: 'Content_View', value: content_view], [$class: 'StringParameterValue', name: 'Lifecycle_Environment', value: lifecycles[i]]]
      }
    }
      
  }
