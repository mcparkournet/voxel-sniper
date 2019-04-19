package com.thevoxelbox.voxelsniper.brush;

import com.thevoxelbox.voxelsniper.Message;
import com.thevoxelbox.voxelsniper.SnipeData;
import com.thevoxelbox.voxelsniper.Undo;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;

/**
 * @author Voxel
 */
public class SetRedstoneFlipBrush extends AbstractBrush {

	@Nullable
	private Block block;
	private Undo undo;
	private boolean northSouth = true;

	/**
	 *
	 */
	public SetRedstoneFlipBrush() {
		this.setName("Set Redstone Flip");
	}

	private boolean set(Block bl) {
		if (this.block == null) {
			this.block = bl;
			return true;
		} else {
			this.undo = new Undo();
			int lowX = (this.block.getX() <= bl.getX()) ? this.block.getX() : bl.getX();
			int lowY = (this.block.getY() <= bl.getY()) ? this.block.getY() : bl.getY();
			int lowZ = (this.block.getZ() <= bl.getZ()) ? this.block.getZ() : bl.getZ();
			int highX = (this.block.getX() >= bl.getX()) ? this.block.getX() : bl.getX();
			int highY = (this.block.getY() >= bl.getY()) ? this.block.getY() : bl.getY();
			int highZ = (this.block.getZ() >= bl.getZ()) ? this.block.getZ() : bl.getZ();
			for (int y = lowY; y <= highY; y++) {
				for (int x = lowX; x <= highX; x++) {
					for (int z = lowZ; z <= highZ; z++) {
						this.perform(this.clampY(x, y, z));
					}
				}
			}
			this.block = null;
			return false;
		}
	}

	@SuppressWarnings("deprecation")
	private void perform(Block bl) {
		if (bl.getType() == Material.LEGACY_DIODE_BLOCK_ON || bl.getType() == Material.LEGACY_DIODE_BLOCK_OFF) {
			if (this.northSouth) {
				if ((bl.getData() % 4) == 1) {
					this.undo.put(bl);
					bl.setData((byte) (bl.getData() + 2));
				} else if ((bl.getData() % 4) == 3) {
					this.undo.put(bl);
					bl.setData((byte) (bl.getData() - 2));
				}
			} else {
				if ((bl.getData() % 4) == 2) {
					this.undo.put(bl);
					bl.setData((byte) (bl.getData() - 2));
				} else if ((bl.getData() % 4) == 0) {
					this.undo.put(bl);
					bl.setData((byte) (bl.getData() + 2));
				}
			}
		}
	}

	@Override
	protected final void arrow(SnipeData v) {
		if (this.set(this.getTargetBlock())) {
			v.sendMessage(ChatColor.GRAY + "Point one");
		} else {
			v.owner()
				.storeUndo(this.undo);
		}
	}

	@Override
	protected final void powder(SnipeData v) {
		if (this.set(this.getLastBlock())) {
			v.sendMessage(ChatColor.GRAY + "Point one");
		} else {
			v.owner()
				.storeUndo(this.undo);
		}
	}

	@Override
	public final void info(Message message) {
		this.block = null;
		message.brushName(this.getName());
	}

	@Override
	public final void parameters(String[] parameters, SnipeData snipeData) {
		for (int i = 1; i < parameters.length; i++) {
			if (parameters[i].equalsIgnoreCase("info")) {
				snipeData.sendMessage(ChatColor.GOLD + "Set Repeater Flip Parameters:");
				snipeData.sendMessage(ChatColor.AQUA + "/b setrf <direction> -- valid direction inputs are(n,s,e,world), Set the direction that you wish to flip your repeaters, defaults to north/south.");
				return;
			}
			if (parameters[i].startsWith("n") || parameters[i].startsWith("s") || parameters[i].startsWith("ns")) {
				this.northSouth = true;
				snipeData.sendMessage(ChatColor.AQUA + "Flip direction set to north/south");
			} else if (parameters[i].startsWith("e") || parameters[i].startsWith("world") || parameters[i].startsWith("ew")) {
				this.northSouth = false;
				snipeData.sendMessage(ChatColor.AQUA + "Flip direction set to east/west.");
			} else {
				snipeData.sendMessage(ChatColor.RED + "Invalid brush parameters! use the info parameter to display parameter info.");
			}
		}
	}

	@Override
	public String getPermissionNode() {
		return "voxelsniper.brush.setredstoneflip";
	}
}