package me.nonplay.ndailyrewards.nbt;

import org.bukkit.block.Block;

import me.nonplay.ndailyrewards.nbt.utils.MinecraftVersion;

public class NBTBlock {

	private final Block block;
	private final NBTChunk nbtChunk;
	
	public NBTBlock(Block block) {
		this.block = block;
		if(!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_7_R4)) {
			throw new NbtApiException("NBTBlock is only working for 1.7.4+!");
		}
		nbtChunk = new NBTChunk(block.getChunk());
	}
	
	
	public NBTCompound getData() {
		return nbtChunk.getPersistentDataContainer().getOrCreateCompound("blocks").getOrCreateCompound(block.getX() + "_" + block.getY() + "_" + block.getZ());
	}


}
