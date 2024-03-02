package bookingservice.service.impl;

import bookingservice.model.Booking;
import java.util.function.Predicate;

class BookingPredicate implements Predicate<Booking> {
    @Override
    public boolean test(Booking booking) {
        return booking.getStatus() == Booking.Status.CONFIRMED
                || booking.getStatus() == Booking.Status.PENDING;
    }
}
