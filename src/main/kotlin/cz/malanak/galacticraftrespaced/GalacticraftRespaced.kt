package cz.malanak.galacticraftrespaced

import com.mojang.logging.LogUtils
import cz.malanak.galacticraftrespaced.blocks.ModBlocks
import cz.malanak.galacticraftrespaced.items.ModItems.Companion.EXAMPLE_ITEM
import cz.malanak.galacticraftrespaced.items.ModItems.Companion.ITEMS
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.language.LanguageInfo
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.data.DataGenerator
import net.minecraft.data.PackOutput
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Blocks
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.ModLoadingContext
import net.neoforged.fml.common.Mod
import net.neoforged.fml.common.Mod.EventBusSubscriber
import net.neoforged.fml.config.ModConfig
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.common.data.LanguageProvider
import net.neoforged.neoforge.data.event.GatherDataEvent
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion.MOD_ID
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister


// The value here should match an entry in the META-INF/mods.toml file
@Mod(GalacticraftRespaced.MODID)
class GalacticraftRespaced(modEventBus: IEventBus) {
    companion object {
        // Define mod id in a common place for everything to reference
        const val MODID = "galacticraftrespaced"

        // Directly reference a slf4j logger
        val LOGGER = LogUtils.getLogger()

        // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace




        // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace


        // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
        val CREATIVE_MODE_TABS: DeferredRegister<CreativeModeTab> =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID)
        val TAB_BUILDER = CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.$MODID"))
                .withTabsBefore(CreativeModeTabs.COMBAT)
                .icon { EXAMPLE_ITEM.get().defaultInstance }
                .displayItems { _: ItemDisplayParameters?, output: CreativeModeTab.Output ->
                    output.accept(EXAMPLE_ITEM.get())
                    output.accept(ModBlocks.EXAMPLE_BLOCK_ITEM.get())
                };
        val ITEM_TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> = CREATIVE_MODE_TABS.register("galacticraftrespaced_tab") { ->TAB_BUILDER.build() }

    }

    init {
        modEventBus.addListener(this::commonSetup)
        ModBlocks.BLOCKS.register(modEventBus)
        ITEMS.register(modEventBus)
        CREATIVE_MODE_TABS.register(modEventBus)
        NeoForge.EVENT_BUS.register(this)
        // modEventBus.addListener(this::addCreative)
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC)

        modEventBus.addListener(DataGen::onGatherData)
    }

    private fun commonSetup(event: FMLCommonSetupEvent) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP")

        if (Config.logDirtBlock) LOGGER.info(
            "DIRT BLOCK >> {}",
            BuiltInRegistries.BLOCK.getKey(Blocks.DIRT)
        )

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber)

        Config.items.forEach { item: Item ->
            LOGGER.info(
                "ITEM >> {}",
                item.toString()
            )
        }
    }

    // Add the example block item to the building blocks tab
    private fun addCreative(event: BuildCreativeModeTabContentsEvent) {
        // if (event.tabKey === CreativeModeTabs.BUILDING_BLOCKS) event.accept(EXAMPLE_BLOCK_ITEM)
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting")
    }



    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
    object ClientModEvents {
        @SubscribeEvent
        fun onClientSetup(event: FMLClientSetupEvent) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP")
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().user.name)
        }
    }


}