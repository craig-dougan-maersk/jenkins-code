def lookupKey(String kv, Boolean output_array=false, String consul_url="https://10.12.114.174") {
  def myurl = new URL("${consul_url}/v1/kv/${kv}").getText()
  def encryptedString = myurl.tokenize('"')[11]
  def decodedString = new String(encryptedString.decodeBase64())  
  if (output_array == true) {
       def decodedArray=[]
      decodedArray=decodedString.split('\n')
      return decodedArray
  } else {
    return decodedString
  }
}

def lookupKeySubFolders(String kv, Boolean output_array=false, String consul_url="https://10.12.114.174") {
    def keyDepth=0
  def cleanedArray=[]
    for (int i=0; i<kv.size();i++) {
        if ((kv[i]=='/') && (i != (kv.size()-1))) {
            keyDepth++
        }
        lookupKeyDepth=keyDepth
    }
    def myurl = new URL("${consul_url}/v1/kv/${kv}?keys").getText()
    def keyString = myurl.split('","')
    for (int i=0; i<keyString.size(); i++) {

        if (keyString[i][0] == "[") {
            currentRecord=keyString[i].substring(2)
        } else if (keyString[i][-1] == "]") {
            length=keyString[i].size()
            currentRecord=keyString[i].substring(0,length-2)
        } else {
            currentRecord=keyString[i]
        }
        if (currentRecord[-1] == '/') {
            if (currentRecord.split('/').size() > (lookupKeyDepth+1)) {
                cleanedArray << new String(currentRecord.split('/')[lookupKeyDepth+1])
            }
        }
    }
    if (output_array == true) {
      return cleanedArray.unique()
    } else {
      return cleanedArray.unique().join('\n')
    }
}

return this
