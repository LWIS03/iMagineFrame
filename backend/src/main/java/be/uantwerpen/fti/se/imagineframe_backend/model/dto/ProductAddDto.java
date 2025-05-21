package be.uantwerpen.fti.se.imagineframe_backend.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductAddDto {
    private String name;
    private String description;
    private String imageUrl;
    private Map<String, String> properties;
    private List<Long> tagIds;
}
