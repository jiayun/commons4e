//$Id$
package org.jiayun.commons4e.internal.lang.actions;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.jiayun.commons4e.internal.lang.generators.LangGenerators;

/**
 * @author jiayun
 */
public final class GenerateAction implements IObjectActionDelegate {

    private IType objectClass;

    private Shell parentShell;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
     *      org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        parentShell = targetPart.getSite().getShell();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        LangGenerators.getGenerator(action.getId()).generate(parentShell,
                objectClass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {

        if (selection != null && selection instanceof IStructuredSelection) {

            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            Object firstElement = structuredSelection.getFirstElement();

            if (firstElement != null && firstElement instanceof IType) {

                IType selected = (IType) firstElement;
                try {
                    if (selected.isClass() && !selected.isReadOnly()) {
                        objectClass = selected;
                        action.setEnabled(true);
                        return;
                    }
                } catch (JavaModelException e) {
                    MessageDialog.openError(parentShell, "Error", e
                            .getMessage());
                }
            }
        }

        objectClass = null;
        action.setEnabled(false);
    }

}
