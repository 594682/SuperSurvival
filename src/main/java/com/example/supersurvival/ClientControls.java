package com.example.supersurvival;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=SuperSurvival.ID, value=Dist.CLIENT)
public class ClientControls {
    private static int lPresses = 0;
    private static long lastL = 0L;
    @SubscribeEvent
    public static void onKey(net.minecraftforge.client.event.InputEvent.Key e) {
        if (e.getKey() != org.lwjgl.glfw.GLFW.GLFW_KEY_L ||
            e.getAction() != org.lwjgl.glfw.GLFW.GLFW_PRESS) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        long now = System.currentTimeMillis();
        lPresses = (now - lastL <= 1500L) ? lPresses + 1 : 1;
        lastL = now;
        if (lPresses >= 3) {
            lPresses = 0;
            SuperSurvival.CHANNEL.sendToServer(new SuperSurvival.CheatUnlock());
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc=Minecraft.getInstance();
        if (mc.player==null || mc.level==null) return;
        var mount=mc.player.getVehicle();
        if (!(mount instanceof AbstractMinecart) && (mount==null || mount.getType()!=EntityType.ENDER_DRAGON)) return;
        float f=(mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0);
        float s=(mc.options.keyRight.isDown()?1:0)-(mc.options.keyLeft.isDown()?1:0);
        SuperSurvival.CHANNEL.sendToServer(new SuperSurvival.Control(f,s,
            mc.options.keyJump.isDown(),mc.options.keyShift.isDown()));
    }
}
