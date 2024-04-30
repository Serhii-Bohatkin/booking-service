package bookingservice.controller;

import bookingservice.dto.booking.BookingDto;
import bookingservice.dto.booking.BookingRequestDto;
import bookingservice.model.Booking;
import bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking management", description = "Endpoints for managing bookings")
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a booking")
    public BookingDto create(@RequestBody @Valid BookingRequestDto requestDto) {
        return bookingService.create(requestDto);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Find all bookings by user id and status",
            description = "Find all bookings by: user id and status or user id only. If "
                    + "parameters are not specified, all bookings will be found.")
    public List<BookingDto> getAllByUserIdAndStatus(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Booking.Status status,
            Pageable pageable) {
        return bookingService.getAllByUserIdAndStatus(userId, status, pageable);
    }

    @GetMapping("/my")
    @Operation(summary = "Find all bookings of the current user")
    public List<BookingDto> getAllForCurrentUser(Pageable pageable) {
        return bookingService.getAllForCurrentUser(pageable);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Find a booking by id")
    public BookingDto getById(@PathVariable Long id) {
        return bookingService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user's booking by id")
    public BookingDto update(@PathVariable Long id,
                             @RequestBody @Valid BookingRequestDto requestDto) {
        return bookingService.updateBooking(id, requestDto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete booking by id")
    public void delete(@PathVariable Long id) {
        bookingService.delete(id);
    }
}
