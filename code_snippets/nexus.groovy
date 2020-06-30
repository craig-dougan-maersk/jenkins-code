def trigger_nexus_task(string nexus_user, string nexus_pass, string nexus_host, string update_metadata_task_id) {
      sh """#!/bin/bash
        curl -X GET -u ${nexus_user}:${nexus_pass} http://${nexus_host}:8081/nexus/service/local/schedule_run/${update_metadata_task_id}  
      """

    }
}