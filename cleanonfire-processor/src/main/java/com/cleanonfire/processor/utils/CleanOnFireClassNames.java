package com.cleanonfire.processor.utils;

import com.squareup.javapoet.ClassName;

/**
 * Created by heitorgianastasio on 03/10/17.
 */

public final class CleanOnFireClassNames {
    private CleanOnFireClassNames(){}

    public static ClassName BASE_DAO = ClassName.get("com.cleanonfire.api.data.orm","BaseCleanDAO");
    public static ClassName CLEAN_SQLITE_HELPER = ClassName.get("com.cleanonfire.api.data.orm","SQLiteCleanHelper");
}
