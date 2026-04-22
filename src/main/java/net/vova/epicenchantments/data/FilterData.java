package net.vova.epicenchantments.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashSet;
import java.util.Set;

public class FilterData implements INBTSerializable<CompoundTag> {
    private Set<String> filteredItems = new HashSet<>();

    // Добавить предмет в фильтр
    public void addItem(String itemId) {
        // Нормализуем ID
        String normalized = normalizeItemId(itemId);
        filteredItems.add(normalized);
    }

    // Удалить предмет из фильтра
    public void removeItem(String itemId) {
        String normalized = normalizeItemId(itemId);
        filteredItems.remove(normalized);
    }

    // Проверить, есть ли предмет в фильтре
    public boolean isItemFiltered(String itemId) {
        String normalized = normalizeItemId(itemId);
        return filteredItems.contains(normalized);
    }

    // Нормализация ID предмета
    private String normalizeItemId(String itemId) {
        if (itemId.startsWith("minecraft:")) {
            return itemId.substring(10); // Убираем "minecraft:"
        }
        return itemId;
    }

    // Получить все отфильтрованные предметы
    public Set<String> getFilteredItems() {
        return new HashSet<>(filteredItems);
    }

    // Очистить фильтр
    public void clear() {
        filteredItems.clear();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();

        for (String itemId : filteredItems) {
            list.add(StringTag.valueOf(itemId));
        }

        tag.put("FilteredItems", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        filteredItems.clear();

        if (nbt.contains("FilteredItems")) {
            ListTag list = nbt.getList("FilteredItems", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                filteredItems.add(list.getString(i));
            }
        }
    }

    // Получить данные из предмета
    public static FilterData fromItemStack(ItemStack stack) {
        FilterData data = new FilterData();

        if (stack.hasTag() && stack.getTag().contains("FilterData")) {
            CompoundTag tag = stack.getTag().getCompound("FilterData");
            data.deserializeNBT(tag);
        }

        return data;
    }

    // Сохранить данные в предмет
    public static void saveToItemStack(ItemStack stack, FilterData data) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("FilterData", data.serializeNBT());
    }
}