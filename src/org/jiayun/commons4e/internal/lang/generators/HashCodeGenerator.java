/*
 * Created on 2004/7/24
 */
package org.jiayun.commons4e.internal.lang.generators;

import org.eclipse.jdt.core.IType;
import org.eclipse.swt.widgets.Shell;


/**
 * @author jiayun
 */
public final class HashCodeGenerator implements ILangGenerator {
    
    public static final ILangGenerator instance = new HashCodeGenerator();
    
    private HashCodeGenerator() {
    }
    
    public static ILangGenerator getInstance() {
        return instance;
    }

    /* (non-Javadoc)
     * @see org.jiayun.commons4e.internal.lang.generators.ILangGenerator#generate(org.eclipse.swt.widgets.Shell, org.eclipse.jdt.core.IType)
     */
    public void generate(Shell parentShell, IType objectClass) {
        // TODO Auto-generated method stub
        System.out.println("generate hashCode()");

    }

}
