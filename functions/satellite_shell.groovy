//////abcdef-vmub011.somedomain.com

//get smart class parameter id (every sc parameter of every module has a unique id)
//-------------------------------------------------------------------------------------------
//https://1.2.112.191/api/smart_class_parameters?search=parameter=admin_http_port,puppetclass_name=pit_websphere_as
//
// outputs - 
//}


//add puppet class to hosts (get class ids from above)
//-----------------------------------------------------
//curl -k -u username:password -H "Accept: version=2,application/json" -H
//"Content-Type: application/json" -d '{"puppetclass_id":"280"}' -X POST
//https://foreman_server/api/hosts/testhost11.ide.int.com/puppetclass_ids

//Get Puppet Class Name from ID
//--------------------------------
//curl -k -u restapiuser:abc123 -H "Accept: version=2,application/json" -H "Content-Type: application/json" -X GET https://1.2.112.191/api/puppetclasses/319
//


//Get Puppet Class ID
//----------------------
//curl -k -u restapiuser:abc123 -H "Accept: version=2,application/json" -H "Content-Type: application/json" -X GET https://1.2.112.191/api/puppetclasses?search=name=test_foreman
//
//output:   "results": {"test_foreman":[{"id":560,"name":"test_foreman","created_at":"2017-08-24T14:46:30.022Z","updated_at":"2017-08-24T14:46:30.022Z"}]}


//List Puppet classes assigned to host
//------------------------------------
//https://1.2.112.191/api/hosts/abcdef-vmua600.somedomain.com/puppetclasses/
//

//List Puppet Class IDs assigned to host
//-----------------------------------------
//https://1.2.112.191/api/hosts/abcdef-vmub011.somedomain.com/puppetclass_ids
//
//outputs - {"results":[319,560]}


//Create new Smart Parameter override
//--------------------------------------
//curl -k -u restapiuser:abc123 -H "Accept: version=2,application/json" -H "Content-Type: application/json" -X POST -d '{"override_value": {"match": "fqdn=abcdef-vmub011.somedomain.com","value":"TRUE"}}' https://1.2.112.191/api/smart_class_parameters/1449/override_values

//Update Existing Smart Parameter override
//------------------------------------------
//curl -k -u restapiuser:abc123 -H "Accept: version=2,application/json" -H "Content-Type: application/json" -X PUT -d '{"value":"false"}' https://1.2.112.191/api/smart_class_parameters/1449/override_values/2321

//Get smart parameter Matcher ID
//-------------------------------
//curl -k -u restapiuser:abc123 -H "Accept: version=2,application/json" -H "Content-Type: application/json" -X GET https://1.2.112.191/api/v2/smart_class_parameters/1449
