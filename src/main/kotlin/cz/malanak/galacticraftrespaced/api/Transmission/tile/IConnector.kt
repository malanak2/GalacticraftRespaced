package cz.malanak.galacticraftrespaced.api.Transmission.tile

import cz.malanak.galacticraftrespaced.api.Transmission.NetworkType
import net.minecraft.core.Direction


/**
 * Applied to TileEntities that can connect to an electrical OR oxygen network.
 *
 * @author Calclavia, micdoodle8
 */
interface IConnector {
    /**
     * @return If the connection is possible.
     */
    fun canConnect(direction: Direction?, type: NetworkType?): Boolean
}