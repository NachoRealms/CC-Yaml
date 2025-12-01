package cn.chengzhimeow.ccyaml.configuration.yaml;

import cn.chengzhimeow.ccyaml.configuration.*;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class YamlRepresenter extends Representer {
    @Getter
    private final @NotNull List<Integer> foldLineList = new ArrayList<>();

    public YamlRepresenter(DumperOptions options) {
        super(options);
        this.representers.put(StringSection.class, new StringSectionDataRepresenter(this));
        this.representers.put(StringSectionData.class, new StringSectionDataRepresenter(this));
        this.representers.put(YamlStringSectionData.class, new StringSectionDataRepresenter(this));

        this.representers.put(ConfigurationSection.class, new ConfigurationSectionRepresenter(this));
        this.representers.put(MemoryConfiguration.class, new ConfigurationSectionRepresenter(this));
        this.representers.put(YamlConfiguration.class, new ConfigurationSectionRepresenter(this));
    }

    /**
     * 将字符串注释列表转换为 SnakeYAML 的 CommentLine 列表
     *
     * @param comments    字符串注释列表
     * @param commentType 注释类型 (BLOCK, IN_LINE)
     * @return CommentLine 列表
     */
    public @NotNull List<CommentLine> getCommentLines(@NotNull List<String> comments, @NotNull CommentType commentType) {
        if (YamlConfiguration.isNotNullAndEmpty(comments)) return new ArrayList<>();

        List<CommentLine> lines = new ArrayList<>();
        for (String comment : comments) {
            // null 或空字符串表示一个空行注释
            lines.add(new CommentLine(
                    null,
                    null,
                    comment == null ? "" : " " + comment,
                    comment == null ? CommentType.BLANK_LINE : commentType
            ));
        }
        return lines;
    }


    /**
     * 将包含 SectionData 的 Map 递归转换为 SnakeYAML 的 MappingNode
     *
     * @param map 包含 SectionData 的 Map
     * @return 转换后的 MappingNode
     */
    protected @NotNull MappingNode mapToMappingNode(@NotNull Map<String, SectionData> map) {
        List<NodeTuple> tupleList = new ArrayList<>();

        for (Map.Entry<String, SectionData> entry : map.entrySet()) {
            Node keyNode = this.represent(entry.getKey());
            Node valueNode;

            SectionData sectionData = entry.getValue();
            Object data = sectionData.getData();
            if (data instanceof Map<?, ?> v) // noinspection unchecked
                valueNode = this.mapToMappingNode((Map<String, SectionData>) v);
            else valueNode = this.represent(data);

            // 应用注释
            List<String> commentList = sectionData.getCommentList();
            if (commentList.isEmpty()) keyNode.setBlockComments(null);
            else keyNode.setBlockComments(this.getCommentLines(commentList, CommentType.BLOCK));

            List<String> inlineCommentList = sectionData.getInlineCommentList();
            if (valueNode instanceof MappingNode || valueNode instanceof SequenceNode)
                keyNode.setInLineComments(this.getCommentLines(inlineCommentList, CommentType.IN_LINE));
            else valueNode.setInLineComments(this.getCommentLines(inlineCommentList, CommentType.IN_LINE));

            tupleList.add(new NodeTuple(keyNode, valueNode));
        }

        return new MappingNode(Tag.MAP, tupleList, DumperOptions.FlowStyle.BLOCK);
    }

    private record StringSectionDataRepresenter(
            YamlRepresenter representer
    ) implements Represent {
        @Override
        public Node representData(Object o) {
            StringSection styledString = (StringSection) o;
            String value = styledString.getValue();

            if (styledString instanceof YamlStringSectionData yamlStringSectionData) {
                if (value != null && yamlStringSectionData.node().getScalarStyle() == DumperOptions.ScalarStyle.FOLDED) {
                    this.representer.foldLineList.add(yamlStringSectionData.node().getStartMark().getLine());
                    return this.representer.representScalar(Tag.STR, value.replace(" ", "\n"), DumperOptions.ScalarStyle.LITERAL);
                } else
                    return this.representer.representScalar(Tag.STR, value, yamlStringSectionData.node().getScalarStyle());
            } else return this.representer.representScalar(Tag.STR, value, DumperOptions.ScalarStyle.PLAIN);
        }
    }

    private record ConfigurationSectionRepresenter(
            YamlRepresenter representer
    ) implements Represent {
        @Override
        public Node representData(Object o) {
            ConfigurationSection section = (ConfigurationSection) o;
            // noinspection DataFlowIssue
            return representer.mapToMappingNode((Map<String, SectionData>) section.getData().getData());
        }
    }
}
