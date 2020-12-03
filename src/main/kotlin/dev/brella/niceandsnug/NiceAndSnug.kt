package dev.brella.niceandsnug

import dev.brella.niceandsnug.NiceAndSnug.MOD_ID
import dev.brella.niceandsnug.proxy.SnugProxy
import dev.brella.niceandsnug.tileentities.TileEntitySnugCompressed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import net.minecraft.block.Block
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ModelRegistryEvent
import net.minecraftforge.common.config.Configuration
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.SidedProxy
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStartingEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.registry.GameRegistry
import org.apache.logging.log4j.Logger
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

@Mod(modid = MOD_ID, name = NiceAndSnug.MOD_NAME, version = NiceAndSnug.VERSION, modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter")
@Mod.EventBusSubscriber(modid = MOD_ID)
object NiceAndSnug: CoroutineScope {
    public const val MOD_ID = "nice_and_snug"
    public const val MOD_NAME = "Nice and Snug"
    public const val VERSION = "1.0.0a"

    public const val DEFAULT_LIGHT_MULTIPLIER = 8.0
    public const val DEFAULT_ENCHANT_POWER_BONUS = 8.0
    
    public lateinit var logger: Logger

    public val creativeTab: CreativeTabs = object: CreativeTabs(MOD_ID) {
        override fun createIcon(): ItemStack = ItemStack(Blocks.COBBLESTONE)
    }

    @SidedProxy(serverSide = "dev.brella.niceandsnug.proxy.ServerProxy", clientSide = "dev.brella.niceandsnug.proxy.ClientProxy")
    public lateinit var proxy: SnugProxy

    override val coroutineContext: CoroutineContext = SupervisorJob() + Executors.newSingleThreadExecutor { task -> Thread(task, MOD_NAME).apply { isDaemon = true } }.asCoroutineDispatcher()

    public lateinit var config: Configuration
    public val lightMultiplier by lazy { config["multipliers", "light_level", DEFAULT_LIGHT_MULTIPLIER].double }
    public val doExtraLighting by lazy { true }
    public val fakeLightingMaxRange by lazy { 128 }
    public val doFakeLightingInUnloaded by lazy { true }
    public val fakeLightingStep by lazy { 3 }

    public val enchantPowerBonusMultiplier by lazy { config["multipliers", "enchant_power_bonus", DEFAULT_ENCHANT_POWER_BONUS].double.toFloat() }

    public val itemBurnTimeMultiplier by lazy { 10.0 }

    @Mod.EventHandler
    fun preInitialisation(event: FMLPreInitializationEvent) {
        logger = event.modLog
        config = Configuration(event.suggestedConfigurationFile)

        GameRegistry.registerTileEntity(TileEntitySnugCompressed::class.java, ResourceLocation(MOD_ID, "snug_compressed"))
    }

//    @Mod.EventHandler
//    fun initialisation(event: FMLInitializationEvent) {
//        ForgeRegistries.RECIPES.register()
//    }
    @Mod.EventHandler
    fun serverStarting(event: FMLServerStartingEvent) {
        proxy.serverStarting(event)
    }

    @SubscribeEvent
    fun registerBlocks(event: RegistryEvent.Register<Block>) {
        SnugBlocks.register(event.registry)
    }

    @SubscribeEvent
    fun registerItems(event: RegistryEvent.Register<Item>) {
        SnugBlocks.registerItemBlocks(event.registry)
    }

    @SubscribeEvent
    fun registerModels(event: ModelRegistryEvent) {
        SnugBlocks.registerModels()
    }

    @SubscribeEvent
    fun registerRecipes(event: RegistryEvent.Register<IRecipe>) {
        event.registry.register(CompressedRecipe())
    }
}