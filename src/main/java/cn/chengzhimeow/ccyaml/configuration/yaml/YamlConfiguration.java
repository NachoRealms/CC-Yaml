package cn.chengzhimeow.ccyaml.configuration.yaml;

import cn.chengzhimeow.ccyaml.configuration.MemoryConfiguration;
import cn.chengzhimeow.ccyaml.configuration.SectionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.MappingNode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class YamlConfiguration extends MemoryConfiguration {
    /**
     * 默认加载配置实例
     */
    public static @NotNull LoaderOptions defaultLoaderOptions() {
        LoaderOptions loaderOptions = new LoaderOptions();
        loaderOptions.setMaxAliasesForCollections(Integer.MAX_VALUE);
        loaderOptions.setCodePointLimit(Integer.MAX_VALUE);
        loaderOptions.setNestingDepthLimit(100);
        loaderOptions.setProcessComments(true);

        return loaderOptions;
    }

    /**
     * 默认加载输出实例
     */
    public static @NotNull DumperOptions defaultDumperOptions() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setWidth(Integer.MAX_VALUE);
        dumperOptions.setProcessComments(true);
        dumperOptions.setPrettyFlow(true);
        dumperOptions.setSplitLines(false);
        dumperOptions.setIndent(2);

        return dumperOptions;
    }

    /**
     * 从 Reader 加载配置文件
     *
     * @param reader 配置文件读取实例
     * @return 加载完成的 YamlConfiguration 实例
     */
    public static @NotNull YamlConfiguration loadConfiguration(@NotNull Reader reader) {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(reader);
        return configuration;
    }

    /**
     * 从 InputStream 加载配置文件
     *
     * @param inputStream 配置文件输入流实例
     * @return 加载完成的 YamlConfiguration 实例
     */
    public static @NotNull YamlConfiguration loadConfiguration(@NotNull InputStream inputStream) {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(inputStream);
        return configuration;
    }

    /**
     * 从 File 加载配置文件
     *
     * @param file 配置文件文件实例
     * @return 加载完成的 YamlConfiguration 实例
     * @throws IOException 如果文件读取失败
     */
    public static @NotNull YamlConfiguration loadConfiguration(@NotNull File file) throws IOException {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(file);
        return configuration;
    }

    /**
     * 检查指定集合实例不为空
     *
     * @param collection 集合实例
     * @return 结果
     */
    public static boolean isNotNullAndEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }
    public final @NotNull LoaderOptions loaderOptions;
    public final @NotNull DumperOptions dumperOptions;
    private final @NotNull YamlConstructor constructor;
    private final @NotNull YamlRepresenter representer;
    private final @NotNull Yaml yaml;

    public YamlConfiguration(@NotNull LoaderOptions loaderOptions, @NotNull DumperOptions dumperOptions, @NotNull YamlConstructor constructor, @NotNull YamlRepresenter representer) {
        super(null, "");

        this.loaderOptions = loaderOptions;
        this.dumperOptions = dumperOptions;
        this.constructor = constructor;
        this.representer = representer;

        this.yaml = new Yaml(this.constructor, this.representer, this.dumperOptions, this.loaderOptions);
    }

    public YamlConfiguration(@NotNull LoaderOptions loaderOptions, @NotNull DumperOptions dumperOptions, @NotNull YamlConstructor constructor) {
        this(loaderOptions, dumperOptions, constructor, new YamlRepresenter(dumperOptions));
    }

    public YamlConfiguration(@NotNull LoaderOptions loaderOptions, @NotNull DumperOptions dumperOptions, @NotNull YamlRepresenter representer) {
        this(loaderOptions, dumperOptions, new YamlConstructor(loaderOptions), representer);
    }

    public YamlConfiguration(@NotNull LoaderOptions loaderOptions, @NotNull DumperOptions dumperOptions) {
        this(loaderOptions, dumperOptions, new YamlConstructor(loaderOptions));
    }

    public YamlConfiguration(@NotNull DumperOptions dumperOptions) {
        this(YamlConfiguration.defaultLoaderOptions(), dumperOptions);
    }

    public YamlConfiguration(@NotNull LoaderOptions loaderOptions) {
        this(loaderOptions, YamlConfiguration.defaultDumperOptions());
    }

    public YamlConfiguration() {
        this(YamlConfiguration.defaultLoaderOptions());
    }

    /**
     * 从 Reader 加载配置文件
     *
     * @param reader 配置文件读取实例
     */
    public void load(@NotNull Reader reader) {
        MappingNode node = (MappingNode) this.yaml.compose(reader);
        if (node != null) this.data = this.constructor.mappingNodeToSectionData(node);
    }

    /**
     * 从 InputStream 加载配置文件
     *
     * @param inputStream 配置文件输入流实例
     */
    public void load(@NotNull InputStream inputStream) {
        try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
            this.load(reader);
        } catch (IOException e) {
            throw new RuntimeException("无法从输入流加载 YAML", e);
        }
    }

    /**
     * 从 File 加载配置文件
     *
     * @param file 配置文件文件实例
     * @throws IOException 如果文件读取失败
     */
    public void load(@NotNull File file) throws IOException {
        if (!file.exists()) throw new FileNotFoundException("找不到文件: " + file.getPath());

        try (FileInputStream fis = new FileInputStream(file)) {
            this.load(fis);
        }
    }

    /**
     * 将配置数据保存到文件
     *
     * @param file 目标文件实例
     * @throws IOException 如果文件写入失败
     */
    public void save(@NotNull File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null) Files.createDirectories(parent.toPath());

        SectionData sectionData = this.data;
        assert sectionData.getData() != null;
        // noinspection unchecked
        MappingNode node = this.representer.mapToMappingNode((Map<String, SectionData>) sectionData.getData());
        node.setBlockComments(this.representer.getCommentLines(sectionData.getCommentList(), CommentType.BLOCK));
        node.setInLineComments(this.representer.getCommentLines(sectionData.getInlineCommentList(), CommentType.IN_LINE));
        node.setEndComments(this.representer.getCommentLines(sectionData.getEndCommentList(), CommentType.BLOCK));

        StringWriter stringWriter = new StringWriter();
        if (!YamlConfiguration.isNotNullAndEmpty(node.getBlockComments()) || !YamlConfiguration.isNotNullAndEmpty(node.getEndComments()) || !YamlConfiguration.isNotNullAndEmpty(node.getValue())) {
            if (node.getValue().isEmpty()) node.setFlowStyle(DumperOptions.FlowStyle.FLOW);
            this.yaml.serialize(node, stringWriter);
        }
        String text = stringWriter.toString();

        String[] lines = text.split("\n");
        List<Integer> replaceLines = this.representer.getFoldLineList();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (replaceLines.contains(i)) {
                    line = line.replace("|", ">");
                }
                writer.write(line);
                writer.newLine();
            }
        }

        this.representer.getFoldLineList().clear();
    }

    /**
     * 获取尾部块注释
     *
     * @return 注释列表
     */
    public @NotNull List<String> getEndCommentList() {
        return this.getData().getEndCommentList();
    }
}
