package cn.chengzhimeow.ccyaml.configuration;

import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@ToString(exclude = {"parent"})
@SuppressWarnings("unused")
public class MemoryConfiguration implements ConfigurationSection {
    /**
     * 创建空配置节点
     *
     * @param parent 父配置节点
     * @param path   当前节点的路径
     * @return 节点
     */
    public static @NotNull MemoryConfiguration empty(@Nullable ConfigurationSection parent, @Nullable String path) {
        return new MemoryConfiguration(parent, path);
    }

    /**
     * 创建空配置节点
     *
     * @param parent 父配置节点
     * @return 节点
     */
    public static @NotNull MemoryConfiguration empty(@Nullable ConfigurationSection parent) {
        return new MemoryConfiguration(parent, null);
    }

    /**
     * 创建空配置节点
     *
     * @return 节点
     */
    public static @NotNull MemoryConfiguration empty() {
        return new MemoryConfiguration(null, null);
    }

    /**
     * 递归获取 Map 中的所有键 (包括子 Map 的键)
     *
     * @param map 要扫描的 Map
     * @return 所有键的集合
     */
    private static @NotNull Set<String> getKeys(@NotNull Map<String, ?> map) {
        Set<String> set = new HashSet<>();

        for (Map.Entry<String, ?> entry : map.entrySet()) {
            set.add(entry.getKey());
            if (entry.getValue() instanceof SectionData data && data.getData() instanceof Map) {
                // noinspection unchecked
                for (String key : MemoryConfiguration.getKeys((Map<String, ?>) data.getData())) {
                    set.add(entry.getKey() + "." + key);
                }
            }
        }

        return set;
    }
    private final @Nullable ConfigurationSection parent;
    private final @Nullable String path;
    protected @NotNull SectionData data = new SectionData(new LinkedHashMap<String, SectionData>());

    /**
     * MemoryConfiguration 的构造函数
     *
     * @param parent 父配置节点
     * @param path   当前节点的路径
     */
    protected MemoryConfiguration(@Nullable ConfigurationSection parent, @Nullable String path) {
        this.parent = parent;
        this.path = path;
    }

    @Override
    public @Nullable ConfigurationSection getParent() {
        return this.parent;
    }

    @Override
    public @Nullable String getPath() {
        return this.path;
    }

    @Override
    public @NotNull SectionData getData() {
        return this.data;
    }

    /**
     * 获取当前节点的完整路径键
     *
     * @param path 相对路径
     * @return 完整路径
     */
    @Override
    public @NotNull String getKey(String path) {
        if (this.path == null || this.path.isEmpty()) return path;
        return this.path + "." + path;
    }

    @Override
    public void set(@NotNull String path, @Nullable Object value) {
        String[] keys = path.split("\\.");
        int end = keys.length - 1;

        // noinspection unchecked
        Map<String, SectionData> currentMap = (Map<String, SectionData>) this.data.getData();
        for (int i = 0; i < end; i++) {
            String key = keys[i];
            SectionData sectionData = Objects.requireNonNull(currentMap).get(key);

            if (sectionData == null || !(sectionData.getData() instanceof Map)) {
                Map<String, SectionData> newMap = new LinkedHashMap<>();
                sectionData = new SectionData(newMap);
                currentMap.put(key, sectionData);
            }

            // noinspection unchecked
            currentMap = (Map<String, SectionData>) sectionData.getData();
        }

        String finalKey = keys[end];
        if (value == null) Objects.requireNonNull(currentMap).remove(finalKey);
        else {
            SectionData data = Objects.requireNonNull(currentMap).get(finalKey);
            if (data == null) data = new SectionData();

            if (value instanceof ConfigurationSection section) data = section.getData();
            else if (value instanceof Map) // noinspection unchecked
                data.setData(SectionData.fromMap((Map<Object, Object>) value).getData());
            else data.setData(value);

            currentMap.put(finalKey, data);
        }
    }

    @Override
    public @NotNull SectionData getSectionData(String path) {
        String[] keys = path.split("\\.");
        int end = keys.length - 1;

        // noinspection unchecked
        Map<String, SectionData> currentMap = (Map<String, SectionData>) this.data.getData();
        for (int i = 0; i < end; i++) {
            String key = keys[i];

            SectionData currentSectionData = Objects.requireNonNull(currentMap).get(key);
            if (currentSectionData != null && currentSectionData.getData() instanceof Map) // noinspection unchecked
                currentMap = (Map<String, SectionData>) currentSectionData.getData();
            else return new SectionData();
        }

        SectionData sectionData = Objects.requireNonNull(currentMap).get(keys[end]);
        return sectionData != null ? sectionData : new SectionData();
    }

    @Override
    public @NotNull Set<String> getKeys(boolean deep) {
        // noinspection unchecked
        Map<String, SectionData> map = (Map<String, SectionData>) this.data.getData();

        if (!deep) return Objects.requireNonNull(map).keySet();
        return MemoryConfiguration.getKeys(Objects.requireNonNull(map));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MemoryConfiguration memoryConfiguration) {
            return memoryConfiguration.data.equals(this.data);
        }
        return false;
    }
}
