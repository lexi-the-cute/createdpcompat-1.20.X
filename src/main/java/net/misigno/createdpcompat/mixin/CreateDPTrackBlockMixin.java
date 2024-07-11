package net.misigno.createdpcompat.mixin;

import com.simibubi.create.content.trains.CubeParticleData;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackBlockEntity;
import com.simibubi.create.content.trains.track.TrackShape;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.misigno.createdpcompat.CreateDPCompat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.simibubi.create.content.trains.track.TrackBlock.SHAPE;
import static net.kyrptonaught.customportalapi.util.ColorUtil.getColorForBlock;

@Mixin(value = TrackBlock.class)
public abstract class CreateDPTrackBlockMixin {
	@Shadow @Final public static EnumProperty<TrackShape> SHAPE;

	@Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;", ordinal = 0), method = "connectToPortal")
	private Block isNetherPortalBlock(BlockState instance) {

		if (instance.getBlock() instanceof NetherPortalBlock || instance.getBlock() instanceof CustomPortalBlock)
			return Blocks.NETHER_PORTAL;
		return instance.getBlock();
	}

	@Inject(at = @At(value = "HEAD"), method = "animateTick", cancellable = true)
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
