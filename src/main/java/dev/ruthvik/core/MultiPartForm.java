package dev.ruthvik.core;

import dev.ruthvik.exception.BadRequestException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.Optional;

public class MultiPartForm {
    private final Map<String, String> formFields;
    private final Map<String, FileItem> fileFields;

    MultiPartForm (Map<String,String> formFields, Map<String,FileItem> fileFields){
        this.formFields = formFields;
        this.fileFields = fileFields;
    }

    public Optional<String> getFormField(String key) {
        return Optional.ofNullable(formFields.get(key));
    }

    public String getFormFieldOrThrow(String key) {
        String value = formFields.get(key);
        if (value == null) {
            throw new BadRequestException("Missing required form field: " + key);
        }
        return value;
    }

    public Map<String,FileItem> getAllFiles(){
        return this.fileFields;
    }

    public Optional<FileItem> getFile(String key) {
        return Optional.ofNullable(fileFields.get(key));
    }

    public FileItem getFileOrThrow(String key) {
        FileItem fileItem = fileFields.get(key);
        if (fileItem == null) {
            throw new BadRequestException("Missing required file upload: " + key);
        }
        return fileItem;
    }

    @Setter(AccessLevel.PACKAGE)
    @Getter
    @ToString
    public static class FileItem {
        private String fileName;
        private String contentType;
        private byte[] content;
    }
}
