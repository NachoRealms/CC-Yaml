package cn.chengzhimeow.ccyaml.manager;

import cn.chengzhimeow.ccyaml.CCYaml;
import cn.chengzhimeow.ccyaml.configuration.yaml.YamlConfiguration;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public abstract class AbstractYamlManager {
    private final @NotNull CCYaml instance;
    private @Nullable YamlConfiguration data;
    private @Nullable File file;

    public AbstractYamlManager(@NotNull CCYaml instance) {
        this.instance = instance;
    }

    /**
     * 源文件路径字符串
     */
    abstract public String originFilePath();

    /**
     * 文件路径字符串
     */
    abstract public String filePath();

    /**
     * 获取文件实例
     *
     * @return 文件实例
     */
    public File getFile() {
        if (this.file == null)
            this.file = new File(this.instance.getParent(), this.filePath());
        return this.file;
    }

    /**
     * 保存默认文件
     */
    public void saveDefaultFile() {
        this.instance.getFileManager().saveResource(this.originFilePath(), this.filePath(), false);
    }

    /**
     * 保存文件
     */
    @SneakyThrows
    public void save() {
        this.getData().save(this.getFile());
    }

    /**
     * 更新配置
     */
    @SneakyThrows
    public void update() {
        String version = this.instance.getVersion();
        String configVersion = this.getData().getString(this.instance.getConfigVersionKey());

        // 版本相同不处理
        if (configVersion != null && configVersion.equals(version)) return;

        URL url = this.instance.getClassLoader().getResource(this.originFilePath());
        if (url == null) return;

        try (InputStream in = url.openStream()) {
            YamlConfiguration originConfig = YamlConfiguration.loadConfiguration(in);
            Set<String> originConfigKeys = originConfig.getKeys(true);
            Set<String> configKeys = this.getData().getKeys(true);

            // 过滤完全存在的键
            originConfigKeys.removeAll(configKeys);

            // 过滤有父级的键
            {
                Set<String> filteredKeys = new HashSet<>();
                for (String key : originConfigKeys) {
                    int lastDotIndex = key.lastIndexOf('.');
                    if (lastDotIndex == -1) filteredKeys.add(key);
                    else {
                        String parentKey = key.substring(0, lastDotIndex);
                        if (configKeys.contains(parentKey)) filteredKeys.add(key);
                    }
                }
                originConfigKeys = filteredKeys;
            }

            forKey:
            for (String key : originConfigKeys) {
                String[] keyParts = key.split("\\.");
                StringBuilder prefixBuilder = new StringBuilder();

                // 绕过部分配置项使其不被更新加入
                for (int i = 0; i < keyParts.length; i++) {
                    String part = keyParts[i];

                    if (i > 0) prefixBuilder.append('.');
                    prefixBuilder.append(part);

                    List<String> prefixComments = originConfig.getCommentList(prefixBuilder.toString());
                    if (prefixComments.contains("!noUpdate")) continue forKey;
                }

                // 更新配置值
                this.getData().set(key, originConfig.get(key));

                // 更新注释
                List<String> comments = originConfig.getCommentList(prefixBuilder.toString());
                if (comments.isEmpty()) continue;
                this.getData().setCommentList(key, comments);
            }

            this.getData().set(this.instance.getConfigVersionKey(), version);
            this.getData().save(this.getFile());
        }
    }

    /**
     * 重载配置
     */
    @SneakyThrows
    public void reload() {
        this.data = YamlConfiguration.loadConfiguration(this.getFile());
    }

    /**
     * 获取配置实例
     *
     * @return 配置实例
     */
    public @NotNull YamlConfiguration getData() {
        if (this.data == null) this.reload();
        return this.data;
    }
}
