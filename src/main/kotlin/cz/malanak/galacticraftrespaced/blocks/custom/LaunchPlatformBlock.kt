package cz.malanak.galacticraftrespaced.blocks.custom

import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.Property
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape


class LaunchPlatformBlock(props: Properties) : Block(props)  {
    companion object {
        val FORMED = BooleanProperty.create("formed")
        val IS_MAIN = BooleanProperty.create("is_main")
        private var shapesCache: Map<BlockState, VoxelShape>? = null

        // val test = Blocks.DARK_OAK_BUTTON

        val platform1 = Block.box(0.0, 0.0,0.0,16.0,2.0,16.0)
        val platform2 = Block.box(0.0, 0.0,0.0,16.0,4.0,16.0)
        val platform3 = Block.box(0.0, 0.0,0.0,16.0,6.0,16.0)
    }

    var lastClicked: Long = 0

    init {
        registerDefaultState(stateDefinition.any().setValue(FORMED, false).setValue(IS_MAIN, false))
        shapesCache = this.getShapeForEachState { BlockState: BlockState? -> calculateShape(BlockState)
        }
    }

    private fun calculateShape(Blockstate: BlockState?): VoxelShape {
        if (Blockstate?.getValue(FORMED) == false) return platform1
        return when (Blockstate?.getValue(IS_MAIN)) {
            true -> platform2
            false -> platform1
            else -> platform1
        }
    }

    protected fun interact(
        pState: BlockState?,
        pLevel: Level,
        pPos: BlockPos?,
        pPlayer: Player,
        pHand: InteractionHand?,
        hitSide: Direction?,
        hitLocation: Vec3?
    ): InteractionResult {
        if (System.currentTimeMillis() - lastClicked < 1000) return InteractionResult.CONSUME
        lastClicked = System.currentTimeMillis()
        // if (pLevel.isClientSide) return InteractionResult.CONSUME;

        var sameInPlace = 0
        for (i in -1..1)
        {
            for (j in -1..1)
            {
                if (pLevel.getBlockState(pPos?.offset(i, 0, j)!!).block.name.toString() == "translation{key='block.minecraft.air', args=[]}") continue
                if (i == 0 && j == 0) continue
                if (pLevel.getBlockState(pPos.offset(i, 0, j)).block.name.toString() == "translation{key='block.galacticraftrespaced.launch_platform', args=[]}") sameInPlace++
                // pPlayer.sendSystemMessage(Component.literal("Block: " + pLevel.getBlockState(pPos.offset(i, 0, j)).block.name.toString() + "X offset: " + i + ", Z offset: " + j))
                // if (pLevel.getBlockState(pPos.offset(i, j, 0)).block.name == LaunchPlatformBlock)
            }
        }

        if (sameInPlace != 8 && pState?.getValue(FORMED) != true)
        {
            pPlayer.sendSystemMessage(Component.literal("You need to click the middle block to assemble!"))
            return InteractionResult.CONSUME
        }
        this.stateDefinition.any().setValue(FORMED, true)
        for (i in -1..1)
        {
            for (j in -1..1)
            {
                if (pLevel.getBlockState(pPos?.offset(i, 0, j)!!).block.name.toString() == "translation{key='block.minecraft.air', args=[]}") continue
                // pPlayer.sendSystemMessage(Component.literal(pLevel.getBlockState(pPos.offset(i, 0, j)).block.name.toString() + " at " + i + " " + j))
                if (pLevel.getBlockState(pPos.offset(i, 0, j)).block.name.toString() == "translation{key='block.galacticraftrespaced.launch_platform', args=[]}")
                {
                    // pPlayer.sendSystemMessage(Component.literal("Setting formed"))
                    pLevel.setBlock(pPos.offset(i, 0, j), this.stateDefinition.any(), 1)
                    /*if (i == 0 && j == 0)
                        pPlayer.sendSystemMessage(Component.literal("Setting main"))
                        pLevel.getBlockState(pPos.offset(i, 0, j)).setValue(IS_MAIN, true)*/
                }
                // pPlayer.sendSystemMessage(Component.literal("Block: " + pLevel.getBlockState(pPos.offset(i, 0, j)).block.name.toString() + "X offset: " + i + ", Z offset: " + j))
                // if (pLevel.getBlockState(pPos.offset(i, j, 0)).block.name == LaunchPlatformBlock)
            }
        }
        this.stateDefinition.any().setValue(IS_MAIN, true)

        return InteractionResult.SUCCESS
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
                IS_MAIN
            )
        )
    }



    override fun onBlockStateChange(level: LevelReader?, pos: BlockPos?, oldState: BlockState?, newState: BlockState?) {
        super.onBlockStateChange(level, pos, oldState, newState)
    }

    @Deprecated("Deprecated in mc, but I dont care :lmao:")
    override fun getShape(
        pState: BlockState,
        pLevel: BlockGetter,
        pPos: BlockPos,
        pContext: CollisionContext
    ): VoxelShape {
        return shapesCache!![pState]!!
    }

}