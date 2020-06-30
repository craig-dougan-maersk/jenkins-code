URL: http://localhost:8080/script
import org.jenkinsci.plugins.scriptsecurity.scripts.ScriptApproval

println("INFO: Whitelisting requirements for Jenkinsfile API Calls")

// Create a list of the required signatures
def requiredSigs = [
    'method groovy.json.JsonSlurperClassic parseText java.lang.String',
    'method java.io.Flushable flush',
    'method java.io.Writer write java.lang.String',
    'method java.lang.AutoCloseable close',
    'method java.net.HttpURLConnection setRequestMethod java.lang.String',
    'method java.net.URL openConnection',
    'method java.net.URLConnection connect',
    'method java.net.URLConnection getContent',
    'method java.net.URLConnection getOutputStream',
    'method java.net.URLConnection setDoOutput boolean',
    'method java.net.URLConnection setRequestProperty java.lang.String java.lang.String',
    'new groovy.json.JsonSlurperClassic',
    'new java.io.OutputStreamWriter java.io.OutputStream',
    'staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods findAll java.lang.String java.lang.String',
    'staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods getText java.io.InputStream',
    'staticMethod org.codehaus.groovy.runtime.DefaultGroovyMethods getText java.net.URL java.util.Map',

    // Signatures already approved which may have introduced a security vulnerability (recommend clearing):
    'method java.net.URL openConnection',
]

// Get a handle on our approval object
approver = ScriptApproval.get()

// Aprove each of them
requiredSigs.each {
    approver.approveSignature(it)
}

println("INFO: Jenkinsfile API calls signatures approved")
