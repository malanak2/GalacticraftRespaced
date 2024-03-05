package assets.galacticraftrespaced.api.power

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.Vec3

interface ILaserNode: IEnergyHandlerGC {
    fun getInputPoint(): Vec3?

    fun getOutputPoint(offset: Boolean): Vec3?

    fun getTarget(): ILaserNode?

    fun getTile(): BlockEntity?

    fun canConnectTo(node: ILaserNode?): Boolean

    fun getColor(): Vec3?

    fun addNode(node: ILaserNode?)

    fun removeNode(node: ILaserNode?)

    fun compareTo(otherNode: ILaserNode?, origin: BlockPos?): Int
}