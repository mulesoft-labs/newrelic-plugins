package org.mule.newrelic.plugins.artemis.util;

import com.arakelian.jq.ImmutableJqLibrary;
import com.arakelian.jq.JqLibrary;

public class JQHolder {

    public static final JqLibrary LIBRARY = ImmutableJqLibrary.of();
}
