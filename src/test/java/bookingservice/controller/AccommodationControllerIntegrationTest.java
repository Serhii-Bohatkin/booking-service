package bookingservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bookingservice.dto.accommodation.AccommodationDto;
import bookingservice.dto.accommodation.AccommodationRequestDto;
import bookingservice.model.Accommodation;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.org.apache.commons.lang3.builder.EqualsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccommodationControllerIntegrationTest {
    public static final Long ACCOMMODATION_ID = 1L;
    public static final String SQL_SCRIPT_BEFORE_TEST
            = "classpath:database/add-data-for-accommodation-tests.sql";
    public static final String SQL_SCRIPT_AFTER_TEST
            = "classpath:database/delete-data-for-accommodation-tests.sql";
    private static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void createAccommodation_validAccommodationRequestDto_returnAccommodationResponseDto()
            throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(createAccommodationRequestDto());
        AccommodationDto expected = createAccommodationDto();
        MvcResult result = mockMvc.perform(post("/accommodations")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        AccommodationDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), AccommodationDto.class);
        EqualsBuilder.reflectionEquals(actual, expected, "id");
    }

    @Test
    @WithMockUser
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void getAll_ShouldReturnTwoAccommodation() throws Exception {
        List<AccommodationDto> expected = new ArrayList<>();
        expected.add(createAccommodationDto());
        expected.add(createSecondAccommodationDto());
        MvcResult result = mockMvc.perform(get("/accommodations/all"))
                .andExpect(status().isOk())
                .andReturn();
        AccommodationDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(), AccommodationDto[].class);
        assertThat(actual).hasSize(2);
        assertThat(Arrays.stream(actual).toList()).isEqualTo(expected);
    }

    @Test
    @WithMockUser
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void getById_ValidId_Success() throws Exception {
        AccommodationDto expected = createAccommodationDto();
        MvcResult result = mockMvc.perform(get("/accommodations/" + expected.id()))
                .andExpect(status().isOk())
                .andReturn();
        AccommodationDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), AccommodationDto.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @WithMockUser
    public void getById_NonExistingAccommodationId_Exception() throws Exception {
        MvcResult result = mockMvc.perform(get("/accommodations/" + Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andReturn();
        String actual = result.getResponse().getContentAsString();
        assertThat(actual).contains("Accommodation with id " + Long.MAX_VALUE + " not found");
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void update_ValidId_ShouldReturnAccommodationDto() throws Exception {
        AccommodationRequestDto requestDto = createAccommodationRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        AccommodationDto expected = createAccommodationDto();
        MvcResult result = mockMvc.perform(put("/accommodations/" + ACCOMMODATION_ID)
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        AccommodationDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), AccommodationDto.class);
        EqualsBuilder.reflectionEquals(actual, expected, "id");
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    public void update_InvalidId_Exception() throws Exception {
        AccommodationRequestDto requestDto = createAccommodationRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);
        MvcResult result = mockMvc.perform(put("/accommodations/" + Long.MAX_VALUE)
                        .content(jsonRequest).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn();
        String actual = result.getResponse().getContentAsString();
        assertThat(actual).contains("Accommodation with id " + Long.MAX_VALUE + " not found");
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void delete_ValidId_NoContent() throws Exception {
        mockMvc.perform(delete("/accommodations/" + ACCOMMODATION_ID))
                .andExpect(status().isNoContent());
    }

    private AccommodationRequestDto createAccommodationRequestDto() {
        return new AccommodationRequestDto(
                Accommodation.Type.HOUSE,
                "Lviv, Shevchenko St. 12",
                "Studio, 1 Bedroom",
                List.of("golf courses"),
                BigDecimal.valueOf(15),
                10
        );
    }

    private AccommodationDto createAccommodationDto() {
        return new AccommodationDto(
                ACCOMMODATION_ID,
                Accommodation.Type.HOUSE,
                "Lviv, Shevchenko St. 12",
                "Studio, 1 Bedroom",
                List.of("golf courses"),
                BigDecimal.valueOf(15),
                10
        );
    }

    private AccommodationDto createSecondAccommodationDto() {
        return new AccommodationDto(
                2L,
                Accommodation.Type.APARTMENT,
                "Lviv, Perekopska St. 1",
                "Studio, 1 Bedroom",
                List.of("health club facilities"),
                BigDecimal.valueOf(30),
                5
        );
    }
}
