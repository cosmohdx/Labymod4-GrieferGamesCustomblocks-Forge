package net.griefergames.customblocks;

import net.labymod.api.addon.AddonConfig;
import net.labymod.api.client.gui.screen.widget.widgets.input.ButtonWidget.ButtonSetting;
import net.labymod.api.client.gui.screen.widget.widgets.input.SwitchWidget.SwitchSetting;
import net.labymod.api.configuration.loader.property.ConfigProperty;
import net.labymod.api.models.OperatingSystem;
import net.labymod.api.util.MethodOrder;

public class GrieferGamesCustomblocksConfiguration extends AddonConfig {

  @SwitchSetting
  private final ConfigProperty<Boolean> enabled = new ConfigProperty<>(true);

  @Override
  public ConfigProperty<Boolean> enabled() {
    return this.enabled;
  }

  @MethodOrder(after = "enabled")
  @ButtonSetting
  public void curseForgeButton() {
    OperatingSystem.getPlatform().openUrl("https://www.curseforge.com/minecraft/mc-mods/mysterymod-customblocks");
  }

}
