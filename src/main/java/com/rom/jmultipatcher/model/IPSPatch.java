package com.rom.jmultipatcher.model;

import com.rom.jmultipatcher.Utils;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class IPSPatch {

    private byte[] PATCH_BYTES = new byte[]{0x50, 0x41, 0x54, 0x43, 0x48};
    private byte[] EOF_BYTES = new byte[]{0x45, 0x4f, 0x46};
    private List<byte[]> records = new ArrayList<>();

    public void addRecord(byte[] offset, byte[] data) {
        // 3 bytes for offset, 2 bytes for size, then data
        byte[] offsetBytes = new byte[3];
        byte[] sizeBytes = new byte[2];

        offsetBytes[0] = offset[1];
        offsetBytes[1] = offset[2];
        offsetBytes[2] = offset[3];

        byte[] dataSize = Utils.byteArrayfromInt(data.length);
        sizeBytes[1] = dataSize[3];
        sizeBytes[0] = dataSize[2];

        ByteBuffer recordBB = ByteBuffer.allocate(offsetBytes.length + sizeBytes.length + data.length);

        byte[] concatByteArray = Utils.concatByteArray(offsetBytes, sizeBytes, offsetBytes.length + sizeBytes.length, 0);

        concatByteArray = Utils.concatByteArray(concatByteArray, data, concatByteArray.length + data.length, 0);

        records.add(concatByteArray);

    }

    public byte[] toByteArray() {
        int totalByteArraySize = 0;
        for (byte[] byteArr : records) {
            totalByteArraySize += byteArr.length;
        }
        totalByteArraySize += 8; // "PATCH" + data + "EOF";
        ByteBuffer data = ByteBuffer.allocate(totalByteArraySize);
        byte[] dataBB = data.array();
        int offset = 5;
        for (byte[] byteArr : records) {

                    System.arraycopy(byteArr, 0, dataBB, offset, byteArr.length);
    
            offset += byteArr.length;
        }

        return data.array();
    }
}

class IPSRecord {

    public IPSRecord(int offset, int size, byte[] data) {
    }
    byte[] recordData;
}
