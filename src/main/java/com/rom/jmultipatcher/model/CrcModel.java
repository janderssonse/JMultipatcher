package com.rom.jmultipatcher.model;

public class CrcModel {

    private long sourceCrc;
    private long targetCrc;
    private long patchCrc;

    public long getSourceCrc() {
        return sourceCrc;
    }

    public long getTargetCrc() {
        return targetCrc;
    }

    public void setTargetCrc(final long targetCrc) {
        this.targetCrc = targetCrc;
    }

    public long getPatchCrc() {
        return patchCrc;
    }

    public void setPatchCrc(final long patchCrc) {
        this.patchCrc = patchCrc;
    }

    public void setSourceCrc(final long sourceCrc) {
        this.sourceCrc = sourceCrc;
    }
}
