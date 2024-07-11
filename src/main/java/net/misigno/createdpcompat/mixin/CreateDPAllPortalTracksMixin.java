// Sponge
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

// Minecraft
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.portal.PortalForcer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.Entity;
import com.simibubi.create.foundation.utility.Pair;
import net.minecraft.world.level.portal.PortalInfo;

// Create
import com.simibubi.create.content.contraptions.glue.SuperGlueEntity;
import com.simibubi.create.content.trains.track.AllPortalTracks;
import com.simibubi.create.foundation.utility.BlockFace;

// CustomPortalAPI
import static net.kyrptonaught.customportalapi.util.CustomTeleporter.customTPTarget;
import static net.kyrptonaught.customportalapi.util.CustomTeleporter.wrapRegistryKey;
import net.kyrptonaught.customportalapi.util.PortalLink;

@Mixin(value = AllPortalTracks.class)
public abstract class CreateDPAllPortalTracksMixin {
	ServerLevel level;
	BlockFace inboundTrack;
	BlockPos portalPos;
	BlockState portalState;
	PortalLink link;
	ServerLevel otherLevel;

	Entity probe;
	Block frameBlock;


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

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"), method = "getOtherSide")
	private Block isNetherPortalBlock2(BlockState instance) {
		if (instance.getBlock() instanceof NetherPortalBlock || instance.getBlock() instanceof CustomPortalBlock)
			return Blocks.NETHER_PORTAL;
		return instance.getBlock();
	}

	@ModifyVariable(method = "getOtherSide", at = @At("STORE"), ordinal = 0, index = 6)
	private ResourceKey<Level> getCustomDim(ResourceKey<Level> value) {
		if (this.portalState.getBlock() instanceof NetherPortalBlock) {
			return level.dimension()== Level.NETHER ? Level.OVERWORLD : Level.NETHER;
		}
		return wrapRegistryKey(this.link.dimID) == level.dimension() ? wrapRegistryKey(this.link.returnDimID) : wrapRegistryKey(this.link.dimID);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/contraptions/glue/SuperGlueEntity;findDimensionEntryPoint(Lnet/minecraft/server/level/ServerLevel;)Lnet/minecraft/world/level/portal/PortalInfo;", shift = At.Shift.BEFORE), method = "getOtherSide", locals = LocalCapture.CAPTURE_FAILHARD)
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
}