package meteordevelopment.meteorclient.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.datafixers.util.Pair;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.render.DisplayItemUtils;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static meteordevelopment.meteorclient.utils.Utils.getWindowHeight;
import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;
import static com.mojang.blaze3d.platform.InputConstants.*;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;

public class ListModulesScreen extends TabScreen {
    private WTextBox search;
    private WVerticalList list;
    private Category selected = null; // null = すべて
    private String searchText = "";
    private Module settingsModule = null;

    public ListModulesScreen(GuiTheme theme) {
        super(theme, Tabs.get().getFirst());
    }

    @Override
    public void initWidgets() {
        WWindow w = theme.window("Modules");
        w.id = "list-modules";
        add(w).center();
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.spacing = 4;
        w.view.maxHeight = 520;

        search = w.add(theme.textBox(searchText)).minWidth(800).expandX().widget();
        search.setFocused(settingsModule == null);
        search.action = () -> {
            searchText = search.get();
            refresh();
        };

        WHorizontalList tabs = w.add(theme.horizontalList()).expandX().widget();
        WButton all = tabs.add(theme.button("All")).widget();
        all.action = () -> {
            selected = null;
            refresh();
        };
        for (Category category : Modules.loopCategories()) {
            WButton b = tabs.add(theme.button(category.name)).widget();
            b.action = () -> {
                selected = category;
                refresh();
            };
        }

        w.add(theme.horizontalSeparator()).expandX();

        list = w.add(theme.verticalList()).expandX().widget();
        refresh();

        // 設定の小窓(あとから足すので、一覧の上に重なる)
        if (settingsModule != null) addSettingsWindow(settingsModule);
    }

    @Override
    protected void beforeRootRender(GuiRenderer renderer) {
        double scale = 0.85 + 0.15 * animProgress;
        renderer.pushScale(getWindowWidth() / 2.0, getWindowHeight() / 2.0, scale);
    }

    @Override
    protected void afterRootRender(GuiRenderer renderer) {
        renderer.popScale();
    }

    private void refresh() {
        list.clear();
        String filter = search.get().trim().toLowerCase();
        int count = 0;

        for (Module module : Modules.get().getAll()) {
            if (selected != null && module.category != selected) continue;
            if (!filter.isEmpty()
                && !module.title.toLowerCase().contains(filter)
                && !module.name.toLowerCase().contains(filter)) continue;

            list.add(theme.module(module)).expandX();
            count++;
        }

        list.add(theme.label(count + " results").color(theme.textSecondaryColor()));
    }

    public void openSettings(Module module) {
        settingsModule = module;
        reload();
    }

    private void closeSettings() {
        settingsModule = null;
        reload();
    }

    private void addSettingsWindow(Module module) {
        WWindow sw = theme.window(module.title);
        sw.id = "module-settings";
        add(sw).center();
        sw.view.scrollOnlyWhenMouseOver = true;
        sw.view.hasScrollBar = false;
        sw.view.maxHeight = 520;

        sw.add(theme.label(module.description, getWindowWidth() / 2.0)).expandX();

        if (!module.settings.groups.isEmpty()) {
            sw.add(theme.settings(module.settings)).expandX();
        }

        // Bind
        WSection section = sw.add(theme.section("Bind", true)).expandX().widget();

        WHorizontalList bind = section.add(theme.horizontalList()).expandX().widget();
        bind.add(theme.label("Bind: "));
        WKeybind keybind = bind.add(theme.keybind(module.keybind)).expandX().widget();
        keybind.actionOnSet = () -> Modules.get().setModuleToBind(module);

        WButton reset = bind.add(theme.button(GuiRenderer.RESET)).expandCellX().right().widget();
        reset.action = keybind::resetBind;
        reset.tooltip = "Reset";

        WHorizontalList tobr = section.add(theme.horizontalList()).widget();
        tobr.add(theme.label("Toggle on bind release: "));
        WCheckbox tobrC = tobr.add(theme.checkbox(module.toggleOnBindRelease)).widget();
        tobrC.action = () -> module.toggleOnBindRelease = tobrC.checked;

        WHorizontalList cf = section.add(theme.horizontalList()).widget();
        cf.add(theme.label("Chat Feedback: "));
        WCheckbox cfC = cf.add(theme.checkbox(module.chatFeedback)).widget();
        cfC.action = () -> module.chatFeedback = cfC.checked;

        sw.add(theme.horizontalSeparator()).expandX();

        // Bottom
        WHorizontalList bottom = sw.add(theme.horizontalList()).expandX().widget();
        bottom.add(theme.label("Active: "));
        WCheckbox active = bottom.add(theme.checkbox(module.isActive())).expandCellX().widget();
        active.action = () -> {
            if (module.isActive() != active.checked) module.toggle();
        };

        WButton close = bottom.add(theme.button("Close")).right().widget();
        close.action = this::closeSettings;
    }
}
