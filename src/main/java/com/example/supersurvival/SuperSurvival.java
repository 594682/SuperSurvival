package com.example.supersurvival;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.level.levelgen.Heightmap;
import java.util.function.Supplier;

@Mod(SuperSurvival.ID)
public class SuperSurvival {
    public static final String ID = "supersurvival";
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final RegistryObject<Block> PORTAL = BLOCKS.register("portal_block",
        () -> new Block(BlockBehaviour.Properties.copy(Blocks.OBSIDIAN).lightLevel(s -> 12)));
    public static final RegistryObject<Item> PORTAL_ITEM = ITEMS.register("portal_block",
        () -> new BlockItem(PORTAL.get(), new Item.Properties()));
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(ID, "control"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    public SuperSurvival() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus); ITEMS.register(bus);
        bus.addListener(this::tab);
        MinecraftForge.EVENT_BUS.register(this);
        CHANNEL.registerMessage(0, Control.class, Control::encode, Control::decode, Control::handle);
        CHANNEL.registerMessage(1, CheatUnlock.class, CheatUnlock::encode, CheatUnlock::decode, CheatUnlock::handle);
    }
    private void tab(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) e.accept(PORTAL_ITEM);
    }

    // Client sends key state; the server validates the actual mount before applying movement.
    public record Control(float forward, float strafe, boolean up, boolean down) {
        public static void encode(Control m, net.minecraft.network.FriendlyByteBuf b) {
            b.writeFloat(m.forward); b.writeFloat(m.strafe); b.writeBoolean(m.up); b.writeBoolean(m.down);
        }
        public static Control decode(net.minecraft.network.FriendlyByteBuf b) {
            return new Control(b.readFloat(), b.readFloat(), b.readBoolean(), b.readBoolean());
        }
        public static void handle(Control m, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer p = ctx.get().getSender();
                if (p == null) return;
                Entity mount = p.getVehicle();
                if (!(mount instanceof AbstractMinecart) && mount != null && mount.getType() != EntityType.ENDER_DRAGON) return;
                if (mount == null) return;
                float f = Math.max(-1, Math.min(1, m.forward));
                float s = Math.max(-1, Math.min(1, m.strafe));
                double yaw = Math.toRadians(p.getYRot());
                double dx = -Math.sin(yaw)*f + Math.cos(yaw)*s;
                double dz = Math.cos(yaw)*f + Math.sin(yaw)*s;
                double dy = m.up ? 0.48 : (m.down ? -0.48 : 0);
                Vec3 v = new Vec3(dx*0.48, dy, dz*0.48);
                mount.setDeltaMovement(v);
                mount.hasImpulse = true;
                mount.fallDistance = 0;
                mount.setNoGravity(true);
            });
            ctx.get().setPacketHandled(true);
        }
    }

    public record CheatUnlock() {
        public static void encode(CheatUnlock m, net.minecraft.network.FriendlyByteBuf b) {}
        public static CheatUnlock decode(net.minecraft.network.FriendlyByteBuf b) { return new CheatUnlock(); }
        public static void handle(CheatUnlock m, Supplier<NetworkEvent.Context> context) {
            context.get().enqueueWork(() -> {
                ServerPlayer player = context.get().getSender();
                if (player == null) return;
                // Only allow this powerful shortcut in an integrated singleplayer server.
                if (!player.server.isSingleplayerOwner(player.getGameProfile())) return;
                player.server.getPlayerList().op(player.getGameProfile());
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§aLLL: 已启用作弊命令权限，游戏模式保持不变。"));
            });
            context.get().setPacketHandled(true);
        }
    }

    @SubscribeEvent
    public void onInteract(PlayerInteractEvent.RightClickItem e) {
        if (e.getLevel().isClientSide()) return;
        if (!e.getItemStack().is(Items.BUCKET)) return;
        Level level = e.getLevel();
        long day = level.getDayTime() % 24000L;
        // Looking almost straight upward during daytime, with direct sky visibility.
        if (day >= 0 && day < 12000 && e.getEntity().getXRot() < -70 &&
            level.canSeeSky(e.getEntity().blockPosition().above())) {
            ItemStack emptyBuckets = e.getItemStack();
            if (emptyBuckets.getCount() == 1) {
                e.getEntity().setItemInHand(e.getHand(), new ItemStack(Items.LAVA_BUCKET));
            } else {
                emptyBuckets.shrink(1);
                e.getEntity().getInventory().placeItemBackInInventory(new ItemStack(Items.LAVA_BUCKET));
            }
            e.setCancellationResult(InteractionResult.SUCCESS);
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBlock(PlayerInteractEvent.RightClickBlock e) {
        if (e.getLevel().isClientSide() || e.getHand() != InteractionHand.MAIN_HAND) return;
        if (!e.getLevel().getBlockState(e.getPos()).is(PORTAL.get())) return;
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        ServerLevel target = p.server.getLevel(p.serverLevel().dimension() == Level.NETHER ? Level.OVERWORLD : Level.NETHER);
        if (target != null) {
            BlockPos pos = target.getSharedSpawnPos();
            // Safe fallback: build a platform and clear headroom at the destination.
            BlockPos landing = new BlockPos(pos.getX(), Math.max(80, pos.getY()+3), pos.getZ());
            target.setBlockAndUpdate(landing.below(), Blocks.OBSIDIAN.defaultBlockState());
            target.setBlockAndUpdate(landing, Blocks.AIR.defaultBlockState());
            target.setBlockAndUpdate(landing.above(), Blocks.AIR.defaultBlockState());
            p.teleportTo(target, landing.getX()+0.5, landing.getY(), landing.getZ()+0.5, p.getYRot(), p.getXRot());
        }
        e.setCancellationResult(InteractionResult.SUCCESS); e.setCanceled(true);
    }

    @SubscribeEvent
    public void onAttack(LivingAttackEvent e) {
        if (!(e.getSource().getEntity() instanceof ServerPlayer p)) return;
        if (p.level().isClientSide()) return;
        if (e.getEntity() instanceof Villager v) {
            Trades.install(v);
            e.setCanceled(true);
        } else if (e.getEntity().getType() == EntityType.ENDER_DRAGON &&
                   p.getMainHandItem().is(Items.END_ROD)) {
            p.startRiding(e.getEntity(), true);
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        if (p.serverLevel().dimension() != Level.OVERWORLD) return;
        ServerLevel level = p.serverLevel();
        level.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY).set(true, level.getServer());
        // World-wide marker prevents repeated villages; constructed settlement near first login.
        VillageMarker data = level.getDataStorage().computeIfAbsent(
            VillageMarker::load, VillageMarker::new, "supersurvival_spawn_village");
        if (!data.created) {
            BlockPos center = level.getSharedSpawnPos().offset(24,0,24);
            for (int i=0;i<4;i++) {
                int x = center.getX()+(i%2)*14, z = center.getZ()+(i/2)*14;
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x,z);
                BlockPos base = new BlockPos(x,y,z);
                for(int a=-3;a<=3;a++) for(int b=-3;b<=3;b++) {
                    level.setBlockAndUpdate(base.offset(a,-1,b), Blocks.OAK_PLANKS.defaultBlockState());
                    for(int h=0;h<=3;h++) {
                        BlockPos at=base.offset(a,h,b);
                        boolean wall=(Math.abs(a)==3||Math.abs(b)==3);
                        level.setBlockAndUpdate(at, wall ? Blocks.OAK_PLANKS.defaultBlockState() : Blocks.AIR.defaultBlockState());
                    }
                    level.setBlockAndUpdate(base.offset(a,4,b), Blocks.OAK_PLANKS.defaultBlockState());
                }
                level.setBlockAndUpdate(base.offset(0,0,-3), Blocks.AIR.defaultBlockState());
                level.setBlockAndUpdate(base.offset(0,1,-3), Blocks.AIR.defaultBlockState());
                for (int bedX : new int[]{-1, 1}) {
                    BlockState foot = Blocks.RED_BED.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.BedBlock.FACING, Direction.SOUTH)
                        .setValue(net.minecraft.world.level.block.BedBlock.PART,
                            net.minecraft.world.level.block.state.properties.BedPart.FOOT);
                    BlockState head = foot.setValue(net.minecraft.world.level.block.BedBlock.PART,
                        net.minecraft.world.level.block.state.properties.BedPart.HEAD);
                    level.setBlockAndUpdate(base.offset(bedX, 0, 0), foot);
                    level.setBlockAndUpdate(base.offset(bedX, 0, 1), head);
                }
                level.setBlockAndUpdate(base.offset(2,0,2), Blocks.LECTERN.defaultBlockState());
                for(int n=0;n<2;n++) {
                    Villager villager=EntityType.VILLAGER.create(level);
                    if(villager!=null) {
                        villager.moveTo(x+n, y+1, z, 0, 0);
                        level.addFreshEntity(villager);
                    }
                }
            }
            data.created = true;
            data.setDirty();
        }
    }
    public static class VillageMarker extends net.minecraft.world.level.saveddata.SavedData {
        boolean created=false;
        static VillageMarker load(CompoundTag tag) {
            VillageMarker m=new VillageMarker(); m.created=tag.getBoolean("created"); return m;
        }
        @Override public CompoundTag save(CompoundTag tag) { tag.putBoolean("created",created); return tag; }
    }
}
