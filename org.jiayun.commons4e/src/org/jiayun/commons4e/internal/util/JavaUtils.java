//$Id$
package org.jiayun.commons4e.internal.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.swt.SWT;

/*
 * This class contains some code from
 *      org.eclipse.jdt.internal.corext.codemanipulation.StubUtility
 *      org.eclipse.jdt.internal.corext.util.Strings
 */
/**
 * @author jiayun
 */
public final class JavaUtils {

    private JavaUtils() {
    }

    public static void addSuperInterface(final IType objectClass,
            final String interfaceName) throws JavaModelException,
            InvalidInputException {

        ITypeHierarchy typeHierarchy = objectClass.newSupertypeHierarchy(null);
        IType[] interfaces = typeHierarchy.getAllInterfaces();
        for (int i = 0, size = interfaces.length; i < size; i++) {
            if (interfaces[i].getElementName().equals(interfaceName)) { return; }
        }

        ICompilationUnit cu = objectClass.getCompilationUnit();
        IBuffer buffer = cu.getBuffer();
        char[] source = buffer.getCharacters();
        IScanner scanner = ToolFactory
                .createScanner(false, false, false, false);
        scanner.setSource(source);
        scanner.resetTo(objectClass.getNameRange().getOffset(),
                source.length - 1);

        if (objectClass.getSuperInterfaceNames().length == 0) {

            while (true) {
                int token = scanner.getNextToken();
                if (token == ITerminalSymbols.TokenNameLBRACE) {

                    buffer.replace(scanner.getCurrentTokenStartPosition(), 0,
                            "implements " + interfaceName + " ");
                    break;
                }
            }

        } else {

            while (true) {
                int token = scanner.getNextToken();
                if (token == ITerminalSymbols.TokenNameimplements) {

                    buffer.replace(scanner.getCurrentTokenEndPosition() + 1, 0,
                            " " + interfaceName + ",");
                    break;
                }
            }

        }

    }

    public static IField[] getNonStaticFields(final IType objectClass)
            throws JavaModelException {

        IField[] fields;
        fields = objectClass.getFields();

        List result = new ArrayList();

        for (int i = 0, size = fields.length; i < size; i++) {
            if (!Flags.isStatic(fields[i].getFlags())) {
                result.add(fields[i]);
            }
        }

        return (IField[]) result.toArray(new IField[result.size()]);
    }

    public static String getMethodLabel(final IMethod method) {
        StringBuffer result = new StringBuffer("`");

        String[] params = method.getParameterTypes();

        result.append(method.getElementName());
        result.append("(");
        for (int i = 0; i < params.length; i++) {
            if (i != 0) {
                result.append(", ");
            }
            result.append(Signature.toString(params[i]));
        }
        result.append(")`");

        return result.toString();
    }

    /**
     * Examines a string and returns the first line delimiter found.
     */
    public static String getLineDelimiterUsed(IJavaElement elem)
            throws JavaModelException {
        ICompilationUnit cu = (ICompilationUnit) elem
                .getAncestor(IJavaElement.COMPILATION_UNIT);
        if (cu != null && cu.exists()) {
            IBuffer buf = cu.getBuffer();
            int length = buf.getLength();
            for (int i = 0; i < length; i++) {
                char ch = buf.getChar(i);
                if (ch == SWT.CR) {
                    if (i + 1 < length) {
                        if (buf.getChar(i + 1) == SWT.LF) { return "\r\n"; //$NON-NLS-1$
                        }
                    }
                    return "\r"; //$NON-NLS-1$
                } else if (ch == SWT.LF) { return "\n"; //$NON-NLS-1$
                }
            }
        }
        return System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Evaluates the indention used by a Java element. (in tabulators)
     */
    public static int getIndentUsed(IJavaElement elem)
            throws JavaModelException {
        if (elem instanceof ISourceReference) {
            ICompilationUnit cu = (ICompilationUnit) elem
                    .getAncestor(IJavaElement.COMPILATION_UNIT);
            if (cu != null) {
                IBuffer buf = cu.getBuffer();
                int offset = ((ISourceReference) elem).getSourceRange()
                        .getOffset();
                int i = offset;
                // find beginning of line
                while (i > 0 && !isLineDelimiterChar(buf.getChar(i - 1))) {
                    i--;
                }
                return computeIndent(buf.getText(i, offset - i), getTabWidth());
            }
        }
        return 0;
    }

    private static int getTabWidth() {
        Preferences preferences = JavaCore.getPlugin().getPluginPreferences();
        return preferences
                .getInt(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE);
    }

    /**
     * Indent char is a space char but not a line delimiters.
     * <code>== Character.isWhitespace(ch) && ch != '\n' && ch != '\r'</code>
     */
    private static boolean isIndentChar(char ch) {
        return Character.isWhitespace(ch) && !isLineDelimiterChar(ch);
    }

    /**
     * Line delimiter chars are '\n' and '\r'.
     */
    private static boolean isLineDelimiterChar(char ch) {
        return ch == '\n' || ch == '\r';
    }

    /**
     * Returns the indent of the given string.
     * 
     * @param line
     *            the text line
     * @param tabWidth
     *            the width of the '\t' character.
     */
    private static int computeIndent(String line, int tabWidth) {
        int result = 0;
        int blanks = 0;
        int size = line.length();
        for (int i = 0; i < size; i++) {
            char c = line.charAt(i);
            if (c == '\t') {
                result++;
                blanks = 0;
            } else if (isIndentChar(c)) {
                blanks++;
                if (blanks == tabWidth) {
                    result++;
                    blanks = 0;
                }
            } else {
                return result;
            }
        }
        return result;
    }
}
