package cz.malanak.galacticraftrespaced.api.Transmission.tile

import cz.malanak.galacticraftrespaced.api.Transmission.NetworkType



interface INetworkProvider {
    fun getNetworkType(): NetworkType?

    fun canTransmit(): Boolean
}