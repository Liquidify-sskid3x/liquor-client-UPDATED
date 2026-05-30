package com.example.liquorclient.command.impl;

import com.example.liquorclient.command.Command;
import com.example.liquorclient.module.Module;
import com.example.liquorclient.module.ModuleManager;
import com.example.liquorclient.utility.ChatUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.CommandSource;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public class BindCommand extends Command {
    public BindCommand() {
        super("bind", "Binds a module to a key.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(RequiredArgumentBuilder.<CommandSource, String>argument("module", StringArgumentType.string())
            .suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(ModuleManager.getModules().stream().map(Module::getName), suggestionsBuilder))
            .then(RequiredArgumentBuilder.<CommandSource, String>argument("key", StringArgumentType.greedyString())
                .executes(context -> {
                    String moduleName = StringArgumentType.getString(context, "module");
                    String keyName = StringArgumentType.getString(context, "key");
                    
                    Module module = null;
                    for (Module m : ModuleManager.getModules()) {
                        if (m.getName().equalsIgnoreCase(moduleName)) {
                            module = m;
                            break;
                        }
                    }
                    
                    if (module == null) {
                        ChatUtils.error("Module not found: " + moduleName);
                        return 0;
                    }
                    
                    int key = parseKey(keyName);
                    module.getKeybind().setValue(key);
                    ChatUtils.message(module.getName() + " bound to " + com.example.liquorclient.gui.ClickGui.getKeyName(key));
                    return 1;
                })
            )
        );
    }

    private int parseKey(String keyName) {
        String key = keyName.trim().toUpperCase(Locale.ROOT);
        if (key.equals("NONE") || key.equals("UNKNOWN") || key.equals("UNBIND")) return GLFW.GLFW_KEY_UNKNOWN;
        if (key.length() == 1) {
             // Basic single char mapping
             char c = key.charAt(0);
             if (c >= 'A' && c <= 'Z') return GLFW.GLFW_KEY_A + (c - 'A');
             if (c >= '0' && c <= '9') return GLFW.GLFW_KEY_0 + (c - '0');
        }

        return switch (key) {
            case "RSHIFT", "RIGHT_SHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "LSHIFT", "LEFT_SHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RCTRL", "RIGHT_CONTROL" -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "LCTRL", "LEFT_CONTROL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "ENTER" -> GLFW.GLFW_KEY_ENTER;
            default -> {
                if (key.matches("F\\d+")) {
                    int fn = Integer.parseInt(key.substring(1));
                    yield fn >= 1 && fn <= 25 ? GLFW.GLFW_KEY_F1 + fn - 1 : GLFW.GLFW_KEY_UNKNOWN;
                }
                yield GLFW.GLFW_KEY_UNKNOWN;
            }
        };
    }
}
