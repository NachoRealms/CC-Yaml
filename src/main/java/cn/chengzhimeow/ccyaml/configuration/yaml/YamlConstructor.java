package cn.chengzhimeow.ccyaml.configuration.yaml;

import cn.chengzhimeow.ccyaml.configuration.MemoryConfiguration;
import cn.chengzhimeow.ccyaml.configuration.SectionData;
import cn.chengzhimeow.ccyaml.configuration.StringSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class YamlConstructor extends SafeConstructor {
    public YamlConstructor(LoaderOptions loaderOptions) {
        super(loaderOptions);
        this.yamlConstructors.put(Tag.STR, new StringConstructor());
        this.yamlConstructors.put(Tag.MAP, new MapConstructor(this));
    }

    /**
     * 将 SnakeYAML 的 CommentLine 列表转换为字符串列表
     *
     * @param comments CommentLine 列表
     * @return 字符串注释列表
     */
    public @NotNull List<String> getCommentLines(List<CommentLine> comments) {
        if (YamlConfiguration.isNotNullAndEmpty(comments)) return new ArrayList<>();

        List<String> lines = new ArrayList<>();
        for (CommentLine comment : comments) {
            String line = comment.getValue();
            // 如果不是空行, 则去除前导空格, 否则添加 null 以表示空行
            lines.add(comment.getCommentType() != CommentType.BLANK_LINE ?
                      (line.startsWith(" ") ? line.substring(1) : line) : null
            );
        }
        return lines;
    }

    /**
     * 将 SnakeYAML 的 MappingNode 递归转换为 SectionData 结构
     *
     * @param root MappingNode 根节点
     * @return 转换后的 SectionData
     */
    protected @NotNull SectionData mappingNodeToSectionData(@Nullable MappingNode root) {
        Map<String, SectionData> map = new LinkedHashMap<>();
        if (root == null) return new SectionData(map);

        this.flattenMapping(root);
        for (NodeTuple tuple : root.getValue()) {
            String keyString = String.valueOf(this.constructObject(tuple.getKeyNode()));
            Node valueNode = tuple.getValueNode();

            // 处理锚点
            while (valueNode instanceof AnchorNode) {
                valueNode = ((AnchorNode) valueNode).getRealNode();
            }

            SectionData sectionData;
            if (valueNode instanceof MappingNode mappingNode)
                sectionData = this.mappingNodeToSectionData(mappingNode);
            else sectionData = new SectionData(this.constructObject(valueNode));

            // 读取注释
            sectionData.setCommentList(this.getCommentLines(tuple.getKeyNode().getBlockComments()));
            if (valueNode instanceof MappingNode || valueNode instanceof SequenceNode)
                sectionData.setInlineCommentList(this.getCommentLines(tuple.getKeyNode().getInLineComments()));
            else sectionData.setInlineCommentList(this.getCommentLines(valueNode.getInLineComments()));

            map.put(keyString, sectionData);
        }

        SectionData data = new SectionData(map);
        data.setCommentList(this.getCommentLines(root.getBlockComments()));
        data.setInlineCommentList(this.getCommentLines(root.getInLineComments()));
        data.setEndCommentList(this.getCommentLines(root.getEndComments()));

        return data;
    }

    private static class StringConstructor extends AbstractConstruct {
        @Override
        public Object construct(Node node) {
            ScalarNode scalarNode = (ScalarNode) node;
            return new YamlStringSectionData(scalarNode);
        }
    }

    private static class MapConstructor extends AbstractConstruct {
        private final YamlConstructor constructor;

        public MapConstructor(YamlConstructor constructor) {
            this.constructor = constructor;
        }

        @Override
        public Object construct(Node node) {
            MappingNode mappingNode = (MappingNode) node;
            Map<Object, Object> origin = node.isTwoStepsConstruction() ? constructor.createDefaultMap(mappingNode.getValue().size()) : constructor.constructMapping(mappingNode);
            Map<String, Object> out = new LinkedHashMap<>();
            origin.forEach((k, v) -> {
                if (k instanceof StringSection key) out.put(key.getValue(), v);
                else if (k instanceof String key) out.put(key, v);
            });
            MemoryConfiguration configuration = MemoryConfiguration.empty();
            configuration.getData().setData(out);
            return configuration;
        }
    }
}
