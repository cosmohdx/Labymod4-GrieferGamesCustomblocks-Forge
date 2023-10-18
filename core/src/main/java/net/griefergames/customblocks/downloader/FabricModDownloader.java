package net.griefergames.customblocks.downloader;

import com.google.gson.Gson;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.Optional;
import net.griefergames.customblocks.GrieferGamesCustomblockConstants;
import net.griefergames.customblocks.GrieferGamesCustomblocksAddon;
import net.labymod.api.Laby;
import net.labymod.api.addon.LoadedAddon;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.modloader.ModLoaderId;
import net.labymod.api.modloader.ModLoaderRegistry;
import net.labymod.api.modloader.mod.ModInfo;
import net.labymod.api.notification.Notification;
import net.labymod.api.util.version.SemanticVersion;

/**
 * Downloader of the FabricMod from Resources of the jar
 */
public class FabricModDownloader {

  private final GrieferGamesCustomblocksAddon addon;

  public FabricModDownloader(GrieferGamesCustomblocksAddon addon) {
    this.addon = addon;
  }

  /**
   * Returns the path for the given path
   *
   * @param path path without assets/NAMESPACE/
   * @return path with assets/NAMESPACE/
   */
  private static String inPath(String path) {
    return "assets/"+GrieferGamesCustomblocksAddon.NAMESPACE+"/"+path;
  }

  /**
   * Returns the resource as Stream
   * @param path path without assets/NAMESPACE/
   * @return InputStream
   */
  private InputStream getResourceAsStream(String path) throws IOException {
    ResourceLocation location = ResourceLocation.create(GrieferGamesCustomblocksAddon.NAMESPACE, path);
    if(location.getClass().getSimpleName().equalsIgnoreCase("PathResourceLocation")) {
      return location.openStream();
    }
    String inPathString = inPath(path);
    Optional<LoadedAddon> optionalAddon = Laby.labyAPI().addonService().getOptionalAddon(GrieferGamesCustomblocksAddon.NAMESPACE);
    if(!optionalAddon.isPresent()) {
      return null;
    }
    URL resource = optionalAddon.get().getClassLoader().getResource(inPathString);
    if(resource != null) {
      return resource.openStream();
    }
    resource = this.getClass().getClassLoader().getResource(inPathString);
    if(resource != null) {
      return resource.openStream();
    }
    return null;
  }

  /**
   * Downloads the FarbicMod for the given Version
   *
   * @param version Version
   * @return Notification if there is smth to display for the user
   */
  public Notification downloadFabricModVersion(String version) {
    checkFabricPaths(version);

    VersionData versionData = getVersionData();
    if(versionData == null) {
      this.addon.logger().error("Failed to load version_data.json. Try update the LabyMod Addon.");
      return Notification.builder()
          .title(Component.translatable("customblocks.fabricmod_update_broken.title"))
          .text(Component.translatable("customblocks.fabricmod_update_broken.content"))
          .icon(GrieferGamesCustomblocksAddon.CUSTOMBLOCKS_ICON)
          .build();
    }

    if(!versionData.versions.containsKey(version)) {
      this.addon.logger().info("No Fabric Mod for Minecraft "+version+" found");
      return null;
    }
    if(checkModVersionExists(version, versionData.versions.get(version), versionData.fileName)) {
      this.addon.logger().info("Fabric Mod for Minecraft "+version+" is already up to date");
      return null;
    }
    removeOlderVersions(version, versionData.versions.get(version), versionData.fileName);
    return downloadFabricMod(version, versionData.versions.get(version), versionData.fileName);
  }

  /**
   * Loads the VersionData from the version_data.json
   * @return VersionData
   */
  public VersionData getVersionData() {
    VersionData versionData = null;
    Gson gson = new Gson();
    try(InputStream data = getResourceAsStream("fabric-mod/version_data.json")){
      return gson.fromJson(new InputStreamReader(data), VersionData.class);
    }catch (Exception ex) {
      return null;
    }
  }

  /**
   * Checks if the farbic mod path is existing, if not it creates it
   * @param version The version to check
   */
  private void checkFabricPaths(String version) {
    try {
      // Create Farbic Mods part if its not existing
      Files.createDirectories(
          GrieferGamesCustomblockConstants.versionedPath(GrieferGamesCustomblockConstants.MODS_DIRECTORY_PATH, version)
          , (FileAttribute<?>[])new FileAttribute[0]);
    } catch (IOException exception) {
      throw new RuntimeException("Failed to create versioned mod directories", exception);
    }
  }

  /**
   * Checks if the Fabric Mod is already downloaded
   *
   * @param mcVersion Minecraft Version
   * @param farbicVersion Required Fabric Mod Version
   * @param fileName File Name of the Fabric Mod
   * @return true if the Fabric Mod is already downloaded
   */
  private boolean checkModVersionExists(String mcVersion, String farbicVersion, String fileName) {
    ModInfo customBlocksMod = ModLoaderRegistry.instance().getById(ModLoaderId.FABRIC).getModInfo("mysterymod_customblocks");
    if(customBlocksMod == null) {
      return false;
    }
    return !customBlocksMod.version().isLowerThan(new SemanticVersion(farbicVersion));
  }

  /**
   * Removes older versions of the Fabric Mod
   *
   * @param mcVersion Minecraft Version
   * @param farbicVersion Required Fabric Mod Version
   * @param fileName File Name of the Fabric Mod
   */
  private void removeOlderVersions(String mcVersion, String farbicVersion, String fileName) {
    //@TODO Change to ModLoaderRegistry when its possible within api
    File modsDirectory = GrieferGamesCustomblockConstants.versionedPath(GrieferGamesCustomblockConstants.MODS_DIRECTORY_PATH, mcVersion).toFile();
    File[] files = modsDirectory.listFiles();
    for(File file : files) {
      if(file.getName().startsWith(fileName.replace("{minecraftVersion}", mcVersion).replace("{version}", ""))) {
        file.delete();
      }
    }
  }

  /**
   * Downloads the Fabric mod
   *
   * @param mcVersion Minecraft Version
   * @param farbicVersion Required Fabric Mod Version
   * @param fileName File Name of the Fabric Mod
   * @return Notification if the download was successful
   */
  private Notification downloadFabricMod(String mcVersion, String farbicVersion, String fileName) {
    File DOWNLOAD_AS_FILE = new File(
    GrieferGamesCustomblockConstants.versionedPath(GrieferGamesCustomblockConstants.MODS_DIRECTORY_PATH, mcVersion)
        .toFile(), fileName.replace("{minecraftVersion}", mcVersion).replace("{version}", farbicVersion)
    );
    boolean success = false;
    try (BufferedInputStream in = new BufferedInputStream(getResourceAsStream("fabric-mod/"+mcVersion+".jar"))) {
      FileOutputStream fileOutputStream = new FileOutputStream(DOWNLOAD_AS_FILE);
      byte[] dataBuffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
        fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
      this.addon.logger().info("Updated Fabric Mod to version "+farbicVersion+" for Minecraft "+mcVersion);
      success = true;
    } catch (Exception e) {
      // handle exception
      e.printStackTrace();
    }
    if(success) {
      return Notification.builder()
          .title(Component.translatable("customblocks.fabricmod_updated.title"))
          .text(Component.translatable("customblocks.fabricmod_updated.content"))
          .icon(GrieferGamesCustomblocksAddon.CUSTOMBLOCKS_ICON)
          .build();
    }else{
      return Notification.builder()
          .title(Component.translatable("customblocks.fabricmod_update_error.title"))
          .text(Component.translatable("customblocks.fabricmod_update_error.content"))
          .icon(GrieferGamesCustomblocksAddon.CUSTOMBLOCKS_ICON)
          .build();
    }
  }

}
