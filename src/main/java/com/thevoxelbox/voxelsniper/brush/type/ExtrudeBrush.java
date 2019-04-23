package com.thevoxelbox.voxelsniper.brush.type;

import com.thevoxelbox.voxelsniper.sniper.Sniper;
import com.thevoxelbox.voxelsniper.sniper.Undo;
import com.thevoxelbox.voxelsniper.sniper.toolkit.Messages;
import com.thevoxelbox.voxelsniper.sniper.toolkit.ToolkitProperties;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * http://www.voxelwiki.com/minecraft/Voxelsniper#Extrude_Brush
 *
 * @author psanker
 */
public class ExtrudeBrush extends AbstractBrush {

	private double trueCircle;

	public ExtrudeBrush() {
		super("Extrude");
	}

	private void extrudeUpOrDown(ToolkitProperties toolkitProperties, boolean isUp) {
		int brushSize = toolkitProperties.getBrushSize();
		double brushSizeSquared = Math.pow(brushSize + this.trueCircle, 2);
		Undo undo = new Undo();
		for (int x = -brushSize; x <= brushSize; x++) {
			double xSquared = Math.pow(x, 2);
			for (int z = -brushSize; z <= brushSize; z++) {
				if ((xSquared + Math.pow(z, 2)) <= brushSizeSquared) {
					int direction = (isUp ? 1 : -1);
					for (int y = 0; y < Math.abs(toolkitProperties.getVoxelHeight()); y++) {
						int tempY = y * direction;
						undo = this.perform(this.clampY(this.getTargetBlock()
							.getX() + x, this.getTargetBlock()
							.getY() + tempY, this.getTargetBlock()
							.getZ() + z), this.clampY(this.getTargetBlock()
							.getX() + x, this.getTargetBlock()
							.getY() + tempY + direction, this.getTargetBlock()
							.getZ() + z), toolkitProperties, undo);
					}
				}
			}
		}
		Sniper owner = toolkitProperties.getOwner();
		owner.storeUndo(undo);
	}

	private void extrudeNorthOrSouth(ToolkitProperties v, boolean isSouth) {
		int brushSize = v.getBrushSize();
		double brushSizeSquared = Math.pow(brushSize + this.trueCircle, 2);
		Undo undo = new Undo();
		for (int x = -brushSize; x <= brushSize; x++) {
			double xSquared = Math.pow(x, 2);
			for (int y = -brushSize; y <= brushSize; y++) {
				if ((xSquared + Math.pow(y, 2)) <= brushSizeSquared) {
					int direction = (isSouth) ? 1 : -1;
					for (int z = 0; z < Math.abs(v.getVoxelHeight()); z++) {
						int tempZ = z * direction;
						undo = this.perform(this.clampY(this.getTargetBlock()
							.getX() + x, this.getTargetBlock()
							.getY() + y, this.getTargetBlock()
							.getZ() + tempZ), this.clampY(this.getTargetBlock()
							.getX() + x, this.getTargetBlock()
							.getY() + y, this.getTargetBlock()
							.getZ() + tempZ + direction), v, undo);
					}
				}
			}
		}
		v.getOwner()
			.storeUndo(undo);
	}

	private void extrudeEastOrWest(ToolkitProperties v, boolean isEast) {
		int brushSize = v.getBrushSize();
		double brushSizeSquared = Math.pow(brushSize + this.trueCircle, 2);
		Undo undo = new Undo();
		for (int y = -brushSize; y <= brushSize; y++) {
			double ySquared = Math.pow(y, 2);
			for (int z = -brushSize; z <= brushSize; z++) {
				if ((ySquared + Math.pow(z, 2)) <= brushSizeSquared) {
					int direction = (isEast) ? 1 : -1;
					for (int x = 0; x < Math.abs(v.getVoxelHeight()); x++) {
						int tempX = x * direction;
						undo = this.perform(this.clampY(this.getTargetBlock()
							.getX() + tempX, this.getTargetBlock()
							.getY() + y, this.getTargetBlock()
							.getZ() + z), this.clampY(this.getTargetBlock()
							.getX() + tempX + direction, this.getTargetBlock()
							.getY() + y, this.getTargetBlock()
							.getZ() + z), v, undo);
					}
				}
			}
		}
		v.getOwner()
			.storeUndo(undo);
	}

	private Undo perform(Block block1, Block block2, ToolkitProperties toolkitProperties, Undo undo) {
		if (toolkitProperties.isVoxelListContains(this.getBlockData(block1.getX(), block1.getY(), block1.getZ()))) {
			undo.put(block2);
			this.setBlockType(block2.getZ(), block2.getX(), block2.getY(), this.getBlockType(block1.getX(), block1.getY(), block1.getZ()));
			this.clampY(block2.getX(), block2.getY(), block2.getZ())
				.setBlockData(this.clampY(block1.getX(), block1.getY(), block1.getZ())
					.getBlockData());
		}
		return undo;
	}

	private void selectExtrudeMethod(ToolkitProperties v, BlockFace blockFace, boolean towardsUser) {
		if (blockFace == null || v.getVoxelHeight() == 0) {
			return;
		}
		switch (blockFace) {
			case UP:
				extrudeUpOrDown(v, towardsUser);
				break;
			case SOUTH:
				extrudeNorthOrSouth(v, towardsUser);
				break;
			case EAST:
				extrudeEastOrWest(v, towardsUser);
				break;
			default:
				break;
		}
	}

	@Override
	public final void arrow(ToolkitProperties toolkitProperties) {
		this.selectExtrudeMethod(toolkitProperties, this.getTargetBlock()
			.getFace(this.getLastBlock()), false);
	}

	@Override
	public final void powder(ToolkitProperties toolkitProperties) {
		this.selectExtrudeMethod(toolkitProperties, this.getTargetBlock()
			.getFace(this.getLastBlock()), true);
	}

	@Override
	public final void info(Messages messages) {
		messages.brushName(this.getName());
		messages.size();
		messages.height();
		messages.voxelList();
		messages.custom(ChatColor.AQUA + ((this.trueCircle == 0.5) ? "True circle mode ON" : "True circle mode OFF"));
	}

	@Override
	public final void parameters(String[] parameters, ToolkitProperties toolkitProperties) {
		for (int i = 1; i < parameters.length; i++) {
			String parameter = parameters[i];
			try {
				if (parameter.equalsIgnoreCase("info")) {
					toolkitProperties.sendMessage(ChatColor.GOLD + "Extrude brush Parameters:");
					toolkitProperties.sendMessage(ChatColor.AQUA + "/b ex true -- will use a true circle algorithm instead of the skinnier version with classic sniper nubs. /b ex false will switch back. (false is default)");
					return;
				} else if (parameter.startsWith("true")) {
					this.trueCircle = 0.5;
					toolkitProperties.sendMessage(ChatColor.AQUA + "True circle mode ON.");
				} else if (parameter.startsWith("false")) {
					this.trueCircle = 0;
					toolkitProperties.sendMessage(ChatColor.AQUA + "True circle mode OFF.");
				} else {
					toolkitProperties.sendMessage(ChatColor.RED + "Invalid brush parameters! Use the \"info\" parameter to display parameter info.");
					return;
				}
			} catch (RuntimeException exception) {
				toolkitProperties.sendMessage(ChatColor.RED + "Incorrect parameter \"" + parameter + "\"; use the \"info\" parameter.");
			}
		}
	}

	@Override
	public String getPermissionNode() {
		return "voxelsniper.brush.extrude";
	}
}