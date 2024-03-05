package cz.malanak.galacticraftrespaced.api.Transmission.grid


/**
 * Implement this in your network class/interface if you plan to have your own network defined by specific conductors and acceptors.
 *
 * @param  <N> - the class/interface Type value in which you implement this
 * @param  <C> - the class/interface Type which makes up the network's conductor Set
 * @param  <A> - the class/interface Type which makes up the network's acceptor Set
 *
 * @author     aidancbrady
 */
interface IGridNetwork<N, C, A> {
    /**
     * Refreshes and cleans up conductor references of this network, as well as updating the acceptor set.
     */
    fun refresh()

    /**
     * Gets the Set of conductors that make up this network.
     *
     * @return conductor set
     */
    fun getTransmitters(): Set<org.checkerframework.checker.units.qual.C?>?

    /**
     * Creates a new network that makes up the current network and the network defined in the parameters. Be sure to refresh the new network inside this method.
     *
     * @param  network network to merge
     *
     * @return         The merged network
     */
    fun merge(network: N): N

    /**
     * Splits a network by the conductor referenced in the parameters. It will then create and refresh the new independent networks possibly created by this operation.
     *
     * @param connection the conductor connection to split the network from
     */
    fun split(connection: org.checkerframework.checker.units.qual.C?)
}