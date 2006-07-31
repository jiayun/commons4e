//$Id$
package org.jiayun.commons4e.internal.util;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.jiayun.commons4e.Commons4ePlugin;
import org.jiayun.commons4e.internal.ui.preferences.PreferenceConstants;

/**
 * @author jiayun
 */
public final class PreferenceUtils {

    private PreferenceUtils() {
    }

    public static boolean getCacheHashCode() {
        return Commons4ePlugin.getDefault().getPluginPreferences().getBoolean(
                PreferenceConstants.CACHE_HASHCODE);
    }

    public static String getHashCodeCachingField() {
        return Commons4ePlugin.getDefault().getPluginPreferences().getString(
                PreferenceConstants.HASHCODE_CACHING_FIELD);
    }

    public static boolean getCacheToString() {
        return Commons4ePlugin.getDefault().getPluginPreferences().getBoolean(
                PreferenceConstants.CACHE_TOSTRING);
    }

    public static String getToStringCachingField() {
        return Commons4ePlugin.getDefault().getPluginPreferences().getString(
                PreferenceConstants.TOSTRING_CACHING_FIELD);
    }

    public static boolean getAddOverride() {
        return Commons4ePlugin.getDefault().getPluginPreferences().getBoolean(
                PreferenceConstants.ADD_OVERRIDE_ANNOTATION);
    }

    public static boolean getGenerifyCompareTo() {
        return Commons4ePlugin.getDefault().getPluginPreferences().getBoolean(
                PreferenceConstants.GENERIFY_COMPARETO);
    }

    public static boolean getDisplayFieldsOfSuperclasses() {
        return Commons4ePlugin.getDefault().getPluginPreferences().getBoolean(
                PreferenceConstants.DISPLAY_FIELDS_OF_SUPERCLASSES);
    }

    public static boolean isSourceLevelGreaterThanOrEqualTo5(
            IJavaProject project) {
        float sc = Float.parseFloat(project.getOption(JavaCore.COMPILER_SOURCE,
                true));
        return sc >= 1.5;
    }
}
