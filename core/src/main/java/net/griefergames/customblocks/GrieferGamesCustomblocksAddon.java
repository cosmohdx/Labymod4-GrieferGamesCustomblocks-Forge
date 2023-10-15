package net.griefergames.customblocks;

import net.griefergames.customblocks.downloader.FabricModDownloader;
import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.client.component.Component;
import net.labymod.api.client.gui.icon.Icon;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.addon.lifecycle.AddonPostEnableEvent;
import net.labymod.api.event.addon.lifecycle.GlobalAddonPostEnableEvent;
import net.labymod.api.models.addon.annotation.AddonMain;
import net.labymod.api.notification.Notification;

@AddonMain
public class GrieferGamesCustomblocksAddon extends LabyAddon<GrieferGamesCustomblocksConfiguration> {

  private static final String NAMESPACE = Laby.labyAPI().getNamespace(GrieferGamesCustomblocksAddon.class);
  public static final Icon CUSTOMBLOCKS_ICON = Icon.texture(ResourceLocation.create(NAMESPACE, "textures/icon.png"));

  @Override
  protected void enable() {
    this.registerSettingCategory();
  }

  public void on(AddonPostEnableEvent event) {
    FabricModDownloader downloader = new FabricModDownloader(this);
    logger().info("Downloading FabricMod for Version: " + this.labyAPI().minecraft().getVersion());
    downloader.downloadFabricModVersion(this.labyAPI().minecraft().getVersion());
  }

  @Subscribe
  public void on(GlobalAddonPostEnableEvent event) {
    if(this.labyAPI().addonService().getAddon(GrieferGamesCustomblockConstants.LABY_FABRIC_ADDON_NAME).isEmpty()) {
      this.labyAPI().notificationController().push(
          Notification.builder()
              .title(Component.translatable("customblocks.missing_labyfabric.title"))
              .text(Component.translatable("customblocks.missing_labyfabric.content"))
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
