package dev.spiritstudios.snapper.mixin;

import dev.spiritstudios.snapper.gui.widget.GridListAbstraction;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EntryListWidget.class)
public abstract class EntryListWidgetMixin {

    @Shadow public abstract int getRowWidth();

    @Shadow public abstract int getRowLeft();

    @Shadow protected int headerHeight;

    @Shadow @Final protected int itemHeight;

    @Shadow protected abstract int getEntryCount();

    @Shadow @Final private List<Object> children;

    @Inject(method = "getEntryAtPosition", at = @At("HEAD"), cancellable = true)
    private void snapper$getEntryAtPosition(double x, double y, CallbackInfoReturnable<Object> cir) {
        if (this instanceof GridListAbstraction gridList) {
            if (gridList.showGrid()) {
                ScrollableWidget scrollableWidget = (ScrollableWidget) (Object) this;
                int rowWidth = this.getRowWidth();
                int relX = MathHelper.floor(x - this.getRowLeft());
                int relY = MathHelper.floor(y - (double) scrollableWidget.getY()) - this.headerHeight;

                if (relX < 0 || relX > rowWidth || relY < 0 || relY > scrollableWidget.getBottom()) cir.setReturnValue(null);

                int rowIndex = (relY + (int) scrollableWidget.getScrollY()) / this.itemHeight;
                int colIndex = MathHelper.floor(((float) relX / (float) rowWidth) * (float) gridList.getColumnCount());
                int entryIndex = rowIndex * gridList.getColumnCount() + colIndex;

                cir.setReturnValue(entryIndex >= 0 && entryIndex < getEntryCount() ? this.children.get(entryIndex) : null);
            }
        }
    }
}
