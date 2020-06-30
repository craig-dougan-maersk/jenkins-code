def groupsInRepository(String repository_id, Boolean output_array=true, String nexus_host="abc-watm-artefactrepo.somedomain.com")
{
  def nexus_url="http://${nexus_host}:8081"    
  def url = "${nexus_url}/nexus/content/repositories/${repository_id}/"
  data = new URL(url).getText()
  def myArray = []
  data.findAll("${repository_id}/.*/</a>").each {
    myArray = myArray + [myval = it.replaceAll('^.*">', '').replaceAll("/</a>",'')]
  }
  if (output_array == true) {
    return myArray
  } else {
    return output_array.join('\n')
  }
}

// Example xml dataset for artifactIdsInGroup
//
//<data>
//  <artifact>
//    <groupId>cdougan</groupId>
//    <artifactId>pit_ibm_install_manager</artifactId>
//    <version>0.1.2</version>
//    <latestRelease>0.1.2</latestRelease>


def artifactIdsInGroup(String group_id, String repository_id, Boolean output_array=true, String nexus_host="abc-watm-artefactrepo.somedomain.com")
{
  def nexus_url="http://${nexus_host}:8081"
  xmlUrl = "${nexus_url}/nexus/service/local/lucene/search?g=${group_id}&r=${repository_id}"
  def xml = xmlUrl.toURL().text
  def xmlResults = new XmlParser().parseText(xml)

  def mylist = xmlResults.data.artifact.collect {
    it.artifactId.text()
  }
  if (output_array == true) {
    return mylist.unique()
  } else {
    return mylist.unique().join('\n')
  }
}
return this
