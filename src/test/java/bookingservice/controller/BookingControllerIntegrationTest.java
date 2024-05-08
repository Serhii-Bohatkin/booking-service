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

import bookingservice.dto.booking.BookingDto;
import bookingservice.dto.booking.BookingRequestDto;
import bookingservice.model.Booking;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
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
public class BookingControllerIntegrationTest {
    public static final String SQL_SCRIPT_BEFORE_TEST
            = "classpath:database/add-data-for-accommodation-tests.sql";
    public static final String SQL_SCRIPT_AFTER_TEST
            = "classpath:database/delete-data-for-accommodation-tests.sql";
    public static final long BOOKING_ID = 1L;
    public static final long ACCOMMODATION_ID = 1L;
    public static final long USER_ID = 1L;
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
    @WithMockUser(username = "admin@gmail.com")
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void create_ValidRequestDto_ShouldReturnBookingDto() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(createBookingRequestDto());
        BookingDto expected = createBookingDto();
        MvcResult result = mockMvc.perform(
                        post("/bookings")
                                .content(jsonRequest)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
        BookingDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookingDto.class);
        EqualsBuilder.reflectionEquals(actual, expected, "id");
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void getAllByUserIdAndStatus_ValidIdAndStatus_ShouldReturnOneBookingDto()
            throws Exception {
        BookingDto expected = createBookingDto();
        MvcResult result = mockMvc.perform(get("/bookings?userId=1&status=CONFIRMED"))
                .andExpect(status().isOk())
                .andReturn();
        BookingDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookingDto[].class);
        assertThat(actual).hasSize(1);
        assertThat(actual[0]).isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void getAllByUserIdAndStatus_ValidIdWithoutStatus_ShouldReturnTwoBookingDto()
            throws Exception {
        MvcResult result = mockMvc.perform(get("/bookings?userId=" + USER_ID))
                .andExpect(status().isOk())
                .andReturn();
        BookingDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookingDto[].class);
        assertThat(actual).hasSize(2);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void getAllByUserIdAndStatus_WithoutIdAndWithoutStatus_ShouldReturnTwoBookingDto()
            throws Exception {
        MvcResult result = mockMvc.perform(get("/bookings"))
                .andExpect(status().isOk())
                .andReturn();
        BookingDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookingDto[].class);
        assertThat(actual).hasSize(2);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void getAllByUserIdAndStatus_NonExistingUserId_ShouldReturnEmptyList()
            throws Exception {
        MvcResult result = mockMvc.perform(get("/bookings?userId=" + Long.MAX_VALUE))
                .andExpect(status().isOk())
                .andReturn();
        BookingDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookingDto[].class);
        assertThat(actual).isEmpty();
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void getAllForCurrentUser_ShouldReturnTwoBookingDto()
            throws Exception {
        MvcResult result = mockMvc.perform(get("/bookings/my"))
                .andExpect(status().isOk())
                .andReturn();
        BookingDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookingDto[].class);
        assertThat(actual).hasSize(2);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void getById_ValidBookingId_ShouldReturnBookingDto()
            throws Exception {
        BookingDto expected = createBookingDto();
        MvcResult result = mockMvc.perform(get("/bookings/" + BOOKING_ID))
                .andExpect(status().isOk())
                .andReturn();
        BookingDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookingDto.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void getById_NonExistingBookingId_NotFound() throws Exception {
        mockMvc.perform(get("/bookings/" + Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void update_ValidIdAndRequestDto_ShouldReturnBookingDto() throws Exception {
        BookingDto expected = new BookingDto(
                BOOKING_ID,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                ACCOMMODATION_ID,
                USER_ID,
                Booking.Status.CONFIRMED);
        String jsonRequest = objectMapper.writeValueAsString(createBookingRequestDto());
        MvcResult result = mockMvc.perform(
                        put("/bookings/" + BOOKING_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest)
                )
                .andExpect(status().isOk())
                .andReturn();
        BookingDto actual = objectMapper.readValue(
                result.getResponse().getContentAsString(), BookingDto.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void update_NonExistingBookingId_NotFound() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(createBookingRequestDto());
        mockMvc.perform(
                        put("/bookings/" + Long.MAX_VALUE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void delete_ValidId_NoContent() throws Exception {
        mockMvc.perform(delete("/bookings/" + BOOKING_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = SQL_SCRIPT_BEFORE_TEST, executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = SQL_SCRIPT_AFTER_TEST, executionPhase = AFTER_TEST_METHOD)
    public void delete_NonExistingId_Exception() throws Exception {
        mockMvc.perform(delete("/bookings/" + Long.MAX_VALUE))
                .andExpect(status().isNotFound());
    }

    private BookingRequestDto createBookingRequestDto() {
        return new BookingRequestDto(
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                ACCOMMODATION_ID
        );
    }

    private BookingDto createBookingDto() {
        return new BookingDto(
                BOOKING_ID,
                LocalDate.of(2024, 4, 22),
                LocalDate.of(2024, 4, 23),
                ACCOMMODATION_ID,
                USER_ID,
                Booking.Status.CONFIRMED);
    }
}
