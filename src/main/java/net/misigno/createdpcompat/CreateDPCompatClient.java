package net.misigno.createdpcompat;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.trains.track.TrackBlock;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import com.simibubi.create.Create;
import io.github.fabricators_of_create.porting_lib.util.EnvExecutor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.kyrptonaught.customportalapi.CustomPortalApiRegistry;
import net.kyrptonaught.customportalapi.CustomPortalBlock;
import net.kyrptonaught.customportalapi.util.PortalLink;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.simibubi.create.content.trains.track.TrackBlock.SHAPE;


public class CreateDPCompatClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
	/*	ColorProviderRegistry.BLOCK.register((blockState, blockAndTintGetter, blockPos, tintIndex) -> {
			BlockPos portalPos = blockPos;
			if(blockState.getValue(SHAPE).isPortal()) {
				switch (blockState.getValue(SHAPE)) {
					case TN -> {
						portalPos = portalPos.north();
					}
					case TE -> {
						portalPos = portalPos.east();
					}
					case TS -> {
						portalPos = portalPos.south();
					}
					case TW -> {
						portalPos = portalPos.west();
					}
				}
				BlockState portalState = Minecraft.getInstance().level.getBlockState(portalPos);
				PortalLink link = null;
				if(portalState.getBlock() instanceof CustomPortalBlock)
					link = CustomPortalApiRegistry.getPortalLinkFromBase(((CustomPortalBlock) portalState.getBlock()).getPortalBase(Minecraft.getInstance().level, portalPos));
				if(link != null){
					return link.colorID;
				}
				if(portalState.getBlock() instanceof NetherPortalBlock)
					return 0xf0a0f0;
			}
			return 0;
		}, AllBlocks.TRACK.get());
		BlockRenderLayerMap.INSTANCE.putBlock(AllBlocks.TRACK.get(),RenderType.translucent());
		*/
	}
}

