package me.nonplay.ndailyrewards.utils;

import me.nonplay.ndailyrewards.NDailyRewards;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;

public class Files
{
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
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static void mkdir(final File file) {
        try {
            file.mkdir();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void create(final File f) {
        f.getParentFile().mkdirs();
        try {
            f.createNewFile();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static List<String> getFilesFolder(final String folderz) {
        final List<String> names = new ArrayList<String>();
        final File folder = new File(NDailyRewards.instance.getDataFolder() + folderz + "/");
        final File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            return names;
        }
        for (int i = 0; i < listOfFiles.length; ++i) {
            if (listOfFiles[i].isFile()) {
                names.add(listOfFiles[i].getName());
            }
        }
        return names;
    }
}
