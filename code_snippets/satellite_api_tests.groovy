node('hostname.net'){
    checkout([$class: 'GitSCM', branches: [[name: '*/master']],
              doGenerateSubmoduleConfigurations: false,
              extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'library']],
              submoduleCfg: [],
              userRemoteConfigs: [[credentialsId: 'BB_key',
                                   url: 'ssh://git@bitbuckethost:7999/jen/jenkins-functions.git']]])
    env.rootdir=pwd()
    println 'loading function'
    def satelliteLibrary = load 'library/satellite.groovy'
    withCredentials([usernamePassword(credentialsId: 'RHS_REST_API', passwordVariable: 'restpwd', usernameVariable: 'restuser')]) {
        def paramid=satelliteLibrary.smartClassParameterID("admin_http_port","pit_websphere_as",restuser,restpwd)
        println "paramid of smartclass parameter 'admin_http_port' for 'pit_websphere_as' is ${paramid}"
        def classname=satelliteLibrary.puppetClassName("502",restuser,restpwd)
        println "the class name for class id 502 is ${classname}"
        def classid=satelliteLibrary.puppetClassId("pit_websphere_as",restuser,restpwd)
        println "the class id for 'pit_websphere_as' is ${classid}"
        def modulelist=satelliteLibrary.puppetClassesAssignedToHost('host_one',restuser,restpwd,false)
        println "the modules assigned to host host_one are ${modulelist}"
        def moduleidlist=satelliteLibrary.puppetClassIDsAssignedToHost('host_one',restuser,restpwd,false)
        println "the modules ids assigned to host host_one are ${moduleidlist}"
        def matcherid=satelliteLibrary.smartParameterMatcherID('host_one','1449',restuser,restpwd)
        if (matcherid == null) {
            print "No matcher found for parameter id 1449 relating to host host_one"
        } else {
            println "Matcher id for smart parameter 1449 relating to host host_one is ${matcherid}"
        }
    }
}

