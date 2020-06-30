def setBuildStatus(string bitbucket_user,
                   string bitbucket_pass,
                   string build_state,
                   string commit_number,
                   string build_name,
                   string build_url,
                   string build_description,
                   string bitbucket_server) {
    sh """#!/bin/bash
		    echo '{' > bb.\$\$.json
		    echo '"state": "${build_state}",' >> bb.\$\$.json
            echo '"key": "${build_name}",' >> bb.\$\$.json
            echo '"name": "${build_name}",' >> bb.\$\$.json
            echo '"url": "${build_url}",' >> bb.\$\$.json
            echo '"description": "${build_description}"' >> bb.\$\$.json
	        echo '}' >> bb.\$\$.json
		    curl -k -u ${bitbucket_user}:${bitbucket_pass} -H "Content-Type: application/json" -X POST  https://${bitbucket_server}/rest/build-status/1.0/commits/${commit_number} -d@bb.\$\$.json
		    rm  -f bb.\$\$.json
		    """
}

def updateBuildStatus(string bitbucket_user,
                      string bitbucket_pass,
                      string build_state,
                      string commit_number,
                      string bitbucket_server) {
    sh """#!/bin/bash
		    echo '{' > bb.\$\$.json
		    echo '"state": "${build_state}",' >> bb.\$\$.json
	            echo '}' >> bb.\$\$.json
		    curl -k -u ${bitbucket_user}:${bitbucket_pass} -H "Content-Type: application/json" -X POST  https://${bitbucket_server}/rest/build-status/1.0/commits/${commit_number} -d@bb.\$\$.json
		    rm  -f bb.\$\$.json
		    """
}


// setBitBucketBuildStatus( "jenkins_puppet_test",
//                          "packerjenkins",
//                          "INPROGRESS",
//                          "abcdef1234",
//                          "my_test_build",
//                          "www.myjenkins.com/build/jobs/myjob/12",
//                          "some build I did",
//                          "abc-wscm-sourcerepo.somedomain.net")

// updateBitBucketBuildStatus( "jenkins_puppet_test",
//                          "packerjenkins",
//                          "SUCCESSFULL",
//                          "abcdef1234",
//                          "abc-wscm-sourcerepo.somedomain.net")
return this
