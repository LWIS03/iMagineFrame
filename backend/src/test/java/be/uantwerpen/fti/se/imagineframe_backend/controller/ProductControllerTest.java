package be.uantwerpen.fti.se.imagineframe_backend.controller;

import be.uantwerpen.fti.se.imagineframe_backend.exceptionHandling.GlobalExceptionHandler;
import be.uantwerpen.fti.se.imagineframe_backend.model.Product;
import be.uantwerpen.fti.se.imagineframe_backend.model.Tag;
import be.uantwerpen.fti.se.imagineframe_backend.model.User;
import be.uantwerpen.fti.se.imagineframe_backend.model.dto.ProductAddDto;
import be.uantwerpen.fti.se.imagineframe_backend.repository.ProductRepository;
import be.uantwerpen.fti.se.imagineframe_backend.repository.TagRepository;
import be.uantwerpen.fti.se.imagineframe_backend.security.UrlSecurity;
import be.uantwerpen.fti.se.imagineframe_backend.service.FileStorageService;
import be.uantwerpen.fti.se.imagineframe_backend.service.ProductService;
import be.uantwerpen.fti.se.imagineframe_backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ProductService productService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private UserService userService;

    @Mock
    private UrlSecurity urlSecurity;

    private Product product;

    @InjectMocks
    private ProductController productController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ProductControllerTest(ProductRepository productRepository, TagRepository tagRepository, ProductService productService, FileStorageService fileStorageService, UserService userService, UrlSecurity urlSecurity) {
        this.productRepository = productRepository;
        this.tagRepository = tagRepository;
        this.productService = productService;
        this.fileStorageService = fileStorageService;
        this.userService = userService;
        this.urlSecurity = urlSecurity;
    }

    @BeforeEach
    void setUp() {
        Map<String, String> properties = new HashMap<>();
        properties.put("sweetness", "8");
        properties.put("Taste", "Cola");
        objectMapper.registerModule(new JavaTimeModule());

        product = new Product("Dr_Pepper", "Refreshment", properties, "");

        mockMvc = MockMvcBuilders.standaloneSetup(productController).setControllerAdvice(GlobalExceptionHandler.class).build();
    }

    @Test
    public void testAddProduct() throws Exception {
        MockMultipartFile productJson = new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(product));
        MockMultipartFile imageFile = new MockMultipartFile("image", "image.jpg", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/products/add")
                        .file(productJson)
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(content().string("Product added successfully"))
                .andExpect(status().isOk());

    }

    @Test
    public void testGetProductById() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dr_Pepper"));

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetProductByIdNotFound() throws Exception {
        when(productRepository.findById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/id/2"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("product not found: 2"));
    }

    @Test
    public void testGetProducts() throws Exception {
        when(productRepository.findAll()).thenReturn(Collections.singletonList(product));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dr_Pepper"));
    }

    @Test
    public void testDeleteProductById() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(delete("/products/id/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("1 deleted successfully"));

        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteProductByIdNotFound() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/products/id/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("product not found: 1"));
    }

    @Test
    public void testGetProductByName() throws Exception {
        when(productRepository.findByName("Dr_Pepper")).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/name/Dr_Pepper"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dr_Pepper"));
    }

    @Test
    public void testGetProductByNameNotFound() throws Exception {
        when(productRepository.findByName("Dr_Pepper")).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/name/Dr_Pepper"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("product not found: Dr_Pepper"));
    }

    @Test
    public void testDeleteProductByName() throws Exception {
        when(productRepository.findByName("Dr_Pepper")).thenReturn(Optional.of(product));

        mockMvc.perform(delete("/products/name/Dr_Pepper"))
                .andExpect(status().isOk())
                .andExpect(content().string("Dr_Pepper deleted successfully"));
    }

    @Test
    public void testDeleteProductByNameNotFound() throws Exception {
        when(productRepository.findByName("Dr_Pepper")).thenReturn(Optional.empty());

        mockMvc.perform(delete("/products/name/Dr_Pepper"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("product not found: Dr_Pepper"));
    }

    @Test
    public void testAddTagToProduct_ProductNotFound() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/products/1/addTag/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("product not found: 1"));
    }

    @Test
    public void testAddTagToProduct_TagNotFound() throws Exception {
        Product product = new Product("Dr_Pepper", "Refreshment", new HashMap<>(), "");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/products/1/addTag/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("tag not found: 1"));
    }

    @Test
    public void testRemoveTagFromProduct_Success() throws Exception {
        Product product = new Product("Dr_Pepper", "Refreshment", new HashMap<>(), "");
        Tag tag = new Tag("SoftDrink");

        product.addTag(tag);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        mockMvc.perform(delete("/products/1/removeTag/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tag removed successfully from product"));

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testRemoveTagFromProduct_ProductNotFound() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/products/1/removeTag/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("product not found: 1"));
    }

    @Test
    public void testRemoveTagFromProduct_TagNotFound() throws Exception {
        Product product = new Product("Dr_Pepper", "Refreshment", new HashMap<>(), "");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(tagRepository.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/products/1/removeTag/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("tag not found: 1"));
    }

    @Test
    public void testRemoveTagFromProduct_TagNotAssociated() throws Exception {
        Product product = new Product("Dr_Pepper", "Refreshment", new HashMap<>(), "");
        Tag tag = new Tag("SoftDrink");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        mockMvc.perform(delete("/products/1/removeTag/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("tag in product not found: Product ID: 1, Tag ID: 1"));
    }

    @Test
    public void testGetProductImage_Success() throws Exception {

        String fileName = "test-image.jpg";
        Path uploadsDir = Paths.get("uploads");
        Files.createDirectories(uploadsDir);
        Path filePath = uploadsDir.resolve(fileName);
        byte[] imageContent = "fake image content".getBytes();
        Files.write(filePath, imageContent);


        product.setImageUrl(fileName);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/1/image"))
                .andExpect(status().isOk())
                .andExpect(content().bytes(imageContent))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));

        Files.deleteIfExists(filePath);
    }

    @Test
    public void testGetProductImage_FileNotFound() throws Exception {
        product.setImageUrl("non-existent.jpg");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        mockMvc.perform(get("/products/1/image"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Could not read file: non-existent.jpg"));
    }

    @Test
    public void testEditProduct_Success() throws Exception {
        ReflectionTestUtils.setField(product, "productId", 1L);
        product.setName("Dr_Pepper");
        product.setDescription("Refreshment");

        ProductAddDto productAddDto = new ProductAddDto();
        productAddDto.setName("Dr_Pepper Updated");
        productAddDto.setDescription("Updated Description");
        productAddDto.setTagIds(Collections.singletonList(1L));
        productAddDto.setImageUrl("updated-image.jpg");

        String productJson = objectMapper.writeValueAsString(productAddDto);
        MockMultipartFile productPart = new MockMultipartFile("product", "", "application/json", productJson.getBytes());

        Tag tag = new Tag("SoftDrink");
        ReflectionTestUtils.setField(tag, "id", 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        mockMvc.perform(multipart("/products/1")
                        .file(productPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Product updated successfully"));

        verify(productService).updateProductInformation(eq(product), any(ProductAddDto.class));
        verify(productService).saveProduct(product);
    }

    @Test
    public void testEditProduct_WithImageUrl_Success() throws Exception {
        ReflectionTestUtils.setField(product, "productId", 1L);

        ProductAddDto productAddDto = new ProductAddDto();
        productAddDto.setName("Updated Name");
        productAddDto.setDescription("Updated Description");
        productAddDto.setImageUrl("updated-image.jpg");
        productAddDto.setTagIds(Collections.singletonList(1L));

        Tag tag = new Tag("SoftDrink");
        ReflectionTestUtils.setField(tag, "id", 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        String json = objectMapper.writeValueAsString(productAddDto);
        MockMultipartFile productPart = new MockMultipartFile("product", "", "application/json", json.getBytes());

        mockMvc.perform(multipart("/products/1")
                        .file(productPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Product updated successfully"));

        verify(productService).updateProductInformation(eq(product), any(ProductAddDto.class));
        verify(productService).saveProduct(product);
    }

    @Test
    public void testEditProduct_WithImageFile_Success() throws Exception {
        ReflectionTestUtils.setField(product, "productId", 1L);

        ProductAddDto productAddDto = new ProductAddDto();
        productAddDto.setName("Updated Name");
        productAddDto.setDescription("Updated Description");
        productAddDto.setTagIds(Collections.singletonList(1L));

        Tag tag = new Tag("SoftDrink");
        ReflectionTestUtils.setField(tag, "id", 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(fileStorageService.storeFile(any())).thenReturn("stored-image.jpg");

        String json = objectMapper.writeValueAsString(productAddDto);
        MockMultipartFile productPart = new MockMultipartFile("product", "", "application/json", json.getBytes());
        MockMultipartFile imagePart = new MockMultipartFile("image", "image.jpg", "image/jpeg", "fake image content".getBytes());

        mockMvc.perform(multipart("/products/1")
                        .file(productPart)
                        .file(imagePart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Product updated successfully"));

        verify(productService).updateProductInformation(eq(product), any(ProductAddDto.class));
        verify(fileStorageService).storeFile(any());
        verify(productService).saveProduct(product);
    }

    @Test
    public void testEditProduct_ProductNotFound() throws Exception {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        ProductAddDto dto = new ProductAddDto();
        dto.setName("Updated Name");

        String json = objectMapper.writeValueAsString(dto);
        MockMultipartFile productPart = new MockMultipartFile("product", "", "application/json", json.getBytes());

        mockMvc.perform(multipart("/products/1")
                        .file(productPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound())
                .andExpect(content().string("product not found: 1"));
    }

    @Test
    public void testEditProduct_TagNotFound() throws Exception {
        ReflectionTestUtils.setField(product, "productId", 1L);

        ProductAddDto dto = new ProductAddDto();
        dto.setName("Updated Name");
        dto.setTagIds(Collections.singletonList(999L)); // Non-existent tag

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(tagRepository.findById(999L)).thenReturn(Optional.empty());

        String json = objectMapper.writeValueAsString(dto);
        MockMultipartFile productPart = new MockMultipartFile("product", "", "application/json", json.getBytes());

        mockMvc.perform(multipart("/products/1")
                        .file(productPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound())
                .andExpect(content().string("tag not found: 999"));
    }

    @Test
    public void testEditProduct_NoImageProvided() throws Exception {
        ReflectionTestUtils.setField(product, "productId", 1L);

        ProductAddDto dto = new ProductAddDto();
        dto.setName("No Image Update");
        dto.setDescription("No image");
        dto.setTagIds(Collections.singletonList(1L));

        Tag tag = new Tag("NoImageTag");
        ReflectionTestUtils.setField(tag, "id", 1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));

        String json = objectMapper.writeValueAsString(dto);
        MockMultipartFile productPart = new MockMultipartFile("product", "", "application/json", json.getBytes());

        mockMvc.perform(multipart("/products/1")
                        .file(productPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Product updated successfully"));

        verify(productService).updateProductInformation(eq(product), any(ProductAddDto.class));
        verify(productService).saveProduct(product);
        verify(fileStorageService, never()).storeFile(any());
    }

    @Test
    public void testEditProduct_NoTagsProvided() throws Exception {
        ReflectionTestUtils.setField(product, "productId", 1L);

        ProductAddDto dto = new ProductAddDto();
        dto.setName("No Tags Update");
        dto.setDescription("No tags");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        String json = objectMapper.writeValueAsString(dto);
        MockMultipartFile productPart = new MockMultipartFile("product", "", "application/json", json.getBytes());

        mockMvc.perform(multipart("/products/1")
                        .file(productPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Product updated successfully"));

        verify(productService).updateProductInformation(eq(product), any(ProductAddDto.class));
        verify(productService).saveProduct(product);
        verify(tagRepository, never()).findById(any());
    }

    @Test
    @WithMockUser(username = "testuser", authorities = {"product_manager"})
    public void testGetProductStockReportUrl() throws Exception {
        User mockUser = new User();
        mockUser.setUsername("testuser");

        when(userService.findUser("testuser")).thenReturn(mockUser);
        when(urlSecurity.createUrlToken(mockUser)).thenReturn("secure-token");

        mockMvc.perform(get("/products/report"))
                .andExpect(status().isOk())
                .andExpect(content().string("/api/files/report/secure-token"));

        verify(userService, times(1)).findUser("testuser");
        verify(urlSecurity, times(1)).createUrlToken(mockUser);
    }
}
