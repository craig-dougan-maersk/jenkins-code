def triggerTask(String nexus_user, String nexus_pass, String nexus_host, String update_metadata_task_id) {
    sh """#!/bin/bash
        curl -X GET -u ${nexus_user}:${nexus_pass} http://${nexus_host}/nexus/service/local/schedule_run/${update_metadata_task_id}  
      """

}

def uploadFile(String nexus_repo,
                      String nexus_group,
                      String nexus_artifact,
                      String nexus_version,
                      String nexus_package,
                      String nexus_classifier,
                      String file_to_upload,
                      String nexus_user,
                      String nexus_pass,
                      String nexus_host="abc-watm-artefactrepo.somedomain.com:8081") {
    sh """#!/bin/bash
                        curl -v \
                        -F "r=${nexus_repo}" \
                        -F "g=${nexus_group}" \
                        -F "a=${nexus_artifact}" \
                        -F "v=${nexus_version}" \
                        -F "p=${nexus_package}" \
                        -F "e=${nexus_package}" \
                        -F "c=${nexus_classifier}" \
                        -F "file=@${file_to_upload}" \
                        -u ${nexus_user}:${nexus_pass} http://${nexus_host}/nexus/service/local/artifact/maven/content
                        """
}
return this
