package net.griefergames.customblocks;

import net.labymod.api.Constants;
import net.labymod.api.loader.platform.PlatformEnvironment;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GrieferGamesCustomblockConstants {

  public static final String LABY_FABRIC_ADDON_NAME = "labyfabric";

  public static final Path FORGE_DIRECTORY = Constants.Files.LABYMOD_DIRECTORY.resolve("forge");
  public static final String FABRIC_VERSION_PATH = FORGE_DIRECTORY + "/%s";
  public static final String MODS_DIRECTORY_PATH = FABRIC_VERSION_PATH + "/mods";

  public static Path versionedPath(String path) {
    return versionedPath(path, PlatformEnvironment.getRunningVersion());
  }

  public static Path versionedPath(String path, String version) {
    return Paths.get(String.format(path, new Object[] { version }), new String[0]);
  }

}
