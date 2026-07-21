package dev.spiritstudios.snapper.gui.widget.config;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

public class PanoramaSizeSliders {
    public final LinearLayout layout;

    public PanoramaSizeSliders(int spacing) {
        this.layout = LinearLayout.vertical().spacing(spacing);

    }
}
