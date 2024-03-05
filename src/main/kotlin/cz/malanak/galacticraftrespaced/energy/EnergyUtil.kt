/*
 * Copyright (c) 2023 Team Galacticraft
 *
 * Licensed under the MIT license.
 * See LICENSE file in the project root for details.
 */
package micdoodle8.mods.galacticraft.core.energy

import cz.malanak.galacticraftrespaced.api.Transmission.NetworkType
import cz.malanak.galacticraftrespaced.api.Transmission.tile.IConductor
import cz.malanak.galacticraftrespaced.api.Transmission.tile.IConnector
import cz.malanak.galacticraftrespaced.api.vector.BlockVec3
import net.minecraft.core.Direction


import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.world.World
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.Capability
import net.neoforged.neoforge.capabilities.ICapabilityProvider
import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.math.floor

object EnergyUtil {

    fun getAdjacentPowerConnections(tile: BlockEntity): Array<BlockEntity?> {
        val adjacentConnections: Array<BlockEntity?> = arrayOfNulls<BlockEntity>(6)

        val thisVec: BlockVec3 = BlockVec3(tile)
        for (direction in Direction.VALUES) {
            if (tile is IConnector && !(tile as IConnector).canConnect(direction, NetworkType.POWER)) {
                continue
            }

            val BlockEntity: BlockEntity = tile.level?.let { thisVec.getBlockEntityOnSide(it, direction) } ?: continue

            if (BlockEntity is IConnector) {
                if ((BlockEntity as IConnector).canConnect(direction.getOpposite(), NetworkType.POWER)) {
                    adjacentConnections[direction.ordinal] = BlockEntity
                }
                continue
            }

            if ((!EnergyConfigHandler.disableMekanismOutput || !EnergyConfigHandler.disableMekanismInput) && isMekLoaded && (BlockEntity is IStrictEnergyAcceptor || BlockEntity is IStrictEnergyOutputter)) {
                // Do not connect GC wires directly to Mek Universal Cables
                try {
                    if (clazzMekCable != null && clazzMekCable!!.isInstance(BlockEntity)) {
                        continue
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (BlockEntity is IStrictEnergyAcceptor && (BlockEntity as IStrictEnergyAcceptor).canReceiveEnergy(
                        direction.getOpposite()
                    )
                ) {
                    adjacentConnections[direction.ordinal()] = BlockEntity
                } else if (BlockEntity is IStrictEnergyOutputter && (BlockEntity as IStrictEnergyOutputter).canOutputEnergy(
                        direction.getOpposite()
                    )
                ) {
                    adjacentConnections[direction.ordinal()] = BlockEntity
                }
                continue
            }

            if ((!EnergyConfigHandler.disableBuildCraftOutput || !EnergyConfigHandler.disableBuildCraftInput) && isBCReallyLoaded) {
                // Do not connect GC wires directly to BC pipes of any type
                try {
                    if (clazzPipeTile != null && clazzPipeTile!!.isInstance(BlockEntity)) {
                        continue
                    }
                } catch (e: Exception) {
                }

                if (hasCapability(BlockEntity, MjAPI.CAP_CONNECTOR, direction.getOpposite()) || hasCapability(
                        BlockEntity,
                        MjAPI.CAP_RECEIVER,
                        direction.getOpposite()
                    )
                    || hasCapability(BlockEntity, MjAPI.CAP_PASSIVE_PROVIDER, direction.getOpposite())
                ) {
                    adjacentConnections[direction.ordinal()] = BlockEntity
                    continue
                }
            }

            if ((!EnergyConfigHandler.disableRFOutput || !EnergyConfigHandler.disableRFInput) && isRFLoaded && BlockEntity is IEnergyConnection) {
                if (isRF2Loaded && (BlockEntity is IEnergyProvider || BlockEntity is IEnergyReceiver) || isRF1Loaded && BlockEntity is IEnergyHandler || clazzRailcraftEngine != null && clazzRailcraftEngine!!.isInstance(
                        BlockEntity
                    )
                ) {
                    if (clazzMFRRednetEnergyCable != null && clazzMFRRednetEnergyCable!!.isInstance(BlockEntity)) {
                        continue
                    }

                    if ((BlockEntity as IEnergyConnection).canConnectEnergy(direction.getOpposite())) {
                        adjacentConnections[direction.ordinal()] = BlockEntity
                    }
                }
                continue
            }

            if ((!EnergyConfigHandler.disableIC2Output || !EnergyConfigHandler.disableIC2Input) && isIC2Loaded && !isIC2ClassicLoaded) {
                if (BlockEntity is IEnergyConductor) {
                    continue
                }

                if (!tile.getWorld().isRemote) {
                    var IC2tile: Any = BlockEntity
                    val checkingIC2 = thisVec.toBlockPos().offset(direction)
                    try {
                        IC2tile = EnergyNet.instance.getSubTile(tile.getWorld(), checkingIC2)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    if (IC2tile is IEnergyAcceptor && tile is IEnergyEmitter) {
                        if ((IC2tile as IEnergyAcceptor).acceptsEnergyFrom(
                                tile as IEnergyEmitter,
                                direction.getOpposite()
                            )
                        ) {
                            adjacentConnections[direction.ordinal()] = BlockEntity
                        }
                        continue
                    }
                    if (IC2tile is IEnergyEmitter && tile is IEnergyAcceptor) {
                        if ((IC2tile as IEnergyEmitter).emitsEnergyTo(
                                tile as IEnergyAcceptor,
                                direction.getOpposite()
                            )
                        ) {
                            adjacentConnections[direction.ordinal()] = BlockEntity
                        }
                        continue
                    }
                } else {
                    try {
                        var clazz: Class<*>? = BlockEntity.getClass()
                        if (clazz!!.name.startsWith("ic2")) {
                            // Special case: IC2's transformers don't seem to
                            // setup their sink and source directions in Energy
                            // clientside
                            if (clazz.name.startsWith("ic2.core.block.wiring.BlockEntityTransformer")) {
                                adjacentConnections[direction.ordinal()] = BlockEntity
                                continue
                            }

                            var energyField: Field? = null
                            fieldLoop@ while (energyField == null && clazz != null) {
                                for (f in clazz.declaredFields) {
                                    if (f.name == "energy") {
                                        energyField = f
                                        break@fieldLoop
                                    }
                                }
                                clazz = clazz.superclass
                            }
                            energyField!!.isAccessible = true
                            val energy = energyField[BlockEntity]
                            var connections: Set<Direction?>
                            if (tile is IEnergyEmitter) {
                                connections =
                                    energy.javaClass.getMethod("getSinkDirs").invoke(energy) as Set<Direction?>
                                if (connections.contains(direction.getOpposite())) {
                                    adjacentConnections[direction.ordinal()] = BlockEntity
                                    continue
                                }
                            }
                            if (tile is IEnergyAcceptor) {
                                connections =
                                    energy.javaClass.getMethod("getSourceDirs").invoke(energy) as Set<Direction?>
                                if (connections.contains(direction.getOpposite())) {
                                    adjacentConnections[direction.ordinal()] = BlockEntity
                                    continue
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (hasCapability(BlockEntity, net.minecraftforge.energy.CapabilityEnergy.ENERGY, direction.getOpposite())) {
                // Do not connect GC wires directly to power conduits
                if (clazzEnderIOCable != null && clazzEnderIOCable!!.isInstance(BlockEntity)) {
                    continue
                }

                val forgeEnergy: IEnergyStorage = getCapability(
                    BlockEntity,
                    net.minecraftforge.energy.CapabilityEnergy.ENERGY,
                    direction.getOpposite()
                )
                if (forgeEnergy != null && (forgeEnergy.canReceive() && !EnergyConfigHandler.disableFEOutput || forgeEnergy.canExtract() && !EnergyConfigHandler.disableFEInput)) {
                    adjacentConnections[direction.ordinal()] = BlockEntity
                }
            }
        }

        return adjacentConnections
    }

    /**
     * Similar to getAdjacentPowerConnections but specific to energy receivers
     * only Adds the adjacent power connections found to the passed acceptors,
     * directions parameter Lists (Note: an acceptor can therefore sometimes be
     * entered in the Lists more than once, with a different direction each
     * time: this would represent GC wires connected to the acceptor on more
     * than one side.)
     *
     * @param conductor
     * @param connectedAcceptors
     * @param directions
     * @throws Exception
     */
    @Throws(Exception::class)
    fun setAdjacentPowerConnections(
        conductor: BlockEntity,
        connectedAcceptors: MutableList<Any?>,
        directions: MutableList<Direction?>
    ) {
        val thisVec: BlockVec3 = BlockVec3(conductor)
        val world: World = conductor.getWorld()
        for (direction in Direction.VALUES) {
            val BlockEntity: BlockEntity = thisVec.getBlockEntityOnSide(world, direction)

            if (BlockEntity == null || BlockEntity is IConductor) // world.getBlockEntity
            // will
            // not
            // have
            // returned
            // an
            // invalid
            // tile,
            // invalid
            // tiles
            // are
            // null
            {
                continue
            }

            val sideFrom: Direction = direction.getOpposite()

            if (BlockEntity is IElectrical) {
                if ((BlockEntity as IElectrical).canConnect(sideFrom, NetworkType.POWER)) {
                    connectedAcceptors.add(BlockEntity)
                    directions.add(sideFrom)
                }
                continue
            }

            if (!EnergyConfigHandler.disableMekanismOutput && isMekLoaded && BlockEntity is IStrictEnergyAcceptor) {
                if (clazzMekCable != null && clazzMekCable!!.isInstance(BlockEntity)) {
                    continue
                }
                if ((BlockEntity as IStrictEnergyAcceptor).canReceiveEnergy(sideFrom)) {
                    connectedAcceptors.add(BlockEntity)
                    directions.add(sideFrom)
                }
                continue
            }

            if (!EnergyConfigHandler.disableIC2Output && isIC2Loaded && !world.isRemote) {
                var IC2tile: IEnergyTile? = null
                val checkingIC2 = thisVec.toBlockPos().offset(direction)
                try {
                    IC2tile = EnergyNet.instance.getSubTile(world, checkingIC2)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (IC2tile is IEnergyConductor) {
                    continue
                }
                if (IC2tile is IEnergyAcceptor) {
                    if ((IC2tile as IEnergyAcceptor).acceptsEnergyFrom(conductor as IEnergyEmitter, sideFrom)) {
                        connectedAcceptors.add(IC2tile)
                        directions.add(sideFrom)
                    }
                    continue
                }
            }

            if (!EnergyConfigHandler.disableBuildCraftOutput && isBCReallyLoaded) {
                if (clazzPipeTile != null && clazzPipeTile!!.isInstance(BlockEntity)) {
                    continue
                }

                if (hasCapability(BlockEntity, MjAPI.CAP_RECEIVER, sideFrom)) {
                    val bcReceiver: IMjReceiver = getCapability(BlockEntity, MjAPI.CAP_RECEIVER, sideFrom)
                    connectedAcceptors.add(bcReceiver)
                    directions.add(sideFrom)
                    continue
                }
            }

            if (!EnergyConfigHandler.disableRFOutput && (isRF2Loaded && BlockEntity is IEnergyReceiver) || (isRF1Loaded && BlockEntity is IEnergyHandler)) {
                if (clazzMFRRednetEnergyCable != null && clazzMFRRednetEnergyCable!!.isInstance(BlockEntity)) {
                    continue
                }

                if ((BlockEntity as IEnergyConnection).canConnectEnergy(sideFrom)) {
                    connectedAcceptors.add(BlockEntity)
                    directions.add(sideFrom)
                }
                continue
            }

            if (!EnergyConfigHandler.disableFEOutput && hasCapability(
                    BlockEntity,
                    net.minecraftforge.energy.CapabilityEnergy.ENERGY,
                    sideFrom
                )
            ) {
                if (clazzEnderIOCable != null && clazzEnderIOCable!!.isInstance(BlockEntity)) {
                    continue
                }

                val forgeEnergy: IEnergyStorage =
                    getCapability(BlockEntity, net.minecraftforge.energy.CapabilityEnergy.ENERGY, sideFrom)
                if (forgeEnergy != null && forgeEnergy.canReceive()) {
                    connectedAcceptors.add(forgeEnergy)
                    directions.add(sideFrom)
                }
            }
        }
        return
    }

    fun otherModsEnergyTransfer(tileAdj: BlockEntity, inputAdj: Direction?, toSend: Float, simulate: Boolean): Float {
        if (isMekLoaded && !EnergyConfigHandler.disableMekanismOutput) {
            var tileMek: IStrictEnergyAcceptor? = null
            if (mekCableAcceptor == null) {
                initialiseMekCapabilities()
            }
            if (tileAdj is IStrictEnergyAcceptor) {
                tileMek = tileAdj as IStrictEnergyAcceptor
            } else if (mekCableAcceptor != null && hasCapability(tileAdj, mekCableAcceptor, inputAdj)) {
                tileMek = getCapability(tileAdj, mekCableAcceptor, inputAdj)
            }

            if (tileMek != null && tileMek.canReceiveEnergy(inputAdj)) {
                val transferredMek =
                    tileMek.acceptEnergy(inputAdj, toSend * EnergyConfigHandler.TO_MEKANISM_RATIO, simulate) as Float
                return transferredMek / EnergyConfigHandler.TO_MEKANISM_RATIO
            }
        } else if (isIC2Loaded && !EnergyConfigHandler.disableIC2Output && tileAdj is IEnergySink) {
            // TODO: need to use new subTile system
            var demanded = 0.0
            try {
                demanded = demandedEnergyIC2!!.invoke(tileAdj) as Double
            } catch (ex: Exception) {
                if (ConfigManagerCore.enableDebug) {
                    ex.printStackTrace()
                }
            }

            if (simulate) {
                return Math.min(toSend, demanded.toFloat() / EnergyConfigHandler.TO_IC2_RATIO)
            }

            val energySendingIC2 = Math.min(toSend * EnergyConfigHandler.TO_IC2_RATIO, demanded)
            if (energySendingIC2 >= 1.0) {
                var result = 0.0
                try {
                    result = if (voltageParameterIC2) {
                        energySendingIC2 - injectEnergyIC2!!.invoke(
                            tileAdj,
                            inputAdj,
                            energySendingIC2,
                            120.0
                        ) as Double
                    } else {
                        energySendingIC2 - injectEnergyIC2!!.invoke(tileAdj, inputAdj, energySendingIC2) as Double
                    }
                } catch (ex: Exception) {
                    if (ConfigManagerCore.enableDebug) {
                        ex.printStackTrace()
                    }
                }
                if (result < 0.0) {
                    return 0f
                }
                return result.toFloat() / EnergyConfigHandler.TO_IC2_RATIO
            }
        } else if (isBCReallyLoaded && !EnergyConfigHandler.disableBuildCraftOutput && hasCapability(
                tileAdj,
                MjAPI.CAP_RECEIVER,
                inputAdj
            )
        ) // MJ API
        {
            val bcReceiver: IMjReceiver = getCapability(tileAdj, MjAPI.CAP_RECEIVER, inputAdj)
            val toSendBC = Math.min((toSend * EnergyConfigHandler.TO_BC_RATIO) as Long, bcReceiver.getPowerRequested())
            val sent: Float = (toSendBC - bcReceiver.receivePower(toSendBC, simulate)) / EnergyConfigHandler.TO_BC_RATIO
            return sent
        } else if (isRF2Loaded && !EnergyConfigHandler.disableRFOutput && tileAdj is IEnergyReceiver) {
            val sent: Float = (tileAdj as IEnergyReceiver).receiveEnergy(
                inputAdj,
                floor(toSend * EnergyConfigHandler.TO_RF_RATIO).toInt(), simulate
            ) / EnergyConfigHandler.TO_RF_RATIO
            //          GalacticraftCore.logger.debug("Beam/storage offering RF2 up to " + toSend + " into pipe, it accepted " + sent);
            return sent
        } else if (!EnergyConfigHandler.disableFEOutput && hasCapability(
                tileAdj,
                net.minecraftforge.energy.CapabilityEnergy.ENERGY,
                inputAdj
            )
        ) {
            val forgeEnergy: IEnergyStorage =
                getCapability(tileAdj, net.minecraftforge.energy.CapabilityEnergy.ENERGY, inputAdj)
            if (forgeEnergy != null && forgeEnergy.canReceive()) {
                val sent: Float = forgeEnergy.receiveEnergy(
                    floor(toSend * EnergyConfigHandler.TO_RF_RATIO)
                        .toInt(), simulate
                ) / EnergyConfigHandler.TO_RF_RATIO
                return sent
            }
        }

        return 0f
    }

    fun otherModsEnergyExtract(tileAdj: BlockEntity, inputAdj: Direction?, toPull: Float, simulate: Boolean): Float {
        if (isIC2Loaded && !EnergyConfigHandler.disableIC2Input && tileAdj is IEnergySource) {
            var offered = 0.0
            try {
                offered = offeredEnergyIC2!!.invoke(tileAdj) as Double
            } catch (ex: Exception) {
                if (ConfigManagerCore.enableDebug) {
                    ex.printStackTrace()
                }
            }

            if (simulate) {
                return Math.min(toPull, offered.toFloat() / EnergyConfigHandler.TO_IC2_RATIO)
            }

            val energySendingIC2 = Math.min(toPull * EnergyConfigHandler.TO_IC2_RATIO, offered)
            if (energySendingIC2 >= 1.0) {
                var resultIC2 = 0.0
                try {
                    resultIC2 = energySendingIC2 - drawEnergyIC2!!.invoke(tileAdj, energySendingIC2) as Double
                } catch (ex: Exception) {
                    if (ConfigManagerCore.enableDebug) {
                        ex.printStackTrace()
                    }
                }
                if (resultIC2 < 0.0) {
                    resultIC2 = 0.0
                }
                return resultIC2.toFloat() / EnergyConfigHandler.TO_IC2_RATIO
            }
        } else if (isBCReallyLoaded && !EnergyConfigHandler.disableBuildCraftInput && hasCapability(
                tileAdj,
                MjAPI.CAP_PASSIVE_PROVIDER,
                inputAdj
            )
        ) {
            val bcEmitter: IMjPassiveProvider = getCapability(tileAdj, MjAPI.CAP_PASSIVE_PROVIDER, inputAdj)
            val toSendBC = (toPull * EnergyConfigHandler.TO_BC_RATIO) as Long
            val sent: Float = bcEmitter.extractPower(toSendBC, toSendBC, simulate) / EnergyConfigHandler.TO_BC_RATIO
            return sent
        } else if (isRF2Loaded && !EnergyConfigHandler.disableRFInput && tileAdj is IEnergyProvider) {
            val sent: Float = (tileAdj as IEnergyProvider).extractEnergy(
                inputAdj,
                floor(toPull * EnergyConfigHandler.TO_RF_RATIO).toInt(), simulate
            ) / EnergyConfigHandler.TO_RF_RATIO
            return sent
        } else if (!EnergyConfigHandler.disableFEInput && hasCapability(
                tileAdj,
                net.minecraftforge.energy.CapabilityEnergy.ENERGY,
                inputAdj
            )
        ) {
            val forgeEnergy: IEnergyStorage =
                getCapability(tileAdj, net.minecraftforge.energy.CapabilityEnergy.ENERGY, inputAdj)
            if (forgeEnergy != null && forgeEnergy.canExtract()) {
                val sent: Float = forgeEnergy.extractEnergy(
                    floor(toPull * EnergyConfigHandler.TO_RF_RATIO)
                        .toInt(), simulate
                ) / EnergyConfigHandler.TO_RF_RATIO
                return sent
            }
        }

        return 0f
    }

    /**
     * Test whether an energy connection can be made to a tile using other mods'
     * energy methods.
     *
     * Parameters:
     *
     * @param tileAdj - the tile under test, it might be an energy tile from
     * another mod
     * @param inputAdj - the energy input side for that tile which is under test
     */
    fun otherModCanReceive(tileAdj: BlockEntity, inputAdj: Direction?): Boolean {
        if (tileAdj is TileBaseConductor || tileAdj is EnergyStorageTile) {
            return false // Do not try using other mods' methods to connect to
            // GC's own tiles
        }

        if (isMekLoaded && tileAdj is IStrictEnergyAcceptor) {
            return (tileAdj as IStrictEnergyAcceptor).canReceiveEnergy(inputAdj)
        } else if (isIC2Loaded && tileAdj is IEnergyAcceptor) {
            return (tileAdj as IEnergyAcceptor).acceptsEnergyFrom(null, inputAdj)
        } else if (isBCReallyLoaded && hasCapability(tileAdj, MjAPI.CAP_RECEIVER, inputAdj)) {
            val bcReceiver: IMjReceiver = getCapability(tileAdj, MjAPI.CAP_RECEIVER, inputAdj)
            return bcReceiver.canReceive()
        } else if (isRF1Loaded && tileAdj is IEnergyHandler || isRF2Loaded && tileAdj is IEnergyReceiver) {
            return (tileAdj as IEnergyConnection).canConnectEnergy(inputAdj)
        } else if (hasCapability(tileAdj, net.minecraftforge.energy.CapabilityEnergy.ENERGY, inputAdj)) {
            return (getCapability(tileAdj, net.minecraftforge.energy.CapabilityEnergy.ENERGY, inputAdj).canReceive())
        }

        return false
    }

    /**
     * Test whether a tile can output energy using other mods' energy methods.
     * Currently restricted to IC2 and RF mods - Mekanism tiles do not provide
     * an interface to "output" energy
     *
     * Parameters:
     *
     * @param tileAdj - the tile under test, it might be an energy tile from
     * another mod
     * @param side - the energy output side for that tile which is under test
     */
    fun otherModCanProduce(tileAdj: BlockEntity, side: Direction?): Boolean {
        if (tileAdj is TileBaseConductor || tileAdj is EnergyStorageTile) {
            return false // Do not try using other mods' methods to connect to
            // GC's own tiles
        }

        if (isIC2Loaded && tileAdj is IEnergyEmitter) {
            return (tileAdj as IEnergyEmitter).emitsEnergyTo(null, side)
        }

        if (isBCReallyLoaded && hasCapability(tileAdj, MjAPI.CAP_PASSIVE_PROVIDER, side)) {
            return true
        }

        if (hasCapability(tileAdj, net.minecraftforge.energy.CapabilityEnergy.ENERGY, side)) {
            return (getCapability(tileAdj, net.minecraftforge.energy.CapabilityEnergy.ENERGY, side).canExtract())
        }

        return false
    }

    fun initialiseIC2Methods(): Boolean {
        // Initialise a couple of non-IC2 classes
        try {
            clazzMekCable = Class.forName("mekanism.common.tile.transmitter.BlockEntityUniversalCable")
        } catch (e: Exception) {
        }
        try {
            clazzEnderIOCable = Class.forName("crazypants.enderio.conduits.conduit.TileConduitBundle")
        } catch (e: Exception) {
        }
        try {
            clazzMFRRednetEnergyCable =
                Class.forName("powercrystals.minefactoryreloaded.tile.rednet.BlockEntityRedNetEnergy")
        } catch (e: Exception) {
        }
        try {
            clazzRailcraftEngine = Class.forName("mods.railcraft.common.blocks.single.TileEngine")
        } catch (e: Exception) {
        }

        clazzPipeTile = CompatibilityManager.classBCTransportPipeTile

        try {
            clazzPipeWood = Class.forName("buildcraft.transport.pipes.PipePowerWood")
        } catch (e: Exception) {
        }

        if (isMekLoaded) {
            try {
                mekCapabilities = Class.forName("mekanism.common.capabilities.Capabilities")
            } catch (ignore: Exception) {
            }
        }

        if (isIC2Loaded) {
            GalacticraftCore.logger.debug("Initialising IC2 methods OK")

            try {
                clazzIC2EnergyTile = Class.forName("ic2.core.energy.Tile")
                if (clazzIC2EnergyTile != null) isIC2TileLoaded = true
            } catch (ignore: Exception) {
            }

            try {
                clazzIC2Cable = Class.forName("ic2.api.energy.tile.IEnergyConductor")
                val clazz = Class.forName("ic2.api.energy.tile.IEnergySink")

                GalacticraftCore.logger.debug("Found IC2 IEnergySink class OK")

                try {
                    demandedEnergyIC2 = clazz.getMethod("getDemandedEnergy")
                } catch (e: Exception) {
                    // if that fails, try legacy version
                    try {
                        demandedEnergyIC2 = clazz.getMethod("demandedEnergyUnits")
                    } catch (ee: Exception) {
                        ee.printStackTrace()
                    }
                }

                GalacticraftCore.logger.debug("Set IC2 demandedEnergy method OK")

                try {
                    injectEnergyIC2 = clazz.getMethod(
                        "injectEnergy",
                        Direction::class.java,
                        Double::class.javaPrimitiveType,
                        Double::class.javaPrimitiveType
                    )
                    voltageParameterIC2 = true
                    GalacticraftCore.logger.debug("Set IC2 injectEnergy method OK")
                } catch (e: Exception) {
                    // if that fails, try legacy version
                    try {
                        injectEnergyIC2 = clazz.getMethod(
                            "injectEnergyUnits",
                            Direction::class.java,
                            Double::class.javaPrimitiveType
                        )
                        GalacticraftCore.logger.debug("IC2 inject 1.7.2 succeeded")
                    } catch (ee: Exception) {
                        ee.printStackTrace()
                    }
                }

                val clazzSource = Class.forName("ic2.api.energy.tile.IEnergySource")
                offeredEnergyIC2 = clazzSource.getMethod("getOfferedEnergy")
                drawEnergyIC2 = clazzSource.getMethod("drawEnergy", Double::class.javaPrimitiveType)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return true
    }

    fun isElectricItem(item: Item?): Boolean {
        if (item is IItemElectric) return true

        if (item == null) return false

        if (EnergyConfigHandler.isRFAPILoaded()) {
            if (item is IEnergyContainerItem) return true
        }
        if (EnergyConfigHandler.isIndustrialCraft2Loaded()) {
            if (item is IElectricItem) return true
            if (item is ISpecialElectricItem) return true
        }
        if (EnergyConfigHandler.isMekanismLoaded()) {
            if (item is IEnergizedItem) return true
        }

        return false
    }

    fun isChargedElectricItem(stack: ItemStack): Boolean {
        if (stack.isEmpty) return false

        val item: Item = stack.item
        if (item is IItemElectric) {
            return (item as IItemElectric).getElectricityStored(stack) > 0
        }

        if (item === Items.AIR) return false

        if (EnergyConfigHandler.isRFAPILoaded()) {
            if (item is IEnergyContainerItem) return (item as IEnergyContainerItem).getEnergyStored(stack) > 0
        }

        if (EnergyConfigHandler.isIndustrialCraft2Loaded()) {
            if (item is ISpecialElectricItem) {
                val electricItem: ISpecialElectricItem = item as ISpecialElectricItem
                return electricItem.getManager(stack)
                    .discharge(stack, Double.POSITIVE_INFINITY, 3, true, true, true) > 0.0
            } else if (item is IElectricItem) {
                val electricItem: IElectricItem = item as IElectricItem
                return electricItem.canProvideEnergy(stack)
            }
        }

        if (EnergyConfigHandler.isMekanismLoaded()) {
            if (item is IEnergizedItem) return (item as IEnergizedItem).getEnergy(stack) > 0
        }

        return false
    }

    fun isFillableElectricItem(stack: ItemStack): Boolean {
        if (stack.isEmpty) return false

        val item: Item = stack.item
        if (item is IItemElectric) {
            return (item as IItemElectric).getElectricityStored(stack) < (item as IItemElectric).getMaxElectricityStored(
                stack
            )
        }

        if (item === Items.AIR) return false

        if (EnergyConfigHandler.isRFAPILoaded()) {
            if (item is IEnergyContainerItem) return (item as IEnergyContainerItem).getEnergyStored(stack) < (item as IEnergyContainerItem).getMaxEnergyStored(
                stack
            )
        }

        if (EnergyConfigHandler.isIndustrialCraft2Loaded()) {
            if (item is ISpecialElectricItem) {
                val electricItem: ISpecialElectricItem = item as ISpecialElectricItem
                return electricItem.getManager(stack).charge(stack, Double.POSITIVE_INFINITY, 3, true, true) > 0.0
            } else if (item is IElectricItem) {
                val electricItem: IElectricItem = item as IElectricItem
                return electricItem.canProvideEnergy(stack)
            }
        }

        if (EnergyConfigHandler.isMekanismLoaded()) {
            if (item is IEnergizedItem) return (item as IEnergizedItem).getEnergy(stack) < (item as IEnergizedItem).getMaxEnergy(
                stack
            )
        }

        return false
    }

    fun hasCapability(provider: ICapabilityProvider<*, *, *>?, capability: Capability<*>?, side: Direction?): Boolean {
        return if ((provider == null || capability == null)) false else provider.hasCapability(capability, side)
    }

    fun <T> getCapability(provider: ICapabilityProvider<*, *, *>?, capability: Capability<T>?, side: Direction?): T? {
        return if ((provider == null || capability == null)) null else provider.getCapability(capability, side)
    }

    fun initialiseMekCapabilities() {
        try {
            fieldCableAcceptor = mekCapabilities!!.getField("ENERGY_ACCEPTOR_CAPABILITY")
            if (fieldCableAcceptor != null) {
                mekCableAcceptor = fieldCableAcceptor!![null] as Capability
            }
            fieldEnergyStorage = mekCapabilities!!.getField("ENERGY_STORAGE_CAPABILITY")
            if (fieldEnergyStorage != null) {
                mekEnergyStorage = fieldEnergyStorage!![null] as Capability
            }
            val gasHandlerCapability = mekCapabilities!!.getField("GAS_HANDLER_CAPABILITY")
            if (gasHandlerCapability != null) {
                mekGasHandler = gasHandlerCapability[null] as Capability
            }
            val gasTubeConnection = mekCapabilities!!.getField("TUBE_CONNECTION_CAPABILITY")
            if (gasTubeConnection != null) {
                mekTubeConnection = gasTubeConnection[null] as Capability
            }
            fieldCableOutput = mekCapabilities!!.getField("ENERGY_OUTPUTTER_CAPABILITY")
            if (fieldCableOutput != null) {
                mekCableOutput = fieldCableOutput!![null] as Capability
            }
        } catch (e: Exception) {
        }
    }

    fun checkMekGasHandler(capability: Capability<*>?): Boolean {
        if (!EnergyConfigHandler.isMekanismLoaded() || capability == null || mekCapabilities == null) {
            return false
        }
        if (mekGasHandler == null) {
            initialiseMekCapabilities()
        }
        return capability === mekGasHandler || capability === mekTubeConnection
    }
}