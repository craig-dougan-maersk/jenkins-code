
def url = new URL('http://your_rest_endpoint')
def http = url.openConnection()
http.setDoOutput(true)
http.setRequestMethod('PUT')
http.setRequestProperty('User-agent', 'groovy script')

def out = new OutputStreamWriter(http.outputStream)
out.write('data')
out.close()
