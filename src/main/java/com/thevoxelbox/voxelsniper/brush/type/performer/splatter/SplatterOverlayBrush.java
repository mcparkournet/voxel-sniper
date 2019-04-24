package com.thevoxelbox.voxelsniper.brush.type.performer.splatter;

import java.util.Random;
import com.thevoxelbox.voxelsniper.brush.type.performer.AbstractPerformerBrush;
import com.thevoxelbox.voxelsniper.sniper.Sniper;
import com.thevoxelbox.voxelsniper.sniper.toolkit.Messages;
import com.thevoxelbox.voxelsniper.sniper.toolkit.ToolkitProperties;
import com.thevoxelbox.voxelsniper.util.LegacyMaterialConverter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * http://www.voxelwiki.com/minecraft/Voxelsniper#Splatter_Overlay_Brush
 *
 * @author Gavjenks Splatterized blockPositionY Giltwist
 */
public class SplatterOverlayBrush extends AbstractPerformerBrush {

	private static final int GROW_PERCENT_MIN = 1;
	private static final int GROW_PERCENT_DEFAULT = 1000;
	private static final int GROW_PERCENT_MAX = 9999;
	private static final int SEED_PERCENT_MIN = 1;
	private static final int SEED_PERCENT_DEFAULT = 1000;
	private static final int SEED_PERCENT_MAX = 9999;
	private static final int SPLATREC_PERCENT_MIN = 1;
	private static final int SPLATREC_PERCENT_DEFAULT = 3;
	private static final int SPLATREC_PERCENT_MAX = 10;

	private int seedPercent; // Chance block on first pass is made active
	private int growPercent; // chance block on recursion pass is made active
	private int splatterRecursions; // How many times you grow the seeds
	private int yOffset;
	private boolean randomizeHeight;
	private Random generator = new Random();
	private int depth = 3;
	private boolean allBlocks;

	public SplatterOverlayBrush() {
		super("Splatter Overlay");
	}

	private void splatterOverlay(ToolkitProperties toolkitProperties) {
		// Splatter Time
		int[][] splat = new int[2 * toolkitProperties.getBrushSize() + 1][2 * toolkitProperties.getBrushSize() + 1];
		// Seed the array
		for (int x = 2 * toolkitProperties.getBrushSize(); x >= 0; x--) {
			for (int y = 2 * toolkitProperties.getBrushSize(); y >= 0; y--) {
				if (this.generator.nextInt(SEED_PERCENT_MAX + 1) <= this.seedPercent) {
					splat[x][y] = 1;
				}
			}
		}
		// Grow the seeds
		int gref = this.growPercent;
		int[][] tempSplat = new int[2 * toolkitProperties.getBrushSize() + 1][2 * toolkitProperties.getBrushSize() + 1];
		for (int r = 0; r < this.splatterRecursions; r++) {
			this.growPercent = gref - ((gref / this.splatterRecursions) * (r));
			for (int x = 2 * toolkitProperties.getBrushSize(); x >= 0; x--) {
				for (int y = 2 * toolkitProperties.getBrushSize(); y >= 0; y--) {
					tempSplat[x][y] = splat[x][y]; // prime tempsplat
					int growcheck = 0;
					if (splat[x][y] == 0) {
						if (x != 0 && splat[x - 1][y] == 1) {
							growcheck++;
						}
						if (y != 0 && splat[x][y - 1] == 1) {
							growcheck++;
						}
						if (x != 2 * toolkitProperties.getBrushSize() && splat[x + 1][y] == 1) {
							growcheck++;
						}
						if (y != 2 * toolkitProperties.getBrushSize() && splat[x][y + 1] == 1) {
							growcheck++;
						}
					}
					if (growcheck >= 1 && this.generator.nextInt(GROW_PERCENT_MAX + 1) <= this.growPercent) {
						tempSplat[x][y] = 1; // prevent bleed into splat
					}
				}
			}
			// integrate tempsplat back into splat at end of iteration
			for (int x = 2 * toolkitProperties.getBrushSize(); x >= 0; x--) {
				if (2 * toolkitProperties.getBrushSize() + 1 >= 0) {
					System.arraycopy(tempSplat[x], 0, splat[x], 0, 2 * toolkitProperties.getBrushSize() + 1);
				}
			}
		}
		this.growPercent = gref;
		int[][] memory = new int[2 * toolkitProperties.getBrushSize() + 1][2 * toolkitProperties.getBrushSize() + 1];
		double brushSizeSquared = Math.pow(toolkitProperties.getBrushSize() + 0.5, 2);
		for (int z = toolkitProperties.getBrushSize(); z >= -toolkitProperties.getBrushSize(); z--) {
			for (int x = toolkitProperties.getBrushSize(); x >= -toolkitProperties.getBrushSize(); x--) {
				Block targetBlock = this.getTargetBlock();
				for (int y = targetBlock.getY(); y > 0; y--) {
					// start scanning from the height you clicked at
					if (memory[x + toolkitProperties.getBrushSize()][z + toolkitProperties.getBrushSize()] != 1) {
						// if haven't already found the surface in this column
						if ((Math.pow(x, 2) + Math.pow(z, 2)) <= brushSizeSquared && splat[x + toolkitProperties.getBrushSize()][z + toolkitProperties.getBrushSize()] == 1) {
							// if inside of the column && if to be splattered
							Material check = this.getBlockType(targetBlock.getX() + x, y + 1, targetBlock.getZ() + z);
							if (check.isEmpty() || check == Material.WATER) {
								// must start at surface... this prevents it filling stuff in if you click in a wall
								// and it starts out below surface.
								if (this.allBlocks) {
									int depth = this.randomizeHeight ? this.generator.nextInt(this.depth) : this.depth;
									for (int i = this.depth - 1; ((this.depth - i) <= depth); i--) {
										if (!this.clampY(targetBlock.getX() + x, y - i, targetBlock.getZ() + z)
											.getType()
											.isEmpty()) {
											// fills down as many layers as you specify in parameters
											this.performer.perform(this.clampY(targetBlock.getX() + x, y - i + this.yOffset, targetBlock.getZ() + z));
											// stop it from checking any other blocks in this vertical 1x1 column.
											memory[x + toolkitProperties.getBrushSize()][z + toolkitProperties.getBrushSize()] = 1;
										}
									}
								} else {
									// if the override parameter has not been activated, go to the switch that filters out manmade stuff.
									switch (LegacyMaterialConverter.getLegacyMaterialId(this.getBlockType(targetBlock.getX() + x, y, targetBlock.getZ() + z))) {
										case 1:
										case 2:
										case 3:
										case 12:
										case 13:
										case 24:// These cases filter out any manufactured or refined blocks, any trees and leas, etc. that you don't want to mess with.
										case 48:
										case 82:
										case 49:
										case 78:
											int depth = this.randomizeHeight ? this.generator.nextInt(this.depth) : this.depth;
											for (int d = this.depth - 1; ((this.depth - d) <= depth); d--) {
												if (!this.clampY(targetBlock.getX() + x, y - d, targetBlock.getZ() + z)
													.getType()
													.isEmpty()) {
													// fills down as many layers as you specify in parameters
													this.performer.perform(this.clampY(targetBlock.getX() + x, y - d + this.yOffset, targetBlock.getZ() + z));
													// stop it from checking any other blocks in this vertical 1x1 column.
													memory[x + toolkitProperties.getBrushSize()][z + toolkitProperties.getBrushSize()] = 1;
												}
											}
											break;
										default:
											break;
									}
								}
							}
						}
					}
				}
			}
		}
		toolkitProperties.getOwner()
			.storeUndo(this.performer.getUndo());
	}

	private void splatterOverlayTwo(ToolkitProperties toolkitProperties) {
		// Splatter Time
		int[][] splat = new int[2 * toolkitProperties.getBrushSize() + 1][2 * toolkitProperties.getBrushSize() + 1];
		// Seed the array
		for (int x = 2 * toolkitProperties.getBrushSize(); x >= 0; x--) {
			for (int y = 2 * toolkitProperties.getBrushSize(); y >= 0; y--) {
				if (this.generator.nextInt(SEED_PERCENT_MAX + 1) <= this.seedPercent) {
					splat[x][y] = 1;
				}
			}
		}
		// Grow the seeds
		int gref = this.growPercent;
		int[][] tempsplat = new int[2 * toolkitProperties.getBrushSize() + 1][2 * toolkitProperties.getBrushSize() + 1];
		for (int r = 0; r < this.splatterRecursions; r++) {
			this.growPercent = gref - ((gref / this.splatterRecursions) * (r));
			for (int x = 2 * toolkitProperties.getBrushSize(); x >= 0; x--) {
				for (int y = 2 * toolkitProperties.getBrushSize(); y >= 0; y--) {
					tempsplat[x][y] = splat[x][y]; // prime tempsplat
					int growcheck = 0;
					if (splat[x][y] == 0) {
						if (x != 0 && splat[x - 1][y] == 1) {
							growcheck++;
						}
						if (y != 0 && splat[x][y - 1] == 1) {
							growcheck++;
						}
						if (x != 2 * toolkitProperties.getBrushSize() && splat[x + 1][y] == 1) {
							growcheck++;
						}
						if (y != 2 * toolkitProperties.getBrushSize() && splat[x][y + 1] == 1) {
							growcheck++;
						}
					}
					if (growcheck >= 1 && this.generator.nextInt(GROW_PERCENT_MAX + 1) <= this.growPercent) {
						tempsplat[x][y] = 1; // prevent bleed into splat
					}
				}
			}
			// integrate tempsplat back into splat at end of iteration
			for (int x = 2 * toolkitProperties.getBrushSize(); x >= 0; x--) {
				if (2 * toolkitProperties.getBrushSize() + 1 >= 0) {
					System.arraycopy(tempsplat[x], 0, splat[x], 0, 2 * toolkitProperties.getBrushSize() + 1);
				}
			}
		}
		this.growPercent = gref;
		int[][] memory = new int[toolkitProperties.getBrushSize() * 2 + 1][toolkitProperties.getBrushSize() * 2 + 1];
		double brushSizeSquared = Math.pow(toolkitProperties.getBrushSize() + 0.5, 2);
		for (int z = toolkitProperties.getBrushSize(); z >= -toolkitProperties.getBrushSize(); z--) {
			for (int x = toolkitProperties.getBrushSize(); x >= -toolkitProperties.getBrushSize(); x--) {
				Block targetBlock = this.getTargetBlock();
				for (int y = targetBlock.getY(); y > 0; y--) { // start scanning from the height you clicked at
					if (memory[x + toolkitProperties.getBrushSize()][z + toolkitProperties.getBrushSize()] != 1) { // if haven't already found the surface in this column
						if ((Math.pow(x, 2) + Math.pow(z, 2)) <= brushSizeSquared && splat[x + toolkitProperties.getBrushSize()][z + toolkitProperties.getBrushSize()] == 1) { // if inside of the column...&& if to be splattered
							if (!getBlockType(targetBlock.getX() + x, y - 1, targetBlock.getZ() + z).isEmpty()) { // if not a floating block (like one of Notch'world pools)
								if (getBlockType(targetBlock.getX() + x, y + 1, targetBlock.getZ() + z).isEmpty()) { // must start at surface... this prevents it filling stuff in if
									// you click in a wall and it starts out below surface.
									if (this.allBlocks) {
										int depth = this.randomizeHeight ? this.generator.nextInt(this.depth) : this.depth;
										for (int i = 1; (i < depth + 1); i++) {
											this.performer.perform(clampY(targetBlock.getX() + x, y + i + this.yOffset, targetBlock.getZ() + z)); // fills down as many layers as you specify in
											// parameters
											memory[x + toolkitProperties.getBrushSize()][z + toolkitProperties.getBrushSize()] = 1; // stop it from checking any other blocks in this vertical 1x1 column.
										}
									} else { // if the override parameter has not been activated, go to the switch that filters out manmade stuff.
										switch (LegacyMaterialConverter.getLegacyMaterialId(getBlockType(targetBlock.getX() + x, y, targetBlock.getZ() + z))) {
											case 1:
											case 2:
											case 3:
											case 12:
											case 13:
											case 14: // These cases filter out any manufactured or refined blocks, any trees and leas, etc. that you don't want to
												// mess with.
											case 15:
											case 16:
											case 24:
											case 48:
											case 82:
											case 49:
											case 78:
												int depth = this.randomizeHeight ? this.generator.nextInt(this.depth) : this.depth;
												for (int i = 1; (i < depth + 1); i++) {
													this.performer.perform(clampY(targetBlock.getX() + x, y + i + this.yOffset, targetBlock.getZ() + z)); // fills down as many layers as you specify
													// in parameters
													memory[x + toolkitProperties.getBrushSize()][z + toolkitProperties.getBrushSize()] = 1; // stop it from checking any other blocks in this vertical 1x1 column.
												}
												break;
											default:
												break;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		Sniper owner = toolkitProperties.getOwner();
		owner.storeUndo(this.performer.getUndo());
	}

	@Override
	public final void arrow(ToolkitProperties toolkitProperties) {
		this.splatterOverlay(toolkitProperties);
	}

	@Override
	public final void powder(ToolkitProperties toolkitProperties) {
		this.splatterOverlayTwo(toolkitProperties);
	}

	@Override
	public final void info(Messages messages) {
		if (this.seedPercent < SEED_PERCENT_MIN || this.seedPercent > SEED_PERCENT_MAX) {
			this.seedPercent = SEED_PERCENT_DEFAULT;
		}
		if (this.growPercent < GROW_PERCENT_MIN || this.growPercent > GROW_PERCENT_MAX) {
			this.growPercent = GROW_PERCENT_DEFAULT;
		}
		if (this.splatterRecursions < SPLATREC_PERCENT_MIN || this.splatterRecursions > SPLATREC_PERCENT_MAX) {
			this.splatterRecursions = SPLATREC_PERCENT_DEFAULT;
		}
		messages.brushName(this.getName());
		messages.size();
		messages.custom(ChatColor.BLUE + "Seed percent set to: " + this.seedPercent / 100 + "%");
		messages.custom(ChatColor.BLUE + "Growth percent set to: " + this.growPercent / 100 + "%");
		messages.custom(ChatColor.BLUE + "Recursions set to: " + this.splatterRecursions);
		messages.custom(ChatColor.BLUE + "Y-Offset set to: " + this.yOffset);
	}

	@Override
	public final void parameters(String[] parameters, ToolkitProperties toolkitProperties) {
		for (int i = 1; i < parameters.length; i++) {
			String parameter = parameters[i];
			try {
				if (parameter.equalsIgnoreCase("info")) {
					toolkitProperties.sendMessage(ChatColor.GOLD + "Splatter Overlay brush parameters:");
					toolkitProperties.sendMessage(ChatColor.AQUA + "d[number] (ex:  d3) How many blocks deep you want to replace from the surface.");
					toolkitProperties.sendMessage(ChatColor.BLUE + "all (ex:  /b over all) Sets the brush to overlay over ALL materials, not just natural surface ones (will no longer ignore trees and buildings).  The parameter /some will set it back to default.");
					toolkitProperties.sendMessage(ChatColor.AQUA + "/b sover s[int] -- set a seed percentage (1-9999). 100 = 1% Default is 1000");
					toolkitProperties.sendMessage(ChatColor.AQUA + "/b sover g[int] -- set a growth percentage (1-9999).  Default is 1000");
					toolkitProperties.sendMessage(ChatColor.AQUA + "/b sover r[int] -- set a recursion (1-10).  Default is 3");
					return;
				} else if (!parameter.isEmpty() && parameter.charAt(0) == 'd') {
					this.depth = Integer.parseInt(parameter.replace("d", ""));
					toolkitProperties.sendMessage(ChatColor.AQUA + "Depth set to " + this.depth);
					if (this.depth < 1) {
						this.depth = 1;
					}
				} else if (parameter.startsWith("all")) {
					this.allBlocks = true;
					toolkitProperties.sendMessage(ChatColor.BLUE + "Will overlay over any block." + this.depth);
				} else if (parameter.startsWith("some")) {
					this.allBlocks = false;
					toolkitProperties.sendMessage(ChatColor.BLUE + "Will overlay only natural block types." + this.depth);
				} else if (!parameter.isEmpty() && parameter.charAt(0) == 's') {
					double temp = Integer.parseInt(parameter.replace("s", ""));
					if (temp >= SEED_PERCENT_MIN && temp <= SEED_PERCENT_MAX) {
						toolkitProperties.sendMessage(ChatColor.AQUA + "Seed percent set to: " + temp / 100 + "%");
						this.seedPercent = (int) temp;
					} else {
						toolkitProperties.sendMessage(ChatColor.RED + "Seed percent must be an integer 1-9999!");
					}
				} else if (!parameter.isEmpty() && parameter.charAt(0) == 'g') {
					double temp = Integer.parseInt(parameter.replace("g", ""));
					if (temp >= GROW_PERCENT_MIN && temp <= GROW_PERCENT_MAX) {
						toolkitProperties.sendMessage(ChatColor.AQUA + "Growth percent set to: " + temp / 100 + "%");
						this.growPercent = (int) temp;
					} else {
						toolkitProperties.sendMessage(ChatColor.RED + "Growth percent must be an integer 1-9999!");
					}
				} else if (parameter.startsWith("randh")) {
					this.randomizeHeight = !this.randomizeHeight;
					toolkitProperties.sendMessage(ChatColor.RED + "RandomizeHeight set to: " + this.randomizeHeight);
				} else if (!parameter.isEmpty() && parameter.charAt(0) == 'r') {
					int temp = Integer.parseInt(parameter.replace("r", ""));
					if (temp >= SPLATREC_PERCENT_MIN && temp <= SPLATREC_PERCENT_MAX) {
						toolkitProperties.sendMessage(ChatColor.AQUA + "Recursions set to: " + temp);
						this.splatterRecursions = temp;
					} else {
						toolkitProperties.sendMessage(ChatColor.RED + "Recursions must be an integer 1-10!");
					}
				} else if (parameter.startsWith("yoff")) {
					int temp = Integer.parseInt(parameter.replace("yoff", ""));
					if (temp >= SPLATREC_PERCENT_MIN && temp <= SPLATREC_PERCENT_MAX) {
						toolkitProperties.sendMessage(ChatColor.AQUA + "Y-Offset set to: " + temp);
						this.yOffset = temp;
					} else {
						toolkitProperties.sendMessage(ChatColor.RED + "Recursions must be an integer 1-10!");
					}
				} else {
					toolkitProperties.sendMessage(ChatColor.RED + "Invalid brush parameters! use the info parameter to display parameter info.");
				}
			} catch (NumberFormatException exception) {
				toolkitProperties.sendMessage(String.format("An error occured while processing parameter %s.", parameter));
			}
		}
	}

	@Override
	public String getPermissionNode() {
		return "voxelsniper.brush.splatteroverlay";
	}
}