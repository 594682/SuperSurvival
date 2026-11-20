package com.example.supersurvival;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class Trades {
    record Ench(String name,int level) {}

    static void add(MerchantOffers v,ItemStack out) {
        v.add(new MerchantOffer(new ItemStack(Items.DIRT),out,9999999,0,0f));
    }
    static void addGear(MerchantOffers v,Item item,Ench... enchants) {
        ItemStack stack=new ItemStack(item);
        CompoundTag tag=stack.getOrCreateTag();
        tag.putBoolean("Unbreakable",true);
        ListTag list=new ListTag();
        for(Ench en:enchants) {
            CompoundTag t=new CompoundTag();
            t.putString("id","minecraft:"+en.name());
            t.putShort("lvl",(short)en.level());
            list.add(t);
        }
        tag.put("Enchantments",list);
        add(v,stack);
    }
    static void addBulk(MerchantOffers v,Item item) {
        ItemStack stack=new ItemStack(item, Math.min(67, item.getDefaultInstance().getMaxStackSize()));
        add(v,stack);
    }
    static void addPotion(MerchantOffers v,String id) {
        ItemStack stack=new ItemStack(Items.SPLASH_POTION, Items.SPLASH_POTION.getDefaultInstance().getMaxStackSize());
        stack.getOrCreateTag().putString("Potion","minecraft:"+id);
        add(v,stack);
    }
    static void addBook(MerchantOffers v) {
        ItemStack stack=new ItemStack(Items.ENCHANTED_BOOK, Items.ENCHANTED_BOOK.getDefaultInstance().getMaxStackSize());
        ListTag list=new ListTag();
        for(String id:BOOK_ENCHANTS) {
            CompoundTag t=new CompoundTag();
            t.putString("id","minecraft:"+id); t.putShort("lvl",(short)255);
            list.add(t);
        }
        stack.getOrCreateTag().put("StoredEnchantments",list);
        add(v,stack);
    }
    private static final String[] BOOK_ENCHANTS = new String[] {
        "aqua_affinity", "bane_of_arthropods", "blast_protection", "channeling", "depth_strider", "efficiency", "feather_falling", "fire_aspect", "fire_protection", "flame", "knockback", "looting", "loyalty", "luck_of_the_sea", "lure", "mending", "multishot", "power", "projectile_protection", "protection", "punch", "quick_charge", "respiration", "sharpness", "silk_touch", "smite", "soul_speed", "sweeping", "swift_sneak", "thorns", "unbreaking"
    };
    static void install(Villager villager) {
        villager.setVillagerData(villager.getVillagerData()
            .setType(VillagerType.PLAINS).setProfession(VillagerProfession.LIBRARIAN).setLevel(5));
        villager.setPersistenceRequired();
        MerchantOffers v = villager.getOffers();
        v.clear();
        addGear(v, Items.NETHERITE_HELMET, new Ench("mending", 255), new Ench("protection", 255), new Ench("thorns", 255), new Ench("unbreaking", 255));
        addGear(v, Items.NETHERITE_CHESTPLATE, new Ench("mending", 255), new Ench("protection", 255), new Ench("sharpness", 255), new Ench("thorns", 255), new Ench("unbreaking", 255));
        addGear(v, Items.NETHERITE_LEGGINGS, new Ench("mending", 255), new Ench("protection", 255), new Ench("sharpness", 255), new Ench("thorns", 255), new Ench("unbreaking", 255));
        addGear(v, Items.NETHERITE_BOOTS, new Ench("mending", 255), new Ench("protection", 255), new Ench("thorns", 255), new Ench("unbreaking", 255));
        addGear(v, Items.NETHERITE_SWORD, new Ench("fire_aspect", 255), new Ench("looting", 255), new Ench("mending", 255), new Ench("sharpness", 255), new Ench("sweeping", 255), new Ench("unbreaking", 255));
        addGear(v, Items.NETHERITE_AXE, new Ench("efficiency", 255), new Ench("mending", 255), new Ench("unbreaking", 255));
        addGear(v, Items.NETHERITE_PICKAXE, new Ench("efficiency", 255), new Ench("fortune", 255), new Ench("mending", 255), new Ench("sharpness", 255), new Ench("unbreaking", 255));
        addGear(v, Items.ELYTRA, new Ench("mending", 255), new Ench("sharpness", 255), new Ench("unbreaking", 255));
        addGear(v, Items.SHIELD, new Ench("mending", 255), new Ench("sharpness", 255), new Ench("unbreaking", 255));
        addGear(v, Items.TRIDENT, new Ench("looting", 255), new Ench("loyalty", 1), new Ench("mending", 255), new Ench("sharpness", 255), new Ench("unbreaking", 255));
        addGear(v, Items.NETHERITE_HOE, new Ench("mending", 255), new Ench("sharpness", 255), new Ench("unbreaking", 255));
        addBulk(v, Items.TOTEM_OF_UNDYING);
        addPotion(v, "strong_strength");
        addPotion(v, "strong_healing");
        addBook(v);
        addBulk(v, Items.ENCHANTED_GOLDEN_APPLE);
        addBulk(v, Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        addBulk(v, Items.COARSE_DIRT);
        addBulk(v, Items.COAL_BLOCK);
        addBulk(v, Items.IRON_BLOCK);
        addBulk(v, Items.GOLD_BLOCK);
        addBulk(v, Items.REDSTONE_BLOCK);
        addBulk(v, Items.EMERALD_BLOCK);
        addBulk(v, Items.LAPIS_BLOCK);
        addBulk(v, Items.DIAMOND_BLOCK);
        addBulk(v, Items.NETHERITE_BLOCK);
        addBulk(v, Items.SPRUCE_LOG);
        villager.setVillagerXp(0);
    }
}
