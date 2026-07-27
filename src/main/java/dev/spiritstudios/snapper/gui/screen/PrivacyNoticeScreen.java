/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

/*
 * This file is adapted from AxolotlClient.
 * https://github.com/AxolotlClient/AxolotlClient-mod/blob/b2fe6c3f1b96b87bf1d8a50966def994ca18c9a9/1.21/src/main/java/io/github/axolotlclient/api/PrivacyNoticeScreen.java
 * ~ moehreag
 */

package dev.spiritstudios.snapper.gui.screen;

import dev.spiritstudios.snapper.SnapperConfig;
import dev.spiritstudios.snapper.util.uploading.AxolotlClientApi;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.util.function.Consumer;

public class PrivacyNoticeScreen extends Screen {
    private static final URI TERMS_URI = URI.create("https://axolotlclient.com/terms");

    private final Screen parent;
    private final Runnable callback;
    private MultiLineLabel message;

    public PrivacyNoticeScreen(Screen parent, Runnable callback) {
        super(Component.translatable("snapper.privacy_notice"));
        this.parent = parent;
        this.callback = callback;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.centeredText(this.font, this.title, this.width / 2, getTitleY(), CommonColors.WHITE);

        message.visitLines(
                TextAlignment.CENTER,
                width / 2, getMessageY(),
                10,
                graphics.textRenderer()
        );
    }

    @Override
    public @NonNull Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(),
                Component.translatable("snapper.privacy_notice.description"));
    }

    @Override
    protected void init() {
        message = MultiLineLabel.create(Minecraft.getInstance().font,
                Component.translatable("snapper.privacy_notice.description"), width - 50);

        int y = Mth.clamp(this.getMessageY() + this.getMessagesHeight() + 20, this.height / 6 + 96, this.height - 24);

        this.addButtons(y);
    }

    private void addButtons(int y) {
        int buttonWidth = 120;

        addRenderableWidget(Button.builder(Component.translatable("snapper.privacy_notice.view_terms"), _ ->
                Util.getPlatform().openUri(TERMS_URI)).bounds(width / 2 - (buttonWidth / 2) - buttonWidth - 5, y, buttonWidth, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("snapper.privacy_notice.accept"), _ -> {
            SnapperConfig.editAsync(m -> m.termsAccepted = AxolotlClientApi.TermsAcceptance.ACCEPTED);
            this.onClose();
        }).bounds(width / 2 - (buttonWidth / 2), y, buttonWidth, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("snapper.privacy_notice.deny"), _ -> {
            SnapperConfig.editAsync(m -> m.termsAccepted = AxolotlClientApi.TermsAcceptance.DENIED);
            this.onClose();
        }).bounds(width / 2 - (buttonWidth / 2) + buttonWidth + 5, y, buttonWidth, 20).build());
    }


    @Override
    public void onClose() {
        callback.run();
        this.minecraft.gui.setScreen(parent);
    }

    private int getTitleY() {
        int i = (this.height - this.getMessagesHeight()) / 2;
        return Mth.clamp(i - 20 - 9, 10, 80);
    }

    private int getMessageY() {
        return this.getTitleY() + 20;
    }

    private int getMessagesHeight() {
        return this.message.getLineCount() * 9;
    }
}
