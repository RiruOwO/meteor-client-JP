package meteordevelopment.meteorclient.utils;

import net.minecraft.resources.Identifier; // ResourceLocation から変更

public enum CharacterImage {
    CHAR_1("char1.png", "char1"),
    CHAR_2("char2.png", "char2"),
    CHAR_3("char3.png", "char3"),
    CHAR_4("char4.png", "char4"),
    CHAR_5("char5.png", "char810");

    private final String fileName;
    private final String displayName;

    CharacterImage(String fileName, String displayName) {
        this.fileName = fileName;
        this.displayName = displayName;
    }

    public Identifier getIdentifier() {
        // 26.2 ではファクトリメソッドを使用
        return Identifier.fromNamespaceAndPath("meteor-client", "textures/gui/characters/" + fileName);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
