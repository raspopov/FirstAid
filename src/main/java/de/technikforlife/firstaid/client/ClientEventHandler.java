package de.technikforlife.firstaid.client;

import de.technikforlife.firstaid.FirstAid;
import de.technikforlife.firstaid.FirstAidConfig;
import de.technikforlife.firstaid.client.tutorial.GuiTutorial;
import de.technikforlife.firstaid.damagesystem.PlayerDamageModel;
import de.technikforlife.firstaid.damagesystem.capability.CapabilityExtendedHealthSystem;
import de.technikforlife.firstaid.items.FirstAidItems;
import de.technikforlife.firstaid.network.MessageGetDamageInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

@SideOnly(Side.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.BANDAGE, 0, new ModelResourceLocation("firstaid:bandage"));
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.PLASTER, 0, new ModelResourceLocation("firstaid:plaster"));
        ModelLoader.setCustomModelResourceLocation(FirstAidItems.MORPHINE, 0, new ModelResourceLocation("firstaid:morphine"));
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entityLiving = event.getEntityLiving();
        if (entityLiving instanceof EntityPlayer) {
            if (entityLiving.getName().equals(Minecraft.getMinecraft().player.getName()) && GuiApplyHealthItem.isOpen)
                Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.KeyInputEvent event) {
        if (ClientProxy.showWounds.isPressed()) {
            if (!FirstAidConfig.hasTutorial) {
                FirstAidConfig.hasTutorial = true;
                ConfigManager.sync(FirstAid.MODID, Config.Type.INSTANCE);
                Minecraft.getMinecraft().displayGuiScreen(new GuiTutorial());
            }
            else {
                FirstAid.proxy.showGuiApplyHealth();
                FirstAid.NETWORKING.sendToServer(new MessageGetDamageInfo());
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerConnect(PlayerEvent.PlayerLoggedInEvent event) {
        if (!FirstAidConfig.hasTutorial)
            event.player.sendMessage(new TextComponentString("[First Aid] Press " + ClientProxy.showWounds.getDisplayName() + " for the tutorial"));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side == Side.CLIENT && GuiApplyHealthItem.INSTANCE != null && GuiApplyHealthItem.INSTANCE.hasData && !event.player.isCreative())
            GuiApplyHealthItem.INSTANCE.damageModel.tick(event.player.world, event.player, true);
        if (event.phase == TickEvent.Phase.END && event.side == Side.CLIENT)
            GuiIngameForge.renderHealth = false;
    }
}