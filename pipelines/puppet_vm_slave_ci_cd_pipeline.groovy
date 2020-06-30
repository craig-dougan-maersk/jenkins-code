def sshCredentialID='rhel7_docker_puppet_tester'
def jenkinsSlave='abcdef-vmua671.abc.net'
def nexus_repository="A0470-Puppet-Modules"
def nexus_url='http://abc-watm-artefactrepo.somedomain.com:8081'
    
node("${jenkinsSlave}") {
    def pipeline_workspace=pwd()
        sshagent(["${sshCredentialID}"]) {
	        stage('Set in-progress Build Status in BitBucket') {
		    env.puppetmodule=git_url.tokenize('/')[-1].tokenize('.')[-2]
		    sh """#!/bin/bash
		    echo '{' > start.json
		    echo '"state": "INPROGRESS",' >> start.json
		    echo '"key": "puppet_validator",' >> start.json
	            echo '"name": "puppet_validator",' >> start.json
	            echo '"url": "${JENKINS_URL}",' >> start.json
	            echo '"description": "Validator job for ${git_commit} on ${git_branch} for ${puppetmodule}"' >> start.json
	            echo '}' >> start.json
		    curl -k -u jenkins_puppet_test:jenkins -H "Content-Type: application/json" -X POST  https://abc-wscm-sourcerepo.somedomain.com/rest/build-status/1.0/commits/${git_commit} -d@start.json
		    """
		}
                stage('Clone git repo') {
		    env.bbproject=git_url.tokenize('/')[-2]
		    env.modulename=puppetmodule.tokenize('-')[-1]
                    checkout([$class: 'GitSCM', branches: [[name: git_branch]], 
                    doGenerateSubmoduleConfigurations: false, 
                    extensions: [
                        [$class: 'RelativeTargetDirectory', 
                        relativeTargetDir: "modules/${puppetmodule}"]
                    ], 
                    submoduleCfg: [], 
                    userRemoteConfigs: [
                        [credentialsId: sshCredentialID, 
                        url: git_url]
                        ]
                    ])
		    //env.clonedBranch = sh(returnStdout: true, script: "cd modules/${puppetmodule};git status| head -1|cut -d' ' -f3-").trim()
                }
          		stage('Check Git history') {
                    sh """#!/bin/bash
                    source /opt/rh/rh-ruby24/enable
                    cd modules/${puppetmodule}
                    find . -name '*.pp' | while read line
                    do
                        echo "validating syntax of \$line"
                        puppet parser validate \$line
                    done
                    """
                }
                stage('Validate Syntax') {
                    sh """#!/bin/bash
                    source /opt/rh/rh-ruby24/enable
                    cd modules/${puppetmodule}
                    find . -name '*.pp' | while read line
                    do
                        echo "validating syntax of \$line"
                        puppet parser validate \$line
                    done
                    """
                }
                stage('Rake Spec Tests') {
                    sh """#!/bin/bash
                    source /opt/rh/rh-ruby24/enable
                    cd modules/${puppetmodule}
                    rake spec_clean
                    rake spec
                    """
                }
                stage('Smoke testing [noop]') {
                    sh """#!/bin/bash
                    cd modules/${puppetmodule}
                    source /opt/rh/rh-ruby24/enable
                    rake spec_prep
                    sudo puppet apply --noop -e 'class {"${puppetmodule}":}' --modulepath=./spec/fixtures/modules
                    rake spec_clean
                    """
                }      
                stage('Build Module Package') {
                    sh """#!/bin/bash
                        cat modules/${puppetmodule}/metadata.json | python -c "import sys,json; print json.load(sys.stdin)['name']" > "${pipeline_workspace}/full_module_name.txt"
                        cat ${pipeline_workspace}/full_module_name.txt | awk -F- '{print \$1}'  > "${pipeline_workspace}/module_author.txt"
                        cat modules/${puppetmodule}/metadata.json | python -c "import sys,json; print json.load(sys.stdin)['version']"  > "${pipeline_workspace}/module_version.txt"
                    	echo "Checking metadata.json -> "
						cat modules/${puppetmodule}/metadata.json 
					"""
                    env.full_module_name=readFile("${pipeline_workspace}/full_module_name.txt").trim()
                    env.module_author=readFile("${pipeline_workspace}/module_author.txt").trim()
                    env.module_version=readFile("${pipeline_workspace}/module_version.txt").trim()
		    env.safe_git_branch=git_branch.tokenize('/')[-1]
		    print "Module version is ************************* ${module_version}"
		    print "Full_module_name is ******************* ${full_module_name}"
		    print "Safe_git_branch is ********************* ${safe_git_branch}"
                    sh """#!/bin/bash
                    cd "modules/${puppetmodule}"
					echo "Checking Module version : ${module_version}"
					if [[ \$(git describe --tags 2>/dev/null) != "${module_version}" ]]; then
                        git tag -a ${module_version}-${safe_git_branch} -m "Version ${module_version}";
                    fi
                    git_num_of_commits=\$(git log | grep ^commit | wc -l)
                    git_hash=\$(git describe --tags --long| cut -d- -f4)
                    if [[ $safe_git_branch == "master" ]]; then
                        release_version=1
                    else
                        release_version="git.${safe_git_branch}.\${git_num_of_commits}.\${git_hash}"
                    fi
		    echo "git describe tags ****************** \$(git describe --tags --long)"
		    echo "num of commits is ******************* \${git_num_of_commits}"
		    echo "release version is ********************* \${release_version}"
                    echo "\${release_version}" > ${pipeline_workspace}/release_version.txt
                    cd "${pipeline_workspace}"
                    puppet module build modules/${puppetmodule}
                    """
                    env.release_version = readFile("${pipeline_workspace}/release_version.txt").trim()
                    env.compiled_file=pipeline_workspace + "/modules/" + puppetmodule + "/pkg/" + full_module_name + "-" + module_version + '.tar.gz'
                }
                stage ('Upload Module to Nexus') {
                    withCredentials([usernamePassword(credentialsId: 'puppet_deploy_to_nexus', passwordVariable: 'deploy_to_nexus_password', usernameVariable: 'deploy_to_nexus_user')]) {
                        sh """#!/bin/bash
                        curl -v \
                        -F "r=${nexus_repository}" \
                        -F "g=${module_author}" \
                        -F "a=${puppetmodule}" \
                        -F "v=${module_version}" \
                        -F "p=tar.gz" \
                        -F "e=tar.gz" \
                        -F "c=${release_version}" \
                        -F "file=@${compiled_file}" \
                        -u ${deploy_to_nexus_user}:${deploy_to_nexus_password} ${nexus_url}/nexus/service/local/artifact/maven/content
                        """
                    }
                }
                stage('Build RPM') {
                    sh """#!/bin/bash
		    set -x
                    cd ${pipeline_workspace}
                    echo "pipeline_workspace : ${pipeline_workspace}"
                    echo "%_topdir       ${pipeline_workspace}/rpm" > ${pipeline_workspace}/.rpmmacros
                    echo "%_tmppath      ${pipeline_workspace}/rpm/tmp" }} > ${pipeline_workspace}/.rpmmacros
                    mkdir -p  ${pipeline_workspace}/rpm ${pipeline_workspace}/rpm/BUILD ${pipeline_workspace}/rpm/RPMS ${pipeline_workspace}/rpm/RPMS/noarch ${pipeline_workspace}/rpm/SOURCES ${pipeline_workspace}/rpm/SPECS ${pipeline_workspace}/rpm/SRPM ${pipeline_workspace}/rpm/tmp
                    INSTALL_DIR="/opt/puppet-modules"
                    MODULE_FILE=\$(basename "${compiled_file}")
                    echo "Module File : \${MODULE_FILE}"
                    MODULE_DIR=\$(echo "\${MODULE_FILE}" | sed 's/.tar.*//g')
                    echo "Module_dir : \${MODULE_DIR}"
                    cp ${compiled_file} ${pipeline_workspace}/rpm/SOURCES/
                    DESCRIPTION=\$(grep summary "modules/\${puppetmodule}/metadata.json" | awk -F: '{print \$2}' | sed 's/,\$//g')
                    RPM_PREFIX="puppet_module_${full_module_name}"
                    echo "Summary: Locally installs \${RPM_PREFIX} to be used with puppet apply" > "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "Name: \${RPM_PREFIX}" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "Version: ${module_version}" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "License: Restricted" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "Release: ${release_version}" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "BuildRoot: %{_builddir}/%{name}-root" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "Packager: ${module_author}" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "Prefix: \${INSTALL_DIR}" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "BuildArchitectures: noarch" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "Source1: \${MODULE_FILE}" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "%description" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "\${DESCRIPTION}" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "%prep" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "%build" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "pwd" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "cd %{_sourcedir}" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "%install" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "pwd" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "echo \"Removing RPM Build Root : \\\${RPM_BUILD_ROOT}\"" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "rm -rf \\\${RPM_BUILD_ROOT}" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "mkdir -p \\\${RPM_BUILD_ROOT}\${INSTALL_DIR}" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "tar zxvf %{SOURCE1} --directory=\\\${RPM_BUILD_ROOT}\${INSTALL_DIR}" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "mv \\\${RPM_BUILD_ROOT}\${INSTALL_DIR}/\${MODULE_DIR} \\\${RPM_BUILD_ROOT}\${INSTALL_DIR}/${puppetmodule}" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "%clean" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "rm -rf \\\${RPM_BUILD_ROOT}" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "%files" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "%defattr(-,puppet,puppet)"  >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    echo "\${INSTALL_DIR}/${puppetmodule}/" >> "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    rpmbuild --define "_topdir ${pipeline_workspace}/rpm" --define "_tmppath ${pipeline_workspace}/rpm/tmp" -ba "${pipeline_workspace}/rpm/SPECS/puppet-module-\${RPM_PREFIX}-${module_version}-${release_version}.spec"
                    built_rpm=${pipeline_workspace}/rpm/RPMS/noarch/\${RPM_PREFIX}-${module_version}-${release_version}.noarch.rpm
                    echo \$built_rpm > ${pipeline_workspace}/built_rpm.txt
                    """ 
                    env.built_rpm = readFile("${pipeline_workspace}/built_rpm.txt").trim()
                }
                stage('Upload RPM to Nexus') {
                    withCredentials([usernamePassword(credentialsId: 'puppet_deploy_to_nexus', passwordVariable: 'deploy_to_nexus_password', usernameVariable: 'deploy_to_nexus_user')]) {
                        sh """#!/bin/bash
                        curl -v \
                        -F "r=${nexus_repository}" \
                        -F "g=${module_author}" \
                        -F "a=${puppetmodule}" \
                        -F "v=${module_version}" \
                        -F "p=rpm" \
                        -F "e=rpm" \
                        -F "c=${release_version}" \
                        -F "file=@${built_rpm}" \
                        -u ${deploy_to_nexus_user}:${deploy_to_nexus_password} ${nexus_url}/nexus/service/local/artifact/maven/content
                        """
                    }
                }
              stage('Update RPM Metadata in Nexus') {
                withCredentials([usernamePassword(credentialsId: 'puppet_deploy_to_nexus', passwordVariable: 'deploy_to_nexus_password', usernameVariable: 'deploy_to_nexus_user')]) {
                  sh """#!/bin/bash
                  curl -X GET -u ${deploy_to_nexus_user}:${deploy_to_nexus_password} http://abc-watm-artefactrepo.somedomain.com:8081/nexus/service/local/schedule_run/814   
                  """
                }
              }
              stage('Set in-progress Build Status in BitBucket') {
	        env.puppetmodule=git_url.tokenize('/')[-1].tokenize('.')[-2]
		sh """#!/bin/bash
		echo '{' > stop.json
		echo '"state": "SUCCESSFULL",' >> stop.json
		echo '"key": "puppet_validator",' >> stop.json
		echo '"name": "puppet_validator",' >> stop.json
		echo '"url": "${JENKINS_URL}",' >> stop.json
		echo '"description": "Validator job for ${git_commit} on ${git_branch} for ${puppetmodule}"' >> stop.json
		echo '}' >> stop.json
		curl -k -u jenkins_puppet_test:jenkins -H "Content-Type: application/json" -X POST  https://abc-wscm-sourcerepo.somedomain.com/rest/build-status/1.0/commits/${git_commit} -d@stop.json
		"""
		}
        }
}

