package dev.spiritstudios.snapper;

//#if FABRIC
import net.fabricmc.api.ClientModInitializer;
//#elseif FORGE
//#if MC >= 1.16.5
//$$ import net.minecraftforge.eventbus.api.IEventBus;
//$$ import net.minecraftforge.fml.common.Mod;
//$$ import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
//$$ import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
//#else
//$$ import net.minecraftforge.fml.common.Mod;
//$$ import net.minecraftforge.fml.common.event.FMLInitializationEvent;
//#endif
//#elseif NEOFORGE
//$$ import net.neoforged.bus.api.IEventBus;
//$$ import net.neoforged.fml.common.Mod;
//$$ import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
//#endif

//#if FORGE-LIKE
//#if MC >= 1.16.5
//$$ @Mod(SnapperConstants.ID)
//#else
//$$ @Mod(modid = SnapperConstants.ID, version = SnapperConstants.VERSION)
//#endif
//#endif
public class SnapperEntrypoint
        //#if FABRIC
        implements ClientModInitializer
        //#endif
{

    //#if FORGE && MC >= 1.16.5
    //$$ public SnapperEntrypoint() {
    //$$     setupForgeEvents(FMLJavaModLoadingContext.get().getModEventBus());
    //$$ }
    //#elseif NEOFORGE
    //$$ public SnapperEntrypoint(IEventBus modEventBus) {
    //$$     setupForgeEvents(modEventBus);
    //$$ }
    //#endif

    //#if FABRIC
    @Override
    //#elseif FORGE && MC <= 1.12.2
    //$$ @Mod.EventHandler
    //#endif
    public void onInitializeClient(
            //#if FORGE-LIKE
            //#if MC >= 1.16.5
            //$$ FMLClientSetupEvent event
            //#else
            //$$ FMLInitializationEvent event
            //#endif
            //#endif
    ) {
        //#if FORGE && MC <= 1.12.2
        //$$ if (!event
        //#if MC <= 1.8.9
        //$$ .side.isClient
        //#else
        //$$ .getSide().isClient()
        //#endif
        //$$ ) {
        //$$     return;
        //$$ }
        //#endif

        new Snapper().onInitializeClient();
    }

    //#if FORGE-LIKE && MC >= 1.16.5
    //$$ private void setupForgeEvents(IEventBus modEventBus) {
    //$$     modEventBus.addListener(this::onInitializeClient);
    //$$ }
    //#endif

}