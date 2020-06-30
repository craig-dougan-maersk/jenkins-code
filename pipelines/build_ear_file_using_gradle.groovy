env.artefact_name="gradle-java-ear"
def branch_pattern_to_base_checkout_on='*/master'
def minikube_cert_url='git@bitbucket.org:blainethemono/.minikube.git'
def git_repo_url='git@bitbucket.org:blainethemono/gradle-java-ear.git'
env.DOCKER_TLS_VERIFY="1"
env.DOCKER_HOST="tcp://192.168.99.103:2376"
env.DOCKER_API_VERSION="1.35"

node('master') {
    def pipeline_workspace=pwd()
    env.DOCKER_CERT_PATH="${pipeline_workspace}/.minikube/certs"
    checkout([$class: 'GitSCM', branches: [[name: branch_pattern_to_base_checkout_on ]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: "${pipeline_workspace}/.minikube/" ]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'my_ssh_key', url: minikube_cert_url ]]])
 
    checkout([$class: 'GitSCM', branches: [[name: branch_pattern_to_base_checkout_on ]], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: artefact_name ]], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'my_ssh_key', url: git_repo_url ]]])

    sh """
    cd ${pipeline_workspace}/${artefact_name}
    ls -l
    docker run --rm -e GITURL=https://bitbucket.org/blainethemono/gradle-java-ear.git gradle_builder
    
    """
}
