/*
 * Created on 2004/7/24
 */
package org.jiayun.commons4e.internal.lang.generators;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * @author jiayun
 */
public final class EqualsGenerator implements ILangGenerator {
    
    public static final ILangGenerator instance = new EqualsGenerator();
    
    private EqualsGenerator() {
    }
    
    public static ILangGenerator getInstance() {
        return instance;
    }

    /* (non-Javadoc)
     * @see org.jiayun.commons4e.internal.lang.generators.ILangGenerator#generate(org.eclipse.swt.widgets.Shell, org.eclipse.jdt.core.IType)
     */
    public void generate(Shell parentShell, IType objectClass) {
        // TODO Auto-generated method stub
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(parentShell, new JavaElementLabelProvider());
        try {
            dialog.setElements(objectClass.getFields());
        } catch (JavaModelException e) {
            // getFields() failed, do nothing
        }
        dialog.open();

    }

}
