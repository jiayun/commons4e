//$Id$
package org.jiayun.commons4e.internal.lang.generators;

import java.util.HashMap;
import java.util.Map;

/**
 * @author jiayun
 */
public final class LangGenerators {

    private LangGenerators() {
    }

    public static final String COMPARETO_GENERATOR_KEY = "org.jiayun.commons4e.lang.actions.GenerateCompareToAction";

    public static final String EQUALS_HASHCODE_GENERATOR_KEY = "org.jiayun.commons4e.lang.actions.GenerateEqualsHashCodeAction";

    public static final String TOSTRING_GENERATOR_KEY = "org.jiayun.commons4e.lang.actions.GenerateToStringAction";

    private static final Map generators = new HashMap();

    static {
        generators.put(COMPARETO_GENERATOR_KEY, CompareToGenerator
                .getInstance());
        generators.put(EQUALS_HASHCODE_GENERATOR_KEY, EqualsHashCodeGenerator.getInstance());
        generators.put(TOSTRING_GENERATOR_KEY, ToStringGenerator.getInstance());
    }

    public static ILangGenerator getGenerator(String key) {
        return (ILangGenerator) generators.get(key);
    }
}
