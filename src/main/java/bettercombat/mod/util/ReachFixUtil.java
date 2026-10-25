package bettercombat.mod.util;

import meldexun.reachfix.config.ReachFixConfig;
import meldexun.reachfix.hook.client.EntityRendererHook;
import meldexun.reachfix.util.BoundingBoxUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

/**
 * Modified code based on ReachFix
 * Credit to https://github.com/Meldexun/ReachFix
 */
public abstract class ReachFixUtil {

	/**
	 * Range validator based on meldexun.reachfix.hook.NetHandlerPlayServerHook
	 */
	public static boolean isEntityInRange(EntityPlayer player, Entity entity, EnumHand hand) {
		AxisAlignedBB aabb = BoundingBoxUtil.getInteractionBoundingBox(entity, 1.0F);
		double distanceSq = BoundingBoxUtil.distanceSq(aabb, player.getPositionEyes(1.0F));
		double reach = meldexun.reachfix.util.ReachFixUtil.getEntityReach(player, hand) + 1.0D;
		return distanceSq < reach * reach;
	}
	
	/**
	 * Directly return ReachFix's normal pointedObject value
	 */
	public static RayTraceResult pointedObject(Entity viewEntity, EntityPlayer player, EnumHand hand, World world, float partialTicks) {
		return EntityRendererHook.pointedObject(viewEntity, player, hand, world, partialTicks);
	}
	
	/**
	 * Identical to ReachFix's pointedObject, except ignoring blocks without bounding boxes
	 */
	public static RayTraceResult pointedObjectIgnorePassable(Entity viewEntity, EntityPlayer player, EnumHand hand, World world, float partialTicks) {
		Vec3d start = viewEntity.getPositionEyes(partialTicks);
		Vec3d look = viewEntity.getLook(partialTicks);
		double blockReach = meldexun.reachfix.util.ReachFixUtil.getBlockReach(player, hand);
		double entityReach = meldexun.reachfix.util.ReachFixUtil.getEntityReach(player, hand);
		Vec3d end = start.add(look.scale(Math.max(blockReach, entityReach)));
		RayTraceResult pointedBlock = world.rayTraceBlocks(start, end, false, true, false);
		RayTraceResult pointedEntity = getPointedEntity(viewEntity, world, start, end, partialTicks);
		if(!isNullOrMiss(pointedBlock)) {
			if(!isNullOrMiss(pointedEntity)) {
				double distBlock = start.squareDistanceTo(pointedBlock.hitVec);
				double distEntity = start.squareDistanceTo(pointedEntity.hitVec);
				if(distBlock < distEntity) {
					if(distBlock < blockReach * blockReach) {
						return pointedBlock;
					}
				}
				else if(distEntity < entityReach * entityReach) {
					return pointedEntity;
				}
			}
			else if(start.squareDistanceTo(pointedBlock.hitVec) < blockReach * blockReach) {
				return pointedBlock;
			}
		}
		else if(!isNullOrMiss(pointedEntity) && start.squareDistanceTo(pointedEntity.hitVec) < entityReach * entityReach) {
			return pointedEntity;
		}
		
		return new RayTraceResult(RayTraceResult.Type.MISS, end, (EnumFacing)null, new BlockPos(end));
	}
	
	/**
	 * Identical to ReachFix's getPointedEntity, copied out for accessibility
	 */
	@Nullable
	private static RayTraceResult getPointedEntity(Entity viewEntity, World world, Vec3d start, Vec3d end, float partialTicks) {
		AxisAlignedBB aabb = (new AxisAlignedBB(start, end)).grow(1.0);
		Entity lowestRidingEntity = viewEntity.getLowestRidingEntity();
		List<Entity> possibleEntities = world.getEntitiesInAABBexcluding(viewEntity, aabb, (entityx) -> {
			return !EntitySelectors.NOT_SPECTATING.apply(entityx) ? false : entityx.canBeCollidedWith();
		});
		RayTraceResult result = null;
		Entity pointedEntity = null;
		double min = Double.MAX_VALUE;
		Iterator var12 = possibleEntities.iterator();
		
		Entity entity;
		AxisAlignedBB entityAabb;
		label37:
		do {
			while(var12.hasNext()) {
				entity = (Entity)var12.next();
				entityAabb = BoundingBoxUtil.getInteractionBoundingBox(entity, partialTicks);
				if (lowestRidingEntity == entity.getLowestRidingEntity() && !entity.canRiderInteract()) {
					continue label37;
				}
				
				if (entityAabb.contains(start)) {
					return new RayTraceResult(entity, start);
				}
				
				RayTraceResult rtr = entityAabb.calculateIntercept(start, end);
				if (!isNullOrMiss(rtr)) {
					double dist = start.squareDistanceTo(rtr.hitVec);
					if (dist < min) {
						result = rtr;
						pointedEntity = entity;
						min = dist;
					}
				}
			}
			
			if (isNullOrMiss(result)) {
				return null;
			}
			
			return new RayTraceResult(pointedEntity, result.hitVec);
		} while(!ReachFixConfig.getInstance().forceInteractionInsideVehicles || !entityAabb.contains(start));
		
		return new RayTraceResult(entity, start);
	}
	
	/**
	 * Identical to ReachFix's isNullOrMiss, copied out for accessibility
	 */
	private static boolean isNullOrMiss(RayTraceResult rayTraceResult) {
		return rayTraceResult == null || rayTraceResult.typeOfHit == RayTraceResult.Type.MISS;
	}
}