package moze_intel.projecte.gameObjs.entity;

import javax.annotation.Nonnull;
import moze_intel.projecte.gameObjs.ObjHandler;
import moze_intel.projecte.gameObjs.items.ItemPE;
import moze_intel.projecte.utils.PlayerHelper;
import moze_intel.projecte.utils.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityLavaProjectile extends ThrowableEntity {

	public EntityLavaProjectile(EntityType<EntityLavaProjectile> type, World world) {
		super(type, world);
	}

	public EntityLavaProjectile(PlayerEntity entity, World world) {
		super(ObjHandler.LAVA_PROJECTILE, entity, world);
	}

	@Override
	protected void registerData() {
	}

	@Override
	public void tick() {
		super.tick();
		if (!world.isRemote) {
			if (ticksExisted > 400 || !world.isBlockPresent(getPosition())) {
				remove();
				return;
			}
			if (getThrower() instanceof ServerPlayerEntity) {
				ServerPlayerEntity player = (ServerPlayerEntity) getThrower();
				BlockPos.getAllInBox(getPosition().add(-3, -3, -3), getPosition().add(3, 3, 3)).forEach(pos -> {
					Block block = world.getBlockState(pos).getBlock();
					if (block == Blocks.WATER) {
						pos = pos.toImmutable();
						if (PlayerHelper.hasBreakPermission(player, pos)) {
							world.removeBlock(pos, false);
							world.playSound(null, pos, SoundEvents.ENTITY_BLAZE_BURN, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
						}
					}
				});
			}
			if (getPosY() > 128) {
				WorldInfo worldInfo = world.getWorldInfo();
				worldInfo.setRaining(false);
				remove();
			}
		}
	}

	@Override
	public float getGravityVelocity() {
		return 0;
	}

	@Override
	protected void onImpact(@Nonnull RayTraceResult mop) {
		if (world.isRemote) {
			return;
		}
		if (!(getThrower() instanceof PlayerEntity)) {
			remove();
			return;
		}
		PlayerEntity player = (PlayerEntity) getThrower();
		ItemStack found = PlayerHelper.findFirstItem(player, ObjHandler.volcanite);
		if (!found.isEmpty() && ItemPE.consumeFuel(player, found, 32, true)) {
			if (mop instanceof BlockRayTraceResult) {
				BlockRayTraceResult result = (BlockRayTraceResult) mop;
				WorldHelper.placeFluid((ServerPlayerEntity) player, world, result.getPos(), result.getFace(), Fluids.LAVA, false);
			} else if (mop instanceof EntityRayTraceResult) {
				Entity ent = ((EntityRayTraceResult) mop).getEntity();
				ent.setFire(5);
				ent.attackEntityFrom(DamageSource.IN_FIRE, 5);
			}
		}
		remove();
	}

	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}