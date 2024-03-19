package com.seabrief.Logger.Parser.Extractors.SQL;

import java.util.Locale;

public class SQL {
    private String pattern;

    public SQL() {
        pattern = "";
    }

    public SQL pattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public SQL field(String key, Object variable) {
        if (variable instanceof Double || variable instanceof Float) {
            pattern = pattern.replace("@" + key, String.format(Locale.US, "%.4f", variable));
        } else {
            pattern = pattern.replace("@" + key, variable.toString());
        }

        return this;
    }

    public String build() {
        return pattern;
    }
}
