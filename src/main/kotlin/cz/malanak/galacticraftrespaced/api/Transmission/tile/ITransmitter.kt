package cz.malanak.galacticraftrespaced.api.Transmission.tile

import cz.malanak.galacticraftrespaced.api.Transmission.NetworkType



interface ITransmitter: INetworkProvider, INetworkConnection {
    override fun getNetworkType(): NetworkType?

    override fun canTransmit(): Boolean
}