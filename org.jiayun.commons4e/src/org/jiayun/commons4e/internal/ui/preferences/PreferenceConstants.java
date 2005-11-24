//$Id$
package org.jiayun.commons4e.internal.ui.preferences;

import org.eclipse.core.runtime.Preferences;
import org.jiayun.commons4e.Commons4ePlugin;

/**
 * @author jiayun
 */
public class PreferenceConstants {

    private PreferenceConstants() {
    }

    public static final String CACHE_HASHCODE = "cacheHashCode";

    public static final String HASHCODE_CACHING_FIELD = "hashCodeCachingField";

    public static final String CACHE_TOSTRING = "cacheToString";

    public static final String TOSTRING_CACHING_FIELD = "toStringCachingField";

    public static final String ADD_OVERRIDE_ANNOTATION = "addOverrideAnnotation";

    public static final String GENERIFY_COMPARETO = "generifyCompareTo";

    public static void initializeDefaultValues() {
        Preferences preferences = Commons4ePlugin.getDefault()
                .getPluginPreferences();
        preferences.setDefault(CACHE_HASHCODE, true);
        preferences.setDefault(HASHCODE_CACHING_FIELD, "hashCode");
        preferences.setDefault(CACHE_TOSTRING, true);
        preferences.setDefault(TOSTRING_CACHING_FIELD, "toString");
        preferences.setDefault(ADD_OVERRIDE_ANNOTATION, true);
        preferences.setDefault(GENERIFY_COMPARETO, true);
    }
}
