package dev.spiritstudios.snapper;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.adjuster.tools.AdjustableInjectNode;
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.List;

@Deprecated
// fixes a bug in specter
// specter is already on 1.21.5 and i really dont want to make a new version of specter-
// for an old version of mc. so you get this
// REMOVE THIS WHENEVER WE UPDATE TO 1.21.5
public final class REMOVETHISAFTER1point21point5 implements MixinAnnotationAdjuster {
    @Override
    public AdjustableAnnotationNode adjust(List<String> targetClassNames, String mixinClassName, MethodNode method, AdjustableAnnotationNode annotation) {
        if (!mixinClassName.equals("dev.spiritstudios.specter.mixin.serialization.TranslatableTextContentMixin"))
            return annotation;
        if (!annotation.is(Inject.class)) return annotation;

        AdjustableInjectNode inject = annotation.as(AdjustableInjectNode.class);

        if (!inject.getMethod().contains("visit(Lnet/minecraft/text/StringVisitable$StyledVisitor;Lnet/minecraft/text/Style;)Ljava/util/Optional;") || !inject.getMethod().contains("visit(Lnet/minecraft/text/StringVisitable$Visitor;)Ljava/util/Optional;")) return annotation;

        return inject.withAt(at -> {
            at.getFirst()
                    .withValue(ignored -> "INVOKE")
                    .withTarget(ignored -> "Ljava/util/List;iterator()Ljava/util/Iterator;");

            return at;
        });
    }
}
