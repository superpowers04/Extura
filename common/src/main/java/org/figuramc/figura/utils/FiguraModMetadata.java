package org.figuramc.figura.utils;

import dev.architectury.injectables.annotations.ExpectPlatform;

public abstract class FiguraModMetadata {
    private final String modId;
    protected FiguraModMetadata(String modID) {
        this.modId = modID;
    }

    public abstract String getCustomValueAsString(String key);
    public abstract Number getCustomValueAsNumber(String key);
    public abstract Boolean getCustomValueAsBoolean(String key);
    public abstract Object getCustomValueAsObject(String key);

    public abstract Version getModVersion();

    public String getModId() {
        return this.modId;
    }
    @ExpectPlatform
    public static FiguraModMetadata getMetadataForMod(String modID) {
        // note: this is needed to get FiguraMod to not assert under testing conditions
        // should not happen in any production (because @ExpectPlatform)
        throw new AssertionError();
    }
}
