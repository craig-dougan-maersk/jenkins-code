def consul
def lookupkey
def lookupSubFolder
def lookupkeyArray
def lookupFolderArray

node('abcdef-vmua671.abc.net') {
  stage('Checkout Jenkins Functions') {
    checkout([$class: 'GitSCM', branches: [[name: '*/master']], 
    doGenerateSubmoduleConfigurations: false, 
    extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'library']], 
    submoduleCfg: [], 
    userRemoteConfigs: [[credentialsId: 'BBkey', 
      url: 'ssh://git@abc-wscm-sourcerepo.somedomain.com:7999/jen/jenkins-functions.git']]])
  }
  stage('load functions') {
    consul = load 'library/consul.groovy'
  }
  stage('Look up keys in Consul') {
    lookupkeyArray=consul.lookupKey('A1111/Patching/Patching_Windows',true)
    lookupkeyList=consul.lookupKey('A1111/Patching/Patching_Windows')
    lookupsubFolderArray=consul.lookupKeySubFolders('A1111/WebSphere/application_nodes',true)
    lookupsubFolderList=consul.lookupKeySubFolders('A1111/WebSphere/application_nodes')
  }
  stage('Print values of looked up keys in list Format') {
    println "The value of key 'A1111/Patching/Patching_Windows' (in list format) is "
    println "${lookupkeyList}"
    println "The folders under key 'A1111/WebSphere/application_nodes' are (in list format) "
    println "${lookupsubFolderList}"
  }
  stage('Print values of looked up keys in Array Format') {
    println "The value of key 'A1111/Patching/Patching_Windows' (in array format) is "
    println "${lookupkeyArray}"
    println "The folders under key 'A1111/WebSphere/application_nodes' are (in array format) "
    println "${lookupsubFolderArray}"
  }
  stage('Loop Through Arrays and Output elements') {
    println "looping through lookupkeyArray array:"
    for (i=0; i<lookupkeyArray.size();i++) {
        println lookupkeyArray[i]
    }
    println "looping through lookupsubFolderArray array:"
    for (i=0; i<lookupsubFolderArray.size();i++) {
        println lookupsubFolderArray[i]
    }
  } 
}
