package space.bxteam.ndailyrewards.utils;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;

public class Files {
    public static void copy(final InputStream inputStream, final File file) {
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(file);
            final byte[] array = new byte[1024];
            int read;
            while ((read = inputStream.read(array)) > 0) {
                fileOutputStream.write(array, 0, read);
            }
            fileOutputStream.close();
            inputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void mkdir(final File file) {
        try {
            file.mkdir(); // Create dir
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
