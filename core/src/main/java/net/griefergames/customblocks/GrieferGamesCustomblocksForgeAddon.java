package net.griefergames.customblocks;

import net.griefergames.customblocks.downloader.ForgeModDownloader;
import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.addon.lifecycle.GlobalAddonPostEnableEvent;
import net.labymod.api.models.addon.annotation.AddonMain;
import net.labymod.api.notification.Notification;

@AddonMain
public class GrieferGamesCustomblocksForgeAddon extends LabyAddon<GrieferGamesCustomblocksConfiguration> {

  public static final String NAMESPACE = Laby.labyAPI().getNamespace(GrieferGamesCustomblocksForgeAddon.class);
  public static final Icon CUSTOMBLOCKS_ICON = Icon.texture(ResourceLocation.create(NAMESPACE, "textures/icon.png"));

  private ForgeModDownloader downloader;

  private ForgeModDownloader getDownloader() {
    if(downloader == null) {
      downloader = new ForgeModDownloader(this);
    }
    return downloader;
  }

  /**
   * Download the forge mod on enable
   */
  @Override
  protected void enable() {
    this.registerSettingCategory();
    if(!this.configuration().enabled().get()) {
      return;
    }
    logger().info("Downloading ForgeMod for Version: " + this.labyAPI().minecraft().getVersion());
    Notification notification = getDownloader().downloadForgeModVersion(this.labyAPI().minecraft().getVersion());
    if(notification != null) {
      this.labyAPI().notificationController().push(notification);
    }
  }

  @Subscribe
  public void on(GlobalAddonPostEnableEvent event) {
    if(!this.configuration().enabled().get()) {
      return;
    }
    if(this.labyAPI().addonService().getAddon(GrieferGamesCustomblockConstants.LABY_FORGE_ADDON_NAME).isEmpty()) {
      this.labyAPI().notificationController().push(
          Notification.builder()
              .title(Component.translatable("customblocks_forge.missing_labyforge.title"))
              .text(Component.translatable("customblocks_forge.missing_labyforge.content"))
              .icon(CUSTOMBLOCKS_ICON)
              .build()
      );
    }
  }

  @Override
  protected Class<? extends GrieferGamesCustomblocksConfiguration> configurationClass() {
    return GrieferGamesCustomblocksConfiguration.class;
  }

}
