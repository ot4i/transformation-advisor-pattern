package org.eclipse.jet.compiled;

import org.eclipse.jet.JET2Context;
import org.eclipse.jet.JET2Template;
import org.eclipse.jet.JET2Writer;

public class _jet_main implements JET2Template {

    public _jet_main() {
        super();
    }

    private static final String NL = System.getProperty("line.separator"); //$NON-NLS-1$

    public void generate(final JET2Context context, final JET2Writer __out) {
        JET2Writer out = __out;
 com.ibm.etools.mft.pattern.support.Generation.onGenerate(context, "com.ibm.TransformationAdvisorPattern", "Id16492efc8ed1be137fffe7c5be4", "Id16492efc8edca178009d6aa18a7", "pattern"); 
        out.write(NL);         
    }
}
