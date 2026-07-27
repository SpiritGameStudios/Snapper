package dev.spiritstudios.snapper.gui.screen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class ParentReloaderScreen extends Screen implements ReloadableScreen {
    protected final Screen parent;

    protected ParentReloaderScreen(Component title, Screen parent) {
        super(title);
        this.parent = parent;
    }

    protected boolean shouldReloadParent = false;
    protected boolean shouldRecreateParent = false;

    @Override
    public void reload() {
        this.shouldReloadParent = true;
    }

    @Override
    public void recreateList() {
        this.shouldRecreateParent = true;
    }

    @Override
    public void onClose() {
        if (parent instanceof ReloadableScreen reloadable) {
            if (shouldReloadParent) reloadable.reload();
            if (shouldRecreateParent) reloadable.recreateList();
        }

        this.minecraft.gui.setScreen(this.parent);
    }
}
