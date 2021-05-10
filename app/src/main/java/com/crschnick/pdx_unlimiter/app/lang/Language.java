package com.crschnick.pdx_unlimiter.app.lang;

import java.util.Locale;

public class Language {

    public static Language ENGLISH = new Language(Locale.ENGLISH, "l_english", "English");

    private final Locale locale;
    private final String id;
    private final String displayName;

    public Language(Locale locale, String id, String displayName) {
        this.locale = locale;
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}
