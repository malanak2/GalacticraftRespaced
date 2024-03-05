package cz.malanak.galacticraftrespaced.api.vector

import net.minecraft.CrashReport
import net.minecraft.CrashReportCategory
import net.minecraft.ReportedException
import net.minecraft.commands.arguments.NbtTagArgument
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.data.structures.NbtToSnbt
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import kotlin.math.floor
import kotlin.math.sqrt
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Mth

import net.minecraft.BlockUtil

import net.minecraft.world.level.chunk.LevelChunk
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProviders
import net.minecraft.world.phys.Vec3
import javax.annotation.Nullable

/* BlockVec3 is similar to galacticraft.api.vector.Vector3?
 *
 * But for speed it uses integer arithmetic not doubles, for block coordinates
 * This reduces unnecessary type conversion between integers and doubles and back again.
 * (Minecraft block coordinates are always integers, only entity coordinates are doubles.)
 *
 */
class BlockVec3(): Cloneable{
    var x: Int = 0
    var y: Int = 0
    var z: Int = 0
    var sideDoneBits: Int = 0
    companion object {
        private var chunkCached: LevelChunk? = null
        lateinit var chunkCacheDim: ResourceKey<Level>
        private var chunkCacheX = 1876000 // outside the world edge
        private var chunkCacheZ = 1876000 // outside the world edge
        private var chunkCached_Client: LevelChunk? = null
        lateinit var chunkCacheDim_Client: ResourceKey<Level>
        private var chunkCacheX_Client = 1876000 // outside the world edge
        private var chunkCacheZ_Client = 1876000 // outside the world edge
    }


    // INVALID_VECTOR is used in cases where a null vector cannot be used
    val INVALID_VECTOR: BlockVec3 = BlockVec3(-1, -1, -1)

    init {
        this.x = 0
        this.y = 0
        this.z = 0
    }

    constructor(pos: BlockPos) : this() {
        BlockVec3(pos.x, pos.y, pos.z)
    }

    constructor(x: Int, y: Int, z: Int) : this()
    {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor(par1: Entity) : this()
    {
        this.x = floor(par1.x).toInt()
        this.y = floor(par1.y).toInt()
        this.z = floor(par1.z).toInt()
    }

    constructor(par1: BlockEntity) : this()
    {
        this.x = par1.blockPos.x;
        this.y = par1.blockPos.y
        this.z = par1.blockPos.z
    }

    /**
     * Makes a new copy of this Vector. Prevents variable referencing problems.
     */
    override fun clone(): BlockVec3 {
        return BlockVec3(this.x, this.y, this.z)
    }

    fun toBlockPos(): BlockPos {
        return BlockPos(this.x, this.y, this.z)
    }

    /**
     * Get block ID at the BlockVec3 coordinates, with a forced chunk load if
     * the coordinates are unloaded.
     *
     * @param world
     * @return the block ID, or null if the y-coordinate is less than 0 or
     * greater than 256 or the x or z is outside the Minecraft worldmap.
     */
    fun getBlockState(world: Level): BlockState? {
        if (this.y < 0 || (this.y >= 256) || (this.x < -30000000) || (this.z < -30000000) || (this.x >= 30000000) || (this.z >= 30000000)) {
            return null
        }

        val chunkx = this.x shr 4
        val chunkz = this.z shr 4
        try {
            if (!world.isClientSide) {
                if (BlockVec3.chunkCacheX_Client == chunkx && BlockVec3.chunkCacheZ_Client == chunkz && BlockVec3.chunkCacheDim_Client == world.dimension() && BlockVec3.chunkCached_Client?.level?.isLoaded(this.toBlockPos()) == true) {
                    return BlockVec3.chunkCached_Client!!.getBlockState(BlockPos(this.x and 15, this.y, this.z and 15))
                }
                val chunk: LevelChunk = world.getChunk(chunkx, chunkz)
                BlockVec3.chunkCached_Client = chunk
                BlockVec3.chunkCacheDim_Client = world.dimension()
                BlockVec3.chunkCacheX_Client = chunkx
                BlockVec3.chunkCacheZ_Client = chunkz
                return chunk.getBlockState(BlockPos(this.x and 15, this.y, this.z and 15))
            }
            // this will be within the same chunk
            if (BlockVec3.chunkCacheX == chunkx && BlockVec3.chunkCacheZ == chunkz && BlockVec3.chunkCacheDim == world.dimension() && BlockVec3.chunkCached?.level?.isLoaded(this.toBlockPos()) == true) {
                return BlockVec3.chunkCached!!.getBlockState(BlockPos(this.x and 15, this.y, this.z and 15))
            }
            val chunk: LevelChunk = world.getChunk(chunkx, chunkz)
            BlockVec3.chunkCached = chunk
            BlockVec3.chunkCacheDim = world.dimension()
            BlockVec3.chunkCacheX = chunkx
            BlockVec3.chunkCacheZ = chunkz
            return chunk.getBlockState(BlockPos(this.x and 15, this.y, this.z and 15))
        } catch (throwable: Throwable) {
            val crashreport: CrashReport =
                CrashReport.forThrowable(throwable, "Oxygen Sealer thread: Exception getting block type in world")
            val crashreportcategory: CrashReportCategory = crashreport.addCategory("Requested block coordinates")
            crashreportcategory.setDetail(
                "Location", CrashReportCategory.formatLocation(
                    LevelHeightAccessor.create(world.minBuildHeight, world.maxBuildHeight),
                    BlockPos(
                        this.x,
                        this.y,
                        this.z
                    )
                )
            )
            throw ReportedException(crashreport)
        }
    }

    /**
     * Get block ID at the BlockVec3 coordinates without forcing a chunk load.
     *
     * @param world
     * @return the block ID, or null if the y-coordinate is less than 0 or
     * greater than 256 or the x or z is outside the Minecraft worldmap.
     * Returns Blocks.BEDROCK if the coordinates being checked are in an
     * unloaded chunk
     */
    fun getBlockState_noChunkLoad(world: Level): BlockState? {
        if (this.y < 0 || (this.y >= 256) || (this.x < -30000000) || (this.z < -30000000) || (this.x >= 30000000) || (this.z >= 30000000)) {
            return null
        }

        val chunkx = this.x shr 4
        val chunkz = this.z shr 4
        try {
            if (world.isLoaded(BlockPos(chunkx*16, 0, chunkz*16))) {//getLoadedChunk(chunkx, chunkz) != null) {
                if (!world.isClientSide) {
                    if (BlockVec3.chunkCacheX_Client == chunkx && BlockVec3.chunkCacheZ_Client == chunkz && BlockVec3.chunkCacheDim_Client == world.dimension() && world.isLoaded(BlockPos(BlockVec3.chunkCached_Client!!.pos.x * 16, 0, BlockVec3.chunkCached_Client!!.pos.z * 16))) {
                        return BlockVec3.chunkCached_Client!!.getBlockState(BlockPos(this.x and 15, this.y, this.z and 15))
                    }
                    val chunk: LevelChunk = world.getChunk(chunkx, chunkz)
                    BlockVec3.chunkCached_Client = chunk
                    BlockVec3.chunkCacheDim_Client = world.dimension()
                    BlockVec3.chunkCacheX_Client = chunkx
                    BlockVec3.chunkCacheZ_Client = chunkz
                    return chunk.getBlockState(BlockPos(this.x and 15, this.y, this.z and 15))
                }
                // calls to
                // this will be within the same chunk
                if (BlockVec3.chunkCacheX == chunkx && BlockVec3.chunkCacheZ == chunkz && BlockVec3.chunkCacheDim == world.dimension() && world.isLoaded(BlockPos(
                        BlockVec3.chunkCached_Client?.pos?.x?.times(16)!!, 0, BlockVec3.chunkCached_Client!!.pos.z * 16))) {
                    return BlockVec3.chunkCached!!.getBlockState(BlockPos(this.x and 15, this.y, this.z and 15))
                }
                val chunk: LevelChunk = world.getChunk(chunkx, chunkz)
                BlockVec3.chunkCached = chunk
                BlockVec3.chunkCacheDim = world.dimension()
                BlockVec3.chunkCacheX = chunkx
                BlockVec3.chunkCacheZ = chunkz
                return chunk.getBlockState(BlockPos(this.x and 15, this.y, this.z and 15))
            }
            // Chunk doesn't exist - meaning, it is not loaded
            return Blocks.BEDROCK.defaultBlockState()
        } catch (throwable: Throwable) {
            val crashreport: CrashReport =
                CrashReport.forThrowable(throwable, "Oxygen Sealer thread: Exception getting block type in world")
            val crashreportcategory: CrashReportCategory = crashreport.addCategory("Requested block coordinates")
            crashreportcategory.setDetail(
                "Location", CrashReportCategory.formatLocation(
                    LevelHeightAccessor.create(world.minBuildHeight, world.maxBuildHeight),
                    BlockPos(
                        this.x,
                        this.y,
                        this.z
                    )
                )
            )
            throw ReportedException(crashreport)
        }
    }
    /* TODO: FIX
    fun getBlockState(par1iBlockAccess: IBlockAccess): BlockState {
        return par1iBlockAccess.getBlockState(BlockPos(this.x, this.y, this.z))
    }*/

    /**
     * Get block ID at the BlockVec3 coordinates without forcing a chunk load.
     * Only call this 'safe' version if x and z coordinates are within the
     * Minecraft world map (-30m to +30m)
     *
     * @param world
     * @return the block ID, or null if the y-coordinate is less than 0 or
     * greater than 256. Returns Blocks.BEDROCK if the coordinates being
     * checked are in an unloaded chunk
     */
    @Nullable
    fun getBlockStateSafe_noChunkLoad(world: Level): BlockState? {
        if (this.y < 0 || this.y >= 256) {
            return null
        }

        val chunkx = this.x shr 4
        val chunkz = this.z shr 4
        try {
            if (world.isLoaded(BlockPos(chunkx*16, 0, chunkz * 16))) {
                if (!world.isClientSide) {
                    if (BlockVec3.chunkCacheX_Client == chunkx && BlockVec3.chunkCacheZ_Client == chunkz && BlockVec3.chunkCacheDim_Client == world.dimension() && world.isLoaded(BlockPos(BlockVec3.chunkCached_Client!!.pos.x * 16, 0, BlockVec3.chunkCached_Client!!.pos.z * 16))) {
                        return BlockVec3.chunkCached_Client!!.getBlockState(BlockPos(this.x and 15, this.y, this.z and 15))
                    }
                    val chunk: LevelChunk = world.getChunk(chunkx, chunkz)
                    BlockVec3.chunkCached_Client = chunk
                    BlockVec3.chunkCacheDim_Client = world.dimension()
                    BlockVec3.chunkCacheX_Client = chunkx
                    BlockVec3.chunkCacheZ_Client = chunkz
                    return chunk.getBlockState(BlockPos(this.x and 15, this.y, this.z and 15))
                }
                // calls to
                // this will be within the same chunk
                if (BlockVec3.chunkCacheX == chunkx && BlockVec3.chunkCacheZ == chunkz && BlockVec3.chunkCacheDim == world.dimension() && world.isLoaded(BlockPos(BlockVec3.chunkCached_Client!!.pos.x * 16, 0, BlockVec3.chunkCached_Client!!.pos.z * 16))) {
                    return BlockVec3.chunkCached!!.getBlockState(BlockPos(this.x and 15, this.y, this.z and 15))
                }
                val chunk: LevelChunk = world.getChunk(chunkx, chunkz)
                BlockVec3.chunkCached = chunk
                BlockVec3.chunkCacheDim = world.dimension()
                BlockVec3.chunkCacheX = chunkx
                BlockVec3.chunkCacheZ = chunkz
                return chunk.getBlockState(BlockPos(this.x and 15, this.y, this.z and 15))
            }
            // Chunk doesn't exist - meaning, it is not loaded
            return Blocks.BEDROCK.defaultBlockState()
        } catch (throwable: Throwable) {
            val crashreport: CrashReport =
                CrashReport.forThrowable(throwable, "Oxygen Sealer thread: Exception getting block type in world")
            val crashreportcategory: CrashReportCategory = crashreport.addCategory("Requested block coordinates")
            crashreportcategory.setDetail(
                "Location", CrashReportCategory.formatLocation(
                    LevelHeightAccessor.create(world.minBuildHeight, world.maxBuildHeight),
                    BlockPos(
                        this.x,
                        this.y,
                        this.z
                    )
                )
            )
            throw ReportedException(crashreport)
        }
    }

    fun translate(par1: BlockVec3): BlockVec3 {
        this.x += par1.x
        this.y += par1.y
        this.z += par1.z
        return this
    }

    fun translate(par1x: Int, par1y: Int, par1z: Int): BlockVec3 {
        this.x += par1x
        this.y += par1y
        this.z += par1z
        return this
    }

    fun add(par1: BlockVec3, a: BlockVec3): BlockVec3 {
        return BlockVec3(par1.x + a.x, par1.y + a.y, par1.z + a.z)
    }

    fun subtract(par1: BlockVec3): BlockVec3 {
        this.x -= par1.x
        this.y -= par1.y
        this.z -= par1.z

        return this
    }

    fun scale(par1: Int): BlockVec3 {
        this.x *= par1
        this.y *= par1
        this.z *= par1

        return this
    }

    fun modifyPositionFromSide(side: Direction, amount: Int): BlockVec3 {
        when (side.ordinal) {
            0 -> this.y -= amount
            1 -> this.y += amount
            2 -> this.z -= amount
            3 -> this.z += amount
            4 -> this.x -= amount
            5 -> this.x += amount
        }
        return this
    }

    fun newVecSide(side: Int): BlockVec3 {
        val vec: BlockVec3 = BlockVec3(this.x, this.y, this.z)
        vec.sideDoneBits = (1 shl (side xor 1)) + (side shl 6)
        when (side) {
            0 -> {
                vec.y--
                return vec
            }

            1 -> {
                vec.y++
                return vec
            }

            2 -> {
                vec.z--
                return vec
            }

            3 -> {
                vec.z++
                return vec
            }

            4 -> {
                vec.x--
                return vec
            }

            5 -> {
                vec.x++
                return vec
            }
        }
        return vec
    }

    fun modifyPositionFromSide(side: Direction): BlockVec3 {
        return this.modifyPositionFromSide(side, 1)
    }

    override fun hashCode(): Int {
        // Upgraded hashCode calculation from the one in VecDirPair to something
        // a bit stronger and faster
        return ((this.y * 379 + this.x) * 373 + this.z) * 7
    }

    override fun equals(o: Any?): Boolean {
        if (o is BlockVec3) {
            val vector = o
            return this.x == vector.x && (this.y == vector.y) && (this.z == vector.z)
        }

        return false
    }

    override fun toString(): String {
        return "[" + this.x + "," + this.y + "," + this.z + "]"
    }


    // TODO: Fix
    /**
     * This will load the chunk.
     *
     * @param world
     * @return TileEntity
     */
    /*
    fun getTileEntity(world: IBlockAccess): BlockEntity {
        return world.getTileEntity(BlockPos(this.x, this.y, this.z))
    }*/

    /**
     * No chunk load: returns null if chunk to side is unloaded
     *
     * @param world
     * @param side
     * @return TileEntity
     */
    fun getBlockEntityOnSide(world: Level, side: Direction?): BlockEntity? {
        if (side == null) {
            return null
        }

        var x = this.x
        var y = this.y
        var z = this.z
        when (side.ordinal) {
            0 -> y--
            1 -> y++
            2 -> z--
            3 -> z++
            4 -> x--
            5 -> x++
            else -> return null
        }
        val pos = BlockPos(x, y, z)
        return if (world.isLoaded(pos)) world.getBlockEntity(pos) else null
    }

    /**
     * No chunk load: returns null if chunk to side is unloaded
     *
     * @param world
     * @param side
     * @return TileEntity
     */
    fun getTileEntityOnSide(world: Level, side: Int): BlockEntity? {
        var x = this.x
        var y = this.y
        var z = this.z
        when (side) {
            0 -> y--
            1 -> y++
            2 -> z--
            3 -> z++
            4 -> x--
            5 -> x++
            else -> return null
        }
        val pos = BlockPos(x, y, z)
        return if (world.isLoaded(pos)) world.getBlockEntity(pos) else null
    }

    /**
     * This will load the chunk to the side.
     *
     * @param world
     * @param side
     * @return boolean true if face is solid, false if not solid
     */
    fun blockOnSideHasSolidFace(world: Level, side: Int): Boolean {
        var x = this.x
        var y = this.y
        var z = this.z
        when (side) {
            0 -> y--
            1 -> y++
            2 -> z--
            3 -> z++
            4 -> x--
            5 -> x++
            else -> return false
        }
        val pos = BlockPos(x, y, z)
        return true // world.getBlockState(pos).getBlock().isSideSolid(world.getBlockState(pos), world, pos, Direction.byIndex(side xor 1))
    }

    /**
     * No chunk load: returns null if chunk is unloaded
     *
     * @param world
     * @param side
     * @return Block
     */
    fun getBlockOnSide(world: Level, side: Int): Block? {
        var x = this.x
        var y = this.y
        var z = this.z
        when (side) {
            0 -> y--
            1 -> y++
            2 -> z--
            3 -> z++
            4 -> x--
            5 -> x++
            else -> return null
        }
        val pos = BlockPos(x, y, z)
        return if (world.isLoaded(pos)) world.getBlockState(pos).getBlock() else null
    }
/*
    fun getBlockMetadata(world: IBlockAccess): Int {
        val state: IBlockState = world.getBlockState(BlockPos(x, y, z))
        return state.getBlock().getMetaFromState(state)
    }
    */

    fun readFromNBT(nbtCompound: CompoundTag): BlockVec3 {
        val tempVector: BlockVec3 = BlockVec3()
        tempVector.x = nbtCompound.getInt("x")
        tempVector.y = nbtCompound.getInt("y")
        tempVector.z = nbtCompound.getInt("z")
        return tempVector
    }

    fun distanceTo(vector: BlockVec3): Int {
        val var2 = vector.x - this.x
        val var4 = vector.y - this.y
        val var6 = vector.z - this.z
        return Mth.floor(sqrt((var2 * var2 + var4 * var4 + var6 * var6).toDouble()))
    }

    fun distanceSquared(vector: BlockVec3): Int {
        val var2 = vector.x - this.x
        val var4 = vector.y - this.y
        val var6 = vector.z - this.z
        return var2 * var2 + var4 * var4 + var6 * var6
    }

    fun writeToNBT(tag: CompoundTag): CompoundTag {
        tag.putInt("x", this.x)
        tag.putInt("y", this.y)
        tag.putInt("z", this.z)
        return tag
    }

    fun BlockVec3(tag: CompoundTag) {
        this.x = tag.getInt("x")
        this.y = tag.getInt("y")
        this.z = tag.getInt("z")
    }

    fun writeToNBT(tag: CompoundTag, prefix: String): CompoundTag {
        tag.putInt(prefix + "_x", this.x)
        tag.putInt(prefix + "_y", this.y)
        tag.putInt(prefix + "_z", this.z)
        return tag
    }

    fun readFromNBT(tag: CompoundTag, prefix: String): BlockVec3? {
        val readX: Int = tag.getInt(prefix + "_x")
        if (readX == 0) return null
        val readY: Int = tag.getInt(prefix + "_y")
        if (readY == 0) return null
        val readZ: Int = tag.getInt(prefix + "_z")
        if (readZ == 0) return null
        return BlockVec3(readX, readY, readZ)
    }

    fun getMagnitude(): Double {
        return sqrt(getMagnitudeSquared().toDouble())
    }

    fun getMagnitudeSquared(): Int {
        return this.x * this.x + (this.y * this.y) + (this.z * this.z)
    }

    fun setBlock(worldObj: Level, block: BlockState?) {
        worldObj.setBlock(BlockPos(x, y, z), block, 3)
    }

    fun blockExists(world: Level): Boolean {
        return world.isLoaded(BlockPos(this.x, this.y, this.z))
    }

    fun setSideDone(side: Int) {
        this.sideDoneBits = this.sideDoneBits or (1 shl side)
    }

    fun getTileEntityForce(world: Level): BlockEntity? {
        val chunkx = this.x shr 4
        val chunkz = this.z shr 4

        if (world.isLoaded(BlockPos(chunkx * 16, 0, chunkz * 16))
        ) return world.getBlockEntity(this.toBlockPos())

        val chunk: LevelChunk = world.getChunk(chunkx, chunkz)
        chunk.setLoaded(true)
        return chunk.getBlockEntity(BlockPos(this.x and 15, this.y, this.z and 15), LevelChunk.EntityCreationType.IMMEDIATE)
    }

    fun midPoint(): Vec3 {
        return Vec3(this.x + 0.5, this.y + 0.5, this.z + 0.5)
    }
}