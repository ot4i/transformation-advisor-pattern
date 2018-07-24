package com.ibm.TransformationAdvisorPattern.code;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.ibm.broker.config.appdev.FlowRendererMSGFLOW;
import com.ibm.broker.config.appdev.MessageFlow;
import com.ibm.broker.config.appdev.Node;
import com.ibm.broker.config.appdev.NodeProperty;
import com.ibm.broker.config.appdev.patterns.GeneratePatternInstanceTransform;
import com.ibm.broker.config.appdev.patterns.PatternInstanceManager;

public class Analyse implements GeneratePatternInstanceTransform {

	final static Logger logger = Logger.getLogger(Analyse.class);	
	static public final String AdminConstants_BAR_EXTENSION = ".bar";
	static public final String AdminConstants_XSDZIP_EXTENSION = ".xsdzip";
	static public final String AdminConstants_RDB_SCHEMA_EXTENSION = ".tblxmi";
	static public final String AdminConstants_XSD_FILE_EXTENSION = ".xsd";
	static public final String AdminConstants_WSDL_FILE_EXTENSION = ".wsdl";
	static public final String AdminConstants_DICTIONARY_EXTENSION = ".dictionary";
    static public final String AdminConstants_XSL_EXTENSION = ".xsl";
    static public final String AdminConstants_XSLT_EXTENSION = ".xslt";
    static public final String AdminConstants_XML_EXTENSION = ".xml";
    static public final String AdminConstants_JAR_EXT = ".jar";
    static public final String AdminConstants_INADAPTER_FILE_EXTENSION = ".inadapter";
    static public final String AdminConstants_OUTADAPTER_FILE_EXTENSION = ".outadapter";
    static public final String AdminConstants_INSCA_FILE_EXTENSION = ".insca";
    static public final String AdminConstants_OUTSCA_FILE_EXTENSION = ".outsca";
    static public final String AdminConstants_PHP_FILE_EXTENSION = ".php";
    static public final String AdminConstants_IDL_EXTENSION = ".idl";
    static public final String AdminConstants_MAP_EXTENSION = ".map";
    static public final String AdminConstants_ESQL_FILE_EXTENSION = ".esql";
    static public final String AdminConstants_RULE_EXTENSION = ".rule";
    static public final String AdminConstants_RULES_EXTENSION = ".rules";
    static public final String AdminConstants_DESCRIPTOR_EXTENSION = ".descriptor";
    static public final String AdminConstants_MQSC_FILE_EXTENSION = ".mqsc";
    static public final String AdminConstants_TESTCASE_EXTENSION = ".testcase";
    static public final String AdminConstants_JSON_EXTENSION = ".json";
    static public final String AdminConstants_APPLICATION_CONTAINER_EXT = ".appzip";
    static public final String AdminConstants_LIBRARY_CONTAINER_EXT = ".libzip";
    static public final String AdminConstants_DOTNET_CONTAINER_EXT = ".appdomainzip";
    static public final String AdminConstants_SHARED_LIBRARY_CONTAINER_EXT = ".shlibzip";
    static public final String AdminConstants_MSG_SUFFIX_MESSAGE = ".MESSAGE";
    
    

    
	@Override
	public void onGeneratePatternInstance(PatternInstanceManager patternInstanceManager) {
		
		String location = patternInstanceManager.getWorkspaceLocation(); // The location for the generated projects 		
		String patternInstanceName = patternInstanceManager.getPatternInstanceName(); // The pattern instance name for this generation		
		String ppBAR = patternInstanceManager.getParameterValue("ppBAR"); // The name of the selected BAR file
		String ppCheckSoftware = patternInstanceManager.getParameterValue("ppCheckSoftware"); 
		String ppCheckPrivate = patternInstanceManager.getParameterValue("ppCheckPrivate");
		String ppCheckPublic = patternInstanceManager.getParameterValue("ppCheckPublic");		
		String[] checklistArray = new String[]{ppCheckSoftware,ppCheckPrivate,ppCheckPublic};
		String outputProject = location + File.separator + patternInstanceName+"_TransformationAdvisor";
		initialiseLog4JAppender(outputProject);		
		logger.info("The value of pattern instance name is: "+patternInstanceName);
		logger.info("The value of location is: "+location);
		logger.info("The value of ppBAR is: "+ppBAR);
		logger.info("The value of ppCheckSoftware is: "+ppCheckSoftware);
		logger.info("The value of ppCheckPrivate is: "+ppCheckPrivate);
		logger.info("The value of ppCheckPublic is: "+ppCheckPublic);
		unzipAndRecurseThroughFlows(ppBAR,checklistArray,location + File.separator + patternInstanceName+"_TransformationAdvisor" + File.separator + "Results.html");

	}
	
	public static void initialiseLog4JAppender(String outputProject) { 

	  FileAppender fa = new FileAppender();
	  fa.setName("FileLogger");
	  fa.setFile((outputProject + File.separator + "TransformationAdvisorPattern.log"));
	  fa.setLayout(new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} %-5p [%c{1}] %m%n"));
	  fa.setThreshold(Level.INFO);
	  fa.setAppend(true);
	  fa.activateOptions();
	  logger.addAppender(fa);	  	  
	}
		
    public static void unzipAndRecurseThroughFlows(String zipName, String[] checklistArray, String fileName) {

		try {			
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipName));
			ZipEntry zipEntry;

		    boolean anyValidEntries = false; // In future will be used to check there is at least one valid entry in the BAR content zipstream
		    boolean seenApp = false; // In future will be used to check we have at least one application deployed at root BAR level
		    ArrayList<String> barErrorEntries = new ArrayList<String>();
		    ArrayList<String[]> resultsList = new ArrayList<String[]>();
		    // The resultsList is an ArrayList. Each entry is an array of 7 Strings. 
		    // These 7 Strings provide a representation of all the results of the analysis 		    
		    //0 Artifact name
		    //1 Runs in ACE software
		    //2 Comment for ACE software
		    //3 Runs in ACE on ICP
		    //4 Comment for ACE on ICP
		    //5 Runs in ACE on Public
		    //6 Comment for ACE on Public
		    
		    // If the root is not a BAR then we have recursed so do not check for appzip
		    if (!zipName.toLowerCase().endsWith("bar")) {
		      seenApp = true;
		    }
		    		    
			while(((zipEntry = zis.getNextEntry())!=null) && !zipEntry.isDirectory()) {
				String zipEntryName=zipEntry.getName();
				logger.info("Working through BAR file "+zipName+" and discovered "+zipEntryName);
				if (!zipEntryName.startsWith("src/") && !zipEntryName.startsWith("META-INF/")) {

			        // Once we have a single valid entry we don't need to check again
			        if (!anyValidEntries) {
			          anyValidEntries=isSupportedArtifact(zipEntryName);
			        }
			        
			        // Do not allow msgflows at the root of the bar
			        if (zipName.toLowerCase().endsWith(AdminConstants_BAR_EXTENSION) && zipEntryName.toLowerCase().endsWith("msgflow") ) {			          
			          barErrorEntries.add("A message flow file with the extension .msgflow was found at the root of the BAR file.");
			          String[] newResult = {zipEntryName,"true","","true","","false","A message flow file with the extension .msgflow was found at the root of the BAR file."};
			          resultsList.add(newResult);
			        }        

			        // Do not allow static libraries (libzips) at the root of the bar
			        if (zipName.toLowerCase().endsWith(AdminConstants_BAR_EXTENSION) && zipEntryName.toLowerCase().endsWith(AdminConstants_LIBRARY_CONTAINER_EXT) ) {
			          barErrorEntries.add("A static library with the extension .libzip was found at the root of the BAR file");
			          String[] newResult = {zipEntryName,"true","","true","","false","A static library with the extension .libzip was found at the root of the BAR file"};
			          resultsList.add(newResult);
			        } 
					
					// Recurse through appzip looking for flows and subflows					
					if (zipEntryName.toLowerCase().endsWith(AdminConstants_APPLICATION_CONTAINER_EXT)) {
						//logger.info("Discovered .appzip file in the BAR file: "+zipEntryName);
						byte[] bytes = extractZipEntryToByteArray(zis);
						ZipInputStream appzipzis = new ZipInputStream((new ByteArrayInputStream(bytes)));						
						ZipEntry appzipEntry;
						while(((appzipEntry = appzipzis.getNextEntry())!=null) && !appzipEntry.isDirectory()) {
							String appzipEntryName=appzipEntry.getName();
							logger.info("Working through appzip "+zipEntryName+" and discovered "+appzipEntryName);
							if (appzipEntryName.toLowerCase().endsWith("msgflow") || zipEntryName.toLowerCase().endsWith("subflow")) {								
								byte[] flowbytes = extractZipEntryToByteArray(appzipzis);
								String msgflowString = new String(flowbytes, "UTF-8");
								MessageFlow flow = FlowRendererMSGFLOW.read(msgflowString, appzipEntryName);
						        String[] newResult = analyseFlow(flow,checklistArray,zipEntryName+" - "+appzipEntryName);
						        resultsList.add(newResult);
							}									
						}
					}
					
					// Recurse through shlibzip looking for flows (shouldnt exist) and subflows
					if (zipEntryName.toLowerCase().endsWith(AdminConstants_SHARED_LIBRARY_CONTAINER_EXT)) {
						byte[] bytes = extractZipEntryToByteArray(zis);
						ZipInputStream shlibzipzis = new ZipInputStream((new ByteArrayInputStream(bytes)));						
						ZipEntry shlibzipEntry;
						while(((shlibzipEntry = shlibzipzis.getNextEntry())!=null) && !shlibzipEntry.isDirectory()) {
							String shlibzipEntryName=shlibzipEntry.getName();							
							logger.info("Working through shlibzip "+zipEntryName+" and discovered "+shlibzipEntryName);
							if (shlibzipEntryName.toLowerCase().endsWith("msgflow") || zipEntryName.toLowerCase().endsWith("subflow")) {								
								byte[] flowbytes = extractZipEntryToByteArray(shlibzipzis);
								String msgflowString = new String(flowbytes, "UTF-8");
								MessageFlow flow = FlowRendererMSGFLOW.read(msgflowString, shlibzipEntryName);
						        String[] newResult = analyseFlow(flow,checklistArray,zipEntryName+" - "+shlibzipEntryName);
						        resultsList.add(newResult);
							}									
						}
					}
					
				}
    		}
			writeResults(fileName,resultsList,zipName);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }
    
    private static byte[] extractZipEntryToByteArray(ZipInputStream zipStream) throws IOException {
   
      // Extract zip entry to byte[]
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      int data = 0;
      while( ( data = zipStream.read() ) != - 1 ) {
        output.write( data );
      }
      output.close();
      byte[] bytes =  output.toByteArray();
      return bytes;
    }
    
    public static String[] analyseFlow(MessageFlow mf, String[] checklistArray, String artifactName) {
    	
            String flowname = mf.getName();
            Vector<Node> mfnodes = new Vector<Node>(); 
            mfnodes = mf.getNodes();

            // Use two separate Hashmaps to store results ...
            // One for IBM Cloud Private and one for IBM Cloud Public
            HashMap<String,String> controversialNodesInFlowForCloudPrivate = new HashMap<String, String>();
            HashMap<String,String> disallowedNodesInFlowForCloudPublic = new HashMap<String, String>();
            
            String privateComment = ""; // This needs to be built up from the entries in controversialNodesInFlowForCloudPrivate
            if (checklistArray[1].equals("true")) {
            	// Check for suitability for ACE on ICP
            	Iterator<Node> j = mfnodes.iterator();
            	while (j.hasNext()) {
                    Node currentNode = (Node) j.next();
                    String currentNodeType = currentNode.getTypeName().toString();
                    if (	currentNodeType.equals("ComIbmSequenceNode")
                    		|| currentNodeType.equals("ComIbmReSequenceNode")
                    		|| currentNodeType.equals("ComIbmCollectorNode")
                    		|| currentNodeType.equals("ComIbmTimeoutControlNode")
                    		|| currentNodeType.equals("ComIbmTimeoutNotificationNode")
                    		|| currentNodeType.equals("ComIbmAggregateControlNode")
                    		|| currentNodeType.equals("ComIbmAggregateReplyNode")
                    		|| currentNodeType.equals("ComIbmAggregateRequestNode")
                    		|| currentNodeType.equals("ComIbmMQInputNode")
                    		|| currentNodeType.equals("ComIbmMQOutputNode")
                    		|| currentNodeType.equals("ComIbmMQGetNode")
                    		|| currentNodeType.equals("ComIbmMQReplyNode")                    		
                    	) {                    	
                    	controversialNodesInFlowForCloudPrivate.put(currentNode.getNodeName(), currentNodeType);
                    }
            	}
            	if (controversialNodesInFlowForCloudPrivate.size() > 0) {        	            		
            		Iterator<Map.Entry<String, String>> it = controversialNodesInFlowForCloudPrivate.entrySet().iterator();
            		while (it.hasNext()) {
            		    Map.Entry<String, String> pair = it.next();
               		    privateComment = privateComment + "<li>The message flow named "+flowname+" contains a node of type "+pair.getValue()+" named "+pair.getKey()+"</li>";
            		    logger.info("IBM Cloud Private: The message flow named "+flowname+" contains a node of type "+pair.getValue()+" named "+pair.getKey());
            		}            		
                }
            }
                     
            String publicComment = ""; // This needs to be built up from the entries in disallowedNodesInFlowForCloudPublic
            if (checklistArray[2].equals("true")) {
            	// Check for suitability for ACE on Cloud
            	logger.info("Run a simple compatability check for ACE on Cloud message flow nodes");          	
            	disallowedNodesInFlowForCloudPublic = checkForDisallowedNodesInFlow(mf, allowedNodes);
            	if (disallowedNodesInFlowForCloudPublic.size() > 0) {        	
                	// This is just a placeholder, and should become more sophisticated to make a row entry in a table
            		Iterator<Map.Entry<String, String>> it = disallowedNodesInFlowForCloudPublic.entrySet().iterator();
            		while (it.hasNext()) {
            		    Map.Entry<String, String> pair = it.next();
            		    publicComment = publicComment + "<li>The message flow named "+mf.getName()+" contains a node of type "+pair.getValue()+" named "+pair.getKey()+"</li>";
            		    logger.info("IBM Cloud App Connect Enterprise service: The message flow named "+mf.getName()+" contains a node of type "+pair.getValue()+" named "+pair.getKey());
            		}            		
                }                    

            }
            
            String publicResult = ""; 
            String privateResult = "";            
                        
            if (disallowedNodesInFlowForCloudPublic.size() > 0) {publicResult = "false";} else {publicResult = "true";}  
            if (controversialNodesInFlowForCloudPrivate.size() > 0) {privateResult = "false";} else {privateResult = "true";}
            String[] result = {artifactName,"true","",privateResult,privateComment,publicResult,publicComment};
            return result;
                    
    }
    

    public static void checkACEonCloud(MessageFlow mf) {
    	logger.info("Run a simple compatability check for ACE on Cloud message flow nodes");    	
    	HashMap<String,String> disallowedNodesInFlow;
        disallowedNodesInFlow = checkForDisallowedNodesInFlow(mf, allowedNodes);
        if (disallowedNodesInFlow.size() > 0) {        	
        	logger.info("The message flow named "+mf.getName()+" contains at least one instance of an message flow node which cannot be used with ACE on Cloud.");
        }    	
    }
    
  
    public static HashMap<String, String> checkForDisallowedNodesInFlow(MessageFlow flow, ArrayList<String> allowedNodes) {

      HashMap<String, String> disallowedNodesInFlow = new HashMap<String, String>();
      Vector<com.ibm.broker.config.appdev.Node> nodes = flow.getNodes();
      Iterator<com.ibm.broker.config.appdev.Node> nodeIterator = nodes.iterator();
      while (nodeIterator.hasNext()) {
        com.ibm.broker.config.appdev.Node nextNode = nodeIterator.next();
        String nodeType = nextNode.getTypeName();
        if (!allowedNodes.contains(nodeType) && (!(nextNode instanceof com.ibm.broker.config.appdev.SubFlowNode))) {
          disallowedNodesInFlow.put(nextNode.getNodeName(), nodeType);
        }
        
        
        // Check MQ nodes are using policy, if not reject the node
        if (nodeType.equals("ComIbmMQGetNode") || nodeType.equals("ComIbmMQOutputNode") ||
            nodeType.equals("ComIbmMQInputNode") ||
            nodeType.equals("ComIbmMQReplyNode")  ) {
          
        	NodeProperty policyProperty = nextNode.findPropertyByName("policyUrl");
        	if (policyProperty==null || policyProperty.equals("")) {          
        		disallowedNodesInFlow.put(nextNode.getNodeName(), nodeType);
        	}          
        }
        
        // Check file nodes are using FTP, if not reject the node
        if (nodeType.equals("ComIbmFileInputNode") || nodeType.equals("ComIbmFileOutputNode") ||
            nodeType.equals("ComIbmFileReadNode") ) {
               
          NodeProperty fileFtpProperty = nextNode.findPropertyByName("fileFtp");
          logger.info("File node fileFtpProperty:" + fileFtpProperty);
          logger.info("File node fileFtpProperty:" + fileFtpProperty.getPropertyValue()); 
          logger.info("File node fileFtpProperty:" + String.valueOf(fileFtpProperty.getPropertyValue()));
          if (!String.valueOf(fileFtpProperty.getPropertyValue()).equals("true")) {
            logger.info("File node does not have a valid fileFtpProperty"); 
            disallowedNodesInFlow.put(nextNode.getNodeName(), nodeType);
          } 
          
        }        
      }
      return disallowedNodesInFlow;
    }
    
    // Allowed message flow node types for ACEoC
    static final ArrayList<String> allowedNodes = new ArrayList<String>(Arrays.asList(
        "eflow:FCMSource",
        "eflow:FCMSink",
        "InputNode",
        "OutputNode",
        "ComIbmAppConnectRESTRequestNode",
        "ComIbmComputeNode",
        "ComIbmCallableFlowInputNode",
        "ComIbmCallableFlowReplyNode",
        "ComIbmCallableFlowInvokeNode",
        "ComIbmCallableFlowAsyncInvokeNode",
        "ComIbmCallableFlowAsyncResponseNode",
        "ComIbmDatabaseNode",
        "ComIbmDatabaseInputNode",
        "ComIbmExtractNode",
        "ComIbmFileInputNode",
        "ComIbmFileOutputNode",
        "ComIbmFileReadNode",
        "ComIbmFilterNode",
        "ComIbmFlowOrderNode",
        "ComIbmHTTPAsyncRequestNode",
        "ComIbmHTTPAsyncResponseNode",
        "ComIbmHTTPHeaderNode",
        "ComIbmJavaComputeNode",
        "ComIbmMQGetNode",
        "ComIbmMQOutputNode",
        "ComIbmMQInputNode",
        "ComIbmMQReplyNode",
        "ComIbmMSLMappingNode",
        "ComIbmPassthruNode",
        "ComIbmResetContentDescriptorNode",
        "ComIbmRESTRequestNode", 
        "ComIbmRESTAsyncRequestNode", 
        "ComIbmRESTAsyncResponseNode",
        "ComIbmRouteNode",
        "ComIbmRouteToLabelNode",
        "ComIbmSOAPAsyncRequestNode",
        "ComIbmSOAPAsyncResponseNode",
        "ComIbmSOAPEnvelopeNode",
        "ComIbmSOAPExtractNode",
        "ComIbmSOAPInputNode",
        "ComIbmSOAPReplyNode",
        "ComIbmSOAPRequestNode",
        "ComIbmTCPIPClientInputNode",
        "ComIbmTCPIPClientOutputNode",
        "ComIbmTCPIPClientReceiveNode",
        "ComIbmThrowNode",
        "ComIbmTraceNode",
        "ComIbmTryCatchNode",
        "ComIbmValidateNode",
        "ComIbmXslMqsiNode",
        "ComIbmWSInputNode",
        "ComIbmWSReplyNode",
        "ComIbmWSRequestNode",
        "ComIbmLabelNode",
        "com/ibm/connector/kafka/ComIbmOutputNode",
        "com/ibm/connector/kafka/ComIbmEventInputNode"
        ));

    // Check for a valid BAR File Entry
    public static boolean isSupportedArtifact(String fileName) {
      fileName = fileName.toLowerCase();
      if (fileName.endsWith(AdminConstants_XSDZIP_EXTENSION) ||
          fileName.endsWith(AdminConstants_RDB_SCHEMA_EXTENSION) ||
          fileName.endsWith(AdminConstants_XSD_FILE_EXTENSION) ||
          fileName.endsWith(AdminConstants_WSDL_FILE_EXTENSION) ||
          fileName.endsWith(AdminConstants_DICTIONARY_EXTENSION) ||
          fileName.endsWith(AdminConstants_XSL_EXTENSION) ||
          fileName.endsWith(AdminConstants_XSLT_EXTENSION) ||
          fileName.endsWith(AdminConstants_XML_EXTENSION) ||
          fileName.endsWith(AdminConstants_JAR_EXT) ||
          fileName.endsWith(AdminConstants_INADAPTER_FILE_EXTENSION) ||
          fileName.endsWith(AdminConstants_OUTADAPTER_FILE_EXTENSION) ||
          fileName.endsWith(AdminConstants_INSCA_FILE_EXTENSION) ||
          fileName.endsWith(AdminConstants_OUTSCA_FILE_EXTENSION) ||
          fileName.endsWith(AdminConstants_PHP_FILE_EXTENSION) ||
          fileName.endsWith(AdminConstants_IDL_EXTENSION) ||
          fileName.endsWith(AdminConstants_MAP_EXTENSION) ||
          fileName.endsWith(AdminConstants_ESQL_FILE_EXTENSION) ||
          fileName.endsWith(AdminConstants_RULE_EXTENSION) ||
          fileName.endsWith(AdminConstants_RULES_EXTENSION) ||
          fileName.endsWith(AdminConstants_DESCRIPTOR_EXTENSION) ||
          fileName.endsWith(AdminConstants_MQSC_FILE_EXTENSION) ||
          fileName.endsWith(AdminConstants_TESTCASE_EXTENSION) ||
          fileName.endsWith(AdminConstants_JSON_EXTENSION) ||
          fileName.endsWith(AdminConstants_APPLICATION_CONTAINER_EXT) ||
          fileName.endsWith(AdminConstants_LIBRARY_CONTAINER_EXT) ||
          fileName.endsWith(AdminConstants_DOTNET_CONTAINER_EXT) ||
          fileName.endsWith(AdminConstants_SHARED_LIBRARY_CONTAINER_EXT)
          ) {
        return true;
      }
      return false;
  }    
    
    public static void writeResults(String fileName,ArrayList<String[]> resultsList, String BARName) {
    	
	File file = new File(fileName);
	FileOutputStream fop;
	try {
		fop = new FileOutputStream(file);
		// if file does not exist, then create it
		if (!file.exists()) {
			file.createNewFile();
		}
		String htmlcontent = "<!DOCTYPE html><html><head><style>"+
		"body {font-family: calibri;}"+
		"table {border: 1px solid black;margin-top: 25px;}"+
		"tr {border-bottom: 1px solid black;padding: 5px;}"+
		"td {border-bottom: 1px solid black;padding: 5px;}"+
		".collapsible {background-color: #eee;color: #444;cursor: pointer;padding: 18px;width: 100%;border: none;text-align: left;outline: none;font-size: 15px;}"+
		".active, .collapsible:hover {background-color: #ccc;}"+
		".content {padding: 0 18px;display: none;overflow: hidden;background-color: #f1f1f1;}"+		
		"</style></head><body>" +
		"<h2><img src=\"appconnect_enterprise_logo.png\" width=\"75\" height=\"75\" align=\"middle\"/> Transformation Advisor Results</h2>The results below show the outcome of analysing BAR file "+BARName+"<table width=\"100%\" height=\"50%\"><tr bgcolor=\"blue\" style=\"font-family:'Calibri'\" padding=\"5\"><th>Message Flow Name</th><th>Runs in ACE software</th><th>Runs in ACE on IBM Cloud Private</th><th>Runs in ACE on IBM Cloud</th></tr>";		
		int footnotesCounter = 0;
		ArrayList<String> footnotes = new ArrayList<String>();		
		Iterator<String[]> iterator = resultsList.iterator();
		while (iterator.hasNext()) {
			String iconSoftwareResult;
			String iconPublicResult;
            String iconPrivateResult;
            String[] currentEntry = iterator.next();
            if (currentEntry[1].equals("true")) {iconSoftwareResult = "<img src=\"tick.gif\"/>";} else {
            	iconSoftwareResult = "<img src=\"cross.gif\"/> Note "+Integer.toString(footnotesCounter);
            	footnotes.add(currentEntry[2]);
            	footnotesCounter++;
            }
            if (currentEntry[3].equals("true")) {iconPublicResult = "<img src=\"tick.gif\"/>";} else {            	
            	iconPublicResult = "<img src=\"cross.gif\"/> Note "+Integer.toString(footnotesCounter);
            	footnotes.add(currentEntry[4]);
            	footnotesCounter++;            
            }
            if (currentEntry[5].equals("true")) {iconPrivateResult = "<img src=\"tick.gif\"/>";} else {
            	iconPrivateResult = "<img src=\"cross.gif\"/> Note "+Integer.toString(footnotesCounter);
            	footnotes.add(currentEntry[6]);
            	footnotesCounter++;            
            }			
			htmlcontent = htmlcontent + 
					"<tr bgcolor=\"#DEDEDE\" style=\"font-family:'Calibri'\" padding=\"5\"><td align=\"center\">"+currentEntry[0]+"</td><td align=\"center\">"+iconSoftwareResult+"</td><td align=\"center\">"+ iconPrivateResult +"</td><td align=\"center\">"+ iconPublicResult +"</td></tr>";
		}		
		htmlcontent = htmlcontent + "</table><p></p>";
		Iterator<String> footnoteIterator = footnotes.iterator();
		int footnotesTracker = 0;
		while (footnoteIterator.hasNext()) {
			String currentFootnote = footnoteIterator.next();
			htmlcontent = htmlcontent + "<button class=\"collapsible\"><strong>Note "+Integer.toString(footnotesTracker) + "</strong></button><div class=\"content\"><p><ul>"+ currentFootnote+"</ul></p></div><p></p>";			
			footnotesTracker++;
		}
		htmlcontent = htmlcontent + 
		"<script>"+
		"var coll = document.getElementsByClassName(\"collapsible\");"+
		"var i;"+
		"for (i = 0; i < coll.length; i++) {"+
		"    coll[i].addEventListener(\"click\", function() {"+
		"        this.classList.toggle(\"active\");"+
		"        var content = this.nextElementSibling;"+
		"        if (content.style.display === \"block\") {"+
		"            content.style.display = \"none\";"+
		"        } else {"+
		"            content.style.display = \"block\";"+
		"        }"+
		"    });"+
		"}"+
		"</script>"+
		"</body>"+
		"</html>";
		
		logger.info("About to write htmlcontent: "+htmlcontent);
		byte[] contentInBytes = htmlcontent.getBytes();
		fop.write(contentInBytes);
		fop.flush();
		fop.close();
		logger.removeAllAppenders();
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }  
}
