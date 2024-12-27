package code;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class Pathes {

    public static String getMinecraftOrigin(){ //Get Minecraft Directory
        String os = System.getProperty("os.name").toLowerCase();
        String home = System.getProperty("user.home");

        if(os.contains("win")){
            return home + "\\AppData\\Roaming\\.minecraft";
        } else if(os.contains("mac")){
            return home + "/Library/Application Support/minecraft";
        } else {
            return home + "/.minecraft";
        }
    }

    public static String getJarDir(){ //Get program dir 
        try {
            File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return jarFile.getParent();
        } catch (Exception e) {
            e.printStackTrace();
            return System.getProperty("user.dir");
        }

    }

    public static boolean searchNecessaryFolders(String minePath){ //Search for necessary folders to run program
        File saves = new File(minePath, "saves");
        File rp = new File(minePath, "resourcepacks");

        return saves.exists() && rp.exists();
    }

    public static File searchDir(String dirPath, String name){ //Search a dir
        File dir = new File(dirPath);

        if(!dir.exists() || !dir.isDirectory()){
            System.out.println("Diretorio invalido: " + dirPath);
            return null;
        }

        for(File file : Objects.requireNonNull(dir.listFiles())){
            if(file.getName().equals(name) && file.isDirectory() && new File(file, "level.dat").exists()){
                return file;
            }

            if(file.getName().equals(name)){
            if(file.isDirectory() && new File(file, "pack.mcmeta").exists() || file.getName().endsWith(".zip")){
                return file;
            }}
        }
        return null;
    }

    public static File findMap(File dir){ //Search the map in folder
        if(!dir.exists() || !dir.isDirectory()){
            throw new IllegalArgumentException("Diretorio invalido: " + dir.getAbsolutePath());
        }

        for(File file : Objects.requireNonNull(dir.listFiles())){
            if(file.isDirectory()){
                if(new File(file, "level.dat").exists()){
                    return file;
                }
            }
        }
        return null;
    }

    public static File findResource(File dir){ //Search the rp in folder
        if(!dir.exists() || !dir.isDirectory()){
            throw new IllegalArgumentException("Diretorio invalido: " + dir.getAbsolutePath());
        }

        for(File file : Objects.requireNonNull(dir.listFiles())){
            if(file.isFile() && file.getName().toLowerCase().endsWith(".zip")){
                return file;
            }

            if(file.isDirectory() && new File(file, "pack.mcmeta").exists()){
                return file;
            }
        }
        return null;
    }

    public static void copyFileOrFolder(File source, File destination) throws IOException {
        if(source.isDirectory()){
            copyFolder(source.toPath(), destination.toPath().resolve(source.getName()));
        } else if(source.isFile()) {
            Files.copy(source.toPath(), destination.toPath().resolve(source.getName()), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static void copyFolder(Path sourceDir, Path targetDir) throws IOException {
        Files.createDirectories(targetDir);

        Files.walk(sourceDir).forEach(sourcePath ->{
            try {
                Path targetPath = targetDir.resolve(sourceDir.relativize(sourcePath));
                if(Files.isDirectory(sourcePath)){
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
