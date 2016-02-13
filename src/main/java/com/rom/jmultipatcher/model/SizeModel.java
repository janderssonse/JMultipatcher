package com.rom.jmultipatcher.model;

public class SizeModel {

    private long sourceSize;
    private long targetSize;
    private long patchSize;

    public long getSourceSize() {
        return sourceSize;
    }

    public void setSourceSize(final long sourceSize) {
        this.sourceSize = sourceSize;
    }

    public long getTargetSize() {
        return targetSize;
    }

    public void setTargetSize(final long targetSize) {
        this.targetSize = targetSize;
    }

    public long getPatchSize() {
        return patchSize;
    }

    public void setPatchSize(final long patchSize) {
        this.patchSize = patchSize;
    }

}
