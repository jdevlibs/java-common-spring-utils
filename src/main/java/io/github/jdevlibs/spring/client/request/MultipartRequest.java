package io.github.jdevlibs.spring.client.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author supot.jdev
 * @version 1.0
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MultipartRequest extends FormRequest {
    private Map<String, FilePart> files;

    public void addFile(String name, FilePart value) {
        if (name == null || name.isEmpty()) {
            return;
        }
        if (files == null) {
            files = new HashMap<>();
        }
        files.put(name, value);
    }

    @Override
    public void clearForm() {
        super.clearForm();
        files = null;
    }

    @Data
    @ToString(of = {"name", "path"})
    public static class FilePart implements Serializable {

        private String name;
        private String path;
        private byte[] contents;

        public boolean isFileByPath() {
            return (path != null && !path.isEmpty() && contents == null);
        }

        public boolean isFileBinary() {
            return (contents != null && contents.length > 0);
        }
    }
}