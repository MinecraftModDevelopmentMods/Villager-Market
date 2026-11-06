package net.modcrafters.minecraft.villagermarket.neoforge.gametest;

import net.modcrafters.minecraft.villagermarket.Constants;
import net.modcrafters.minecraft.villagermarket.gametest.MarketGameTests;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;
//? if >=1.21.5 {
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;
//?} else {
/*import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.Collection;
*///?}

//? if <1.21.5
//@GameTestHolder(Constants.MOD_ID)
public final class NeoForgeMarketGameTests {
    private NeoForgeMarketGameTests() {
    }

    public static void register(IEventBus modBus) {
        if (!GameTestHooks.isGametestEnabled()) {
            return;
        }
        //? if >=1.21.5 {
        DeferredRegister<Consumer<GameTestHelper>> functions = DeferredRegister.create(Registries.TEST_FUNCTION, Constants.MOD_ID);
        for (MarketGameTests.Case test : MarketGameTests.CASES) {
            functions.register(test.name(), () -> test.body());
        }
        functions.register(modBus);
        //?}
        modBus.addListener(NeoForgeMarketGameTests::registerTests);
    }

    //? if >=1.21.5 {
    private static void registerTests(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(id("default"));
        for (MarketGameTests.Case test : MarketGameTests.CASES) {
            event.registerTest(id(test.name()), new FunctionGameTestInstance(
                    ResourceKey.create(Registries.TEST_FUNCTION, id(test.name())),
                    new TestData<>(environment, id("wide"), 100, 0, true)
            ));
        }
    }
    //?} else {
    /*private static void registerTests(RegisterGameTestsEvent event) {
        event.register(NeoForgeMarketGameTests.class);
    }

    @GameTestGenerator
    public static Collection<TestFunction> generated() {
        return MarketGameTests.CASES.stream()
                .map(test -> new TestFunction(test.name(), Constants.MOD_ID + "." + test.name(), "villagermarket:wide",
                        100, 0L, true, test.body()))
                .toList();
    }
    *///?}

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, path);
    }
}
