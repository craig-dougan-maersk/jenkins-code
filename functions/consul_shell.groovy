def lookupKey(kv,url="https://10.12.114.174") {
    curl_url = "${url}/v1/kv/${kv}"
    run_curl = sh(returnStdout: true, script: "curl -ks ${curl_url}|python -c 'import json,sys;obj=json.load(sys.stdin);print obj[0][\"Value\"];'|base64 --decode").trim()
    return run_curl
}
return this
