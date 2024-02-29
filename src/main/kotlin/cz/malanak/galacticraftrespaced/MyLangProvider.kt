package cz.malanak.galacticraftrespaced

import cz.malanak.galacticraftrespaced.blocks.ModBlocks
import net.minecraft.data.PackOutput
import net.minecraft.locale.Language
import net.neoforged.neoforge.common.data.LanguageProvider

class MyLangProvider(output: PackOutput, modid: String, locale: String) : LanguageProvider(output, modid, locale) {
    override fun addTranslations() {
        this.addBlock(ModBlocks.EXAMPLE_BLOCK, "E");
    }
}