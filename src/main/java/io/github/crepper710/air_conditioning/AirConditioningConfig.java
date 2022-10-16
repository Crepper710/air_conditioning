package io.github.crepper710.air_conditioning;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.*;

public class AirConditioningConfig {
    private static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<Object>>> ACCEPTED_COOLING_FUELS;
    private static final ForgeConfigSpec.ConfigValue<List<? extends List<Object>>> ACCEPTED_HEATING_FUELS;
    private static final ForgeConfigSpec.IntValue MAX_COOLING_FUEL;
    private static final ForgeConfigSpec.IntValue MAX_HEATING_FUEL;
    private static final ForgeConfigSpec.IntValue COOLING_FUEL_PER_TICK;
    private static final ForgeConfigSpec.IntValue HEATING_FUEL_PER_TICK;
    private static final ForgeConfigSpec.DoubleValue COOLING_AMOUNT_PER_RPM_DIGIT;
    private static final ForgeConfigSpec.DoubleValue HEATING_AMOUNT_PER_RPM_DIGIT;
    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment(
                "note: 1 \u00B0MC = 42 \u00B0F/ 23.33 \u00B0C"
        );
        builder.push("fuels");
        ACCEPTED_COOLING_FUELS = builder.comment(
                "structure: [[item_name(s), fuel_amount], ...]",
                "    item_name(s):",
                "        \"id:item\" or",
                "        \"id:item1,id:item2\"",
                "    fuel_amount:",
                "        any integer",
                "example: [[\"minecraft:snow_block\", 1024]]"
        ).defineList("cooling_fuel", Arrays.asList(Arrays.asList("minecraft:snow_block", 1024)), (obj) -> {
            if (obj instanceof List) {
                List<?> list = (List<?>) obj;
                if (list.size() != 2) {
                    return false;
                }
                return list.get(0) instanceof String && list.get(1) instanceof Integer;
            }
            return false;
        });
        ACCEPTED_HEATING_FUELS = builder.comment(
                "structure: same as cooling_fuel",
                "example: [[\"minecraft:magma_block\", 1024]]"
        ).defineList("heating_fuel", Arrays.asList(Arrays.asList("minecraft:magma_block", 1024)), (obj) -> {
            if (obj instanceof List) {
                List<?> list = (List<?>) obj;
                if (list.size() != 2) {
                    return false;
                }
                return list.get(0) instanceof String && list.get(1) instanceof Integer;
            }
            return false;
        });
        MAX_COOLING_FUEL = builder.comment(
                "Max amount for cooling fuel"
        ).defineInRange("max_cooling_fuel", 0x10000, 0, Integer.MAX_VALUE);
        MAX_HEATING_FUEL = builder.comment(
                "Max amount for heating fuel"
        ).defineInRange("max_heating_fuel", 0x10000, 0, Integer.MAX_VALUE);
        COOLING_FUEL_PER_TICK = builder.comment(
                "How much cooling fuel is used every active tick"
        ).defineInRange("cooling_fuel_per_tick", 1, 0, Integer.MAX_VALUE);
        HEATING_FUEL_PER_TICK = builder.comment(
                "How much heating fuel is used every active tick"
        ).defineInRange("heating_fuel_per_tick", 1, 0, Integer.MAX_VALUE);
        builder.pop();
        builder.push("temperature");
        COOLING_AMOUNT_PER_RPM_DIGIT = builder.comment(
                "Amount of cooling per RPM binary digit (0-9 RPM digits) in \u00B0MC (see top of file for conversion)"
        ).defineInRange("cooling_amount_per_rpm_digit", 0.1, 0.0, Double.MAX_VALUE);
        HEATING_AMOUNT_PER_RPM_DIGIT = builder.comment(
                "Amount of heating per RPM binary digit (0-9 RPM digits) in \u00B0MC (see top of file for conversion)"
        ).defineInRange("heating_amount_per_rpm_digit", 0.1, 0.0, Double.MAX_VALUE);
        builder.pop();
        SPEC = builder.build();
    }

    private static Map<String, Integer> cachedAcceptedCoolingFuels;
    private static int acceptedCoolingFuelsInputHashCode = -1;

    private static Map<String, Integer> cachedAcceptedHeatingFuels;
    private static int acceptedHeatingFuelsInputHashCode = -1;

    public static Map<String, Integer> getAcceptedCoolingFuels() {
        List<? extends List<Object>> input = ACCEPTED_COOLING_FUELS.get();
        if (input.hashCode() != acceptedCoolingFuelsInputHashCode) {
            acceptedCoolingFuelsInputHashCode = input.hashCode();
            HashMap<String, Integer> temp = parseAcceptedFuels(input);
            cachedAcceptedCoolingFuels = Collections.unmodifiableMap(temp);
        }
        return cachedAcceptedCoolingFuels;
    }

    public static Map<String, Integer> getAcceptedHeatingFuels() {
        List<? extends List<Object>> input = ACCEPTED_HEATING_FUELS.get();
        if (input.hashCode() != acceptedHeatingFuelsInputHashCode) {
            acceptedHeatingFuelsInputHashCode = input.hashCode();
            HashMap<String, Integer> temp = parseAcceptedFuels(input);
            cachedAcceptedHeatingFuels = Collections.unmodifiableMap(temp);
        }
        return cachedAcceptedHeatingFuels;
    }

    private static HashMap<String, Integer> parseAcceptedFuels(List<? extends List<Object>> input) {
        HashMap<String, Integer> out = new HashMap<>();
        for (List<Object> entry : input) {
            if (entry.size() != 2) {
                continue;
            }
            Object o1 = entry.get(0);
            Object o2 = entry.get(1);
            if (o1 instanceof String && o2 instanceof Integer) {
                String item = (String) o1;
                item = item.toLowerCase(Locale.ROOT);
                int fuelAmount = (int) (Integer) o2;
                if (item.contains(",")) {
                    for (String subItem : item.split(",")) {
                        subItem = subItem.trim();
                        out.put(subItem, fuelAmount);
                    }
                } else {
                    item = item.trim();
                    out.put(item, fuelAmount);
                }
            }
        }
        return out;
    }

    public static int getMaxCoolingFuel() {
        return MAX_COOLING_FUEL.get();
    }

    public static int getMaxHeatingFuel() {
        return MAX_HEATING_FUEL.get();
    }

    public static int getCoolingFuelPerTick() {
        return COOLING_FUEL_PER_TICK.get();
    }

    public static int getHeatingFuelPerTick() {
        return HEATING_FUEL_PER_TICK.get();
    }

    public static double getCoolingAmountPerRpmDigit() {
        return COOLING_AMOUNT_PER_RPM_DIGIT.get();
    }

    public static double getHeatingAmountPerRpmDigit() {
        return HEATING_AMOUNT_PER_RPM_DIGIT.get();
    }

    public static void setup() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "air_conditioning.toml");
    }
}
