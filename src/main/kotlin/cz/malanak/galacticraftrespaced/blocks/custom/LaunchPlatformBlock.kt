package cz.malanak.galacticraftrespaced.blocks.custom

import com.google.common.collect.ImmutableMap
import cz.malanak.galacticraftrespaced.GalacticraftRespaced
import net.minecraft.core.BlockPos
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.BigDripleafBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.block.state.properties.Property
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.function.Function

class LaunchPlatformBlock(props: Properties) : Block(props)  {
    companion object {
        val FORMED = BooleanProperty.create("formed")
        val LEVEL = IntegerProperty.create("level", 1, 3)
        val test = Blocks.BIG_DRIPLEAF
        private var shapesCache: Map<BlockState, VoxelShape>? = null

        val platform1 = Block.box(0.0, 0.0,0.0,16.0,2.0,16.0)
        val platform2 = Block.box(0.0, 0.0,0.0,16.0,3.0,16.0)
        val platform3 = Block.box(0.0, 0.0,0.0,16.0,4.0,16.0)
    }

    init {
        registerDefaultState(stateDefinition.any().setValue(FORMED, false).setValue(LEVEL, 1))
        shapesCache = this.getShapeForEachState { BlockState: BlockState? -> calculateShape(BlockState)
        }
    }

    private fun calculateShape(Blockstate: BlockState?): VoxelShape {
        if (Blockstate?.getValue(FORMED) == false) return platform1
        return when (Blockstate?.getValue(LEVEL)) {
            1-> platform1
            2-> platform2
            3-> platform3
            else -> platform1
        }
    }
    override fun getStateForPlacement(pContext: BlockPlaceContext): BlockState? {
        return super.getStateForPlacement(pContext)
    }

    override fun onNeighborChange(state: BlockState?, level: LevelReader?, pos: BlockPos?, neighbor: BlockPos?) {
        super.onNeighborChange(state, level, pos, neighbor)
    }

    override fun createBlockStateDefinition(pBuilder: StateDefinition.Builder<Block?, BlockState?>) {
        pBuilder.add(
            *arrayOf<Property<*>>(
                FORMED,
                LEVEL
            )
        )
    }

    override fun onBlockStateChange(level: LevelReader?, pos: BlockPos?, oldState: BlockState?, newState: BlockState?) {
        if (level?.isClientSide == true)
        {
            GalacticraftRespaced.LOGGER.info("Blockstate changed!");
        }

        super.onBlockStateChange(level, pos, oldState, newState)
    }

    @Deprecated("Deprecated in mc, but I dont care :lmao:")
    override fun getShape(
        pState: BlockState,
        pLevel: BlockGetter,
        pPos: BlockPos,
        pContext: CollisionContext
    ): VoxelShape {
        return shapesCache!!.get(pState)!!
    }

}