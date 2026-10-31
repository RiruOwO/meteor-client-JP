package meteordevelopment.meteorclient.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WSection;
import meteordevelopment.meteorclient.gui.widgets.containers.WVerticalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static meteordevelopment.meteorclient.utils.Utils.getWindowHeight;
import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;

public class ListModulesScreen extends TabScreen {
    private WVerticalList moduleListContainer; // リスト専用コンテナ（ここだけを更新する）
    private Category selected = null;
    private Module selectedModule = null;
    private String searchText = "";
    private WTextBox search;

    public ListModulesScreen(GuiTheme theme) {
        super(theme, Tabs.get().getFirst());
    }

    @Override
    public void initWidgets() {
        // 画面左上に固定で配置するための水平レイアウト
        WHorizontalList layout = add(theme.horizontalList()).widget();

        // ==========================================
        // 左側：カテゴリ一覧ウィンドウ（固定・スクロールなし）
        // ==========================================
        WWindow sidebarWindow = theme.window("Categories");
        sidebarWindow.id = "categories-window";
        layout.add(sidebarWindow).widget();
        sidebarWindow.view.scrollOnlyWhenMouseOver = false;
        sidebarWindow.view.hasScrollBar = false;
        sidebarWindow.view.maxHeight = 9999;

        var allBtn = sidebarWindow.add(theme.button("All")).expandX().widget();
        allBtn.action = () -> {
            selected = null;
            selectedModule = null;
            updateModuleList();
        };

        for (Category category : Modules.loopCategories()) {
            var b = sidebarWindow.add(theme.button(category.name)).expandX().widget();
            b.action = () -> {
                selected = category;
                selectedModule = null;
                updateModuleList();
            };
        }

        // ==========================================
        // 右側：モジュール一覧ウィンドウ（スクロール可能）
        // ==========================================
        WWindow contentWindow = theme.window("Modules");
        contentWindow.id = "modules-window";
        layout.add(contentWindow).widget();
        contentWindow.view.scrollOnlyWhenMouseOver = true;
        contentWindow.view.hasScrollBar = true;
        contentWindow.view.maxHeight = 520;

        // 検索欄の作成（最初の1回だけ。これ以降、検索時に破棄されない）
        //search = contentWindow.add(theme.textBox(searchText)).minWidth(300).expandX().widget();
        //search.setFocused(true);
        //search.action = () -> {
        //   searchText = search.get();
        //    updateModuleList(); // リスト部分だけを更新
        //};
        contentWindow.add(theme.horizontalSeparator()).expandX();

        // リスト専用のコンテナ（ここだけをクリアして再描画する）
        moduleListContainer = contentWindow.add(theme.verticalList()).widget();

        // 初回描画
        updateModuleList();
    }

    // リスト部分（または設定画面）だけを更新するメソッド
    private void updateModuleList() {
        if (moduleListContainer == null) return;
        moduleListContainer.clear(); // リスト部分だけをクリア

        if (selectedModule != null) {
            // 設定画面を表示
            addModuleSettings(moduleListContainer, selectedModule);
        } else {
            // モジュール一覧を表示
            String filter = searchText.trim().toLowerCase();
            for (Module module : Modules.get().getAll()) {
                if (selected != null && module.category != selected) continue;
                if (!filter.isEmpty()
                    && !module.title.toLowerCase().contains(filter)
                    && !module.name.toLowerCase().contains(filter)) continue;

                // ★ ここは元のコードのまま（標準デザインを維持）
                moduleListContainer.add(theme.module(module)).expandX();
            }
        }
    }

    private void addModuleSettings(WVerticalList parent, Module module) {
        var backBtn = parent.add(theme.button("← Back")).widget();
        backBtn.action = () -> {
            selectedModule = null;
            updateModuleList();
        };

        parent.add(theme.label(module.title)).expandX();
        parent.add(theme.label(module.description)).expandX();

        if (!module.settings.groups.isEmpty()) {
            parent.add(theme.settings(module.settings)).expandX();
        }

        WSection section = parent.add(theme.section("Bind", true)).expandX().widget();
        WHorizontalList bind = section.add(theme.horizontalList()).expandX().widget();
        bind.add(theme.label("Bind: "));
        WKeybind keybind = bind.add(theme.keybind(module.keybind)).expandX().widget();
        keybind.actionOnSet = () -> Modules.get().setModuleToBind(module);

        var reset = bind.add(theme.button(GuiRenderer.RESET)).expandCellX().right().widget();
        reset.action = keybind::resetBind;
        reset.tooltip = "Reset";

        WHorizontalList tobr = section.add(theme.horizontalList()).widget();
        tobr.add(theme.label("Toggle on bind release: "));
        var tobrC = tobr.add(theme.checkbox(module.toggleOnBindRelease)).widget();
        tobrC.action = () -> module.toggleOnBindRelease = tobrC.checked;

        WHorizontalList cf = section.add(theme.horizontalList()).widget();
        cf.add(theme.label("Chat Feedback: "));
        var cfC = cf.add(theme.checkbox(module.chatFeedback)).widget();
        cfC.action = () -> module.chatFeedback = cfC.checked;

        parent.add(theme.horizontalSeparator()).expandX();

        WHorizontalList bottom = parent.add(theme.horizontalList()).expandX().widget();
        bottom.add(theme.label("Active: "));
        var active = bottom.add(theme.checkbox(module.isActive())).expandCellX().widget();
        active.action = () -> {
            if (module.isActive() != active.checked) module.toggle();
        };
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

    // WMeteorModule からの呼び出し用
    public void openSettings(Module module) {
        selectedModule = module;
        updateModuleList(); // reload() を呼ばない
    }
}
