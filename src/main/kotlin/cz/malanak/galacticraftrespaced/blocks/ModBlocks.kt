package cz.malanak.galacticraftrespaced.blocks

import cz.malanak.galacticraftrespaced.GalacticraftRespaced
import cz.malanak.galacticraftrespaced.blocks.custom.LaunchPlatformBlock
import cz.malanak.galacticraftrespaced.items.ModItems.Companion.ITEMS
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister
import java.util.function.Supplier

class ModBlocks {
    companion object {
        public val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(GalacticraftRespaced.MODID)
        val LAUNCH_PLATFORM_BLOCK: DeferredBlock<LaunchPlatformBlock> = BLOCKS.register<LaunchPlatformBlock>(
                "launch_platform",
                Supplier<LaunchPlatformBlock> {
                    LaunchPlatformBlock(BlockBehaviour.Properties.of()
                            .destroyTime(2.0f)
                            .explosionResistance(10.0f)
                            .sound(SoundType.GRAVEL)
                            .lightLevel {state: BlockState? -> 7}
                    )
                }

        )
        val EXAMPLE_BLOCK_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("launch_platform", LAUNCH_PLATFORM_BLOCK)
    }

}