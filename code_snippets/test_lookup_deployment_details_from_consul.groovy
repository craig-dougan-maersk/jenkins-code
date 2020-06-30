node('hostname.net'){
checkout([$class: 'GitSCM', branches: [[name: '*/master']], 
  doGenerateSubmoduleConfigurations: false, 
  extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'library']], 
  submoduleCfg: [], 
  userRemoteConfigs: [[credentialsId: 'rhel7_docker_puppet_tester', 
    url: 'ssh://git@bitbucket-host:7999/jen/jenkins-functions.git']]])
env.rootdir=pwd()
println 'loading function'
def exone = load 'library/example_one.groovy'
exone.example_one()
//def extwo=load "library/example_two.groovy"
//exone.example_one()   
println 'end of pipeline'
}

