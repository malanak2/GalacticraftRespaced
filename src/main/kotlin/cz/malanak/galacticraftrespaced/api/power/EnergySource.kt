package assets.galacticraftrespaced.api.power

import net.minecraft.core.Direction

interface EnergySource {
    class EnergySourceWireless(nodes: List<ILaserNode>) : EnergySource {
        val nodes: List<ILaserNode> = nodes
    }

    class EnergySourceAdjacent(direction: Direction) : EnergySource {
        val direction: Direction = direction
    }
}