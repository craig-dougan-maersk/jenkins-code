def CheckSyntax(dirToCheck = '.') {
    results = sh(returnStatus: true, script: "source ~/.bash_profile && echo 'starting puppet parser validation' && ind ${dirToCheck} -name '*.pp' | xargs puppet parser validate && returnCode=\$? && echo 'finished puppet parser validate'")
    return results
}

def SpecTest(dirToCheck = '.') {
    results = sh(returnStatus: true, script: "source ~/.bash_profile && cd ${dirToCheck} && rake spec_clean && rm -rf spec/fixtures/modules/* && rake spec")
    return results
}


def LintTest(dirToCheck = '.') {
    results = sh(returnStatus: true, script: "source ~/.bash_profile && cd ${dirToCheck} && rake lint")
    return results
}

def SmokeTest(moduleName, modulePath = '.', specprep = true) {
    if specprep {
        results = sh(returnStatus: true, script: "source ~/.bash_profile && cd ${modulePath} && rake spec_prep && puppet apply --noop --modulepath=./spec/fixtures/modules -e \"class{'${moduleName}':}\" && rake spec_clean")
    } else {
        results = sh(returnStatus: true, script: "source ~/.bash_profile && puppet apply --noop --modulepath=.${modulePath} -e \"class{'${moduleName}':}\"")
    }

}
return this