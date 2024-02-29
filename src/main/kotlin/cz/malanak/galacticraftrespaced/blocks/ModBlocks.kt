package cz.malanak.galacticraftrespaced.blocks

import cz.malanak.galacticraftrespaced.GalacticraftRespaced
import cz.malanak.galacticraftrespaced.items.ModItems
import cz.malanak.galacticraftrespaced.items.ModItems.Companion.ITEMS
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.material.MapColor
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

class ModBlocks {
    companion object {
        public val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(GalacticraftRespaced.MODID)
        val EXAMPLE_BLOCK: DeferredBlock<Block> = BLOCKS.registerSimpleBlock(
                "example_block",
                BlockBehaviour.Properties.of().mapColor(MapColor.STONE)

        )
        val EXAMPLE_BLOCK_ITEM: DeferredItem<BlockItem> = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK)
    }

}