package dev.spiritstudios.snapper.gui.widget;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;

import java.util.function.Consumer;

public class DeadSpaceElement implements LayoutElement {
    private final int width;
    private final int height;

    private int x = 0;
    private int y = 0;

    public DeadSpaceElement(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {

    }
}
