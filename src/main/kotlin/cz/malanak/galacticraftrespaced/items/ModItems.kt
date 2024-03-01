package cz.malanak.galacticraftrespaced.items

import cz.malanak.galacticraftrespaced.GalacticraftRespaced
import net.minecraft.world.food.FoodProperties
import net.minecraft.world.item.Item
import net.neoforged.neoforge.registries.DeferredItem
import net.neoforged.neoforge.registries.DeferredRegister

class ModItems {
    companion object
    {
        public val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(GalacticraftRespaced.MODID)
        val EXAMPLE_ITEM: DeferredItem<Item> = ITEMS.registerSimpleItem(
                "example_item", Item.Properties().food(
                FoodProperties.Builder()
                .alwaysEat().nutrition(1).saturationMod(2f).build()
            )
        )
    }
}