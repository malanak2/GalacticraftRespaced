package assets.galacticraftrespaced.api.power

interface IEnergyStorageGC {
    /**
     * Add energy to the storage.
     *
     * @param amount Maximum amount of energy to receive
     * @param simulate If true, the transfer will only be simulated.
     * @return The amount of energy that was successfully received (or would
     * have been, if simulated).
     */
    fun receiveEnergyGC(amount: Float, simulate: Boolean): Float

    /**
     * Remove energy from the storage.
     *
     * @param amount Maximum amount of energy to extract
     * @param simulate If true, the transfer will only be simulated.
     * @return The amount of energy that was successfully extracted (or would
     * have been, if simulated).
     */
    fun extractEnergyGC(amount: Float, simulate: Boolean): Float

    /**
     * Returns the amount of energy stored
     */
    fun getEnergyStoredGC(): Float

    /**
     * Returns the maximum amount of energy stored
     */
    fun getCapacityGC(): Float
}