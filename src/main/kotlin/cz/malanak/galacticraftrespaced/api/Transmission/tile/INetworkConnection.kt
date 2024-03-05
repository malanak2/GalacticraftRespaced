package cz.malanak.galacticraftrespaced.api.Transmission.tile

import net.minecraft.world.level.block.entity.BlockEntity


/**
 * Applied to TileEntities.
 *
 * @author Calclavia
 */
interface INetworkConnection: IConnector {
    /**
     * Gets a list of all the connected TileEntities that this conductor is
     * connected to. The array's length should be always the 6 adjacent wires.
     *
     * @return
     */
    fun getAdjacentConnections(): Array<BlockEntity?>?

    /**
     * Refreshes the conductor
     */
    fun refresh()

    fun onNetworkChanged()
}