/* JMultiPatcher - yet another rompatcher utility
 Copyright © 2014 Josef Andersson <josef.andersson@fripost.org>

 This file is part of JMultiPatcher.

 JMultiPatcher is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 any later version.

 JMultiPatcher is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with JMultiPatcher.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.rom.jmultipatcher.patchformat;

import com.rom.jmultipatcher.model.CrcModel;
import com.rom.jmultipatcher.model.FileManagerModel;
import com.rom.jmultipatcher.model.SizeModel;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class IpsPatcherTest {

    /* Integration-like test with real files. 
    
     IPS input in HEX
     P  A  T  C  H              Offset:     Size    data    Offset      Size    Data                     E  O  F  
     /* IPS: 50 41 54 43 48     00 00 03    00 01   FF      00 10 03    00 05   FF FF FF FF FF          45 4F 46 */
    @Test
    public void testMultipleOffsetVariedDataSize() throws Exception {

        String patchfilePath = filepathAsString(File.separator + "ipspatches" + File.separator + "multipleOffsetVariedDataSizeTest.ips");
        String sourcefilePath = filepathAsString(File.separator + "roms" + File.separator + "zeros.rom");
        String outputFilePatch = sourcefilePath.replace(File.separator + "roms" + File.separator + "zeros.rom", File.separator + "output" + File.separator + "zeros.patched.rom");
        FileManagerModel fileManager = new FileManagerModel(new CrcModel(),new SizeModel());
        fileManager.setPatchfilepath(patchfilePath);
        fileManager.setSourcefilepath(sourcefilePath);
        fileManager.setTargetfilepath(outputFilePatch);

        IpsPatcher instance = new IpsPatcher();
        instance.applyPatch(fileManager);

        try (RandomAccessFile patchedFile = new RandomAccessFile(outputFilePatch, "r");) {

            byte[] offset = {00, 00, 03};
            byte[] dataSizeSource = {00, 01};
            int dataSizeAsInt = IpsPatcher.getDataSizeAsInt(dataSizeSource);
            int offsetAsInt = IpsPatcher.getOffsetAsInt(offset);
            byte data[] = new byte[dataSizeAsInt];
            patchedFile.seek(offsetAsInt);
            int read = patchedFile.read(data);
            assertEquals(1, read);//bytes read
            assertEquals(data[0], -1);//FF hex (signed )

            byte[] offset2 = {00, 16, 03};
            byte[] dataSizeSource2 = {00, 05};
            int dataSizeAsInt2 = IpsPatcher.getDataSizeAsInt(dataSizeSource2);
            int offsetAsInt2 = IpsPatcher.getOffsetAsInt(offset2);
            byte data2[] = new byte[dataSizeAsInt2];
            patchedFile.seek(offsetAsInt2);
            int read2 = patchedFile.read(data2);
            assertEquals(5, read2);//bytes read
            assertEquals(data[0], -1);//FF hex (signed )
        }
    }

    /* Integration-like test with real files. 
    
     IPS input in HEX
     P  A  T  C  H              Offset:         Size    Data    Offset      RLE     Size    RleValue      E  O  F  
     /* 50 41 54 43 48          00 00 03        00 01   FF      00 10 03    00 00   00 11   FF            45 4F 46 */
    @Test
    public void testMultipleOffsetVariedDataSizeAndRLE() throws Exception {
        String patchfilePath = filepathAsString(File.separator + "ipspatches" + File.separator + "multipleOffsetVariedDataSizeRLETest.ips");
        String sourcefilePath = filepathAsString(File.separator + "roms" + File.separator + "zeros.rom");
        String outputFilePatch = sourcefilePath.replace(File.separator + "roms" + File.separator + "zeros.rom", File.separator + "output" + File.separator + "zeros.patched.rom");
      
        IPatcher instance = new IpsPatcher();
    
        FileManagerModel fileManager = new FileManagerModel(new CrcModel(), new SizeModel());
        fileManager.setPatchfilepath(patchfilePath);
        fileManager.setSourcefilepath(sourcefilePath);
        fileManager.setTargetfilepath(outputFilePatch);
        
        instance.applyPatch(fileManager);

        try (RandomAccessFile patchedFile = new RandomAccessFile(outputFilePatch, "r");) {

            byte[] offset = {00, 00, 03};//signed bytes
            byte[] dataSizeSource = {00, 01};
            int dataSizeAsInt = IpsPatcher.getDataSizeAsInt(dataSizeSource);
            int offsetAsInt = IpsPatcher.getOffsetAsInt(offset);
            byte data[] = new byte[dataSizeAsInt];
            patchedFile.seek(offsetAsInt);
            int read = patchedFile.read(data);
            assertEquals(1, read);//bytes read
            assertEquals(data[0], -1);//0

            byte[] offset2 = {00, 16, 03};//signed bytes
            int offsetAsInt2 = IpsPatcher.getOffsetAsInt(offset2);
            byte data2[] = new byte[11];
            patchedFile.seek(offsetAsInt2);
            int read2 = patchedFile.read(data2);
            assertEquals(11, read2);//bytes read
            for (int i = 0; i < 11; i++) {
                assertEquals(data2[i], -1);//FF hex signed
            }
        }
    }

    private String filepathAsString(String relativeTestfilePath) {
        Path resourcePath = null;

        try {
            URL resourceUrl = getClass().getResource(relativeTestfilePath);
            if (resourceUrl == null) {
                throw new IllegalArgumentException("Couldn't find/read file");
            }
            resourcePath = Paths.get(resourceUrl.toURI());
        } catch (URISyntaxException ex) {
            System.out.println("Unable to find testfile with relativePath" + relativeTestfilePath);
        }
        return resourcePath == null ? "" : resourcePath.toString();
    }

}
