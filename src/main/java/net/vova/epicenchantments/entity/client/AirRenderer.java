package net.vova.epicenchantments.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.vova.epicenchantments.EpicEnchantments;
import net.vova.epicenchantments.entity.custom.AirEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class AirRenderer extends MobRenderer<AirEntity, AirModel<AirEntity>> {
    public AirRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new AirModel<>(pContext.bakeLayer(ModModelLayers.AIR_LAYER)), 2f);
    }

    @Override
    public ResourceLocation getTextureLocation(AirEntity pEntity) {
        return new ResourceLocation(EpicEnchantments.MODID, "textures/entity/rhino.png");
    }

    @Override
    public void render(AirEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        if(pEntity.isBaby()) {
            pMatrixStack.scale(0.5f, 0.5f, 0.5f);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}
