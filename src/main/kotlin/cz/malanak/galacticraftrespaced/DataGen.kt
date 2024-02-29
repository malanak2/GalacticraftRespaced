package cz.malanak.galacticraftrespaced

import net.minecraft.data.DataGenerator
import net.minecraft.data.PackOutput
import net.neoforged.neoforge.data.event.GatherDataEvent

class DataGen {

    companion object {
        fun onGatherData(event: GatherDataEvent) {
            val gen: DataGenerator = event.generator
            val out: PackOutput = gen.packOutput
            gen.addProvider( // Tell generator to run only when client assets are generating
                    event.includeClient(),
                    MyLangProvider(out, GalacticraftRespaced.MODID, "en_us")
            )  // Localizations for American English
        }

    }

}