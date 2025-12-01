package cn.chengzhimeow.ccyaml.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
@Setter
@ToString
@SuppressWarnings("unused")
public class SectionData {
    /**
     * 从 Map 转换
     *
     * @param map 要转换的 Map
     * @return 转换后的 SectionData
     */
    public static @NotNull SectionData fromMap(@NotNull Map<Object, Object> map) {
        Map<String, SectionData> dataMap = new LinkedHashMap<>();

        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object keyObj = entry.getKey();
            String key;
            if (keyObj instanceof String s) key = s;
            else if (keyObj instanceof StringSection s) key = s.getValue();
            else continue;

            if (entry.getValue() instanceof SectionData value) dataMap.put(key, value);
            else if (entry.getValue() instanceof Map) // noinspection unchecked
                dataMap.put(key, SectionData.fromMap((Map<Object, Object>) entry.getValue()));
            else dataMap.put(key, new SectionData(entry.getValue()));
        }

        return new SectionData(dataMap);
    }
    private @Nullable Object data;
    private @NotNull List<String> commentList;
    private @NotNull List<String> inlineCommentList;
    private @NotNull List<String> endCommentList;

    public SectionData(@Nullable Object data) {
        if (data instanceof String s) data = new StringSectionData(s);
        this.data = data;
        this.commentList = new ArrayList<>();
        this.inlineCommentList = new ArrayList<>();
        this.endCommentList = new ArrayList<>();
    }

    public SectionData() {
        this(null);
    }

    public void setData(@Nullable Object data) {
        if (data instanceof SectionData value) this.data = value.data;
        else if (data instanceof Map) // noinspection unchecked
            this.data = SectionData.fromMap((Map<Object, Object>) data).data;
        else this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof SectionData target)) return false;
        return Objects.equals(this.data, target.data) &&
                this.commentList.equals(target.commentList) &&
                this.inlineCommentList.equals(target.inlineCommentList) &&
                this.endCommentList.equals(target.endCommentList);
    }
}
