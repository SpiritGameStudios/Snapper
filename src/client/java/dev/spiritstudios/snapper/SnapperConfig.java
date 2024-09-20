package dev.spiritstudios.snapper;

import dev.spiritstudios.specter.api.config.Config;
import net.minecraft.util.Identifier;

public class SnapperConfig extends Config<SnapperConfig> {
    public static final SnapperConfig INSTANCE = create(SnapperConfig.class);

    @Override
    public Identifier getId() { return Identifier.of(Snapper.MODID, "snapper"); }

    public Value<Boolean> copyTakenScreenshot = booleanValue(false)
            .comment("Whether to copy screenshots to clipboard when taken.")
            .build();

    public Value<Boolean> gridMode = booleanValue(false)
            .comment("Whether to view screenshots in a grid or not.")
            .build();

    public Value<Integer> gameMenuDisplayPosition = intValue(1)
            .comment("Where to display Snapper button on game menu.")
            .build();
}