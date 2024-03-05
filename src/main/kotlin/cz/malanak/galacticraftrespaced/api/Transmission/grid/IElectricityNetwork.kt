package cz.malanak.galacticraftrespaced.api.Transmission.grid

import cz.malanak.galacticraftrespaced.api.Transmission.tile.IConductor
import net.minecraft.world.level.block.entity.BlockEntity


/**
 * The Electrical Network in interface form.
 *
 * @author Calclavia
 */
interface IElectricityNetwork: IGridNetwork<IElectricityNetwork, IConductor, BlockEntity> {

}