

def checkout(string git_branch, string checkout_dir, string git_url, string git_credentials) {
    // GIT Checkout based on Variables above
    def checkoutVars = checkout([$class                           : 'GitSCM', branches: [[name: git_branch]],
                                 doGenerateSubmoduleConfigurations: false,
                                 extensions                       : [[$class: 'RelativeTargetDirectory', relativeTargetDir: checkout_dir]],
                                 submoduleCfg                     : [],
                                 userRemoteConfigs                : [[credentialsId: git_credentials,
                                                                      url          : git_url]]])
}

return this

// checkoutfromBitBucket( "my branch",
//                        "my_module_dir",
//                        "https://bitbucket.org/scm/myproject/myrepo.git",
//                        "1234567abcderf")

