def jenkins_slave_label='master'
def branch_pattern_to_base_checkout_on='*/master'
env.DOCKER_TLS_VERIFY="1"
env.DOCKER_HOST="tcp://192.168.99.103:2376"
env.DOCKER_API_VERSION="1.35"
def git_credentials='apicredentials'
env.dockerUser='cdougan'

node(jenkins_slave_label) {
    def pipeline_workspace=pwd()
    env.DOCKER_CERT_PATH="/etc/minikube/certs"
    stage('Build Gradle') {
      env.docker_tag='gradle_builder'
      def git_repo_url='git@bitbucket.org:blainethemono/dockerfile-gradle_builder.git'
      checkout([$class: 'GitSCM', branches: [[name: branch_pattern_to_base_checkout_on ]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: docker_tag ]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: git_credentials, url: git_repo_url ]]])
      withCredentials([usernamePassword(credentialsId: 'dockerlogin', passwordVariable: 'dockerpass', usernameVariable: 'dockeruser')]) {
      sh """
        cd ${docker_tag}
        docker build . --tag ${dockerUser}/${docker_tag}
        docker login --username ${dockeruser} --password ${dockerpass}
        docker push ${dockerUser}/${docker_tag}  
      """
      }
    }
   stage('Build Jenkins') {
      env.docker_tag='jenkins_with_docker'
      def git_repo_url='git@bitbucket.org:blainethemono/dockerfile-jenkins_with_docker.git'
      checkout([$class: 'GitSCM', branches: [[name: branch_pattern_to_base_checkout_on ]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: docker_tag ]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: git_credentials, url: git_repo_url ]]])
      withCredentials([usernamePassword(credentialsId: 'dockerlogin', passwordVariable: 'dockerpass', usernameVariable: 'dockeruser')]) {
      sh """
        cd ${docker_tag}
        docker build . --tag ${dockerUser}/${docker_tag}
        docker login --username ${dockeruser} --password ${dockerpass}
        docker push ${dockerUser}/${docker_tag}  
      """
     }
   }
}
