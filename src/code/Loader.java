package code;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Loader {
    public static Map<String, File> firstLoad(String directory){
        Map<String, File> result = new HashMap<>();

        File dir = new File(directory);
        if(!dir.exists() || !dir.isDirectory()){
            return null;
        }

        File map = null;
        File rp = null;

        for(File file : dir.listFiles()){
            if(file.isDirectory() && new File(file, "level.dat").exists()){
                if(map != null){
                    return null;
                }
                map = file;
            }

            if(file.isDirectory() && new File(file, "pack.mcmeta").exists() || file.isFile() && file.getName().toLowerCase().endsWith(".zip")){
                if(rp != null){
                    return null;
                }
                rp = file;
            }
        }

        if(map != null){
            result.put("map", map);
        }
        if(rp != null){
            result.put("resource", rp);
        }

        return result.size() > 0 ? result : null;
    }

    public static List<String> findMapsAndResources(String directory){
        List<String> maps = new ArrayList<>();
        List<String> resource = new ArrayList<>();

        File dir = new File(directory);
        if(!dir.exists() || !dir.isDirectory()){
            return null;
        }

        for(File file: dir.listFiles()){
            if(file.isDirectory()){
                if(new File(file, "level.dat").exists()){
                    maps.add(file.getName());
                }

                if(new File(file, "pack.mcmeta").exists()){
                    resource.add(file.getName());
                }
            } else if(file.isFile() && file.getName().toLowerCase().endsWith(".zip")){
                resource.add(file.getName());
            }
        }

        List<String> items = new ArrayList<>();
        items.addAll(maps);
        items.addAll(resource);
        return items;
    }
}
