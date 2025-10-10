package cn.chengzhimeow.ccyaml.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("unused")
public interface ConfigurationSection {
    /**
     * 获取当前节点夫配置实例
     *
     * @return 节点夫配置实例
     */
    @Nullable ConfigurationSection getParent();

    /**
     * 获取当前节点完整路径
     *
     * @return 节点完整路径
     */
    @Nullable String getPath();

    /**
     * 获取当前节点完整数据
     *
     * @return 节点完整数据
     */
    @NotNull SectionData getData();

    /**
     * 获取当前节点的完整路径键
     *
     * @param path 相对路径
     * @return 完整路径
     */
    @NotNull String getKey(String path);

    /**
     * 设定指定路径的值
     *
     * @param path  值的路径
     * @param value 要设定的值, 如果为 null 则会移除该键
     */
    void set(@NotNull String path, @Nullable Object value);

    /**
     * 获取指定路径的 SectionData 对象
     *
     * @param path 路径
     * @return 包含数据和注释的 SectionData 对象, 如果路径不存在则返回一个空的 SectionData
     */
    @NotNull SectionData getSectionData(String path);

    /**
     * 设定指定路径的块注释 (在键值对上方)
     *
     * @param path        路径
     * @param commentList 注释列表
     */
    default void setCommentList(@NotNull String path, @NotNull List<String> commentList) {
        this.getSectionData(path).setCommentList(commentList);
    }

    /**
     * 获取指定路径的块注释
     *
     * @param path 路径
     * @return 注释列表
     */
    default @NotNull List<String> getCommentList(@NotNull String path) {
        return this.getSectionData(path).getCommentList();
    }

    /**
     * 获取块注释
     *
     * @return 注释列表
     */
    default @NotNull List<String> getCommentList() {
        return this.getData().getCommentList();
    }

    /**
     * 设定块注释 (在键值对上方)
     *
     * @param commentList 注释列表
     */
    default void setCommentList(@NotNull List<String> commentList) {
        this.getData().setCommentList(commentList);
    }

    /**
     * 设定指定路径的行内注释
     *
     * @param path        路径
     * @param commentList 注释列表
     */
    default void setInlineCommentList(@NotNull String path, @NotNull List<String> commentList) {
        this.getSectionData(path).setInlineCommentList(commentList);
    }

    /**
     * 获取指定路径的行内注释 (在键值对后方)
     *
     * @param path 路径
     * @return 注释列表
     */
    default @NotNull List<String> getInlineCommentList(@NotNull String path) {
        return this.getSectionData(path).getInlineCommentList();
    }

    /**
     * 获取行内注释 (在键值对后方)
     *
     * @return 注释列表
     */
    default @NotNull List<String> getInlineCommentList() {
        return this.getData().getInlineCommentList();
    }

    /**
     * 设定行内注释
     *
     * @param commentList 注释列表
     */
    default void setInlineCommentList(@NotNull List<String> commentList) {
        this.getData().setInlineCommentList(commentList);
    }

    /**
     * 获取指定路径的值并转换为指定类型
     *
     * @param path  路径
     * @param clazz 目标类型的 Class 对象
     * @param <T>   目标类型
     * @return 转换后的值, 如果路径不存在或类型不匹配可能返回 null 或抛出 ClassCastException
     */
    default @Nullable <T> T get(@NotNull String path, @NotNull Class<T> clazz) {
        Object data = this.getSectionData(path).getData();
        if (data == null) return null;

        if (clazz != StringSection.class && data instanceof StringSection str) data = str.getValue();
        else if (clazz == String.class) {
            if (!(data instanceof String)) data = data.toString();
        } else if (clazz == Long.class) {
            if (data instanceof Integer v) data = v.longValue();
        } else if (clazz == Integer.class) {
            if (data instanceof Double v) data = v.intValue();
        } else if (clazz == Short.class) {
            if (data instanceof Integer v) data = v.shortValue();
            else if (data instanceof Double v) data = v.shortValue();
        } else if (clazz == Double.class) {
            if (data instanceof Integer v) data = v.doubleValue();
        } else if (clazz == Float.class) {
            if (data instanceof Integer v) data = v.floatValue();
            else if (data instanceof Double v) data = v.floatValue();
        }

        // noinspection unchecked
        return (T) data;
    }

    /**
     * 获取指定路径的 Object 类型的值
     *
     * @param path 路径
     * @return Object 值, 如果不存在则为 null
     */
    default @Nullable Object get(@NotNull String path) {
        return this.get(path, Object.class);
    }

    /**
     * 检查指定路径是否存在值
     *
     * @param path 路径
     * @return 如果路径存在值则为 true, 否则为 false
     */
    default boolean has(@NotNull String path) {
        return this.getSectionData(path).getData() != null;
    }

    /**
     * 获取指定路径的字符串值
     *
     * @param path 路径
     * @param def  默认值
     * @return 字符串值, 如果不存在则返回默认值
     */
    default @Nullable String getString(@NotNull String path, @Nullable String def) {
        String value = this.get(path, String.class);
        return value != null ? value : def;
    }

    /**
     * 获取指定路径的字符串值
     *
     * @param path 路径
     * @return 字符串值, 如果不存在则返回 null
     */
    default @Nullable String getString(@NotNull String path) {
        return this.getString(path, null);
    }

    /**
     * 获取指定路径的短整数值
     *
     * @param path 路径
     * @param def  默认值
     * @return 短整数值, 如果不存在则返回默认值
     */
    default @Nullable Short getShort(@NotNull String path, @Nullable Short def) {
        Short value = this.get(path, Short.class);
        return value != null ? value : def;
    }

    /**
     * 获取指定路径的短整数值
     *
     * @param path 路径
     * @return 短整数值, 如果不存在则返回 0
     */
    @SuppressWarnings("DataFlowIssue")
    default short getShort(@NotNull String path) {
        return this.getShort(path, (short) 0);
    }

    /**
     * 获取指定路径的整数值
     *
     * @param path 路径
     * @param def  默认值
     * @return 整数值, 如果不存在则返回默认值
     */
    default @Nullable Integer getInt(@NotNull String path, @Nullable Integer def) {
        Integer value = this.get(path, Integer.class);
        return value != null ? value : def;
    }

    /**
     * 获取指定路径的整数值
     *
     * @param path 路径
     * @return 整数值, 如果不存在则返回 0
     */
    @SuppressWarnings("DataFlowIssue")
    default int getInt(@NotNull String path) {
        return this.getInt(path, 0);
    }

    /**
     * 获取指定路径的长整数值
     *
     * @param path 路径
     * @param def  默认值
     * @return 长整数值, 如果不存在则返回默认值
     */
    default @Nullable Long getLong(@NotNull String path, @Nullable Long def) {
        Long value = this.get(path, Long.class);
        return value != null ? value : def;
    }

    /**
     * 获取指定路径的长整数值
     *
     * @param path 路径
     * @return 长整数值, 如果不存在则返回 0L
     */
    @SuppressWarnings("DataFlowIssue")
    default long getLong(@NotNull String path) {
        return this.getLong(path, 0L);
    }

    /**
     * 获取指定路径的布尔值
     *
     * @param path 路径
     * @param def  默认值
     * @return 布尔值, 如果不存在则返回默认值
     */
    default @Nullable Boolean getBoolean(@NotNull String path, @Nullable Boolean def) {
        Boolean value = this.get(path, Boolean.class);
        return value != null ? value : def;
    }

    /**
     * 获取指定路径的布尔值
     *
     * @param path 路径
     * @return 布尔值, 如果不存在则返回 false
     */
    @SuppressWarnings("DataFlowIssue")
    default boolean getBoolean(@NotNull String path) {
        return this.getBoolean(path, false);
    }

    /**
     * 获取指定路径的双精度浮点数值
     *
     * @param path 路径
     * @param def  默认值
     * @return 双精度浮点数值, 如果不存在则返回默认值
     */
    default @Nullable Double getDouble(@NotNull String path, @Nullable Double def) {
        Double value = this.get(path, Double.class);
        return value != null ? value : def;
    }

    /**
     * 获取指定路径的双精度浮点数值
     *
     * @param path 路径
     * @return 双精度浮点数值, 如果不存在则返回 0.0
     */
    @SuppressWarnings("DataFlowIssue")
    default double getDouble(@NotNull String path) {
        return this.getDouble(path, 0.0);
    }

    /**
     * 获取指定路径的单精度浮点数值
     *
     * @param path 路径
     * @param def  默认值
     * @return 单精度浮点数值, 如果不存在则返回默认值
     */
    default @Nullable Float getFloat(@NotNull String path, @Nullable Float def) {
        Float value = this.get(path, Float.class);
        return value != null ? value : def;
    }

    /**
     * 获取指定路径的单精度浮点数值
     *
     * @param path 路径
     * @return 单精度浮点数值, 如果不存在则返回 0.0f
     */
    @SuppressWarnings("DataFlowIssue")
    default float getFloat(@NotNull String path) {
        return this.getFloat(path, 0.0f);
    }

    /**
     * 获取指定路径的列表值
     *
     * @param path  路径
     * @param def   默认列表
     * @param clazz 列表元素的 Class 对象 (由于类型擦除, 此参数仅用于明确意图)
     * @param <T>   列表元素的类型
     * @return 列表值, 如果不存在则返回默认列表
     */
    @SuppressWarnings("unchecked")
    default @Nullable <T> List<T> getList(@NotNull String path, @Nullable List<T> def, @NotNull Class<T> clazz) {
        List<T> list = this.get(path, List.class);
        if (list == null) return def;
        if (list.isEmpty()) return list;

        if (clazz != StringSection.class && list.get(0) instanceof StringSection)
            return (List<T>) list.stream()
                    .map(i -> ((StringSection) i).getValue())
                    .toList();

        return list;
    }

    /**
     * 获取指定路径的列表值
     *
     * @param path  路径
     * @param clazz 列表元素的 Class 对象 (由于类型擦除, 此参数仅用于明确意图)
     * @param <T>   列表元素的类型
     * @return 列表值, 如果不存在则返回一个空列表
     */
    @SuppressWarnings("DataFlowIssue")
    default @NotNull <T> List<T> getList(@NotNull String path, @NotNull Class<T> clazz) {
        return this.getList(path, new ArrayList<>(), clazz);
    }

    /**
     * 获取指定路径的字符串列表
     *
     * @param path 路径
     * @param def  默认列表
     * @return 字符串列表, 如果不存在则返回默认列表
     */
    default @Nullable List<String> getStringList(@NotNull String path, @Nullable List<String> def) {
        return this.getList(path, def, String.class);
    }

    /**
     * 获取指定路径的字符串列表
     *
     * @param path 路径
     * @return 字符串列表, 如果不存在则返回一个空列表
     */
    @SuppressWarnings("DataFlowIssue")
    default @NotNull List<String> getStringList(String path) {
        return this.getStringList(path, new ArrayList<>());
    }

    /**
     * 获取指定路径的整数列表
     *
     * @param path 路径
     * @param def  默认列表
     * @return 整数列表, 如果不存在则返回默认列表
     */
    default @Nullable List<Integer> getIntList(@NotNull String path, @Nullable List<Integer> def) {
        return this.getList(path, def, Integer.class);
    }

    /**
     * 获取指定路径的整数列表
     *
     * @param path 路径
     * @return 整数列表, 如果不存在则返回一个空列表
     */
    @SuppressWarnings("DataFlowIssue")
    default @NotNull List<Integer> getIntList(@NotNull String path) {
        return this.getIntList(path, new ArrayList<>());
    }

    /**
     * 获取指定路径的布尔值列表
     *
     * @param path 路径
     * @param def  默认列表
     * @return 布尔值列表, 如果不存在则返回默认列表
     */
    default @Nullable List<Boolean> getBooleanList(@NotNull String path, @Nullable List<Boolean> def) {
        return this.getList(path, def, Boolean.class);
    }

    /**
     * 获取指定路径的布尔值列表
     *
     * @param path 路径
     * @return 布尔值列表, 如果不存在则返回一个空列表
     */
    @SuppressWarnings("DataFlowIssue")
    default @NotNull List<Boolean> getBooleanList(@NotNull String path) {
        return this.getBooleanList(path, new ArrayList<>());
    }

    /**
     * 获取指定路径的双精度浮点数列表
     *
     * @param path 路径
     * @param def  默认列表
     * @return 双精度浮点数列表, 如果不存在则返回默认列表
     */
    default @Nullable List<Double> getDoubleList(@NotNull String path, @Nullable List<Double> def) {
        return this.getList(path, def, Double.class);
    }

    /**
     * 获取指定路径的双精度浮点数列表
     *
     * @param path 路径
     * @return 双精度浮点数列表, 如果不存在则返回一个空列表
     */
    @SuppressWarnings("DataFlowIssue")
    default @NotNull List<Double> getDoubleList(@NotNull String path) {
        return this.getDoubleList(path, new ArrayList<>());
    }

    /**
     * 获取指定路径的单精度浮点数列表
     *
     * @param path 路径
     * @param def  默认列表
     * @return 单精度浮点数列表, 如果不存在则返回默认列表
     */
    default @Nullable List<Float> getFloatList(@NotNull String path, @Nullable List<Float> def) {
        return this.getList(path, def, Float.class);
    }

    /**
     * 获取指定路径的单精度浮点数列表
     *
     * @param path 路径
     * @return 单精度浮点数列表, 如果不存在则返回一个空列表
     */
    @SuppressWarnings("DataFlowIssue")
    default @NotNull List<Float> getFloatList(@NotNull String path) {
        return this.getFloatList(path, new ArrayList<>());
    }

    /**
     * 获取当前配置节点下的所有键
     *
     * @param deep 是否深度获取 (即包含所有子节点的键)
     * @return 键的集合
     */
    @NotNull Set<String> getKeys(boolean deep);

    /**
     * 获取当前配置节点下的所有直接子键 (非深度)
     *
     * @return 键的集合
     */
    default @NotNull Set<String> getKeys() {
        return this.getKeys(false);
    }

    /**
     * 获取指定路径下的子配置节点
     *
     * @param path 路径
     * @return 子配置节点, 如果路径不存在或不是一个配置节点则返回 null
     */
    default @Nullable ConfigurationSection getConfigurationSection(@NotNull String path) {
        SectionData data = this.getSectionData(path);
        if (data.getData() instanceof ConfigurationSection section) return section;
        if (!(data.getData() instanceof Map)) return null;

        MemoryConfiguration configuration = new MemoryConfiguration(this, this.getKey(path));
        configuration.data = data;
        return configuration;
    }

    /**
     * 获取指定路径下的配置节点列表
     *
     * @param path 路径
     * @return 配置节点列表
     */
    default @NotNull List<ConfigurationSection> getConfigurationSectionList(@NotNull String path) {
        List<ConfigurationSection> maps = this.getList(path, new ArrayList<>(), ConfigurationSection.class);
        assert maps != null;

        List<ConfigurationSection> result = new ArrayList<>();
        for (ConfigurationSection configuration : maps) {
            if (configuration == null) continue;
            result.add(configuration);
        }
        return result;
    }
}
