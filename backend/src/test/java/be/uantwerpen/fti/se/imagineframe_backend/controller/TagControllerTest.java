package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.model.Product;
import be.uantwerpen.fti.se.imagineframe_backend.model.Tag;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.TagAddDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.TagRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagRepository tagRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "email@test.be")
    public void testGetAllTags() throws Exception {
        Tag tag1 = new Tag("tag1");
        Tag tag2 = new Tag("tag2");

        when(tagRepository.findAll()).thenReturn(List.of(tag1, tag2));

        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("tag1"))
                .andExpect(jsonPath("$[1].name").value("tag2"));

        verify(tagRepository, times(1)).findAll();
    }

    @Test
    @WithMockUser(username = "email@test.be", authorities = "product_write")
    void testAddTag_Success() throws Exception {
        TagAddDto tagDTO = new TagAddDto();
        tagDTO.setName("Tag1");

        when(tagRepository.findByName("Tag1")).thenReturn(Optional.empty());

        mockMvc.perform(post("/tags/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Tag Tag1 added successfully"));

        verify(tagRepository, times(1)).save(any(Tag.class));
    }

    @Test
    @WithMockUser(username = "email@test.be", authorities = "product_write")
    void testAddTag_NotSuccess() throws Exception {
        Tag tag = new Tag("Tag1");

        when(tagRepository.findByName("Tag1")).thenReturn(Optional.of(tag));

        TagAddDto tagDTO = new TagAddDto();
        tagDTO.setName("Tag1");

        mockMvc.perform(post("/tags/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tagDTO)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Tag with name Tag1 already exists"));

        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    @WithMockUser(username = "email@test.be")
    void testReturnProductsWithTagId_Found() throws Exception {
        Long tagId = 1L;
        Tag tag = new Tag("Tag1");

        Product product = new Product("Pepsi", "Refreshment", new HashMap<>(), "");
        Set<Product> products = new HashSet<>();
        products.add(product);
        tag.setProducts(products);

        when(tagRepository.findById(tagId)).thenReturn(Optional.of(tag));

        mockMvc.perform(get("/tags/id/{id}", tagId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Pepsi"));

        verify(tagRepository, times(1)).findById(tagId);
    }

    @Test
    @WithMockUser(username = "email@test.be", authorities = "product_write")
    void testReturnProductsWithTagId_NotFound() throws Exception {
        Long tagId = 1L;

        when(tagRepository.findById(tagId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/tags/id/{id}", tagId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("No product found with tag ID " + tagId));

        verify(tagRepository, times(1)).findById(tagId);
    }
}
