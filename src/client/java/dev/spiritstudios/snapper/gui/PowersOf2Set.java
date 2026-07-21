package dev.spiritstudios.snapper.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.client.OptionInstance;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

import java.util.Optional;

public class PowersOf2Set implements OptionInstance.SliderableValueSet<Integer> {
    private final int minInclusive;
    private final int maxInclusive;

    private final int minLog2;
    private final int maxLog2;

    public PowersOf2Set(int minInclusive, int maxInclusive) {
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;

        this.minLog2 = Mth.log2(minInclusive);
        this.maxLog2 = Mth.log2(maxInclusive);
    }

    @Override
    public Optional<Integer> next(Integer current) {
        return Optional.of(Mth.clamp(current << 1, minInclusive, maxInclusive));
    }

    @Override
    public Optional<Integer> previous(Integer current) {
        return Optional.of(Mth.clamp(current >> 1, minInclusive, maxInclusive));
    }

    @Override
    public double toSliderValue(Integer value) {
        if (value == this.maxInclusive) return 1.0;
        if (value == this.minInclusive) return 0.0;

        return Mth.inverseLerp(Mth.log2(value) + 0.5, minLog2, maxLog2 + 1.0);
    }

    @Override
    public Integer fromSliderValue(double slider) {
        if (slider > 0.0) slider = Math.max(slider - Mth.EPSILON, 0.0);

        return 1 << Mth.floor(Mth.lerp(slider, minLog2, maxLog2 + 1.0));
    }

    @Override
    public Optional<Integer> validateValue(Integer value) {
        return value.compareTo(this.minInclusive) >= 0 &&
                value.compareTo(this.maxInclusive) <= 0 &&
                Mth.isPowerOfTwo(value) ?
                Optional.of(value) :
                Optional.empty();
    }

    @Override
    public Codec<Integer> codec() {
        return ExtraCodecs.intRange(minInclusive, maxInclusive).validate(value -> !Mth.isPowerOfTwo(value) ?
                DataResult.error(() -> "Expected a power of 2. Instead found: " + value) :
                DataResult.success(value));
    }
}
