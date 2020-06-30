def publishContentView(String cv,String org,String sathost) {
      sshagent(['b573e2d8-7244-4ef1-9471-79b37c8dc134']) {
        sh "ssh -o StrictHostKeyChecking=no hammeruser@${sathost} \"/usr/bin/hammer content-view publish --name ${cv} --organization=${org}\""
      }
}
return this
