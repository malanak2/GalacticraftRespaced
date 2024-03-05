package cz.malanak.galacticraftrespaced.api.Transmission.tile

import cz.malanak.galacticraftrespaced.api.Transmission.grid.IElectricityNetwork



interface IConductor {
    /**
     * @return The tier of this conductor - must be 1 or 2
     */
    fun getTierGC(): Int

    /**
     * @return This conductor's electricity network.
     */
    fun getNetwork(): IElectricityNetwork?
}