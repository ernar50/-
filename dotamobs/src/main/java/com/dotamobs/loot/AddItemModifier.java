package com.dotamobs.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Global loot modifier: with the given chance, adds one copy of an item to the loot.
 * Which loot tables it applies to (chests) is set in the JSON files under data/dotamobs/loot_modifiers.
 */
public class AddItemModifier extends LootModifier {
    public static final Supplier<MapCodec<AddItemModifier>> CODEC = Suppliers.memoize(() ->
        RecordCodecBuilder.mapCodec(inst -> codecStart(inst).and(
            inst.group(
                ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(m -> m.item),
                Codec.FLOAT.fieldOf("chance").forGetter(m -> m.chance)
            )).apply(inst, AddItemModifier::new)
        ));

    private final Item item;
    private final float chance;

    public AddItemModifier(LootItemCondition[] conditions, Item item, float chance) {
        super(conditions);
        this.item = item;
        this.chance = chance;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(LootTable table, ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.getRandom().nextFloat() < this.chance) {
            generatedLoot.add(new ItemStack(this.item));
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
