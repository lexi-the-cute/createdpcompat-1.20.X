package net.misigno.createdpcompat.mixin;

import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.trains.CubeParticleData;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.content.trains.track.TrackShape;
import com.simibubi.create.foundation.utility.BlockFace;
import com.simibubi.create.foundation.utility.Pair;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.misigno.createdpcompat.CreateDPCompat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.simibubi.create.content.trains.track.TrackBlock.SHAPE;
import static net.kyrptonaught.customportalapi.util.ColorUtil.getColorForBlock;
import static net.kyrptonaught.customportalapi.util.CustomTeleporter.customTPTarget;
import static net.kyrptonaught.customportalapi.util.CustomTeleporter.wrapRegistryKey;


@Mixin(value = TrackBlock.class)
public abstract class CreateDPCompatMixin {


	@Shadow @Final public static EnumProperty<TrackShape> SHAPE;
	ServerLevel level;
	BlockFace inboundTrack;
	BlockPos portalPos;
	BlockState portalState;
	PortalLink link;
	ServerLevel otherLevel;


	@Inject(at = @At(value = "HEAD"), method = "getOtherSide")
	private void getOtherSideData(ServerLevel level, BlockFace inboundTrack, CallbackInfoReturnable<Pair<ServerLevel, BlockFace>> cir) {
		this.level = level;
		this.inboundTrack = inboundTrack;
		this.portalPos = inboundTrack.getConnectedPos();
		this.portalState = level.getBlockState(portalPos);
		if (portalState.getBlock() instanceof CustomPortalBlock) {
			this.link = CustomPortalApiRegistry.getPortalLinkFromBase(((CustomPortalBlock) portalState.getBlock()).getPortalBase(level, portalPos));
			this.otherLevel = level.getServer().getLevel(wrapRegistryKey(this.link.dimID) == level.dimension() ? wrapRegistryKey(this.link.returnDimID) : wrapRegistryKey(this.link.dimID));
		}
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;" ,ordinal = 0), method = "connectToNether")
	private Block isNetherPortalBlock(BlockState instance) {

		if (instance.getBlock() instanceof NetherPortalBlock || instance.getBlock() instanceof CustomPortalBlock)
			return Blocks.NETHER_PORTAL;
		return instance.getBlock();
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"), method = "getOtherSide")
	private Block isNetherPortalBlock2(BlockState instance) {
		if (instance.getBlock() instanceof NetherPortalBlock || instance.getBlock() instanceof CustomPortalBlock)
			return Blocks.NETHER_PORTAL;
		return instance.getBlock();
	}

	@ModifyVariable(method = "getOtherSide", at = @At("STORE"),ordinal = 0,index = 6)
	private ResourceKey<Level> getCustomDim(ResourceKey<Level> value) {
		if (this.portalState.getBlock() instanceof NetherPortalBlock) {
			return level.dimension()== Level.NETHER ? Level.OVERWORLD : Level.NETHER;
		}
		return wrapRegistryKey(this.link.dimID) == level.dimension() ? wrapRegistryKey(this.link.returnDimID) : wrapRegistryKey(this.link.dimID);
	}

	Entity probe;
	Block frameBlock;

	@Inject(at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/glue/SuperGlueEntity;findDimensionEntryPoint(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/level/portal/PortalInfo;",shift = At.Shift.BEFORE), method = "getOtherSide",locals = LocalCapture.CAPTURE_FAILHARD)
	private void getPortalData(ServerLevel level, BlockFace inboundTrack, CallbackInfoReturnable<Pair<ServerLevel, BlockFace>> cir, BlockPos portalPos, BlockState portalState, MinecraftServer minecraftserver, ResourceKey resourcekey, ServerLevel otherLevel, PortalForcer teleporter, SuperGlueEntity probe) {
		if (portalState.getBlock() instanceof NetherPortalBlock)
			return;
		this.probe = probe;
		this.frameBlock = ((CustomPortalBlock) portalState.getBlock()).getPortalBase(level, portalPos);
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/glue/SuperGlueEntity;findDimensionEntryPoint(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/level/portal/PortalInfo;"), method = "getOtherSide")
	private PortalInfo getPortalDest(SuperGlueEntity instance, ServerLevel pDestination){
		if (this.portalState.getBlock() instanceof NetherPortalBlock)
			return instance.findDimensionEntryPoint(pDestination);
		return customTPTarget(otherLevel,probe,portalPos,frameBlock,link.getFrameTester());
	}

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"), method = "getOtherSide")
	private Comparable getPortalAxisProperty(BlockState instance, Property property){
		if(this.portalState.getBlock() instanceof NetherPortalBlock)
			return instance.getValue(NetherPortalBlock.AXIS);
		return instance.getValue(CustomPortalBlock.AXIS);
	}

	@Inject(at = @At(value = "HEAD"),method = "animateTick",cancellable = true)
	private void newAnimateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRand, CallbackInfo ci){
		if(pState.getValue(SHAPE).isPortal()) {
			float f[];
			BlockPos portalPos = pPos;
			switch (pState.getValue(SHAPE)){
				case TN -> {
					portalPos=portalPos.north();
				}
				case TE -> {
					portalPos=portalPos.east();
				}
				case TS -> {
					portalPos=portalPos.south();
				}
				case TW -> {
					portalPos=portalPos.west();
				}
			}
			BlockState portalState = pLevel.getBlockState(portalPos);
			PortalLink link = null;
			if(portalState.getBlock() instanceof CustomPortalBlock)
				link = CustomPortalApiRegistry.getPortalLinkFromBase(((CustomPortalBlock) portalState.getBlock()).getPortalBase(pLevel, portalPos));
			if(link != null){
				f = getColorForBlock(link.colorID);
				int rand = pRand.nextIntBetweenInclusive((int) (Math.min(Math.min(f[0],f[1]),f[2])*-1000), (int) (1000-(Math.max(Math.max(f[0],f[1]),f[2]))*1000)%500);
				float tint = rand/1000.00f;
				Vec3 v = Vec3.atLowerCornerOf(pPos)
						.subtract(.125f, 0, .125f);
				CubeParticleData data =
						new CubeParticleData(f[0]+tint, f[1]+tint, f[2]+tint, .0125f + .0625f * pRand.nextFloat(), 30, false);
				pLevel.addParticle(data, v.x + pRand.nextFloat() * 1.5f, v.y + .25f, v.z + pRand.nextFloat() * 1.5f, 0.0D,
						0.04D, 0.0D);
				ci.cancel();
			}
		}
	}
}
