package assets.galacticraftrespaced.api.power
// From https://github.com/TeamGalacticraft/Galacticraft-Legacy/blob/master-1.12/src/main/java/micdoodle8/mods/galacticraft/api/power/IEnergyHandlerGC.java
interface IEnergyHandlerGC {
    /**
     * Add energy from an external source
     *
     * @param from Energy Source that is providing power
     * @param amount Maximum amount of energy to receive
     * @param simulate If true, the transfer will only be simulated.
     * @return The amount of energy that was successfully received (or would
     * have been, if simulated).
     */
    fun receiveEnergyGC(from: EnergySource?, amount: Float, simulate: Boolean): Float

    /**
     * Remove energy, transferring it to an external source
     *
     * @param from Energy Source that is extracting power
     * @param amount Maximum amount of energy to extract
     * @param simulate If true, the transfer will only be simulated.
     * @return The amount of energy that was successfully extracted (or would
     * have been, if simulated).
     */
    fun extractEnergyGC(from: EnergySource?, amount: Float, simulate: Boolean): Float

    /**
     * Returns true if the handler can interface with the provided energy source
     */
    fun nodeAvailable(from: EnergySource?): Boolean

    /**
     * Returns the amount of energy stored in this handler available to the
     * provided source
     */
    fun getEnergyStoredGC(from: EnergySource?): Float

    /**
     * Returns the maximum amount of energy stored in this handler available to
     * the provided source
     */
    fun getMaxEnergyStoredGC(from: EnergySource?): Float
}