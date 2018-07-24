/**
 * Pattern program for use with IBM WebSphere Message Broker.
 *
 * COPYRIGHT NOTICE AND LICENSE
 * © Copyright International Business Machines Corporation 2009, 2011
 * Licensed Materials - Property of IBM
 *
 * On condition that the user is also then a licensed user of the specific 
 * version of the IBM product named above, this pattern program may be   
 * used, executed, copied and modified without obligation to make any  
 * royalty payment to IBM, as follows:
 *
 * (a) for the user's own instruction and study; and
 *
 * (b) in order to develop one or more applications designed to run with an IBM
 *     WebSphere Message Broker software product, either (i) for the licensed user's
 *     own internal use or (ii) for redistribution by the licensed user, as part of  
 *     such an application and in the licensed user's own product or products.
 *
 * No other rights under copyright are granted without prior written permission
 * of International Business Machines Corporation.
 *
 * In all other respects, the licensing terms and conditions associated with
 * the above-named IBM product continue to apply without modification.
 *
 * NO WARRANTY 
 * These materials and this sample program illustrate programming techniques. 
 * They have not been thoroughly tested under all conditions. 
 *
 * IBM therefore cannot and does not in any way guarantee, warrant represent 
 * or imply the reliability, serviceability, or function of this sample program. 
 * 
 * To the fullest extent permitted by applicable law, this program is provided by  
 * IBM "As Is", without warranty of any kind (express or implied), including without  
 * limitation any implied warranty of merchantability (satisfactory quality) or fitness 
 * for any particular purpose.
 */

package com.ibm.TransformationAdvisorPattern.pattern;

import java.util.Map;
import org.eclipse.osgi.util.NLS;
import com.ibm.TransformationAdvisorPattern.plugin.PatternBundle;
import com.ibm.TransformationAdvisorPattern.plugin.PatternPlugin;
import com.ibm.etools.patterns.model.base.IPatternBundle;

public class PatternMessages extends PatternBundle implements IPatternBundle {
	private static final String BUNDLE_NAME = "com.ibm.TransformationAdvisorPattern.pattern.messages"; //$NON-NLS-1$
	private static final Map<String, String> map;	
	private static final String[] enumerations = {
	 	"5061747465726E7321424152", //$NON-NLS-1$
	 	"5061747465726E73214469726563746F7279", //$NON-NLS-1$
	};
	
	public static String getStringStatic(String key) {
		return map.get(key);
	}
	
	public String getString(String key) {
		return map.get(key);
	}

	public static String com_ibm_TransformationAdvisorPattern_pattern_group_Id164931d9d29d9b92371a8f73775;		
	public static String com_ibm_TransformationAdvisorPattern_pattern_group_Id164931d9d29d9b92371a8f73775_description;		



	public static String com_ibm_TransformationAdvisorPattern_pattern_pov_root_ppBAR;		
	public static String com_ibm_TransformationAdvisorPattern_pattern_pov_root_ppBAR_watermark;		

	public static String com_ibm_TransformationAdvisorPattern_pattern_pov_root_ppCheckSoftware;		
	public static String com_ibm_TransformationAdvisorPattern_pattern_pov_root_ppCheckSoftware_watermark;		

	public static String com_ibm_TransformationAdvisorPattern_pattern_pov_root_ppCheckPrivate;		
	public static String com_ibm_TransformationAdvisorPattern_pattern_pov_root_ppCheckPrivate_watermark;		

	public static String com_ibm_TransformationAdvisorPattern_pattern_pov_root_ppCheckPublic;		
	public static String com_ibm_TransformationAdvisorPattern_pattern_pov_root_ppCheckPublic_watermark;		








	public static String com_ibm_TransformationAdvisorPattern_pattern_pov_Id1649319b30b34a7d512064e0a97_5061747465726E7321424152;		
	public static String com_ibm_TransformationAdvisorPattern_pattern_pov_Id1649319b30b34a7d512064e0a97_5061747465726E73214469726563746F7279;		

	
	static {
		NLS.initializeMessages(BUNDLE_NAME, PatternMessages.class);
		PatternPlugin.addBundle(PatternMessages.class);
		map = PatternBundle.createMessageMap(PatternMessages.class, enumerations);
	}
}
