package moze_intel.projecte.utils;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import moze_intel.projecte.PECore;
import moze_intel.projecte.api.PESounds;
import moze_intel.projecte.config.ProjectEConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoublePlantBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.block.ILiquidContainer;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.TNTBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

/**
 * Helper class for anything that touches a World. Notice: Please try to keep methods tidy and alphabetically ordered. Thanks!
 */
public final class WorldHelper {

	private static final Tag<Block> HARVEST_BLACKLIST = new BlockTags.Wrapper(new ResourceLocation(PECore.MODID, "harvest_blacklist"));
	private static Set<EntityType<?>> interdictionBlacklist = Collections.emptySet();
	private static Set<EntityType<?>> swrgBlacklist = Collections.emptySet();
	private static final Predicate<Entity> SWRG_REPEL_PREDICATE = entity -> !entity.isSpectator() && !swrgBlacklist.contains(entity.getType());
	private static final Predicate<Entity> INTERDICTION_REPEL_PREDICATE = entity -> !entity.isSpectator() && !interdictionBlacklist.contains(entity.getType());

	public static void setInterdictionBlacklist(Set<EntityType<?>> types) {
		interdictionBlacklist = ImmutableSet.copyOf(types);
	}

	public static void setSwrgBlacklist(Set<EntityType<?>> types) {
		swrgBlacklist = ImmutableSet.copyOf(types);
	}

	public static void createLootDrop(List<ItemStack> drops, World world, BlockPos pos) {
		createLootDrop(drops, world, pos.getX(), pos.getY(), pos.getZ());
	}

	public static void createLootDrop(List<ItemStack> drops, World world, double x, double y, double z) {
		if (!drops.isEmpty()) {
			ItemHelper.compactItemListNoStacksize(drops);
			for (ItemStack drop : drops) {
				ItemEntity ent = new ItemEntity(world, x, y, z);
				ent.setItem(drop);
				world.addEntity(ent);
			}
		}
	}

	/**
	 * Equivalent of World.newExplosion
	 */
	public static void createNovaExplosion(World world, Entity exploder, double x, double y, double z, float power) {
		NovaExplosion explosion = new NovaExplosion(world, exploder, x, y, z, power, true, Explosion.Mode.BREAK);
		if (!MinecraftForge.EVENT_BUS.post(new ExplosionEvent.Start(world, explosion))) {
			explosion.doExplosionA();
			explosion.doExplosionB(true);
		}
	}

	public static void dropInventory(IItemHandler inv, World world, BlockPos pos) {
		if (inv == null) {
			return;
		}
		for (int i = 0; i < inv.getSlots(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (!stack.isEmpty()) {
				ItemEntity ent = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ());
				ent.setItem(stack);
				world.addEntity(ent);
			}
		}
	}

	public static void extinguishNearby(World world, PlayerEntity player) {
		BlockPos.getAllInBox(new BlockPos(player).add(-1, -1, -1), new BlockPos(player).add(1, 1, 1)).forEach(pos -> {
			pos = pos.toImmutable();
			if (world.getBlockState(pos).getBlock() == Blocks.FIRE && PlayerHelper.hasBreakPermission((ServerPlayerEntity) player, pos)) {
				world.removeBlock(pos, false);
			}
		});
	}

	public static void freezeInBoundingBox(World world, AxisAlignedBB box, PlayerEntity player, boolean random) {
		for (BlockPos pos : getPositionsFromBox(box)) {
			BlockState state = world.getBlockState(pos);
			Block b = state.getBlock();
			//Ensure we are immutable so that changing blocks doesn't act weird
			pos = pos.toImmutable();
			if (b == Blocks.WATER && (!random || world.rand.nextInt(128) == 0)) {
				if (player != null) {
					PlayerHelper.checkedReplaceBlock((ServerPlayerEntity) player, pos, Blocks.ICE.getDefaultState());
				} else {
					world.setBlockState(pos, Blocks.ICE.getDefaultState());
				}
			} else if (Block.doesSideFillSquare(state.getCollisionShape(world, pos.down()), Direction.UP)) {
				BlockPos up = pos.up();
				BlockState stateUp = world.getBlockState(up);
				BlockState newState = null;

				if (stateUp.getBlock().isAir(stateUp, world, up) && (!random || world.rand.nextInt(128) == 0)) {
					newState = Blocks.SNOW.getDefaultState();
				} else if (stateUp.getBlock() == Blocks.SNOW && stateUp.get(SnowBlock.LAYERS) < 8 && world.rand.nextInt(512) == 0) {
					newState = stateUp.with(SnowBlock.LAYERS, stateUp.get(SnowBlock.LAYERS) + 1);
				}
				if (newState != null) {
					if (player != null) {
						PlayerHelper.checkedReplaceBlock((ServerPlayerEntity) player, up, newState);
					} else {
						world.setBlockState(up, newState);
					}
				}
			}
		}
	}

	/**
	 * Checks if a block is a {@link ILiquidContainer} that supports a specific fluid type.
	 */
	public static boolean isLiquidContainerForFluid(IBlockReader world, BlockPos pos, BlockState state, Fluid fluid) {
		return state.getBlock() instanceof ILiquidContainer && ((ILiquidContainer) state.getBlock()).canContainFluid(world, pos, state, fluid);
	}

	/**
	 * Attempts to place a fluid in a specific spot if the spot is a {@link ILiquidContainer} that supports the fluid otherwise try to place it in the block that is on
	 * the given side of the clicked block.
	 */
	public static void placeFluid(ServerPlayerEntity player, World world, BlockPos pos, Direction sideHit, FlowingFluid fluid, boolean checkWaterVaporize) {
		if (isLiquidContainerForFluid(world, pos, world.getBlockState(pos), fluid)) {
			//If the spot can be logged with our fluid then try using the position directly
			placeFluid(player, world, pos, fluid, checkWaterVaporize);
		} else {
			//Otherwise offset it because we clicked against the block
			placeFluid(player, world, pos.offset(sideHit), fluid, checkWaterVaporize);
		}
	}

	/**
	 * Attempts to place a fluid in a specific spot, if the spot is a {@link ILiquidContainer} that supports the fluid, insert it instead.
	 *
	 * @apiNote Call this from the server side
	 */
	public static void placeFluid(ServerPlayerEntity player, World world, BlockPos pos, FlowingFluid fluid, boolean checkWaterVaporize) {
		BlockState blockState = world.getBlockState(pos);
		if (checkWaterVaporize && world.dimension.doesWaterVaporize() && fluid.isIn(FluidTags.WATER)) {
			world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
			for (int l = 0; l < 8; ++l) {
				world.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
			}
		} else if (isLiquidContainerForFluid(world, pos, blockState, fluid)) {
			((ILiquidContainer) blockState.getBlock()).receiveFluid(world, pos, blockState, fluid.getStillFluidState(false));
		} else {
			Material material = blockState.getMaterial();
			if ((!material.isSolid() || material.isReplaceable()) && !material.isLiquid()) {
				world.destroyBlock(pos, true);
			}
			PlayerHelper.checkedPlaceBlock(player, pos, fluid.getDefaultState().getBlockState());
		}
	}

	/**
	 * Gets an ItemHandler of a specific tile from the given side. Falls back to using wrappers if the tile is an instance of an ISidedInventory/IInventory.
	 */
	@Nullable
	public static IItemHandler getItemHandler(@Nonnull TileEntity tile, @Nullable Direction direction) {
		Optional<IItemHandler> capability = LazyOptionalHelper.toOptional(tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction));
		if (capability.isPresent()) {
			return capability.get();
		} else if (tile instanceof ISidedInventory) {
			return new SidedInvWrapper((ISidedInventory) tile, direction);
		} else if (tile instanceof IInventory) {
			return new InvWrapper((IInventory) tile);
		}
		return null;
	}

	/**
	 * Gets an AABB for AOE digging operations. The offset increases both the breadth and depth of the box.
	 */
	public static AxisAlignedBB getBroadDeepBox(BlockPos pos, Direction direction, int offset) {
		switch (direction) {
			case EAST:
				return new AxisAlignedBB(pos.getX() - offset, pos.getY() - offset, pos.getZ() - offset, pos.getX(), pos.getY() + offset, pos.getZ() + offset);
			case WEST:
				return new AxisAlignedBB(pos.getX(), pos.getY() - offset, pos.getZ() - offset, pos.getX() + offset, pos.getY() + offset, pos.getZ() + offset);
			case UP:
				return new AxisAlignedBB(pos.getX() - offset, pos.getY() - offset, pos.getZ() - offset, pos.getX() + offset, pos.getY(), pos.getZ() + offset);
			case DOWN:
				return new AxisAlignedBB(pos.getX() - offset, pos.getY(), pos.getZ() - offset, pos.getX() + offset, pos.getY() + offset, pos.getZ() + offset);
			case SOUTH:
				return new AxisAlignedBB(pos.getX() - offset, pos.getY() - offset, pos.getZ() - offset, pos.getX() + offset, pos.getY() + offset, pos.getZ());
			case NORTH:
				return new AxisAlignedBB(pos.getX() - offset, pos.getY() - offset, pos.getZ(), pos.getX() + offset, pos.getY() + offset, pos.getZ() + offset);
			default:
				return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		}
	}

	/**
	 * Returns in AABB that is always 3x3 orthogonal to the side hit, but varies in depth in the direction of the side hit
	 */
	public static AxisAlignedBB getDeepBox(BlockPos pos, Direction direction, int depth) {
		switch (direction) {
			case EAST:
				return new AxisAlignedBB(pos.getX() - depth, pos.getY() - 1, pos.getZ() - 1, pos.getX(), pos.getY() + 1, pos.getZ() + 1);
			case WEST:
				return new AxisAlignedBB(pos.getX(), pos.getY() - 1, pos.getZ() - 1, pos.getX() + depth, pos.getY() + 1, pos.getZ() + 1);
			case UP:
				return new AxisAlignedBB(pos.getX() - 1, pos.getY() - depth, pos.getZ() - 1, pos.getX() + 1, pos.getY(), pos.getZ() + 1);
			case DOWN:
				return new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 1, pos.getY() + depth, pos.getZ() + 1);
			case SOUTH:
				return new AxisAlignedBB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - depth, pos.getX() + 1, pos.getY() + 1, pos.getZ());
			case NORTH:
				return new AxisAlignedBB(pos.getX() - 1, pos.getY() - 1, pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + depth);
			default:
				return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		}
	}

	/**
	 * Returns in AABB that is always a single block deep but is size x size orthogonal to the side hit
	 */
	public static AxisAlignedBB getBroadBox(BlockPos pos, Direction direction, int size) {
		switch (direction) {
			case EAST:
			case WEST:
				return new AxisAlignedBB(pos.getX(), pos.getY() - size, pos.getZ() - size, pos.getX(), pos.getY() + size, pos.getZ() + size);
			case UP:
			case DOWN:
				return new AxisAlignedBB(pos.getX() - size, pos.getY(), pos.getZ() - size, pos.getX() + size, pos.getY(), pos.getZ() + size);
			case SOUTH:
			case NORTH:
				return new AxisAlignedBB(pos.getX() - size, pos.getY() - size, pos.getZ(), pos.getX() + size, pos.getY() + size, pos.getZ());
			default:
				return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
		}
	}

	/**
	 * Gets an AABB for AOE digging operations. The charge increases only the breadth of the box. Y level remains constant. As such, a direction hit is unneeded.
	 */
	public static AxisAlignedBB getFlatYBox(BlockPos pos, int offset) {
		return new AxisAlignedBB(pos.getX() - offset, pos.getY(), pos.getZ() - offset, pos.getX() + offset, pos.getY(), pos.getZ() + offset);
	}

	/**
	 * Wrapper around BlockPos.getAllInBox() with an AABB Note that this is inclusive of all positions in the AABB!
	 */
	public static Iterable<BlockPos> getPositionsFromBox(AxisAlignedBB box) {
		return getPositionsFromBox(new BlockPos(box.minX, box.minY, box.minZ), new BlockPos(box.maxX, box.maxY, box.maxZ));
	}

	/**
	 * Wrapper around BlockPos.getAllInBox()
	 */
	public static Iterable<BlockPos> getPositionsFromBox(BlockPos corner1, BlockPos corner2) {
		return () -> BlockPos.getAllInBox(corner1, corner2).iterator();
	}


	public static List<TileEntity> getTileEntitiesWithinAABB(World world, AxisAlignedBB bBox) {
		List<TileEntity> list = new ArrayList<>();
		for (BlockPos pos : getPositionsFromBox(bBox)) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile != null) {
				list.add(tile);
			}
		}
		return list;
	}

	/**
	 * Gravitates an entity, vanilla xp orb style, towards a position Code adapted from EntityXPOrb and OpenBlocks Vacuum Hopper, mostly the former
	 */
	public static void gravitateEntityTowards(Entity ent, double x, double y, double z) {
		double dX = x - ent.getPosX();
		double dY = y - ent.getPosY();
		double dZ = z - ent.getPosZ();
		double dist = Math.sqrt(dX * dX + dY * dY + dZ * dZ);

		double vel = 1.0 - dist / 15.0;
		if (vel > 0.0D) {
			vel *= vel;
			ent.setMotion(ent.getMotion().add(dX / dist * vel * 0.1, dY / dist * vel * 0.2, dZ / dist * vel * 0.1));
		}
	}

	public static void growNearbyRandomly(boolean harvest, World world, BlockPos pos, PlayerEntity player) {
		if (!(world instanceof ServerWorld)) {
			return;
		}
		int chance = harvest ? 16 : 32;
		for (BlockPos currentPos : getPositionsFromBox(pos.add(-5, -3, -5), pos.add(5, 3, 5))) {
			currentPos = currentPos.toImmutable();
			BlockState state = world.getBlockState(currentPos);
			Block crop = state.getBlock();

			// Vines, leaves, tallgrass, deadbush, doubleplants
			if (crop instanceof IShearable) {
				if (harvest) {
					harvestBlock(world, currentPos, (ServerPlayerEntity) player);
				}
			}
			// Carrot, cocoa, wheat, grass (creates flowers and tall grass in vicinity),
			// Mushroom, potato, sapling, stems, tallgrass
			else if (crop instanceof IGrowable) {
				IGrowable growable = (IGrowable) crop;
				if (!growable.canGrow(world, currentPos, state, false)) {
					if (harvest && !crop.isIn(HARVEST_BLACKLIST)) {
						harvestBlock(world, currentPos, (ServerPlayerEntity) player);
					}
				} else if (crop != Blocks.GRASS_BLOCK || ProjectEConfig.server.items.harvBandGrass.get()) {
					if (world.rand.nextInt(chance) == 0) {
						growable.grow((ServerWorld) world, world.rand, currentPos, state);
					}
				}
			}
			// All modded
			// Cactus, Reeds, Netherwart, Flower
			else if (crop instanceof IPlantable) {
				if (world.rand.nextInt(chance / 4) == 0) {
					for (int i = 0; i < (harvest ? 8 : 4); i++) {
						state.randomTick((ServerWorld) world, currentPos, world.rand);
					}
				}
				if (harvest) {
					if (crop instanceof FlowerBlock || crop instanceof DoublePlantBlock) {
						//Handle double plant blocks that were not already handled due to being shearable
						harvestBlock(world, currentPos, (ServerPlayerEntity) player);
					} else if (crop == Blocks.SUGAR_CANE || crop == Blocks.CACTUS) {
						if (world.getBlockState(currentPos.up()).getBlock() == crop && world.getBlockState(currentPos.up(2)).getBlock() == crop) {
							for (int i = crop == Blocks.SUGAR_CANE ? 1 : 0; i < 3; i++) {
								harvestBlock(world, currentPos.up(i), (ServerPlayerEntity) player);
							}
						}
					} else if (crop == Blocks.NETHER_WART) {
						if (state.get(NetherWartBlock.AGE) == 3) {
							harvestBlock(world, currentPos, (ServerPlayerEntity) player);
						}
					}
				}
			}
		}
	}

	/**
	 * Breaks and "harvests" a block if the player has permission to break it or there is no player
	 */
	private static void harvestBlock(World world, BlockPos pos, @Nullable ServerPlayerEntity player) {
		if (player == null || PlayerHelper.hasBreakPermission(player, pos)) {
			world.destroyBlock(pos, true, player);
		}
	}

	/**
	 * Recursively mines out a vein of the given Block, starting from the provided coordinates
	 */
	public static int harvestVein(World world, PlayerEntity player, ItemStack stack, BlockPos pos, Block target, List<ItemStack> currentDrops, int numMined) {
		if (numMined >= Constants.MAX_VEIN_SIZE) {
			return numMined;
		}
		AxisAlignedBB b = new AxisAlignedBB(pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1, pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
		for (BlockPos currentPos : getPositionsFromBox(b)) {
			BlockState currentState = world.getBlockState(currentPos);
			if (currentState.getBlock() == target) {
				//Ensure we are immutable so that changing blocks doesn't act weird
				currentPos = currentPos.toImmutable();
				if (PlayerHelper.hasBreakPermission((ServerPlayerEntity) player, currentPos)) {
					numMined++;
					currentDrops.addAll(Block.getDrops(currentState, (ServerWorld) world, currentPos, world.getTileEntity(currentPos), player, stack));
					world.removeBlock(currentPos, false);
					numMined = harvestVein(world, player, stack, currentPos, target, currentDrops, numMined);
					if (numMined >= Constants.MAX_VEIN_SIZE) {
						break;
					}
				}
			}
		}
		return numMined;
	}

	public static void igniteNearby(World world, PlayerEntity player) {
		for (BlockPos pos : BlockPos.getAllInBoxMutable(new BlockPos(player).add(-8, -5, -8), new BlockPos(player).add(8, 5, 8))) {
			if (world.rand.nextInt(128) == 0 && world.isAirBlock(pos)) {
				PlayerHelper.checkedPlaceBlock((ServerPlayerEntity) player, pos.toImmutable(), Blocks.FIRE.getDefaultState());
			}
		}
	}

	/**
	 * Repels projectiles and mobs in the given AABB away from a given point
	 */
	public static void repelEntitiesInterdiction(World world, AxisAlignedBB effectBounds, double x, double y, double z) {
		Vec3d vec = new Vec3d(x, y, z);
		for (Entity ent : world.getEntitiesWithinAABB(Entity.class, effectBounds, INTERDICTION_REPEL_PREDICATE)) {
			if (ent instanceof MobEntity || ent instanceof IProjectile) {
				if (!ProjectEConfig.server.effects.interdictionMode.get() || ent instanceof IMob || ent instanceof IProjectile) {
					if (ent instanceof AbstractArrowEntity && ((AbstractArrowEntity) ent).onGround) {
						continue;
					}
					repelEntity(vec, ent);
				}
			}
		}
	}

	/**
	 * Repels projectiles and mobs in the given AABB away from a given player, if the player is not the thrower of the projectile
	 */
	public static void repelEntitiesSWRG(World world, AxisAlignedBB effectBounds, PlayerEntity player) {
		Vec3d playerVec = player.getPositionVec();
		for (Entity ent : world.getEntitiesWithinAABB(Entity.class, effectBounds, SWRG_REPEL_PREDICATE)) {
			if (ent instanceof MobEntity || ent instanceof IProjectile) {
				if (ent instanceof AbstractArrowEntity) {
					AbstractArrowEntity arrow = (AbstractArrowEntity) ent;
					if (arrow.onGround || player.getUniqueID().equals(arrow.shootingEntity)) {
						continue;
					}
				} else if (ent instanceof ThrowableEntity) {
					LivingEntity thrower = ((ThrowableEntity) ent).getThrower();
					//Note: Eventually we wouldn't have the check for if the world is remote and the thrower is null
					// it is needed to make sure it renders properly for when a player throws an ender pearl, or other throwable
					// but makes it so the client can't quite properly render it if we properly deflect another player's throwable
					if (world.isRemote() && thrower == null || thrower != null && player.getUniqueID().equals(thrower.getUniqueID())) {
						continue;
					}
				}
				repelEntity(playerVec, ent);
			}
		}
	}

	private static void repelEntity(Vec3d vec, Entity entity) {
		Vec3d t = new Vec3d(entity.getPosX(), entity.getPosY(), entity.getPosZ());
		Vec3d r = new Vec3d(t.x - vec.x, t.y - vec.y, t.z - vec.z);
		double distance = vec.distanceTo(t) + 0.1;
		entity.setMotion(entity.getMotion().add(r.scale(1 / 1.5 * 1 / distance)));
	}

	@Nonnull
	public static ActionResultType igniteTNT(ItemUseContext ctx) {
		World world = ctx.getWorld();
		BlockPos pos = ctx.getPos();
		Direction side = ctx.getFace();
		BlockState state = world.getBlockState(pos);
		if (state.isFlammable(world, pos, side)) {
			if (!world.isRemote && PlayerHelper.hasBreakPermission((ServerPlayerEntity) ctx.getPlayer(), pos)) {
				// Ignite the block
				state.catchFire(world, pos, side, ctx.getPlayer());
				if (state.getBlock() instanceof TNTBlock) {
					world.removeBlock(pos, false);
				}
				world.playSound(null, ctx.getPlayer().getPosX(), ctx.getPlayer().getPosY(), ctx.getPlayer().getPosZ(), PESounds.POWER, SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.PASS;
	}
}